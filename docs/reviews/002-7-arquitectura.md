# Revisión 002 · 7 — Arquitectura integral

- **Prompt:** `docs/prompts/06-revisar.md`, modo `arquitectura` (paso 7 de 9 de la ronda 002)
- **Fecha:** 2026-11-12 · **Commit revisado:** `183d054` (`master`)
- **Alcance:** todo el repositorio, apoyándose en los informes de los pasos 1 a 6 de la ronda (`002-1-dominio.md` … `002-6-cicd.md`)

## 1. Resumen

`contract-api` cumple la arquitectura definida:
- La regla de dependencia se cumple con las dependencias **reales** (los `pom.xml` y los imports).
- Puertos y adaptadores están bien orientados y los modelos de cada capa, separados.
- Las reglas de negocio están implementadas en la capa que indica `business-rules.md`.
- No hay patrones fuera del alcance.

**La ronda 002 termina con 0 ERROR, 6 WARNING y 8 INFO.** Cuatro de los WARNING tienen la misma causa: la documentación no se actualizó en el mismo cambio que el código (§5).

**Build:** el de la ronda (paso 1) sobre `183d054`: BUILD SUCCESS, 362 pruebas, 0 fallos. **Quality Gate:** Jenkins #16, OK.

## 2. Comprobaciones

| # | Comprobación | Severidad | Archivo/componente | Evidencia | Regla o criterio | Corrección propuesta |
|---|---|---|---|---|---|---|
| 1 | Regla de dependencia con dependencias reales, incluida la regla 9 | OK | `*/pom.xml`, imports de `main` | `domain` → nada; `application` → `domain`; `infrastructure` → `application` (y `domain` de forma transitiva). Dentro de `infrastructure`: `api` → `config`; `config` → `security` (para registrar beans); `persistence` y `security` → nada | `architecture.md` §3.1, §3.9 | — |
| 2 | Puertos y adaptadores bien orientados | OK | pasos 2–4 | Controller → `UseCase` (10); `Port` ← adaptador (7 de 7) | `architecture.md` §3.7 | — |
| 3 | Separación de modelos | OK | pasos 1–4 | Dominio, `Command`/`Query`/`Result` (records), Request/Response y entidades `*JpaEntity`, todos distintos | `architecture.md` §3.4, §3.5 | — |
| 4 | Nombres y paquetes según §2 y §5 | WARNING | `docs/architecture.md:65` | Los nombres de clases siguen §5. Pero §2 lista como agregados «contract, employee», y el código tiene además `identity` (`User`, `RefreshToken`, `Username`, `PasswordPolicy`…) | `architecture.md` §2 | Ver W-06 |
| 5 | Cada regla `BR-*` en su capa | OK | trazabilidad completa (§3) | Las reglas implementadas están en la capa de §4; las pendientes están documentadas en `technical.md` §1 | `business-rules.md` §4 | — |
| 6 | Sin lógica de negocio en controllers, mappers, repositorios ni adaptadores | OK | pasos 3 y 4 | Mappers y controllers solo traducen; las reglas viven en el dominio y en los casos de uso | `architecture.md` §3.8 | — |
| 7 | Sin patrones ni módulos fuera del alcance | OK | todo el repositorio | 3 módulos; sin CQRS, *event sourcing*, *mediator*, *specification* de dominio ni microservicios. `EmployeeSpecifications` es un mecanismo técnico de Spring Data en `persistence`, no un patrón de dominio | `CLAUDE.md` §4 | — |
| 8 | Coherencia entre código y documentos | WARNING | `business-rules.md` §3/§4, `technical.md:24`, `architecture.md` §2 | Ver W-02, W-03, W-05 y W-06 | `CLAUDE.md` §4 («actualiza la documentación en el mismo cambio») | Ver §5 |
| 9 | Hallazgos con causa común | — | ronda 002 | 4 de 6 WARNING son documentación desfasada respecto del código | — | Ver §5 |

## 3. Trazabilidad completa (`business-rules.md` §4)

| Regla | Capa | Implementación | Prueba | Estado |
|---|---|---|---|---|
| BR-CTR-001, 005, 008, 010 | application | — | — | NOT COVERED: pendiente documentado (sin casos de uso de contratos) |
| BR-CTR-002, 003, 004, 006, 007, 009, §5 | domain | `Contract`, `ContractPeriod`, `ContractStatus`, `Salary`, `Termination` | `ContractTest`, `ContractPeriodTest`, `ContractStatusTest` | PASS |
| BR-CTR-011 | api | — | — | NOT COVERED: pendiente documentado |
| BR-CTR-012 | application | — | — | NOT COVERED: pendiente documentado |
| BR-EMP-001 | application | `RegisterEmployeeService` + `uk_employees_document` | `RegisterEmployeeServiceTest`, `EmployeePersistenceAdapterTest` | PASS |
| BR-EMP-002, 003 | domain | `DocumentNumber`, `Employee.update` | `EmployeeValueObjectsTest`, `EmployeeTest` | PASS |
| BR-EMP-004 | application | `EmployeeSearchQuery`, `ListEmployeesService`, `EmployeeSpecifications` | `ListEmployeesServiceTest`, `EmployeePersistenceAdapterTest` | PASS |
| BR-EMP-005, 008 | domain | — | — | NOT COVERED (W-02) |
| BR-EMP-006, 007 | application | — | — | NOT COVERED: pendiente documentado |
| BR-USR-001 | application | `CreateUserService` + `uk_users_username` | `CreateUserServiceTest`, `IdentityPersistenceAdapterTest` | PASS |
| BR-USR-002, 003 | domain | `Username`, `PasswordPolicy` | `IdentityValueObjectsTest`, `PasswordPolicyTest` | PASS |
| BR-USR-004 | application | `AuthenticateUserService`, `RefreshTokenService` | `AuthenticateUserServiceTest`, `AuthenticationFlowIntegrationTest` | PASS |
| BR-USR-005 | application | — | — | NOT COVERED (W-03) |
| BR-USR-006 | domain/application | `RefreshToken`, `RefreshTokenService` | `RefreshTokenTest`, `RefreshTokenServiceTest`, `AuthenticationFlowIntegrationTest` | PASS |
| BR-USR-007 | application | `BootstrapAdministratorService` + `BootstrapAdministratorRunner` | `BootstrapAdministratorServiceTest`, `BootstrapAdministratorIntegrationTest` | PASS |

## 4. Matriz consolidada de la ronda 002

| Id | Severidad | Capa | Componente | Hallazgo | Informe de origen | Acción |
|---|---|---|---|---|---|---|
| W-01 | WARNING | domain | `Contract.restore` | Acepta estados inconsistentes sin advertirlo | 002-1 | Validar invariantes estructurales en `restore` |
| W-02 | WARNING | domain / docs | `Employee`, `business-rules.md` §3–§4 | `EmployeeStatus` (BR-EMP-005, 008) documentado como atributo pero no implementado | 002-1 | Implementar `DeactivateEmployeeUseCase` o marcarlo como pendiente en §3/§4 |
| W-03 | WARNING | application / docs | `business-rules.md` §4.1/§6, `technical.md` §3.3 | BR-USR-005 sin caso de uso que la dispare y sin figurar como pendiente | 002-2 | Definir `ChangeOwnPassword`/`DisableUser` (`/01-definir reglas`) o anotarlo como pendiente |
| W-04 | WARNING | infrastructure | `EmployeeJpaEntity` | Sin `@Version`; el motivo del diferido de la ronda 001 («`Employee` es inmutable») ya no aplica | 002-3 | Bloqueo optimista + `409`, o renovar el diferido con un motivo actual |
| W-05 | WARNING | docs | `technical.md:24` | Dice que la autorización por permisos está «solo en `/api/v1/users`» | 002-4 | Corregir la frase |
| W-06 | WARNING | docs | `architecture.md:65` | §2 no menciona el agregado `identity` | 002-7 | Agregar `identity` a §2 |
| I-01 | INFO | domain | `UserTest` | `User.restore` sin prueba | 002-1 | Agregar la prueba |
| I-02 | INFO | docs | `technical.md` §2.1 | No lista el `401` en los endpoints de empleados | 002-4 | Alinear la tabla con el contrato |
| I-03 | INFO | security | — | CORS sin configurar (pendiente del diseño, paso 2.4) | 002-5 | Implementarlo en la fase 2 |
| I-04 | INFO | repo | historial de git | Contraseñas de prueba local en el historial | 002-5 | No reutilizarlas fuera de local |
| I-05 | INFO | ci | pipeline | Sin escaneo de CVE (control No evaluado) | 002-5 | Agregar Trivy con `/08-cambiar-pipeline` |
| I-06 | INFO | ci | `ci-cd.yml` | Acciones fijadas por versión mayor, no por SHA | 002-6 | SHA + Dependabot |
| I-07 | INFO | ci | `docker/jenkins/Dockerfile` | `hadolint DL3008` (`jq` sin versión) | 002-6 | Fijar la versión o justificar la excepción |
| I-08 | INFO | ci | job `deploy` | El despliegue en Render nunca se ejecutó (No evaluado) | 002-6 | Configurar Render (`docs/guides/ci-cd.md` §5.4) |

## 5. Causa común

**W-02, W-03, W-05 y W-06 son la misma falla de proceso.** El código cambió (la fase de seguridad, la autorización por permisos, el agregado `identity`) y los documentos que lo describen no se actualizaron en el mismo cambio, aunque `CLAUDE.md` §4 lo exige. Los PR de esas funcionalidades se integraron antes de que existiera `/06-revisar pr`, cuya comprobación 4 («documentación del mismo cambio») lo habría detectado. Recomendación: usar `/06-revisar pr <n>` antes de cada merge.

## 6. Patrones y alcance

No se introdujo ningún patrón, *framework* ni funcionalidad fuera de lo definido. Las dependencias nuevas de la última etapa (SonarQube y Jenkins en `docker/jenkins/`) son de infraestructura local y están justificadas en ADR-0014 y ADR-0015.

## 7. Pruebas

362 pruebas: 163 de dominio sin Spring, 46 de aplicación con Mockito sobre puertos y 153 de infraestructura (slices web, integración con PostgreSQL real y seguridad). La cobertura de líneas está entre 95,3 % y 96,7 % por módulo, sobre el umbral del 80 %. Los vacíos son de funcionalidad pendiente, no de pruebas de lo implementado.

## 8. Conclusión y siguiente paso

El proyecto cumple técnicamente `CLAUDE.md`, `architecture.md` y `business-rules.md` en lo implementado. Los 6 WARNING se reparten así: una mejora de robustez (W-01), un riesgo de concurrencia (W-04) y cuatro desfases de documentación (W-02, W-03, W-05 y W-06).

**Siguiente paso del orden:** `/07-corregir ronda 002` (paso 8 de 9). Es el único paso que modifica código: presentará el plan con la decisión sobre cada hallazgo (corregir, diferir o falso positivo) y esperará tu aprobación antes de cambiar nada. Después, `/06-revisar final`.
