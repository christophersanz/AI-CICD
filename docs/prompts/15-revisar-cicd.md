# 15 · Ciclo de revisión 6/9 — CI/CD

| | |
|---|---|
| **Comando** | `/15-revisar-cicd [NNN de la ronda de revisión; vacío = siguiente]` |
| **Archivo** | `docs/prompts/15-revisar-cicd.md` (única versión; los cambios se hacen aquí y quedan en el historial de git) |
| **Origen** | Nuevo en este proyecto (fuentes en `docs/prompts/README.md` §3) |

> `{ARGUMENTO}` es lo que se escribe después del comando. `DP.<clave>` es un dato del proyecto: está en `CLAUDE.md` → *Datos para los prompts*. Sin Claude Code, copia este prompt y reemplaza `{ARGUMENTO}` a mano.

## Rol
Actúa como ingeniero DevOps senior experto en las herramientas de `DP.ci`, `DP.ci-local`, Docker y análisis estático. Buscas evidencia en el código; no confirmas lo que ya se cree ni inventas problemas.

## Reglas comunes
Aplica `docs/prompts/09-reglas-revision.md`: orden del ciclo, fuentes de verdad, severidades, reglas y formato del informe. Esta revisión no modifica código.

## Alcance
Lo que describen `DP.ci`, `DP.imagen`, `DP.despliegue`, `DP.ci-local` y `DP.sonarqube` (workflows, scripts, reglas de protección versionadas, plantilla de PR, `Dockerfile`, archivos de despliegue y `docker-compose.yml`), contra `DP.adr-cicd`, `DP.flujo-git` y la guía de CI/CD de `DP.guias`. Puedes consultar el estado real en solo lectura (con `gh`: `gh run list`, `gh api repos/{owner}/{repo}/rulesets`; y la API del CI local). **Revisa solo este alcance**; lo que veas fuera de él va como INFO con una línea, para la revisión que le corresponde.
Ronda: `{ARGUMENTO}` (vacía = el siguiente NNN libre en `docs/reviews/`, o el de la ronda en curso si ya existe un informe `10`–`16` sin su `18`).

## Comprobaciones
> Los scripts de CI/CD los generó una IA: **no asumas que funcionan**. Toda afirmación ("compila", "la imagen arranca", "el job pasa") debe estar respaldada por un comando ejecutado o por una ejecución real (ID de run o número de build), con su resultado. Lo que no se pudo probar va como "No evaluado".

0. **Validación estática:** `actionlint` sobre el workflow (expresiones, contextos y `shellcheck` de los `run:`) y `hadolint` sobre el `Dockerfile` (con sus imágenes Docker si no están instalados); `bash -n` en los scripts de `DP.ci` y `DP.ci-local`; `.dockerignore` excluye `target/`, `.git/` e IDEs y no excluye nada que el `Dockerfile` copie.
1. Flujo de Git (`DP.flujo-git`): las reglas de protección activas coinciden con las versionadas, los checks requeridos existen con ese nombre exacto y, si `DP.despliegue` exige aprobación, el environment de producción pide revisor.
2. Workflow: jobs, condiciones por evento (PR, `DP.rama-principal`, tag), `permissions:` mínimos por job (`packages: write` solo donde se publica), `timeout-minutes`, `concurrency` y caché de Maven; en un `pull_request` nunca hay login ni push a registros; acciones de terceros que existen en la versión indicada (verificado, no supuesto) y, como recomendación, fijadas por SHA.
3. Imagen: `Dockerfile` multi-stage; la imagen final no tiene JDK, código fuente ni `target/` y no corre como root (compruébalo con `docker run --rm --entrypoint id`); configuración de producción; capas de dependencias reutilizadas en un segundo build; etiquetas según `DP.imagen`; publicación solo desde la rama principal y tags. Recomendaciones opcionales, justificadas: escaneo de la imagen (Trivy) y Dependabot para acciones y Maven.
4. Verificación y despliegue según `DP.despliegue`: prueba de humo, aprobación humana antes de producción, despliegue de una imagen inmutable (sha o versión, no `latest`) y rollback si existe.
5. CI local (`DP.ci-local`), si existe: etapas alineadas con su ADR, disparador que sobrevive a reinicios, Quality Gate (`DP.sonarqube`) antes de construir y desplegar, rollback y limpieza de imágenes y contenedores temporales.
6. Secretos: solo en *secrets*/environments o en los archivos locales no versionados que indique `DP.ci-local`; nada impreso en logs.
7. Coherencia: `DP.guias`, `technical.md` y los diagramas describen el pipeline actual.
8. Evidencia de ejecución: últimas ejecuciones en la rama principal y último build del CI local (resultado por job o etapa). Si algo falla, distingue un fallo **del pipeline** (configuración) de uno **del código** (prueba rota).
9. Al terminar, elimina los contenedores y volúmenes de prueba que hayas creado.

## Entrega
Informe `docs/reviews/NNN-15-cicd.md` con el formato de las reglas comunes. **No modifiques ningún otro archivo.**
Siguiente comando del orden: `/16-revisar-arquitectura`.
