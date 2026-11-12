# ADR-0014: Pipeline de Jenkins por etapas con despliegue local

- **Estado:** Aceptado
- **Fecha:** 2026-11-12
- **Amplía:** [ADR-0010](0010-jenkins-local-de-practica.md). El `Jenkinsfile` deja de reproducir solo las dos etapas del CI y termina con la API desplegada en la máquina local.

## Contexto

El `Jenkinsfile` tenía dos etapas (`./mvnw clean verify` e imagen Docker), y el Stage View mostraba dos columnas: si algo fallaba, había que leer el log completo para saber si fue la compilación, una prueba unitaria, Testcontainers o el umbral de cobertura. Además, el pipeline terminaba con una imagen construida que nadie arrancaba. En la presentación final del curso, el pipeline de Jenkins mejor valorado mostraba cada paso como una etapa y terminaba con la aplicación desplegada en un servidor on-premise. El despliegue persistente de GitHub Actions (ADR-0013) depende de una cuenta en Render. Jenkins, en cambio, ya corre en la máquina del desarrollador y controla su Docker.

## Decisión

- El `Jenkinsfile` tiene una etapa por paso: **Compilar** (`install -DskipTests`, para que las etapas siguientes trabajen por módulo), **Pruebas unitarias** (`domain`, `application`), **Pruebas de integración** (`infrastructure`, con Testcontainers), **Cobertura** (`jacoco:report` y `jacoco:check`, mismo umbral del 80 %), **Imagen Docker**, **Verificación de la imagen** y **Despliegue local**.
- **Verificación de la imagen** (`docker/jenkins/scripts/verify-image.sh`): equivale al job del mismo nombre en GitHub Actions. Usa una red, un PostgreSQL y claves JWT temporales, y ejecuta la prueba de humo compartida (`.github/scripts/smoke-test.sh`). Al terminar, pase o falle, lo retira todo.
- **Despliegue local** (`docker/jenkins/scripts/deploy-local.sh`): entorno persistente en el Docker del host. Lo forman la red `contract-api-deploy`, el contenedor `contract-api-deploy-db` (volumen `contract-api-deploy-data`) y la API `contract-api-deploy` en `127.0.0.1:${DEPLOY_PORT:-8090}`, todos con `--restart unless-stopped`.
  - Las claves JWT y la contraseña de la base se generan una sola vez en `$JENKINS_HOME/contract-api-deploy`, así que los tokens siguen valiendo entre despliegues.
  - La contraseña del administrador llega por `DEPLOY_ADMIN_PASSWORD` desde `docker/jenkins/.env`. Si no está definida, la etapa se omite.
  - Después de desplegar repite la prueba de humo. Si falla, vuelve a levantar la imagen anterior y el build falla.
  - Solo conserva la imagen desplegada y la anterior. Los demás tags `contract-api:jenkins-*` se borran.
- No hay aprobación manual (`input`) antes del despliegue local. La aprobación ya ocurre al integrar el PR en `master` con los checks en verde (ADR-0012), y este entorno solo existe en la máquina del desarrollador.
- La prueba de humo imprime `::add-mask::` solo en GitHub Actions (`GITHUB_ACTIONS=true`), para que el token no quede en el log de Jenkins.

## Alternativas consideradas

1. **Mantener dos etapas y desplegar a mano con `docker run`.** Pro: nada nuevo que mantener. Contra: el Stage View no ubica el fallo y el despliegue no se repite igual cada vez.
2. **Desplegar con el `docker-compose.yml` de la raíz.** Pro: reutiliza la definición existente. Contra: ese compose usa el perfil `local` y el volumen compartido con el proyecto v3, donde ya existe otro `admin`, y además publica el puerto 8081 que se usa para desarrollar.
3. **Etapa `input` de aprobación antes de desplegar.** Pro: imita el environment `production`. Contra: cada build sondeado quedaría esperando y, con `disableConcurrentBuilds`, bloquearía los siguientes; la aprobación real ya está en GitHub.
4. **SonarQube local con Quality Gate como etapa.** Pro: análisis estático dentro de Jenkins. Contra: otro contenedor de ~2 GB de memoria; se deja como seguimiento (el CI oficial ya puede usar SonarCloud, ADR-0011).

## Consecuencias

- Positivas:
  - El Stage View muestra en qué paso falló un build.
  - Cada merge a `master` deja la API actualizada en `http://localhost:8090` con datos persistentes, y vuelve sola a la versión anterior si la nueva no pasa la prueba de humo.
  - La verificación y la prueba de humo son las mismas que en GitHub Actions.
- Negativas:
  - Maven se invoca varias veces por build, lo que añade unos segundos frente a un único `verify`.
  - Jenkins se conecta a las redes de los contenedores que crea, lo que refuerza el riesgo ya aceptado de que controle el Docker del host (ADR-0010).
  - Cambiar `DEPLOY_ADMIN_PASSWORD` después del primer despliegue no cambia la contraseña del `admin` existente (BR-USR-007).
- Seguimiento: SonarQube local con Quality Gate; reemplazar el Docker local por un servidor real (SSH + `docker compose`) si se necesita un entorno compartido.
