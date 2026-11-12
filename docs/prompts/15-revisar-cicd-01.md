# 15 · Ciclo de revisión 6/9 — CI/CD (v01)

| | |
|---|---|
| **Comando** | `/15-revisar-cicd [NNN de la ronda de revisión; vacío = siguiente]` |
| **Archivo** | `docs/prompts/15-revisar-cicd-01.md` |
| **Versión anterior** | — |

> `{ARGUMENTO}` es lo que se escribe después del comando. Sin Claude Code, copia este prompt y reemplaza `{ARGUMENTO}` a mano.

## Rol
Actúa como ingeniero DevOps senior experto en GitHub Actions, Jenkins, Docker y SonarQube. Buscas evidencia en el código; no confirmas lo que ya se cree ni inventas problemas.

## Reglas comunes
Aplica `docs/prompts/09-reglas-revision-01.md`: orden del ciclo, fuentes de verdad, severidades, reglas y formato del informe. Esta revisión no modifica código.

## Alcance
`.github/` (workflow, scripts, ruleset, plantilla de PR), `Dockerfile`, `render.yaml`, `Jenkinsfile`, `docker/jenkins/` y `docker-compose.yml`, contra ADR-0009 a ADR-0015 y `docs/guides/ci-cd.md`. Puedes consultar el estado real con `gh` (`gh run list`, `gh api repos/{owner}/{repo}/rulesets`) y la API del Jenkins local, solo lectura. **Revisa solo este alcance**; lo que veas fuera de él va como INFO con una línea, para la revisión que le corresponde.
Ronda: `{ARGUMENTO}` (vacía = el siguiente NNN libre en `docs/reviews/`, o el de la ronda en curso si ya existe un informe `10`–`16` sin su `18`).

## Comprobaciones
> Los scripts de CI/CD los generó una IA: **no asumas que funcionan**. Toda afirmación ("compila", "la imagen arranca", "el job pasa") debe estar respaldada por un comando ejecutado o por una ejecución real (ID de run o número de build), con su resultado. Lo que no se pudo probar va como "No evaluado".

0. **Validación estática:** `actionlint` sobre el workflow (expresiones, contextos y `shellcheck` de los `run:`) y `hadolint` sobre el `Dockerfile` (con sus imágenes Docker si no están instalados); `bash -n` en `.github/scripts/` y `docker/jenkins/scripts/`; `.dockerignore` excluye `target/`, `.git/` e IDEs y no excluye nada que el `Dockerfile` copie.
1. Flujo GitHub Flow (ADR-0012): el ruleset activo coincide con `.github/ruleset/`, los checks requeridos existen con ese nombre exacto y el environment `production` pide revisor.
2. Workflow: jobs, condiciones por evento (PR, `master`, tag), `permissions:` mínimos por job (`packages: write` solo donde se publica), `timeout-minutes`, `concurrency` y caché de Maven; en un `pull_request` nunca hay login ni push a registros; acciones de terceros que existen en la versión indicada (verificado, no supuesto) y, como recomendación, fijadas por SHA.
3. Imagen: `Dockerfile` multi-stage; la imagen final no tiene JDK, código fuente ni `target/` y no corre como root (compruébalo con `docker run --rm --entrypoint id`); perfil `prod`; capas de dependencias reutilizadas en un segundo build; etiquetas `latest`/`sha`/versión; publicación solo desde `master` y tags. Recomendaciones opcionales, justificadas: escaneo de la imagen (Trivy) y Dependabot para acciones y Maven.
4. Verificación y despliegue: prueba de humo compartida, aprobación antes de Render, despliegue por sha (no `latest`).
5. Jenkins: etapas del `Jenkinsfile` alineadas con ADR-0014, sondeo en `casc.yaml` y `Jenkinsfile`, Quality Gate de ADR-0015, rollback del despliegue local, limpieza de imágenes y contenedores temporales.
6. Secretos: solo en *secrets*/environments o en `docker/jenkins/.env` (no versionado); nada impreso en logs.
7. Coherencia: la guía de CI/CD, `technical.md` §9 y los diagramas describen el pipeline actual.
8. Evidencia de ejecución: últimas ejecuciones en `master` y último build de Jenkins (resultado por job o etapa). Si algo falla, distingue un fallo **del pipeline** (configuración) de uno **del código** (prueba rota).
9. Al terminar, elimina los contenedores y volúmenes de prueba que hayas creado.

## Entrega
Informe `docs/reviews/NNN-15-cicd.md` con el formato de las reglas comunes. **No modifiques ningún otro archivo.**
Siguiente comando del orden: `/16-revisar-arquitectura`.
