# Guía de CI/CD de contract-api

Guía práctica para trabajar con el flujo de integración y entrega continua del proyecto: cómo se integra un cambio en `master`, qué valida el pipeline, dónde se publica la imagen Docker y cómo ejecutarla. Las decisiones y sus motivos están en [ADR-0009](../adr/0009-ci-cd-github-actions.md) (GitHub Actions) y [ADR-0010](../adr/0010-jenkins-local-de-practica.md) (Jenkins local).

## Índice

1. [Visión general](#1-visión-general)
2. [Requisitos](#2-requisitos)
3. [Flujo de trabajo en una sola rama](#3-flujo-de-trabajo-en-una-sola-rama)
4. [Protección de `master` (ruleset)](#4-protección-de-master-ruleset)
5. [El pipeline de GitHub Actions](#5-el-pipeline-de-github-actions)
6. [Publicar una versión](#6-publicar-una-versión)
7. [Registros de imágenes: GHCR y Docker Hub](#7-registros-de-imágenes-ghcr-y-docker-hub)
8. [Ejecutar la imagen publicada](#8-ejecutar-la-imagen-publicada)
9. [Manejo de secretos](#9-manejo-de-secretos)
10. [Jenkins local](#10-jenkins-local)
11. [Problemas frecuentes](#11-problemas-frecuentes)

## 1. Visión general

```mermaid
flowchart LR
    A[Commit en master<br/>en local] -->|push| E{CI en master<br/>build + pruebas<br/>+ cobertura + imagen}
    E -->|rojo| A
    E -->|verde| F[(GHCR<br/>latest, sha)]
    E -->|verde| G[(Docker Hub<br/>latest, sha)]
    A -.->|tag vX.Y.Z| H[CI del tag]
    H --> I[(GHCR y Docker Hub<br/>X.Y.Z, X.Y)]
```

| Evento | Build y pruebas | Imagen | Publicación |
|---|:-:|:-:|---|
| Push a `master` | ✅ | se construye | `latest` y sha corto |
| Tag `vX.Y.Z` | ✅ | se construye | `X.Y.Z` y `X.Y` |
| Pull Request a `master` (opcional) | ✅ | se construye | ninguna |
| Manual (*Run workflow*) | ✅ | se construye | según la rama elegida |

## 2. Requisitos

- Git y una cuenta de GitHub con acceso al repositorio.
- Docker Desktop (contenedores Linux) para ejecutar la imagen, las pruebas con Testcontainers y Jenkins.
- JDK 21 solo si compilas en local (`./mvnw` descarga Maven).
- Git Bash o PowerShell. Los comandos indican en cuál se ejecutan cuando la sintaxis difiere.

## 3. Flujo de trabajo en una sola rama

`master` es la única rama de larga vida: se trabaja en local sobre ella y se hace push directo. Como el CI se ejecuta **después** del push, verifica en local antes de subir.

### 3.1 Actualizar la copia local

```bash
git checkout master
git pull
```

### 3.2 Trabajar y verificar en local

```bash
./mvnw clean verify      # lo mismo que ejecuta el CI, incluido el umbral de cobertura
```

Los mensajes de commit van en español, en imperativo y explican el porqué:

```bash
git add <archivos>
git commit -m "Validar el formato del correo al registrar empleados"
```

### 3.3 Subir y revisar el CI

```bash
git pull --rebase        # si alguien más subió cambios (no se permite force push)
git push
```

Revisa la ejecución en *Actions → CI/CD*. Si queda en rojo, corrígelo con un **commit nuevo** en `master`: no se reescribe el historial ya subido.

Para un cambio grande o que quieras revisar antes, puedes abrir una rama corta y un Pull Request a `master`: el CI se ejecuta igual sobre el PR, pero no es obligatorio.

## 4. Protección de `master` (ruleset)

La regla `master-protegida` está versionada en [`.github/ruleset/master-protegida.json`](../../.github/ruleset/master-protegida.json) y se aplica a la rama por defecto:

| Regla | Efecto |
|---|---|
| Sin force push | No se puede reescribir el historial de `master` (`git push --force` se rechaza). |
| Sin borrado | `master` no se puede eliminar. |

El push directo está permitido: no se exige Pull Request ni checks previos.

**Configurarla en un repositorio nuevo o en tu fork:** *Settings → Rules → Rulesets → New ruleset → Import a ruleset* y selecciona el JSON.

> En un repositorio **privado** con plan Free, GitHub permite crear la regla pero **no la aplica** (aparece con un ícono amarillo). Hace falta que el repositorio sea público o un plan Pro/Team. Con la regla activa el ícono es verde.

Si cambias el JSON, hay que volver a importarlo a mano en GitHub: el archivo es la referencia, no se aplica solo.

## 5. El pipeline de GitHub Actions

Definido en [`.github/workflows/ci-cd.yml`](../../.github/workflows/ci-cd.yml). Tiene dos jobs; el segundo solo empieza si el primero termina bien. El análisis de SonarCloud es un paso del primero ([ADR-0011](../adr/0011-analisis-estatico-sonarcloud.md)).

### 5.1 Job `Build, pruebas y cobertura`

- Ejecuta `./mvnw -B -ntp clean verify` con JDK 21 (Temurin) y caché de Maven.
- Incluye las pruebas de integración con **Testcontainers** (PostgreSQL real; el runner `ubuntu-latest` ya tiene Docker).
- **JaCoCo** exige al menos un **80 % de líneas cubiertas** en `domain` y `application`; si no se alcanza, el build falla. `infrastructure` solo genera el informe.
- Al terminar, aunque falle:
  - escribe una tabla de cobertura por módulo en el **resumen** de la ejecución (*Actions → ejecución → Summary*);
  - publica el artefacto **reports** (informes de Surefire y JaCoCo, 14 días). Descárgalo desde la ejecución y abre `*/target/site/jacoco/index.html`.
- Si el repositorio tiene el secreto `SONAR_TOKEN`, ejecuta el **análisis de SonarCloud** con la cobertura de JaCoCo y espera el **Quality Gate**: si no se cumple, el job falla (sección 5.4). Sin el secreto, el paso se omite.

### 5.2 Job `Imagen Docker`

- Construye la imagen con el [`Dockerfile`](../../Dockerfile) multi-stage: compila con el Maven Wrapper (sin pruebas, ya se ejecutaron), extrae las capas de Spring Boot y genera una imagen con JRE 21, usuario sin privilegios y perfil `prod`.
- En un push a `master` y en tags la construye y la publica (sección 7). En un PR opcional solo la construye.
- Usa la caché de GitHub Actions para las capas, así que las ejecuciones siguientes son más rápidas.

### 5.3 Ejecutar el pipeline a mano

*Actions → CI/CD → Run workflow* y elige la rama. Útil, por ejemplo, después de configurar un secreto sin tener que hacer un commit.

### 5.4 Configurar SonarCloud (una sola vez)

1. Entra a <https://sonarcloud.io> con tu cuenta de GitHub → **+ → Analyze new project** → elige el repositorio (gratis para repositorios públicos).
2. En el proyecto: *Administration → Analysis Method* → desactiva **Automatic Analysis** (si está activo, el análisis desde el CI falla).
3. *My Account → Security* → genera un token.
4. En GitHub: *Settings → Secrets and variables → Actions → Secrets* → `SONAR_TOKEN` = el token.
5. Solo si tu organización o clave de proyecto en SonarCloud son distintas de las de GitHub, crea en la pestaña **Variables** `SONAR_ORGANIZATION` y/o `SONAR_PROJECT_KEY`. Por defecto se usan `<dueño>` y `<dueño>_<repositorio>` (p. ej. `christophersanz` y `christophersanz_AI-CICD`), que son los que crea SonarCloud al importar.
6. *Run workflow* sobre `master` y revisa el resultado en el panel del proyecto en SonarCloud. Ese panel es la evidencia de **"Análisis estático sin issues críticos"**.

## 6. Publicar una versión

Las versiones siguen [SemVer](https://semver.org/lang/es/) y se crean con un tag sobre `master`:

```bash
git checkout master
git pull
git tag v0.2.0
git push origin v0.2.0
```

El pipeline publica la imagen con las etiquetas `0.2.0` y `0.2`. `latest` no cambia con un tag: solo avanza con los push a `master`.

Opcional: *Releases → Draft a new release*, elige el tag, pulsa **Generate release notes** y **Publish release**.

## 7. Registros de imágenes: GHCR y Docker Hub

La misma imagen y las mismas etiquetas se publican en dos registros:

| | GHCR | Docker Hub |
|---|---|---|
| Imagen | `ghcr.io/christophersanz/ai-cicd` | `docker.io/<usuario-dockerhub>/ai-cicd` |
| Visibilidad | Privada por defecto (se puede hacer pública en *Packages → Package settings*) | Pública |
| Para descargar | `docker login ghcr.io` con un token | Sin login |
| Credenciales en el pipeline | `GITHUB_TOKEN`, automático | Variable `DOCKERHUB_USERNAME` y secreto `DOCKERHUB_TOKEN` |

### 7.1 Descargar desde Docker Hub (recomendado)

```bash
docker pull <usuario-dockerhub>/ai-cicd:latest
docker pull <usuario-dockerhub>/ai-cicd:0.1.0
```

Docker Hub limita las descargas anónimas por IP. Si un aula entera comparte IP y alcanza el límite, inicia sesión con `docker login` (cuenta gratuita) para tener un límite mayor.

### 7.2 Descargar desde GHCR

1. Crea un token **classic** (empieza por `ghp_`) con solo el permiso `read:packages`: <https://github.com/settings/tokens/new?scopes=read:packages&description=ghcr-read>. Los tokens *fine-grained* no sirven para GHCR.
2. (Solo si el repositorio pertenece a una organización con SSO) autoriza el token con **Configure SSO**.
3. Inicia sesión **sin escribir el token en el comando** (PowerShell):

```powershell
$t = Read-Host "Token"                     # pega el token cuando aparezca "Token:"
$t | docker login ghcr.io -u <tu-usuario-github> --password-stdin
Remove-Variable t
```

4. `docker pull ghcr.io/christophersanz/ai-cicd:latest`

### 7.3 Configurar Docker Hub en tu propio repositorio o fork

Sin esta configuración el pipeline publica solo en GHCR y no falla.

1. En Docker Hub: crea el repositorio `ai-cicd` como **Public** y un token en *Account settings → Personal access tokens* con permiso **Read & Write**.
2. En GitHub: *Settings → Secrets and variables → Actions*:
   - pestaña **Variables** → *New repository variable*: `DOCKERHUB_USERNAME` = tu usuario de Docker Hub;
   - pestaña **Secrets** → *New repository secret*: `DOCKERHUB_TOKEN` = el token.
3. Ejecuta el pipeline (*Run workflow* sobre `master`) y comprueba que el paso **Login en Docker Hub** está en verde, no *skipped*.

> El usuario va como **variable** y no como secreto a propósito: forma parte del nombre de la imagen, y los secretos se enmascaran como `***` en los logs.

## 8. Ejecutar la imagen publicada

La imagen arranca con el perfil `prod`: **no tiene valores por defecto ni genera claves**. Necesita la base de datos y un par de claves RSA para firmar los JWT; si falta algo, no arranca. Swagger está desactivado.

### 8.1 Base de datos

Usa el PostgreSQL del proyecto:

```bash
docker compose up -d        # desde la raíz del repositorio: PostgreSQL (5432) y pgAdmin (5050)
```

### 8.2 Generar las claves y el archivo de variables (Git Bash)

Créalos **fuera del repositorio**:

```bash
mkdir -p ~/contract-api-prod && cd ~/contract-api-prod

openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out private.pem
openssl pkey -in private.pem -pubout -out public.pem

# Cada PEM en una sola línea con \n literales (la API los admite así)
PUB=$(awk '{printf "%s\\n", $0}' public.pem)
PRIV=$(awk '{printf "%s\\n", $0}' private.pem)

cat > prod.env <<EOF
DB_URL=jdbc:postgresql://host.docker.internal:5432/contracts
DB_USERNAME=contract
DB_PASSWORD=contract
SECURITY_JWT_PUBLIC_KEY=$PUB
SECURITY_JWT_PRIVATE_KEY=$PRIV
SECURITY_BOOTSTRAP_ADMIN_USERNAME=admin
SECURITY_BOOTSTRAP_ADMIN_PASSWORD=<elige-una-contraseña-de-12-o-más>
EOF
```

- `host.docker.internal` permite al contenedor llegar al PostgreSQL publicado en tu máquina.
- La contraseña del administrador debe tener entre 12 y 64 caracteres (BR-USR-003).
- El administrador inicial solo se crea si **no existe ningún usuario `ADMIN`** en la base (BR-USR-007). Si ya probaste la API en local con esa base, usa las credenciales que creaste entonces.

### 8.3 Ejecutar el contenedor

Git Bash:

```bash
docker run -d --name contract-api-prod -p 8081:8081 \
  --env-file ~/contract-api-prod/prod.env \
  <usuario-dockerhub>/ai-cicd:latest

docker logs -f contract-api-prod      # espera "Started ContractApiApplication"; sal con Ctrl+C
```

PowerShell (la continuación de línea es el acento grave y `~` no se expande para programas externos):

```powershell
docker run -d --name contract-api-prod -p 8081:8081 `
  --env-file "$HOME\contract-api-prod\prod.env" `
  <usuario-dockerhub>/ai-cicd:latest
```

Si el puerto 8081 está ocupado (por ejemplo, por la API en `spring-boot:run`), usa `-p 8082:8081` y llama al 8082.

### 8.4 Probar la API (PowerShell)

```powershell
$pwd = Read-Host "Contraseña de admin" -AsSecureString
$plain = [Net.NetworkCredential]::new('', $pwd).Password
$body = @{ username = 'admin'; password = $plain } | ConvertTo-Json

$login = Invoke-RestMethod -Method Post -Uri http://localhost:8081/api/v1/auth/login `
  -ContentType 'application/json' -Body $body
$h = @{ Authorization = "Bearer $($login.accessToken)" }

Invoke-RestMethod http://localhost:8081/api/v1/auth/me -Headers $h            # usuario actual
Invoke-RestMethod http://localhost:8081/actuator/health -Headers $h           # status UP (requiere system:monitor)
Invoke-RestMethod "http://localhost:8081/api/v1/employees?page=0&size=5" -Headers $h
```

Un `401` con código `AUTH-002` en el login significa usuario o contraseña incorrectos.

### 8.5 Limpiar

```bash
docker rm -f contract-api-prod
```

## 9. Manejo de secretos

`private.pem` firma todos los tokens de acceso: quien la tenga puede emitir tokens válidos.

**Nunca:**

- versionar claves, `.env` ni tokens: `.gitignore` y `.dockerignore` ya ignoran `*.pem`, `*.key` y `*.env` como red de seguridad;
- escribir un token dentro de un comando o pegarlo en un chat, captura o ticket: queda en el historial de la terminal y en el destino. Si ocurre, **revócalo y crea otro**;
- reutilizar las contraseñas por defecto del entorno local (`contract`, `admin`) en un servidor.

**Variables de entorno frente a archivos.** Con `--env-file`, las claves quedan como variables de entorno y cualquiera con acceso a Docker las ve con `docker inspect <contenedor>`. Es más seguro montarlas como archivos con `secrets` de Docker Compose y que Spring Boot las lea con `configtree` (cada archivo de la carpeta se convierte en la propiedad de su nombre). No requiere cambios en el código:

```yaml
# ~/contract-api-prod/docker-compose.yml
services:
  api:
    image: <usuario-dockerhub>/ai-cicd:latest
    ports: ["8081:8081"]
    environment:
      SPRING_CONFIG_IMPORT: "optional:configtree:/run/secrets/"
      DB_URL: jdbc:postgresql://host.docker.internal:5432/contracts
      DB_USERNAME: contract
    secrets:
      - source: jwt_private
        target: security.jwt.private-key
      - source: jwt_public
        target: security.jwt.public-key
      - source: db_password
        target: spring.datasource.password
secrets:
  jwt_private: { file: ./private.pem }
  jwt_public:  { file: ./public.pem }
  db_password: { file: ./db_password.txt }
```

`db_password.txt` contiene solo la contraseña, sin salto de línea final (PowerShell: `Set-Content -NoNewline -Encoding ascii db_password.txt contract`). Los PEM van tal cual, con sus saltos de línea. Se levanta con `docker compose up -d` desde esa carpeta.

**En un servidor real** las claves viven en un gestor de secretos (Kubernetes Secrets, AWS Secrets Manager, Azure Key Vault, *Environment secrets* de GitHub) y la plataforma las inyecta al desplegar; nunca en la imagen ni junto al código.

**Rotación:** genera un par nuevo, actualiza el secreto, usa un `SECURITY_JWT_KEY_ID` nuevo (p. ej. `contract-api-2`) y reinicia. Los access tokens emitidos con la clave anterior dejan de valer (duran como máximo 15 minutos); los refresh tokens no se ven afectados porque son opacos y están en la base de datos.

## 10. Jenkins local

Para practicar con Jenkins hay un entorno listo en [`docker/jenkins/`](../../docker/jenkins/README.md) que ejecuta el [`Jenkinsfile`](../../Jenkinsfile) del repositorio: mismas etapas que el CI (`./mvnw clean verify` e imagen Docker), sin publicar.

```bash
docker compose --env-file docker/jenkins/.env -f docker/jenkins/docker-compose.yml up -d --build
```

Abre <http://localhost:8080>, entra con `admin` / `admin` y ejecuta el job **contract-api** con **Build Now**. La primera ejecución tarda unos minutos porque descarga dependencias e imágenes. Los detalles (variables, fork, limpieza) están en su [README](../../docker/jenkins/README.md).

Jenkins es solo de práctica: el CI oficial que valida cada push a `master` es GitHub Actions ([ADR-0010](../adr/0010-jenkins-local-de-practica.md)).

## 11. Problemas frecuentes

| Síntoma | Causa | Solución |
|---|---|---|
| El workflow no se ejecuta nunca | El workflow escucha `master` y el push se hizo a otra rama, o la rama por defecto no es `master` | Trabajar sobre `master` y dejarla como rama por defecto (*Settings → General → Default branch*) |
| `./mvnw: Permission denied` | `mvnw` versionado sin permiso de ejecución | `git update-index --chmod=+x mvnw` y commit |
| `cannot open ./.mvn/wrapper/maven-wrapper.properties` | Falta la carpeta `.mvn/` en el repositorio | Versionar `.mvn/wrapper/maven-wrapper.properties` |
| `Análisis SonarCloud` aparece *skipped* | Falta el secreto `SONAR_TOKEN` | Sección 5.4 |
| SonarCloud: *You are running CI analysis while Automatic Analysis is enabled* | Análisis automático activo | Desactivarlo (sección 5.4, paso 2) |
| El job falla en *Quality Gate* | El código nuevo no cumple el Quality Gate (issues, cobertura) | Revisar el panel de SonarCloud y corregir |
| El ruleset aparece con ícono amarillo | Repositorio privado con plan Free | Hacer público el repositorio o usar un plan Pro/Team (sección 4) |
| `git push` rechazado: *Cannot force-push to this branch* | El ruleset `master-protegida` impide reescribir el historial | No usar `--force`: `git pull --rebase` y luego `git push`; corregir errores con un commit nuevo |
| `git push` rechazado: *Updates were rejected because the remote contains work…* | `master` en GitHub tiene commits que no tienes en local | `git pull --rebase` y luego `git push` |
| El build falla por cobertura | `domain` o `application` por debajo del 80 % de líneas | Añadir pruebas; ver el informe en el artefacto **reports** |
| `Login en Docker Hub` aparece *skipped* | El workflow no ve la variable `DOCKERHUB_USERNAME` (se creó como secreto o en un *Environment*) | Crearla en la pestaña **Variables** del repositorio y ejecutar *Run workflow* |
| `Login en Docker Hub` falla con `unauthorized` | Token sin permiso *Write*, vencido o de otra cuenta | Generar un token **Read & Write** de la cuenta correcta y actualizar `DOCKERHUB_TOKEN` |
| `denied` al hacer `docker login ghcr.io` | Token vacío (no se ve al pegar), *fine-grained*, sin `read:packages` o sin autorización SSO | Usar `--password-stdin` (sección 7.2) y revisar el token |
| *Public: Setting is disabled by organization administrators* | La organización no permite paquetes públicos en GHCR | Usar Docker Hub o `docker login ghcr.io` |
| El contenedor termina al arrancar con *JWT keys are not configured* | Faltan `SECURITY_JWT_PUBLIC_KEY` / `SECURITY_JWT_PRIVATE_KEY` | Revisar `prod.env` (sección 8.2) |
| El contenedor no conecta a la base de datos | PostgreSQL no está levantado o `DB_URL` usa `localhost` | `docker compose up -d` y usar `host.docker.internal` |
| *An ADMIN user already exists; initial administrator not created.* | La base ya tenía un administrador | Es lo esperado: usa sus credenciales |
| `port is already allocated` | Otro proceso usa el puerto (8080 Jenkins, 8081 la API) | Detenerlo o publicar en otro puerto (`-p 8082:8081`) |
| `docker build` falla en Jenkins con `--mount` | Falta BuildKit (`buildx`) | La imagen de `docker/jenkins` ya lo incluye; reconstruirla con `--build` |
