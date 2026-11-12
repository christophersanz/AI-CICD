# Jenkins local de práctica

Jenkins en Docker, ya configurado para ejecutar el `Jenkinsfile` de este repositorio: compila, ejecuta las pruebas unitarias y las de integración (Testcontainers), verifica la cobertura, pasa el Quality Gate de un SonarQube local, construye la imagen Docker, la prueba en un entorno efímero y la despliega en tu máquina (`http://localhost:8090`), cada paso en su propia etapa del *Stage View* (ADR-0014). Es solo para practicar; el CI oficial es GitHub Actions (ADR-0009 y ADR-0010).

## Requisitos

- Docker Desktop en ejecución (contenedores Linux), con al menos 6 GB de memoria asignada (SonarQube usa ~2 GB).
- Puertos `8080` (Jenkins), `9000` (SonarQube) y `8090` (API desplegada, configurable con `DEPLOY_PORT`) libres.
- Conexión a internet la primera vez (descarga plugins, dependencias de Maven e imágenes).

## Levantar Jenkins

Desde la raíz del repositorio:

```bash
cp -n docker/jenkins/.env.example docker/jenkins/.env   # solo la primera vez (no se versiona)
docker compose --env-file docker/jenkins/.env -f docker/jenkins/docker-compose.yml up -d --build
```

La primera vez tarda unos minutos (construye la imagen e instala los plugins). Cuando `docker logs contract-api-jenkins` muestre `Jenkins is fully up and running`:

1. Abre http://localhost:8080 e inicia sesión con `admin` / la contraseña de `JENKINS_ADMIN_PASSWORD` en `.env`.
2. Entra al job **contract-api** y pulsa **Build Now** (o **Construir ahora**) una vez.
3. Revisa el avance en **Console Output** y, al terminar, los resultados de las pruebas y el informe de cobertura en los artefactos del build.

Jenkins **construye solo**, incluso antes de ese primer build y tras cada reinicio: el job de `casc.yaml` y el `Jenkinsfile` declaran `pollSCM('H/5 * * * *')`, así que revisa `REPO_BRANCH` en GitHub cada ~5 minutos y lanza un build cuando hay commits nuevos (en **Changes** aparecen los commits incluidos). Se usa sondeo porque GitHub no puede llamar a un Jenkins en `localhost` con un webhook. El registro de cada sondeo está en **Git Polling Log** del job. El sondeo va también en `casc.yaml` porque JCasC recrea el job en cada arranque: si solo estuviera en el `Jenkinsfile`, el job quedaría sin disparador hasta el siguiente build manual. Si cambias la expresión, cámbiala en los dos archivos. Jenkins solo sondea mientras está encendido; al levantarlo detecta en el primer sondeo los commits que se subieron con Jenkins apagado.

No hay asistente inicial: los plugins, el usuario y el job se crean al arrancar con *Configuration as Code* (`casc.yaml`).

## Configuración

Variables en `docker/jenkins/.env`, creado a partir de la plantilla `docker/jenkins/.env.example` (también se pueden definir en la terminal). Es un archivo distinto del `.env` de la raíz, que usa el `docker-compose.yml` de la API:

| Variable | Por defecto | Uso |
|---|---|---|
| `JENKINS_ADMIN_USER` | `admin` | Usuario administrador |
| `JENKINS_ADMIN_PASSWORD` | `admin` | Contraseña del administrador |
| `REPO_URL` | `https://github.com/christophersanz/AI-CICD.git` | Cambiarla si trabajas sobre tu propio fork |
| `REPO_BRANCH` | `master` | Rama que construye el job |
| `DEPLOY_ADMIN_PASSWORD` | *(vacía)* | Contraseña del `admin` del entorno desplegado (12–64 caracteres). Vacía, la etapa *Despliegue local* se omite. Solo se aplica en el primer despliegue |
| `DEPLOY_PORT` | `8090` | Puerto de `127.0.0.1` donde queda la API desplegada |
| `SONAR_ADMIN_PASSWORD` | *(vacía)* | Contraseña de `admin` en SonarQube (12+ caracteres con mayúscula, minúscula, dígito y símbolo). En el primer análisis Jenkins cambia la de por defecto (`admin`) por esta. Vacía, la etapa *Análisis SonarQube* se omite |

Ejemplo en PowerShell para construir tu fork:

```powershell
$env:REPO_URL = "https://github.com/<tu-usuario>/AI-CICD.git"
docker compose --env-file docker/jenkins/.env -f docker/jenkins/docker-compose.yml up -d
```

## Cómo funciona

- La imagen (`Dockerfile`) parte de `jenkins/jenkins:lts-jdk21` y añade el cliente de Docker con `buildx` y `jq` (para la prueba de humo).
- Para llegar a los contenedores de la verificación y del despliegue, Jenkins se une a sus redes (`contract-api-verify-<build>`, `contract-api-deploy`).
- Jenkins usa el Docker de tu máquina mediante `/var/run/docker.sock`: los contenedores de las pruebas y de `docker build` aparecen junto a los tuyos en `docker ps`. Por eso Testcontainers usa `TESTCONTAINERS_HOST_OVERRIDE=host.docker.internal`.
- El contenedor se ejecuta como `root` y controla el Docker del host. Es aceptable solo en local: no expongas este Jenkins en una red.
- Jenkins solo escucha en `127.0.0.1`, y la contraseña por defecto es solo para uso local.

## SonarQube

El compose levanta también `contract-api-sonarqube` (<http://localhost:9000>, usuario `admin` y tu `SONAR_ADMIN_PASSWORD`). La etapa *Análisis SonarQube* (`scripts/sonar-analysis.sh`, ADR-0015) prepara en el primer build lo que falte:

- cambia la contraseña por defecto de `admin`;
- genera un token de análisis, que se guarda en el volumen de Jenkins;
- crea el proyecto `contract-api` y el Quality Gate `contract-api`.

Después analiza el código con la cobertura de JaCoCo y espera el resultado del Quality Gate.

| Condición (todo el código) | Falla si |
|---|---|
| Bugs | hay más de 0 |
| Vulnerabilidades | hay más de 0 |
| Security hotspots revisados | menos del 100 % |
| Rating de mantenibilidad | peor que A |
| Cobertura | menos del 80 % |

Si el Quality Gate falla, el build se detiene antes de construir la imagen. El log de la etapa muestra el valor de cada condición. Si un hallazgo no aplica, se justifica en el código con `@SuppressWarnings("java:Sxxxx")` y un comentario que explique por qué, no marcándolo en el panel. Así la decisión queda versionada y se revisa en el PR.

SonarQube usa la base embebida H2, que sirve para practicar pero no permite actualizar de versión conservando los datos. Para cambiar la imagen, borra los volúmenes `contract-api-jenkins_sonarqube-*` (`down -v` borra también los de Jenkins); el siguiente build vuelve a prepararlo todo.

## Despliegue local

La última etapa ejecuta `scripts/deploy-local.sh` y deja corriendo, con `--restart unless-stopped`:

| Contenedor | Qué es |
|---|---|
| `contract-api-deploy` | La API con la imagen `contract-api:jenkins-<build>` y el perfil `prod`, en `http://localhost:8090` |
| `contract-api-deploy-db` | PostgreSQL 16 con el volumen `contract-api-deploy-data` (los datos sobreviven a los despliegues) |

- Las claves JWT y la contraseña de la base se generan en el primer despliegue y se guardan en el volumen de Jenkins (`/var/jenkins_home/contract-api-deploy`). Por eso un token sigue valiendo después de un redeploy.
- Inicia sesión con `admin` y tu `DEPLOY_ADMIN_PASSWORD`: `POST http://localhost:8090/api/v1/auth/login`.
- Cada despliegue repite la prueba de humo. Si falla, el script vuelve a levantar la imagen anterior y el build termina en rojo.
- Solo se conservan la imagen desplegada y la anterior; los demás tags `contract-api:jenkins-*` se borran.

Para borrar el entorno desplegado (datos incluidos):

```bash
docker rm -f contract-api-deploy contract-api-deploy-db
docker volume rm contract-api-deploy-data
docker network rm contract-api-deploy
docker exec contract-api-jenkins rm -rf /var/jenkins_home/contract-api-deploy
```

## Detener y limpiar

```bash
docker compose -f docker/jenkins/docker-compose.yml down       # detiene; conserva builds y caché de Maven
docker compose -f docker/jenkins/docker-compose.yml down -v    # además borra el volumen (empieza de cero)
```
