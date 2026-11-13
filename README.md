# contract-api

[![CI/CD](https://github.com/christophersanz/AI-CICD/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/christophersanz/AI-CICD/actions/workflows/ci-cd.yml)

API REST para la gestión de contratos laborales. Java 21, Spring Boot 3, Clean Architecture + DDD (`domain`, `application`, `infrastructure`).

## Documentación

Los nombres de los tres documentos base (`architecture.md`, `business-rules.md`, `technical.md`) están en inglés por convención del curso, y porque los prompts y el kit los citan con ese nombre. El resto de la documentación usa nombres en español.

| Documento | Contenido |
|---|---|
| [CLAUDE.md](CLAUDE.md) | Punto de entrada: stack, comandos, flujo de Git, forma de trabajar y datos para los prompts |
| **Base** | |
| [docs/architecture.md](docs/architecture.md) | Capas, reglas de dependencia, convenciones, API, errores y pruebas |
| [docs/business-rules.md](docs/business-rules.md) | Glosario, agregados, reglas de negocio, estados, casos de uso, errores y preguntas abiertas |
| [docs/technical.md](docs/technical.md) | Estado implementado: endpoints, seguridad, configuración, base de datos y CI/CD |
| **Decisiones y diseño** | |
| [docs/adr/](docs/adr/) | Decisiones de arquitectura: dominio (0001–0004), seguridad (0005; 0006–0008 reservados), CI/CD (0009–0015) |
| [docs/design/seguridad.md](docs/design/seguridad.md) | Diseño de la seguridad (3A, JWT, refresh tokens, permisos) y sus fases |
| [docs/design/seguridad-pruebas-locales.md](docs/design/seguridad-pruebas-locales.md) | Probar la seguridad en local con PowerShell y Postman |
| **Guías** | |
| [docs/guides/diagramas-arquitectura.md](docs/guides/diagramas-arquitectura.md) | Diagramas de la arquitectura: contexto, módulos, puertos y adaptadores, dominio, flujos de petición, tokens y errores |
| [docs/guides/ci-cd.md](docs/guides/ci-cd.md) | CI/CD: GitHub Flow, pipeline, despliegue con aprobación, versiones, imágenes, ejecutar la imagen (Windows y macOS) y Jenkins local |
| **API** | |
| [docs/api/](docs/api/README.md) | Contrato OpenAPI 3.1 (`contract/openapi.yaml` y `.json`), cómo regenerarlo y la colección Postman (`postman/`) |
| [docs/api/postman/](docs/api/postman/) | Colección Postman (login y endpoints, con aserciones de códigos HTTP) y entorno local, junto al contrato OpenAPI |
| **Trabajo con IA** | |
| [docs/prompts/](docs/prompts/README.md) | 9 prompts genéricos por orden de uso (`NN-nombre.md`), cada uno con su comando de Claude Code en `.claude/commands/`, y el kit para reutilizarlos en otros proyectos (`docs/prompts/kit/`) |
| [docs/reviews/](docs/reviews/README.md) | Informes de revisión: una carpeta por ronda con su resumen, y su índice |
| [docs/sessions/](docs/sessions/README.md) | Archivos de sesión: progreso de cada prompt para retomarlo sin perder contexto |
| [docs/ai-log.md](docs/ai-log.md) | Bitácora de IA: tareas, lo aceptado, lo corregido, tiempos y métricas |
| **Infraestructura local** | |
| [docker/jenkins/](docker/jenkins/README.md) | Jenkins local con SonarQube y despliegue local |
| `docker/pgadmin/` | Servidor preconfigurado de pgAdmin para `docker compose` |

## Levantar todo con Docker (recomendado)

```bash
cp .env.example .env                 # variables locales (no se versiona)
docker compose up -d --build         # PostgreSQL + pgAdmin + API
docker logs -f contract-api          # espera "Started ContractApiApplication"
```

| Servicio | URL | Credenciales (de `.env`) |
|---|---|---|
| API / Swagger UI | http://localhost:8081/swagger-ui.html | `admin` / `SECURITY_BOOTSTRAP_ADMIN_PASSWORD` |
| pgAdmin | http://localhost:5050 | `PGADMIN_EMAIL` / `PGADMIN_PASSWORD` |
| PostgreSQL | `localhost:5432`, base `contracts` | `contract` / `DB_PASSWORD` |

En Swagger: `POST /api/v1/auth/login` → copia `accessToken` → **Authorize**. Para detenerlo: `docker compose down` (añade `-v` para borrar los datos).

## Ejecutar en local (sin contenedor para la API)

```bash
docker compose up -d                       # PostgreSQL (5432) y pgAdmin (http://localhost:5050)
./mvnw install -DskipTests
./mvnw spring-boot:run -pl infrastructure  # http://localhost:8081, perfil local
```

Para iniciar sesión, define antes `SECURITY_BOOTSTRAP_ADMIN_USERNAME` y `SECURITY_BOOTSTRAP_ADMIN_PASSWORD` (ver la guía de prueba local).

## Imagen Docker

```bash
docker build -t contract-api .
docker run -p 8081:8081 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/contracts -e DB_USERNAME=contract -e DB_PASSWORD=... \
  -e SECURITY_JWT_PUBLIC_KEY="$(cat public.pem)" -e SECURITY_JWT_PRIVATE_KEY="$(cat private.pem)" \
  contract-api
```

La imagen usa el perfil `prod`: no tiene valores por defecto ni clave efímera, y Swagger está desactivado. Las imágenes publicadas están en `ghcr.io/christophersanz/ai-cicd` (requiere `docker login ghcr.io` con un token `read:packages`) y, si el repositorio tiene configurado Docker Hub, en `docker.io/<usuario>/ai-cicd` (pública).

## Flujo de trabajo

GitHub Flow ([ADR-0012](docs/adr/0012-github-flow-y-despliegue-con-aprobacion.md)): cada cambio va en una rama corta y entra a `master` por Pull Request con *squash*, solo con los checks en verde. En cada PR y en cada merge el pipeline compila, ejecuta las pruebas (incluidas las de Testcontainers), exige una cobertura ≥ 80 % en `domain` y `application`, analiza el código en SonarCloud (si está configurado) y construye y publica la imagen en GHCR y, si está configurado, en Docker Hub. Tras el merge, una prueba de humo verifica la imagen publicada en un entorno efímero y el despliegue en [Render](https://render.com) espera la aprobación del environment `production` ([ADR-0013](docs/adr/0013-despliegue-en-render.md)): Render ejecuta exactamente esa imagen (por su sha) y la misma prueba de humo se repite contra la URL pública.
