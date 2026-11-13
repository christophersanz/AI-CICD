# Revisión 003 · 2 — Aplicación

- **Prompt:** `docs/prompts/06-revisar.md`, modo `aplicacion` (paso 2 de 9)
- **Fecha:** 2026-11-13 · **Commit revisado:** `027f37f` (`master`)
- **Informes de la ronda:** `01-revision-dominio.md`

## 1. Resumen

La capa `application` cumple todas sus reglas obligatorias:
- depende solo de `domain` y de `@Transactional`;
- tiene 10 puertos de entrada y 7 de salida con tipos del dominio;
- tiene un servicio por caso de uso, sin `@Service`, registrado en `UseCaseConfig`;
- cada error de cada servicio tiene su prueba con Mockito, y solo se mockean puertos de salida.

**No cambió ninguna línea de código desde la ronda 002.** Comparé los 56 archivos tocados desde `26fdf64` sin comentarios, y la única diferencia es el JavaDoc de #16. No hay ERROR ni WARNING. Hay 1 INFO nuevo, que es un error del propio JavaDoc de #16.

**Build:** se reutiliza el del paso 1 (`01-revision-dominio.md`), que es del mismo commit `027f37f` y sin cambios de código. Solo hay un archivo nuevo sin seguimiento, el informe del paso 1. Resultado: BUILD SUCCESS.

| Módulo | Pruebas | Fallidas | Con error | Omitidas | Líneas | Ramas |
|---|---:|---:|---:|---:|---:|---:|
| application | 46 | 0 | 0 | 0 | 96,7 % | 92,9 % |

**Último Quality Gate:** OK en Jenkins #26 sobre `027f37f`.

## 2. Comprobaciones

| # | Comprobación | Severidad | Archivo/componente | Evidencia | Regla o criterio | Corrección propuesta |
|---|---|---|---|---|---|---|
| 1 | Solo depende de `domain` y de `@Transactional` | OK | `application/pom.xml`, imports | Dependencias: `domain` y `spring-tx`; en *test*, JUnit, AssertJ y Mockito. Único import externo: `org.springframework.transaction.annotation.Transactional`, con 10 usos | `architecture.md` §3.3 | — |
| 2 | Puertos `port/in` y `port/out` con nombres y tipos del dominio | OK | `port/in/*` (10), `port/out/*` (7) | `<Verbo><Entidad>UseCase` y `<Entidad>RepositoryPort` más los puertos de identidad. Ningún tipo técnico (`Page`, JPA, HTTP, `Jwt`); la paginación usa `PageResult` | `architecture.md` §5 | — |
| 3 | `Command`/`Query`/`Result` como records | OK | `dto/*` (19) | Todos son `record`, salvo los enums `EmployeeSortField` y `SortDirection`. Solo `EmployeeSearchQuery` valida: BR-EMP-004, que §4 asigna a `application`, con el mismo criterio que la ronda 002 | `architecture.md` §5 | — |
| 4 | Un servicio por caso de uso, sin `@Service`, registrado en `infrastructure/config` | OK | `usecase/*`, `UseCaseConfig.java` | 10 servicios; cada uno implementa su `UseCase`. 0 `@Service` o `@Component`; 10 `new …Service(` en `UseCaseConfig` | `architecture.md` §3.3 y §3.6 | — |
| 5 | Orquesta sin absorber reglas del dominio | OK | `usecase/*` | Sin cambios de código desde la ronda 002, cuando se verificó en OK | `architecture.md` §4 | — |
| 6 | Reglas de §4 asignadas a `application` | OK | ver trazabilidad | Las de empleados e identidad están implementadas y probadas. BR-USR-005 está **diferida** (W-03 de la ronda 002, documentada como pendiente) | `business-rules.md` §4 | — |
| 7 | Límites transaccionales | OK | `usecase/*` | Un `@Transactional` por servicio: 3 `readOnly` en consultas y `noRollbackFor = InvalidRefreshTokenException` en `RefreshTokenService`, para que la revocación de la familia se confirme (§6) | `architecture.md` §8 | — |
| 8 | Excepciones de aplicación con código del catálogo | OK | `exception/*` | `EMP-001`, `EMP-404`, `AUTH-002`, `AUTH-007`, `AUTH-009` y `AUTH-404` coinciden con §8, verificado en #16 | `business-rules.md` §8 | — |
| 9 | Pruebas con Mockito que mockean solo puertos | OK | `application/src/test/**` | 46 pruebas en 9 clases. Los 21 `@Mock` son de los 7 puertos de salida y de nada más. Cada excepción que lanza cada servicio tiene su prueba (§4) | `architecture.md` §9 | — |

## 3. Trazabilidad de reglas de aplicación

| Regla | Implementación | Prueba | Estado |
|---|---|---|---|
| BR-EMP-001 | `RegisterEmployeeService` (`existsByDocumentNumber`), respaldada por `uk_employees_document` y el adaptador ante peticiones concurrentes | `RegisterEmployeeServiceTest.shouldThrowDuplicateEmployeeDocumentWhenDocumentAlreadyExists` | PASS |
| BR-EMP-004 | `EmployeeSearchQuery` y `ListEmployeesService` | `ListEmployeesServiceTest` (8 pruebas, incluida `shouldRejectQueryWhenDocumentAndNameAreCombined`) | PASS |
| BR-USR-001 | `CreateUserService`, `BootstrapAdministratorService` | `shouldThrowDuplicateUsernameWhenUsernameAlreadyExists`, `shouldThrowDuplicateUsernameWhenUsernameBelongsToNonAdminUser` | PASS |
| BR-USR-004 | `AuthenticateUserService` (`AUTH-002`), `RefreshTokenService` (revoca la familia y `AUTH-005`) | `shouldRejectLoginWithGenericErrorWhenUserIsDisabled`, `shouldRevokeFamilyWhenUserIsDisabled` | PASS |
| BR-USR-006 (parte de aplicación) | `RefreshTokenService` | `shouldRevokeWholeFamilyWhenAlreadyUsedTokenIsPresented`, `shouldKeepRevocationCommittedWhenRefreshFails` | PASS |
| BR-USR-007 | `BootstrapAdministratorService` | `BootstrapAdministratorServiceTest` (4 pruebas) | PASS |
| BR-USR-005 | — | — | NOT COVERED: **diferida** (W-03 de la ronda 002, §9) |
| BR-CTR-001, 005, 008, 010, 012 | — (no hay casos de uso de contratos) | — | NOT COVERED: pendiente documentado (`technical.md` §1) |
| BR-EMP-006, 007 | — | — | NOT COVERED: pendiente documentado |

## 4. Pruebas existentes y faltantes

- **Existentes:** 46 ejecuciones (43 métodos, más los casos parametrizados).

  | Servicio | Prueba | Errores probados |
  |---|---|---|
  | `AuthenticateUser` | 4 | `AUTH-002` (credenciales y usuario deshabilitado) |
  | `BootstrapAdministrator` | 4 | `AUTH-007`, `AUTH-008` |
  | `CreateUser` | 6 | `AUTH-007`, `AUTH-009`, `AUTH-008`, `VAL-001` |
  | `GetEmployee` | 2 | `EMP-404` |
  | `ListEmployees` | 8 | `VAL-001` (3 variantes) |
  | `Logout` y `GetCurrentUser` | 5 | idempotencia del logout, `AUTH-404` |
  | `RefreshToken` | 7 | `AUTH-005` (5 variantes), revocación confirmada |
  | `RegisterEmployee` | 4 | `EMP-001`, `EMP-002`, `VAL-001` |
  | `UpdateEmployee` | 3 | `EMP-404`, `VAL-001` |

- **Faltantes:** ninguna para las reglas implementadas. Las de BR-USR-005, BR-EMP-006/007 y los contratos llegarán con sus casos de uso.

## 5. Hallazgos

### ERROR
Ninguno.

### WARNING
Ninguno.

### INFO

**I-07 · El JavaDoc de dos puertos de entrada dice que el identificador puede no ser un UUID válido.**
- **Dónde:** `port/in/GetEmployeeUseCase.java:15-16` («si el identificador no es un UUID válido») y `port/in/UpdateEmployeeUseCase.java:18-19` («si el identificador, los nombres o el correo no cumplen su formato»).
- **El problema:** `EmployeeQuery.employeeId` y `UpdateEmployeeCommand.employeeId` son de tipo `UUID`, así que el identificador ya llega convertido. El texto lo convierte Spring en la API, que responde 400 si no es un UUID. En la aplicación, el `VAL-001` solo puede ocurrir si el identificador **falta**, porque `new EmployeeId(null)` lo rechaza.
- **Origen:** es un error de la IA en #16, de la misma familia que el JavaDoc de `of(String)` que se eliminó en #18.
- **Corrección propuesta:** cambiar el texto por «si falta el identificador ({@code VAL-001})» y, en `UpdateEmployeeUseCase`, separar ese caso del de los nombres y el correo. Es solo documentación.

**Compartido con el paso 1:** I-06 (bloques JavaDoc sin línea en blanco antes de los tags) también afecta a `application`: 30 bloques en 21 archivos, contados con el mismo script que en el paso 1.

**Fuera del alcance (para otro modo):** ninguna observación.

## 6. Aspectos positivos

- **`noRollbackFor` en `RefreshTokenService`,** probado con `shouldKeepRevocationCommittedWhenRefreshFails`: la revocación por reutilización se confirma aunque la petición termine en `AUTH-005`.
- **`toString()` que oculta secretos** en `LoginCommand`, `CreateUserCommand`, `TokenPairResult` y los demás DTO con credenciales, probado con `shouldNotExposeTokensWhenResultIsPrinted` y `shouldNotExposePasswordWhenCommandIsPrinted`.
- **Logout idempotente sin filtrar información:** un token ajeno o inexistente no produce error (`shouldIgnoreLogoutWhenTokenBelongsToAnotherUser`).
- **Solo se mockean puertos de salida.** Así las pruebas no dependen de detalles internos de los servicios.

## 7. Conclusión técnica

La capa `application` está igual que cuando se revisó en la ronda 002, y en buen estado. Lo único nuevo es I-07, una imprecisión del JavaDoc que se corrige con una línea en cada uno de los dos puertos.

**Siguiente paso:** `/06-revisar infraestructura` (paso 3 de 9, ronda 003).
