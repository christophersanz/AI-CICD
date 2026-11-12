## 5. Datos para los prompts (DP)

Los prompts de `docs/prompts/` son genéricos. Cuando un prompt dice `DP.<clave>`, el valor sale de esta tabla: es lo único que cambia de un proyecto a otro. Completa cada `[...]`; si algo no existe en el proyecto, escribe **"No aplica"** y los prompts lo omitirán (por ejemplo, sin CI local o sin despliegue).

| Clave | Qué poner | Valor en este proyecto |
|---|---|---|
| `nombre` | Nombre del servicio | `[mi-servicio]` |
| `stack` | Lenguaje, framework, build y librerías de prueba (define el rol de los prompts de código) | [Java 21, Spring Boot 3.5, Maven, JUnit 5, AssertJ, Mockito, Testcontainers] |
| `dominio-negocio` | Negocio y país o contexto, en una línea (define el rol de los prompts de requisitos y reglas) | [Gestión de pedidos de una tienda en línea] |
| `normativa` | Cómo se tratan las reglas con base legal o normativa | [Se marcan con ⚖️ y se validan con el área legal / No aplica] |
| `paquete-base` | Paquete raíz de Java | `[com.empresa.servicio]` |
| `modulos` | Módulos y dónde está la API | [`domain`, `application`, `infrastructure` (API en `infrastructure/api`)] |
| `build` | Comando que compila, prueba y verifica la cobertura (el mismo que el CI) | `[./mvnw clean verify]` |
| `base-datos` | Motor y versión | [PostgreSQL 16] |
| `migraciones` | Herramienta y carpeta | [Flyway en `infrastructure/src/main/resources/db/migration`] |
| `pruebas-integracion` | Cómo se prueba contra la base real | [Testcontainers con PostgreSQL (nunca H2)] |
| `cobertura-minima` | Umbral y herramienta | [80 % de líneas en `domain` y `application` (JaCoCo)] |
| `rama-principal` | Rama de integración | `[main]` |
| `flujo-git` | Flujo de ramas y protecciones (y su ADR) | [GitHub Flow: rama corta + PR con checks requeridos y *squash*] |
| `seguridad` | Mecanismo de autenticación y autorización, y su diseño o ADR | [JWT + permisos con `@PreAuthorize` según `business-rules.md` §4.1 / No aplica todavía] |
| `adr-arquitectura` | ADR de arquitectura que las revisiones deben respetar | [ADR-0001, ADR-0002] |
| `adr-cicd` | ADR de CI/CD | [ADR-0003 / No aplica] |
| `ci` | Plataforma, workflow y carpeta de scripts del CI oficial | [GitHub Actions: `.github/workflows/ci.yml`, scripts en `.github/scripts/`] |
| `imagen` | Cómo se construye y dónde se publica la imagen | [`Dockerfile` multi-stage, publicada en GHCR con `latest` y `sha-<corto>`] |
| `despliegue` | Entornos, destino y aprobaciones | [Despliegue en [plataforma] tras aprobar el environment `production` / No aplica] |
| `ci-local` | CI local (URL, archivos y cómo apuntarlo a otra rama) | [Jenkins en http://localhost:8080 con `Jenkinsfile` / No aplica] |
| `sonarqube` | Servidor, proyecto, Quality Gate, credenciales y dónde se definen sus condiciones | [SonarQube en http://localhost:9000, proyecto `[mi-servicio]` / SonarCloud / No aplica] |
| `openapi` | Dónde está el contrato OpenAPI y cómo se regenera | [`docs/api/contract/openapi.yaml`, generado con springdoc] |
| `postman` | Colección de pruebas de la API | [`docs/api/postman/` / No aplica] |
| `guias` | Guías con diagramas y la guía del pipeline | [`docs/guides/diagramas-arquitectura.md`, `docs/guides/ci-cd.md`] |

`docs/prompts/kit/verificar.sh` comprueba que estén todas las claves que usan los prompts.
