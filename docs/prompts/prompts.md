# Prompts del proyecto

Índice de los prompts de `contract-api`. Cada prompt es un archivo `docs/prompts/NN-nombre-VV.md`:

- **`NN`** es el **orden** en que se usa: `0x` desarrollo, `09`–`18` ciclo de revisión, `2x` calidad, operación y prompts.
- **`VV`** es la **versión**: `01` es la primera; `02` mejora un prompt anterior, que pasa a `historico/`. Nunca hay dos versiones activas del mismo prompt. Es la idea del docente en la sesión 01: "versión uno apesta, versión dos mejora, versión tres ya la puedes presentar".

Cada prompt tiene un **comando de Claude Code** con el mismo nombre sin versión (`/02-implementar-caso-uso`). Está en `.claude/commands/`, tiene tres líneas y solo carga el prompt vigente, como el `06.0.-fix.md` de los modelos del curso ("Lee y ejecuta las instrucciones de @prompts/…"). El contenido está **solo** en `docs/prompts/`: para mejorar un prompt se edita aquí, no en el comando. Sin Claude Code, se copia el archivo y se reemplaza `{ARGUMENTO}` a mano.

## 1. Prompts en orden

### 0x · Desarrollo de una funcionalidad

| # | Archivo | Comando y argumento | Cuándo | Entrega |
|---|---|---|---|---|
| 00 | [`00-refinar-requisitos-01.md`](00-refinar-requisitos-01.md) | `/00-refinar-requisitos <requerimiento>` | Llega una funcionalidad nueva | MVPs, historias con criterios de aceptación y dudas en `docs/requirements/` |
| 01 | [`01-reglas-negocio-01.md`](01-reglas-negocio-01.md) | `/01-reglas-negocio <tema>` | El comportamiento no está claro o hay preguntas abiertas | Preguntas al autor y reglas `BR-*` en `business-rules.md` |
| 02 | [`02-implementar-caso-uso-02.md`](02-implementar-caso-uso-02.md) | `/02-implementar-caso-uso <NombreUseCase>` | Implementar un caso de uso de `business-rules.md` §6 | Dominio → API → pruebas, con autovalidación por capa |
| 03 | [`03-pruebas-01.md`](03-pruebas-01.md) | `/03-pruebas <clase o caso de uso>` | Completar o mejorar pruebas | Matriz regla → prueba y pruebas con su patrón |
| 04 | [`04-documentar-01.md`](04-documentar-01.md) | `/04-documentar <alcance>` | JavaDoc, OpenAPI, diagramas, ADR o guías desactualizados | Documentación alineada con el código |
| 05 | [`05-revisar-pr-01.md`](05-revisar-pr-01.md) | `/05-revisar-pr <número o rama>` | Antes del *Squash and merge* | Informe del diff y `APROBAR` / `CAMBIOS REQUERIDOS` |
| 06 | [`06-refactorizar-01.md`](06-refactorizar-01.md) | `/06-refactorizar <objetivo>` | Reorganizar código sin cambiar el comportamiento | Refactor en pasos compilables con tabla de movimientos |

### 09–18 · Ciclo de revisión (siempre en este orden)

| # | Archivo | Comando | Revisa | Modifica código |
|---|---|---|---|:-:|
| 09 | [`09-reglas-revision-01.md`](09-reglas-revision-01.md) | *(lo incluyen todas las revisiones)* | Fuentes de verdad, severidades ERROR / WARNING / INFO, formato y numeración de rondas | — |
| 10 | [`10-revisar-dominio-01.md`](10-revisar-dominio-01.md) | `/10-revisar-dominio [ronda]` | `domain` | No |
| 11 | [`11-revisar-aplicacion-01.md`](11-revisar-aplicacion-01.md) | `/11-revisar-aplicacion [ronda]` | `application` | No |
| 12 | [`12-revisar-infraestructura-01.md`](12-revisar-infraestructura-01.md) | `/12-revisar-infraestructura [ronda]` | Persistencia, adaptadores y configuración | No |
| 13 | [`13-revisar-api-01.md`](13-revisar-api-01.md) | `/13-revisar-api [ronda]` | Controllers, DTOs, errores y OpenAPI | No |
| 14 | [`14-revisar-seguridad-01.md`](14-revisar-seguridad-01.md) | `/14-revisar-seguridad [ronda]` | Seguridad transversal (OWASP) | No |
| 15 | [`15-revisar-cicd-01.md`](15-revisar-cicd-01.md) | `/15-revisar-cicd [ronda]` | Pipeline, Docker, Jenkins y despliegues | No |
| 16 | [`16-revisar-arquitectura-02.md`](16-revisar-arquitectura-02.md) | `/16-revisar-arquitectura [ronda]` | Integral, consolidando 10–15 | No |
| 17 | [`17-corregir-hallazgos-01.md`](17-corregir-hallazgos-01.md) | `/17-corregir-hallazgos [ronda]` | Corrige los ERROR y WARNING confirmados | **Sí, el único** |
| 18 | [`18-revision-final-01.md`](18-revision-final-01.md) | `/18-revision-final [ronda]` | Verifica las correcciones: `FINAL STATUS: READY` / `NOT READY` | No |

### 2x · Calidad, operación y prompts

| # | Archivo | Comando | Cuándo |
|---|---|---|---|
| 20 | [`20-corregir-sonar-01.md`](20-corregir-sonar-01.md) | `/20-corregir-sonar [regla o archivo]` | El Quality Gate de SonarQube falla en Jenkins |
| 21 | [`21-cambiar-pipeline-02.md`](21-cambiar-pipeline-02.md) | `/21-cambiar-pipeline <cambio>` | Cambiar GitHub Actions, Jenkins, Docker o el despliegue |
| 22 | [`22-crear-o-mejorar-prompt-01.md`](22-crear-o-mejorar-prompt-01.md) | `/22-crear-o-mejorar-prompt <tarea o archivo>` | Hace falta un prompt nuevo o mejorar uno (nueva versión) |

### Flujos típicos

- **Funcionalidad nueva:** `00` → `01` (por cada duda) → `02` → `03` → `04` → `05` → merge.
- **Antes de una entrega:** `10` → `11` → `12` → `13` → `14` → `15` → `16` → `17` → `18`, hasta `READY`.
- **Después de un cambio acotado:** solo las revisiones de las capas tocadas, en orden, y luego `17` → `18`.
- **Reorganizar código:** `06` → la revisión de la capa tocada.
- **Build de Jenkins en rojo por SonarQube:** `20`.

## 2. Estructura común de todo prompt

Cada prompt tiene estas 10 partes. Si falta una, el prompt está incompleto. `22-crear-o-mejorar-prompt` las aplica al crear uno nuevo.

1. **Rol:** experto concreto para la tarea ("actúa como arquitecto de software especializado en Java…", sesión 02).
2. **Contexto y fuentes de verdad:** qué existe hoy y qué documentos mandan (`CLAUDE.md`, `architecture.md`, `business-rules.md`, `technical.md`, ADR). Si el código contradice un documento, se reporta; no se elige en silencio.
3. **Validación del argumento:** qué hacer si `{ARGUMENTO}` falta o no coincide (listar opciones y detenerse).
4. **Objetivo verificable:** cómo se sabe que terminó (una prueba, un endpoint que responde, un Quality Gate en verde, `FINAL STATUS: READY`).
5. **Restricciones (qué NO hacer):** no inventar reglas de negocio ni nombres o ubicaciones (salen de los documentos o se preguntan); no agregar funcionalidades, patrones ni dependencias fuera del alcance; no integrar PRs ni aprobar despliegues; no tocar archivos fuera del alcance.
6. **Análisis de impacto y plan, sin cambiar código:** con código que ya funciona, **nunca se ejecuta en bloque** (sesión 03). Primero se identifica qué está bien y se mantiene, qué cambia, qué queda igual, qué es obsoleto, los supuestos, los archivos a crear, modificar o eliminar y los riesgos, incluida la sobreingeniería. **Nada se implementa hasta que el autor lo aprueba.**
7. **Pasos pequeños con aprobación:** un paso a la vez. Al terminar cada uno se muestran los archivos tocados, un resumen y el resultado de compilación y pruebas, y se espera "continuar".
8. **Verificación:** `./mvnw clean verify` en verde y, si cambia código de producción o el pipeline, el Jenkins local con su Quality Gate. Ante un fallo, primero la causa raíz. Lo generado por IA no se da por bueno sin un comando ejecutado que lo demuestre.
9. **Informe final:** archivos creados, modificados, movidos y eliminados; pruebas (ejecutadas, exitosas, fallidas, con error, omitidas); decisiones; pendientes con su regla o pregunta abierta; documentación actualizada. **Nunca afirmar que funciona sin compilar y probar.**
10. **Archivo de sesión:** el progreso queda en `docs/sessions/` (ver su [README](../sessions/README.md)) para retomar sin perder contexto ni gastar tokens.

En las revisiones se añade: **separar revisar de corregir** (solo `17` modifica código), **no inventar problemas** y **no inflar la severidad**.

Buenas prácticas para todos:
- Prompts cortos y enfocados que remiten a los documentos, no prompts de "15 páginas" (sesión 01). Un tema por sesión: seguridad, negocio e infraestructura por separado (sesión 05).
- No quedarse con el primer resultado: revisar, pedir otra revisión y comparar. Cada iteración se registra en `docs/ai-log.md`.
- Si un cambio de código mueve la arquitectura o el contexto, se actualizan en el mismo cambio los documentos y los prompts afectados (sesión 03).
- Todo cambio va en una rama corta con PR (ADR-0012). El *Squash and merge* y la aprobación de `production` son del autor.

## 3. De dónde sale cada práctica

| Práctica | Fuente |
|---|---|
| Contextualizar, pedir, validar e iterar; **qué hacer y qué no hacer**; versiones sucesivas; no prompts de 15 páginas | Docente, sesión 01 |
| Rol especializado al inicio del prompt; pedirle a la IA que cree el prompt; un prompt genérico más prompts por capa; contexto dividido en `CLAUDE.md` / `architecture.md` / `business-rules.md` / `prompts`; fuentes de verdad; informe de revisión por severidad en `docs/reviews/` | Docente, sesión 02 |
| Prompt parametrizado por caso de uso; con código funcionando, primero leer, analizar e evaluar el impacto; la IA no inventa nombres ni ubicaciones; actualizar los prompts si cambia la arquitectura; atender primero los warnings y errores | Docente, sesión 03 |
| Pruebas con contexto → prompt → revisión → ejecución → umbrales (JaCoCo 80 %, SonarQube); builders y datos generados | Docente, sesión 04 |
| Primero un plan que el usuario aprueba ("si el prompt no lo tiene, estás descalificado"); contexto modular; un tema por sesión; saber qué patrones de prueba usaste | Docente, sesión 05 |
| Revisión por capa (01–05), un único prompt de corrección (06), revisión final con `READY` / `NOT READY` (07); severidades ERROR / WARNING / OK; no confundir reglas de `domain` y `application`; diferidos deliberadamente | Modelos del curso, `F2-F4/01.-Modelos/use-case-ca-v1/prompts/` |
| Implementar una capa y validarla antes de pasar a la siguiente | Modelos del curso, `use-case-ca/prompts-sales-order-clean-architecture.md` |
| Qué está bien, qué cambia, qué no se toca, qué es obsoleto y supuestos; riesgo de sobreingeniería; tabla de movimientos; archivos creados, movidos y eliminados; causa raíz antes de corregir; "nunca afirmar que funciona sin compilar y probar" | `F1/api-demo-v1/prompts/` (enterprise-ready y package-by-feature) |
| Los scripts de CI/CD generados por IA no se asumen correctos; `actionlint` y `hadolint`; acciones verificadas y fijadas; imagen sin root ni JDK; fallo del pipeline frente a fallo del código; limpiar contenedores | `CURS-000617-CICD-v2/.../001-devops-validar-cicd.md` |
| Revisión integral con fuentes de verdad y trazabilidad de reglas; implementación de caso de uso paso a paso; refactor del dominio; seguridad 3A | Prompts del curso `001`–`005` (ahora en `historico/`) |
| Contexto, objetivo, actividades, restricciones e informe final; **archivo de sesión** que la IA actualiza en cada paso | Presentación de Juan José, sesión 05 |
| Perfil de arquitecto y revisiones repetidas; la IA **pregunta** las reglas de negocio que faltan | Presentación de Ángel, sesión 05 |
| Requisitos refinados con IA y divididos en **MVPs**; Quality Gate estricto antes de desplegar | Presentación de Jairo, sesión 05 |
| GitHub Flow, verificación con Jenkins y SonarQube, correcciones de Sonar justificadas en el código | Este proyecto (ADR-0012 a ADR-0015) |

## 4. Plantilla rápida para tareas sin prompt

Para una tarea puntual que no justifica un prompt propio (si se va a repetir, usa `/22-crear-o-mejorar-prompt`):

```markdown
## Rol
Actúa como [rol experto] en Java 21, Spring Boot 3, Clean Architecture y DDD.

## Contexto
- Lee: CLAUDE.md, docs/architecture.md, docs/business-rules.md [y los ADR o guías que apliquen].
- Situación actual: [qué existe hoy; confírmalo contra el código, no lo asumas].

## Objetivo
[Qué se quiere lograr]. Se considera terminado cuando [criterio verificable].

## Restricciones
- No inventes reglas de negocio, nombres ni ubicaciones: si faltan, pregúntame.
- No agregues funcionalidades, patrones ni dependencias fuera del alcance; no integres PRs ni apruebes despliegues.

## Forma de trabajo
1. Analiza el impacto (qué está bien, qué cambia, qué no se toca, supuestos, archivos, riesgos) sin cambiar código, y espera mi aprobación.
2. Un paso a la vez, deteniéndote al final de cada uno.

## Verificación e informe final
- `./mvnw clean verify` en verde. Informe: archivos, pruebas, decisiones y pendientes. No afirmes que funciona sin compilar y probar.
```

## 5. Prompts históricos

En [`historico/`](historico/) están los prompts que se usaron durante el curso, tal como se ejecutaron. Se conservan como evidencia del proceso (los citan `docs/ai-log.md`, `docs/reviews/` y `docs/design/`) y no se usan para trabajo nuevo.

| Archivo | Uso | Reemplazado por |
|---|---|---|
| `historico/001-review-01.md` | Revisión integral (sesión 02) | Ciclo de revisión `10`–`18`; su sucesor directo es `16-revisar-arquitectura-02.md` |
| `historico/002.-refactoring-v1.md` | Refactor del dominio por agregado (sesión 03) | `06-refactorizar-01.md` |
| `historico/003-security-01.md` | Diseño de la seguridad 3A (sesión 03) | `docs/design/003-security-design.md` y `14-revisar-seguridad-01.md` |
| `historico/004-implement-usecase.md` | Caso de uso paso a paso (sesión 03) | `02-implementar-caso-uso-02.md` |
| `historico/005-security-02.md` | Seguridad fase 1, pasos 1.6–1.8 (sesión 04) | `docs/design/003-security-design.md` |
| `historico/006-cicd.md` | Puesta en marcha del CI/CD con parche (sesión 04) | `21-cambiar-pipeline-02.md` |
