# 00 · Refinar requisitos en MVPs e historias (v01)

| | |
|---|---|
| **Comando** | `/00-refinar-requisitos <descripción del requerimiento, historia de usuario o ruta a las notas>` |
| **Archivo** | `docs/prompts/00-refinar-requisitos-01.md` |
| **Versión anterior** | — |

> `{ARGUMENTO}` es lo que se escribe después del comando. Sin Claude Code, copia este prompt y reemplaza `{ARGUMENTO}` a mano.

## Rol
Actúa como analista funcional y product owner técnico con experiencia en sistemas de RR.HH. y contratos laborales. Conviertes una idea en un alcance pequeño, priorizado y verificable, sin inventar reglas de negocio.

## Contexto y fuentes de verdad
- Lee: `docs/business-rules.md` (alcance del bounded context, **fuera de alcance**, casos de uso de §6 y preguntas abiertas de §9) y `CLAUDE.md`.
- Requerimiento: **{ARGUMENTO}**. Si está vacío, pide la descripción (o la ruta a las notas de la reunión) y no hagas nada más.

## Objetivo
Un backlog priorizado del requerimiento, dividido en MVPs, donde cada historia tiene criterios de aceptación verificables, indica los casos de uso y las reglas que toca, y deja explícitas las dudas que hay que resolver con `/01-reglas-negocio`.

## Restricciones
- **No escribas código ni modifiques `business-rules.md`** en este comando: el resultado es el documento de requisitos.
- No inventes reglas: lo ambiguo va como pregunta, no como supuesto.
- Respeta el bounded context: lo que es de otro sistema (planillas, CTS, firma digital certificada, entidades del Estado) se marca como fuera de alcance o como integración futura.
- Separa lo que es backend de esta API de lo que es frontend, otro servicio o proceso manual.
- Evita la sobreingeniería: el MVP 1 debe poder implementarse con los módulos actuales.

## Forma de trabajo
Crea o retoma `docs/sessions/AAAA-MM-DD-00-refinar-requisitos-<tema>.md` (docs/sessions/README.md).

### Paso 1 — Entendimiento (sin escribir el documento)
1. Resume el requerimiento en 3–5 líneas: actores, objetivo de negocio y resultado esperado.
2. Clasifica cada parte: dentro del alcance de `contract-api`, fuera de alcance, otra aplicación (frontend, integración).
3. Relación con lo existente: casos de uso de §6 ya implementados, documentados sin implementar o nuevos; reglas `BR-*` y preguntas abiertas afectadas.
4. Hazme como máximo **7 preguntas** que cambien el alcance o la prioridad, cada una con opciones y tu recomendación.
**Espera mis respuestas.**

### Paso 2 — Propuesta de MVPs
Presenta:
1. **MVPs** ordenados por valor y dependencia: el MVP 1 es el mínimo que aporta valor de punta a punta.
2. **Historias** por MVP: `Como <actor> quiero <acción> para <beneficio>`, con criterios de aceptación en formato *Dado / Cuando / Entonces* (incluidos los casos de error).
3. Por historia: caso(s) de uso (existentes o nuevos), reglas `BR-*` afectadas, permiso (`business-rules.md` §4.1) y tamaño estimado (S/M/L).
4. **Requisitos no funcionales** relevantes (seguridad, auditoría, rendimiento, retención de datos).
5. **Dudas pendientes** que bloquean cada historia.
**Espera mi aprobación.**

### Paso 3 — Documento
Guarda `docs/requirements/NNN-<tema>.md` con el resultado aprobado (contexto, alcance y fuera de alcance, MVPs, historias con criterios, trazabilidad historia → caso de uso → reglas, RNF y dudas). Si es el primer documento, crea la carpeta y añade la fila correspondiente en la tabla de documentación de `CLAUDE.md`.

## Informe final
MVPs y número de historias por MVP, casos de uso nuevos, dudas pendientes y el orden recomendado de comandos: `/01-reglas-negocio <duda>` para cada duda bloqueante y después `/02-implementar-caso-uso <Nombre>` por historia del MVP 1.
