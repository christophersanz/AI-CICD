# Revisión 002 · 6 — CI/CD

- **Prompt:** `docs/prompts/06-revisar.md`, modo `cicd` (paso 6 de 9 de la ronda 002)
- **Fecha:** 2026-11-12 · **Commit revisado:** `183d054` (`master`)
- **Alcance:**
  - CI: `.github/workflows/ci-cd.yml`, `.github/scripts/`, `.github/ruleset/` y la plantilla de PR.
  - Imagen y despliegue: `Dockerfile`, `render.yaml` y `docker-compose.yml`.
  - CI local: `Jenkinsfile` y `docker/jenkins/`.
  - Criterio: ADR-0009 a ADR-0015 y `docs/guides/ci-cd.md`.

> Todo lo que este informe da por verificado se comprobó con un comando ejecutado: `actionlint`, `hadolint`, `bash -n`, `gh api`, `docker run` sobre la imagen y la API de Jenkins.

## 1. Resumen

El pipeline cumple los ADR:
- Las reglas de protección activas en GitHub son idénticas a las versionadas, y los checks requeridos coinciden con los nombres de los jobs.
- El environment `production` exige la aprobación del autor.
- Cada job tiene permisos mínimos y en un PR no se publica nada.
- La imagen no tiene JDK ni código fuente y no corre como root.
- El Jenkins local ejecuta sus 8 etapas con el Quality Gate.
- La validación estática pasa sin errores.

No hay ERROR ni WARNING. Quedan tres INFO: las acciones están fijadas por versión mayor y no por SHA, hay dos avisos de `hadolint` en el `Dockerfile` de Jenkins, y el despliegue en Render nunca se ejecutó porque Render no está configurado (No evaluado).

**Build:** el de la ronda (paso 1). **Última ejecución en `master`:** CI/CD de `183d054` → success. **CI local:** Jenkins #16 sobre `183d054` → SUCCESS, Quality Gate OK.

## 2. Comprobaciones

| # | Comprobación | Severidad | Archivo/componente | Evidencia (comando) | Regla o criterio | Corrección propuesta |
|---|---|---|---|---|---|---|
| 0 | Validación estática | OK (INFO en Jenkins) | workflow, `Dockerfile`, `docker/jenkins/Dockerfile`, scripts | `actionlint 1.7.7` → 0 errores; `hadolint v2.12.0` sobre `Dockerfile` → 0 avisos; sobre `docker/jenkins/Dockerfile` → `DL3002` (último `USER` root, aceptado en ADR-0010 por el socket de Docker) y `DL3008` (`apt-get install jq` sin versión fijada); `bash -n` en los 7 scripts (2 de `.github/scripts`, 3 de `docker/jenkins/scripts` y 2 del kit) → OK | ADR-0009, ADR-0010 | Ver I-07 |
| 1 | Flujo de Git (`DP.flujo-git`) | OK | ruleset `master-protegida`, environment `production` | `gh api …/rulesets`: activo y versionado son iguales (`deletion`, `non_fast_forward`, `pull_request`, `required_status_checks`); checks requeridos «Build, pruebas y cobertura» e «Imagen Docker», con los mismos nombres que los jobs; `production` con revisor `christophersanz` y política de ramas | ADR-0012 | — |
| 2 | Workflow: eventos, permisos, *timeouts*, concurrencia, caché, acciones | OK (INFO en las acciones) | `ci-cd.yml` | `permissions: contents: read` global; `packages: write` solo en `image`; `timeout-minutes` en los 4 jobs; `concurrency` que cancela solo en PR y otra propia en `deploy`; `cache: maven` y caché de capas `type=gha`. Logins con `if: github.event_name != 'pull_request'` y `push: ${{ github.event_name != 'pull_request' }}`. Acciones `actions/*` y `docker/*` en versión mayor (`@v4` a `@v7`) | ADR-0009 | Ver I-06 |
| 3 | Imagen | OK | `Dockerfile` | `docker run --entrypoint id ai-cicd:local` → `uid=999(app)`; sin `javac`; sin `src/`, `target/` ni `pom.xml`; `SPRING_PROFILES_ACTIVE=prod`; multi-stage con capas de Spring Boot. Etiquetas `latest`, `sha-<corto>` y versión con `docker/metadata-action` | ADR-0009; `DP.imagen` | — |
| 4 | Verificación y despliegue (`DP.despliegue`) | OK / No evaluado | jobs `verify` y `deploy`, `render.yaml`, `.github/scripts/` | `verify` levanta la imagen del sha recién publicado con PostgreSQL y ejecuta `smoke-test.sh` (verde en cada merge). `deploy` exige la aprobación de `production`, despliega por sha (no `latest`) y repite la prueba de humo contra la URL pública. Nunca se ejecutó, porque falta `RENDER_SERVICE_ID` | ADR-0012, ADR-0013 | Ver I-08 |
| 5 | CI local (`DP.ci-local`) | OK | `Jenkinsfile`, `docker/jenkins/` | Disparador en `casc.yaml` y en el `Jenkinsfile`; 8 etapas con el Quality Gate antes de la imagen; verificación efímera; despliegue local con rollback (probado en la ronda anterior) y limpieza de imágenes. Jenkins #16 → SUCCESS, con el gate en OK | ADR-0014, ADR-0015 | — |
| 6 | Secretos | OK | workflow, scripts de Jenkins | Secretos solo como *secrets* o en `docker/jenkins/.env` (ignorado por git); contraseñas de verificación generadas en el momento y enmascaradas (`::add-mask::` solo en GitHub Actions) | ADR-0012, ADR-0014 | — |
| 7 | Coherencia de la documentación | OK | `docs/guides/ci-cd.md`, `docs/technical.md` §9, `docs/guides/diagramas-arquitectura.md`, `CLAUDE.md` §5 | Describen los mismos 4 jobs, las 8 etapas de Jenkins, el Quality Gate y los dos despliegues | `CLAUDE.md` §4 | — |
| 8 | Evidencia de ejecución | OK | GitHub Actions, Jenkins | Últimas ejecuciones en `master` (PR #6 a #9) → success; Jenkins #16 → SUCCESS | — | — |
| 9 | Limpieza | OK | — | `actionlint` y `hadolint` se ejecutaron con `--rm`; no quedaron contenedores de prueba | — | — |

## 3. Trazabilidad con los ADR de CI/CD

| ADR | Decisión | Implementación | Evidencia | Estado |
|---|---|---|---|---|
| 0009 | GitHub Actions; imagen en GHCR y Docker Hub | `ci-cd.yml` (`build`, `image`) | runs en verde | PASS |
| 0010 | Jenkins local de práctica | `docker/jenkins/` | Jenkins #16 | PASS |
| 0011 | SonarCloud opcional | paso condicionado a `SONAR_TOKEN` | se omite sin el secreto | PASS (opcional) |
| 0012 | GitHub Flow y aprobación de `production` | ruleset y environment | `gh api` | PASS |
| 0013 | Despliegue en Render | job `deploy`, `render.yaml` | nunca se ejecutó (falta configuración) | No evaluado |
| 0014 | Jenkins por etapas y despliegue local | `Jenkinsfile`, `deploy-local.sh` | Jenkins #16; `localhost:8090` | PASS |
| 0015 | SonarQube local con Quality Gate | `sonar-analysis.sh` | gate OK en #16 | PASS |

## 4. Pruebas existentes y faltantes

- **Existentes:** la prueba de humo compartida (`smoke-test.sh`) en `verify`, en el despliegue de Render y en Jenkins; el rollback del despliegue local, probado con una imagen inválida.
- **Faltantes:** una ejecución real de `deploy` en Render (I-08).

## 5. Hallazgos

### ERROR
Ninguno.

### WARNING
Ninguno.

### INFO

**I-06 · Acciones de terceros fijadas por versión mayor, no por SHA.** `actions/checkout@v7`, `docker/build-push-action@v7` y otras. Si se compromete una acción, una versión mayor puede cambiar sin aviso. Recomendación: fijarlas por SHA con el comentario de la versión (`uses: actions/checkout@<sha> # v7.x.y`) y mantenerlas con Dependabot (`.github/dependabot.yml`, `package-ecosystem: github-actions`).

**I-07 · Avisos de `hadolint` en `docker/jenkins/Dockerfile`.** `DL3002` (root) está aceptado en ADR-0010. `DL3008`: `apt-get install jq` sin versión. Es una imagen solo de práctica local, así que se puede fijar (`jq=<versión>`) o registrar la excepción con `# hadolint ignore=DL3008` y su motivo.

**I-08 · El despliegue en Render no se ha ejecutado nunca (No evaluado).** El job queda `skipped` porque el repositorio no define `RENDER_SERVICE_ID`; los pasos para configurarlo están en `docs/guides/ci-cd.md` §5.4. Hasta que se ejecute, no hay evidencia de que funcione.

## 6. Aspectos positivos

- Las reglas de protección versionadas son idénticas a las activas, y eso se puede auditar desde el repositorio.
- El despliegue usa la imagen exacta del sha que pasó la verificación, nunca `latest`.
- El mismo script de prueba de humo sirve a GitHub Actions y a Jenkins.
- La imagen final es mínima y no corre como root.

## 7. Conclusión y siguiente paso

El CI/CD cumple los ADR 0009 a 0015. Lo pendiente es endurecimiento opcional (SHA, `hadolint`) y la configuración de Render.

**Siguiente paso del orden:** `/06-revisar arquitectura` (paso 7 de 9, ronda 002).
