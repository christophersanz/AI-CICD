---
description: Completa y mejora las pruebas de una clase o caso de uso de contract-api, con patrones de prueba explícitos
argument-hint: <Clase | NombreUseCase | módulo>
---

# Rol
Actúa como ingeniero de QA y desarrollador senior de Java 21 experto en JUnit 5, AssertJ, Mockito, Testcontainers y `spring-security-test`. Además de escribir pruebas, debes poder **explicar qué patrón usa cada una y por qué**.

# Contexto y fuentes de verdad
- @docs/architecture.md §9 (estrategia por capa, nombres y cobertura) y @docs/business-rules.md (reglas `BR-*` y errores).
- Objetivo de la sesión: **$ARGUMENTS** (si está vacío, pregunta qué clase, caso de uso o módulo; no elijas tú).

# Objetivo
Cada regla y cada error que toca `$ARGUMENTS` tiene al menos una prueba que falla si la regla se rompe; los casos límite están cubiertos; `./mvnw clean verify` pasa con cobertura ≥ 80 % de líneas en `domain` y `application`.

# Restricciones
- No cambies código de producción para que una prueba pase. Si una prueba revela un bug, detente, muéstralo con la prueba que falla y propón la corrección.
- Pruebas de dominio sin Spring ni mocks; casos de uso mockeando solo puertos de salida; persistencia con Testcontainers (nunca H2); web con `@WebMvcTest`.
- No añadas librerías de pruebas sin justificarlo.

# Forma de trabajo
Crea o retoma `docs/sessions/AAAA-MM-DD-tests-<objetivo>.md` (docs/sessions/README.md).

## Paso 1 — Diagnóstico (sin escribir pruebas)
1. **Matriz de trazabilidad** de lo que existe hoy:

   | Regla / error | Capa | Prueba(s) que la cubren | ¿Falla si se rompe la regla? |
   |---|---|---|---|

2. **Vacíos:** reglas o errores sin prueba, pruebas que no verifican nada relevante (solo `assertNotNull`, sin verificar el `code` del error), duplicadas o frágiles.
3. **Casos límite pendientes:** valores en el borde (8 y 9 dígitos de DNI, 12 y 65 caracteres de contraseña), periodos que se tocan en un día, opcionales ausentes, transiciones no permitidas desde **cada** estado, entradas enormes o con espacios.
4. **Plan** con las pruebas a crear, su capa y el patrón que usará cada una.
Espera mi aprobación.

## Paso 2 — Implementar
Aplica y nombra estos patrones donde correspondan:

| Patrón | Cuándo usarlo en este proyecto |
|---|---|
| **Arrange-Act-Assert** (Given/When/Then) | Siempre; una sola razón de fallo por prueba |
| **Object Mother / Test Data Builder** | Agregados con muchos campos (`ContractMother.activeFixedTerm()`, `EmployeeMother.active()`) |
| **Parametrizadas** (`@ParameterizedTest`, `@CsvSource`, `@EnumSource`) | Una regla con varios valores válidos/ inválidos; transiciones por estado |
| **`@Nested`** | Agrupar por comportamiento (`class WhenSigning`, `class WhenRenewing`) |
| **Test double: stub vs mock** | Stub para lo que devuelve un puerto; `verify` de Mockito solo cuando la interacción es la regla (p. ej. que se persistió o se revocó la familia de tokens) |
| **Fake clock** (`Clock.fixed`) | Fechas, vencimientos y `occurredOn` de eventos |
| **Pruebas de contrato web** | `@WebMvcTest` verificando status, `Content-Type: application/problem+json`, `type`, `code` y `errors` |
| **Seguridad** | `@WithMockUser(authorities = "...")` para 403 por permiso; sin token para 401 |
| **Integración con contenedores** | `@DataJpaTest` + Testcontainers PostgreSQL para consultas, índices y restricciones únicas |
| **Prueba de regresión** | Todo bug corregido lleva la prueba que lo reproducía (p. ej. la regex de `Permission` con 200 000 caracteres) |

Nombres `should<Resultado>When<Condición>`, AssertJ, sin lógica condicional dentro de las pruebas.

## Paso 3 — Verificar
1. `./mvnw clean verify`: número de pruebas, fallos y cobertura por módulo (JaCoCo).
2. Mutación manual de comprobación: para 2–3 reglas críticas, explica qué cambio en el código de producción haría fallar cada prueba nueva.

# Informe final
Matriz de trazabilidad actualizada, pruebas añadidas por patrón, cobertura antes/después, bugs encontrados (si hubo) y vacíos que quedan. Incluye un párrafo listo para la presentación: "qué patrones de prueba usé y por qué".
