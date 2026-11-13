# Revisión 002 · 2 — Aplicación

- **Prompt:** `docs/prompts/06-revisar.md`, modo `aplicacion` (paso 2 de 9 de la ronda 002)
- **Fecha:** 2026-11-12 · **Commit revisado:** `183d054` (`master`)
- **Alcance:** paquete `pe.edu.galaxy.training.ia.ca.application` (10 puertos de entrada, 7 de salida, 10 servicios, DTO y excepciones) y sus pruebas (9 clases, 46 pruebas)

## 1. Resumen

La capa de aplicación cumple la regla de dependencia y las convenciones de `architecture.md`. Depende solo de `domain` y de `spring-tx`, cada caso de uso es una clase registrada como `@Bean` en `infrastructure/config/UseCaseConfig` y las pruebas mockean solo puertos de salida. Las reglas de aplicación de los casos de uso implementados (empleados e identidad) están cumplidas y probadas. No hay ERROR. Hay un WARNING: BR-USR-005 no tiene ningún caso de uso que la dispare ni figura como pendiente en ningún documento. Los casos de uso de contratos y de desactivación de empleados no están implementados, pero `technical.md` §1 los declara pendientes, así que no son hallazgos.

**Build:** el de la ronda (paso 1) sobre el mismo commit `183d054`: BUILD SUCCESS, 46 pruebas de `application` sin fallos, cobertura de 96,7 % en líneas y 92,9 % en ramas. **Quality Gate:** Jenkins #16, OK.

## 2. Comprobaciones

| # | Comprobación | Severidad | Archivo/componente | Evidencia | Regla o criterio | Corrección propuesta |
|---|---|---|---|---|---|---|
| 1 | Solo depende de `domain` y, como máximo, de `@Transactional` | OK | `application/pom.xml`, imports | Dependencias: `domain` y `spring-tx` (más JUnit, AssertJ y Mockito en *test*). Único import externo: `org.springframework.transaction.annotation.Transactional` (10 usos) | `architecture.md` §3.3 | — |
| 2 | Puertos `port/in` y `port/out` con nombres y tipos del dominio | OK | `port/in/*` (10), `port/out/*` (7) | `<Verbo><Entidad>UseCase`; `EmployeeRepositoryPort`, `UserRepositoryPort`, `RoleRepositoryPort`, `RefreshTokenRepositoryPort` y los puertos de identidad (`CredentialsAuthenticatorPort`, `PasswordHasherPort`, `TokenIssuerPort`); firmas sin tipos técnicos | `architecture.md` §5 | — |
| 3 | `Command`/`Query`/`Result` como records | OK | `dto/*` | Todos son `record`, salvo los enums `EmployeeSortField` y `SortDirection`, que son valores de consulta | `architecture.md` §5 | — |
| 4 | Un servicio por caso de uso, sin `@Service`, registrado en `infrastructure/config` | OK | `usecase/*`, `UseCaseConfig.java` | 10 servicios y 10 `@Bean`; 0 anotaciones `@Service`/`@Component` en `application` | `architecture.md` §3.3 y §3.6 | — |
| 5 | Orquesta sin absorber reglas del dominio | OK | `usecase/*` | Los servicios cargan, delegan a los agregados (`Employee.update`, `User.create`, `RefreshToken`), validan las reglas de aplicación y persisten | `architecture.md` §4 | — |
| 6 | Reglas de §4 asignadas a `application` | WARNING | ver trazabilidad | Las de empleados e identidad están implementadas. BR-USR-005 no tiene ningún caso de uso que la dispare (W-03) | `business-rules.md` §4 | Ver W-03 |
| 7 | Límites transaccionales | OK | `usecase/*` | Un `@Transactional` por servicio; `RefreshTokenService` usa `noRollbackFor = InvalidRefreshTokenException`, de modo que la revocación de la familia se confirma aunque la operación falle, como pide el detalle de §6 | `architecture.md` §8; `business-rules.md` §6 | — |
| 8 | Excepciones de aplicación con código del catálogo | OK | `exception/*` | `NotFoundException`/`ConflictException` y subclases (`EmployeeNotFound` `EMP-404`, `DuplicateEmployeeDocument` `EMP-001`, `DuplicateUsername` `AUTH-007`, `RoleNotFound` `AUTH-009`, `UserNotFound` `AUTH-404`, `InvalidCredentials` `AUTH-002`) | `business-rules.md` §8 | — |
| 9 | Pruebas con Mockito que mockean solo puertos | OK | `application/src/test/**` | 46 pruebas; tipos mockeados: los 7 puertos de salida, nada más. Todos los servicios tienen pruebas (`Logout` y `GetCurrentUser` en `LogoutAndCurrentUserServiceTest`) | `architecture.md` §9 | — |

## 3. Trazabilidad de reglas de aplicación

| Regla | Implementación | Prueba | Estado |
|---|---|---|---|
| BR-EMP-001 | `RegisterEmployeeService` (`existsByDocumentNumber`). Respaldada por `uk_employees_document` (V1) y la traducción de `DataIntegrityViolationException` en `EmployeePersistenceAdapter` ante peticiones concurrentes | `RegisterEmployeeServiceTest` | PASS |
| BR-EMP-004 | `EmployeeSearchQuery` (documento y nombre excluyentes → `VAL-001`) y `ListEmployeesService` | `ListEmployeesServiceTest` (8 pruebas) | PASS |
| BR-USR-001 | `CreateUserService` y `BootstrapAdministratorService` (`existsByUsername`); respaldada por `uk_users_username` (V2) y `UserPersistenceAdapter` | `CreateUserServiceTest` | PASS |
| BR-USR-004 | `AuthenticateUserService` (`isEnabled` → `AUTH-002`); `RefreshTokenService` (usuario deshabilitado → revoca la familia y `AUTH-005`) | `AuthenticateUserServiceTest`, `RefreshTokenServiceTest` | PASS |
| BR-USR-006 (parte de aplicación) | `RefreshTokenService` (token usado → revoca la familia; `noRollbackFor`) | `RefreshTokenServiceTest` (7 pruebas) | PASS |
| BR-USR-007 | `BootstrapAdministratorService` (no crea si existe un `ADMIN`; valida `PasswordPolicy`; `AUTH-007` si el username está ocupado) | `BootstrapAdministratorServiceTest` | PASS |
| BR-USR-005 | — (no existe ningún caso de uso para deshabilitar un usuario o cambiar su contraseña) | — | NOT COVERED (W-03) |
| BR-CTR-001, 005, 008, 010, 012 | — (no hay casos de uso de contratos) | — | NOT COVERED: pendiente documentado (`technical.md` §1) |
| BR-EMP-006, 007 | — (no hay desactivación ni contratos) | — | NOT COVERED: pendiente documentado (`technical.md` §1) |

## 4. Pruebas existentes y faltantes

- **Existentes:** 9 clases y 46 pruebas, una o más por servicio; caminos felices y de error (duplicados, no encontrado, credenciales inválidas, token reutilizado o expirado, administrador ya existente).
- **Faltantes:** ninguna para los casos de uso implementados. Las de contratos y de desactivación llegarán con sus casos de uso.

## 5. Hallazgos

### ERROR
Ninguno.

### WARNING

**W-03 · BR-USR-005 no tiene ningún caso de uso que la dispare y no figura como pendiente**
- **Archivos:** `docs/business-rules.md` §4 (BR-USR-005, capa `application`), §4.1 («Sin permiso explícito: … cambiar la contraseña propia») y §6 (sin `ChangePasswordUseCase` ni `DisableUserUseCase`); `docs/technical.md` §3.3.
- **Evidencia:** no existe en `application` ningún caso de uso para cambiar la contraseña ni para deshabilitar un usuario, y no hay ningún método que revoque todos los refresh tokens de un usuario. `technical.md` §3.3 solo lista como pendiente la auditoría.
- **Explicación:** hoy la regla no se incumple, porque los eventos que la disparan no existen. Pero `business-rules.md` §4.1 da por hecho el cambio de la contraseña propia, y ninguna lista de pendientes recoge la regla. Al implementar esos casos de uso es fácil olvidar la revocación.
- **Corrección propuesta:** agregar a `business-rules.md` §6 los casos de uso `ChangeOwnPasswordUseCase` y `DisableUserUseCase` (o, si no se harán, quitar la mención de §4.1), y anotar BR-USR-005 como pendiente en `technical.md` §1 / §3.3. Decidirlo con `/01-definir reglas cambio-de-contraseña`.

### INFO
Ninguno.

## 6. Aspectos positivos

- Los casos de uso se registran sin anotaciones de Spring, así `application` puede probarse sin contenedor.
- La unicidad se valida en dos niveles: primero en el caso de uso, para devolver un error claro, y después en la base, con la traducción del error de integridad, que cubre las carreras.
- `noRollbackFor` en `RefreshTokenService` resuelve la detección de reutilización de forma correcta y está documentado en el propio código.
- Las pruebas mockean solo puertos y verifican interacciones (`verify`, `never`, `ArgumentCaptor`) cuando la interacción es la regla, como la revocación de la familia.

## 7. Conclusión y siguiente paso

La capa de aplicación cumple las reglas obligatorias. W-03 es un vacío de documentación y alcance, no de código.

**Siguiente paso del orden:** `/06-revisar infraestructura` (paso 3 de 9, ronda 002).
