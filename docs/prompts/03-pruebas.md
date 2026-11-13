# 03 · Completar y mejorar pruebas

| | |
|---|---|
| **Comando** | `/03-pruebas <Clase | NombreUseCase | módulo>` |
| **Archivo** | `docs/prompts/03-pruebas.md` (única versión; los cambios se hacen aquí y quedan en el historial de git) |
| **Origen** | Nuevo en este proyecto (fuentes en `docs/prompts/README.md` §3) |

> `{ARGUMENTO}` es lo que se escribe después del comando. `DP.<clave>` es un dato del proyecto: está en `CLAUDE.md` → *Datos para los prompts*. Sin Claude Code, copia este prompt y reemplaza `{ARGUMENTO}` a mano.
>
> **Documentos y secciones:** `architecture.md`, `business-rules.md` y `technical.md` son los archivos que indican `DP.doc-arquitectura`, `DP.doc-reglas` y `DP.doc-tecnico`. Si uno es «No aplica», omite lo que este prompt diga de él. `§N` es la numeración de las plantillas del kit (`docs/prompts/kit/*.plantilla.md`); si el proyecto organiza sus documentos de otra forma, `DP.secciones` dice dónde está cada sección y qué formato tienen los ID de las reglas (`BR-*` en la plantilla). Las capas y la ubicación de la API salen de `DP.modulos`.

## Rol
Actúa como ingeniero de QA y desarrollador senior experto en el stack y las librerías de prueba del proyecto (`DP.stack`). Además de escribir pruebas, debes poder **explicar qué patrón usa cada una y por qué**.

## Contexto y fuentes de verdad
- `architecture.md` §9 (estrategia por capa, nombres y cobertura) y `business-rules.md` (reglas `BR-*` y errores).
- Objetivo de la sesión: **{ARGUMENTO}** (si está vacío, pregunta qué clase, caso de uso o módulo; no elijas tú).

## Ciclo de trabajo (sesión 04)
**Contexto → prompt → revisión → ejecución → umbrales.** La IA genera las pruebas, pero se revisan antes de aceptarlas: una prueba que siempre pasa no prueba nada. Los umbrales son `DP.cobertura-minima` y el Quality Gate (`DP.sonarqube`). No basta con probar el camino feliz: hay que cubrir los errores, los límites y los datos variados (builders y datos generados).

## Objetivo
Cada regla y cada error que toca `{ARGUMENTO}` tiene al menos una prueba que falla si la regla se rompe; los casos límite están cubiertos; `DP.build` pasa con cobertura según `DP.cobertura-minima`.

## Restricciones
- No cambies código de producción para que una prueba pase. Si una prueba revela un bug, detente, muéstralo con la prueba que falla y propón la corrección.
- Pruebas de dominio sin Spring ni mocks; casos de uso mockeando solo puertos de salida; persistencia según `DP.pruebas-integracion`; web con `@WebMvcTest`.
- **No debilites ni borres pruebas existentes** (aserciones menos estrictas, `@Disabled`, casos eliminados) para que algo pase. Si una prueba existente es incorrecta o frágil, muéstralo con evidencia y propón el cambio; lo decido yo.
- No añadas librerías de pruebas sin justificarlo.

## Forma de trabajo
- Rama `test/<objetivo>` desde `DP.rama-principal` actualizado; al terminar, commit en español, push y PR. No integres el PR.
- Crea o retoma `docs/sessions/AAAA-MM-DD-03-pruebas-<objetivo>.md` (docs/sessions/README.md).

### Paso 1 — Diagnóstico (sin escribir pruebas)
1. **Matriz de trazabilidad** de lo que existe hoy:

   | Regla / error | Capa | Prueba(s) que la cubren | ¿Falla si se rompe la regla? |
   |---|---|---|---|

2. **Vacíos:** reglas o errores sin prueba, pruebas que no verifican nada relevante (solo `assertNotNull`, sin verificar el `code` del error), duplicadas o frágiles.
3. **Casos límite pendientes:** valores en el borde (el mínimo, el máximo y uno fuera de cada rango o longitud que fije `business-rules.md`), periodos que se tocan en un día, opcionales ausentes, transiciones no permitidas desde **cada** estado, entradas enormes o con espacios.
4. **Plan** con las pruebas a crear, su capa y el patrón que usará cada una.
Espera mi aprobación.

### Paso 2 — Implementar
Aplica y nombra estos patrones donde correspondan:

| Patrón | Cuándo usarlo en este proyecto |
|---|---|
| **Arrange-Act-Assert** (Given/When/Then) | Siempre; una sola razón de fallo por prueba |
| **Object Mother / Test Data Builder** | Agregados con muchos campos (`<Agregado>Mother.<estado>()`, p. ej. `OrderMother.confirmed()`) |
| **Parametrizadas** (`@ParameterizedTest`, `@CsvSource`, `@EnumSource`) | Una regla con varios valores válidos/ inválidos; transiciones por estado |
| **`@Nested`** | Agrupar por comportamiento (`class WhenSigning`, `class WhenRenewing`) |
| **Test double: stub vs mock** | Stub para lo que devuelve un puerto; `verify` de Mockito solo cuando la interacción es la regla (p. ej. que se persistió o se revocó la familia de tokens) |
| **Fake clock** (`Clock.fixed`) | Fechas, vencimientos y `occurredOn` de eventos |
| **Pruebas de contrato web** | `@WebMvcTest` verificando status, `Content-Type: application/problem+json`, `type`, `code` y `errors` |
| **Seguridad** | `@WithMockUser(authorities = "...")` para 403 por permiso; sin token para 401 |
| **Integración con contenedores** | `@DataJpaTest` con `DP.pruebas-integracion` para consultas, índices y restricciones únicas |
| **Prueba de regresión** | Todo bug corregido lleva la prueba que lo reproducía (p. ej. una entrada enorme que antes desbordaba la pila) |

Nombres `should<Resultado>When<Condición>`, AssertJ, sin lógica condicional dentro de las pruebas.

### Paso 3 — Verificar
1. `DP.build`: número de pruebas, fallos y cobertura por módulo.
2. Mutación manual de comprobación: para 2–3 reglas críticas, explica qué cambio en el código de producción haría fallar cada prueba nueva.

## Informe final
Matriz de trazabilidad actualizada, pruebas añadidas por patrón, pruebas existentes modificadas (si las hubo, con su motivo), cobertura antes/después, bugs encontrados (si hubo) y vacíos que quedan. Incluye un párrafo listo para la presentación: "qué patrones de prueba usé y por qué".
