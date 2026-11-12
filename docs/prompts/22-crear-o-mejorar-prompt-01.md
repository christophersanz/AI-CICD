# 22 · Crear o mejorar un prompt (v01)

| | |
|---|---|
| **Comando** | `/22-crear-o-mejorar-prompt <tarea nueva | archivo de docs/prompts a mejorar>` |
| **Archivo** | `docs/prompts/22-crear-o-mejorar-prompt-01.md` |
| **Versión anterior** | — |

> `{ARGUMENTO}` es lo que se escribe después del comando. Sin Claude Code, copia este prompt y reemplaza `{ARGUMENTO}` a mano.

## Rol
Actúa como ingeniero de prompts especializado en desarrollo de software asistido por IA con Claude Code, y como arquitecto Java que conoce este proyecto. Diseñas prompts que la IA puede ejecutar sin adivinar: qué hacer, cómo, dónde y qué no hacer.

## Contexto y fuentes de verdad
- Lee: `docs/prompts/README.md` (orden, estructura común de 10 partes y de dónde sale cada práctica) y los prompts vigentes de `docs/prompts/` para no duplicar.
- Lee también `CLAUDE.md`, `docs/architecture.md` y `docs/business-rules.md`: un prompt de este proyecto se apoya en ellos, no los repite.
- Pedido: **{ARGUMENTO}**. Puede ser una tarea nueva ("prompt para migrar a Java 25") o un prompt existente a mejorar (`docs/prompts/NN-nombre-VV.md`). Si está vacío, pide cuál.

## Objetivo
Un prompt en `docs/prompts/NN-nombre-VV.md` con las 10 partes de la estructura común, sin duplicar otro prompt, con su comando en `.claude/commands/` y registrado en el índice.

## Restricciones
- **Antes de crear uno nuevo, busca duplicados:** si otro prompt ya cubre la tarea, propón mejorarlo (nueva versión) en lugar de crear otro.
- Corto y enfocado: un prompt por tarea, que remite a los documentos en lugar de copiarlos. Nada de prompts de "15 páginas" (sesión 01) ni de parches incrustados.
- No inventes reglas de negocio ni decisiones de arquitectura dentro del prompt: si hacen falta, el prompt debe preguntar o remitir a `/01-reglas-negocio`.
- No modifiques código del proyecto.

## Forma de trabajo

### Paso 1 — Análisis (sin escribir el prompt)
1. Tarea, actor y resultado esperado.
2. Prompts existentes relacionados y si hay duplicación (cita las secciones).
3. Número propuesto según el orden (`0x` desarrollo, `09`–`18` revisión, `2x` calidad, operación y prompts) y nombre en español, verbo + objeto (`07-migrar-version-java`).
4. Para una mejora: qué funciona del prompt actual, qué falla (con ejemplos de ejecuciones si los hay, p. ej. en `docs/sessions/` o `docs/ai-log.md`) y qué cambiaría.
**Espera mi aprobación.**

### Paso 2 — Redacción
Escribe el prompt con:
1. Cabecera: título `# NN · Título (vVV)` y tabla con comando, archivo y versión anterior.
2. Las 10 partes: rol, contexto y fuentes de verdad, validación del argumento (`{ARGUMENTO}`), objetivo verificable, restricciones (qué NO hacer), análisis de impacto y plan con aprobación, pasos pequeños, verificación, informe final y archivo de sesión.
3. Si es una revisión: remite a `docs/prompts/09-reglas-revision-01.md` y no modifica código.

### Paso 3 — Versionado y registro
- **Nuevo:** `NN-nombre-01.md` y el lanzador `.claude/commands/NN-nombre.md` (mismo formato que los demás: `description`, `argument-hint`, `@docs/prompts/...` y `ARGUMENTO = $ARGUMENTS`).
- **Mejora:** `NN-nombre-VV+1.md`; la versión anterior pasa a `docs/prompts/historico/` **con su mismo nombre** (`NN-nombre-VV.md`) y el lanzador apunta a la nueva. Nunca dos versiones activas del mismo prompt.
- Actualiza `docs/prompts/README.md` (tabla de su fase y, si aplica, la tabla de fuentes) y anota la entrada en `docs/ai-log.md`.

### Paso 4 — Prueba en seco
Explica cómo se comportaría el prompt con un argumento real del proyecto (qué leería, qué preguntaría, qué entregaría) e identifica ambigüedades; corrígelas antes de cerrar.

## Informe final
Archivo creado o versionado, lanzador, cambios en el índice, duplicaciones evitadas y la prueba en seco.
