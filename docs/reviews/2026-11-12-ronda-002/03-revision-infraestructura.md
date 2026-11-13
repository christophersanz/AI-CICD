# Revisión 002 · 3 — Infraestructura

- **Prompt:** `docs/prompts/06-revisar.md`, modo `infraestructura` (paso 3 de 9 de la ronda 002)
- **Fecha:** 2026-11-12 · **Commit revisado:** `183d054` (`master`)
- **Alcance:** `infrastructure` sin `api`: `persistence` (adaptadores, entidades, mappers, repositorios), `security` (adaptadores), `config`, migraciones Flyway V1–V5, `application*.yml` y sus pruebas

## 1. Resumen

Los siete puertos de salida tienen su adaptador. Las entidades JPA están separadas del dominio, las migraciones son coherentes y tienen restricciones, claves foráneas e índices, la configuración no expone secretos y las pruebas de persistencia usan PostgreSQL real con Testcontainers. No hay ERROR. Hay un WARNING: no existe bloqueo optimista, aunque `Employee` ya se modifica (`UpdateEmployeeUseCase`), y la razón por la que la revisión 001 lo diferió dejó de ser válida.

**Build:** el de la ronda (paso 1) sobre `183d054`: BUILD SUCCESS, 153 pruebas de `infrastructure` sin fallos, cobertura de 96,3 % en líneas y 75,4 % en ramas. **Quality Gate:** Jenkins #16, OK.

## 2. Comprobaciones

| # | Comprobación | Severidad | Archivo/componente | Evidencia | Regla o criterio | Corrección propuesta |
|---|---|---|---|---|---|---|
| 1 | Cada puerto de salida tiene su adaptador; sin dependencias hacia `api` | OK | `persistence/adapter/*`, `security/*` | `EmployeeRepositoryPort` → `EmployeePersistenceAdapter`, `UserRepositoryPort` → `UserPersistenceAdapter`, `RoleRepositoryPort` → `RolePersistenceAdapter`, `RefreshTokenRepositoryPort` → `RefreshTokenPersistenceAdapter`, `CredentialsAuthenticatorPort` → `CredentialsAuthenticatorAdapter`, `PasswordHasherPort` → `PasswordHasherAdapter`, `TokenIssuerPort` → `TokenIssuerAdapter`. `persistence`, `security` y `config` no importan `infrastructure.api` | `architecture.md` §3.7 y §3.9 | — |
| 2 | Entidades JPA separadas del dominio y mappers sin lógica | OK | `persistence/entity/*JpaEntity`, `persistence/mapper/*` | Las entidades usan el sufijo `JpaEntity`; los mappers reconstruyen con `restore(...)` y solo copian campos | `architecture.md` §3.5 | — |
| 3 | Repositorios como mecanismo técnico | OK | `persistence/repository/*` | Spring Data y `EmployeeSpecifications`; ningún repositorio se usa fuera de `persistence` | `architecture.md` §2 | — |
| 4 | Migraciones coherentes: `NOT NULL`, únicos, claves foráneas e índices | OK | `db/migration/V1`–`V5` | `uk_employees_document`, `uk_users_username`, `ck_users_username_lowercase`, `uk_refresh_tokens_token_hash`, `ck_refresh_tokens_expiration`, `ck_refresh_tokens_revocation`; claves foráneas en `role_permissions`, `user_roles` y `refresh_tokens` (con `ON DELETE CASCADE` hacia `users`); índices por `family_id` y `user_id`. La búsqueda por nombre no usa índice por una decisión documentada en `EmployeeSpecifications` (volumen bajo) | `business-rules.md` BR-EMP-001, BR-USR-001 y 006 | — |
| 5 | Adaptadores de seguridad sin reglas de negocio | OK | `security/authn`, `security/crypto`, `security/jwt` | Implementan los puertos de identidad; las reglas (BR-USR-004 y 006) están en `application`. El análisis de seguridad corresponde al paso 5 | `architecture.md` §3.9 | — |
| 6 | Configuración: perfiles, secretos, `open-in-view`, logs | OK | `application.yml`, `application-local.yml`, `application-prod.yml` | `open-in-view: false`; `ddl-auto: validate`; `DB_PASSWORD`, `SECURITY_JWT_*` por variable de entorno (solo el perfil `local` tiene un valor por defecto para la base); logs estructurados ECS | `architecture.md` §8 | — |
| 7 | Rendimiento: N+1, `EAGER`, paginación | OK | `UserJpaEntity`, `RoleJpaEntity` | Los dos `@ElementCollection(fetch = EAGER)` son conjuntos pequeños de nombres (roles de un usuario y permisos de un rol); el listado de empleados está paginado | `architecture.md` §6 | — |
| 8 | Concurrencia en modificaciones | WARNING | `persistence/entity/EmployeeJpaEntity.java`, `UpdateEmployeeService` | No hay `@Version` en ninguna entidad. `PUT /api/v1/employees/{id}` reemplaza nombres y correo: con dos ediciones simultáneas gana la última, sin aviso | `architecture.md` §6 («Conflicto de estado o concurrencia → 409») | Ver W-04 |
| 9 | Pruebas de integración con la base real | OK | `infrastructure/src/test/**` | `EmployeePersistenceAdapterTest`, `IdentityPersistenceAdapterTest`, `RefreshTokenPersistenceAdapterTest`, `AuthenticationFlowIntegrationTest` y `BootstrapAdministratorIntegrationTest` con `PostgreSQLContainer` y `@ServiceConnection`; no hay H2 | `architecture.md` §9 | — |

## 3. Trazabilidad de reglas con soporte en infraestructura

| Regla | Implementación | Prueba | Estado |
|---|---|---|---|
| BR-EMP-001 (en la base) | `uk_employees_document` y traducción de `DataIntegrityViolationException` en `EmployeePersistenceAdapter` | `EmployeePersistenceAdapterTest` | PASS |
| BR-EMP-004 (búsqueda) | `EmployeeSpecifications` (`translate()` para quitar tildes y escape de comodines) | `EmployeePersistenceAdapterTest` | PASS |
| BR-USR-001 / 002 (en la base) | `uk_users_username`, `ck_users_username_lowercase`, `UserPersistenceAdapter` | `IdentityPersistenceAdapterTest` | PASS |
| BR-USR-006 (persistencia) | `refresh_tokens` (hash único, familia, `replaced_by`, revocación) y `RefreshTokenPersistenceAdapter` | `RefreshTokenPersistenceAdapterTest` | PASS |
| BR-USR-007 | `BootstrapAdministratorRunner` | `BootstrapAdministratorIntegrationTest`, `BootstrapAdministratorRunnerTest` | PASS |
| §4.1 (roles y permisos) | `V3__seed_roles_permissions.sql`, `V5__seed_employee_read_permission.sql` | `AuthenticationFlowIntegrationTest` | PASS |

## 4. Pruebas existentes y faltantes

- **Existentes:** integración contra PostgreSQL real para los cuatro adaptadores de persistencia y el flujo completo de autenticación; pruebas unitarias del arranque del administrador.
- **Faltantes:** una prueba de edición concurrente de un empleado, cuando se resuelva W-04.

## 5. Hallazgos

### ERROR
Ninguno.

### WARNING

**W-04 · Sin bloqueo optimista; el motivo por el que se difirió ya no se cumple**
- **Archivos:** `infrastructure/src/main/java/pe/edu/galaxy/training/ia/ca/infrastructure/persistence/entity/EmployeeJpaEntity.java` (sin `@Version`); `application/.../usecase/UpdateEmployeeService.java`.
- **Regla:** `architecture.md` §6 define `409` para «conflicto de estado o concurrencia», pero no hay ningún mecanismo que detecte ese conflicto.
- **Evidencia:** `docs/reviews/2026-09-26-ronda-001/07-revision-integral.md`, tabla de diferidos: «Riesgo de carrera en BR-CTR-005 y generación de `ContractNumber`; `@Version` — Los casos de uso de contrato no están implementados; `Employee` es inmutable». Hoy `Employee` tiene `update(...)` y está expuesto con `PUT /api/v1/employees/{id}`.
- **Explicación:** dos usuarios de RR.HH. que editan el mismo empleado a la vez se pisan: el último sobrescribe al primero sin error (*lost update*). El riesgo es bajo con el volumen actual, pero la justificación del diferido quedó obsoleta.
- **Corrección propuesta:**
  1. Migración `V6` con `version BIGINT NOT NULL DEFAULT 0` en `employees` y `@Version` en `EmployeeJpaEntity`.
  2. Llevar la versión al dominio o al `Result`, y aceptarla en el `PUT` (`If-Match` con ETag, o un campo `version`).
  3. Traducir `OptimisticLockingFailureException` a `409` con un código nuevo del catálogo, definido con `/01-definir reglas concurrencia`.

  Si se decide no hacerlo, registrar en la próxima ronda un nuevo diferido con la razón actualizada (por ejemplo, «riesgo aceptado: pocas ediciones simultáneas»).

### INFO
Ninguno.

## 6. Aspectos positivos

- `open-in-view: false` y `ddl-auto: validate`: el esquema lo define Flyway y Hibernate solo lo verifica.
- Restricciones `CHECK` que protegen la integridad en la propia base (`username` en minúsculas, expiración posterior a la emisión, revocación con motivo).
- La búsqueda sin tildes con `translate()`, sin extensiones de PostgreSQL, documentada en el código junto con el motivo de no usar índice.
- Pruebas de integración con el mismo motor que en producción (`@ServiceConnection`).

## 7. Conclusión y siguiente paso

La infraestructura cumple las reglas obligatorias. W-04 es un riesgo de concurrencia que antes se había diferido con una razón que ya no aplica.

**Siguiente paso del orden:** `/06-revisar api` (paso 4 de 9, ronda 002).
