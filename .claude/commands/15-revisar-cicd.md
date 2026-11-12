---
description: Ciclo de revisión 6/9: revisa CI/CD, Docker, Jenkins y despliegues de contract-api (sin modificar nada)
argument-hint: [NNN de la ronda de revisión; vacío = siguiente]
---

# Rol
Actúa como ingeniero DevOps senior experto en GitHub Actions, Jenkins, Docker y SonarQube. Buscas evidencia en el código; no confirmas lo que ya se cree ni inventas problemas.

# Reglas comunes
@docs/prompts/review-rules.md

Fuentes de verdad: @CLAUDE.md, @docs/architecture.md, @docs/business-rules.md, `docs/technical.md` y `docs/adr/`.

# Alcance
`.github/` (workflow, scripts, ruleset, plantilla de PR), `Dockerfile`, `render.yaml`, `Jenkinsfile`, `docker/jenkins/` y `docker-compose.yml`, contra ADR-0009 a ADR-0015 y `docs/guides/ci-cd.md`. Puedes consultar el estado real con `gh` (`gh run list`, `gh api repos/{owner}/{repo}/rulesets`) y la API del Jenkins local, solo lectura. **Revisa solo este alcance**; lo que veas fuera de él va como INFO con una línea, para la revisión que le corresponde.
Ronda: `$ARGUMENTS` (vacía = el siguiente NNN libre en `docs/reviews/`, o el de la ronda en curso si ya existe un informe `10`–`16` sin su `18`).

# Comprobaciones
1. Flujo GitHub Flow (ADR-0012): el ruleset activo coincide con `.github/ruleset/`, los checks requeridos existen con ese nombre exacto y el environment `production` pide revisor.
2. Workflow: jobs, condiciones por evento (PR, `master`, tag), permisos mínimos por job, concurrencia, caché, versiones de acciones fijadas.
3. Imagen: `Dockerfile` multi-stage, usuario sin privilegios, perfil `prod`, etiquetas `latest`/`sha`/versión, publicación solo desde `master` y tags.
4. Verificación y despliegue: prueba de humo compartida, aprobación antes de Render, despliegue por sha (no `latest`).
5. Jenkins: etapas del `Jenkinsfile` alineadas con ADR-0014, sondeo en `casc.yaml` y `Jenkinsfile`, Quality Gate de ADR-0015, rollback del despliegue local, limpieza de imágenes y contenedores temporales.
6. Secretos: solo en *secrets*/environments o en `docker/jenkins/.env` (no versionado); nada impreso en logs.
7. Coherencia: la guía de CI/CD, `technical.md` §9 y los diagramas describen el pipeline actual.
8. Evidencia de ejecución: últimas ejecuciones en `master` y último build de Jenkins (resultado por job o etapa).

# Entrega
Informe `docs/reviews/NNN-15-cicd.md` con el formato de las reglas comunes. **No modifiques ningún otro archivo.**
Siguiente comando del orden: `/16-revisar-arquitectura`.
