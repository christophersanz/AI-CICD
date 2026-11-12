#!/usr/bin/env bash
# Etapa "Verificación de la imagen" del Jenkinsfile (ADR-0014). Equivale al job homónimo de GitHub Actions:
# levanta la imagen recién construida con un PostgreSQL propio en una red temporal, ejecuta la prueba de humo
# y lo retira todo, pase o falle. Claves JWT y contraseña del administrador se generan en el momento.
#   Uso: verify-image.sh <imagen>
# Jenkins corre en un contenedor que usa el Docker del host: para llegar a la API se une a la red temporal.
set -euo pipefail

image="${1:?Falta la imagen}"
id="contract-api-verify-${BUILD_NUMBER:-manual}"
net="$id"
db="$id-db"
app="$id-app"
self="${JENKINS_CONTAINER:-contract-api-jenkins}"
work=$(mktemp -d)
scripts_dir="$(cd "$(dirname "$0")/../.." && pwd)/.github/scripts"

cleanup() {
  local rc=$?
  if [ "$rc" -ne 0 ]; then
    echo "--- Logs de $app ---"
    docker logs "$app" 2>&1 | tail -n 150 || true
  fi
  docker network disconnect "$net" "$self" >/dev/null 2>&1 || true
  docker rm -f "$app" "$db" >/dev/null 2>&1 || true
  docker network rm "$net" >/dev/null 2>&1 || true
  rm -rf "$work"
  exit "$rc"
}
trap cleanup EXIT

openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out "$work/private.pem" 2>/dev/null
openssl pkey -in "$work/private.pem" -pubout -out "$work/public.pem"
admin_password="Verify-$(openssl rand -hex 12)"
{
  echo "DB_URL=jdbc:postgresql://$db:5432/contracts"
  echo "DB_USERNAME=contract"
  echo "DB_PASSWORD=contract"
  echo "SECURITY_JWT_PUBLIC_KEY=$(awk '{printf "%s\\n", $0}' "$work/public.pem")"
  echo "SECURITY_JWT_PRIVATE_KEY=$(awk '{printf "%s\\n", $0}' "$work/private.pem")"
  echo "SECURITY_BOOTSTRAP_ADMIN_USERNAME=admin"
  echo "SECURITY_BOOTSTRAP_ADMIN_PASSWORD=$admin_password"
} > "$work/prod.env"

docker network create "$net" >/dev/null
docker run -d --name "$db" --network "$net" \
  -e POSTGRES_DB=contracts -e POSTGRES_USER=contract -e POSTGRES_PASSWORD=contract \
  postgres:16-alpine >/dev/null
until docker exec "$db" pg_isready -U contract -d contracts >/dev/null 2>&1; do sleep 2; done

docker run -d --name "$app" --network "$net" --env-file "$work/prod.env" "$image" >/dev/null
docker network connect "$net" "$self"

ADMIN_PASSWORD="$admin_password" IMAGE="$image" \
  "$scripts_dir/smoke-test.sh" "http://$app:8081" "Imagen verificada (Jenkins, entorno efímero)"
