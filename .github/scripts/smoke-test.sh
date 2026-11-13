#!/usr/bin/env bash
# Prueba de humo de contract-api (ADR-0012, ADR-0014). La usan el job de verificación de GitHub Actions y las
# etapas de verificación y de despliegue del Jenkinsfile.
#   Uso: smoke-test.sh <url-base> <título del resumen>
#   Variables: ADMIN_USERNAME (por defecto admin), ADMIN_PASSWORD (obligatoria), IMAGE (informativa),
#              HEALTH_TIMEOUT en segundos (por defecto 120).
# Comprueba que la API responde UP (Flyway migró la base), que el administrador inicial inicia sesión
# (BR-USR-007) y que la autenticación JWT y la autorización por permisos funcionan de extremo a extremo.
set -euo pipefail

base_url="${1:?Falta la URL base}"
title="${2:-Prueba de humo}"
: "${ADMIN_PASSWORD:?Falta ADMIN_PASSWORD}"
username="${ADMIN_USERNAME:-admin}"
timeout="${HEALTH_TIMEOUT:-120}"
summary="${GITHUB_STEP_SUMMARY:-/dev/stdout}"

deadline=$((SECONDS + timeout))
until curl -fsS --max-time 10 "$base_url/actuator/health" 2>/dev/null | grep -q '"status":"UP"'; do
  if [ "$SECONDS" -ge "$deadline" ]; then
    echo "::error::La API no respondió UP en $base_url/actuator/health en $timeout s"
    exit 1
  fi
  sleep 5
done
echo "API lista en $base_url"

body=$(jq -nc --arg u "$username" --arg p "$ADMIN_PASSWORD" '{username: $u, password: $p}')
token=$(curl -fsS --max-time 30 -X POST "$base_url/api/v1/auth/login" \
  -H 'Content-Type: application/json' -d "$body" | jq -r '.accessToken') || token=""
if [ -z "$token" ] || [ "$token" = "null" ]; then
  echo "::error::El login de $username no devolvió un access token"
  exit 1
fi
# En GitHub Actions oculta el token en el log; en Jenkins no se imprime.
[ "${GITHUB_ACTIONS:-}" = true ] && echo "::add-mask::$token"

status() { curl -s -o /dev/null --max-time 30 -w '%{http_code}' "$@"; }
me=$(status -H "Authorization: Bearer $token" "$base_url/api/v1/auth/me")
employees=$(status -H "Authorization: Bearer $token" "$base_url/api/v1/employees")
anonymous=$(status "$base_url/api/v1/employees")

{
  echo "### $title"
  echo "URL: \`$base_url\`${IMAGE:+ · Imagen: \`$IMAGE\`}"
  echo ""
  echo "| Comprobación | Esperado | Obtenido |"
  echo "|---|:-:|:-:|"
  echo "| \`GET /actuator/health\` | UP | UP |"
  echo "| \`POST /api/v1/auth/login\` ($username) | token | token |"
  echo "| \`GET /api/v1/auth/me\` | 200 | $me |"
  echo "| \`GET /api/v1/employees\` con token | 200 | $employees |"
  echo "| \`GET /api/v1/employees\` sin token | 401 | $anonymous |"
} >> "$summary"

if [ "$me" != 200 ] || [ "$employees" != 200 ] || [ "$anonymous" != 401 ]; then
  echo "::error::La prueba de humo falló (me=$me, employees=$employees, sin token=$anonymous)"
  exit 1
fi
echo "Prueba de humo superada"
