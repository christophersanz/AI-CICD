# Bitácora de IA

Registro del uso de asistentes de IA en `contract-api`: qué se pidió, qué se aceptó, qué hubo que corregir y cuánto tiempo tomó. Es la fuente de los datos de productividad de la presentación.

- **Asistente principal:** Claude Code (con `CLAUDE.md`, `docs/architecture.md` y `docs/business-rules.md` como contexto).
- **Flujo:** Explore → Plan → Code → Commit. Cada cambio se acepta solo con `./mvnw clean verify` en verde.
- **Prompts versionados:** comandos de Claude Code en `.claude/commands/` (índice en `docs/prompts/prompts.md`); los prompts numerados `001`–`006` de `docs/prompts/` son el histórico del curso. El progreso de cada comando queda en `docs/sessions/`.
- **Regla:** nunca se pegan secretos ni datos reales; las credenciales de los ejemplos son de prueba.

> Los campos `[__]` son tiempos que solo el autor puede medir: complétalos con tus valores reales. El resto de los datos sale del repositorio (revisiones, diseño, historial y builds).

## Resumen

| # | Fecha | Tarea | Prompt | Con IA | Sin IA (est.) | Errores de la IA corregidos |
|---|---|---|---|---|---|---|
| 1 | [__] | Diseño de arquitectura y modelo de dominio | `prompts.md` §1 (v1) | [__ h] | [__ h] | [__] |
| 2 | 2026-09-26 | Revisión integral del repositorio | `001-review-01.md` | [__ h] | [__ h] | 3 |
| 3 | 2026-10-03 | Refactor del dominio por agregado | `002.-refactoring-v1.md` | [__ h] | [__ h] | [__] |
| 4 | 2026-10-03 | Diseño de la seguridad (3A) | `003-security-01.md` | [__ h] | [__ h] | [__] |
| 5 | [__] | Casos de uso de empleados | `004-implement-usecase.md` | [__ h] | [__ h] | [__] |
| 6 | 2026-10-16 | Seguridad fase 1: tokens, login, `AuthController` | `005-security-02.md` | [__ h] | [__ h] | 3 |
| 7 | 2026-10-24 | CI/CD, Docker y SonarCloud | `006-cicd.md` | [__ h] | [__ h] | 3 |
| 8 | 2026-10-24 | Autorización con `@PreAuthorize` y colección Postman | sesión Claude Code | [__ h] | [__ h] | 3 |
| 9 | 2026-10-24 | Presentación final | sesión Claude Code | [__ h] | [__ h] | 2 |
| 10 | 2026-11-11 | GitHub Flow, despliegue con aprobación y Render (PR #1, #2) | sesión Claude Code | [__ h] | [__ h] | 1 |
| 11 | 2026-11-12 | Jenkins por etapas, despliegue local y SonarQube (PR #3–#5) | sesión Claude Code | [__ h] | [__ h] | 3 |
| 12 | 2026-11-12 | Prompts v2: comandos de Claude Code y archivos de sesión | sesión Claude Code | [__ h] | [__ h] | [__] |
| | | **Total** | | **[__ h]** | **[__ h]** | **[__]** |

## Entradas

### #1 — Diseño de arquitectura y modelo de dominio

- **Prompt:** plantilla "Diseño de arquitectura" de la primera versión de `docs/prompts/prompts.md` §1 (rol de arquitecto, contexto de negocio, entregables); se puede ver en el historial de git.
- **Aceptado:** módulos `domain` / `application` / `infrastructure`, agregados `Contract` y `Employee`, catálogo de reglas `BR-XXX-NNN` y de errores, ADR-0001 a ADR-0003.
- **Corregido:** [__]
- **Tiempo:** [__ h] · **sin IA (est.):** [__ h]

### #2 — Revisión integral (`001-review-01.md`)

- **Resultado:** `docs/reviews/001-review-01-result.md`. 20 hallazgos: 1 alto, 6 medios, 13 bajos. Build en verde (dominio 94,2 % de líneas).
- **Aceptado:** trazabilidad de reglas de negocio contra el código y las pruebas, plan de acción priorizado.
- **Corregido:**
  - El prompt de revisión contradecía dos ADRs vigentes: se corrigió el prompt, no el código.
  - La documentación exigía 80 % de cobertura, pero JaCoCo no estaba en el `pom.xml`: se añadió el plugin con el umbral.
  - Hallazgo alto H-01: Hibernate registra el DNI al violar la clave única. **Pendiente** de corregir.
- **Tiempo:** [__ h] · **sin IA (est.):** [__ h]

### #3 — Refactor del dominio (`002.-refactoring-v1.md`)

- **Aceptado:** paquetes por agregado con `common` y `shared` (ADR-0004), `Guard` y excepciones de dominio en lugar de `NullPointerException`, `Clock` como parámetro en vez de `now()`.
- **Corregido:** [__]
- **Tiempo:** [__ h] · **sin IA (est.):** [__ h]

### #4 — Diseño de la seguridad (`003-security-01.md`)

- **Aceptado:** `docs/design/003-security-design.md` y ADR-0005: JWT RS256 propio, refresh token opaco con rotación y detección de reutilización, RBAC por permisos, implementación por fases.
- **Corregido:** [__]
- **Tiempo:** [__ h] · **sin IA (est.):** [__ h]

### #5 — Casos de uso de empleados (`004-implement-usecase.md`)

- **Aceptado:** `RegisterEmployee`, `UpdateEmployee`, `GetEmployee` y `ListEmployees` siguiendo el orden dominio → puerto → caso de uso → adaptador → controller → pruebas.
- **Corregido:** [__]
- **Tiempo:** [__ h] · **sin IA (est.):** [__ h]

### #6 — Seguridad fase 1 (`005-security-02.md`)

- **Aceptado:** `TokenIssuerAdapter`, login, refresh, logout, `/auth/me`, creación del administrador inicial.
- **Corregido** (desviaciones D6 y D9 del diseño 003):
  - BCrypt solo compara los primeros 72 bytes: se rechazan contraseñas más largas antes de autenticar.
  - `InvalidRefreshTokenException` respondía 422 y `UnauthenticatedException` 500: ambas pasan a 401.
  - La creación de usuarios quedaba abierta a cualquier autenticado: regla de URL temporal con `user:manage`.
- **Tiempo:** [__ h] · **sin IA (est.):** [__ h]

### #7 — CI/CD (`006-cicd.md`)

- **Aceptado:** workflow de GitHub Actions, SonarCloud (ADR-0011), imagen en GHCR, API en `docker compose`, `.env.example`, Jenkins local.
- **Corregido:**
  - El workflow escuchaba `main` y el repositorio usa `master`: nunca se habría ejecutado.
  - Faltaba `.mvn/wrapper/maven-wrapper.properties`: `./mvnw` y el `Dockerfile` fallaban.
  - `mvnw` estaba en git sin permiso de ejecución.
- **Medido:** últimas 5 ejecuciones del pipeline en verde, entre 1 min 55 s y 3 min 11 s.
- **Tiempo:** [__ h] · **sin IA (est.):** [__ h]

### #8 — Autorización con `@PreAuthorize` y colección Postman

- **Aceptado:** `@EnableMethodSecurity`; `@PreAuthorize` con `employee:create`, `employee:update`, `employee:read` y `user:manage`; migración `V5` con `employee:read`; 403 `AUTH-001` desde el controller; 8 pruebas nuevas (403 por permiso y `@WithMockUser`); colección `postman/` con 19 peticiones.
- **Corregido:**
  - La IA dio por implementado `@PreAuthorize` al contar coincidencias que en realidad estaban en comentarios TODO. Se detectó al leer el código.
  - Un `AccessDeniedException` lanzado por `@PreAuthorize` habría llegado al manejador genérico y respondido 500: se añadió su traducción a 403.
  - El paso de la demo "EMPLOYEE → GET /employees → 403" era falso con el código de entonces: se cambió.
- **Medido:** `./mvnw clean verify` en verde con 355 pruebas y 0 fallos; cobertura de líneas 95,3 % (domain), 96,7 % (application), 96,5 % (infrastructure). Colección ejecutada con newman contra una base limpia: 41/41 aserciones.
- **Tiempo:** [__ h] · **sin IA (est.):** [__ h]

### #9 — Presentación final

- **Aceptado:** 16 diapositivas con caso de negocio, diagrama C4, diagrama de secuencia, códigos HTTP, demo, seguridad, pruebas, CI/CD y lecciones; cifras tomadas del repositorio y de GitHub Actions.
- **Corregido:**
  - Afirmaciones de autorización que no coincidían con el código (ver #8).
  - Datos de productividad: la IA no los inventa; quedan como campos a completar.
- **Tiempo:** [__ h] · **sin IA (est.):** [__ h]

### #10 — GitHub Flow, despliegue con aprobación y Render

- **Origen:** comparación con el repositorio del docente (`GalaxyTraining/CURS-000617-CICD`, ruleset `main-protegida`) y con las presentaciones de la sesión 05, donde el único despliegue real fue on-premise.
- **Aceptado:** ruleset `master-protegida` con PR obligatorio, checks requeridos, solo *squash* y rama al día (ADR-0012); environment `production` con revisor obligatorio; job de verificación efímera con prueba de humo compartida (`.github/scripts/smoke-test.sh`); despliegue en Render desde Docker Hub por sha con `render.yaml` (ADR-0013).
- **Corregido:** la primera aprobación de `production` no quedó registrada en GitHub (diálogo cerrado antes de confirmar); se detectó consultando la API y se repitió.
- **Medido:** run `43076800725` con los tres jobs en verde; despliegue efímero de 31 s con login, `/auth/me` y `/employees` (200) y 401 sin token. Render queda pendiente de configurar (job *skipped*).
- **Tiempo:** [__ h] · **sin IA (est.):** [__ h]

### #11 — Jenkins por etapas, despliegue local y SonarQube

- **Origen:** el pipeline de Jenkins de un compañero mostraba cada paso en el *Stage View*, pasaba un Quality Gate de SonarQube y terminaba desplegado; el nuestro tenía 2 etapas y no desplegaba.
- **Aceptado:** sondeo declarado también en `casc.yaml` (antes se perdía al reiniciar); 8 etapas (ADR-0014); verificación efímera y despliegue persistente en `localhost:8090` con rollback automático; SonarQube local con Quality Gate sobre todo el código (ADR-0015).
- **Corregido:**
  - Ruta errónea a `smoke-test.sh` en los scripts de Jenkins (un nivel de directorio de menos): falló el build #5 y se corrigió.
  - Un commit no se hizo por mezclar `-a` con rutas en `git commit`; el build #6 corrió el código anterior. Se detectó al revisar el log y se repitió.
  - La prueba de humo habría impreso el JWT en el log de Jenkins (`::add-mask::` solo funciona en GitHub Actions): se limitó a GitHub Actions.
- **Medido:** el primer análisis de SonarQube encontró 1 bug real (regex de `Permission` con `StackOverflowError` ante entradas largas, reproducido con una prueba) y 2 vulnerabilidades revisadas y justificadas en el código. Build #13 sobre `master`: 8 etapas en verde, Quality Gate con 0 bugs, 0 vulnerabilidades, rating A y 94,2 % de cobertura; rollback probado desplegando una imagen inválida.
- **Tiempo:** [__ h] · **sin IA (est.):** [__ h]

### #12 — Prompts v2

- **Origen:** los prompts 001–005 eran copia de los del docente y estaban desfasados (ArchUnit, "seguridad no implementada", flujo de una sola rama); `006-cicd.md` tenía 911 líneas con un parche incrustado.
- **Aceptado:** `docs/prompts/prompts.md` como índice corto con la estructura común (rol, fuentes de verdad, objetivo verificable, qué no hacer, plan con aprobación, pasos, verificación, informe final, archivo de sesión); siete comandos en `.claude/commands/` (`/business-rules`, `/implement-usecase`, `/tests`, `/review`, `/sonar-fix`, `/document`, `/pipeline`); archivos de sesión en `docs/sessions/`; prompts 001–006 conservados como histórico.
- **Corregido:** [__]
- **Tiempo:** [__ h] · **sin IA (est.):** [__ h]

## Métricas del proyecto

| Métrica | Valor | Fuente |
|---|---|---|
| Pruebas | 362 ejecutadas, 0 fallos (163 domain, 46 application, 153 infrastructure) | `./mvnw clean verify`, 2026-11-12 |
| Cobertura de líneas | 95,3 % / 96,7 % / 96,3 % (domain / application / infrastructure) | JaCoCo, 2026-11-12 |
| Cobertura en la primera revisión | 94,2 % / 90,0 % / 92,7 % | `docs/reviews/001-review-01-result.md` |
| Duración del pipeline | 1 min 55 s – 3 min 11 s | GitHub Actions |
| Issues de Sonar | 1 bug y 2 vulnerabilidades en el primer análisis → 0 y 0 (Quality Gate en verde) | SonarQube local, Jenkins #10 y #11 (2026-11-12) |
| Hallazgos de la revisión con IA | 20 (1 alto, 6 medios, 13 bajos) | revisión 001 |
| Errores de la IA corregidos y registrados | 14 (entradas 2, 6, 7, 8 y 9) | esta bitácora |

## Lecciones

- La IA rinde más con contexto versionado (`CLAUDE.md`, reglas de negocio con ID, ADRs) que con prompts sueltos.
- Lo que la IA afirma sobre el código se verifica leyendo el código y ejecutando el build, no buscando coincidencias de texto.
- Cuando un prompt contradice la documentación, se corrige el prompt.
- Lo que no está definido en `business-rules.md` va a "Preguntas abiertas", no al código.
