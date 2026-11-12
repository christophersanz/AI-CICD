#!/usr/bin/env bash
# Etapa "Análisis SonarQube" del Jenkinsfile (ADR-0015). Prepara el SonarQube local (solo lo que falte) y analiza:
#   1. Espera a que SonarQube esté UP.
#   2. En el primer arranque cambia la contraseña por defecto de admin (admin/admin) por SONAR_ADMIN_PASSWORD.
#   3. Genera un token de análisis y lo guarda en $JENKINS_HOME/sonar/token (se reutiliza mientras sea válido).
#   4. Crea el proyecto y el Quality Gate "contract-api" (condiciones sobre todo el código, ver GATE_CONDITIONS)
#      y lo asigna al proyecto. Las condiciones se reescriben en cada ejecución: cambiarlas aquí basta.
#   5. Ejecuta sonar-maven-plugin con la cobertura JaCoCo de la etapa anterior y espera el Quality Gate:
#      si no se cumple, la etapa falla y no se construye ni se despliega la imagen.
#   Uso: sonar-analysis.sh   (variables: MVN, SONAR_HOST_URL, SONAR_ADMIN_PASSWORD, JENKINS_HOME)
set -euo pipefail

: "${MVN:?Falta MVN (lo define el Jenkinsfile)}"
: "${SONAR_HOST_URL:?Falta SONAR_HOST_URL}"
: "${SONAR_ADMIN_PASSWORD:?Falta SONAR_ADMIN_PASSWORD en docker/jenkins/.env}"
project=contract-api
gate=contract-api
state="${JENKINS_HOME:?}/sonar"

# Condiciones del Quality Gate, sobre todo el código (no solo el nuevo): "métrica operador umbral".
# Equivalen a cero bugs, cero vulnerabilidades, hotspots revisados, rating A de mantenibilidad y 80 % de cobertura.
GATE_CONDITIONS=(
  "bugs GT 0"
  "vulnerabilities GT 0"
  "security_hotspots_reviewed LT 100"
  "sqale_rating GT 1"
  "coverage LT 80"
)

api() { curl -fsS --max-time 30 -u "admin:$SONAR_ADMIN_PASSWORD" "$@"; }

# 1. Disponibilidad (el primer arranque de SonarQube tarda 1-2 minutos).
deadline=$((SECONDS + 300))
until curl -fsS --max-time 10 "$SONAR_HOST_URL/api/system/status" 2>/dev/null | grep -q '"status":"UP"'; do
  if [ "$SECONDS" -ge "$deadline" ]; then
    echo "SonarQube no respondió UP en $SONAR_HOST_URL en 300 s"
    exit 1
  fi
  sleep 5
done

# 2. Contraseña del administrador.
valid() { curl -fsS --max-time 10 -u "$1" "$SONAR_HOST_URL/api/authentication/validate" | grep -q '"valid":true'; }
if ! valid "admin:$SONAR_ADMIN_PASSWORD"; then
  if valid "admin:admin"; then
    echo "Primer arranque: cambiando la contraseña por defecto de admin"
    curl -fsS --max-time 30 -u admin:admin -X POST "$SONAR_HOST_URL/api/users/change_password" \
      --data-urlencode login=admin --data-urlencode previousPassword=admin \
      --data-urlencode "password=$SONAR_ADMIN_PASSWORD" >/dev/null
  else
    echo "SONAR_ADMIN_PASSWORD no coincide con la contraseña de admin en SonarQube (ver docker/jenkins/README.md)"
    exit 1
  fi
fi

# 3. Token de análisis.
mkdir -p "$state"
chmod 700 "$state"
token=$(cat "$state/token" 2>/dev/null || true)
if [ -z "$token" ] || ! valid "$token:"; then
  echo "Generando un token de análisis para Jenkins"
  token=$(api -X POST "$SONAR_HOST_URL/api/user_tokens/generate" \
    --data-urlencode "name=jenkins-$(date +%Y%m%d%H%M%S)" --data-urlencode type=GLOBAL_ANALYSIS_TOKEN | jq -r .token)
  (umask 077 && printf '%s' "$token" > "$state/token")
fi

# 4. Proyecto y Quality Gate.
if [ "$(api "$SONAR_HOST_URL/api/projects/search?projects=$project" | jq '.components | length')" = 0 ]; then
  api -X POST "$SONAR_HOST_URL/api/projects/create" \
    --data-urlencode "project=$project" --data-urlencode "name=$project" --data-urlencode mainBranch=master >/dev/null
fi
if ! api "$SONAR_HOST_URL/api/qualitygates/show?name=$gate" >/dev/null 2>&1; then
  api -X POST "$SONAR_HOST_URL/api/qualitygates/create" --data-urlencode "name=$gate" >/dev/null
fi
for id in $(api "$SONAR_HOST_URL/api/qualitygates/show?name=$gate" | jq -r '.conditions[]?.id'); do
  api -X POST "$SONAR_HOST_URL/api/qualitygates/delete_condition" --data-urlencode "id=$id" >/dev/null
done
for condition in "${GATE_CONDITIONS[@]}"; do
  read -r metric op error <<<"$condition"
  api -X POST "$SONAR_HOST_URL/api/qualitygates/create_condition" --data-urlencode "gateName=$gate" \
    --data-urlencode "metric=$metric" --data-urlencode "op=$op" --data-urlencode "error=$error" >/dev/null
done
api -X POST "$SONAR_HOST_URL/api/qualitygates/select" \
  --data-urlencode "gateName=$gate" --data-urlencode "projectKey=$project" >/dev/null

# 5. Análisis y espera del Quality Gate. El token va por variable de entorno, no en la línea de comandos.
export SONAR_TOKEN="$token"
$MVN org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
  -Dsonar.host.url="$SONAR_HOST_URL" \
  -Dsonar.projectKey="$project" -Dsonar.projectName="$project" \
  -Dsonar.qualitygate.wait=true -Dsonar.qualitygate.timeout=300 || {
    echo "Quality Gate no superado: revisa http://localhost:9000/dashboard?id=$project"
    api "$SONAR_HOST_URL/api/qualitygates/project_status?projectKey=$project" \
      | jq -r '.projectStatus.conditions[] | "  \(.status)  \(.metricKey): \(.actualValue // "-") (umbral \(.comparator) \(.errorThreshold))"' || true
    exit 1
  }

echo "Quality Gate superado:"
api "$SONAR_HOST_URL/api/qualitygates/project_status?projectKey=$project" \
  | jq -r '.projectStatus.conditions[] | "  \(.status)  \(.metricKey): \(.actualValue // "-") (umbral \(.comparator) \(.errorThreshold))"'
echo "Informe: http://localhost:9000/dashboard?id=$project"
