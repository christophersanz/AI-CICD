# CLAUDE.md

Contexto base del repositorio. Este archivo es el punto de entrada: es breve y enlaza a la documentación detallada.

## Documentación del proyecto

| Archivo | Contenido | Cuándo consultarlo |
|---|---|---|
| @docs/architecture.md | Capas, módulos, reglas de dependencia, convenciones de código, API REST, errores y pruebas | Siempre que se cree o modifique código |
| @docs/business-rules.md | Glosario, entidades, invariantes, casos de uso y estados del dominio | Antes de implementar o cambiar lógica de negocio |
| docs/technical.md | Estado implementado: endpoints, seguridad, variables de entorno, esquema de BD, build y CI/CD | Al cambiar endpoints, configuración, migraciones o el pipeline (actualizarlo en el mismo cambio) |
| docs/api/contract/openapi.yaml | Contrato OpenAPI 3.1 generado con springdoc (también en `openapi.json`); cómo regenerarlo en `docs/api/README.md` | Al cambiar endpoints o DTOs de la API (regenerarlo en el mismo cambio) |
| docs/guides/diagramas-arquitectura.md | Guía visual de la arquitectura: diagramas de módulos, puertos y adaptadores, dominio y flujos | Para orientarse en el código; actualizar sus diagramas si cambia la estructura |
| docs/guides/ci-cd.md | Guía práctica de CI/CD: GitHub Flow, pipeline, despliegue con aprobación, versiones e imágenes | Al cambiar el pipeline, el `Dockerfile` o el flujo de ramas |
| @docs/prompts/README.md | Índice de los prompts `docs/prompts/NN-nombre.md` (por orden de uso), sus comandos de Claude Code, la estructura común, las fuentes y el kit reutilizable | Al iniciar una tarea recurrente (requisitos, reglas de negocio, caso de uso, pruebas, documentación, revisión, pipeline) |
| docs/design/seguridad.md | Diseño de la seguridad y sus fases | Al tocar autenticación, autorización o tokens |
| docs/reviews/ | Informes de revisión por ronda (`NNN-<paso>-<modo>.md`) | Antes de corregir (`/07-corregir`) o al hacer la revisión final |
| docs/sessions/ | Progreso de cada prompt | Al retomar una tarea larga |
| docs/ai-log.md | Bitácora de IA (tareas, correcciones y métricas) | Al terminar una tarea con IA (anotar la entrada) |

## 1. Descripción del proyecto

- **Nombre:** `contract-api`
- **Propósito:** API REST para la gestión de **contratos laborales**: registro, firma, renovación, término y consulta de contratos de trabajo.
- **Estilo arquitectónico:** Clean Architecture + DDD táctico, en 3 módulos: `domain`, `application`, `infrastructure`. Detalle en `docs/architecture.md`.
- **Paquete base:** `pe.edu.galaxy.training.ia.ca`
- **Idioma:** código (clases, métodos, paquetes) en **inglés**; documentación, ADRs y explicaciones en **español**.

## 2. Stack tecnológico

| Área | Tecnología |
|---|---|
| Lenguaje | Java 21 (`record`, `sealed`, pattern matching, virtual threads cuando aplique) |
| Framework | Spring Boot 3.x |
| Build | Maven|
| Persistencia | Spring Data JPA + PostgreSQL, migraciones con Flyway |
| Mapeo | MapStruct |
| Validación | Jakarta Bean Validation (solo en la capa de entrada) |
| Seguridad | Spring Security 6 + OAuth2 Resource Server (JWT RS256 con Nimbus); tokens propios (ADR-0005) |
| Documentación API | springdoc-openapi (OpenAPI 3) |
| Observabilidad | Micrometer + OpenTelemetry, logs estructurados en JSON |
| Pruebas | JUnit 5, AssertJ, Mockito, Testcontainers, spring-security-test; cobertura con JaCoCo (≥ 80 % de líneas en `domain` y `application`) |
| CI/CD | GitHub Actions (`.github/workflows/ci-cd.yml`), análisis estático en SonarCloud (ADR-0011), imagen Docker en GHCR y Docker Hub (opcional) (ADR-0009); GitHub Flow con checks obligatorios y despliegue con aprobación en el environment `production` (ADR-0012) a Render (`render.yaml`, ADR-0013); `Jenkinsfile` solo para Jenkins local de práctica, por etapas, con Quality Gate de SonarQube local y despliegue local en `localhost:8090` (ADR-0010, ADR-0014, ADR-0015) |

## 3. Comandos habituales

```bash
./mvnw clean verify                   # Compilar, ejecutar todas las pruebas y verificar la cobertura (igual que el CI)
./mvnw test -pl domain                # Pruebas de un módulo
./mvnw spring-boot:run -pl infrastructure  # Levantar la API
docker compose up -d                  # Levantar dependencias locales (PostgreSQL y pgAdmin)
docker build -t contract-api .        # Construir la imagen (perfil prod)
docker compose --env-file docker/jenkins/.env -f docker/jenkins/docker-compose.yml up -d --build  # Jenkins local de práctica (ADR-0010)
```

## 3.1 Flujo de Git (GitHub Flow, ADR-0012)

- Nunca se hace push directo a `master`: todo cambio va en una rama corta (`feature/*`, `fix/*`, `docs/*`) y entra por Pull Request. Antes de cada push, `./mvnw clean verify` en verde.
- El ruleset `master-protegida` exige los checks **Build, pruebas y cobertura** e **Imagen Docker** en verde, la rama al día con `master` e integración solo con **squash**; sin force push ni borrado. Claude no integra PRs: el *Squash and merge* lo decide el desarrollador.
- Tras el merge, **Verificación de la imagen** prueba la imagen publicada en un entorno efímero y **Despliegue (production)** espera la aprobación del environment `production` para desplegarla en Render. Claude no aprueba despliegues.
- Los mensajes de commit van en español, en imperativo y describen el porqué.
- Una versión se publica con un tag `vX.Y.Z` sobre `master`; el pipeline publica la imagen con esa etiqueta.

## 4. Cómo debe trabajar Claude en este repo

- Para tareas recurrentes usa los prompts de `docs/prompts/` (`NN-nombre.md`, `NN` = orden de uso; un solo archivo por prompt) mediante su comando `/NN-nombre` de `.claude/commands/`, que solo carga ese archivo. Los datos propios del proyecto que usan los prompts están en la sección 5. Son 9, en orden: `/01-definir` (requisitos o reglas), `/02-implementar-caso-uso`, `/03-pruebas`, `/04-documentar`, `/05-refactorizar`, `/06-revisar` (por capa en orden, PR o final `READY`/`NOT READY`), `/07-corregir` (ronda de revisión o Quality Gate), `/08-cambiar-pipeline` y `/09-mejorar-prompt`; índice en `docs/prompts/README.md`. Todos presentan un plan y esperan aprobación antes de cambiar código, y guardan su progreso en `docs/sessions/`. `/06-revisar` no modifica código: corrige `/07-corregir`. Un prompt se mejora editando su único archivo (`/09-mejorar-prompt`); su historia queda en git.
- Antes de crear código, identifica **en qué capa** va y respeta la regla de dependencia (`docs/architecture.md`, sección 3).
- Antes de tocar lógica de negocio, verifica las invariantes y reglas en `docs/business-rules.md`. **No inventes reglas de negocio:** si falta una, pregunta.
- Para una funcionalidad nueva, sigue este orden: **dominio → puerto de entrada → caso de uso → puerto de salida → adaptador → controller → pruebas**.
- Cada caso de uso nuevo incluye sus pruebas unitarias en el mismo cambio.
- Si una petición obliga a violar una regla de arquitectura, **detente y explica el conflicto** proponiendo alternativas antes de implementar.
- Si se agrega o cambia una regla de negocio, actualiza `docs/business-rules.md` en el mismo cambio.
- Las decisiones arquitectónicas relevantes se registran como ADR en `docs/adr/NNNN-titulo.md` (contexto, decisión, alternativas, consecuencias).
- Evita la sobreingeniería: no introduzcas CQRS, event sourcing ni nuevos módulos sin una necesidad justificada.
- No añadas dependencias nuevas al `pom.xml` sin indicarlo y justificarlo.
- Ejecuta `./mvnw clean verify` antes de dar una tarea por terminada: es lo mismo que ejecuta el CI, incluido el umbral de cobertura.
- Si cambias el pipeline, el `Dockerfile` o el flujo de ramas, actualiza ADR-0009/ADR-0012/ADR-0013/ADR-0014/ADR-0015 o registra uno nuevo.

## 5. Datos para los prompts (DP)

Los prompts de `docs/prompts/` son genéricos (kit reutilizable, `docs/prompts/kit/`). Cuando un prompt dice `DP.<clave>`, el valor sale de esta tabla. En otro proyecto solo cambia esta sección.

| Clave | Valor en este proyecto |
|---|---|
| `nombre` | `contract-api` |
| `stack` | Java 21, Spring Boot 3.5, Maven, JUnit 5, AssertJ, Mockito, Testcontainers y `spring-security-test` |
| `dominio-negocio` | Gestión de contratos laborales de una organización en Perú (RR.HH.) |
| `normativa` | Reglas con base en la normativa laboral peruana: se marcan con ⚖️ y se validan con RR.HH./asesoría legal; no citar artículos de ley sin verificarlos |
| `paquete-base` | `pe.edu.galaxy.training.ia.ca` |
| `modulos` | `domain`, `application`, `infrastructure` (la API REST está en `infrastructure/api`) |
| `build` | `./mvnw clean verify` (compila, ejecuta todas las pruebas y verifica la cobertura; requiere Docker para Testcontainers) |
| `base-datos` | PostgreSQL 16 |
| `migraciones` | Flyway en `infrastructure/src/main/resources/db/migration` |
| `pruebas-integracion` | Testcontainers con PostgreSQL real (nunca H2) |
| `cobertura-minima` | 80 % de líneas en `domain` y `application` (JaCoCo; el build falla por debajo) |
| `rama-principal` | `master` |
| `flujo-git` | GitHub Flow: rama corta + PR, ruleset `master-protegida` con checks requeridos, solo *squash* y rama al día (ADR-0012) |
| `seguridad` | JWT RS256 propio + refresh token opaco de un solo uso; permisos con `@PreAuthorize` según `business-rules.md` §4.1; diseño en `docs/design/seguridad.md` (ADR-0005) |
| `adr-arquitectura` | ADR-0003 (sin ArchUnit) y ADR-0004 (paquetes del dominio por agregado) |
| `adr-cicd` | ADR-0009 a ADR-0015 |
| `ci` | GitHub Actions: `.github/workflows/ci-cd.yml`, scripts en `.github/scripts/` (prueba de humo compartida `smoke-test.sh`), ruleset versionado en `.github/ruleset/` |
| `imagen` | `Dockerfile` multi-stage (JRE 21, usuario sin privilegios, perfil `prod`), publicada en GHCR y Docker Hub con etiquetas `latest`, `sha-<corto>` y versión |
| `despliegue` | Verificación efímera de la imagen y despliegue en Render (`render.yaml`) tras la aprobación del environment `production` (ADR-0013); despliegue local de Jenkins en <http://localhost:8090> (ADR-0014) |
| `ci-local` | Jenkins en <http://localhost:8080>: `Jenkinsfile` y `docker/jenkins/` (compose, `casc.yaml`, `scripts/`, `.env` no versionado con `REPO_BRANCH`). Para probar una rama: `REPO_BRANCH=<rama>`, `up -d` del compose y **Build Now**; al terminar, `REPO_BRANCH=master`, `up -d` y **Build Now** sobre `master` (`docker/jenkins/README.md` → *Probar una rama*) |
| `sonarqube` | SonarQube local en <http://localhost:9000>, proyecto y Quality Gate `contract-api`; credenciales `SONAR_ADMIN_PASSWORD` en `docker/jenkins/.env`; condiciones en `docker/jenkins/scripts/sonar-analysis.sh` (ADR-0015) |
| `openapi` | `docs/api/contract/openapi.yaml` y `.json`, generados con springdoc; cómo regenerarlos en `docs/api/README.md` |
| `postman` | Colección y entorno local en `docs/api/postman/` (se ejecutan con `newman` o el Postman CLI) |
| `guias` | `docs/guides/diagramas-arquitectura.md` (diagramas) y `docs/guides/ci-cd.md` (pipeline y *Problemas frecuentes*) |
