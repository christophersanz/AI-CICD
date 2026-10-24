# Contrato de la API (OpenAPI 3.1)

| Archivo | Contenido |
|---|---|
| `openapi.yaml` | Contrato OpenAPI 3.1 en YAML (para leer, versionar y revisar en diffs) |
| `openapi.json` | El mismo contrato en JSON (para importar en Postman, Swagger Editor o generadores de clientes) |

El contrato **se genera desde el código** con springdoc-openapi (anotaciones `@Operation`, `@ApiResponse`
y `@Schema` de los controllers y DTOs de `infrastructure/api`). La fuente de verdad es el código: si cambia
un endpoint, se regenera este contrato en el mismo cambio.

## Endpoints

| Método | Ruta | Autenticación |
|---|---|---|
| `POST` | `/api/v1/auth/login` | Pública |
| `POST` | `/api/v1/auth/refresh` | Pública (refresh token en el cuerpo) |
| `POST` | `/api/v1/auth/logout` | Bearer |
| `GET` | `/api/v1/auth/me` | Bearer |
| `POST` | `/api/v1/users` | Bearer + `user:manage` |
| `POST` | `/api/v1/employees` | Bearer + `employee:create` |
| `GET` | `/api/v1/employees` | Bearer + `employee:read` |
| `GET` | `/api/v1/employees/{id}` | Bearer + `employee:read` |
| `PUT` | `/api/v1/employees/{id}` | Bearer + `employee:update` |

Los errores siguen RFC 9457 (`application/problem+json`) con el campo `code` de `docs/business-rules.md`.

## Consultarlo en vivo

Con el perfil `local` (no en `prod`, que desactiva springdoc):

- Swagger UI: <http://localhost:8081/swagger-ui.html>
- JSON: <http://localhost:8081/v3/api-docs>
- YAML: <http://localhost:8081/v3/api-docs.yaml>

El esquema `ProblemDetail` documenta `code` y `errors` en el nivel superior, como los devuelve la API
(`OpenApiConfig.problemDetailSchemaCustomizer`).

## Regenerarlo

`title`, `version`, `description` y `servers` se fijan al exportar, porque la API no define un bean
`OpenAPI` con esos datos. El YAML se obtiene convirtiendo el JSON ya ajustado, así los dos archivos
tienen el mismo contenido.

```bash
./mvnw -B -ntp install -DskipTests
docker exec contract-api-postgres psql -U contract -d postgres -c "CREATE DATABASE contracts_openapi OWNER contract"
DB_URL=jdbc:postgresql://localhost:5432/contracts_openapi \
SECURITY_BOOTSTRAP_ADMIN_USERNAME=admin SECURITY_BOOTSTRAP_ADMIN_PASSWORD=Temporal-OpenAPI-2026 \
  java -jar infrastructure/target/infrastructure-0.1.0-SNAPSHOT.jar --server.port=8085 &

curl -sf localhost:8085/v3/api-docs | jq '
  .info = {"title":"contract-api","version":"0.1.0","description":"API REST de gestión de contratos laborales. Contrato generado con springdoc-openapi desde el código (ver docs/api/README.md)."}
  | .servers = [{"url":"http://localhost:8081","description":"Local (docker compose / spring-boot:run)"}]' > docs/api/openapi.json
ruby -ryaml -rjson -e 'puts JSON.parse(File.read(ARGV[0])).to_yaml(line_width: -1).sub(/\A---\n/, "")' \
  docs/api/openapi.json > docs/api/openapi.yaml

kill %1
docker exec contract-api-postgres psql -U contract -d postgres -c "DROP DATABASE contracts_openapi"
```

Se usa una base temporal para que las migraciones de Flyway no toquen `contracts` ni `contracts_prod`.
