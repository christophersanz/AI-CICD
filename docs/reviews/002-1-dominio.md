# Revisión 002 · 1 — Dominio

- **Prompt:** `docs/prompts/06-revisar.md`, modo `dominio` (paso 1 de 9 de la ronda 002)
- **Fecha:** 2026-11-12 · **Commit revisado:** `183d054` (`master`)
- **Alcance:** paquete `pe.edu.galaxy.training.ia.ca.domain` (51 clases) y sus pruebas (14 clases, 163 pruebas)

## 1. Resumen

La capa de dominio cumple la regla de dependencia y las invariantes que `business-rules.md` asigna a `domain` y están implementadas: es Java puro, los value objects se validan en el constructor, las transiciones de `Contract` siguen §5 con los códigos documentados y los eventos se fechan con un `Clock`. No hay ERROR. Hay dos WARNING: `Contract.restore(...)` acepta estados inconsistentes sin advertirlo, y el estado del empleado (BR-EMP-005 y BR-EMP-008) figura como atributo en `business-rules.md` §3 pero todavía no existe en el código.

**Build:** `./mvnw clean verify` → BUILD SUCCESS. 362 pruebas: 163 en domain, 46 en application y 153 en infrastructure; 0 fallos, 0 errores, 0 omitidas.

| Módulo | Líneas | Ramas |
|---|---:|---:|
| domain | 95,3 % | 89,8 % |
| application | 96,7 % | 92,9 % |
| infrastructure | 96,3 % | 75,4 % |

**Quality Gate (CI local):** Jenkins #16 sobre `183d054`: bugs 0, vulnerabilidades 0, rating A, cobertura 94,2 % → OK.

## 2. Comprobaciones

| # | Comprobación | Severidad | Archivo/componente | Evidencia | Regla o criterio | Corrección propuesta |
|---|---|---|---|---|---|---|
| 1 | Java puro, sin dependencias de otras capas | OK | `domain/**`, `domain/pom.xml` | Los imports de `main` son solo `java.*` y `…domain.*`; el `pom.xml` solo tiene `junit-jupiter` y `assertj-core` (*test*) | `architecture.md` §3.2 | — |
| 2 | Agregados protegen sus invariantes, sin setters | WARNING | `Contract.java:86-90` | Sin setters públicos; los cambios pasan por `update`, `sign`, `cancel`, `renew`, `terminate` y `expire`. Pero `restore(...)` construye sin validar la consistencia entre `status`, `termination`, `renewals`, `type` y `period` | `architecture.md` §5 (las entidades exponen comportamiento); modelos del curso (invariantes de `reconstitute`) | Ver W-01 |
| 3 | Value objects validados; `Money` con `BigDecimal` y escala fija | OK | `shared/vo/Money.java`, `contract/vo/*`, `employee/vo/*`, `identity/vo/*` | `Guard.notNull` y excepciones de dominio; `ContractId.of`/`EmployeeId.of` convierten `IllegalArgumentException` en `InvalidValueException`; `Money` rechaza negativos y más de 2 decimales y fija la escala 2; sin `float`/`double` | ADR-0004; `architecture.md` §5 | — |
| 4 | Transiciones de estado según §5 | OK | `ContractStatus.java`, `Contract.java:106-163` | `nextStatuses()` reproduce el diagrama; transición inválida → `CTR-008`; `renew()` → `CTR-005`; `expire()` exige `FIXED_TERM`, `ACTIVE` y fecha de corte posterior al fin | `business-rules.md` §5 | — |
| 5 | Eventos de §7 emitidos por el agregado, con `Clock` | OK | `contract/event/*`, `Contract.java` | `ContractEvent` sellado con los 5 eventos de §7; `eventId`, `occurredOn` y `aggregateId()`; `now(clock)` usa `clock.instant()`; no hay `now()` del sistema; `cancel()` no emite evento (§7 no lo define) | `business-rules.md` §7; `architecture.md` §2 | — |
| 6 | Dependencias entre paquetes (ADR-0004) | OK | imports de `common`, `shared`, `contract`, `employee`, `identity` | `common` no importa a nadie; `shared` solo a `common.exception`; `contract` usa de `employee` solo `EmployeeId`; `employee` e `identity` no importan otros agregados | ADR-0004 | — |
| 7 | Excepciones con el código del catálogo | OK | `*/exception/*` | Códigos `CTR-001`, `002`, `004`, `005`, `007`, `008`, `EMP-002`, `VAL-001`, `AUTH-005` y `AUTH-008`, todos en §8; sin conceptos HTTP, JPA ni SQL | `business-rules.md` §8; `architecture.md` §7 | — |
| 8 | Sin patrones fuera del alcance | OK | `domain/**` | Sin CQRS, *event sourcing*, *specifications* ni servicios de dominio innecesarios | `CLAUDE.md` §4 | — |
| 9 | Pruebas de dominio sin Spring ni mocks | OK | `domain/src/test/**` | 163 pruebas JUnit 5 + AssertJ, sin Spring ni Mockito; transiciones parametrizadas (`@CsvSource`, `@EnumSource`) y `@Nested` en `ContractTest` | `architecture.md` §9 | Ver I-01 |

## 3. Trazabilidad de reglas del dominio

| Regla | Implementación | Prueba | Estado |
|---|---|---|---|
| BR-CTR-002 | `ContractPeriod` (fecha de inicio obligatoria) | `ContractPeriodTest.shouldRejectPeriodWhenStartDateIsMissing` | PASS |
| BR-CTR-003 | `ContractPeriod.isCompatibleWith`, `Contract.ensurePeriodMatchesType` | `ContractTest.shouldRejectCreationWhenPeriodDoesNotMatchType` | PASS |
| BR-CTR-004 | `Contract.ensureSalaryNotBelowMinimumWage`, `Salary.isBelow` | `ContractTest.shouldRejectCreationWhenSalaryIsBelowMinimumWage`, `…UpdateWhenSalaryIsBelowMinimumWage`, `…AcceptSalaryWhenItEqualsMinimumWage` | PASS |
| BR-CTR-006 | `ContractStatus.isEditable`, `Contract.update` | `ContractTest.shouldRejectUpdateWhenContractIsNotDraft` | PASS |
| BR-CTR-007 | `Contract.renew` | `ContractTest.shouldRejectRenewalWhenContractIsDraft`, `…RenewalWhenNewEndDateIsNotAfterPrevious` | PASS |
| BR-CTR-009 | `Termination`, `Contract.terminate` | `ContractTest.shouldRejectTerminationWhenDateIsOutsidePeriod`, `…ReasonIsBlank`, `…AcceptTerminationWhenDateIsOnPeriodBoundaries` | PASS |
| §5 (CTR-008) | `ContractStatus.canTransitionTo`, `Contract.ensureCanTransitionTo` | `ContractStatusTest` (parametrizadas por estado), `ContractTest.shouldRejectSignWhenContractIsNotDraft` | PASS |
| BR-EMP-002 | `DocumentNumber` / `DocumentType` | `EmployeeValueObjectsTest.shouldRejectDniWhenItDoesNotHaveExactlyEightDigits` | PASS |
| BR-EMP-003 | `Employee.update` (sin cambio de documento) | `EmployeeTest.shouldReplaceNameAndEmailKeepingIdAndDocumentWhenUpdated` | PASS |
| BR-EMP-005 | — | — | NOT COVERED (ver W-02) |
| BR-EMP-008 | — | — | NOT COVERED (ver W-02) |
| BR-USR-002 | `Username` | `IdentityValueObjectsTest.shouldNormalizeUsernameToLowercaseWhenItHasUppercaseAndSpaces` | PASS |
| BR-USR-003 | `PasswordPolicy` | `PasswordPolicyTest.shouldRejectPasswordWhenShorterThanTwelveCharacters`, `…LongerThanSixtyFourCharacters` | PASS |
| BR-USR-006 (parte de dominio) | `RefreshToken` (rotación y familia) | `RefreshTokenTest.shouldMarkTokenUsedAndReturnSuccessorInSameFamilyWhenRotated` | PASS |

Las reglas que §4 asigna a `application` (BR-CTR-001, 005, 008, 010, 012, BR-EMP-001, 004, 006, 007 y BR-USR-001, 004, 005, 007) no se evalúan aquí: corresponden al paso 2 (`aplicacion`).

## 4. Pruebas existentes y faltantes

- **Existentes:** 14 clases y 163 pruebas. Cubren todas las invariantes implementadas, los límites (salario igual a la RMV, término en los bordes del periodo, contraseña de 12 y 64 caracteres) y la regresión de la regex de `Permission` con entradas largas.
- **Faltantes:** prueba de `User.restore(...)` (I-01); pruebas de BR-EMP-005 y BR-EMP-008 cuando se implementen (W-02).

## 5. Hallazgos

### ERROR
Ninguno.

### WARNING

**W-01 · `Contract.restore(...)` acepta estados inconsistentes sin advertirlo**
- **Archivo:** `domain/src/main/java/pe/edu/galaxy/training/ia/ca/domain/contract/model/Contract.java:86-90`
- **Regla:** las entidades protegen sus invariantes (`architecture.md` §5); los modelos del curso marcan este caso como riesgo en `reconstitute()`.
- **Evidencia:** el JavaDoc dice: «sin volver a … validar reglas de negocio: se asume que el estado persistido ya las cumplía». El constructor solo comprueba que no haya `null`. Se puede reconstruir un contrato `TERMINATED` sin `termination`, uno `ACTIVE` con `termination`, uno `INDEFINITE` con renovaciones, o uno con un `period` incompatible con su `type`.
- **Explicación:** un dato corrupto en la base (migración, carga manual) pasaría al dominio sin error y fallaría más tarde y lejos de su origen. No conviene volver a validar BR-CTR-004, porque depende de la RMV vigente y un contrato antiguo puede estar legítimamente por debajo de la actual. Las invariantes **estructurales** sí se pueden comprobar.
- **Corrección propuesta:** validar en `restore` la consistencia estructural y lanzar `InvalidContractStatusTransitionException` o una excepción de integridad del dominio:

```java
// Antes
public static Contract restore(...) {
    return new Contract(id, number, employeeId, type, jobTitle, salary, period, status, renewals, termination);
}

// Después (sin revalidar BR-CTR-004, que depende de la RMV vigente)
public static Contract restore(...) {
    ensurePeriodMatchesType(period, type);
    if ((status == ContractStatus.TERMINATED) != (termination != null)) { throw ...; }
    if (!renewals.isEmpty() && !type.isRenewable()) { throw ...; }
    return new Contract(id, number, employeeId, type, jobTitle, salary, period, status, renewals, termination);
}
```

**W-02 · El estado del empleado (BR-EMP-005, BR-EMP-008) está documentado como atributo pero no existe en el código**
- **Archivo:** `domain/src/main/java/pe/edu/galaxy/training/ia/ca/domain/employee/model/Employee.java` (sin `status`); `docs/business-rules.md` §2 (`EmployeeStatus`), §3 (atributo `status`), §4 (BR-EMP-005 y 008, capa `domain`) y §8 (`EMP-005`).
- **Evidencia:** no hay `EmployeeStatus`, `deactivate()` ni `reactivate()` en ningún módulo. `business-rules.md` §6, detalle de `ListEmployeesUseCase`, indica: «El filtro por estado se incorpora junto con `DeactivateEmployeeUseCase`; hasta entonces todos los empleados están activos».
- **Explicación:** es un pendiente documentado (los casos de uso `Deactivate`/`ReactivateEmployeeUseCase` no están implementados), así que no es ERROR. Pero §3 presenta `status` como atributo actual y quien lea el documento puede suponer que existe.
- **Corrección propuesta (elegir una):**
  1. Implementarlo con `/02-implementar-caso-uso DeactivateEmployeeUseCase`, que añade `EmployeeStatus`, `deactivate()`/`reactivate()`, `EMP-005` y su migración; o
  2. Mientras no se implemente, marcar en `business-rules.md` §3 la fila `status` y en §4 BR-EMP-005 y BR-EMP-008 como *pendientes (con `DeactivateEmployeeUseCase`)*.

### INFO

**I-01 · `User.restore(...)` no tiene prueba propia.** `Employee.restore` y `Contract.restore` sí la tienen (`EmployeeTest.shouldBeEqualWhenRestoredWithSameId`; `ContractTest`). Agregar una prueba equivalente en `UserTest` cubriría el caso `enabled = false`.

## 6. Aspectos positivos

- Dominio sin dependencias de framework, verificado en los imports y en el `pom.xml`.
- La máquina de estados está en el enum (`ContractStatus.nextStatuses`) y no en `switch` dispersos, con pruebas parametrizadas por estado.
- Eventos sellados por agregado y fechados con `Clock`, lo que facilita las pruebas deterministas.
- `Money` estricto: escala fija, sin negativos y con `RoundingMode.UNNECESSARY`, que evita redondeos silenciosos.
- La pregunta abierta de §9 sobre los motivos de término se resuelve de forma neutral, con un TODO que la referencia (`Termination.java`).

## 7. Conclusión y siguiente paso

El dominio cumple las reglas obligatorias de arquitectura y las de negocio implementadas. W-01 es una mejora de robustez y W-02 es una decisión pendiente: implementar la desactivación o marcarla como pendiente en la documentación.

**Siguiente paso del orden:** `/06-revisar aplicacion` (paso 2 de 9, ronda 002). Al terminar la ronda, los WARNING se corrigen con `/07-corregir ronda 002`.
