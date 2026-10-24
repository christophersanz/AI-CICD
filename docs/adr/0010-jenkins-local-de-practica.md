# ADR-0010: Jenkins local de práctica junto al CI de GitHub Actions

- **Estado:** Aceptado
- **Fecha:** 2026-10-17

## Contexto

El CI/CD oficial es GitHub Actions (ADR-0009), que descartó Jenkins como plataforma. Para el curso se quiere practicar también con Jenkins, ejecutado en local con Docker, sin cambiar el pipeline oficial ni la protección de `main`.

## Decisión

- Se versiona un `Jenkinsfile` declarativo en la raíz con las mismas etapas que el job de CI: `./mvnw clean verify` (pruebas, Testcontainers y umbral de cobertura) y `docker build` de la imagen. No publica en GHCR.
- Jenkins se entrega al alumno en `docker/jenkins/` (`docker-compose.yml`, `Dockerfile`, `plugins.txt`, `casc.yaml`): imagen `jenkins/jenkins:lts-jdk21` con el cliente de Docker y `buildx`, plugins preinstalados y *Configuration as Code* (JCasC) que crea el usuario administrador y el job `contract-api` sin asistente inicial. El repositorio y la rama del job son configurables por variables de entorno.
- Jenkins usa el Docker del host montando `docker.sock`; por eso Testcontainers necesita `TESTCONTAINERS_HOST_OVERRIDE=host.docker.internal`. Solo escucha en `127.0.0.1`.
- GitHub Actions sigue siendo el único check requerido para integrar en `main`.

## Alternativas consideradas

1. **Pipeline y configuración solo en la interfaz de Jenkins.** Pro: nada que versionar. Contra: cada alumno repite el asistente, la instalación de plugins y la creación del job; no se revisa por PR y se pierde al borrar Jenkins.
2. **Docker-in-Docker (`docker:dind`) en lugar del socket del host.** Pro: aísla los contenedores de Jenkins. Contra: contenedor privilegiado, más configuración (TLS, red) y sin caché compartida con el host.

## Consecuencias

- Positivas: se practica Jenkins con el mismo build que el CI oficial; el `Jenkinsfile` se revisa como cualquier cambio.
- Negativas: dos definiciones de pipeline que mantener alineadas; los plugins no tienen versión fijada (se instala la última compatible con la LTS), así que una imagen nueva puede traer cambios; montar `docker.sock` y ejecutar como `root` da a Jenkins control total del Docker del host (aceptable solo en local).
- Seguimiento: si Jenkins pasara a ser el CI oficial, este ADR y ADR-0009 deben reemplazarse.
