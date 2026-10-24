# Jenkins local de práctica

Jenkins en Docker, ya configurado para ejecutar el `Jenkinsfile` de este repositorio: compila, ejecuta todas las pruebas (incluidas las de Testcontainers), verifica la cobertura y construye la imagen Docker. Es solo para practicar; el CI oficial es GitHub Actions (ADR-0009 y ADR-0010).

## Requisitos

- Docker Desktop en ejecución (contenedores Linux).
- Puerto `8080` libre.
- Conexión a internet la primera vez (descarga plugins, dependencias de Maven e imágenes).

## Levantar Jenkins

Desde la raíz del repositorio:

```bash
docker compose -f docker/jenkins/docker-compose.yml up -d --build
```

La primera vez tarda unos minutos (construye la imagen e instala los plugins). Cuando `docker logs contract-api-jenkins` muestre `Jenkins is fully up and running`:

1. Abre http://localhost:8080 e inicia sesión con `admin` / `admin`.
2. Entra al job **contract-api** y pulsa **Build Now** (o **Construir ahora**).
3. Revisa el avance en **Console Output** y, al terminar, los resultados de las pruebas y el informe de cobertura en los artefactos del build.

No hay asistente inicial: los plugins, el usuario y el job se crean al arrancar con *Configuration as Code* (`casc.yaml`).

## Configuración

Variables de entorno opcionales (en la terminal o en un archivo `.env` junto a este `docker-compose.yml`):

| Variable | Por defecto | Uso |
|---|---|---|
| `JENKINS_ADMIN_USER` | `admin` | Usuario administrador |
| `JENKINS_ADMIN_PASSWORD` | `admin` | Contraseña del administrador |
| `REPO_URL` | repositorio del curso | Cambiarla si trabajas sobre tu propio fork |
| `REPO_BRANCH` | `main` | Rama que construye el job |

Ejemplo en PowerShell para construir tu fork:

```powershell
$env:REPO_URL = "https://github.com/<tu-usuario>/CURS-000617-CICD.git"
docker compose -f docker/jenkins/docker-compose.yml up -d
```

## Cómo funciona

- La imagen (`Dockerfile`) parte de `jenkins/jenkins:lts-jdk21` y añade el cliente de Docker con `buildx`.
- Jenkins usa el Docker de tu máquina mediante `/var/run/docker.sock`: los contenedores de las pruebas y de `docker build` aparecen junto a los tuyos en `docker ps`. Por eso Testcontainers usa `TESTCONTAINERS_HOST_OVERRIDE=host.docker.internal`.
- El contenedor se ejecuta como `root` y controla el Docker del host. Es aceptable solo en local: no expongas este Jenkins en una red.
- Jenkins solo escucha en `127.0.0.1`, y la contraseña por defecto es solo para uso local.

## Detener y limpiar

```bash
docker compose -f docker/jenkins/docker-compose.yml down       # detiene; conserva builds y caché de Maven
docker compose -f docker/jenkins/docker-compose.yml down -v    # además borra el volumen (empieza de cero)
```
