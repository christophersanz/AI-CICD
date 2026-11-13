# 08 · Cambiar el pipeline de CI/CD

| | |
|---|---|
| **Comando** | `/08-cambiar-pipeline <cambio a realizar en el pipeline o el despliegue>` |
| **Archivo** | `docs/prompts/08-cambiar-pipeline.md` (única versión; los cambios se hacen aquí y quedan en el historial de git) |
| **Origen** | Mejora del prompt del curso [`006-cicd.md`](https://github.com/christophersanz/AI-CICD/blob/33f74fc/docs/prompts/006-cicd.md) |

> `{ARGUMENTO}` es lo que se escribe después del comando. `DP.<clave>` es un dato del proyecto: está en `CLAUDE.md` → *Datos para los prompts*. Sin Claude Code, copia este prompt y reemplaza `{ARGUMENTO}` a mano.
>
> **Documentos y secciones:** `architecture.md`, `business-rules.md` y `technical.md` son los archivos que indican `DP.doc-arquitectura`, `DP.doc-reglas` y `DP.doc-tecnico`. Si uno es «No aplica», omite lo que este prompt diga de él. `§N` es la numeración de las plantillas del kit (`docs/prompts/kit/*.plantilla.md`); si el proyecto organiza sus documentos de otra forma, `DP.secciones` dice dónde está cada sección y qué formato tienen los ID de las reglas (`BR-*` en la plantilla). Las capas y la ubicación de la API salen de `DP.modulos`.

## Rol
Actúa como ingeniero DevOps senior experto en las herramientas de `DP.ci` y `DP.ci-local`, Docker, análisis estático y despliegue de aplicaciones Spring Boot.

## Contexto y fuentes de verdad
- Lee: `CLAUDE.md` (flujo de Git y *Datos para los prompts*), la guía de CI/CD de `DP.guias`, `technical.md` y los ADR de `DP.adr-cicd`.
- Archivos: los que describen `DP.ci`, `DP.imagen`, `DP.despliegue`, `DP.ci-local` y `DP.sonarqube`.
- Cambio pedido: **{ARGUMENTO}** (si está vacío, pregunta qué se quiere cambiar).

## Objetivo
El cambio funciona en una ejecución real (no solo con la sintaxis validada): los checks del PR en verde y, si toca el CI local, un build con todas sus etapas en el estado esperado.

## Restricciones
- **Sin parches incrustados en el prompt ni archivos de cientos de líneas:** cambios pequeños y revisables.
- Secretos solo como *secrets* del repositorio o del environment, o en los archivos locales no versionados que indique `DP.ci-local`. Nunca en el código, en los logs (enmascarados) ni en la línea de comandos.
- No modifiques las reglas de protección ni los environments sin mostrarme antes el cambio exacto (JSON o comando) y esperar mi aprobación.
- **No integres PRs ni apruebes despliegues** de producción: es el control humano de `DP.flujo-git`.
- Fija versiones de imágenes y acciones (nada de `latest` sin justificarlo).
- Mantén alineados `DP.ci` y `DP.ci-local` cuando el cambio aplique a ambos (por ejemplo, una prueba de humo compartida).

## Forma de trabajo
Crea o retoma `docs/sessions/AAAA-MM-DD-08-cambiar-pipeline-<tema>.md` (docs/sessions/README.md).

### Paso 1 — Análisis de impacto y plan (sin cambiar nada)
1. Estado actual de lo que se va a tocar (confírmalo leyendo los archivos y, si aplica, `gh run list`, `gh api` o la API del CI local).
2. Qué cambia en cada evento (PR, merge a `DP.rama-principal`, tag `vX.Y.Z`, build del CI local) y qué jobs o etapas se ven afectados.
3. Riesgos: checks requeridos que cambian de nombre (romperían las reglas de protección), secretos nuevos, tiempo de ejecución, recursos de Docker.
4. Cómo se va a verificar.
Espera mi aprobación.

### Paso 2 — Implementar
- Rama `feature/<tema>` o `fix/<tema>` desde `DP.rama-principal` actualizado.
- Scripts en las carpetas de scripts de `DP.ci` o `DP.ci-local`, con `set -euo pipefail`, comentarios de cabecera (qué hace, uso, variables) y limpieza con `trap` si crean recursos.
- Valida antes de subir: `actionlint` (workflow), `hadolint` (`Dockerfile`), `bash -n` en los scripts, `docker compose config` si cambia el compose. Usa sus imágenes Docker si no están instalados; no te saltes una validación en silencio.
- No inventes versiones de acciones ni de imágenes: verifica que existen.
- Si una prueba falla, distingue un fallo del pipeline de uno del código; no cambies código de la aplicación para que el pipeline pase.

### Paso 3 — Verificar de punta a punta
1. Push y PR: los checks del PR en verde (`gh pr checks`).
2. Si toca el CI local: apúntalo temporalmente a la rama (cómo: `DP.ci-local`), lanza el job y revisa el resultado de cada etapa (en Jenkins, el *Stage View* o la API `wfapi/describe`). Al terminar, vuelve a la rama principal **y lanza un build sobre ella**: en Jenkins, el sondeo sigue la rama del último build, y sin ese build los merges siguientes no se construyen.
3. Si toca un despliegue: prueba de humo contra el entorno y, si hay rollback, pruébalo forzando un fallo.
4. Si algo falla, diagnostica desde el log, corrige y repite; no reportes éxito sin la ejecución real.
5. Elimina los contenedores, redes y volúmenes de prueba que hayas creado.
6. Tabla final `Comprobación | Resultado (✅ / ❌ / ⚠️ no probado) | Evidencia`.

### Paso 4 — Documentar
ADR nuevo o actualizado de `DP.adr-cicd`, la guía de CI/CD y los diagramas de `DP.guias` (incluida su sección de problemas frecuentes, si existe), `technical.md` y la documentación de `DP.ci-local`. Si cambian los datos del pipeline, actualiza también `CLAUDE.md` → *Datos para los prompts*.

## Informe final
Qué cambió por evento, evidencia de cada verificación (IDs de ejecución, número de build, resultado por etapa), lo que el autor debe hacer (secretos, merge, aprobaciones) y riesgos pendientes.
