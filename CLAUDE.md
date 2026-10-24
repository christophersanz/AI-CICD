# CLAUDE.md

Contexto base del repositorio. Este archivo es el punto de entrada: es breve y enlaza a la documentación detallada.

## Documentación del proyecto

| Archivo | Contenido | Cuándo consultarlo |
|---|---|---|
| @docs/architecture.md | Capas, módulos, reglas de dependencia, convenciones de código, API REST, errores y pruebas | Siempre que se cree o modifique código |
| @docs/business-rules.md | Glosario, entidades, invariantes, casos de uso y estados del dominio | Antes de implementar o cambiar lógica de negocio |
| docs/technical.md | Estado implementado: endpoints, seguridad, variables de entorno, esquema de BD, build y CI/CD | Al cambiar endpoints, configuración, migraciones o el pipeline (actualizarlo en el mismo cambio) |
| docs/guides/arquitectura.md | Guía visual de la arquitectura: diagramas de módulos, puertos y adaptadores, dominio y flujos | Para orientarse en el código; actualizar sus diagramas si cambia la estructura |
| docs/guides/ci-cd.md | Guía práctica de CI/CD: flujo de PR, pipeline, versiones e imágenes | Al cambiar el pipeline, el `Dockerfile` o el flujo de ramas |
| @docs/prompts/prompts.md | Prompts reutilizables para tareas frecuentes | Al iniciar una tarea recurrente (nuevo caso de uso, revisión, ADR, etc.) |

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
| CI/CD | GitHub Actions (`.github/workflows/ci-cd.yml`), imagen Docker en GHCR y Docker Hub (opcional), GitHub Flow (ADR-0009); `Jenkinsfile` solo para Jenkins local de práctica (ADR-0010) |

## 3. Comandos habituales

```bash
./mvnw clean verify                   # Compilar, ejecutar todas las pruebas y verificar la cobertura (igual que el CI)
./mvnw test -pl domain                # Pruebas de un módulo
./mvnw spring-boot:run -pl infrastructure  # Levantar la API
docker compose up -d                  # Levantar dependencias locales (PostgreSQL y pgAdmin)
docker build -t contract-api .        # Construir la imagen (perfil prod)
docker compose -f docker/jenkins/docker-compose.yml up -d --build  # Jenkins local de práctica (ADR-0010)
```

## 3.1 Flujo de Git (GitHub Flow, ADR-0009)

- `main` está protegida y siempre es desplegable: **nunca se hace commit ni push directo a `main`**.
- Cada cambio va en una rama desde `main`: `feature/<tema>`, `fix/<tema>` o `docs/<tema>`, y entra por Pull Request con el CI en verde.
- Los mensajes de commit van en español, en imperativo y describen el porqué.
- Una versión se publica con un tag `vX.Y.Z` sobre `main`; el pipeline publica la imagen con esa etiqueta.

## 4. Cómo debe trabajar Claude en este repo

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
- Si cambias el pipeline, el `Dockerfile` o el flujo de ramas, actualiza ADR-0009 o registra uno nuevo.
