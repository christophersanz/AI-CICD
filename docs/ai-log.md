# Bitácora de IA

Registro del uso de asistentes de IA en `contract-api`: qué se pidió, qué se aceptó, qué hubo que corregir y cuánto tiempo tomó. Es la fuente de los datos de productividad de la presentación.

- **Asistente principal:** Claude Code (con `CLAUDE.md`, `docs/architecture.md` y `docs/business-rules.md` como contexto).
- **Flujo:** Explore → Plan → Code → Commit. Cada cambio se acepta solo con `./mvnw clean verify` en verde.
- **Prompts versionados:** `docs/prompts/` (los `004` y `005` se usan como comandos personalizados).
- **Regla:** nunca se pegan secretos ni datos reales; las credenciales de los ejemplos son de prueba.

> Los campos `[__]` son tiempos que solo el autor puede medir: complétalos con tus valores reales. El resto de los datos sale del repositorio (revisiones, diseño, historial y builds).

## Resumen

| # | Fecha | Tarea | Prompt | Con IA | Sin IA (est.) | Errores de la IA corregidos |
|---|---|---|---|---|---|---|
| 1 | [__] | Diseño de arquitectura y modelo de dominio | `prompts.md` §1 | [__ h] | [__ h] | [__] |
| 2 | 2026-09-26 | Revisión integral del repositorio | `001-review-01.md` | [__ h] | [__ h] | 3 |
| 3 | 2026-10-03 | Refactor del dominio por agregado | `002.-refactoring-v1.md` | [__ h] | [__ h] | [__] |
| 4 | 2026-10-03 | Diseño de la seguridad (3A) | `003-security-01.md` | [__ h] | [__ h] | [__] |
| 5 | [__] | Casos de uso de empleados | `004-implement-usecase.md` | [__ h] | [__ h] | [__] |
| 6 | 2026-10-16 | Seguridad fase 1: tokens, login, `AuthController` | `005-security-02.md` | [__ h] | [__ h] | 3 |
| 7 | 2026-10-24 | CI/CD, Docker y SonarCloud | `006-cicd.md` | [__ h] | [__ h] | 3 |
| 8 | 2026-10-24 | Autorización con `@PreAuthorize` y colección Postman | sesión Claude Code | [__ h] | [__ h] | 3 |
| 9 | 2026-10-24 | Presentación final | sesión Claude Code | [__ h] | [__ h] | 2 |
| | | **Total** | | **[__ h]** | **[__ h]** | **[__]** |

## Entradas

### #1 — Diseño de arquitectura y modelo de dominio

- **Prompt:** plantilla de `docs/prompts/prompts.md` §1 (rol de arquitecto, contexto de negocio, entregables).
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

## Métricas del proyecto

| Métrica | Valor | Fuente |
|---|---|---|
| Pruebas | 355 ejecutadas, 0 fallos | `./mvnw clean verify`, 2026-10-24 |
| Cobertura de líneas | 95,3 % / 96,7 % / 96,5 % (domain / application / infrastructure) | JaCoCo |
| Cobertura en la primera revisión | 94,2 % / 90,0 % / 92,7 % | `docs/reviews/001-review-01-result.md` |
| Duración del pipeline | 1 min 55 s – 3 min 11 s | GitHub Actions |
| Issues de Sonar | [__] — el análisis requiere el secreto `SONAR_TOKEN` | SonarCloud |
| Hallazgos de la revisión con IA | 20 (1 alto, 6 medios, 13 bajos) | revisión 001 |
| Errores de la IA corregidos y registrados | 14 (entradas 2, 6, 7, 8 y 9) | esta bitácora |

## Lecciones

- La IA rinde más con contexto versionado (`CLAUDE.md`, reglas de negocio con ID, ADRs) que con prompts sueltos.
- Lo que la IA afirma sobre el código se verifica leyendo el código y ejecutando el build, no buscando coincidencias de texto.
- Cuando un prompt contradice la documentación, se corrige el prompt.
- Lo que no está definido en `business-rules.md` va a "Preguntas abiertas", no al código.
