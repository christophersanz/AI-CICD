# 06 · Revisar el proyecto (por capa, PR o revisión final)

| | |
|---|---|
| **Comando** | `/06-revisar <modo> [ronda]` — modos: `dominio`, `aplicacion`, `infraestructura`, `api`, `seguridad`, `cicd`, `arquitectura`, `final`, `pr <número o rama>` |
| **Archivo** | `docs/prompts/06-revisar.md` (única versión; los cambios se hacen aquí y quedan en el historial de git) |
| **Origen** | Mejora del prompt del curso [`001-review-01.md`](https://github.com/christophersanz/AI-CICD/blob/33f74fc/docs/prompts/001-review-01.md) y de la serie `01`–`07` de los modelos del curso |

> `{ARGUMENTO}` es lo que se escribe después del comando. `DP.<clave>` es un dato del proyecto: está en `CLAUDE.md` → *Datos para los prompts*. Sin Claude Code, copia este prompt y reemplaza `{ARGUMENTO}` a mano.
>
> **Documentos y secciones:** `architecture.md`, `business-rules.md` y `technical.md` son los archivos que indican `DP.doc-arquitectura`, `DP.doc-reglas` y `DP.doc-tecnico`. Si uno es «No aplica», omite lo que este prompt diga de él. `§N` es la numeración de las plantillas del kit (`docs/prompts/kit/*.plantilla.md`); si el proyecto organiza sus documentos de otra forma, `DP.secciones` dice dónde está cada sección y qué formato tienen los ID de las reglas (`BR-*` en la plantilla). Las capas y la ubicación de la API salen de `DP.modulos`.

> **Este prompt es de revisión: no modifica código, pruebas, configuración ni documentación.** Solo escribe su informe y actualiza los índices de `docs/reviews/` (el `README.md` de la ronda y el general). Las correcciones se hacen con `/07-corregir`.

## Rol
Según el modo, actúa como el experto que indica su sección. En todos los casos buscas evidencia en el código; no confirmas lo que ya se cree ni inventas problemas.

## Validación del argumento
`{ARGUMENTO}` es `<modo> [ronda]`. Si falta el modo o no es uno de la tabla, muestra la tabla con una línea por modo, indica cuál toca según el orden y los informes que ya existen en `docs/reviews/`, y detente.

## Objetivo
Un informe por modo, guardado con el nombre de la tabla *Orden de la revisión*, en el que cada comprobación tiene su severidad y su evidencia verificada (`archivo:línea`, comando ejecutado o reporte), la trazabilidad de las reglas del alcance queda completa y no se modificó ningún otro archivo salvo los índices de `docs/reviews/`. En el modo `final`, además, una única línea `FINAL STATUS: READY` o `NOT READY` que cumple sus condiciones.

## Orden de la revisión

Cada ronda tiene su carpeta, **`docs/reviews/AAAA-MM-DD-ronda-NNN/`**. `NNN` es el número de la ronda (001, 002…) y `AAAA-MM-DD` es la fecha en que empieza.

| Paso | Modo | Revisa | Informe (en la carpeta de la ronda) |
|:-:|---|---|---|
| 1 | `dominio` | Capa `domain` | `01-revision-dominio.md` |
| 2 | `aplicacion` | Capa `application` | `02-revision-aplicacion.md` |
| 3 | `infraestructura` | Persistencia, adaptadores y configuración (sin la API) | `03-revision-infraestructura.md` |
| 4 | `api` | API REST | `04-revision-api.md` |
| 5 | `seguridad` | Seguridad transversal | `05-revision-seguridad.md` |
| 6 | `cicd` | Pipeline, imagen, CI local y despliegues | `06-revision-cicd.md` |
| 7 | `arquitectura` | Integral, consolidando los pasos 1–6 | `07-revision-integral.md` |
| 8 | — | `/07-corregir ronda NNN`: corrige los ERROR y WARNING confirmados (el único que modifica código) | `08-correcciones-aplicadas.md` |
| 9 | `final` | Verifica las correcciones y decide `READY` / `NOT READY` | `09-veredicto-final.md` |
| — | `pr <n>` | El diff de un PR, antes de integrarlo | `docs/reviews/pr/pr-<n>.md` |

**Qué ronda usar:** si `[ronda]` se omite, la ronda en curso es la última carpeta que todavía no tiene `09-veredicto-final.md`. Si no hay ninguna en curso, se crea la siguiente con la fecha de hoy.

**Índices:**
- Al crear la carpeta, crea su `README.md` con el alcance y la tabla `Paso | Informe | Estado | Resultado`, y agrega la ronda a `docs/reviews/README.md`. Si ese índice no existe, por ejemplo en la primera ronda de un proyecto, créalo con la tabla `Ronda | Fecha | Alcance | Resultado`.
- Cada modo marca su fila al terminar.
- `final` escribe el `FINAL STATUS` en el README de la ronda y en el general. Se puede ejecutar solo una parte (por ejemplo, `dominio` → `/07-corregir` → `final` tras cambiar el dominio), pero siempre en este orden: de adentro hacia afuera y de lo particular a lo integral. Revisar por partes enfoca la revisión y consume menos contexto. Al terminar, indica el siguiente paso.


## Fuentes de verdad
Leer completas, en este orden:
1. `CLAUDE.md`: stack, flujo de Git y forma de trabajar.
2. `architecture.md`: capas, reglas de arquitectura (§3), nombres, API, errores y pruebas.
3. `business-rules.md`: reglas `BR-*` con la **capa** que las valida, estados, casos de uso, errores y preguntas abiertas (§9).
4. `technical.md` y los ADR de `docs/adr/`: decisiones vigentes.

Los informes anteriores de `docs/reviews/` dan contexto y trazabilidad, pero **no mandan sobre los documentos**. Si el código contradice un documento, se reporta e indica cuál debería corregirse.

## Severidades
Se usan solo estas, **sin inflarlas**:

| Severidad | Cuándo |
|---|---|
| **ERROR** | Viola una regla de negocio, la regla de dependencia, una regla obligatoria de `architecture.md` §3, un comportamiento obligatorio de la API o la corrección del build y de las pruebas |
| **WARNING** | Debilidad relevante de arquitectura, implementación, mantenibilidad o pruebas que hoy no incumple una regla obligatoria |
| **INFO** | Mejora opcional u observación |
| **OK** | Comprobación verificada (solo en la tabla de comprobaciones) |

## Reglas
- Solo hallazgos con evidencia verificable (`archivo:línea`, clase o método). **No inventar problemas si el código cumple.** Antes de reportar un ERROR, volver a verificar la evidencia.
- Respetar la capa de cada regla: **no marcar como error de `domain` una regla que `business-rules.md` asigna a `application`**, ni al revés.
- No son hallazgos: lo que un ADR descartó (por ejemplo, los de `DP.adr-arquitectura`); lo pendiente por una pregunta abierta de §9 que el código resuelve de forma neutral con su TODO; los diferidos deliberadamente en un informe de correcciones (paso 8) anterior; una preferencia de estilo sin regla que la respalde. Si alguno contradice una regla obligatoria, sí se reporta con la evidencia exacta.
- Lo que no se pueda evaluar va como **No evaluado**, con el motivo.
- **Las búsquedas automáticas (`grep`, conteos, scripts) solo encuentran candidatos.** Cada coincidencia se confirma leyendo el código o el reporte antes de reportarla, y cada número que aparece en el informe (pruebas, archivos, scripts) se cuenta con un comando. Falsos positivos habituales:
  - texto en comentarios o documentación, como la descripción del formato `-----BEGIN PRIVATE KEY-----` en un README;
  - pruebas de clases `@Nested`, que Surefire reporta dentro del archivo de la clase externa;
  - una clase de prueba que cubre varios servicios a la vez.

## Antes de revisar
Ejecutar `DP.build` y anotar compilación y pruebas (ejecutadas, fallidas, con error y omitidas, por módulo) y la cobertura. Dentro de una misma ronda, si el commit revisado es el mismo del build anterior y no hay cambios locales (`git rev-parse HEAD` igual y `git status` limpio), se reutiliza ese resultado, indicando en el informe de qué paso y de qué commit proviene. El modo `final` siempre vuelve a ejecutarlo. Si el CI local (`DP.ci-local`) está activo, anotar el último Quality Gate (`DP.sonarqube`).

## Formato del informe
1. **Resumen** (3–5 líneas) y resultado del build.
2. **Comprobaciones:** `# | Comprobación | Severidad | Archivo/componente | Evidencia | Regla o criterio | Corrección propuesta`.
3. **Trazabilidad de reglas** del alcance: `Regla | Implementación (clase/método) | Prueba | PASS / FAIL / NOT COVERED`. Una regla solo es PASS si su implementación está verificada; si está implementada sin una prueba adecuada, se indica.
4. **Pruebas existentes y faltantes** del alcance.
5. **Hallazgos** agrupados en ERROR, WARNING e INFO. Cada uno con archivo, clase o método, regla afectada, evidencia y explicación; los ERROR, además, con un fragmento antes/después.
6. **Aspectos positivos** que conviene mantener.
7. **Conclusión técnica** y siguiente comando del orden.

## Modos

Cada modo revisa **solo** su alcance; lo que veas fuera de él va como INFO con una línea, para el modo que le corresponde.

### Paso 1 · `dominio` — Dominio
**Rol:** arquitecto experto en DDD táctico y Java.

**Alcance:** Paquete `<DP.paquete-base>.domain` y sus pruebas (módulo `domain` de `DP.modulos`).

**Comprobaciones:**
1. Java puro: sin Spring, JPA, Jakarta, Lombok, Jackson, HTTP ni imports de `application` o `infrastructure` (revisa los imports y, si el dominio es un módulo propio en `DP.modulos`, su `pom.xml` o `build.gradle`; en un solo módulo, la regla se comprueba por paquete).
2. Cada agregado de `business-rules.md` §3 protege las invariantes que `business-rules.md` §4 asigna a `domain`; sin setters públicos ni fábricas (`restore`, `reconstitute`) que permitan saltarse invariantes sin advertirlo.
3. Value objects (`record`) validados en el constructor con excepciones de dominio (o un `Guard` común), nunca `NullPointerException` ni `IllegalArgumentException`; normalización al crear; importes con `BigDecimal` y escala fija, nunca `float`/`double`.
4. Transiciones de estado según §5: las no dibujadas lanzan el código de transición inválida del catálogo (§8), con las excepciones que documente §5.
5. Eventos de §7 emitidos por el agregado dentro de sus métodos, con `eventId`, `occurredOn` desde un `Clock` y `aggregateId`; ningún `now()` en el dominio; no hay eventos que §7 no documente.
6. Dependencias entre paquetes según `architecture.md` §2 y `DP.adr-arquitectura`: `common` y `shared` no importan agregados; un agregado solo usa el identificador de otro.
7. `DomainException` y subclases con el `code` del catálogo (§8), sin conceptos HTTP, JPA ni SQL.
8. Sin abstracciones ni patrones fuera del alcance.
9. Pruebas de dominio sin Spring ni mocks para cada invariante, transición y caso límite.

### Paso 2 · `aplicacion` — Aplicación
**Rol:** arquitecto experto en Clean Architecture y casos de uso.

**Alcance:** Paquete `<DP.paquete-base>.application` y sus pruebas (módulo `application` de `DP.modulos`).

**Comprobaciones:**
1. Depende solo de `domain` y, como máximo, de `@Transactional` (imports y, si es un módulo propio en `DP.modulos`, su `pom.xml` o `build.gradle`); sin JPA, REST, Spring Security ni `infrastructure`.
2. Puertos de entrada en `port/in` con el patrón `<Verbo><Entidad>UseCase`; puertos de salida en `port/out` (`<Entidad>RepositoryPort`, `<Sistema>ClientPort`, puertos de identidad) con métodos en términos del dominio y sin tipos técnicos.
3. `Command`/`Query`/`Result` como records inmutables en `dto`, con validación mínima de presencia (no reglas de negocio).
4. Un servicio por caso de uso (`<Verbo><Entidad>Service`), sin `@Service`, registrado como bean en la configuración de la capa de infraestructura (`DP.modulos`).
5. Los servicios orquestan (cargar → delegar al dominio → validar reglas de `application` → persistir) sin absorber reglas que §4 asigna a `domain`.
6. Reglas que §4 asigna a `application` implementadas (típicamente existencia, unicidad, reglas entre agregados, límites acumulados y autorización por alcance), según lo implementado.
7. Límites transaccionales: un `@Transactional` por caso de uso, sin llamadas externas dentro de la transacción que no lo requieran.
8. Excepciones de aplicación (`NotFoundException`, `ConflictException` y subclases) con el código del catálogo.
9. Pruebas con Mockito que mockean solo puertos de salida: camino feliz, cada regla y cada error.

### Paso 3 · `infraestructura` — Infraestructura
**Rol:** arquitecto experto en Spring Boot, Spring Data JPA, migraciones y Spring Security.

**Alcance:** la capa de infraestructura **excepto** la API (ubicaciones en `DP.modulos`): `persistence`, `client`, `security`, `config`, migraciones (`DP.migraciones`), `application*.yml` y sus pruebas.

**Comprobaciones:**
1. Cada puerto de salida tiene un adaptador que lo implementa; los adaptadores dependen de los puertos y no al revés; `persistence`, `client` y `security` no dependen de `api`.
2. Entidades JPA (`<Entidad>JpaEntity`) separadas del dominio; mappers dominio ↔ JPA sin lógica de negocio; el dominio y los puertos no se adaptaron a JPA.
3. Repositorios Spring Data como mecanismo técnico, no como puertos; sin entidades JPA fuera de `persistence`.
4. Migraciones coherentes con las entidades: `NOT NULL`, los únicos que exige `business-rules.md`, claves foráneas e índices para las consultas de las reglas; migraciones aplicadas que no se editaron.
5. Adaptadores de seguridad (paquete `security` de la infraestructura): implementan los puertos de identidad de `application` y no tienen reglas de negocio; el análisis de seguridad en sí (tokens, autorización, secretos) es de ``/06-revisar seguridad``.
6. Configuración: perfiles `local`/`dev`/`prod`, secretos solo por variables de entorno, `open-in-view` desactivado, logs JSON sin datos sensibles.
7. Rendimiento: N+1, `FetchType.EAGER` innecesario, consultas sin paginar.
8. Pruebas de integración según `DP.pruebas-integracion` para los adaptadores y las restricciones de la base.

### Paso 4 · `api` — API REST
**Rol:** arquitecto experto en diseño de APIs REST, Spring Web y OpenAPI.

**Alcance:** la API REST, en la ubicación que indica `DP.modulos` (controllers, `dto`, mappers, manejo de errores), el contrato OpenAPI (`DP.openapi`) y las pruebas `@WebMvcTest`.

**Comprobaciones:**
1. Controllers como adaptadores de entrada: solo usan puertos `port.in`; no inyectan repositorios, adaptadores ni clases de `security`; sin lógica de negocio.
2. Request/Response en `api/dto`, separados de los modelos de dominio, de `Command`/`Query`/`Result` y de las entidades JPA.
3. Mappers Request → Command y Result → Response, sin lógica de negocio.
4. Bean Validation solo de formato; no sustituye invariantes del dominio.
5. `@PreAuthorize("hasAuthority('<permiso>')")` en cada endpoint según `business-rules.md` §4.1; los públicos son solo los documentados.
6. Rutas en plural y kebab-case, verbos, versionado `/api/v1`, paginación y códigos de `architecture.md` §6 (`201` + `Location`, `204`, `400`, `401`/`403`, `404`, `409`, `422`).
7. Errores RFC 9457 (`application/problem+json`) con `type`, `title`, `status`, `detail` y `code`; todas las excepciones de negocio traducidas; sin stack traces ni detalles internos; 500 sin filtrar el mensaje.
8. OpenAPI completo (`@Operation`, `@ApiResponse` por código, `@Schema`) y el contrato de `DP.openapi` coincide con los endpoints actuales.
9. Pruebas `@WebMvcTest` del éxito, del 400, del 401 sin token, del 403 sin permiso y de cada error de negocio.

### Paso 5 · `seguridad` — Seguridad
**Rol:** especialista en seguridad de aplicaciones (OWASP ASVS y API Security Top 10) y Spring Security.

**Alcance:** Seguridad en todas las capas: el paquete `security` de la infraestructura, `@PreAuthorize` de la API, reglas de identidad de `business-rules.md`, configuración, dependencias y pipeline. Diseño de referencia: `DP.seguridad`.

**Comprobaciones:**
1. Autenticación: login sin enumeración de usuarios (un único error para credenciales inválidas), política de contraseñas y usuarios deshabilitados según `business-rules.md`.
2. Autorización: permiso correcto en cada endpoint (§4.1), reglas de alcance de lectura de `business-rules.md`, sin endpoints accidentalmente públicos (`anyRequest().denyAll()`), actuator protegido.
3. Tokens: firma asimétrica con claves fuera del repositorio, expiración, `iss`/`aud`, refresh tokens opacos hasheados, de un solo uso y revocables, según `business-rules.md`.
4. Entrada y salida: validación, tamaños máximos, errores sin información interna, cabeceras de seguridad, CORS restringido.
5. Secretos: ninguno en el código, en `application*.yml`, en el historial de git, en la imagen Docker ni en los logs del pipeline (enmascarados).
6. Dependencias: versiones con vulnerabilidades conocidas en el `pom.xml` y en las imágenes base.
7. Excepciones del análisis estático (`DP.sonarqube`): cada `@SuppressWarnings("java:S…")` tiene una justificación válida y vigente.
8. Pruebas de seguridad: 401, 403 por permiso, tokens expirados, reutilizados o manipulados.

**En el informe:** Añade al informe una tabla `Riesgo OWASP | Estado | Evidencia`.

### Paso 6 · `cicd` — CI/CD
**Rol:** ingeniero DevOps senior experto en las herramientas de `DP.ci`, `DP.ci-local`, Docker y análisis estático.

**Alcance:** Lo que describen `DP.ci`, `DP.imagen`, `DP.despliegue`, `DP.ci-local` y `DP.sonarqube` (workflows, scripts, reglas de protección versionadas, plantilla de PR, `Dockerfile`, archivos de despliegue y `docker-compose.yml`), contra `DP.adr-cicd`, `DP.flujo-git` y la guía de CI/CD de `DP.guias`. Puedes consultar el estado real en solo lectura (con `gh`: `gh run list`, `gh api repos/{owner}/{repo}/rulesets`; y la API del CI local).

**Comprobaciones:**
> Los scripts de CI/CD los generó una IA: **no asumas que funcionan**. Toda afirmación ("compila", "la imagen arranca", "el job pasa") debe estar respaldada por un comando ejecutado o por una ejecución real (ID de run o número de build), con su resultado. Lo que no se pudo probar va como "No evaluado".

0. **Validación estática:** `actionlint` sobre el workflow (expresiones, contextos y `shellcheck` de los `run:`) y `hadolint` sobre el `Dockerfile` (con sus imágenes Docker si no están instalados); `bash -n` en los scripts de `DP.ci` y `DP.ci-local`; `.dockerignore` excluye `target/`, `.git/` e IDEs y no excluye nada que el `Dockerfile` copie.
1. Flujo de Git (`DP.flujo-git`): las reglas de protección activas coinciden con las versionadas, los checks requeridos existen con ese nombre exacto y, si `DP.despliegue` exige aprobación, el environment de producción pide revisor.
2. Workflow: jobs, condiciones por evento (PR, `DP.rama-principal`, tag), `permissions:` mínimos por job (`packages: write` solo donde se publica), `timeout-minutes`, `concurrency` y caché de Maven; en un `pull_request` nunca hay login ni push a registros; acciones de terceros que existen en la versión indicada (verificado, no supuesto) y, como recomendación, fijadas por SHA.
3. Imagen: `Dockerfile` multi-stage; la imagen final no tiene JDK, código fuente ni `target/` y no corre como root (compruébalo con `docker run --rm --entrypoint id`); configuración de producción; capas de dependencias reutilizadas en un segundo build; etiquetas según `DP.imagen`; publicación solo desde la rama principal y tags. Recomendaciones opcionales, justificadas: escaneo de la imagen (Trivy) y Dependabot para acciones y Maven.
4. Verificación y despliegue según `DP.despliegue`: prueba de humo, aprobación humana antes de producción, despliegue de una imagen inmutable (sha o versión, no `latest`) y rollback si existe.
5. CI local (`DP.ci-local`), si existe: etapas alineadas con su ADR, disparador que sobrevive a reinicios, Quality Gate (`DP.sonarqube`) antes de construir y desplegar, rollback y limpieza de imágenes y contenedores temporales.
6. Secretos: solo en *secrets*/environments o en los archivos locales no versionados que indique `DP.ci-local`; nada impreso en logs.
7. Coherencia: `DP.guias`, `technical.md` y los diagramas describen el pipeline actual.
8. Evidencia de ejecución: últimas ejecuciones en la rama principal y último build del CI local (resultado por job o etapa). Si algo falla, distingue un fallo **del pipeline** (configuración) de uno **del código** (prueba rota).
9. Al terminar, elimina los contenedores y volúmenes de prueba que hayas creado.

### Paso 7 · `arquitectura` — Arquitectura integral
**Rol:** Arquitecto de Software Senior experto en Clean Architecture, arquitectura hexagonal y DDD.

**Alcance:** todo el repositorio.

**Comprobaciones:**
1. Regla de dependencia `infrastructure → application → domain` con las dependencias **reales** (imports y `pom.xml` de cada módulo), incluida la regla 9 de `architecture.md` §3 (aislamiento dentro de `infrastructure`).
2. Puertos y adaptadores: entrada (controller → `UseCase`) y salida (`Port` ← adaptador) correctamente orientados.
3. Separación de modelos: dominio, `Command`/`Query`/`Result`, Request/Response y entidades JPA.
4. Nombres y paquetes según `architecture.md` §2 y §5.
5. Cada regla `BR-*` implementada en la capa que indica `business-rules.md` §4: trazabilidad completa del proyecto.
6. Sin lógica de negocio en controllers, mappers, repositorios ni adaptadores.
7. Sin patrones ni módulos fuera del alcance (CQRS, event sourcing, mediator, specification, microservicios) y sin sobreingeniería.
8. Coherencia entre código, `architecture.md`, `business-rules.md`, `technical.md` y las guías (diagramas incluidos).
9. Hallazgos repetidos entre capas que apuntan a una causa común.

**En el informe:** apóyate en los informes de los pasos 1 a 6 de la misma ronda (si alguno falta, indícalo y recomienda ejecutarlo antes). Añade al informe las secciones **Regla de dependencia** (dependencias reales encontradas), **Patrones y alcance** y una **matriz consolidada** de los hallazgos de la ronda (`Severidad | Capa | Componente | Hallazgo | Informe de origen | Acción`), sin duplicados.

### Modo `pr` — Revisar un PR antes de integrarlo
**Rol:** revisor técnico senior de un equipo que trabaja con `DP.flujo-git`.

**Alcance:** Solo el diff de la rama o PR indicado contra `DP.rama-principal` (`git diff <rama-principal>...<rama>` o `gh pr diff <n>`) y su impacto directo. El argumento es `pr <número o rama>`; sin número, revisa la rama actual.

**Comprobaciones:**
1. Aplica las comprobaciones de los pasos 1 a 6 solo a los archivos del diff.
2. El cambio hace lo que dice el PR y nada más (sin cambios no relacionados).
3. Cada regla, endpoint o error nuevo tiene su prueba; ninguna prueba se debilitó o se eliminó sin motivo.
4. Documentación del mismo cambio: `business-rules.md`, `technical.md`, el contrato OpenAPI (`DP.openapi`), ADR y guías, según lo que toque.
5. Checks del PR en verde (`gh pr checks`) y checklist de la plantilla de PR completa y veraz.
6. Riesgos de integración: migraciones, compatibilidad de la API, configuración o secretos nuevos.

**En el informe:** `docs/reviews/pr/pr-<número o rama>.md`, en la misma rama del PR, con una línea final `Veredicto: APROBAR | CAMBIOS REQUERIDOS` (CAMBIOS REQUERIDOS si hay algún ERROR). Si hay cambios requeridos, se corrigen en la misma rama y se vuelve a ejecutar `/06-revisar pr <n>`; si se aprueba, el autor decide el merge.

### Paso 9 · `final` — Revisión final
**Rol:** Arquitecto de Software Senior responsable de aprobar la entrega. Tu decisión se basa en evidencia verificada hoy, no en lo que dicen los informes anteriores.

**Alcance:** el estado **actual** del repositorio tras `/07-corregir ronda NNN`. Lee todos los informes `NNN-*` de la ronda.

**Comprobaciones:**
1. **Build y pruebas:** `DP.build`, con pruebas por módulo (ejecutadas, exitosas, fallidas, con error, omitidas) y cobertura. Si el build o las pruebas fallan, el estado es NOT READY.
2. **Correcciones reportadas:** no asumas que un hallazgo marcado como corregido en el informe de correcciones (paso 8) lo está. Verifícalo contra el código y las pruebas actuales:

   | Hallazgo (informe de origen) | Corrección reportada | Verificación actual | PASS / FAIL |
   |---|---|---|---|

3. **Revisión por capa** resumida (comprobaciones de los pasos 1 a 6) para detectar regresiones introducidas por las correcciones.
4. **Regla de dependencia** con las dependencias reales (imports y `pom.xml`).
5. **Trazabilidad completa** de `business-rules.md` §4: `Regla | Implementación | Prueba | PASS / FAIL / NOT COVERED`.
6. **Quality Gate** (`DP.sonarqube`), si está disponible.
7. **Diferidos deliberadamente:** los hallazgos que el informe de correcciones marcó como diferidos y los pendientes de §9. No se reportan como defectos, salvo que contradigan una regla obligatoria; en ese caso, con la evidencia exacta.

**Entrega:** informe `09-veredicto-final.md` en la carpeta de la ronda, con esta estructura:

```text
# Revisión final — ronda NNN
## 1. Resumen ejecutivo
## 2. Build, pruebas y Quality Gate
## 3. Revisión por capa (domain, application, infrastructure, api)
## 4. Regla de dependencia
## 5. Trazabilidad de reglas de negocio
## 6. Verificación de las correcciones (paso 8)
## 7. Hallazgos restantes
### ERROR
### WARNING
### INFO
## 8. Diferidos deliberadamente
## 9. Decisión final
FINAL STATUS: READY | NOT READY
```

Termina con **exactamente una** de estas líneas, sin estados intermedios:

```text
FINAL STATUS: READY
FINAL STATUS: NOT READY
```

- **READY:** ningún ERROR, regla de dependencia cumplida, reglas obligatorias implementadas, comportamiento de la API correcto, compilación y pruebas en verde y Quality Gate en verde (si está disponible). Pueden quedar WARNING e INFO.
- **NOT READY:** queda al menos un ERROR. Indica qué comando lo resolvería (`/07-corregir ronda NNN`, `/01-definir reglas <tema>`, `/07-corregir sonar`…).

## Entrega
Guarda el informe con el nombre de la tabla *Orden de la revisión* y el formato de *Formato del informe* (el modo `final` usa su estructura propia). **No modifiques ningún otro archivo.** Termina indicando el siguiente paso del orden.
