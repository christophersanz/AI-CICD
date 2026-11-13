# 01 · Definir qué se construye (requisitos y reglas de negocio)

| | |
|---|---|
| **Comando** | `/01-definir <modo> <tema>` — modos: `requisitos <requerimiento o ruta a las notas>`, `reglas <tema o pregunta abierta>` |
| **Archivo** | `docs/prompts/01-definir.md` (única versión; los cambios se hacen aquí y quedan en el historial de git) |
| **Origen** | Nuevo en este proyecto (fuentes en `docs/prompts/README.md` §3: MVPs de Jairo y reglas preguntadas por la IA de Ángel) |

> `{ARGUMENTO}` es lo que se escribe después del comando. `DP.<clave>` es un dato del proyecto: está en `CLAUDE.md` → *Datos para los prompts*. Sin Claude Code, copia este prompt y reemplaza `{ARGUMENTO}` a mano.

## Rol
Actúa como analista funcional y de negocio, product owner técnico y arquitecto DDD con experiencia en el negocio del proyecto (`DP.dominio-negocio`). Tu trabajo es **preguntar** hasta que el alcance y las reglas queden claros, no suponerlos.

## Contexto y fuentes de verdad
- Lee: `docs/business-rules.md` (alcance y **fuera de alcance** de §1, glosario, reglas `BR-*`, estados, casos de uso de §6, eventos, errores y **preguntas abiertas de §9**), `docs/architecture.md` (§3 y §7) y `CLAUDE.md`.
- El código manda sobre lo implementado: confirma cada regla contra `domain` y `application` antes de proponer cambios.

## Validación del argumento
`{ARGUMENTO}` es `<modo> <tema>`.
- **Sin modo:** si el texto describe una funcionalidad nueva, propón el modo `requisitos`; si es una duda sobre una regla existente, `reglas`; y espera confirmación.
- **`reglas` sin tema:** lista las preguntas abiertas de §9 agrupadas por agregado (§3) y los casos de uso de §6 que aún no tienen código, y detente.
- **`requisitos` sin texto:** pide la descripción o la ruta a las notas de la reunión, y detente.

| Modo | Cuándo | Resultado |
|---|---|---|
| `requisitos` | Llega una funcionalidad nueva o una historia de usuario | Backlog en MVPs, con historias y criterios de aceptación, en `docs/requirements/NNN-<tema>.md` |
| `reglas` | El comportamiento no está claro o hay preguntas abiertas | Reglas `BR-*` nuevas o aclaradas en `business-rules.md` |

Orden habitual: `requisitos` primero y después `reglas` por cada duda bloqueante.

## Objetivo
- **`requisitos`:** un backlog dividido en MVPs donde cada historia tiene criterios de aceptación verificables (*Dado / Cuando / Entonces*, incluidos los errores), indica sus casos de uso, reglas y permiso, y deja explícitas las dudas que bloquean.
- **`reglas`:** el tema queda especificado sin ambigüedad en `business-rules.md`: cada regla con ID `BR-<CTX>-NNN`, capa, código de error y caso de uso que la aplica, y las preguntas resueltas marcadas `[x]` con su respuesta.
- En ambos modos, nada queda supuesto: lo que el autor no confirmó figura como pregunta abierta.

## Restricciones
- **No inventes reglas.** Puedes proponer opciones con sus pros y contras, pero la decisión es del autor. Lo ambiguo va como pregunta, no como supuesto.
- Las reglas con base normativa o legal se tratan como indica `DP.normativa`; no cites normas que no puedas verificar.
- **No escribas código** en este comando: el resultado es documentación. La implementación va después con `/02-implementar-caso-uso`.
- Respeta el bounded context: lo que §1 declara fuera de alcance o es de otro sistema se marca como fuera de alcance o como integración futura. Separa lo que es backend de esta API de lo que es frontend, otro servicio o proceso manual.
- No borres reglas existentes: si una cambia, explica el impacto en el código y las pruebas que la cubren.
- Evita la sobreingeniería: el MVP 1 debe poder implementarse con los módulos actuales.

## Forma de trabajo
Crea o retoma `docs/sessions/AAAA-MM-DD-01-definir-<modo>-<tema>.md` (`docs/sessions/README.md`) y actualízalo al terminar cada paso.

### Modo `requisitos`

**Paso 1 — Entendimiento (sin escribir el documento)**
1. Resume el requerimiento en 3–5 líneas: actores, objetivo de negocio y resultado esperado.
2. Clasifica cada parte: dentro del alcance de `DP.nombre`, fuera de alcance, otra aplicación (frontend, integración).
3. Relación con lo existente: casos de uso de §6 ya implementados, documentados sin implementar o nuevos; reglas `BR-*` y preguntas abiertas afectadas.
4. Hazme como máximo **7 preguntas** que cambien el alcance o la prioridad, cada una con opciones y tu recomendación.
**Espera mis respuestas.**

**Paso 2 — Propuesta de MVPs**
1. **MVPs** ordenados por valor y dependencia: el MVP 1 es el mínimo que aporta valor de punta a punta.
2. **Historias** por MVP: `Como <actor> quiero <acción> para <beneficio>`, con criterios de aceptación *Dado / Cuando / Entonces* (incluidos los casos de error).
3. Por historia: caso(s) de uso (existentes o nuevos), reglas `BR-*` afectadas, permiso (`business-rules.md` §4.1) y tamaño estimado (S/M/L).
4. **Requisitos no funcionales** relevantes (seguridad, auditoría, rendimiento, retención de datos).
5. **Dudas pendientes** que bloquean cada historia.
**Espera mi aprobación.**

**Paso 3 — Documento**
Guarda `docs/requirements/NNN-<tema>.md` con el resultado aprobado (contexto, alcance y fuera de alcance, MVPs, historias con criterios, trazabilidad historia → caso de uso → reglas, RNF y dudas). Si es el primer documento, crea la carpeta y añade la fila en la tabla de documentación de `CLAUDE.md`.

### Modo `reglas`

**Paso 1 — Diagnóstico (sin cambiar nada)**
1. Qué dicen hoy `business-rules.md` y el código sobre el tema (reglas, estados, errores, casos de uso afectados).
2. Vacíos y contradicciones: reglas que el código aplica y no están documentadas, reglas documentadas sin implementar, preguntas abiertas relacionadas.
3. Escenarios concretos que hoy no tienen respuesta (p. ej. "¿qué pasa si la operación ocurre el mismo día en que vence el plazo?").

**Paso 2 — Preguntas**
Hazme como máximo **7 preguntas**, cada una con el escenario que la motiva, 2–3 opciones con su consecuencia (en el dominio, la API y los datos) y tu recomendación, marcada como tal.
**Espera mis respuestas.** Si una respuesta abre otra duda, pregúntala antes de seguir.

**Paso 3 — Propuesta de cambios**
Presenta el diff propuesto de `business-rules.md`:
- glosario (términos nuevos, en español y en código);
- reglas nuevas o modificadas en §4, con ID `BR-<CTX>-NNN` consecutivo, capa y error;
- estados y transiciones (§5, con el diagrama Mermaid) si cambian;
- casos de uso (§6 y su detalle numerado), eventos (§7) y catálogo de errores (§8);
- §9: preguntas resueltas `[x]` con su respuesta y preguntas nuevas `[ ]`.
**Espera mi aprobación.**

**Paso 4 — Aplicar**
1. Aplica el diff aprobado en `business-rules.md`.
2. Lista el impacto en el código (clases, pruebas y endpoints que habrá que cambiar) y el comando sugerido (`/02-implementar-caso-uso <Nombre>` o `/03-pruebas <Clase>`).
3. Si la decisión es arquitectónica (no solo de negocio), propón el ADR.

## Informe final
- **`requisitos`:** MVPs y número de historias por MVP, casos de uso nuevos, dudas pendientes y el orden recomendado: `/01-definir reglas <duda>` por cada duda bloqueante y después `/02-implementar-caso-uso <Nombre>` por historia del MVP 1.
- **`reglas`:** reglas añadidas o cambiadas (IDs), preguntas resueltas y pendientes, impacto en el código y siguiente comando recomendado.
