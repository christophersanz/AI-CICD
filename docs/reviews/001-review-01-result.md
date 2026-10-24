# Resultado de revisión 001-review-01

- **Prompt:** `docs/prompts/001-review-01.md` (ejecutado tal como está, antes de corregirlo)
- **Alcance:** todo el repositorio (iteración 1: dominio completo + `RegisterEmployeeUseCase`)
- **Fecha:** 2026-09-26
- **Fuentes de verdad:** `CLAUDE.md`, `docs/architecture.md`, `docs/business-rules.md`, `docs/adr/0001–0003`

## 0. Resultado de `./mvnw clean verify`

| Módulo | Pruebas | Fallos | Omitidas | Cobertura líneas | Cobertura instrucciones | Cobertura ramas |
|---|---|---|---|---|---|---|
| domain | 64 | 0 | 0 | 94,2 % | 92,5 % | 80,6 % |
| application | 4 | 0 | 0 | 90,0 % | 94,4 % | 100 % |
| infrastructure | 9 | 0 | 0 | 92,7 % | 94,3 % | 50,0 % |

- **BUILD SUCCESS**. Las pruebas de Testcontainers corrieron contra PostgreSQL 16 (Docker disponible).
- La cobertura se midió invocando JaCoCo 0.8.14 por línea de comandos; **el proyecto no tiene JaCoCo configurado** en el `pom.xml` (ver H-15).
- Un primer intento de build terminó con código 1 sin salida capturada; el reintento inmediato fue exitoso. No se pudo determinar la causa.

## 1. Resumen ejecutivo

La base es sólida: la regla de dependencia se cumple y la garantiza la estructura de módulos Maven, el dominio es Java puro con invariantes en value objects y agregados, y el único caso de uso implementado funciona de extremo a extremo con errores RFC 9457. El hallazgo más serio es **operativo**: se registran datos personales (DNI) en los logs, lo que viola `architecture.md` §8. Hay **dos contradicciones entre el prompt y la documentación** (ArchUnit y `@Transactional`) que deben resolverse en el prompt, no en el código. La mayoría de las reglas de contrato que dependen de la capa de aplicación (BR-CTR-001, 005, 008, 010) aún no están implementadas porque sus casos de uso no existen.

| Dimensión | Nota (1–5) | Hallazgos críticos/altos | Comentario |
|---|---|---|---|
| Arquitectura | 4 | 0 | Reglas 1–9 cumplidas. Dos contradicciones del prompt con ADR/architecture.md. |
| Reglas de negocio | 4 | 0 | Todo lo del dominio implementado y probado; una regla no documentada (cambio de tipo). |
| Calidad de código | 4 | 0 | Java 21 idiomático; `restore()` no valida invariantes. |
| API REST | 4 | 0 | Códigos y Problem Details correctos; 400 sin `code` y con `about:blank`. |
| Seguridad | — | — | **No evaluado** como dimensión: diferida por ADR-0001 y sin sección en el prompt (salta de 4 a 6). |
| Persistencia | 4 | 0 | Flyway consistente con JPA (validado con `ddl-auto: validate`). Contratos no evaluables aún. |
| Pruebas | 4 | 0 | Buena estrategia por capa; falta prueba de contexto completo (`UseCaseConfig` sin cubrir). |
| Operación y configuración | 3 | 1 | Datos personales en logs; faltan perfiles `dev`/`prod`. |

## 2. Trazabilidad de reglas de negocio

| Regla | Implementada en (clase/método) | Capa correcta | Prueba | Estado |
|---|---|---|---|---|
| BR-CTR-001 | — (requiere `CreateContractUseCase`) | — | — | ❌ Pendiente |
| BR-CTR-002 | `ContractPeriod.of` | ✅ domain | `ContractPeriodTest.shouldRejectPeriodWhenStartDateIsMissing` | ✅ |
| BR-CTR-003 | `ContractPeriod.of`, `Contract.ensurePeriodMatchesType` | ✅ domain | `ContractPeriodTest` (4), `ContractTest.Create` | ✅ |
| BR-CTR-004 ⚖️ | `Contract.ensureSalaryNotBelowMinimumWage`, `Salary.isBelow` | ✅ domain | `ContractTest.Create`, `ContractTest.Update`, `MoneyAndSalaryTest` | ⚠️ Jornada parcial sin definir (pregunta abierta) |
| BR-CTR-005 | Solo soporte: `ContractPeriod.overlaps` | — | `ContractPeriodTest` (overlap) | ❌ Orquestación pendiente |
| BR-CTR-006 | `Contract.update` | ✅ domain | `ContractTest.Update.shouldRejectUpdateWhenContractIsNotDraft` | ✅ |
| BR-CTR-007 | `Contract.renew` | ✅ domain | `ContractTest.Renew` (4) | ✅ |
| BR-CTR-008 ⚖️ | — (requiere `RenewContractUseCase`) | — | — | ❌ Pendiente |
| BR-CTR-009 | `Contract.terminate`, `Termination` | ✅ domain | `ContractTest.Terminate` (6) | ✅ |
| BR-CTR-010 | Formato: `ContractNumber.of`. Generación/unicidad: — | ✅ / — | `ContractNumberTest` | ⚠️ Parcial |
| BR-CTR-011 | — | — | — | ⏸️ Diferida (ADR-0001) |
| BR-CTR-012 | — | — | — | ⏸️ Diferida (ADR-0001) |
| BR-EMP-001 | `RegisterEmployeeService.register`, `EmployeePersistenceAdapter.save`, `uk_employees_document` | ✅ application + BD | `RegisterEmployeeServiceTest`, `EmployeePersistenceAdapterTest` | ✅ |
| BR-EMP-002 | `DocumentNumber` | ✅ domain | `EmployeeTest` (parametrizada) | ✅ |
| Estados §5 | `Contract.sign/cancel/renew/terminate/expire` | ✅ domain | `ContractTest.SignAndCancel`, `Expire`, etc. | ✅ Transiciones inválidas → `CTR-008` (excepto `renew` → `CTR-005`, documentado) |

**Catálogo de errores:** los códigos del dominio (CTR-001, 002, 004, 005, 007, 008, EMP-002, VAL-001) y EMP-001 existen con el HTTP correcto (422 / 409 para CTR-008 / 409 para conflictos). Aún no existen las excepciones `CTR-003`, `CTR-006`, `CTR-404` y `EMP-404` (sus casos de uso no están implementados).

## 3. Hallazgos

Ordenados por severidad. **Violación** = incumple una regla documentada; **Recomendación** = mejora opcional; **Contradicción** = la documentación se contradice entre sí.

| # | Severidad | Dimensión | Tipo | Archivo:línea | Problema | Regla/principio | Corrección propuesta |
|---|---|---|---|---|---|---|---|
| H-01 | **Alta** | Operación | Violación | `infrastructure/src/main/resources/application.yml` (sin configuración de log de Hibernate) | Al violar la restricción única, `SqlExceptionHelper` registra en ERROR: `Key (document_type, document_number)=(DNI, 87654321) already exists`. El DNI es un dato personal. Evidencia: salida de `EmployeePersistenceAdapterTest`. | `architecture.md` §8: "nunca registrar datos personales" | Silenciar `org.hibernate.engine.jdbc.spi.SqlExceptionHelper`; el adaptador ya traduce la excepción a `EMP-001`. |
| H-02 | Media | Arquitectura | Contradicción | `docs/prompts/001-review-01.md:28` | El prompt exige `ArchitectureTest` con ArchUnit; no existe porque ADR-0003 lo descartó y `CLAUDE.md` lo quitó del stack. | ADR-0003 | **Corregir el prompt**, no el código. |
| H-03 | Media | Arquitectura | Contradicción | `docs/prompts/001-review-01.md:24` vs `application/pom.xml` (`spring-tx`), `RegisterEmployeeService.java:15` | El prompt prohíbe cualquier framework en `application`; el código usa `@Transactional`, que `architecture.md` regla 3 y §8 permiten. | `architecture.md` regla 3 | **Corregir el prompt**. Alternativa (requiere ADR): transacciones con `TransactionTemplate` desde `infrastructure/config`. |
| H-04 | Media | Negocio | Violación | `domain/.../model/Contract.java:82` | `update()` permite cambiar el `type` del contrato. Ninguna regla lo autoriza ni prohíbe. | `CLAUDE.md` §4: "No inventes reglas de negocio" | Confirmar con negocio; si no aplica, quitar `newType`. Registrar la pregunta en §9. |
| H-05 | Media | Calidad | Violación | `domain/.../model/Contract.java:68` | `restore()` no verifica que `termination` exista solo si el estado es `TERMINATED`, ni que `renewals` sea vacío en `INDEFINITE`. | `business-rules.md` §3 (atributos de `Contract`) | Validar ambas invariantes en el constructor privado. |
| H-06 | Media | Pruebas | Recomendación | `infrastructure/.../config/UseCaseConfig.java` (0 % cubierto según JaCoCo) | Ninguna prueba levanta el contexto completo: el registro del bean, el proxy `@Transactional` y el flujo controller → BD solo se verificaron manualmente. | `architecture.md` §9 | Añadir `@SpringBootTest` + Testcontainers para `POST /api/v1/employees`. |
| H-07 | Media | Operación | Recomendación | `infrastructure/.../api/rest/error/GlobalExceptionHandler.java:50` | `log.error("Unexpected error", ex)` registra el mensaje íntegro; `EmployeeControllerTest` demuestra que un mensaje con `password=secret` llega al log. | `architecture.md` §8 | Mantener el log pero documentar/revisar que las excepciones propias no incluyan datos sensibles; considerar un filtro de enmascarado. |
| H-08 | Baja | API | Violación | `GlobalExceptionHandler.java:59` y errores estándar de Spring | Los 400 no incluyen `code`; un JSON malformado responde `type: about:blank` (verificado en prueba de humo). | `architecture.md` §7: campo `code` | Definir un código de validación en el catálogo y aplicarlo a todos los 400. |
| H-09 | Baja | API | Recomendación | `GlobalExceptionHandler.java:83` | El `type` del problema se deriva del nombre de la clase; un renombrado cambia el contrato público de la API. | Estabilidad de contrato | Derivarlo del código de error mediante una tabla explícita. |
| H-10 | Baja | Arquitectura | Contradicción | `infrastructure/.../persistence/adapter/EmployeePersistenceAdapter.java:1` vs `docs/architecture.md:72` | El adaptador está en `persistence/adapter/`; la documentación lo ubica en `persistence/`. | `architecture.md` §2 | **Corregir la documentación** (añadir `adapter/`) o mover la clase. |
| H-11 | Baja | Negocio | Violación | `domain/.../model/Money.java` | Regla no documentada: montos con máximo 2 decimales. | `CLAUDE.md` §4 | Documentarla en `business-rules.md` o confirmarla. |
| H-12 | Baja | Calidad | Recomendación | `application/.../usecase/RegisterEmployeeService.java:27-32` | Se consulta la BD antes de validar nombre y correo. | Fail fast | Construir todos los value objects antes de `existsByDocumentNumber`. |
| H-13 | Baja | Persistencia | Recomendación | `infrastructure/.../persistence/entity/EmployeeJpaEntity.java:19` | La entidad JPA usa el enum de dominio `DocumentType`; renombrar un valor rompería datos. | Acoplamiento | Aceptarlo explícitamente o persistir `String` y convertir en el mapper. |
| H-14 | Baja | Pruebas | Recomendación | `infrastructure/.../api/rest/EmployeeControllerTest.java:35,52` | El request usa `12345999` y el mock devuelve `12345678`; con `any()` no se verifica el mapeo `Request → Command`. | Calidad de prueba | Usar `ArgumentCaptor` y verificar los campos del `Command`. |
| H-15 | Baja | Pruebas | Violación | `pom.xml` | La cobertura ≥ 80 % no se verifica en el build (sin JaCoCo). Hoy se cumple: 94,2 % / 90,0 % líneas. | `architecture.md` §9 | Añadir `jacoco-maven-plugin` con regla de umbral en `verify` (dependencia nueva, requiere aprobación). |
| H-16 | Baja | Pruebas | Violación | `domain/.../model/MoneyAndSalaryTest.java:52` | `shouldCompareSalaryAgainstMinimumWage` no sigue `should<Resultado>When<Condición>`. | `architecture.md` §9 | Renombrar, p. ej. `shouldDetectSalaryBelowMinimumWageWhenAmountIsLower`. |
| H-17 | Baja | Operación | Violación | `infrastructure/src/main/resources/` | Solo existe `application-local.yml`; faltan `dev` y `prod`. | `architecture.md` §8 | Crear ambos perfiles. |
| H-18 | Baja | Operación | Recomendación | `application.yml:34` | `tracing.sampling.probability: 1.0` en todos los perfiles. | Coste en producción | 1.0 solo en `local`; valor bajo en `prod`. |
| H-19 | Baja | Operación | Recomendación | `application.yml:25` | Logs ECS/JSON también en `local`, poco legibles al desarrollar. | Ergonomía | Activarlos solo en `dev`/`prod`. |
| H-20 | Baja | Operación | Violación | `application-local.yml:6`, `docker-compose.yml:8` | Contraseña por defecto `contract` en el repositorio (solo entorno local). | `architecture.md` §8: secretos nunca en el repositorio | Aceptable para local si se documenta; o exigir `DB_PASSWORD` desde `.env` (ya ignorado por git). |

### Antes / después — H-01 (Alta)

**Antes** (`application.yml`): sin configuración; Hibernate registra en ERROR el detalle de PostgreSQL con los valores de la clave duplicada.

```text
ERROR org.hibernate.engine.jdbc.spi.SqlExceptionHelper -- ERROR: duplicate key value violates unique constraint "uk_employees_document"
  Detail: Key (document_type, document_number)=(DNI, 87654321) already exists.
```

**Después:**

```yaml
logging:
  level:
    # El adaptador traduce la violación a EMP-001; el detalle de PostgreSQL contiene datos personales.
    org.hibernate.engine.jdbc.spi.SqlExceptionHelper: "OFF"
  structured:
    format:
      console: ecs
```

## 4. Aspectos positivos

- **Regla de dependencia garantizada por el compilador:** `domain` no declara dependencias y `application` solo `spring-tx`; una importación prohibida no compila (verificado por búsqueda: 0 imports prohibidos).
- **Dominio rico e inmutable:** value objects como `record` validados en el constructor; `Contract` expone comportamiento sin setters; `DomainEvent` es `sealed`.
- **Máquina de estados completa y probada** (27 pruebas de `Contract`), incluidos los límites de fechas.
- **Concurrencia en unicidad de documento bien resuelta:** chequeo previo + restricción única en BD + traducción a `EMP-001`, probada con PostgreSQL real.
- **Problem Details consistentes** con `type` URN, `title` y `code`; los 500 no exponen detalles.
- **Flyway como fuente del esquema** y `ddl-auto: validate` detecta divergencias con las entidades.
- **Decisiones trazables** en ADR-0001 a 0003.

## 5. Plan de acción

**Inmediato** (bloquea release)

| Tarea | Hallazgo | Esfuerzo |
|---|---|---|
| Silenciar `SqlExceptionHelper` y revisar datos personales en logs | H-01, H-07 | S |
| Corregir el prompt 001-review-01 (ArchUnit, `@Transactional`, sección 5, ADRs como fuente) | H-02, H-03 | S |
| Decidir si el tipo de contrato es modificable | H-04 | S |

**Corto plazo**

| Tarea | Hallazgo | Esfuerzo |
|---|---|---|
| Validar invariantes en `Contract.restore` | H-05 | S |
| Prueba `@SpringBootTest` + Testcontainers del registro | H-06 | M |
| `code` en todos los 400 y `type` desde tabla de códigos | H-08, H-09 | S |
| Configurar JaCoCo con umbral 80 % | H-15 | S |
| Implementar `CreateContractUseCase` (BR-CTR-001, 005, 010) | Trazabilidad | L |

**Mejora continua**

| Tarea | Hallazgo | Esfuerzo |
|---|---|---|
| Perfiles `dev`/`prod`, muestreo de trazas y logs por perfil | H-17, H-18, H-19 | S |
| Ajustes menores de pruebas y nombres | H-12, H-14, H-16 | S |
| Documentar ubicación del adaptador, regla de 2 decimales y credencial local | H-10, H-11, H-20 | S |
| Evaluar persistir `DocumentType` como `String` | H-13 | S |

## 6. Actualizaciones sugeridas a la documentación

1. **`docs/prompts/001-review-01.md`:**
   - Línea 24: permitir `@Transactional` en `application`.
   - Línea 28: reemplazar el requisito de ArchUnit por "verificación manual de la sección 3 (ADR-0003)".
   - Fuentes de verdad: añadir `docs/adr/`.
   - Añadir la sección **5. Seguridad** (hoy la numeración salta de 4 a 6), limitada a secretos y datos sensibles mientras rija ADR-0001.
2. **`docs/architecture.md` §2:** añadir `persistence/adapter/` y `application/exception/` ya documentado.
3. **`docs/architecture.md` §8:** explicitar que los logs de librerías que incluyan valores de datos (SQL, parámetros) deben silenciarse o enmascararse.
4. **`docs/business-rules.md` §9:** añadir "¿Se puede cambiar el tipo de un contrato en `DRAFT`?" y documentar "montos con máximo 2 decimales".
5. **`docs/business-rules.md` §8:** añadir un código para errores de validación de formato (400).

## No evaluado

| Aspecto | Motivo |
|---|---|
| Seguridad (autenticación, autorización, BR-CTR-011/012) | Diferida por ADR-0001; el prompt no tiene sección 5. |
| Unicidad de `ContractNumber`, claves foráneas, índices por empleado/estado/fechas, paginación, N+1 | No existe todavía la tabla ni el repositorio de contratos. |
| Riesgo de carrera en BR-CTR-005 y generación de `ContractNumber`; `@Version` | Los casos de uso de contrato no están implementados; `Employee` es inmutable. |
| `traceId` en los logs | No hay logs dentro de una petición con span activo que permitan verificarlo; solo se comprobó el formato ECS. |
| `infrastructure/client` (WireMock) | No hay adaptadores de clientes externos. |
