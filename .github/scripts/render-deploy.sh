#!/usr/bin/env bash
# Despliega una imagen concreta en el servicio de Render y espera a que quede en vivo (ADR-0013).
#   Uso: render-deploy.sh <url-de-la-imagen>
#   Variables: RENDER_API_KEY (secreto del environment production), RENDER_SERVICE_ID (srv-...),
#              DEPLOY_TIMEOUT en segundos (por defecto 900).
# Usa la API de Render (https://api-docs.render.com): POST /services/{id}/deploys con imageUrl y después
# consulta el estado del deploy hasta "live". Falla si Render lo marca como fallido o cancelado.
set -euo pipefail

image="${1:?Falta la URL de la imagen}"
: "${RENDER_API_KEY:?Falta el secreto RENDER_API_KEY}"
: "${RENDER_SERVICE_ID:?Falta la variable RENDER_SERVICE_ID}"
timeout="${DEPLOY_TIMEOUT:-900}"
api="https://api.render.com/v1/services/$RENDER_SERVICE_ID"

render() {
  curl -fsS --max-time 30 -H "Authorization: Bearer $RENDER_API_KEY" -H 'Accept: application/json' "$@"
}

echo "Solicitando el despliegue de $image"
response=$(render -X POST "$api/deploys" -H 'Content-Type: application/json' \
  -d "$(jq -nc --arg image "$image" '{imageUrl: $image}')")
deploy_id=$(jq -r '.id // empty' <<<"$response")
# Si Render encola el deploy detrás de otro puede no devolver el cuerpo: se toma el más reciente.
if [ -z "$deploy_id" ]; then
  deploy_id=$(render "$api/deploys?limit=1" | jq -r '.[0].deploy.id // empty')
fi
[ -n "$deploy_id" ] || { echo "::error::Render no devolvió el id del despliegue"; exit 1; }
echo "DEPLOY_ID=$deploy_id" >> "${GITHUB_ENV:-/dev/null}"
echo "Deploy $deploy_id creado"

deadline=$((SECONDS + timeout))
previous=""
while :; do
  status=$(render "$api/deploys/$deploy_id" | jq -r '.status')
  [ "$status" != "$previous" ] && echo "Estado: $status" && previous="$status"
  case "$status" in
    live) echo "Deploy $deploy_id en vivo"; exit 0 ;;
    build_failed|update_failed|pre_deploy_failed|canceled|deactivated)
      echo "::error::El deploy $deploy_id terminó en $status (revisa los logs del servicio en Render)"
      exit 1 ;;
  esac
  if [ "$SECONDS" -ge "$deadline" ]; then
    echo "::error::El deploy $deploy_id no quedó en vivo en $timeout s (último estado: $status)"
    exit 1
  fi
  sleep 10
done
