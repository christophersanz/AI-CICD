# 09 · Crear o mejorar un prompt

| | |
|---|---|
| **Comando** | `/09-mejorar-prompt <tarea nueva | archivo de docs/prompts a mejorar>` |
| **Archivo** | `docs/prompts/09-mejorar-prompt.md` (única versión; los cambios se hacen aquí y quedan en el historial de git) |
| **Origen** | Nuevo en este proyecto (fuentes en `docs/prompts/README.md` §3) |

> `{ARGUMENTO}` es lo que se escribe después del comando. `DP.<clave>` es un dato del proyecto: está en `CLAUDE.md` → *Datos para los prompts*. Sin Claude Code, copia este prompt y reemplaza `{ARGUMENTO}` a mano.

## Rol
Actúa como ingeniero de prompts especializado en desarrollo de software asistido por IA con Claude Code, y como arquitecto que conoce el stack del proyecto (`DP.stack`). Diseñas prompts que la IA puede ejecutar sin adivinar: qué hacer, cómo, dónde y qué no hacer.

## Contexto y fuentes de verdad
- Lee: `docs/prompts/README.md` (orden, estructura común de 10 partes y de dónde sale cada práctica) y los prompts vigentes de `docs/prompts/` para no duplicar.
- Lee también `CLAUDE.md`, `docs/architecture.md` y `docs/business-rules.md`: un prompt de este proyecto se apoya en ellos, no los repite.
- Pedido: **{ARGUMENTO}**. Puede ser una tarea nueva ("prompt para migrar a Java 25") o un prompt existente a mejorar (`docs/prompts/NN-nombre.md`). Si está vacío, pide cuál.

## Objetivo
Un prompt en `docs/prompts/NN-nombre.md` con las 10 partes de la estructura común, sin duplicar otro prompt, con su comando en `.claude/commands/` y registrado en el índice.

## Restricciones
- **Antes de crear uno nuevo, busca duplicados:** si otro prompt ya cubre la tarea, propón mejorar ese prompt (editando su único archivo) en lugar de crear otro.
- Corto y enfocado: un prompt por tarea, que remite a los documentos en lugar de copiarlos. Nada de prompts de "15 páginas" (sesión 01) ni de parches incrustados.
- No inventes reglas de negocio ni decisiones de arquitectura dentro del prompt: si hacen falta, el prompt debe preguntar o remitir a `/01-definir reglas`.
- No modifiques código del proyecto.

## Forma de trabajo
- Rama `docs/prompt-<nombre>` desde `DP.rama-principal` actualizado; al terminar, commit en español, push y PR. No integres el PR.
- Crea o retoma `docs/sessions/AAAA-MM-DD-09-mejorar-prompt-<tema>.md` (`docs/sessions/README.md`).

### Paso 1 — Análisis (sin escribir el prompt)
1. Tarea, actor y resultado esperado.
2. Prompts existentes relacionados y si hay duplicación (cita las secciones).
3. **Primero, ¿cabe en un prompt existente?** Lo normal es agregar un modo o una sección a uno de los 9 (como los modos de `/06-revisar` o `/07-corregir`) en lugar de crear otro. Solo si no encaja: el número siguiente al último prompt existente (hoy, `10`) y un nombre en español, verbo + objeto (`10-migrar-version-java`). Nunca reutilices un número ni renumeres los existentes: cambiaría sus comandos y sus archivos de sesión.
4. Para una mejora: qué funciona del prompt actual, qué falla (con ejemplos de ejecuciones si los hay, p. ej. en `docs/sessions/` o `docs/ai-log.md`) y qué cambiaría.
**Espera mi aprobación.**

### Paso 2 — Redacción
Escribe el prompt con:
1. Cabecera: título `# NN · Título` y tabla con comando, archivo y origen.
2. Las 10 partes: rol, contexto y fuentes de verdad, validación del argumento (`{ARGUMENTO}`), objetivo verificable, restricciones (qué NO hacer), análisis de impacto y plan con aprobación, pasos pequeños, verificación, informe final y archivo de sesión.
3. Si es una revisión: remite a `docs/prompts/06-revisar.md` y no modifica código.

### Paso 3 — Registro (un solo archivo por prompt)
- **Nuevo:** `docs/prompts/NN-nombre.md` y el lanzador `.claude/commands/NN-nombre.md` (mismo formato que los demás: `description`, `argument-hint`, `@docs/prompts/...` y `ARGUMENTO = $ARGUMENTS`).
- **Mejora:** se edita el **mismo archivo** `NN-nombre.md`; no se crean copias ni versiones `-02`. El cambio va en su propio commit, con un mensaje que explique qué se mejoró y por qué; la versión anterior queda en el historial de git (`git log -p docs/prompts/NN-nombre.md`). El lanzador no cambia.
- Si el prompt usa un dato del proyecto, va como `DP.<clave>` y, si la clave no existe, se agrega a `CLAUDE.md` → *Datos para los prompts* y a la plantilla `docs/prompts/kit/CLAUDE-datos-prompts.md`.
- Actualiza `docs/prompts/README.md` (tabla de prompts y, si aplica, la tabla de fuentes) y anota la entrada en `docs/ai-log.md`.
- Ejecuta `docs/prompts/kit/verificar.sh`: debe terminar con 0 errores (comandos citados que existen, archivo de sesión con el número correcto, claves `DP` definidas).

### Paso 4 — Prueba en seco
Explica cómo se comportaría el prompt con un argumento real del proyecto (qué leería, qué preguntaría, qué entregaría) e identifica ambigüedades; corrígelas antes de cerrar.

## Informe final
Archivo creado o modificado, lanzador, cambios en el índice, duplicaciones evitadas, la prueba en seco, el resultado de `docs/prompts/kit/verificar.sh` (0 errores) y el PR abierto.
