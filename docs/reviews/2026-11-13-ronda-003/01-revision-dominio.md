# Revisión 003 · 1 — Dominio

- **Prompt:** `docs/prompts/06-revisar.md`, modo `dominio` (paso 1 de 9)
- **Fecha:** 2026-11-13 · **Commit revisado:** `027f37f` (`master`, tras integrar los PR #14 a #18), sin cambios locales
- **Ronda anterior:** 002 cerrada con `FINAL STATUS: READY` (`2026-11-12-ronda-002/09-veredicto-final.md`). Cambios en `domain` desde entonces:
  - más pruebas de `ContractPeriod` (#14);
  - JavaDoc de toda la API pública (#16);
  - se eliminó `of(String)` de `ContractId` y `EmployeeId` (#18).

## 1. Resumen

El dominio sigue cumpliendo todas las reglas obligatorias:
- es Java puro, sin dependencias de producción en `domain/pom.xml`;
- los value objects se validan en su constructor con `InvalidValueException`;
- las transiciones de `Contract` siguen §5 y los eventos de §7 se fechan con un `Clock`;
- las reglas que §4 asigna a `domain` tienen implementación y prueba.

No hay ERROR ni WARNING: los cambios de esta ronda no introdujeron regresiones. Hay 6 observaciones INFO: dos *code smells* abiertos en SonarQube, una inconsistencia en el uso del `Clock`, la falta de validación estructural en `RefreshToken.restore`, pruebas menores faltantes y un defecto de estilo del JavaDoc.

**Build:** `./mvnw clean verify` sobre `027f37f` → BUILD SUCCESS.

| Módulo | Pruebas | Fallidas | Con error | Omitidas | Líneas | Ramas |
|---|---:|---:|---:|---:|---:|---:|
| domain | 209 | 0 | 0 | 0 | 95,9 % | 93,1 % |
| application | 46 | 0 | 0 | 0 | — | — |
| infrastructure | 154 | 0 | 0 | 0 | — | — |

**Último Quality Gate (SonarQube local, `contract-api`):** OK en Jenkins #26 sobre `027f37f`, el mismo commit revisado. El build lo lanzó el sondeo y sus 9 etapas terminaron en SUCCESS, con `contract-api:jenkins-26` desplegado en `localhost:8090`. Issues abiertos: 2 *code smells* (I-01 e I-02), 0 bugs y 0 vulnerabilidades.

## 2. Comprobaciones

| # | Comprobación | Severidad | Archivo/componente | Evidencia | Regla o criterio | Corrección propuesta |
|---|---|---|---|---|---|---|
| 1 | Java puro | OK | `domain/src/main/java/**`, `domain/pom.xml` | 0 imports fuera de `java.*` y `…domain.*` (`grep` sobre las 52 clases); `pom.xml` solo declara `junit-jupiter` y `assertj-core` con `scope test` | `architecture.md` §3.2 | — |
| 2 | Agregados protegen sus invariantes; `restore` sin saltarse invariantes | OK | `Contract`, `Employee`, `User` | 0 setters públicos. `Contract.restore` valida periodo, término y renovaciones (`CTR-001`/`CTR-009`, corrección W-01 de la ronda 002). `Employee.restore` y `User.restore` pasan por el constructor, que exige cada campo con `Guard.notNull` | §3 y §4 | Ver I-04 (`RefreshToken`, fuera de §3) |
| 3 | Value objects validados al crearse, sin NPE/IAE, montos con `BigDecimal` | OK | `*/vo/*.java`, `shared/vo/Money.java` | 0 `NullPointerException`, `IllegalArgumentException`, `requireNonNull`, `float` o `double`. `Money` rechaza más de 2 decimales y fija la escala sin redondear (`RoundingMode.UNNECESSARY`) | `architecture.md` §2 y §5 | — |
| 4 | Transiciones según §5 | OK | `ContractStatus.java`, `Contract.java` | `nextStatuses()` coincide con el diagrama. Una transición no dibujada lanza `CTR-008`; `renew` fuera de `ACTIVE`+`FIXED_TERM` lanza `CTR-005` y `update` fuera de `DRAFT`, `CTR-004`, como documenta §5 | §5, §8 | — |
| 5 | Eventos de §7 con `eventId`, `occurredOn` desde un `Clock` y `aggregateId` | OK | `contract/event/*`, `Contract.java` | Los 5 eventos de §7 se emiten en `create`, `sign`, `renew`, `terminate` y `expire`; `ContractEvent.aggregateId()` devuelve `contractId()`. 0 llamadas a `now()` o a un reloj del sistema. No hay eventos que §7 no documente | §7, `architecture.md` §2 | Ver I-03 |
| 6 | Dependencias entre paquetes | OK | `common`, `shared`, agregados | `common` y `shared` no importan agregados y `common` no importa `shared`. El único import entre agregados es `contract ← employee.vo.EmployeeId` | `architecture.md` §2, ADR-0004 | — |
| 7 | Excepciones con el código del catálogo | OK | `*/exception/*.java` | Los 13 códigos de dominio (`CTR-001`…`CTR-009`, `EMP-002`, `AUTH-005`, `AUTH-008`, `VAL-001`) coinciden con §8, verificado excepción por excepción en #16. Sin conceptos HTTP, JPA ni SQL | §8, `architecture.md` §7 | — |
| 8 | Sin abstracciones ni patrones fuera del alcance | OK | `domain` | Se eliminó código sin uso en lugar de abstraerlo (#18). No hay *specifications*, *factories* genéricas ni eventos no documentados | `architecture.md` §5, CLAUDE.md §4 | — |
| 9 | Pruebas de dominio sin Spring ni mocks | OK | `domain/src/test/**` | 14 clases de prueba, 0 con Spring o Mockito; 209 pruebas (`@Nested` en `ContractTest` y `ContractPeriodTest`) | `architecture.md` §9 | Ver I-05 |

## 3. Trazabilidad de reglas (las que §4 asigna a `domain`)

| Regla | Implementación | Prueba | Estado |
|---|---|---|---|
| BR-CTR-002 | `ContractPeriod.of` | `ContractPeriodTest$Creation.shouldRejectPeriodWhenStartDateIsMissing` | PASS |
| BR-CTR-003 | `ContractPeriod.of`, `Contract.ensurePeriodMatchesType` | `ContractPeriodTest$Creation` (límites `0`, `-1`, `-30` y `+1` día); `ContractTest.shouldRejectCreationWhenPeriodDoesNotMatchType` | PASS |
| BR-CTR-004 ⚖️ | `Salary.isBelow`, `Contract.ensureSalaryNotBelowMinimumWage` | `shouldRejectCreationWhenSalaryIsBelowMinimumWage`, `shouldAcceptSalaryWhenItEqualsMinimumWage` | PASS |
| BR-CTR-006 | `ContractStatus.isEditable`, `Contract.update` | `shouldRejectUpdateWhenContractIsNotDraft`, `shouldBeEditableOnlyWhenDraft` | PASS |
| BR-CTR-007 | `Contract.renew`, `ContractPeriod.extendTo` | `shouldExtendEndDateAndRecordRenewalWhenContractIsActiveFixedTerm`, `shouldRejectRenewalWhen…` (borrador, indefinido, fecha no posterior); `ContractPeriodTest$Extension` | PASS |
| BR-CTR-009 | `Contract.terminate`, `Termination`, `ContractPeriod.contains` | `shouldAcceptTerminationWhenDateIsOnPeriodBoundaries`, `shouldNormalizeReasonWhenTerminationIsValid`; `ContractPeriodTest$Contains` | PASS |
| §5 (transiciones, `CTR-008`) | `ContractStatus`, `Contract.transitionTo` | `shouldAllowTransitionWhenItIsInTheStateDiagram`, `shouldExpireContractAndEmitEventWhenCutoffIsAfterEndDate`, `shouldCancelContractWithoutEventWhenItIsDraft` | PASS |
| §7 (eventos) | `Contract` (5 eventos) | `shouldCreateDraftContractAndEmitEventWhenDataIsValid`, `shouldActivateContractAndEmitEventWhenSigningDraft`, `shouldClearEventsWhenTheyArePulled` | PASS |
| `CTR-009` (`restore`) | `Contract.restore` | `ContractTest$Restore` (10 pruebas) | PASS |
| BR-EMP-002 | `DocumentType.DNI`, `DocumentNumber` | `shouldRejectDniWhenItDoesNotHaveExactlyEightDigits` | PASS |
| BR-EMP-003 | `Employee.update` (sin cambiar el documento) | `EmployeeTest.shouldReplaceNameAndEmailKeepingIdAndDocumentWhenUpdated` | PASS |
| BR-EMP-005, BR-EMP-008 | — | — | NOT COVERED: **diferido** (W-02 de la ronda 002, documentado como pendiente) |
| BR-USR-002 | `Username` | `shouldNormalizeUsernameToLowercaseWhenItHasUppercaseAndSpaces`, `shouldRejectUsernameWhenFormatIsInvalid` | PASS |
| BR-USR-003 | `PasswordPolicy.validate` | `shouldAcceptPasswordWhenLengthIsWithinLimits`, `shouldCountCodePointsWhenPasswordHasCharactersOutsideTheBmp` | PASS |
| BR-USR-006 (parte de dominio) | `RefreshToken.rotate`, `revoke` | `shouldMarkTokenUsedAndReturnSuccessorInSameFamilyWhenRotated`, `shouldRejectRotationWhenTokenIsRevoked`/`Expired`, `shouldKeepOriginalReasonWhenTokenIsRevokedTwice` | PASS |

## 4. Pruebas existentes y faltantes

- **Existentes:** 209 pruebas en 14 clases. `ContractPeriod` tiene 100 % de líneas y ramas (#14), con 4 mutaciones comprobadas.
- **Líneas sin cubrir en `domain` (JaCoCo):** 19 líneas y 16 ramas, todas menores:
  - la rama `null` de la normalización en 12 value objects;
  - la validación de `null` del constructor de `UserId` y `RefreshTokenId` (I-05);
  - accessors sin uso en pruebas (`Contract.number()`, `Contract.employeeId()`, `RefreshToken.expiresAt()`);
  - `RefreshToken.restore` (I-04);
  - la rama defensiva inalcanzable `Contract.currentEndDate()` → `orElseThrow`: no se puede llegar a ella porque `renew` y `expire` exigen antes `FIXED_TERM`.
- **Faltantes:** las de BR-EMP-005 y BR-EMP-008 cuando se implementen (diferido) e I-05.

## 5. Hallazgos

### ERROR
Ninguno.

### WARNING
Ninguno.

### INFO

**I-01 · `Email` usa una expresión regular con retroceso super-lineal (SonarQube `java:S8786`).**
- **Dónde:** `employee/vo/Email.java:12`, `^[^@\s]+@[^@\s]+\.[^@\s]+$`. El segundo `[^@\s]+` también acepta `.`, así que una entrada sin punto después de `@` se prueba en O(n²).
- **Por qué es INFO:** la API limita el correo a 254 caracteres (`@Size(max = 254)` en `EmployeeRequest.java:25` y `UpdateEmployeeRequest.java:18`). Con ese límite, el costo es despreciable.
- **Posible mejora:** excluir `.` del dominio, por ejemplo `^[^@\s]+@[^@\s.]+(\.[^@\s.]+)+$`. Antes habría que comprobar que acepta y rechaza los mismos casos de `EmployeeValueObjectsTest`, porque cambia qué dominios se aceptan: `a@b..c` dejaría de valer.

**I-02 · El método `Money.pen` choca con la constante `Money.PEN` (SonarQube `java:S1845`).**
- **Dónde:** `shared/vo/Money.java:56`.
- **Por qué es INFO:** es solo legibilidad. Renombrarlo, por ejemplo a `ofPen` como en `Salary` y `MinimumWage`, toca todos sus usos; se haría con `/05-refactorizar`.

**I-03 · `RefreshToken` usa el `Clock` sin `Guard`, a diferencia de `Contract`.**
- **Dónde:** `identity/model/RefreshToken.java:60` (`issue`), `:98` (`rotate`) y `:120` (`revoke`) llaman a `clock.instant()` directamente. `Contract` usa `Guard.notNull(clock, "clock")`.
- **Efecto:** con un `Clock` nulo se lanza `NullPointerException` en lugar de `VAL-001`.
- **Por qué es INFO:** el `Clock` siempre lo inyecta `infrastructure/config`. `architecture.md` §2 exige la regla de «nunca NPE» a los value objects, y `RefreshToken` es una entidad.

**I-04 · `RefreshToken.restore` no valida la coherencia de su estado.**
- **Dónde:** `identity/model/RefreshToken.java:79-84`. Acepta, por ejemplo, `usedAt` sin `replacedBy` o `revokedAt` sin `revocationReason`.
- **Por qué es INFO y no WARNING (como el W-01 de `Contract`):**
  - la comprobación 2 se aplica a los agregados de `business-rules.md` §3, y `RefreshToken` no está entre ellos;
  - un estado incoherente no habilita reutilizar un token: `isUsable` (`:130-131`) solo mira `usedAt`, `revokedAt` y `expiresAt`.
- **Cobertura:** la prueba de integración `RefreshTokenPersistenceAdapterTest` lo ejercita, pero no hay una prueba de dominio.
- **Posible mejora:** las mismas validaciones estructurales que `Contract.restore`, con `CTR-009` o un código de identidad nuevo. Sería una decisión del autor.

**I-05 · Falta probar que `UserId` y `RefreshTokenId` rechazan `null`.**
- **Dónde:** `identity/vo/UserId.java:18-19`, `identity/vo/RefreshTokenId.java:18-19`.
- **Detalle:** `ContractId` y `EmployeeId` sí lo prueban (`of((UUID) null)` en `ContractValueObjectsTest` y `EmployeeValueObjectsTest`). Faltan las dos aserciones equivalentes en `IdentityValueObjectsTest`.

**I-06 · 94 bloques JavaDoc sin línea en blanco entre la descripción y los tags.**
- **Dónde:** 54 archivos de `domain` y `application`, documentados en #16. Ejemplo: `employee/vo/EmployeeId.java:7-10`.
- **Por qué es INFO:** `javadoc -Xdoclint:all` no lo marca como error, pero no sigue la convención de Oracle. Ya figura como pendiente en `docs/sessions/2026-11-13-05-refactorizar-ids.md`.

**Fuera del alcance (para otro modo):** ninguna observación.

## 6. Aspectos positivos

- **Verificación por mutación** en `ContractPeriodTest`: las 4 mutaciones de reglas críticas hacen fallar las pruebas (`ai-log.md` #15).
- **Código muerto eliminado en vez de abstraído** (#18). Es el criterio de `architecture.md` §5 de evitar la sobreingeniería.
- **JavaDoc trazable:** cada `@throws` cita su código de §8 y su regla `BR-*`, verificados contra `business-rules.md`. `javadoc -Xdoclint:all` termina con 0 avisos (#16).
- **`Money` no redondea nunca:** si un monto trae más de 2 decimales, se rechaza en lugar de perder centavos en silencio.

## 7. Conclusión técnica

La capa `domain` está en buen estado después de los cambios de esta ronda: no hay ERROR ni WARNING.
- I-01, I-02 e I-05 son correcciones pequeñas y seguras.
- I-03 e I-04 alinean `RefreshToken` con `Contract`; I-04 requiere decidir qué código de error usar.
- I-06 es solo estilo de documentación.

`/07-corregir` solo trata ERROR y WARNING, así que estos INFO se incluyen únicamente si el autor lo decide, como I-01 e I-02 en la ronda 002.

**Siguiente paso:** `/06-revisar aplicacion` (paso 2 de 9, ronda 003).
