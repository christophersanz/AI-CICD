# Bitácora de IA

Registro del uso de asistentes de IA en `contract-api`: qué se pidió, qué se aceptó, qué hubo que corregir y cuánto tiempo tomó. Es la fuente de los datos de productividad de la presentación.

- **Asistente principal:** Claude Code (con `CLAUDE.md`, `docs/architecture.md` y `docs/business-rules.md` como contexto).
- **Flujo:** Explore → Plan → Code → Commit. Cada cambio se acepta solo con `./mvnw clean verify` en verde.
- **Prompts versionados en git:** `docs/prompts/NN-nombre.md` (un archivo por prompt; índice en `docs/prompts/README.md`), cada uno con su comando de Claude Code en `.claude/commands/`. Los prompts `001`–`006` que se usaron en el curso se consultan en el commit [`33f74fc`](https://github.com/christophersanz/AI-CICD/tree/33f74fc/docs/prompts). El progreso de cada prompt queda en `docs/sessions/`.
- **Regla:** nunca se pegan secretos ni datos reales; las credenciales de los ejemplos son de prueba.

> **Tiempos (≈): son estimaciones, no mediciones.** Las calculó Claude el 2026-11-14, a pedido del autor, y el autor puede ajustarlas.
> - **Con IA:** duración aproximada de cada tarea, según los registros de las sesiones de Claude Code (desde el 2026-10-16) y el historial de git. Las #1 a #5 son anteriores a esos registros y se estimaron por su alcance. Los registros no permiten medir con exactitud: anotan los eventos en bloques y no separan el trabajo de la IA del tiempo de lectura y aprobación del autor.
> - **Sin IA:** estimación del tiempo que habría tomado hacer lo mismo a mano, según el alcance verificable de cada entrada (archivos, pruebas e informes).
>
> Los conteos de errores que siguen en `[__]` (#1 y #3 a #6) no se pueden estimar sin el registro de esas sesiones.

## Resumen

| # | Fecha | Tarea | Prompt | Con IA | Sin IA (est.) | Errores de la IA corregidos |
|---|---|---|---|---|---|---|
| 1 | [__] | Diseño de arquitectura y modelo de dominio | `docs/prompts/README.md` (antes `README.md`) §1, primera versión | ≈2 h | ≈8 h | [__] |
| 2 | 2026-09-26 | Revisión integral del repositorio | [`001-review-01.md`](https://github.com/christophersanz/AI-CICD/blob/33f74fc/docs/prompts/001-review-01.md) | ≈1 h | ≈6 h | 3 |
| 3 | 2026-10-03 | Refactor del dominio por agregado | [`002.-refactoring-v1.md`](https://github.com/christophersanz/AI-CICD/blob/33f74fc/docs/prompts/002.-refactoring-v1.md) | ≈1,5 h | ≈6 h | [__] |
| 4 | 2026-10-03 | Diseño de la seguridad (3A) | [`003-security-01.md`](https://github.com/christophersanz/AI-CICD/blob/33f74fc/docs/prompts/003-security-01.md) | ≈1,5 h | ≈8 h | [__] |
| 5 | [__] | Casos de uso de empleados | [`004-implement-usecase.md`](https://github.com/christophersanz/AI-CICD/blob/33f74fc/docs/prompts/004-implement-usecase.md) | ≈3 h | ≈12 h | [__] |
| 6 | 2026-10-16 | Seguridad fase 1: tokens, login, `AuthController` | [`005-security-02.md`](https://github.com/christophersanz/AI-CICD/blob/33f74fc/docs/prompts/005-security-02.md) | ≈1,5 h | ≈10 h | 3 |
| 7 | 2026-10-24 | CI/CD, Docker y SonarCloud | [`006-cicd.md`](https://github.com/christophersanz/AI-CICD/blob/33f74fc/docs/prompts/006-cicd.md) | ≈2,5 h | ≈10 h | 3 |
| 8 | 2026-10-24 | Autorización con `@PreAuthorize` y colección Postman | sesión Claude Code | ≈1,5 h | ≈6 h | 3 |
| 9 | 2026-10-24 | Presentación final | sesión Claude Code | ≈1 h | ≈4 h | 2 |
| 10 | 2026-11-11 | GitHub Flow, despliegue con aprobación y Render (PR #1, #2) | sesión Claude Code | ≈2 h | ≈6 h | 1 |
| 11 | 2026-11-12 | Jenkins por etapas, despliegue local y SonarQube (PR #3–#5) | sesión Claude Code | ≈2 h | ≈6 h | 3 |
| 12 | 2026-11-12 | Prompts v2: 9 prompts genéricos ordenados, kit reutilizable y archivos de sesión | sesión Claude Code | ≈2,5 h | ≈10 h | 3 |
| 13 | 2026-11-12 | Ronda de revisión 002 con los prompts nuevos (`/06-revisar`, `/07-corregir`) | `06-revisar.md`, `07-corregir.md` | ≈1 h | ≈8 h | 3 |
| 14 | 2026-11-12 | Evaluación y corrección de los 9 prompts (confiabilidad, buenas prácticas, reutilización) | `09-mejorar-prompt.md` | ≈1 h | ≈4 h | 3 |
| 15 | 2026-11-12 | Pruebas de `ContractPeriod` con `/03-pruebas` (primera ejecución del prompt) | `03-pruebas.md` | ≈1 h | ≈3 h | 0 |
| 16 | 2026-11-13 | JavaDoc de `domain` y `application` con `/04-documentar javadoc` (primera ejecución del prompt) | `04-documentar.md` | ≈1 h | ≈6 h | 3 |
| 17 | 2026-11-13 | Volver Jenkins a `master` con un build tras probar una rama (guía, README y prompts 07/08) | — | ≈0,5 h | ≈1,5 h | 1 |
| 18 | 2026-11-13 | Eliminar el parseo de texto sin uso de `ContractId` y `EmployeeId` con `/05-refactorizar` (primera ejecución del prompt) | `05-refactorizar.md` | ≈1 h | ≈2 h | 2 |
| 19 | 2026-11-13 | Kit reutilizable en proyectos con documentos propios (prueba sobre el modelo del curso) | `09-mejorar-prompt.md` | ≈1 h | ≈4 h | 4 |
| 20 | 2026-11-13 | Quitar Render: el despliegue es solo con Docker (ADR-0016) | — | ≈0,5 h | ≈2 h | 0 |
| 21 | 2026-11-13 | `/06-revisar dominio` completo en el modelo del curso (segundo proyecto) | `06-revisar.md` | ≈1 h | ≈3 h | 1 |
| | | **Total** | | **≈30 h** | **≈125,5 h** | **≥ 35** (sin contar #1 y #3–#6) |

## Entradas

### #1 — Diseño de arquitectura y modelo de dominio

- **Prompt:** plantilla "Diseño de arquitectura" de la primera versión de `docs/prompts/README.md` §1 (rol de arquitecto, contexto de negocio, entregables); se puede ver en el historial de git.
- **Aceptado:** módulos `domain` / `application` / `infrastructure`, agregados `Contract` y `Employee`, catálogo de reglas `BR-XXX-NNN` y de errores, ADR-0001 a ADR-0003.
- **Corregido:** [__]
- **Tiempo:** ≈2 h · **sin IA (est.):** ≈8 h

### #2 — Revisión integral ([`001-review-01.md`](https://github.com/christophersanz/AI-CICD/blob/33f74fc/docs/prompts/001-review-01.md))

- **Resultado:** `docs/reviews/2026-09-26-ronda-001/07-revision-integral.md`. 20 hallazgos: 1 alto, 6 medios, 13 bajos. Build en verde (dominio 94,2 % de líneas).
- **Aceptado:** trazabilidad de reglas de negocio contra el código y las pruebas, plan de acción priorizado.
- **Corregido:**
  - El prompt de revisión contradecía dos ADRs vigentes: se corrigió el prompt, no el código.
  - La documentación exigía 80 % de cobertura, pero JaCoCo no estaba en el `pom.xml`: se añadió el plugin con el umbral.
  - Hallazgo alto H-01: Hibernate registra el DNI al violar la clave única. **Pendiente** de corregir.
- **Tiempo:** ≈1 h · **sin IA (est.):** ≈6 h

### #3 — Refactor del dominio ([`002.-refactoring-v1.md`](https://github.com/christophersanz/AI-CICD/blob/33f74fc/docs/prompts/002.-refactoring-v1.md))

- **Aceptado:** paquetes por agregado con `common` y `shared` (ADR-0004), `Guard` y excepciones de dominio en lugar de `NullPointerException`, `Clock` como parámetro en vez de `now()`.
- **Corregido:** [__]
- **Tiempo:** ≈1,5 h · **sin IA (est.):** ≈6 h

### #4 — Diseño de la seguridad ([`003-security-01.md`](https://github.com/christophersanz/AI-CICD/blob/33f74fc/docs/prompts/003-security-01.md))

- **Aceptado:** `docs/design/seguridad.md` y ADR-0005: JWT RS256 propio, refresh token opaco con rotación y detección de reutilización, RBAC por permisos, implementación por fases.
- **Corregido:** [__]
- **Tiempo:** ≈1,5 h · **sin IA (est.):** ≈8 h

### #5 — Casos de uso de empleados ([`004-implement-usecase.md`](https://github.com/christophersanz/AI-CICD/blob/33f74fc/docs/prompts/004-implement-usecase.md))

- **Aceptado:** `RegisterEmployee`, `UpdateEmployee`, `GetEmployee` y `ListEmployees` siguiendo el orden dominio → puerto → caso de uso → adaptador → controller → pruebas.
- **Corregido:** [__]
- **Tiempo:** ≈3 h · **sin IA (est.):** ≈12 h

### #6 — Seguridad fase 1 ([`005-security-02.md`](https://github.com/christophersanz/AI-CICD/blob/33f74fc/docs/prompts/005-security-02.md))

- **Aceptado:** `TokenIssuerAdapter`, login, refresh, logout, `/auth/me`, creación del administrador inicial.
- **Corregido** (desviaciones D6 y D9 del diseño 003):
  - BCrypt solo compara los primeros 72 bytes: se rechazan contraseñas más largas antes de autenticar.
  - `InvalidRefreshTokenException` respondía 422 y `UnauthenticatedException` 500: ambas pasan a 401.
  - La creación de usuarios quedaba abierta a cualquier autenticado: regla de URL temporal con `user:manage`.
- **Tiempo:** ≈1,5 h · **sin IA (est.):** ≈10 h

### #7 — CI/CD ([`006-cicd.md`](https://github.com/christophersanz/AI-CICD/blob/33f74fc/docs/prompts/006-cicd.md))

- **Aceptado:** workflow de GitHub Actions, SonarCloud (ADR-0011), imagen en GHCR, API en `docker compose`, `.env.example`, Jenkins local.
- **Corregido:**
  - El workflow escuchaba `main` y el repositorio usa `master`: nunca se habría ejecutado.
  - Faltaba `.mvn/wrapper/maven-wrapper.properties`: `./mvnw` y el `Dockerfile` fallaban.
  - `mvnw` estaba en git sin permiso de ejecución.
- **Medido:** últimas 5 ejecuciones del pipeline en verde, entre 1 min 55 s y 3 min 11 s.
- **Tiempo:** ≈2,5 h · **sin IA (est.):** ≈10 h

### #8 — Autorización con `@PreAuthorize` y colección Postman

- **Aceptado:** `@EnableMethodSecurity`; `@PreAuthorize` con `employee:create`, `employee:update`, `employee:read` y `user:manage`; migración `V5` con `employee:read`; 403 `AUTH-001` desde el controller; 8 pruebas nuevas (403 por permiso y `@WithMockUser`); colección `postman/` (hoy en `docs/api/postman/`) con 19 peticiones.
- **Corregido:**
  - La IA dio por implementado `@PreAuthorize` al contar coincidencias que en realidad estaban en comentarios TODO. Se detectó al leer el código.
  - Un `AccessDeniedException` lanzado por `@PreAuthorize` habría llegado al manejador genérico y respondido 500: se añadió su traducción a 403.
  - El paso de la demo "EMPLOYEE → GET /employees → 403" era falso con el código de entonces: se cambió.
- **Medido:** `./mvnw clean verify` en verde con 355 pruebas y 0 fallos; cobertura de líneas 95,3 % (domain), 96,7 % (application), 96,5 % (infrastructure). Colección ejecutada con newman contra una base limpia: 41/41 aserciones.
- **Tiempo:** ≈1,5 h · **sin IA (est.):** ≈6 h

### #9 — Presentación final

- **Aceptado:** 16 diapositivas con caso de negocio, diagrama C4, diagrama de secuencia, códigos HTTP, demo, seguridad, pruebas, CI/CD y lecciones; cifras tomadas del repositorio y de GitHub Actions.
- **Corregido:**
  - Afirmaciones de autorización que no coincidían con el código (ver #8).
  - Datos de productividad: la IA no los inventa; quedan como campos a completar.
- **Tiempo:** ≈1 h · **sin IA (est.):** ≈4 h

### #10 — GitHub Flow, despliegue con aprobación y Render

- **Origen:** comparación con el repositorio del docente (`GalaxyTraining/CURS-000617-CICD`, ruleset `main-protegida`) y con las presentaciones de la sesión 05, donde el único despliegue real fue on-premise.
- **Aceptado:** ruleset `master-protegida` con PR obligatorio, checks requeridos, solo *squash* y rama al día (ADR-0012); environment `production` con revisor obligatorio; job de verificación efímera con prueba de humo compartida (`.github/scripts/smoke-test.sh`); despliegue en Render desde Docker Hub por sha con `render.yaml` (ADR-0013).
- **Corregido:** la primera aprobación de `production` no quedó registrada en GitHub (diálogo cerrado antes de confirmar); se detectó consultando la API y se repitió.
- **Medido:** run `43076800725` con los tres jobs en verde; despliegue efímero de 31 s con login, `/auth/me` y `/employees` (200) y 401 sin token. Render queda pendiente de configurar (job *skipped*).
- **Tiempo:** ≈2 h · **sin IA (est.):** ≈6 h

### #11 — Jenkins por etapas, despliegue local y SonarQube

- **Origen:** el pipeline de Jenkins de un compañero mostraba cada paso en el *Stage View*, pasaba un Quality Gate de SonarQube y terminaba desplegado; el nuestro tenía 2 etapas y no desplegaba.
- **Aceptado:** sondeo declarado también en `casc.yaml` (antes se perdía al reiniciar); 8 etapas (ADR-0014); verificación efímera y despliegue persistente en `localhost:8090` con rollback automático; SonarQube local con Quality Gate sobre todo el código (ADR-0015).
- **Corregido:**
  - Ruta errónea a `smoke-test.sh` en los scripts de Jenkins (un nivel de directorio de menos): falló el build #5 y se corrigió.
  - Un commit no se hizo por mezclar `-a` con rutas en `git commit`; el build #6 corrió el código anterior. Se detectó al revisar el log y se repitió.
  - La prueba de humo habría impreso el JWT en el log de Jenkins (`::add-mask::` solo funciona en GitHub Actions): se limitó a GitHub Actions.
- **Medido:** el primer análisis de SonarQube encontró 1 bug real (regex de `Permission` con `StackOverflowError` ante entradas largas, reproducido con una prueba) y 2 vulnerabilidades revisadas y justificadas en el código. Build #13 sobre `master`: 8 etapas en verde, Quality Gate con 0 bugs, 0 vulnerabilidades, rating A y 94,2 % de cobertura; rollback probado desplegando una imagen inválida.
- **Tiempo:** ≈2 h · **sin IA (est.):** ≈6 h

### #12 — Prompts v2

- **Origen:** los prompts 001–005 eran copia de los del docente y estaban desfasados (ArchUnit, "seguridad no implementada", flujo de una sola rama); [`006-cicd.md`](https://github.com/christophersanz/AI-CICD/blob/33f74fc/docs/prompts/006-cicd.md) tenía 911 líneas con un parche incrustado. Se combinaron las prácticas del docente (sesiones 01–05), la serie de revisión de los modelos del curso (`use-case-ca-v1/prompts` 01–07), los prompts de F1 y las presentaciones de Juan José, Ángel y Jairo (tabla en `docs/prompts/README.md` §3).
- **Aceptado:** `docs/prompts/README.md` como índice corto con la estructura común (rol, fuentes de verdad, objetivo verificable, qué no hacer, plan con aprobación, pasos, verificación, informe final, archivo de sesión); 9 prompts genéricos en `docs/prompts/` (`01-definir`, `02-implementar-caso-uso`, `03-pruebas`, `04-documentar`, `05-refactorizar`, `06-revisar` con modos por capa en orden, PR y final `READY`/`NOT READY`, `07-corregir` para rondas de revisión y Quality Gate, `08-cambiar-pipeline`, `09-mejorar-prompt`), un archivo por prompt y comandos de Claude Code de tres líneas que solo cargan ese archivo; archivos de sesión en `docs/sessions/`; datos propios del proyecto en `CLAUDE.md` §5 (*Datos para los prompts*) y kit para otros proyectos en `docs/prompts/kit/` (plantillas e `instalar.sh`); los prompts 001–006 del curso quedan en el historial de git.
- **Corregido:**
  - La primera versión no revisó los prompts de los modelos del curso ni los de F1; se detectó al pedirle a la IA que verificara sus fuentes y se incorporaron (revisión por capa, corrección separada, revisión final con veredicto).
  - Un reemplazo masivo de nombres de comandos cambió también la ruta `docs/business-rules.md` en todos los comandos; se detectó al revisar el resultado y se revirtió.
  - La segunda versión guardaba el contenido en `.claude/commands/` (carpeta oculta en el IDE); el contenido pasó a `docs/prompts/` y los comandos quedaron como lanzadores.
  - Se probó un esquema con versiones en el nombre (`-01`, `-02`) y carpeta `historico/`; se descartó a pedido del autor por duplicar archivos: un solo archivo por prompt y la historia en git.
  - Llegaron a ser 20 prompts (un comando por capa revisada, como la serie de los modelos del curso); se redujeron a 9 juntando actividades iguales: requisitos y reglas en `01-definir`, las revisiones por capa, de PR y final en `06-revisar` (con modos y el mismo orden), y las correcciones de revisión y de SonarQube en `07-corregir`.
  - Los prompts tenían datos de `contract-api` escritos dentro (paquete, agregados, reglas, ADR, Jenkins, Render, puertos); se movieron a `CLAUDE.md` §5 como claves `DP.*` para reutilizarlos en otros proyectos.
  - Se revisaron los 60 archivos de prompts de CURS-000617 (18 distintos) y las transcripciones de las sesiones 01–05; se incorporó el prompt DevOps de `CURS-000617-CICD-v2`, que no se había leído.
  - El endpoint de *hotspots* de SonarQube usa `project`, no `projectKey`; se comprobó contra el SonarQube local.
- **Tiempo:** ≈2,5 h · **sin IA (est.):** ≈10 h

### #13 — Ronda de revisión 002 con los prompts nuevos

- **Origen:** primera ejecución real de los prompts genéricos: `/06-revisar` en sus 7 modos por capa más `final`, y `/07-corregir ronda 002`. Informes en `docs/reviews/2026-11-12-ronda-002/01-revision-dominio.md` … `2026-11-12-ronda-002/09-veredicto-final.md`.
- **Aceptado:**
  - Revisión: 0 ERROR, 6 WARNING y 8 INFO. Uno de los WARNING detectó que el motivo del diferido de `@Version` de la ronda 001 ya no aplicaba.
  - Corrección con pruebas primero: `Contract.restore` valida invariantes estructurales con el código nuevo `CTR-009` → 500. La documentación quedó alineada (pendientes de `EmployeeStatus` y BR-USR-005, alcance de la autorización y agregado `identity`). `@Version` se difirió con el motivo actualizado, por decisión del autor.
  - Revisión final: `FINAL STATUS: READY`.
- **Corregido:**
  - Varias comprobaciones automáticas de la IA dieron falsos positivos y se descartaron al verificarlas: una prueba «faltante» que estaba en `LogoutAndCurrentUserServiceTest`, «claves privadas» en el historial que eran la descripción del formato PEM, y pruebas `@Nested` que Surefire reporta dentro de la clase externa.
  - Un conteo erróneo en el informe de CI/CD (9 scripts en lugar de 7) se corrigió antes de subirlo.
- **Medido:**
  - 362 → 373 pruebas en verde.
  - Cobertura de líneas de `domain` 95,3 % → 95,8 %.
  - Quality Gate de SonarQube: cobertura 94,2 % → 94,4 %, 0 bugs y 0 vulnerabilidades.
  - Jenkins #17 (rama) y #19 (`master`) en verde.
- **Tiempo:** ≈1 h · **sin IA (est.):** ≈8 h

### #14 — Evaluación de los 9 prompts

- **Origen:** evaluación de los 9 prompts con cuatro criterios (confiables, con buenas prácticas, profesionales y reutilizables), a partir de una lectura completa, de chequeos objetivos de estructura y referencias, y de lo aprendido al ejecutar `/06-revisar` y `/07-corregir` en la ronda 002.
- **Aceptado (11 correcciones, un commit por prompt):**
  - Archivos de sesión con números viejos en `05` (`06-refactorizar`) y `08` (`21-cambiar-pipeline`).
  - `09` se contradecía («nueva versión» frente a «un solo archivo») y su número de ejemplo chocaba con `07`.
  - Sección *Objetivo* en `01` y `06`.
  - `06` reutiliza el build dentro de una ronda y exige confirmar las coincidencias de búsquedas automáticas.
  - `02` decide qué hacer si el caso de uso ya existe y pide la prueba primero ante un bug.
  - `03` prohíbe debilitar pruebas existentes.
  - Rama y PR en `03`, `04` y `09`.
  - Clave `DP.stack` en lugar de «Java 21 / Spring Boot 3» en los roles.
  - `verificar.sh` detecta comandos citados que no existen y archivos de sesión con otro número.
- **Corregido:** los errores de referencias de `05` y `08` venían de la renumeración de 20 a 9 prompts. El `verificar.sh` anterior no los detectaba; el nuevo sí, y además se probó inyectándole un comando inexistente.
- **Medido:**
  - Las 10 partes de la estructura común están en los 9 prompts (`06` no crea archivo de sesión, por diseño).
  - 0 roles con el stack fijo.
  - `verificar.sh`: 0 errores en el proyecto y en una instalación limpia del kit.
- **Tiempo:** ≈1 h · **sin IA (est.):** ≈4 h

### #15 — Pruebas de `ContractPeriod` con `/03-pruebas`

- **Origen:** primera ejecución de `/03-pruebas`. El objetivo (`ContractPeriod`) lo eligió el autor entre las clases con más ramas sin cubrir según JaCoCo.
- **Aceptado:**
  - Diagnóstico: 6 pruebas de rechazo no verificaban el código de error; 3 pruebas tenían varias razones de fallo; el solapamiento con contratos indefinidos (BR-CTR-005), `extendTo(null)` y `equals`/`hashCode` no tenían cobertura.
  - Plan de 9 puntos aprobado: pruebas parametrizadas, un `@MethodSource` con pares de vigencias comprobados en ambos sentidos, `@Nested` por comportamiento y el contrato de igualdad.
  - Las 13 pruebas anteriores se conservaron con su intención y no se tocó el código de producción.
- **Corregido:** nada. El prompt se siguió sin desvíos.
- **Medido:**
  - 13 → 49 ejecuciones en `ContractPeriodTest`; total del proyecto 373 → 409.
  - `ContractPeriod`: líneas 97,3 % → 100 %, ramas 85,4 % → 100 %.
  - Ramas de `domain`: 90,3 % → 93,2 %.
  - 4 mutaciones temporales del código (solapamiento con indefinidos, fin exclusivo, igualdad sin fecha de fin, extensión a la misma fecha): las 4 detectadas y revertidas.
- **Tiempo:** ≈1 h · **sin IA (est.):** ≈3 h

### #16 — JavaDoc de `domain` y `application` con `/04-documentar javadoc`

- **Origen:** primera ejecución de `/04-documentar`. Sin alcance, el prompt hizo el inventario de los 5 tipos de documentación: OpenAPI, diagramas y guías estaban al día. El autor eligió `javadoc`, el punto débil que había observado el docente.
- **Aceptado:** el plan de 6 bloques (puertos de entrada, puertos de salida, excepciones, eventos, agregados y VO, DTO). Cada `@throws` indica su código del catálogo y su regla `BR-*`, y se escribió después de leer el código que lanza la excepción.
- **Corregido (errores de la IA detectados antes del commit):**
  1. «se redondea a 2 decimales» en `Salary` y `MinimumWage`: `Money` usa `RoundingMode.UNNECESSARY` y **rechaza** más de 2 decimales.
  2. «historial sin modificar» en `Contract.renewals()`: devuelve una copia inmutable (`List.copyOf`).
  3. Descripciones generadas por plantilla en los constructores de los servicios («puerto user (repositorio)»): se reescribieron a mano.
  - También se ajustaron 16 `{@code …}` partidos al final de línea y 5 líneas de más de 120 caracteres.
- **Medido:**
  - Tipos públicos con JavaDoc: 53/108 (49 %) → **108/108**. Métodos públicos: 30/182 → **todos**.
  - `javadoc -Xdoclint:all`: 353 comentarios faltantes → **0**, con 0 problemas de sintaxis, referencias o HTML.
  - 108 archivos cambian **solo en comentarios**, demostrado comparando el código sin comentarios contra `master`.
  - 409 pruebas en verde.
- **Tiempo:** ≈1 h · **sin IA (est.):** ≈6 h

### #17 — Jenkins dejaba de construir `master` después de probar una rama

- **Origen:** tras integrar los PR #15 y #16, Jenkins no construyó `master` en más de 10 minutos. El **Git Polling Log** decía `No changes`.
- **Causa raíz (error de la IA):** para probar el PR #15, la IA apuntó Jenkins a la rama del PR (build #23) y al terminar solo restauró `REPO_BRANCH=master`. En un job de Pipeline, el sondeo usa la configuración Git **del último build**, así que siguió sondeando la rama, ya borrada al integrar el PR.
- **Corregido:**
  - Build #24 lanzado a mano sobre `82d1e38`: las 9 etapas en SUCCESS y `contract-api:jenkins-24` desplegado en `localhost:8090`.
  - El procedimiento ahora exige un build sobre `master` al terminar. Está en el README de Jenkins (*Probar una rama antes de integrarla*), en la guía de CI/CD (con una fila nueva en *Problemas frecuentes*), en `DP.ci-local` y en los prompts `07-corregir` y `08-cambiar-pipeline`.
- **Tiempo:** ≈0,5 h · **sin IA (est.):** ≈1,5 h

### #18 — Parseo duplicado de IDs con `/05-refactorizar`

- **Origen:** primera ejecución de `/05-refactorizar`. Sin objetivo, el prompt buscó síntomas reales antes de preguntar: SonarQube daba 0 % de duplicación y 2 *code smells*. El autor eligió el parseo duplicado de `ContractId` y `EmployeeId`.
- **Diagnóstico que cambió la propuesta:** `of(String)` no tenía ningún uso en producción desde el primer commit. La API recibe `@PathVariable UUID` y Spring convierte el texto. Extraerlo a un helper habría sido sobreingeniería, así que se propuso **eliminarlo** y el autor lo aprobó.
- **Corregido (errores de la IA):**
  1. El JavaDoc escrito en #16 decía que `of(String)` servía «por ejemplo, para el ID de una ruta de la API»: era falso, y desapareció con el método.
  2. El diagnóstico omitió un uso en `ContractValueObjectsTest:33`. Se detectó antes de compilar.
- **Medido:**
  - −36 líneas de producción, contando el JavaDoc;
  - 409 pruebas antes y después, todas en verde;
  - 0 usos restantes de `of(String)`;
  - regla de dependencia cumplida.
- **Pendiente:** 94 bloques JavaDoc de #16 sin línea en blanco antes de los tags.
- **Tiempo:** ≈1 h · **sin IA (est.):** ≈2 h

### #19 — Prueba de reutilización del kit en el modelo del curso

- **Origen:** el autor preguntó si los prompts servían en cualquier proyecto Spring Boot. Se probó instalando el kit en una **copia** de `F4/01.-Modelos/use-case-ca-v1` (Sales Order). Ese proyecto tiene un solo módulo, sus documentos están en la raíz, sus reglas usan IDs `RN-NNN` y tiene sus propias secciones.
- **Encontrado:** `verificar.sh` daba 0 errores, pero el kit no habría funcionado:
  - el instalador creó plantillas vacías en `docs/`, y los prompts las habrían leído en lugar de los documentos reales;
  - las secciones citadas (§4, §8) no correspondían a las del proyecto;
  - 7 líneas asumían módulos separados;
  - 4 prompts citaban un `technical.md` que no existe.
- **Corregido:**
  - claves `DP.doc-arquitectura`, `doc-reglas`, `doc-tecnico` y `secciones`;
  - nota común en los 9 prompts y las líneas de módulos apoyadas en `DP.modulos`;
  - un instalador que detecta los documentos existentes;
  - un verificador que marca como error un documento inexistente, una plantilla sin completar o un wrapper de build que falta.
- **Errores de la IA corregidos:**
  1. Se afirmó antes que el kit servía «para cualquier proyecto Spring Boot» sin haberlo probado fuera de `contract-api`.
  2. El primer arreglo del instalador fallaba en el bash 3.2 de macOS (comillas invertidas y `[…]` dentro de `${x//…}`); se cambió a `awk`.
  3. Al completar los datos de prueba se escribió `./mvnw` en un proyecto sin wrapper; eso motivó la comprobación nueva.
  4. La mención «`DP.doc-*`» del README se leía como una clave inexistente.
- **Medido:**
  - modelo del curso, con `DP` completado: **0 errores y 0 avisos**; `contract-api`, también;
  - 4 casos negativos (documento faltante, plantilla, plantilla sobrante, wrapper faltante), todos detectados;
  - `--actualizar` conserva el `DP` del proyecto destino.
- **Pendiente:** ejecutar un prompt completo en el proyecto del profe; hasta ahora solo se probó la instalación y la verificación.
- **Tiempo:** ≈1 h · **sin IA (est.):** ≈4 h

### #20 — Despliegue solo con Docker (sin Render)

- **Origen:** el autor pidió eliminar todo lo relacionado con Render, porque el despliegue es con Docker.
- **Hallado antes de borrar:** en GitHub nunca se configuraron `RENDER_API_KEY`, `RENDER_SERVICE_ID` ni `RENDER_URL`. Por eso el job `Despliegue (production)` terminó siempre en *skipped*, y el único despliegue real era el de Jenkins (ADR-0014).
- **Hecho:**
  - se eliminaron `render.yaml`, `.github/scripts/render-deploy.sh` y el job `deploy` del workflow;
  - en la guía de CI/CD se quitaron §3.5, §4.1, §5.4 y 5 filas de *Problemas frecuentes*, se renumeraron 5.5 → 5.4 y 5.6 → 5.5 y se corrigieron sus referencias;
  - se actualizaron el diagrama de la guía y el de arquitectura, `technical.md` §9.2, `CLAUDE.md` (stack, flujo y `DP.despliegue`), el README y el script de humo.
- **ADR:**
  - ADR-0013 pasa a **Sustituido por ADR-0016**: no se borra, porque es un registro histórico;
  - ADR-0012 conserva GitHub Flow, y su despliegue con aprobación queda sustituido;
  - ADR-0016 nuevo: *Despliegue solo con Docker*.
- **Verificado:**
  - `actionlint` OK;
  - los 23 diagramas Mermaid de las dos guías se renderizan;
  - 0 enlaces a archivos o secciones eliminados;
  - `verificar.sh` da 0 errores.
- **Fuera del repositorio:** queda el environment `production`, vacío y ya sin uso; borrarlo lo decide el autor.
- **Tiempo:** ≈0,5 h · **sin IA (est.):** ≈2 h

### #21 — `/06-revisar dominio` en el modelo del curso

- **Origen:** completar la validación del kit (#19), que solo había probado la instalación y la verificación. El modo `dominio` se ejecutó completo en la copia de `F4/01.-Modelos/use-case-ca-v1` (Sales Order). El original no se tocó y la copia no se sube.
- **Resultado en ese proyecto:**
  - `mvn clean verify` en verde, con 46 pruebas y Java 25;
  - las 9 comprobaciones en OK, sin ERROR ni WARNING;
  - trazabilidad de las 15 reglas de dominio `RN-NNN`;
  - 4 INFO; el más útil: RN-008, el precio conservado, no tiene prueba.
- **Cómo se adaptó el prompt:**
  - encontró las reglas en §2–§5 y su capa en §6 con `DP.secciones`;
  - con un catálogo sin códigos, comparó los mensajes;
  - con *Domain Events* fuera del alcance, verificó que no existieran;
  - en un solo módulo, revisó la regla de dependencia por paquete;
  - creó la carpeta de la ronda, su README y el índice, que no existía.
- **Mejorado en el prompt:** tres frases de `06-revisar` dominio que suponían el estilo de `contract-api` (escala fija de los importes, paquetes `common`/`shared` y el `code` del catálogo) ahora dependen de lo que defina el proyecto.
- **Error de la IA corregido:** se afirmó que la máquina no tenía Java 25 porque `java_home` no lo listaba; estaba en el `PATH`. El build lo demostró antes de escribir el informe.
- **Tiempo:** ≈1 h · **sin IA (est.):** ≈3 h

## Métricas del proyecto

| Métrica | Valor | Fuente |
|---|---|---|
| Pruebas | 409 ejecutadas, 0 fallos (209 domain, 46 application, 154 infrastructure) | `./mvnw clean verify`, 2026-11-12 |
| Cobertura de líneas | 96,0 % / 96,7 % / 96,3 % (domain / application / infrastructure) | JaCoCo, 2026-11-12 |
| Cobertura en la primera revisión | 94,2 % / 90,0 % / 92,7 % | `docs/reviews/2026-09-26-ronda-001/07-revision-integral.md` |
| Duración del pipeline | 1 min 55 s – 3 min 11 s | GitHub Actions |
| Issues de Sonar | 1 bug y 2 vulnerabilidades en el primer análisis → 0 y 0 (Quality Gate en verde) | SonarQube local, Jenkins #10 y #11 (2026-11-12) |
| Hallazgos de la revisión con IA | 20 (1 alto, 6 medios, 13 bajos) | revisión 001 |
| Errores de la IA corregidos y registrados | 14 (entradas 2, 6, 7, 8 y 9) | esta bitácora |

## Lecciones

- La IA rinde más con contexto versionado (`CLAUDE.md`, reglas de negocio con ID, ADRs) que con prompts sueltos.
- Lo que la IA afirma sobre el código se verifica leyendo el código y ejecutando el build, no buscando coincidencias de texto.
- Cuando un prompt contradice la documentación, se corrige el prompt.
- Lo que no está definido en `business-rules.md` va a "Preguntas abiertas", no al código.
