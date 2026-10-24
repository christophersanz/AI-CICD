# ADR-0009: CI/CD con GitHub Actions, GitHub Flow e imagen en GHCR

- **Estado:** Aceptado
- **Fecha:** 2026-10-17
- **Nota:** ADR-0006 a ADR-0008 están reservados para la fase 4 de seguridad (`docs/design/003-security-design.md`, paso 4.3).

## Contexto

La API vivía como una carpeta más del repositorio del curso, junto a otras versiones y proyectos, sin integración continua: las pruebas solo se ejecutaban en local y nada impedía integrar cambios con pruebas rotas. Un pipeline necesita un repositorio con una sola aplicación, una rama principal protegida y un artefacto desplegable.

## Decisión

- **Repositorio propio** (originalmente `GalaxyTraining/CURS-000617-CICD`, extraído con `git subtree split`; esta copia vive en `christophersanz/AI-CICD`).
- **Una sola rama, `master`** (revisado el 2026-10-24; antes GitHub Flow con ramas `feature/*` y PR obligatorio hacia `main`): es la única rama de larga vida y la rama por defecto. Se trabaja en local sobre `master` y se hace push directo; el workflow se ejecuta en cada push. Los Pull Requests a `master` siguen disparando el CI, pero son opcionales.
- **Protección de `master`** con el ruleset `master-protegida` (`.github/ruleset/master-protegida.json`), aplicado a la rama por defecto: sin force push ni borrado. No exige PR ni checks previos.
- **GitHub Actions** (`.github/workflows/ci-cd.yml`):
  - `build`: `./mvnw clean verify` con JDK 21, incluidas las pruebas con Testcontainers y la cobertura JaCoCo; publica los informes y un resumen de cobertura.
  - `image`: construye la imagen con el `Dockerfile` multi-stage; en un PR solo la construye, en `master` la publica en `ghcr.io` (`latest` + sha corto) y en un tag `vX.Y.Z` con las etiquetas de versión. Las mismas etiquetas se publican también en Docker Hub (`docker.io/<usuario>/ai-cicd`) si el repositorio define la variable `DOCKERHUB_USERNAME` y el secreto `DOCKERHUB_TOKEN` (token de acceso con permiso *Read & Write*); sin ellos, por ejemplo en un fork, solo se publica en GHCR.
- **Cobertura:** JaCoCo con un umbral del 80 % de líneas en `domain` y `application` (`architecture.md` §9); el build falla por debajo. `infrastructure` solo genera el informe.
- **Imagen:** JRE 21, usuario sin privilegios, capas de Spring Boot y perfil `prod` (sin valores por defecto, sin clave efímera y sin Swagger).

- **Docker Hub además de GHCR:** la organización no permite paquetes públicos en GHCR, de modo que descargar la imagen exige un token; en Docker Hub el repositorio es público y los alumnos la descargan sin autenticarse. GHCR se mantiene porque no requiere credenciales externas.

## Alternativas consideradas

1. **Mantener la API en el repositorio del curso con filtros por ruta.** Pro: sin repositorio nuevo. Contra: protección de rama, etiquetas de versión y permisos mezclados con el resto del curso.
2. **GitFlow (`develop`, `release/*`).** Pro: útil con versiones planificadas. Contra: más ramas y ceremonia de la que necesita un equipo pequeño con despliegue continuo.
3. **Jenkins o GitLab CI.** Pro: autoalojables. Contra: infraestructura adicional; el código ya está en GitHub y GHCR viene integrado.
4. **GitHub Flow con PR obligatorio y checks requeridos** (diseño inicial). Pro: ningún cambio llega a `master` sin el CI en verde. Contra: ceremonia innecesaria para un único desarrollador que trabaja en local sobre una sola rama; se puede recuperar endureciendo el ruleset.

## Consecuencias

- Positivas: flujo simple para un único desarrollador; cada push a `master` se compila, se prueba, se mide su cobertura y produce una imagen versionada; el historial de `master` no se puede reescribir.
- Negativas del flujo de una sola rama: el CI valida **después** del push, así que un cambio roto puede llegar a `master` (y a la imagen `latest`) si no se ejecuta `./mvnw clean verify` antes; se corrige con un commit nuevo. Si el equipo crece, volver a exigir PR y checks en el ruleset.
- Negativas: las pruebas con Testcontainers alargan el pipeline; el ruleset se versiona en `.github/ruleset/` pero se importa a mano en GitHub (Settings → Rules → Rulesets) y cualquier cambio hay que replicarlo allí; se aplica porque el repositorio es público (en uno privado requiere plan Pro o Team).
- Docker Hub limita las descargas anónimas por IP: un aula que comparte IP puede alcanzar el límite; con `docker login` el límite es mayor.
- Seguimiento: el análisis estático con SonarCloud se incorporó en ADR-0011. Despliegue (CD a un entorno), análisis de seguridad (CodeQL/Dependabot) y pruebas de API con Postman CLI quedan fuera de esta decisión.
