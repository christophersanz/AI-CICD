# Documentación técnica

Referencia del **estado implementado** de `contract-api` (octubre de 2026). Describe lo que existe hoy en el código: API, seguridad, configuración, base de datos, build y despliegue.

- El diseño y las reglas de arquitectura están en [architecture.md](architecture.md).
- Las reglas de negocio están en [business-rules.md](business-rules.md).
- El diseño de seguridad completo y sus fases están en [design/003-security-design.md](design/003-security-design.md).
- Las guías con diagramas y explicaciones están en [guides/arquitectura.md](guides/arquitectura.md) y [guides/ci-cd.md](guides/ci-cd.md).

Si este documento y el código no coinciden, manda el código; actualiza este documento en el mismo cambio.

## 1. Estado de implementación

| Área | Dominio | Caso de uso | Persistencia | API REST |
|---|:-:|:-:|:-:|:-:|
| Empleados: registrar, actualizar, consultar, listar | ✅ | ✅ | ✅ | ✅ |
| Empleados: desactivar / reactivar (BR-EMP-005–008) | — | — | — | — |
| Identidad: login, refresh, logout, usuario actual | ✅ | ✅ | ✅ | ✅ |
| Identidad: crear usuario, administrador inicial | ✅ | ✅ | ✅ | ✅ |
| Contratos (crear, firmar, renovar, terminar, etc.) | ✅ | — | — | — |

- El agregado `Contract` (estados, eventos, value objects y excepciones `CTR-*`) existe y está probado en `domain`, pero no tiene casos de uso, tablas ni endpoints.
- La tabla `employees` no tiene columna de estado: todos los empleados se consideran `ACTIVE`.
- La seguridad está en la **fase 1** del diseño 003: autenticación completa; autorización por permisos solo en `/api/v1/users` (ver §3.3).

## 2. API REST

URL base local: `http://localhost:8081`. Documentación interactiva (solo fuera de `prod`): `/swagger-ui.html`; especificación OpenAPI: `/v3/api-docs`.

### 2.1 Endpoints

| Método y ruta | Caso de uso | Acceso | Respuestas |
|---|---|---|---|
| `POST /api/v1/auth/login` | `AuthenticateUserUseCase` | Público | `200`, `400`, `401` (AUTH-002) |
| `POST /api/v1/auth/refresh` | `RefreshTokenUseCase` | Público | `200`, `400`, `401` (AUTH-005) |
| `POST /api/v1/auth/logout` | `LogoutUseCase` | Autenticado | `204`, `400`, `401` |
| `GET /api/v1/auth/me` | `GetCurrentUserUseCase` | Autenticado | `200`, `401`, `404` (AUTH-404) |
| `POST /api/v1/users` | `CreateUserUseCase` | `user:manage` | `201` + `Location`, `400`, `401`, `403`, `404` (AUTH-009), `409` (AUTH-007), `422` (AUTH-008, VAL-001) |
| `POST /api/v1/employees` | `RegisterEmployeeUseCase` | Autenticado | `201` + `Location`, `400`, `409` (EMP-001), `422` (EMP-002, VAL-001) |
| `GET /api/v1/employees/{id}` | `GetEmployeeUseCase` | Autenticado | `200`, `400`, `404` (EMP-404) |
| `PUT /api/v1/employees/{id}` | `UpdateEmployeeUseCase` | Autenticado | `200`, `400`, `404` (EMP-404), `422` (VAL-001) |
| `GET /api/v1/employees` | `ListEmployeesUseCase` | Autenticado | `200`, `400` |
| `GET /actuator/health` | — | Público | `200` / `503` |
| `GET /actuator/info`, `/actuator/metrics` | — | `system:monitor` | `200`, `401`, `403` |

Todo endpoint autenticado responde `401` con AUTH-003 (token ausente o inválido) o AUTH-004 (token expirado). Cualquier ruta no declarada se deniega.

### 2.2 Cuerpos de petición y respuesta

Las validaciones de Bean Validation solo comprueban presencia, formato y tamaño máximo; las invariantes las valida el dominio y responden `422`.

**`LoginRequest`** — `username` (obligatorio, ≤ 100), `password` (obligatorio, ≤ 256). Un username con formato inválido responde igual que unas credenciales incorrectas (AUTH-002).

**`RefreshTokenRequest`** (refresh y logout) — `refreshToken` (obligatorio, ≤ 128).

**`TokenResponse`** — se devuelve con `Cache-Control: no-store`.

```json
{
  "accessToken": "eyJraWQiOi...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "refreshToken": "q8n3...",
  "refreshExpiresIn": 604800
}
```

**`CurrentUserResponse`** — `id`, `username`, `roles[]`, `permissions[]` (permisos efectivos vigentes en base de datos, no los del token).

**`CreateUserRequest`** — `username` (≤ 100), `password` (≤ 256), `roles[]` (cada uno ≤ 30). **`UserResponse`** — `id`, `username`, `roles[]`, `enabled`.

**`EmployeeRequest`** (alta) — `documentType` (`DNI` | `FOREIGNER_CARD`), `documentNumber` (≤ 20), `firstNames` (≤ 150), `lastNames` (≤ 150), `email` (formato de correo, ≤ 254).

**`UpdateEmployeeRequest`** — `firstNames`, `lastNames`, `email`. El documento no se modifica (BR-EMP-003).

**`EmployeeResponse`** — `id`, `documentType`, `documentNumber`, `firstNames`, `lastNames`, `email`.

### 2.3 Listado de empleados

`GET /api/v1/employees` acepta estos parámetros de consulta, todos opcionales:

| Parámetro | Regla | Por defecto |
|---|---|---|
| `documentType` + `documentNumber` | Búsqueda exacta. Se envían juntos. | — |
| `name` | Texto parcial (≤ 100) en nombres y apellidos, sin distinguir mayúsculas ni tildes. | — |
| `page` | ≥ 0 | `0` |
| `size` | 1–100 | `20` |
| `sort` | `lastNames`, `firstNames`, `documentNumber` o `email`, con `,asc` o `,desc` | `lastNames,asc` |

Combinar el filtro por documento con `name` responde `400` (BR-EMP-004). La respuesta es un `PageResponse`: `content[]`, `page`, `size`, `totalElements`, `totalPages`.

### 2.4 Formato de errores

Todos los errores usan RFC 9457 (`application/problem+json`). `GlobalExceptionHandler` y los manejadores de seguridad construyen el cuerpo así:

- `type`: `urn:contract-api:errors:<slug>`, donde el slug sale del nombre de la excepción (`DuplicateEmployeeDocumentException` → `duplicate-employee-document`).
- `code`: código de negocio del catálogo de [business-rules.md §8](business-rules.md#8-catálogo-de-errores-de-negocio).

| Excepción | HTTP |
|---|---|
| `DomainException` (excepto las de abajo) | `422` |
| `InvalidContractStatusTransitionException` | `409` |
| `InvalidRefreshTokenException` | `401` |
| `UnauthenticatedException` (p. ej. `InvalidCredentialsException`) | `401` |
| `NotFoundException` y subclases | `404` |
| `ConflictException` y subclases | `409` |
| Validación de entrada (`@Valid`) | `400`, con `errors[]` (`field`, `message`) y sin `code` |
| Cualquier otra | `500`, `internal-error`, sin detalle interno |

```json
{
  "type": "urn:contract-api:errors:invalid-request",
  "title": "Invalid request",
  "status": 400,
  "detail": "The request has invalid fields.",
  "errors": [{ "field": "email", "message": "must be a well-formed email address" }]
}
```

## 3. Seguridad

Diseño completo en [design/003-security-design.md](design/003-security-design.md) y decisión en [ADR-0005](adr/0005-incorporar-seguridad.md). Guía de prueba manual: [design/003-security-local-testing.md](design/003-security-local-testing.md).

### 3.1 Cadenas de filtros

`SecurityFilterChainConfig` define dos cadenas, ambas stateless, sin CSRF (el token viaja en la cabecera `Authorization`), sin sesión, sin login de formulario ni HTTP Basic:

1. **Pública** (`@Order(1)`): login, refresh, `/v3/api-docs/**`, `/swagger-ui/**`, `/actuator/health/**`.
2. **Privada** (`@Order(2)`): `/api/v1/users/**` exige `user:manage`; el resto de `/api/v1/**` exige estar autenticado; `/actuator/**` exige `system:monitor`; todo lo demás se deniega.

Los errores `401` y `403` de los filtros se escriben como Problem Details (`ProblemDetailsAuthenticationEntryPoint`, `ProblemDetailsAccessDeniedHandler`).

### 3.2 Tokens

| | Access token | Refresh token |
|---|---|---|
| Formato | JWT firmado RS256 (Nimbus) con `kid` | Valor opaco aleatorio |
| Vida | 15 min (`security.jwt.access-token-ttl`) | 7 días (`security.jwt.refresh-token-ttl`) |
| Almacenamiento | No se guarda | Solo su SHA-256 en `refresh_tokens.token_hash` |
| Uso | Cabecera `Authorization: Bearer` | Un solo uso; rota dentro de su familia (BR-USR-006) |

Claims del access token: `sub` (id del usuario), `iss`, `aud`, `iat`, `exp`, `jti`, `username`, `roles` (informativo) y `perms` (permisos efectivos). El claim `perms` se convierte en authorities sin prefijo (`user:manage`), que es lo que evalúa la autorización. La validación comprueba firma, expiración, `iss` y `aud`.

Contraseñas: hash BCrypt de coste 12 con prefijo de algoritmo (`{bcrypt}...`). Si el algoritmo o el coste configurados cambian, el hash se regenera en el siguiente login correcto.

### 3.3 Pendiente (fases 2–4 del diseño 003)

- Autorización por método con `@PreAuthorize` y `@EnableMethodSecurity`, en lugar de la regla de URL temporal de `/api/v1/users` (BR-CTR-011).
- El permiso `employee:read` figura en business-rules §4.1 pero aún no está en el seed `V3`; hace falta una migración nueva antes de exigirlo.
- Auditoría de eventos de seguridad y endurecimiento.

## 4. Configuración

### 4.1 Perfiles

| Perfil | Uso | Diferencias |
|---|---|---|
| `local` (por defecto) | Desarrollo | Base de datos de `docker-compose.yml` con valores por defecto; si no hay claves, genera un par RSA efímero (los tokens dejan de valer al reiniciar). |
| `prod` | Imagen Docker | Sin valores por defecto: si falta la base de datos o las claves JWT, la aplicación no arranca. Swagger y `/v3/api-docs` desactivados. |

No existe todavía un perfil `dev` con configuración propia.

### 4.2 Variables de entorno

| Variable | Obligatoria en `prod` | Por defecto (`local`) | Descripción |
|---|:-:|---|---|
| `DB_URL` | ✅ | `jdbc:postgresql://localhost:5432/contracts` | URL JDBC de PostgreSQL |
| `DB_USERNAME` | ✅ | `contract` | Usuario de la base de datos |
| `DB_PASSWORD` | ✅ | `contract` | Contraseña de la base de datos |
| `SECURITY_JWT_PUBLIC_KEY` | ✅ | clave efímera | PEM X.509 (`-----BEGIN PUBLIC KEY-----`) |
| `SECURITY_JWT_PRIVATE_KEY` | ✅ | clave efímera | PEM PKCS#8 (`-----BEGIN PRIVATE KEY-----`) |
| `SECURITY_JWT_KEY_ID` | | `contract-api-1` | `kid` de la cabecera del JWT |
| `SECURITY_JWT_ISSUER` | | `contract-api` | Claim `iss` |
| `SECURITY_JWT_AUDIENCE` | | `contract-api` | Claim `aud` |
| `SECURITY_BOOTSTRAP_ADMIN_USERNAME` | | — | Administrador inicial (BR-USR-007) |
| `SECURITY_BOOTSTRAP_ADMIN_PASSWORD` | | — | Debe cumplir BR-USR-003 |
| `SPRING_PROFILES_ACTIVE` | | `local` (`prod` en la imagen) | Perfil activo |

Las claves PEM y los archivos de variables están excluidos de git y del contexto de Docker; nunca se versionan.

Para generar un par de claves:

```bash
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out private.pem
openssl rsa -in private.pem -pubout -out public.pem
```

### 4.3 Administrador inicial

`BootstrapAdministratorRunner` ejecuta `BootstrapAdministratorUseCase` al arrancar. Si no existe ningún usuario `ADMIN` y las dos variables `SECURITY_BOOTSTRAP_ADMIN_*` están definidas, lo crea. Si faltan, solo registra un aviso. Si no puede crearlo (username ocupado por un usuario no `ADMIN` o contraseña débil), la aplicación no arranca.

### 4.4 Otras propiedades

| Propiedad | Valor |
|---|---|
| `server.port` | `8081` |
| `spring.threads.virtual.enabled` | `true` (virtual threads) |
| `spring.jpa.hibernate.ddl-auto` | `validate`: el esquema lo gestiona solo Flyway |
| `spring.jpa.open-in-view` | `false` |
| `security.password.bcrypt-strength` | `12` |

La RMV (`contract.minimum-wage`, ADR-0002) se configurará cuando se implemente `CreateContractUseCase`; hoy no se lee en ninguna parte.

## 5. Base de datos

PostgreSQL 16, esquema gestionado por Flyway (`infrastructure/src/main/resources/db/migration`).

| Migración | Contenido |
|---|---|
| `V1__create_employees.sql` | `employees`, con documento único (`document_type`, `document_number`) |
| `V2__create_identity.sql` | `roles`, `permissions`, `role_permissions`, `users`, `user_roles` |
| `V3__seed_roles_permissions.sql` | Roles `ADMIN`, `HR`, `EMPLOYEE`, permisos y su relación (sin usuarios) |
| `V4__create_refresh_tokens.sql` | `refresh_tokens` |

```mermaid
erDiagram
    users ||--o{ user_roles : tiene
    roles ||--o{ user_roles : asignado
    roles ||--o{ role_permissions : agrupa
    permissions ||--o{ role_permissions : incluido
    users ||--o{ refresh_tokens : posee
    refresh_tokens |o--o| refresh_tokens : replaced_by
    employees {
        uuid id PK
        varchar document_type
        varchar document_number
        varchar first_names
        varchar last_names
        varchar email
    }
    users {
        uuid id PK
        varchar username UK
        varchar password_hash
        boolean enabled
    }
    refresh_tokens {
        uuid id PK
        uuid user_id FK
        uuid family_id
        varchar token_hash UK
        timestamptz issued_at
        timestamptz expires_at
        timestamptz used_at
        uuid replaced_by FK
        timestamptz revoked_at
        varchar revocation_reason
    }
```

Restricciones relevantes:

- `users.username` debe estar en minúsculas (`CHECK`), lo que refuerza la unicidad sin distinguir mayúsculas (BR-USR-001).
- `refresh_tokens`: `expires_at > issued_at`, y `revoked_at` y `revocation_reason` van juntos. Borrar un usuario borra sus roles y tokens (`ON DELETE CASCADE`).
- No hay `users.employee_id`: el vínculo `User` ↔ `Employee` está pendiente (business-rules §9).

Una migración ya aplicada no se edita: los cambios van en una `V<n>` nueva.

## 6. Observabilidad

- **Logs:** formato estructurado ECS (JSON) en consola. Los DTOs con secretos (`LoginRequest`, `CreateUserRequest`, `RefreshTokenRequest`, `TokenResponse`) enmascaran esos campos en `toString()`.
- **Trazas:** Micrometer Tracing con el puente de OpenTelemetry; muestreo al 100 % (`management.tracing.sampling.probability`). No hay exportador configurado.
- **Actuator:** se exponen `health`, `info` y `metrics`. Solo `health` es público.

## 7. Build y pruebas

Proyecto Maven multimódulo (`contract-api-v2`, versión `0.1.0-SNAPSHOT`) sobre Spring Boot 3.5 y Java 21:

| Módulo | Depende de | Dependencias principales |
|---|---|---|
| `domain` | — | Ninguna (Java puro) |
| `application` | `domain` | `spring-tx` (solo `@Transactional`) |
| `infrastructure` | `application` | Web, Validation, Data JPA, Security, OAuth2 Resource Server, Actuator, Flyway, PostgreSQL, MapStruct, springdoc, Micrometer/OTel |

```bash
./mvnw clean verify           # lo mismo que el CI: compila, prueba y verifica la cobertura
./mvnw test -pl domain        # un módulo
```

`verify` falla si la cobertura de líneas de JaCoCo baja del 80 % en `domain` o `application`; `infrastructure` solo genera el informe. Las pruebas de `infrastructure` usan Testcontainers, así que necesitan Docker.

| Capa | Pruebas existentes |
|---|---|
| `domain` | Agregados (`Contract`, `Employee`, `User`, `RefreshToken`), value objects y `PasswordPolicy` |
| `application` | Unitarias de los diez casos de uso (logout y usuario actual comparten clase), con Mockito sobre los puertos de salida |
| `infrastructure/api` | `@WebMvcTest` de `AuthController`, `EmployeeController`, `UserController` |
| `infrastructure/persistence` | Adaptadores y `EmployeeSpecifications` contra PostgreSQL (Testcontainers) |
| `infrastructure/security` | Cadenas de filtros, JWT, emisión de tokens, hash de contraseñas y de refresh tokens |
| Extremo a extremo | `AuthenticationFlowIntegrationTest` (login → acceso → refresh → logout), `BootstrapAdministratorIntegrationTest` |

## 8. Entorno local

```bash
docker compose up -d                       # PostgreSQL (5432) y pgAdmin (http://localhost:5050)
./mvnw install -DskipTests
./mvnw spring-boot:run -pl infrastructure  # http://localhost:8081, perfil local
```

pgAdmin arranca sin pedir contraseña maestra y con el servidor ya registrado (`docker/pgadmin/servers.json`). Las credenciales por defecto de PostgreSQL son `contract` / `contract`; se pueden cambiar con `DB_PASSWORD`.

## 9. Imagen Docker y CI/CD

### 9.1 Imagen

El `Dockerfile` tiene dos etapas:

1. **Build** (`eclipse-temurin:21-jdk`): empaqueta con el Maven Wrapper sin pruebas ni JaCoCo (ya se ejecutaron en el CI) y extrae las capas de Spring Boot.
2. **Runtime** (`eclipse-temurin:21-jre`): copia las capas, se ejecuta con un usuario sin privilegios, perfil `prod`, `-XX:MaxRAMPercentage=75` y puerto `8081`.

### 9.2 Pipeline (GitHub Actions)

`.github/workflows/ci-cd.yml`, según [ADR-0009](adr/0009-ci-cd-github-actions.md). Uso paso a paso en la [guía de CI/CD](guides/ci-cd.md).

| Evento | `build` | `image` |
|---|---|---|
| Pull Request a `main` | `clean verify`, resumen de cobertura e informes | Construye la imagen sin publicarla |
| Push a `main` | Igual | Publica `latest` y `sha-<corto>` |
| Tag `vX.Y.Z` | Igual | Publica `X.Y.Z` y `X.Y` |

- Registros: `ghcr.io/galaxytraining/curs-000617-cicd` siempre; Docker Hub solo si el repositorio define la variable `DOCKERHUB_USERNAME` y el secreto `DOCKERHUB_TOKEN`.
- Los informes de Surefire y JaCoCo se guardan como artefacto `reports` durante 14 días.
- En los PR, un push nuevo cancela la ejecución anterior.

### 9.3 Jenkins local

`Jenkinsfile` reproduce las mismas dos etapas en un Jenkins local de práctica (`docker/jenkins/`, [ADR-0010](adr/0010-jenkins-local-de-practica.md)). No forma parte del flujo oficial.

### 9.4 Flujo de ramas

GitHub Flow: `main` protegida (`.github/ruleset/main-protegida.json`), ramas `feature/*`, `fix/*` o `docs/*`, Pull Request con el CI en verde y versión publicada con un tag `vX.Y.Z` sobre `main`.
