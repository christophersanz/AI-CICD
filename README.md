# contract-api

[![CI/CD](https://github.com/christophersanz/AI-CICD/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/christophersanz/AI-CICD/actions/workflows/ci-cd.yml)

API REST para la gestión de contratos laborales. Java 21, Spring Boot 3, Clean Architecture + DDD (`domain`, `application`, `infrastructure`).

## Documentación

| Documento | Contenido |
|---|---|
| [CLAUDE.md](CLAUDE.md) | Punto de entrada: stack, comandos y forma de trabajo |
| [docs/architecture.md](docs/architecture.md) | Capas, reglas de dependencia, convenciones y pruebas |
| [docs/business-rules.md](docs/business-rules.md) | Reglas de negocio, casos de uso y errores |
| [docs/technical.md](docs/technical.md) | Estado implementado: endpoints, seguridad, configuración, base de datos y CI/CD |
| [docs/adr/](docs/adr/) | Decisiones de arquitectura (CI/CD: ADR-0009; SonarCloud: ADR-0011) |
| [docs/guides/arquitectura.md](docs/guides/arquitectura.md) | Guía visual de la arquitectura: contexto, módulos, puertos y adaptadores, dominio, flujos de petición, tokens y errores |
| [docs/guides/ci-cd.md](docs/guides/ci-cd.md) | Guía de CI/CD: flujo de PR, pipeline, versiones, imágenes en GHCR y Docker Hub, ejecución y Jenkins local |
| [docs/design/003-security-local-testing.md](docs/design/003-security-local-testing.md) | Probar la API en local con PowerShell y Postman |

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

GitHub Flow ([ADR-0009](docs/adr/0009-ci-cd-github-actions.md)): rama `feature/*` desde `master` → Pull Request → CI en verde → merge. El pipeline compila, ejecuta las pruebas (incluidas las de Testcontainers), exige una cobertura ≥ 80 % en `domain` y `application`, analiza el código en SonarCloud (si está configurado) y construye la imagen; al integrar en `master`, la publica en GHCR y, si está configurado, en Docker Hub.
