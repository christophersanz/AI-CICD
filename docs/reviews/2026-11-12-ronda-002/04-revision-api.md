# Revisión 002 · 4 — API REST

- **Prompt:** `docs/prompts/06-revisar.md`, modo `api` (paso 4 de 9 de la ronda 002)
- **Fecha:** 2026-11-12 · **Commit revisado:** `183d054` (`master`)
- **Alcance:** `infrastructure/api` (3 controllers, 11 DTO, 3 mappers, `GlobalExceptionHandler`), `docs/api/contract/openapi.*` y las pruebas `@WebMvcTest` (55 pruebas)

## 1. Resumen

La API cumple `architecture.md` §6 y §7:
- Los controllers solo usan puertos de entrada.
- Cada endpoint protegido exige su permiso de §4.1.
- Los códigos HTTP son los documentados.
- Los errores salen como RFC 9457 con `code`.
- El contrato OpenAPI tiene las mismas 9 operaciones que el código.

No hay ERROR. Hay un WARNING de documentación: `technical.md` dice en un lugar que la autorización por permisos solo existe en `/api/v1/users`, lo que contradice su propia §2.1 y §3 y el código.

**Build:** el de la ronda (paso 1) sobre `183d054`: BUILD SUCCESS (incluye las 55 pruebas web). **Quality Gate:** Jenkins #16, OK.

## 2. Comprobaciones

| # | Comprobación | Severidad | Archivo/componente | Evidencia | Regla o criterio | Corrección propuesta |
|---|---|---|---|---|---|---|
| 1 | Controllers solo con puertos de entrada, sin lógica de negocio | OK | `api/rest/*Controller.java` | No importan `persistence`, `security` ni `client`; el único import de `infrastructure` es `config.OpenApiConfig` (constantes de documentación). Mapean, invocan el `UseCase` y responden | `architecture.md` §3.9 | — |
| 2 | Request/Response separados del dominio, de la aplicación y de JPA | OK | `api/dto/*` (11) | `*Request` / `*Response` propios; ninguno expone entidades de dominio ni JPA | `architecture.md` §3.4 | — |
| 3 | Mappers sin lógica de negocio | OK | `AuthApiMapper`, `EmployeeApiMapper`, `UserApiMapper` | Solo traducen Request → Command/Query y Result → Response | `architecture.md` §4 | — |
| 4 | Bean Validation solo de formato | OK | `api/dto/*`, `GlobalExceptionHandler.handleMethodArgumentNotValid` | Valida presencia y formato; las invariantes siguen en el dominio | `architecture.md` §3.8 | — |
| 5 | `@PreAuthorize` con el permiso de §4.1 en cada endpoint | OK | `EmployeeController`, `UserController`, `AuthController` | `GET /employees` y `GET /employees/{id}` → `employee:read`; `POST /employees` → `employee:create`; `PUT /employees/{id}` → `employee:update`; `POST /users` → `user:manage`. `login` y `refresh` son públicos; `logout` y `me` solo exigen autenticación, por la cadena de filtros, como indica §4.1 («sin permiso explícito») | `business-rules.md` §4.1 | — |
| 6 | Rutas, verbos y códigos de §6 | OK | controllers | `/api/v1/...` en plural; `201` + `Location` en `POST /employees` y `POST /users`; `200` en las consultas, en `PUT` y en `login`/`refresh` (con `Cache-Control: no-store`); `204` en `logout` | `architecture.md` §6 | — |
| 7 | Errores RFC 9457 con `code`, sin detalles internos | OK | `GlobalExceptionHandler` | `type` (`urn:…:<slug>`), `title`, `detail`, `status` y `code`; `DomainException` → 422, salvo `CTR-008` → 409; `NotFound` → 404; `Conflict` → 409; no autenticado → 401; `AccessDeniedException` → 403; cualquier otra → 500 sin mensaje (probado en `EmployeeControllerTest.shouldReturnInternalErrorWithoutDetailsWhenUnexpectedExceptionOccurs`) | `architecture.md` §7 | — |
| 8 | OpenAPI completo y contrato al día | OK | controllers, `docs/api/contract/openapi.yaml` | `@Operation` en los 9 endpoints y `@ApiResponse` por código (12 + 21 + 7). El contrato tiene las mismas 9 operaciones, con sus códigos de error | `architecture.md` §6; `DP.openapi` | — |
| 9 | Pruebas `@WebMvcTest` de éxito y de cada error | OK | `AuthControllerTest` (15), `EmployeeControllerTest` (30), `UserControllerTest` (10) | Cubren 400, 401, 403 (por permiso con `@WithMockUser`), 404, 409, 422 y el 500 sin detalles | `architecture.md` §9 | — |
| 10 | Documentación de la API coherente con el código | WARNING | `docs/technical.md:24` | Dice: «autorización por permisos solo en `/api/v1/users` (ver §3.3)». La misma guía, en la línea 131 y en la tabla de §2.1, documenta `@PreAuthorize` también en los cuatro endpoints de empleados, igual que el código | `CLAUDE.md` (actualizar `technical.md` en el mismo cambio) | Ver W-05 |

## 3. Trazabilidad

| Regla | Implementación | Prueba | Estado |
|---|---|---|---|
| BR-CTR-011 (permisos de escritura) | — (no hay endpoints de contratos) | — | NOT COVERED: pendiente documentado (`technical.md` §1) |
| §4.1 `employee:*` | `@PreAuthorize` en `EmployeeController` | `EmployeeControllerTest` (403 por permiso) | PASS |
| §4.1 `user:manage` | `@PreAuthorize` en `UserController` | `UserControllerTest`, `AuthenticationFlowIntegrationTest.shouldCarryRolePermissionsInTokenAndForbidUserManagementWhenUserIsHr` | PASS |
| `AUTH-002` sin enumeración | `AuthController` → `AuthenticateUserUseCase` | `AuthControllerTest`, `AuthenticationFlowIntegrationTest.shouldRespondInvalidCredentialsWhenPasswordIsWrongOrUserDoesNotExist` | PASS |

## 4. Pruebas existentes y faltantes

- **Existentes:** 55 pruebas web, más las de integración de punta a punta en `AuthenticationFlowIntegrationTest`, que además verifican el esquema Bearer y `ProblemDetail` en el OpenAPI publicado.
- **Faltantes:** ninguna para los endpoints actuales.

## 5. Hallazgos

### ERROR
Ninguno.

### WARNING

**W-05 · `technical.md` se contradice sobre el alcance de la autorización**
- **Archivo:** `docs/technical.md:24`
- **Evidencia:** la línea 24 dice «La seguridad está en la **fase 1** del diseño 003: autenticación completa; autorización por permisos solo en `/api/v1/users` (ver §3.3)». La línea 131 y la tabla de §2.1 documentan `employee:create`, `employee:update` y `employee:read` con `@PreAuthorize` en `EmployeeController`, y así está en el código. La bitácora (`docs/ai-log.md` #8) registra el cambio.
- **Explicación:** la línea 24 quedó de antes de la entrada #8 de la bitácora. Quien lea §1 cree que los endpoints de empleados solo exigen estar autenticado.
- **Corrección propuesta:** en `technical.md` §1, reemplazar la frase por: «autorización por permisos con `@PreAuthorize` en todos los endpoints de empleados y usuarios (§2.1); pendiente: auditoría y endurecimiento (§3.3)».

### INFO

**I-02 · La tabla de §2.1 de `technical.md` no lista el `401` en los endpoints de empleados**, mientras que el contrato OpenAPI sí lo documenta. Agregarlo dejaría la tabla igual al contrato.

## 6. Aspectos positivos

- `Cache-Control: no-store` en las respuestas con tokens.
- El `type` de los errores se deriva del nombre de la excepción, así que es estable y no se repite a mano en cada manejador.
- El 500 no filtra el mensaje interno, y hay una prueba que lo garantiza.
- El contrato OpenAPI se prueba en la integración: esquema Bearer solo en las operaciones privadas y `code` en `ProblemDetail`.

## 7. Conclusión y siguiente paso

La API cumple las reglas obligatorias. W-05 es una frase desactualizada en la documentación técnica.

**Siguiente paso del orden:** `/06-revisar seguridad` (paso 5 de 9, ronda 002).
