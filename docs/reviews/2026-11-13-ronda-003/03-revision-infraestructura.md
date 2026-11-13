# Revisión 003 · 3 — Infraestructura

- **Prompt:** `docs/prompts/06-revisar.md`, modo `infraestructura` (paso 3 de 9)
- **Fecha:** 2026-11-13 · **Commit revisado:** `027f37f` (`master`)
- **Alcance:** `infrastructure` sin `api`: `persistence`, `security`, `config`, migraciones, `application*.yml` y sus pruebas.
- **Informes de la ronda:** `01-revision-dominio.md`, `02-revision-aplicacion.md`

## 1. Resumen

**`infrastructure` no cambió desde la ronda 002:** `git diff 26fdf64 027f37f -- infrastructure` está vacío. Igual volví a comprobar cada punto contra el código actual, sin dar por bueno el informe anterior.
- Cada puerto de salida tiene un adaptador.
- Las entidades JPA y los repositorios no salen de `persistence`.
- Las 5 migraciones no se editaron desde su commit de creación.
- Los secretos llegan por variables de entorno.
- Las pruebas de persistencia corren contra PostgreSQL real con Testcontainers.

No hay ERROR. El único WARNING es W-04 de la ronda 002, **diferido deliberadamente**, que no se vuelve a reportar como defecto. Hay 1 INFO de documentación.

**Build:** se reutiliza el del paso 1 (`01-revision-dominio.md`), del mismo commit `027f37f` y sin cambios de código. Resultado: BUILD SUCCESS; `infrastructure`, 154 pruebas (99 fuera de `api`), 0 fallidas.

**Último Quality Gate:** OK en Jenkins #26 sobre `027f37f`.

## 2. Comprobaciones

| # | Comprobación | Severidad | Archivo/componente | Evidencia | Regla o criterio | Corrección propuesta |
|---|---|---|---|---|---|---|
| 1 | Cada puerto de salida tiene su adaptador; `persistence`, `client` y `security` no dependen de `api` | OK | `persistence/adapter/*`, `security/*` | Los 7 puertos tienen exactamente una implementación: `Employee`, `User`, `Role` y `RefreshToken` en `…PersistenceAdapter`; `CredentialsAuthenticatorAdapter`, `PasswordHasherAdapter` y `TokenIssuerAdapter`. Ningún import de `infrastructure.api` en esos paquetes | `architecture.md` §3.7 y §3.9 | — |
| 2 | Entidades JPA separadas y mappers sin lógica | OK | `persistence/entity/*JpaEntity`, `persistence/mapper/*` | 4 entidades `…JpaEntity`. Los 3 mappers no tienen `if` ni `throw` y reconstruyen con `restore(...)`, así que las invariantes las valida el dominio | `architecture.md` §3.4 y §3.5 | — |
| 3 | Repositorios como mecanismo técnico | OK | `persistence/repository/*` | No hay referencias a `JpaEntity` ni a `JpaRepository` fuera de `persistence` | `architecture.md` §2 | — |
| 4 | Migraciones coherentes y sin editar | OK | `db/migration/V1`–`V5` | Únicos: `uk_employees_document` (BR-EMP-001), `uk_users_username` (BR-USR-001), `uk_refresh_tokens_token_hash`. Checks: `ck_users_username_lowercase`, `ck_refresh_tokens_expiration` y `ck_refresh_tokens_revocation`. Claves foráneas en línea (`REFERENCES users`, `roles`, `permissions`, `refresh_tokens`). Índices `ix_refresh_tokens_family_id`, `ix_refresh_tokens_user_id` e `ix_user_roles_role_name`. Cada migración tiene **un solo commit** (`git log`), así que ninguna se editó después de aplicarse | `business-rules.md` §3 y §4 | — |
| 5 | Adaptadores de seguridad sin reglas de negocio | OK | `security/authn`, `security/crypto`, `security/jwt` | Solo lanzan errores técnicos de arranque (`IllegalStateException` por claves o algoritmos) y `InvalidCredentialsException` en `CredentialsAuthenticatorAdapter.java:34` y `:41`, que unifica las causas en `AUTH-002`, como pide §8. Las reglas BR-USR-004 y 006 están en `application` | `architecture.md` §3.8 | El análisis de seguridad es del paso 5 |
| 6 | Configuración: perfiles, secretos, `open-in-view`, logs | OK | `application.yml`, `-local.yml`, `-prod.yml` | `open-in-view: false`; `ddl-auto: validate`; `DB_*` y `SECURITY_JWT_*` por variable de entorno, sin valores por defecto salvo en `local`, donde son las credenciales del PostgreSQL de `docker-compose.yml`; logs estructurados ECS (`logging.structured.format.console: ecs`). Perfiles: `local` y `prod` (ver I-08) | `architecture.md` §8 | — |
| 7 | Rendimiento: N+1, `EAGER`, paginación | OK | `UserJpaEntity:31`, `RoleJpaEntity:26`, `EmployeePersistenceAdapter:55-60` | Los 2 `@ElementCollection(fetch = EAGER)` son conjuntos pequeños de nombres (los roles de un usuario y los permisos de un rol). El listado de empleados pagina con `PageRequest.of(page, size, sort)` hasta `MAX_PAGE_SIZE = 100`. No hay `findAll()` sin paginar | `architecture.md` §6 | — |
| 8 | Pruebas de integración con la base real | OK | `infrastructure/src/test/**` | 5 clases con Testcontainers (`postgres:16-alpine`): `EmployeePersistenceAdapterTest` (9), `IdentityPersistenceAdapterTest` (8), `RefreshTokenPersistenceAdapterTest` (3), `AuthenticationFlowIntegrationTest` (10) y `BootstrapAdministratorIntegrationTest` (3). **0 usos de H2:** las 5 coincidencias de la búsqueda eran «oauth2» en imports | `DP.pruebas-integracion` | — |
| — | Concurrencia en modificaciones (`@Version`) | Diferido | `EmployeeJpaEntity` | Es W-04 de la ronda 002, **riesgo aceptado** en `2026-11-12-ronda-002/08-correcciones-aplicadas.md` §3. No contradice ninguna regla obligatoria | — | `/01-definir reglas concurrencia` |

## 3. Trazabilidad de reglas (las que se apoyan en infraestructura)

| Regla | Implementación | Prueba | Estado |
|---|---|---|---|
| BR-EMP-001 (concurrencia) | `uk_employees_document` (V1) y la traducción de `DataIntegrityViolationException` en `EmployeePersistenceAdapter` | `EmployeePersistenceAdapterTest` | PASS |
| BR-EMP-004 (búsqueda sin tildes ni mayúsculas) | `EmployeeSpecifications` | `EmployeeSpecificationsTest` (3), `EmployeePersistenceAdapterTest` | PASS |
| BR-USR-001 (concurrencia) | `uk_users_username` y `ck_users_username_lowercase` (V2), y `UserPersistenceAdapter` | `IdentityPersistenceAdapterTest` | PASS |
| BR-USR-003 (hash de contraseñas) | `PasswordEncoderConfig`, `PasswordHasherAdapter` | `PasswordEncoderConfigTest` (6) | PASS |
| BR-USR-006 (bloqueo de fila y revocación) | `findByTokenHashForUpdate`, `revokeFamily` (`where revokedAt is null`) | `RefreshTokenPersistenceAdapterTest` (3), `AuthenticationFlowIntegrationTest` (10) | PASS |
| BR-USR-007 (arranque) | `BootstrapAdministratorRunner` | `BootstrapAdministratorRunnerTest` (8), `BootstrapAdministratorIntegrationTest` (3) | PASS |
| §4.1 (roles y permisos) | `V3__seed_roles_permissions.sql`, `V5__seed_employee_read_permission.sql` | `IdentityPersistenceAdapterTest` | PASS |

## 4. Pruebas existentes y faltantes

- **Existentes:** 99 pruebas en 13 clases fuera de `api`, contadas en los reportes de Surefire.
  - Persistencia: 23.
  - Integración de extremo a extremo: 13.
  - Arranque: 11.
  - Seguridad: 55, entre ellas `CredentialsAuthenticatorAdapterTest` (18), `TokenIssuerAdapterTest` (11) y `SecurityFilterChainTest` (11).
- **Faltantes:** ninguna para lo implementado. Una prueba de concurrencia de `PUT` llegaría con W-04, que está diferido.

## 5. Hallazgos

### ERROR
Ninguno.

### WARNING
Ninguno nuevo. W-04 sigue diferido (ver §2).

### INFO

**I-08 · `architecture.md` §8 describe un perfil `dev` que no existe.**
- **Dónde:** `docs/architecture.md:195` dice «perfiles `local`, `dev`, `prod`», pero `infrastructure/src/main/resources/` solo tiene `application-local.yml` y `application-prod.yml`.
- **Por qué es INFO:** `technical.md` §4.1, que describe el estado implementado, ya lo aclara: «No existe todavía un perfil `dev` con configuración propia». No falta nada en el código: `architecture.md` describe el objetivo.
- **Corrección propuesta:** precisar `architecture.md` §8 («`local` y `prod`; `dev` cuando haya un entorno compartido»), o dejarlo así si el perfil `dev` es un objetivo vigente. Lo decide el autor.

**Fuera del alcance (para otro modo):** ninguna observación.

## 6. Aspectos positivos

- **Integridad en dos niveles:** cada regla de unicidad se valida en la aplicación (`exists…`) y también en la base (`uk_*`), y el adaptador traduce la violación concurrente al mismo código de negocio.
- **Migraciones nunca editadas:** cada una tiene un solo commit, y los cambios de datos van en migraciones nuevas (`V5`).
- **`ddl-auto: validate`:** Hibernate comprueba que el esquema de Flyway coincide con las entidades, pero nunca lo cambia.
- **Testcontainers con la misma versión mayor de PostgreSQL que producción** (16, `DP.base-datos`).

## 7. Conclusión técnica

La infraestructura, sin contar la API, está igual que en la ronda 002 y sigue cumpliendo todas sus reglas. I-08 es solo una precisión de documentación.

**Siguiente paso:** `/06-revisar api` (paso 4 de 9, ronda 003).
