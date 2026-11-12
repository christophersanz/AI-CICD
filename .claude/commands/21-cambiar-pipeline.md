---
description: Operación: cambia el CI/CD (GitHub Actions, Jenkins, Docker, despliegues) y lo verifica de punta a punta
argument-hint: <cambio a realizar en el pipeline o el despliegue>
---

# Rol
Actúa como ingeniero DevOps senior experto en GitHub Actions, Jenkins (pipeline declarativo, JCasC), Docker, SonarQube y despliegue de aplicaciones Spring Boot.

# Contexto y fuentes de verdad
- @CLAUDE.md (§3.1 flujo de Git), `docs/guides/ci-cd.md`, `docs/technical.md` §9 y los ADR de CI/CD:
  - ADR-0009 GitHub Actions e imágenes en GHCR y Docker Hub; ADR-0011 SonarCloud;
  - ADR-0012 GitHub Flow con checks obligatorios y aprobación de `production`; ADR-0013 despliegue en Render;
  - ADR-0010 y ADR-0014 Jenkins local por etapas con verificación y despliegue local; ADR-0015 SonarQube local con Quality Gate.
- Archivos: `.github/workflows/ci-cd.yml`, `.github/scripts/`, `.github/ruleset/`, `Dockerfile`, `render.yaml`, `Jenkinsfile`, `docker/jenkins/` (compose, `casc.yaml`, `scripts/`).
- Cambio pedido: **$ARGUMENTS** (si está vacío, pregunta qué se quiere cambiar).

# Objetivo
El cambio funciona en una ejecución real (no solo con la sintaxis validada): GitHub Actions en el PR y, si toca Jenkins, un build del Jenkins local con todas sus etapas en el estado esperado.

# Restricciones
- **Sin parches incrustados en el prompt ni archivos de cientos de líneas:** cambios pequeños y revisables.
- Secretos solo como *secrets* del repositorio o del environment, o en `docker/jenkins/.env` (no versionado). Nunca en el código, en los logs (enmascarados) ni en la línea de comandos.
- No modifiques el ruleset ni los environments de GitHub sin mostrarme antes el cambio exacto (JSON o comando) y esperar mi aprobación.
- **No integres PRs ni apruebes despliegues** de `production`: es el control humano de ADR-0012.
- Fija versiones de imágenes y acciones (nada de `latest` sin justificarlo).
- Mantén alineados GitHub Actions y el `Jenkinsfile` cuando el cambio aplique a ambos (por ejemplo, la prueba de humo compartida `.github/scripts/smoke-test.sh`).

# Forma de trabajo
Crea o retoma `docs/sessions/AAAA-MM-DD-21-cambiar-pipeline-<tema>.md` (docs/sessions/README.md).

## Paso 1 — Análisis de impacto y plan (sin cambiar nada)
1. Estado actual de lo que se va a tocar (confírmalo leyendo los archivos y, si aplica, `gh run list`, `gh api` o la API del Jenkins local).
2. Qué cambia en cada evento (PR, merge a `master`, tag `vX.Y.Z`, build de Jenkins) y qué jobs o etapas se ven afectados.
3. Riesgos: checks requeridos que cambian de nombre (romperían el ruleset), secretos nuevos, tiempo de ejecución, recursos de Docker.
4. Cómo se va a verificar.
Espera mi aprobación.

## Paso 2 — Implementar
- Rama `feature/<tema>` o `fix/<tema>` desde `master` actualizado.
- Scripts en `.github/scripts/` o `docker/jenkins/scripts/` con `set -euo pipefail`, comentarios de cabecera (qué hace, uso, variables) y limpieza con `trap` si crean recursos.
- Valida antes de subir: `bash -n` en los scripts, YAML con `ruby -ryaml`, `docker compose config` si cambia el compose.

## Paso 3 — Verificar de punta a punta
1. Push y PR: los checks del PR en verde (`gh pr checks`).
2. Si toca Jenkins: apunta temporalmente `REPO_BRANCH` de `docker/jenkins/.env` a la rama, `docker compose ... up -d --build`, lanza el job y revisa el *Stage View* (API `wfapi/describe`) y el log de cada etapa. Vuelve a `REPO_BRANCH=master` al terminar.
3. Si toca un despliegue: prueba de humo contra el entorno y, si hay rollback, pruébalo forzando un fallo.
4. Si algo falla, diagnostica desde el log, corrige y repite; no reportes éxito sin la ejecución real.

## Paso 4 — Documentar
ADR nuevo o actualizado (CLAUDE.md §4: "si cambias el pipeline, actualiza ADR-0009/0012/0013/0014/0015 o registra uno nuevo"), `docs/guides/ci-cd.md` (secciones y *Problemas frecuentes*), `docs/technical.md` §9, `docker/jenkins/README.md` y los diagramas de `docs/guides/arquitectura.md`.

# Informe final
Qué cambió por evento, evidencia de cada verificación (IDs de ejecución, número de build, resultado por etapa), lo que el autor debe hacer (secretos, merge, aprobaciones) y riesgos pendientes.
