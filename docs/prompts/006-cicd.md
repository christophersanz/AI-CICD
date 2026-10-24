Aplica en este repositorio (IA-CICD) los cambios de CI/CD y de arranque local que vienen en el parche de abajo. Sigue estos pasos en orden y detente a explicarme si alguno falla.

Contexto de lo que corrige el parche:
- El workflow `.github/workflows/ci-cd.yml` solo escucha `main`, pero la rama local es `master`: nunca se ejecutaría.
- Falta `.mvn/wrapper/maven-wrapper.properties` (sin él `./mvnw` y el `Dockerfile` fallan) y `mvnw` está en git sin permiso de ejecución.
- Badge, imagen Docker y Jenkins apuntaban al repo del instructor; ahora apuntan a `christophersanz/AI-CICD` (imagen `ai-cicd`).
- Se añade análisis SonarCloud con Quality Gate (ADR-0011), opcional mientras no exista el secreto `SONAR_TOKEN`.
- `docker-compose.yml` ahora levanta también la API (servicio `api`, perfil `local`) y hay un `.env.example` con todas las variables.
- Jenkins local tiene su propia plantilla `docker/jenkins/.env.example` (usuario y contraseña de Jenkins, repositorio y rama): su arranque usa `--env-file docker/jenkins/.env` y falla si ese archivo no existe.

Pasos:

1. Verifica que estás en la raíz del repo (`git rev-parse --show-toplevel`) y muéstrame `git status --short`. Los cambios sin commitear en `CLAUDE.md`, `docker/jenkins/README.md` y `docs/guides/ci-cd.md` son esperados: el parche parte de ellos.

2. Guarda el parche (bloque al final) en `/tmp/cicd.patch` exactamente como está, con un heredoc con comillas para que no se expanda nada:
   cat > /tmp/cicd.patch <<'CICD_PATCH_EOF'
   ...contenido...
   CICD_PATCH_EOF

3. Comprueba y aplica:
   git apply --check /tmp/cicd.patch && git apply /tmp/cicd.patch

4. Permiso de ejecución de mvnw, limpieza de .DS_Store y rama:
   git update-index --chmod=+x mvnw && chmod +x mvnw
   find . -name .DS_Store -not -path './.git/*' -delete
   git branch -m master main

5. Crea los `.env` locales a partir de sus plantillas (no se versionan; si ya existe alguno, conserva sus valores y añade solo las variables que falten):
   cp -n .env.example .env
   cp -n docker/jenkins/.env.example docker/jenkins/.env

6. Revisa el diff (`git diff --stat` y `git diff .github/workflows/ci-cd.yml docker-compose.yml`) y explícame brevemente cada cambio.

7. Haz dos commits en español (este es el arranque del repo; la protección de `main` aún no está activa):
   - "Poner en marcha el CI/CD en el repositorio propio y añadir SonarCloud": .github/, .gitignore, .mvn/, mvnw, CLAUDE.md, docker/jenkins/, docs/adr/, docs/guides/ci-cd.md
   - "Levantar la API con docker compose y documentar sus variables": .env.example, docker-compose.yml, README.md, docs/technical.md
   Comprueba que `.env` y `docker/jenkins/.env` NO quedan en ningún commit.

8. Verifica:
   ./mvnw -B -ntp clean verify          (Docker Desktop debe estar encendido: hay pruebas con Testcontainers)
   docker compose up -d --build
   docker logs -f contract-api          (espera "Started ContractApiApplication"; corta con Ctrl+C)
   Luego prueba el login: POST http://localhost:8081/api/v1/auth/login con {"username":"admin","password":"<SECURITY_BOOTSTRAP_ADMIN_PASSWORD del .env>"} y confirma que devuelve accessToken. Si responde 401, puede que la base ya tuviera un ADMIN previo: dímelo antes de borrar nada.

9. NO hagas push. Al terminar, dime qué falta hacer en GitHub (push de main, cambiar la rama por defecto a main y borrar master, secreto SONAR_TOKEN, importar el ruleset).

Parche:

```diff
diff --git a/.env.example b/.env.example
new file mode 100644
index 0000000..123e566
--- /dev/null
+++ b/.env.example
@@ -0,0 +1,26 @@
+# Copia este archivo como .env (no se versiona) y ajusta los valores.
+#   cp .env.example .env
+#   docker compose up -d --build
+
+# PostgreSQL (docker-compose.yml)
+DB_PASSWORD=contract
+
+# pgAdmin: http://localhost:5050
+PGADMIN_EMAIL=admin@example.com
+PGADMIN_PASSWORD=admin
+
+# API: http://localhost:8081/swagger-ui.html
+# local = Swagger activo y clave JWT efímera si no se definen las claves; prod = sin Swagger y claves obligatorias.
+SPRING_PROFILES_ACTIVE=local
+
+# Administrador inicial (BR-USR-007). Solo se crea si no existe ningún usuario ADMIN.
+# La contraseña debe tener entre 12 y 64 caracteres (BR-USR-003).
+SECURITY_BOOTSTRAP_ADMIN_USERNAME=admin
+SECURITY_BOOTSTRAP_ADMIN_PASSWORD=Admin-Local-2026
+
+# Claves JWT (opcionales en local, obligatorias en prod). Cada PEM en una sola línea con \n literales.
+# Generarlas: ver docs/guides/ci-cd.md §8.2.
+SECURITY_JWT_PUBLIC_KEY=
+SECURITY_JWT_PRIVATE_KEY=
+
+# Jenkins local (ADR-0010) usa su propio archivo: docker/jenkins/.env.example
diff --git a/.github/workflows/ci-cd.yml b/.github/workflows/ci-cd.yml
index 9454da2..dc585a8 100644
--- a/.github/workflows/ci-cd.yml
+++ b/.github/workflows/ci-cd.yml
@@ -1,5 +1,6 @@
-# CI/CD de contract-api (ADR-0009).
-# - Pull Request a main: build, pruebas (Testcontainers), cobertura y build de la imagen sin publicarla.
+# CI/CD de contract-api (ADR-0009, ADR-0011).
+# - Pull Request a main: build, pruebas (Testcontainers), cobertura, análisis SonarCloud (si hay SONAR_TOKEN)
+#   y build de la imagen sin publicarla.
 # - Push a main: lo anterior y publicación de la imagen en GHCR (latest + sha) y, si está configurado,
 #   en Docker Hub (variable DOCKERHUB_USERNAME y secreto DOCKERHUB_TOKEN).
 # - Tag vX.Y.Z: publicación con las etiquetas de versión.
@@ -20,14 +21,21 @@ concurrency:
 permissions:
   contents: read
 
+env:
+  # Nombre de la imagen en Docker Hub (en GHCR se usa el nombre del repositorio).
+  IMAGE_NAME: ai-cicd
+
 jobs:
   build:
     name: Build, pruebas y cobertura
     runs-on: ubuntu-latest
     timeout-minutes: 20
     steps:
+      # Historial completo: SonarCloud lo usa para distinguir el código nuevo y asignar autores.
       - name: Checkout
         uses: actions/checkout@v7
+        with:
+          fetch-depth: 0
 
       - name: JDK 21
         uses: actions/setup-java@v6
@@ -41,6 +49,27 @@ jobs:
       - name: Compilar y verificar
         run: ./mvnw -B -ntp clean verify
 
+      # El análisis solo se ejecuta si el repositorio tiene el secreto SONAR_TOKEN (en un fork se omite).
+      - name: ¿SonarCloud configurado?
+        id: sonar
+        env:
+          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
+        run: |
+          if [ -n "$SONAR_TOKEN" ]; then echo "enabled=true" >> "$GITHUB_OUTPUT"; else echo "enabled=false" >> "$GITHUB_OUTPUT"; fi
+
+      # Reutiliza las clases compiladas y los informes jacoco.xml del paso anterior. Con qualitygate.wait
+      # el job falla si no se cumple el Quality Gate (p. ej. issues críticos en el código nuevo).
+      - name: Análisis SonarCloud
+        if: steps.sonar.outputs.enabled == 'true'
+        env:
+          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
+        run: >
+          ./mvnw -B -ntp org.sonarsource.scanner.maven:sonar-maven-plugin:sonar
+          -Dsonar.host.url=https://sonarcloud.io
+          -Dsonar.organization=${{ vars.SONAR_ORGANIZATION || github.repository_owner }}
+          -Dsonar.projectKey=${{ vars.SONAR_PROJECT_KEY || format('{0}_{1}', github.repository_owner, github.event.repository.name) }}
+          -Dsonar.qualitygate.wait=true
+
       - name: Resumen de cobertura
         if: always()
         run: |
@@ -99,14 +128,14 @@ jobs:
           username: ${{ vars.DOCKERHUB_USERNAME }}
           password: ${{ secrets.DOCKERHUB_TOKEN }}
 
-      # El nombre de la imagen se normaliza a minúsculas (ghcr.io/galaxytraining/curs-000617-cicd).
+      # El nombre de la imagen se normaliza a minúsculas (ghcr.io/christophersanz/ai-cicd).
       - name: Etiquetas
         id: meta
         uses: docker/metadata-action@v6
         with:
           images: |
             ghcr.io/${{ github.repository }}
-            name=docker.io/${{ vars.DOCKERHUB_USERNAME }}/curs-000617-cicd,enable=${{ vars.DOCKERHUB_USERNAME != '' }}
+            name=docker.io/${{ vars.DOCKERHUB_USERNAME }}/${{ env.IMAGE_NAME }},enable=${{ vars.DOCKERHUB_USERNAME != '' }}
           tags: |
             type=ref,event=pr
             type=sha,format=short
diff --git a/.gitignore b/.gitignore
index babc3e1..7f0954e 100644
--- a/.gitignore
+++ b/.gitignore
@@ -8,3 +8,6 @@ target/
 *.pem
 *.key
 *.env
+
+# macOS
+.DS_Store
diff --git a/.mvn/wrapper/maven-wrapper.properties b/.mvn/wrapper/maven-wrapper.properties
new file mode 100644
index 0000000..c0bcafe
--- /dev/null
+++ b/.mvn/wrapper/maven-wrapper.properties
@@ -0,0 +1,3 @@
+wrapperVersion=3.3.4
+distributionType=only-script
+distributionUrl=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.11/apache-maven-3.9.11-bin.zip
diff --git a/CLAUDE.md b/CLAUDE.md
index 74e225d..b4ac7d2 100644
--- a/CLAUDE.md
+++ b/CLAUDE.md
@@ -35,7 +35,7 @@ Contexto base del repositorio. Este archivo es el punto de entrada: es breve y e
 | Documentación API | springdoc-openapi (OpenAPI 3) |
 | Observabilidad | Micrometer + OpenTelemetry, logs estructurados en JSON |
 | Pruebas | JUnit 5, AssertJ, Mockito, Testcontainers, spring-security-test; cobertura con JaCoCo (≥ 80 % de líneas en `domain` y `application`) |
-| CI/CD | GitHub Actions (`.github/workflows/ci-cd.yml`), imagen Docker en GHCR y Docker Hub (opcional), GitHub Flow (ADR-0009); `Jenkinsfile` solo para Jenkins local de práctica (ADR-0010) |
+| CI/CD | GitHub Actions (`.github/workflows/ci-cd.yml`), análisis estático en SonarCloud (ADR-0011), imagen Docker en GHCR y Docker Hub (opcional), GitHub Flow (ADR-0009); `Jenkinsfile` solo para Jenkins local de práctica (ADR-0010) |
 
 ## 3. Comandos habituales
 
diff --git a/README.md b/README.md
index ddd7e5e..6405a12 100644
--- a/README.md
+++ b/README.md
@@ -1,6 +1,6 @@
 # contract-api
 
-[![CI/CD](https://github.com/GalaxyTraining/CURS-000617-CICD/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/GalaxyTraining/CURS-000617-CICD/actions/workflows/ci-cd.yml)
+[![CI/CD](https://github.com/christophersanz/AI-CICD/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/christophersanz/AI-CICD/actions/workflows/ci-cd.yml)
 
 API REST para la gestión de contratos laborales. Java 21, Spring Boot 3, Clean Architecture + DDD (`domain`, `application`, `infrastructure`).
 
@@ -12,12 +12,28 @@ API REST para la gestión de contratos laborales. Java 21, Spring Boot 3, Clean
 | [docs/architecture.md](docs/architecture.md) | Capas, reglas de dependencia, convenciones y pruebas |
 | [docs/business-rules.md](docs/business-rules.md) | Reglas de negocio, casos de uso y errores |
 | [docs/technical.md](docs/technical.md) | Estado implementado: endpoints, seguridad, configuración, base de datos y CI/CD |
-| [docs/adr/](docs/adr/) | Decisiones de arquitectura (CI/CD: ADR-0009) |
+| [docs/adr/](docs/adr/) | Decisiones de arquitectura (CI/CD: ADR-0009; SonarCloud: ADR-0011) |
 | [docs/guides/arquitectura.md](docs/guides/arquitectura.md) | Guía visual de la arquitectura: contexto, módulos, puertos y adaptadores, dominio, flujos de petición, tokens y errores |
 | [docs/guides/ci-cd.md](docs/guides/ci-cd.md) | Guía de CI/CD: flujo de PR, pipeline, versiones, imágenes en GHCR y Docker Hub, ejecución y Jenkins local |
 | [docs/design/003-security-local-testing.md](docs/design/003-security-local-testing.md) | Probar la API en local con PowerShell y Postman |
 
-## Ejecutar en local
+## Levantar todo con Docker (recomendado)
+
+```bash
+cp .env.example .env                 # variables locales (no se versiona)
+docker compose up -d --build         # PostgreSQL + pgAdmin + API
+docker logs -f contract-api          # espera "Started ContractApiApplication"
+```
+
+| Servicio | URL | Credenciales (de `.env`) |
+|---|---|---|
+| API / Swagger UI | http://localhost:8081/swagger-ui.html | `admin` / `SECURITY_BOOTSTRAP_ADMIN_PASSWORD` |
+| pgAdmin | http://localhost:5050 | `PGADMIN_EMAIL` / `PGADMIN_PASSWORD` |
+| PostgreSQL | `localhost:5432`, base `contracts` | `contract` / `DB_PASSWORD` |
+
+En Swagger: `POST /api/v1/auth/login` → copia `accessToken` → **Authorize**. Para detenerlo: `docker compose down` (añade `-v` para borrar los datos).
+
+## Ejecutar en local (sin contenedor para la API)
 
 ```bash
 docker compose up -d                       # PostgreSQL (5432) y pgAdmin (http://localhost:5050)
@@ -37,8 +53,8 @@ docker run -p 8081:8081 \
   contract-api
 ```
 
-La imagen usa el perfil `prod`: no tiene valores por defecto ni clave efímera, y Swagger está desactivado. Las imágenes publicadas están en `ghcr.io/galaxytraining/curs-000617-cicd` (requiere `docker login ghcr.io` con un token `read:packages`) y, si el repositorio tiene configurado Docker Hub, en `docker.io/<usuario>/curs-000617-cicd` (pública).
+La imagen usa el perfil `prod`: no tiene valores por defecto ni clave efímera, y Swagger está desactivado. Las imágenes publicadas están en `ghcr.io/christophersanz/ai-cicd` (requiere `docker login ghcr.io` con un token `read:packages`) y, si el repositorio tiene configurado Docker Hub, en `docker.io/<usuario>/ai-cicd` (pública).
 
 ## Flujo de trabajo
 
-GitHub Flow ([ADR-0009](docs/adr/0009-ci-cd-github-actions.md)): rama `feature/*` desde `main` → Pull Request → CI en verde → merge. El pipeline compila, ejecuta las pruebas (incluidas las de Testcontainers), exige una cobertura ≥ 80 % en `domain` y `application` y construye la imagen; al integrar en `main`, la publica en GHCR y, si está configurado, en Docker Hub.
+GitHub Flow ([ADR-0009](docs/adr/0009-ci-cd-github-actions.md)): rama `feature/*` desde `main` → Pull Request → CI en verde → merge. El pipeline compila, ejecuta las pruebas (incluidas las de Testcontainers), exige una cobertura ≥ 80 % en `domain` y `application`, analiza el código en SonarCloud (si está configurado) y construye la imagen; al integrar en `main`, la publica en GHCR y, si está configurado, en Docker Hub.
diff --git a/docker-compose.yml b/docker-compose.yml
index 9dbace6..94b9aef 100644
--- a/docker-compose.yml
+++ b/docker-compose.yml
@@ -35,6 +35,28 @@ services:
       postgres:
         condition: service_healthy
 
+  # API construida con el Dockerfile. Perfil local: Swagger activo y, si no se pasan claves JWT,
+  # un par RSA efímero (los tokens dejan de valer al reiniciar). Variables en .env (ver .env.example).
+  api:
+    build: .
+    image: ai-cicd:local
+    container_name: contract-api
+    environment:
+      SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE:-local}
+      DB_URL: jdbc:postgresql://postgres:5432/contracts
+      DB_USERNAME: contract
+      DB_PASSWORD: ${DB_PASSWORD:-contract}
+      SECURITY_JWT_PUBLIC_KEY: ${SECURITY_JWT_PUBLIC_KEY:-}
+      SECURITY_JWT_PRIVATE_KEY: ${SECURITY_JWT_PRIVATE_KEY:-}
+      SECURITY_BOOTSTRAP_ADMIN_USERNAME: ${SECURITY_BOOTSTRAP_ADMIN_USERNAME:-admin}
+      SECURITY_BOOTSTRAP_ADMIN_PASSWORD: ${SECURITY_BOOTSTRAP_ADMIN_PASSWORD:?Define SECURITY_BOOTSTRAP_ADMIN_PASSWORD en .env (12-64 caracteres)}
+    ports:
+      - "8081:8081"
+    depends_on:
+      postgres:
+        condition: service_healthy
+    restart: unless-stopped
+
 volumes:
   postgres-data:
     name: api-contratos-ca-v3_postgres-data   # keeps your existing data
diff --git a/docker/jenkins/.env.example b/docker/jenkins/.env.example
new file mode 100644
index 0000000..b3c176e
--- /dev/null
+++ b/docker/jenkins/.env.example
@@ -0,0 +1,12 @@
+# Copia este archivo como docker/jenkins/.env (no se versiona) y ajusta los valores.
+# El arranque usa --env-file docker/jenkins/.env y falla si el archivo no existe.
+#   cp docker/jenkins/.env.example docker/jenkins/.env
+#   docker compose --env-file docker/jenkins/.env -f docker/jenkins/docker-compose.yml up -d --build
+
+# Administrador de Jenkins: http://localhost:8080 (solo uso local).
+JENKINS_ADMIN_USER=admin
+JENKINS_ADMIN_PASSWORD=admin
+
+# Repositorio y rama que construye el job contract-api (cámbialos si trabajas sobre un fork).
+REPO_URL=https://github.com/christophersanz/AI-CICD.git
+REPO_BRANCH=main
diff --git a/docker/jenkins/README.md b/docker/jenkins/README.md
index 6618205..80abeab 100644
--- a/docker/jenkins/README.md
+++ b/docker/jenkins/README.md
@@ -13,6 +13,7 @@ Jenkins en Docker, ya configurado para ejecutar el `Jenkinsfile` de este reposit
 Desde la raíz del repositorio:
 
 ```bash
+cp -n docker/jenkins/.env.example docker/jenkins/.env   # solo la primera vez (no se versiona)
 docker compose --env-file docker/jenkins/.env -f docker/jenkins/docker-compose.yml up -d --build
 ```
 
@@ -26,19 +27,19 @@ No hay asistente inicial: los plugins, el usuario y el job se crean al arrancar
 
 ## Configuración
 
-Variables de entorno opcionales (en la terminal o en un archivo `.env` junto a este `docker-compose.yml`):
+Variables en `docker/jenkins/.env`, creado a partir de la plantilla `docker/jenkins/.env.example` (también se pueden definir en la terminal). Es un archivo distinto del `.env` de la raíz, que usa el `docker-compose.yml` de la API:
 
 | Variable | Por defecto | Uso |
 |---|---|---|
 | `JENKINS_ADMIN_USER` | `admin` | Usuario administrador |
 | `JENKINS_ADMIN_PASSWORD` | `admin` | Contraseña del administrador |
-| `REPO_URL` | repositorio del curso | Cambiarla si trabajas sobre tu propio fork |
+| `REPO_URL` | `https://github.com/christophersanz/AI-CICD.git` | Cambiarla si trabajas sobre tu propio fork |
 | `REPO_BRANCH` | `main` | Rama que construye el job |
 
 Ejemplo en PowerShell para construir tu fork:
 
 ```powershell
-$env:REPO_URL = "https://github.com/<tu-usuario>/CURS-000617-CICD.git"
+$env:REPO_URL = "https://github.com/<tu-usuario>/AI-CICD.git"
 docker compose --env-file docker/jenkins/.env -f docker/jenkins/docker-compose.yml up -d
 ```
 
diff --git a/docker/jenkins/docker-compose.yml b/docker/jenkins/docker-compose.yml
index 90557c4..8dda632 100644
--- a/docker/jenkins/docker-compose.yml
+++ b/docker/jenkins/docker-compose.yml
@@ -13,7 +13,7 @@ services:
     environment:
       JENKINS_ADMIN_USER: ${JENKINS_ADMIN_USER:-admin}
       JENKINS_ADMIN_PASSWORD: ${JENKINS_ADMIN_PASSWORD:-admin}
-      REPO_URL: ${REPO_URL:-https://github.com/GalaxyTraining/CURS-000617-CICD.git}
+      REPO_URL: ${REPO_URL:-https://github.com/christophersanz/AI-CICD.git}
       REPO_BRANCH: ${REPO_BRANCH:-main}
       TESTCONTAINERS_HOST_OVERRIDE: host.docker.internal
     extra_hosts:
diff --git a/docs/adr/0009-ci-cd-github-actions.md b/docs/adr/0009-ci-cd-github-actions.md
index 5247307..ebc04eb 100644
--- a/docs/adr/0009-ci-cd-github-actions.md
+++ b/docs/adr/0009-ci-cd-github-actions.md
@@ -10,11 +10,11 @@ La API vivía como una carpeta más del repositorio del curso, junto a otras ver
 
 ## Decisión
 
-- **Repositorio propio** (`GalaxyTraining/CURS-000617-CICD`), extraído con `git subtree split` para conservar el historial de la v3.
+- **Repositorio propio** (originalmente `GalaxyTraining/CURS-000617-CICD`, extraído con `git subtree split`; esta copia vive en `christophersanz/AI-CICD`).
 - **GitHub Flow:** `main` siempre desplegable; el trabajo se hace en ramas `feature/*`, `fix/*` o `docs/*` y entra por Pull Request con el CI en verde. Protección de `main` con el ruleset `main-protegida` (`.github/ruleset/main-protegida.json`): PR obligatorio (sin aprobaciones mínimas, solo *squash*), rama al día con `main`, checks `Build, pruebas y cobertura` e `Imagen Docker` requeridos, sin force push ni borrado.
 - **GitHub Actions** (`.github/workflows/ci-cd.yml`):
   - `build`: `./mvnw clean verify` con JDK 21, incluidas las pruebas con Testcontainers y la cobertura JaCoCo; publica los informes y un resumen de cobertura.
-  - `image`: construye la imagen con el `Dockerfile` multi-stage; en un PR solo la construye, en `main` la publica en `ghcr.io` (`latest` + sha corto) y en un tag `vX.Y.Z` con las etiquetas de versión. Las mismas etiquetas se publican también en Docker Hub (`docker.io/<usuario>/curs-000617-cicd`) si el repositorio define la variable `DOCKERHUB_USERNAME` y el secreto `DOCKERHUB_TOKEN` (token de acceso con permiso *Read & Write*); sin ellos, por ejemplo en un fork, solo se publica en GHCR.
+  - `image`: construye la imagen con el `Dockerfile` multi-stage; en un PR solo la construye, en `main` la publica en `ghcr.io` (`latest` + sha corto) y en un tag `vX.Y.Z` con las etiquetas de versión. Las mismas etiquetas se publican también en Docker Hub (`docker.io/<usuario>/ai-cicd`) si el repositorio define la variable `DOCKERHUB_USERNAME` y el secreto `DOCKERHUB_TOKEN` (token de acceso con permiso *Read & Write*); sin ellos, por ejemplo en un fork, solo se publica en GHCR.
 - **Cobertura:** JaCoCo con un umbral del 80 % de líneas en `domain` y `application` (`architecture.md` §9); el build falla por debajo. `infrastructure` solo genera el informe.
 - **Imagen:** JRE 21, usuario sin privilegios, capas de Spring Boot y perfil `prod` (sin valores por defecto, sin clave efímera y sin Swagger).
 
@@ -31,4 +31,4 @@ La API vivía como una carpeta más del repositorio del curso, junto a otras ver
 - Positivas: ningún cambio llega a `main` sin compilar, sin pruebas en verde ni por debajo del umbral de cobertura; cada integración en `main` produce una imagen versionada.
 - Negativas: las pruebas con Testcontainers alargan el pipeline; el ruleset se versiona en `.github/ruleset/` pero se importa a mano en GitHub (Settings → Rules → Rulesets) y cualquier cambio hay que replicarlo allí; se aplica porque el repositorio es público (en uno privado requiere plan Pro o Team).
 - Docker Hub limita las descargas anónimas por IP: un aula que comparte IP puede alcanzar el límite; con `docker login` el límite es mayor.
-- Seguimiento: despliegue (CD a un entorno), análisis de seguridad (CodeQL/Dependabot) y pruebas de API con Postman CLI quedan fuera de esta decisión.
+- Seguimiento: el análisis estático con SonarCloud se incorporó en ADR-0011. Despliegue (CD a un entorno), análisis de seguridad (CodeQL/Dependabot) y pruebas de API con Postman CLI quedan fuera de esta decisión.
diff --git a/docs/adr/0011-analisis-estatico-sonarcloud.md b/docs/adr/0011-analisis-estatico-sonarcloud.md
new file mode 100644
index 0000000..1f9b553
--- /dev/null
+++ b/docs/adr/0011-analisis-estatico-sonarcloud.md
@@ -0,0 +1,28 @@
+# ADR-0011: Análisis estático con SonarCloud en el pipeline
+
+- **Estado:** Aceptado
+- **Fecha:** 2026-10-24
+
+## Contexto
+
+Los requisitos del proyecto final piden "análisis estático sin issues críticos" con un **reporte de Sonar** como evidencia, y medir los issues antes y después de los refactors asistidos por IA. El pipeline de ADR-0009 compila, prueba y mide cobertura, pero no analiza la calidad del código.
+
+## Decisión
+
+- Añadir al job `Build, pruebas y cobertura` un paso que ejecuta `sonar-maven-plugin` contra **SonarCloud** después de `./mvnw clean verify`, reutilizando las clases compiladas y los informes `jacoco.xml` de cada módulo.
+- El paso se ejecuta solo si existe el secreto `SONAR_TOKEN`; en un fork o sin configurar se omite y el pipeline no falla.
+- Se usa `-Dsonar.qualitygate.wait=true`: si no se cumple el Quality Gate, el job falla y, por el ruleset `main-protegida`, el PR no puede integrarse. No hace falta un check requerido nuevo.
+- Organización y clave del proyecto se toman de las variables `SONAR_ORGANIZATION` / `SONAR_PROJECT_KEY` o, por defecto, del dueño y nombre del repositorio.
+- El plugin se invoca por coordenadas en el workflow: no se añade nada al `pom.xml`.
+
+## Alternativas consideradas
+
+1. **SonarQube autoalojado (Docker).** Pro: sin cuenta externa; útil también desde Jenkins. Contra: un servidor más que mantener y que no está disponible desde GitHub Actions.
+2. **Solo SonarLint en el IDE.** Pro: sin configuración. Contra: no deja evidencia ni bloquea integraciones.
+3. **Análisis automático de SonarCloud (sin CI).** Pro: cero configuración. Contra: no importa la cobertura de JaCoCo ni bloquea el PR.
+
+## Consecuencias
+
+- Positivas: cada PR queda analizado; el historial de SonarCloud da los datos de issues y cobertura "antes y después" para la presentación.
+- Negativas: depende de un servicio externo y de un token; el análisis añade aproximadamente un minuto al job; hay que desactivar el análisis automático en SonarCloud.
+- Seguimiento: si el Quality Gate por defecto resulta demasiado estricto al inicio, ajustarlo en SonarCloud y documentarlo aquí.
diff --git a/docs/guides/ci-cd.md b/docs/guides/ci-cd.md
index 6ab1ed2..c1528f2 100644
--- a/docs/guides/ci-cd.md
+++ b/docs/guides/ci-cd.md
@@ -109,7 +109,7 @@ Si cambias el JSON, hay que volver a importarlo a mano en GitHub: el archivo es
 
 ## 5. El pipeline de GitHub Actions
 
-Definido en [`.github/workflows/ci-cd.yml`](../../.github/workflows/ci-cd.yml). Tiene dos jobs; el segundo solo empieza si el primero termina bien.
+Definido en [`.github/workflows/ci-cd.yml`](../../.github/workflows/ci-cd.yml). Tiene dos jobs; el segundo solo empieza si el primero termina bien. El análisis de SonarCloud es un paso del primero ([ADR-0011](../adr/0011-analisis-estatico-sonarcloud.md)).
 
 ### 5.1 Job `Build, pruebas y cobertura`
 
@@ -119,6 +119,7 @@ Definido en [`.github/workflows/ci-cd.yml`](../../.github/workflows/ci-cd.yml).
 - Al terminar, aunque falle:
   - escribe una tabla de cobertura por módulo en el **resumen** de la ejecución (*Actions → ejecución → Summary*);
   - publica el artefacto **reports** (informes de Surefire y JaCoCo, 14 días). Descárgalo desde la ejecución y abre `*/target/site/jacoco/index.html`.
+- Si el repositorio tiene el secreto `SONAR_TOKEN`, ejecuta el **análisis de SonarCloud** con la cobertura de JaCoCo y espera el **Quality Gate**: si no se cumple, el job falla (sección 5.4). Sin el secreto, el paso se omite.
 
 ### 5.2 Job `Imagen Docker`
 
@@ -130,6 +131,15 @@ Definido en [`.github/workflows/ci-cd.yml`](../../.github/workflows/ci-cd.yml).
 
 *Actions → CI/CD → Run workflow* y elige la rama. Útil, por ejemplo, después de configurar un secreto sin tener que hacer un commit.
 
+### 5.4 Configurar SonarCloud (una sola vez)
+
+1. Entra a <https://sonarcloud.io> con tu cuenta de GitHub → **+ → Analyze new project** → elige el repositorio (gratis para repositorios públicos).
+2. En el proyecto: *Administration → Analysis Method* → desactiva **Automatic Analysis** (si está activo, el análisis desde el CI falla).
+3. *My Account → Security* → genera un token.
+4. En GitHub: *Settings → Secrets and variables → Actions → Secrets* → `SONAR_TOKEN` = el token.
+5. Solo si tu organización o clave de proyecto en SonarCloud son distintas de las de GitHub, crea en la pestaña **Variables** `SONAR_ORGANIZATION` y/o `SONAR_PROJECT_KEY`. Por defecto se usan `<dueño>` y `<dueño>_<repositorio>` (p. ej. `christophersanz` y `christophersanz_AI-CICD`), que son los que crea SonarCloud al importar.
+6. *Run workflow* sobre `main` y revisa el resultado en el panel del proyecto en SonarCloud. Ese panel es la evidencia de **"Análisis estático sin issues críticos"**.
+
 ## 6. Publicar una versión
 
 Las versiones siguen [SemVer](https://semver.org/lang/es/) y se crean con un tag sobre `main`:
@@ -151,16 +161,16 @@ La misma imagen y las mismas etiquetas se publican en dos registros:
 
 | | GHCR | Docker Hub |
 |---|---|---|
-| Imagen | `ghcr.io/galaxytraining/curs-000617-cicd` | `docker.io/anovoa1975/curs-000617-cicd` |
-| Visibilidad | Privada (la organización no permite paquetes públicos) | Pública |
+| Imagen | `ghcr.io/christophersanz/ai-cicd` | `docker.io/<usuario-dockerhub>/ai-cicd` |
+| Visibilidad | Privada por defecto (se puede hacer pública en *Packages → Package settings*) | Pública |
 | Para descargar | `docker login ghcr.io` con un token | Sin login |
 | Credenciales en el pipeline | `GITHUB_TOKEN`, automático | Variable `DOCKERHUB_USERNAME` y secreto `DOCKERHUB_TOKEN` |
 
 ### 7.1 Descargar desde Docker Hub (recomendado)
 
 ```bash
-docker pull anovoa1975/curs-000617-cicd:latest
-docker pull anovoa1975/curs-000617-cicd:0.1.0
+docker pull <usuario-dockerhub>/ai-cicd:latest
+docker pull <usuario-dockerhub>/ai-cicd:0.1.0
 ```
 
 Docker Hub limita las descargas anónimas por IP. Si un aula entera comparte IP y alcanza el límite, inicia sesión con `docker login` (cuenta gratuita) para tener un límite mayor.
@@ -168,7 +178,7 @@ Docker Hub limita las descargas anónimas por IP. Si un aula entera comparte IP
 ### 7.2 Descargar desde GHCR
 
 1. Crea un token **classic** (empieza por `ghp_`) con solo el permiso `read:packages`: <https://github.com/settings/tokens/new?scopes=read:packages&description=ghcr-read>. Los tokens *fine-grained* no sirven para GHCR.
-2. Si aparece **Configure SSO** junto al token, autorízalo para `GalaxyTraining`.
+2. (Solo si el repositorio pertenece a una organización con SSO) autoriza el token con **Configure SSO**.
 3. Inicia sesión **sin escribir el token en el comando** (PowerShell):
 
 ```powershell
@@ -177,13 +187,13 @@ $t | docker login ghcr.io -u <tu-usuario-github> --password-stdin
 Remove-Variable t
 ```
 
-4. `docker pull ghcr.io/galaxytraining/curs-000617-cicd:latest`
+4. `docker pull ghcr.io/christophersanz/ai-cicd:latest`
 
 ### 7.3 Configurar Docker Hub en tu propio repositorio o fork
 
 Sin esta configuración el pipeline publica solo en GHCR y no falla.
 
-1. En Docker Hub: crea el repositorio `curs-000617-cicd` como **Public** y un token en *Account settings → Personal access tokens* con permiso **Read & Write**.
+1. En Docker Hub: crea el repositorio `ai-cicd` como **Public** y un token en *Account settings → Personal access tokens* con permiso **Read & Write**.
 2. En GitHub: *Settings → Secrets and variables → Actions*:
    - pestaña **Variables** → *New repository variable*: `DOCKERHUB_USERNAME` = tu usuario de Docker Hub;
    - pestaña **Secrets** → *New repository secret*: `DOCKERHUB_TOKEN` = el token.
@@ -239,7 +249,7 @@ Git Bash:
 ```bash
 docker run -d --name contract-api-prod -p 8081:8081 \
   --env-file ~/contract-api-prod/prod.env \
-  anovoa1975/curs-000617-cicd:latest
+  <usuario-dockerhub>/ai-cicd:latest
 
 docker logs -f contract-api-prod      # espera "Started ContractApiApplication"; sal con Ctrl+C
 ```
@@ -249,7 +259,7 @@ PowerShell (la continuación de línea es el acento grave y `~` no se expande pa
 ```powershell
 docker run -d --name contract-api-prod -p 8081:8081 `
   --env-file "$HOME\contract-api-prod\prod.env" `
-  anovoa1975/curs-000617-cicd:latest
+  <usuario-dockerhub>/ai-cicd:latest
 ```
 
 Si el puerto 8081 está ocupado (por ejemplo, por la API en `spring-boot:run`), usa `-p 8082:8081` y llama al 8082.
@@ -294,7 +304,7 @@ docker rm -f contract-api-prod
 # ~/contract-api-prod/docker-compose.yml
 services:
   api:
-    image: anovoa1975/curs-000617-cicd:latest
+    image: <usuario-dockerhub>/ai-cicd:latest
     ports: ["8081:8081"]
     environment:
       SPRING_CONFIG_IMPORT: "optional:configtree:/run/secrets/"
@@ -335,6 +345,12 @@ Jenkins es solo de práctica: el check que protege `main` es el de GitHub Action
 
 | Síntoma | Causa | Solución |
 |---|---|---|
+| El workflow no se ejecuta nunca | La rama por defecto se llama `master` y el workflow escucha `main` | Renombrar la rama a `main` (*Settings → General → Default branch*) |
+| `./mvnw: Permission denied` | `mvnw` versionado sin permiso de ejecución | `git update-index --chmod=+x mvnw` y commit |
+| `cannot open ./.mvn/wrapper/maven-wrapper.properties` | Falta la carpeta `.mvn/` en el repositorio | Versionar `.mvn/wrapper/maven-wrapper.properties` |
+| `Análisis SonarCloud` aparece *skipped* | Falta el secreto `SONAR_TOKEN` | Sección 5.4 |
+| SonarCloud: *You are running CI analysis while Automatic Analysis is enabled* | Análisis automático activo | Desactivarlo (sección 5.4, paso 2) |
+| El job falla en *Quality Gate* | El código nuevo no cumple el Quality Gate (issues, cobertura) | Revisar el panel de SonarCloud y corregir |
 | El ruleset aparece con ícono amarillo | Repositorio privado con plan Free | Hacer público el repositorio o usar un plan Pro/Team (sección 4) |
 | *This branch is out-of-date with the base branch* | `main` avanzó después de abrir el PR | **Update branch** y esperar el CI |
 | *Squash and merge* deshabilitado | Checks pendientes o en rojo, o rama desactualizada | Revisar el job fallido en *Checks*; actualizar la rama |
diff --git a/docs/technical.md b/docs/technical.md
index ce2a743..f0e5f7d 100644
--- a/docs/technical.md
+++ b/docs/technical.md
@@ -296,6 +296,8 @@ docker compose up -d                       # PostgreSQL (5432) y pgAdmin (http:/
 ./mvnw spring-boot:run -pl infrastructure  # http://localhost:8081, perfil local
 ```
 
+`docker compose up -d --build` levanta además la API (servicio `api`, perfil `local` por defecto) con las variables de `.env`; la plantilla con todas las variables es `.env.example`.
+
 pgAdmin arranca sin pedir contraseña maestra y con el servidor ya registrado (`docker/pgadmin/servers.json`). Las credenciales por defecto de PostgreSQL son `contract` / `contract`; se pueden cambiar con `DB_PASSWORD`.
 
 ## 9. Imagen Docker y CI/CD
@@ -313,11 +315,12 @@ El `Dockerfile` tiene dos etapas:
 
 | Evento | `build` | `image` |
 |---|---|---|
-| Pull Request a `main` | `clean verify`, resumen de cobertura e informes | Construye la imagen sin publicarla |
+| Pull Request a `main` | `clean verify`, análisis SonarCloud, resumen de cobertura e informes | Construye la imagen sin publicarla |
 | Push a `main` | Igual | Publica `latest` y `sha-<corto>` |
 | Tag `vX.Y.Z` | Igual | Publica `X.Y.Z` y `X.Y` |
 
-- Registros: `ghcr.io/galaxytraining/curs-000617-cicd` siempre; Docker Hub solo si el repositorio define la variable `DOCKERHUB_USERNAME` y el secreto `DOCKERHUB_TOKEN`.
+- SonarCloud ([ADR-0011](adr/0011-analisis-estatico-sonarcloud.md)): se ejecuta solo si existe el secreto `SONAR_TOKEN`; el job falla si no se cumple el Quality Gate.
+- Registros: `ghcr.io/christophersanz/ai-cicd` siempre; Docker Hub solo si el repositorio define la variable `DOCKERHUB_USERNAME` y el secreto `DOCKERHUB_TOKEN`.
 - Los informes de Surefire y JaCoCo se guardan como artefacto `reports` durante 14 días.
 - En los PR, un push nuevo cancela la ejecución anterior.
 
```
