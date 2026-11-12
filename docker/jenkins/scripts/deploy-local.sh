#!/usr/bin/env bash
# Etapa "Despliegue local" del Jenkinsfile (ADR-0014): deja contract-api corriendo en esta máquina, como un
# despliegue on-premise. A diferencia de la verificación, el entorno es persistente:
#   - red contract-api-deploy, PostgreSQL contract-api-deploy-db (volumen contract-api-deploy-data) y la API
#     contract-api-deploy, todos con --restart unless-stopped; la API publica 127.0.0.1:${DEPLOY_PORT:-8090}.
#   - claves JWT y contraseña de la base se generan una sola vez y se guardan en $JENKINS_HOME/contract-api-deploy
#     (volumen de Jenkins): los tokens siguen valiendo entre despliegues.
#   - la contraseña del administrador llega en DEPLOY_ADMIN_PASSWORD (docker/jenkins/.env); solo se usa al crear
#     la base por primera vez (BR-USR-007), cambiarla después no la actualiza.
# Tras desplegar ejecuta la prueba de humo; si falla, vuelve a levantar la imagen anterior y termina con error.
#   Uso: deploy-local.sh <imagen>
set -euo pipefail

image="${1:?Falta la imagen}"
: "${DEPLOY_ADMIN_PASSWORD:?Falta DEPLOY_ADMIN_PASSWORD en docker/jenkins/.env}"
port="${DEPLOY_PORT:-8090}"
net=contract-api-deploy
db=contract-api-deploy-db
app=contract-api-deploy
self="${JENKINS_CONTAINER:-contract-api-jenkins}"
state="${JENKINS_HOME:?}/contract-api-deploy"
scripts_dir="$(cd "$(dirname "$0")/../.." && pwd)/.github/scripts"

# 1. Configuración persistente (solo la primera vez).
mkdir -p "$state"
chmod 700 "$state"
if [ ! -f "$state/private.pem" ]; then
  echo "Generando las claves JWT del entorno local"
  openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out "$state/private.pem" 2>/dev/null
  openssl pkey -in "$state/private.pem" -pubout -out "$state/public.pem"
fi
[ -f "$state/db_password" ] || openssl rand -hex 16 > "$state/db_password"
db_password=$(cat "$state/db_password")
umask 077
{
  echo "DB_URL=jdbc:postgresql://$db:5432/contracts"
  echo "DB_USERNAME=contract"
  echo "DB_PASSWORD=$db_password"
  echo "SECURITY_JWT_PUBLIC_KEY=$(awk '{printf "%s\\n", $0}' "$state/public.pem")"
  echo "SECURITY_JWT_PRIVATE_KEY=$(awk '{printf "%s\\n", $0}' "$state/private.pem")"
  echo "SECURITY_BOOTSTRAP_ADMIN_USERNAME=admin"
  echo "SECURITY_BOOTSTRAP_ADMIN_PASSWORD=$DEPLOY_ADMIN_PASSWORD"
} > "$state/prod.env"

# 2. Red y base de datos (se crean una vez y se conservan).
docker network inspect "$net" >/dev/null 2>&1 || docker network create "$net" >/dev/null
if ! docker container inspect "$db" >/dev/null 2>&1; then
  echo "Creando la base de datos $db"
  docker run -d --name "$db" --network "$net" --restart unless-stopped \
    -e POSTGRES_DB=contracts -e POSTGRES_USER=contract -e POSTGRES_PASSWORD="$db_password" \
    -v contract-api-deploy-data:/var/lib/postgresql/data postgres:16-alpine >/dev/null
fi
docker start "$db" >/dev/null
until docker exec "$db" pg_isready -U contract -d contracts >/dev/null 2>&1; do sleep 2; done
docker network connect "$net" "$self" >/dev/null 2>&1 || true

# 3. Sustituir la API por la imagen nueva, recordando la anterior para volver atrás.
previous=$(docker container inspect -f '{{.Config.Image}}' "$app" 2>/dev/null || true)
run_api() {
  docker rm -f "$app" >/dev/null 2>&1 || true
  docker run -d --name "$app" --network "$net" --restart unless-stopped \
    -p "127.0.0.1:$port:8081" --env-file "$state/prod.env" "$1" >/dev/null
}
echo "Desplegando $image${previous:+ (reemplaza a $previous)}"
run_api "$image"

if ! ADMIN_PASSWORD="$DEPLOY_ADMIN_PASSWORD" IMAGE="$image" \
     "$scripts_dir/smoke-test.sh" "http://$app:8081" "Despliegue local (Jenkins)"; then
  echo "--- Logs de $app ---"
  docker logs "$app" 2>&1 | tail -n 150 || true
  if [ -n "$previous" ] && [ "$previous" != "$image" ] && docker image inspect "$previous" >/dev/null 2>&1; then
    echo "Volviendo a la imagen anterior: $previous"
    run_api "$previous"
  fi
  exit 1
fi

# 4. Conservar solo la imagen desplegada y la anterior (rollback); el resto de builds se borra.
docker images contract-api --format '{{.Repository}}:{{.Tag}}' | grep ':jenkins-' \
  | grep -vxF -e "$image" -e "${previous:-none}" | xargs -r docker rmi >/dev/null 2>&1 || true

echo "contract-api desplegado en http://localhost:$port (imagen $image)"
