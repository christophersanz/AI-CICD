# Revisión 002 · 8 — Correcciones

- **Prompt:** `docs/prompts/07-corregir.md`, modo `ronda 002` (paso 8 de 9)
- **Fecha:** 2026-11-12 · **Rama:** `fix/revision-002` (sobre `183d054` + informes de los pasos 1–7)
- **Informes de origen:** `01-revision-dominio.md` … `07-revision-integral.md` (matriz consolidada en `002-7` §4)
- **Plan aprobado por el autor:** W-01 con el código nuevo `CTR-009`, W-04 diferido, I-01 e I-02 incluidos.

## 1. Decisión y resultado por hallazgo

Antes de planear, se volvió a verificar cada hallazgo contra el código: `master` seguía en `183d054` y los seis WARNING estaban presentes. No hubo falsos positivos.

| Hallazgo | Decisión | Archivos modificados | Prueba o justificación | Verificado |
|---|---|---|---|:-:|
| W-01 `Contract.restore` sin validar | **Corregido** | `Contract.java` (`restore`), `InconsistentContractStateException.java` (nuevo, `CTR-009`), `GlobalExceptionHandler.java` (`CTR-009` → 500 sin detalles), `business-rules.md` §8 | `ContractTest.Restore`: 10 pruebas; las 7 de rechazo se confirmaron **en rojo** con el código anterior («Expecting code to raise a throwable») y pasan con la corrección. `EmployeeControllerTest.shouldReturnInternalErrorWithoutDetailsWhenPersistedContractIsInconsistent` | Sí |
| W-02 `EmployeeStatus` sin implementar | **Diferido (funcionalidad)** · documentación corregida | `business-rules.md` §3 (`status`) y §4 (BR-EMP-005, BR-EMP-008), marcados como pendientes con `DeactivateEmployeeUseCase` | Implementarlo es una funcionalidad nueva (restricción 1 del prompt) → `/02-implementar-caso-uso DeactivateEmployeeUseCase` | Sí (documentación) |
| W-03 BR-USR-005 sin caso de uso | **Diferido (decisión de negocio)** · documentación corregida | `business-rules.md` §4 (BR-USR-005, pendiente) y §9 (pregunta abierta nueva); `technical.md` §1 y §3.3 | Definir los casos de uso es una decisión de negocio (restricción 4) → `/01-definir reglas cambio-de-contraseña` | Sí (documentación) |
| W-04 sin `@Version` | **Diferido**, con un motivo actualizado (decisión del autor) | — | Ver §3 | — |
| W-05 `technical.md:24` | **Corregido** | `technical.md` §1 | La frase ahora coincide con §2.1, §3 y el código (`@PreAuthorize` en empleados y usuarios) | Sí |
| W-06 `identity` ausente en §2 | **Corregido** | `architecture.md` §2 | Lista `contract, employee, identity` y sus raíces (`User`, `RefreshToken`, `Role`), comprobadas contra `domain/identity/model` | Sí |
| I-01 `User.restore` sin prueba | **Corregido** | `UserTest.java` | `shouldKeepPersistedStateWithoutEventsWhenDisabledUserIsRestored` | Sí |
| I-02 `401` en la tabla de endpoints | **Corregido** | `technical.md` §2.1 | El `401` aparece en los cuatro endpoints de empleados, como en el contrato OpenAPI | Sí |

Los demás INFO (I-03 a I-08) quedan fuera de esta corrección: el prompt corrige ERROR y WARNING, y el autor solo incluyó I-01 e I-02.

## 2. Archivos

- **Creados:**
  - `domain/…/contract/exception/InconsistentContractStateException.java` (W-01)
  - `docs/sessions/2026-11-12-07-corregir-ronda-002.md`
  - este informe
- **Modificados:**
  - `domain/…/contract/model/Contract.java` (W-01)
  - `domain/…/contract/model/ContractTest.java` (W-01)
  - `domain/…/identity/model/UserTest.java` (I-01)
  - `infrastructure/…/api/rest/error/GlobalExceptionHandler.java` (W-01)
  - `infrastructure/…/api/rest/EmployeeControllerTest.java` (W-01)
  - `docs/business-rules.md` (W-01 en §8, W-02, W-03)
  - `docs/technical.md` (W-03, W-05, I-02)
  - `docs/architecture.md` (W-06)
- **Movidos o eliminados:** ninguno.

## 3. Diferidos deliberadamente

`/06-revisar final` no debe reportarlos como defectos, salvo que contradigan una regla obligatoria.

| Hallazgo | Motivo | Cómo se resolverá |
|---|---|---|
| W-04 · bloqueo optimista en `Employee` | **Riesgo aceptado** mientras haya pocas ediciones simultáneas de un mismo empleado. Hacerlo bien cambia el contrato REST (`If-Match` o un campo `version`), requiere una migración `V6` y un código de error `409` nuevo, y eso es una decisión del autor. Reemplaza al diferido de la ronda 001, cuyo motivo («`Employee` es inmutable») ya no aplicaba. | `/01-definir reglas concurrencia` y luego `/02-implementar-caso-uso` |
| W-02 · `EmployeeStatus` (implementación) | Funcionalidad nueva; ya está documentada como pendiente | `/02-implementar-caso-uso DeactivateEmployeeUseCase` |
| W-03 · casos de uso que disparan BR-USR-005 | Decisión de negocio; ya figura como pregunta abierta en §9 | `/01-definir reglas cambio-de-contraseña` |
| I-03 a I-08 | INFO no incluidos por el autor | Ver `07-revision-integral.md` §4 |

Falsos positivos: ninguno.

## 4. Verificación

- **Regla de dependencia de los archivos tocados:** `Contract` e `InconsistentContractStateException` siguen importando solo `java.*` y `…domain.*`. `GlobalExceptionHandler` importa la excepción de dominio, igual que las demás que ya traducía.
- **`./mvnw clean verify`:** BUILD SUCCESS.

  | Módulo | Pruebas | Fallidas | Con error | Omitidas | Cobertura de líneas |
  |---|---:|---:|---:|---:|---:|
  | domain | 173 (antes 163) | 0 | 0 | 0 | 95,8 % (antes 95,3 %) |
  | application | 46 | 0 | 0 | 0 | 96,7 % |
  | infrastructure | 154 (antes 153) | 0 | 0 | 0 | 96,3 % |
  | **Total** | **373** (antes 362) | 0 | 0 | 0 | |

- **Jenkins local** contra la rama (`fix/revision-002`, build #17): las 8 etapas en SUCCESS. Quality Gate: bugs 0, vulnerabilidades 0, rating A y cobertura 94,4 % (antes 94,2 %) → OK. Al terminar, el job volvió a apuntar a `master`.

## 5. Pendientes

Sin problemas pendientes de esta corrección. Siguiente paso: `/06-revisar final` (paso 9 de 9).
