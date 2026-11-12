# Bitácora de IA

Registro del uso de asistentes de IA en `contract-api`: qué se pidió, qué se aceptó, qué hubo que corregir y cuánto tiempo tomó. Es la fuente de los datos de productividad de la presentación.

- **Asistente principal:** Claude Code (con `CLAUDE.md`, `docs/architecture.md` y `docs/business-rules.md` como contexto).
- **Flujo:** Explore → Plan → Code → Commit. Cada cambio se acepta solo con `./mvnw clean verify` en verde.
- **Prompts versionados:** `docs/prompts/NN-nombre-VV.md` (orden y versión; índice en `docs/prompts/README.md`), cada uno con su comando de Claude Code en `.claude/commands/`. Los prompts `001`–`006` del curso están en `docs/prompts/historico/`. El progreso de cada prompt queda en `docs/sessions/`.
- **Regla:** nunca se pegan secretos ni datos reales; las credenciales de los ejemplos son de prueba.

> Los campos `[__]` son tiempos que solo el autor puede medir: complétalos con tus valores reales. El resto de los datos sale del repositorio (revisiones, diseño, historial y builds).

## Resumen

| # | Fecha | Tarea | Prompt | Con IA | Sin IA (est.) | Errores de la IA corregidos |
|---|---|---|---|---|---|---|
| 1 | [__] | Diseño de arquitectura y modelo de dominio | `docs/prompts/README.md` (antes `README.md`) §1, primera versión | [__ h] | [__ h] | [__] |
| 2 | 2026-09-26 | Revisión integral del repositorio | `historico/16-revisar-arquitectura-01.md` | [__ h] | [__ h] | 3 |
| 3 | 2026-10-03 | Refactor del dominio por agregado | `historico/06-refactorizar-01.md` | [__ h] | [__ h] | [__] |
| 4 | 2026-10-03 | Diseño de la seguridad (3A) | `historico/07-implementar-seguridad-01.md` | [__ h] | [__ h] | [__] |
| 5 | [__] | Casos de uso de empleados | `historico/02-implementar-caso-uso-01.md` | [__ h] | [__ h] | [__] |
| 6 | 2026-10-16 | Seguridad fase 1: tokens, login, `AuthController` | `historico/07-implementar-seguridad-02.md` | [__ h] | [__ h] | 3 |
| 7 | 2026-10-24 | CI/CD, Docker y SonarCloud | `historico/21-cambiar-pipeline-01.md` | [__ h] | [__ h] | 3 |
| 8 | 2026-10-24 | Autorización con `@PreAuthorize` y colección Postman | sesión Claude Code | [__ h] | [__ h] | 3 |
| 9 | 2026-10-24 | Presentación final | sesión Claude Code | [__ h] | [__ h] | 2 |
| 10 | 2026-11-11 | GitHub Flow, despliegue con aprobación y Render (PR #1, #2) | sesión Claude Code | [__ h] | [__ h] | 1 |
| 11 | 2026-11-12 | Jenkins por etapas, despliegue local y SonarQube (PR #3–#5) | sesión Claude Code | [__ h] | [__ h] | 3 |
| 12 | 2026-11-12 | Prompts v2: comandos de Claude Code ordenados, ciclo de revisión y archivos de sesión | sesión Claude Code | [__ h] | [__ h] | 3 |
| | | **Total** | | **[__ h]** | **[__ h]** | **[__]** |

## Entradas

### #1 — Diseño de arquitectura y modelo de dominio

- **Prompt:** plantilla "Diseño de arquitectura" de la primera versión de `docs/prompts/README.md` §1 (rol de arquitecto, contexto de negocio, entregables); se puede ver en el historial de git.
- **Aceptado:** módulos `domain` / `application` / `infrastructure`, agregados `Contract` y `Employee`, catálogo de reglas `BR-XXX-NNN` y de errores, ADR-0001 a ADR-0003.
- **Corregido:** [__]
- **Tiempo:** [__ h] · **sin IA (est.):** [__ h]

### #2 — Revisión integral (`historico/16-revisar-arquitectura-01.md`)

- **Resultado:** `docs/reviews/001-16-arquitectura.md`. 20 hallazgos: 1 alto, 6 medios, 13 bajos. Build en verde (dominio 94,2 % de líneas).
- **Aceptado:** trazabilidad de reglas de negocio contra el código y las pruebas, plan de acción priorizado.
- **Corregido:**
  - El prompt de revisión contradecía dos ADRs vigentes: se corrigió el prompt, no el código.
  - La documentación exigía 80 % de cobertura, pero JaCoCo no estaba en el `pom.xml`: se añadió el plugin con el umbral.
  - Hallazgo alto H-01: Hibernate registra el DNI al violar la clave única. **Pendiente** de corregir.
- **Tiempo:** [__ h] · **sin IA (est.):** [__ h]

### #3 — Refactor del dominio (`historico/06-refactorizar-01.md`)

- **Aceptado:** paquetes por agregado con `common` y `shared` (ADR-0004), `Guard` y excepciones de dominio en lugar de `NullPointerException`, `Clock` como parámetro en vez de `now()`.
- **Corregido:** [__]
- **Tiempo:** [__ h] · **sin IA (est.):** [__ h]

### #4 — Diseño de la seguridad (`historico/07-implementar-seguridad-01.md`)

- **Aceptado:** `docs/design/003-security-design.md` y ADR-0005: JWT RS256 propio, refresh token opaco con rotación y detección de reutilización, RBAC por permisos, implementación por fases.
- **Corregido:** [__]
- **Tiempo:** [__ h] · **sin IA (est.):** [__ h]

### #5 — Casos de uso de empleados (`historico/02-implementar-caso-uso-01.md`)

- **Aceptado:** `RegisterEmployee`, `UpdateEmployee`, `GetEmployee` y `ListEmployees` siguiendo el orden dominio → puerto → caso de uso → adaptador → controller → pruebas.
- **Corregido:** [__]
- **Tiempo:** [__ h] · **sin IA (est.):** [__ h]

### #6 — Seguridad fase 1 (`historico/07-implementar-seguridad-02.md`)

- **Aceptado:** `TokenIssuerAdapter`, login, refresh, logout, `/auth/me`, creación del administrador inicial.
- **Corregido** (desviaciones D6 y D9 del diseño 003):
  - BCrypt solo compara los primeros 72 bytes: se rechazan contraseñas más largas antes de autenticar.
  - `InvalidRefreshTokenException` respondía 422 y `UnauthenticatedException` 500: ambas pasan a 401.
  - La creación de usuarios quedaba abierta a cualquier autenticado: regla de URL temporal con `user:manage`.
- **Tiempo:** [__ h] · **sin IA (est.):** [__ h]

### #7 — CI/CD (`historico/21-cambiar-pipeline-01.md`)

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

- **Origen:** los prompts 001–005 eran copia de los del docente y estaban desfasados (ArchUnit, "seguridad no implementada", flujo de una sola rama); `historico/21-cambiar-pipeline-01.md` tenía 911 líneas con un parche incrustado. Se combinaron las prácticas del docente (sesiones 01–05), la serie de revisión de los modelos del curso (`use-case-ca-v1/prompts` 01–07), los prompts de F1 y las presentaciones de Juan José, Ángel y Jairo (tabla en `docs/prompts/README.md` §3).
- **Aceptado:** `docs/prompts/README.md` como índice corto con la estructura común (rol, fuentes de verdad, objetivo verificable, qué no hacer, plan con aprobación, pasos, verificación, informe final, archivo de sesión); 20 prompts en `docs/prompts/` con nombre `NN-nombre-VV.md` (orden y versión) y sin duplicados: desarrollo (`00-refinar-requisitos` a `06-refactorizar`), ciclo de revisión (`09-reglas-revision`, `10-revisar-dominio` a `16-revisar-arquitectura`, `17-corregir-hallazgos` como único que modifica código y `18-revision-final` con `FINAL STATUS: READY`/`NOT READY`) y calidad, operación y prompts (`20-corregir-sonar`, `21-cambiar-pipeline`, `22-crear-o-mejorar-prompt`); comandos de Claude Code de tres líneas que solo cargan el prompt vigente; archivos de sesión en `docs/sessions/`; prompts 001–006 movidos a `docs/prompts/historico/`.
- **Corregido:**
  - La primera versión no revisó los prompts de los modelos del curso ni los de F1; se detectó al pedirle a la IA que verificara sus fuentes y se incorporaron (revisión por capa, corrección separada, revisión final con veredicto).
  - Un reemplazo masivo de nombres de comandos cambió también la ruta `docs/business-rules.md` en todos los comandos; se detectó al revisar el resultado y se revirtió.
  - La segunda versión guardaba el contenido en `.claude/commands/` (carpeta oculta en el IDE) y no tenía versión en el nombre; el contenido pasó a `docs/prompts/NN-nombre-VV.md` y los comandos quedaron como lanzadores.
  - Se revisaron los 60 archivos de prompts de CURS-000617 (18 distintos) y las transcripciones de las sesiones 01–05; se incorporó el prompt DevOps de `CURS-000617-CICD-v2`, que no se había leído.
  - El endpoint de *hotspots* de SonarQube usa `project`, no `projectKey`; se comprobó contra el SonarQube local.
- **Tiempo:** [__ h] · **sin IA (est.):** [__ h]

## Métricas del proyecto

| Métrica | Valor | Fuente |
|---|---|---|
| Pruebas | 362 ejecutadas, 0 fallos (163 domain, 46 application, 153 infrastructure) | `./mvnw clean verify`, 2026-11-12 |
| Cobertura de líneas | 95,3 % / 96,7 % / 96,3 % (domain / application / infrastructure) | JaCoCo, 2026-11-12 |
| Cobertura en la primera revisión | 94,2 % / 90,0 % / 92,7 % | `docs/reviews/001-16-arquitectura.md` |
| Duración del pipeline | 1 min 55 s – 3 min 11 s | GitHub Actions |
| Issues de Sonar | 1 bug y 2 vulnerabilidades en el primer análisis → 0 y 0 (Quality Gate en verde) | SonarQube local, Jenkins #10 y #11 (2026-11-12) |
| Hallazgos de la revisión con IA | 20 (1 alto, 6 medios, 13 bajos) | revisión 001 |
| Errores de la IA corregidos y registrados | 14 (entradas 2, 6, 7, 8 y 9) | esta bitácora |

## Lecciones

- La IA rinde más con contexto versionado (`CLAUDE.md`, reglas de negocio con ID, ADRs) que con prompts sueltos.
- Lo que la IA afirma sobre el código se verifica leyendo el código y ejecutando el build, no buscando coincidencias de texto.
- Cuando un prompt contradice la documentación, se corrige el prompt.
- Lo que no está definido en `business-rules.md` va a "Preguntas abiertas", no al código.
