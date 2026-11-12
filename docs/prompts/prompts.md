# Prompts del proyecto

Índice de los prompts de `contract-api`. Las tareas frecuentes son **comandos de Claude Code** versionados en `.claude/commands/`. Se ejecutan con `/<número>-<nombre> argumentos` y ya incluyen el contexto, el plan, la verificación y el informe. El número indica el **orden** en que se usan, y por eso aparecen ordenados al escribir `/`. Para tareas que no tienen comando, usa la plantilla de la sección 4, que sigue la misma estructura.

## 1. Comandos en orden

### 0x · Desarrollo de una funcionalidad

| # | Comando | Cuándo | Entrega |
|---|---|---|---|
| 0 | `/00-refinar-requisitos <requerimiento>` | Llega una funcionalidad nueva o una historia de usuario | MVPs, historias con criterios de aceptación y dudas, en `docs/requirements/` |
| 1 | `/01-reglas-negocio <tema>` | Antes de implementar algo cuyo comportamiento no está claro, o para cerrar preguntas abiertas | Preguntas al autor y reglas `BR-*` nuevas en `business-rules.md` |
| 2 | `/02-implementar-caso-uso <NombreUseCase>` | Implementar un caso de uso documentado en `business-rules.md` §6 | Dominio → API → pruebas, paso a paso, con autovalidación por capa |
| 3 | `/03-pruebas <clase o caso de uso>` | Completar o mejorar pruebas | Matriz regla → prueba y pruebas nuevas, cada una con su patrón |
| 4 | `/04-documentar <alcance>` | JavaDoc, OpenAPI, diagramas, ADR o guías desactualizados | Documentación técnica alineada con el código |
| 5 | `/05-revisar-pr <número o rama>` | Antes del *Squash and merge* | Informe del diff y veredicto `APROBAR` / `CAMBIOS REQUERIDOS` |

### 1x · Ciclo de revisión (siempre en este orden)

| # | Comando | Revisa | Modifica código |
|---|---|---|:-:|
| 1/9 | `/10-revisar-dominio` | `domain` | No |
| 2/9 | `/11-revisar-aplicacion` | `application` | No |
| 3/9 | `/12-revisar-infraestructura` | Persistencia, seguridad y configuración | No |
| 4/9 | `/13-revisar-api` | Controllers, DTOs, errores y OpenAPI | No |
| 5/9 | `/14-revisar-seguridad` | Seguridad transversal (OWASP) | No |
| 6/9 | `/15-revisar-cicd` | Pipeline, Docker, Jenkins y despliegues | No |
| 7/9 | `/16-revisar-arquitectura` | Integral, consolidando los informes 10–15 | No |
| 8/9 | `/17-corregir-hallazgos` | Corrige los ERROR y WARNING confirmados de la ronda | **Sí, el único** |
| 9/9 | `/18-revision-final` | Verifica las correcciones y decide `FINAL STATUS: READY` / `NOT READY` | No |

Las reglas comunes (fuentes de verdad, severidades **ERROR / WARNING / INFO**, formato del informe y numeración de las rondas) están en [`review-rules.md`](review-rules.md). Cada comando de revisión las incluye.

### 2x · Calidad y operación

| # | Comando | Cuándo | Entrega |
|---|---|---|---|
| 20 | `/20-corregir-sonar [regla o archivo]` | El Quality Gate de SonarQube falla en Jenkins | Cada hallazgo corregido con su prueba, o justificado en el código |
| 21 | `/21-cambiar-pipeline <cambio>` | Cambiar GitHub Actions, Jenkins, Docker o el despliegue | Cambio verificado de punta a punta, con su ADR |

### Flujos típicos

- **Funcionalidad nueva:** `00` → `01` (por cada duda) → `02` → `03` → `04` → `05` → merge.
- **Antes de una entrega:** `10` → `11` → `12` → `13` → `14` → `15` → `16` → `17` → `18` hasta `READY`.
- **Después de un cambio acotado:** solo las revisiones de las capas tocadas, en orden, y luego `17` → `18`.
- **Build de Jenkins en rojo por SonarQube:** `20`.

## 2. Estructura común de todo prompt

Cada comando y la plantilla de la sección 4 tienen estas partes. Si falta una, el prompt está incompleto.

1. **Rol:** experto concreto para la tarea (arquitecto, especialista en seguridad, QA, DevOps, analista).
2. **Contexto y fuentes de verdad:** qué existe hoy y qué documentos mandan (`CLAUDE.md`, `architecture.md`, `business-rules.md`, `technical.md`, ADR). Si el código contradice un documento, se reporta; no se elige en silencio.
3. **Validación del argumento:** qué hacer si falta o no coincide (listar opciones y detenerse).
4. **Objetivo verificable:** cómo se sabe que terminó (una prueba, un endpoint que responde, un Quality Gate en verde, `FINAL STATUS: READY`).
5. **Restricciones (qué NO hacer):** no inventar reglas de negocio; no agregar funcionalidades, patrones ni dependencias fuera del alcance; no integrar PRs ni aprobar despliegues; no tocar archivos fuera del alcance.
6. **Análisis de impacto y plan, sin cambiar código:** qué está bien y se mantiene, qué cambia, qué queda igual, qué es obsoleto, supuestos, archivos a crear, modificar o eliminar, y riesgos (incluida la sobreingeniería). **Nada se implementa hasta que el autor lo aprueba.**
7. **Pasos pequeños con aprobación:** un paso a la vez. Al terminar cada uno se muestran los archivos tocados, un resumen, el resultado de compilación y pruebas, y se espera "continuar".
8. **Verificación:** `./mvnw clean verify` en verde y, si cambia el código de producción o el pipeline, el Jenkins local con su Quality Gate. Ante un fallo, primero la causa raíz y después la corrección.
9. **Informe final:** archivos creados, modificados, movidos y eliminados; pruebas (ejecutadas, exitosas, fallidas, con error, omitidas); decisiones; pendientes con su regla o pregunta abierta; documentación actualizada. **Nunca afirmar que funciona sin haber compilado y probado.**
10. **Archivo de sesión:** el progreso se guarda en `docs/sessions/` (ver su [README](../sessions/README.md)) para retomar sin perder contexto ni repetir trabajo.

En las revisiones se añade: **separar revisar de corregir** (solo `17` modifica código), **no inventar problemas** y **no inflar la severidad**.

Buenas prácticas para todos:
- Un tema por sesión: seguridad, negocio e infraestructura en sesiones distintas. Se consumen menos tokens y el contexto no se mezcla.
- No quedarse con el primer resultado: revisar, pedir otra revisión y comparar. Cada iteración se registra en `docs/ai-log.md`.
- Todo cambio va en una rama corta con PR (ADR-0012). El *Squash and merge* y la aprobación de `production` son del autor.

## 3. De dónde sale cada práctica

| Práctica | Fuente |
|---|---|
| Qué hacer **y qué no hacer**; contextualizar, pedir, validar, iterar (3 a 6 versiones) | Docente, sesión 01 |
| Fuentes de verdad (`CLAUDE.md`, `architecture.md`, `business-rules.md`); hallazgos por severidad; informe en `docs/reviews/` | Docente, sesión 02 |
| Prompt parametrizado por caso de uso; "revisa y analiza el impacto, no cambies código" | Docente, sesión 03 |
| Primero un plan que el usuario aprueba ("si el prompt no lo tiene, estás descalificado"); contexto modular, no de miles de líneas; un tema por sesión; patrones de prueba | Docente, sesión 05 |
| Revisión por capa (01–05), un único prompt de corrección (06), revisión final con `READY` / `NOT READY` (07); severidades ERROR / WARNING / INFO; no confundir reglas de `domain` y de `application`; diferidos deliberadamente | Modelos del curso, `use-case-ca-v1/prompts` |
| Implementar una capa y validarla antes de pasar a la siguiente | Modelos del curso, `prompts-sales-order-clean-architecture.md` |
| Qué está bien, qué cambia, qué no se toca, qué es obsoleto y supuestos; riesgo de sobreingeniería; archivos creados, modificados y eliminados; causa raíz antes de corregir; "nunca afirmar que funciona sin compilar y probar" | Prompts de F1 (`product-enterprise-ready.md`, `refactor-package-by-feature.md`) |
| Estructura contexto, objetivo, actividades, restricciones e informe final; **archivo de sesión** que la IA actualiza en cada paso | Presentación de Juan José, sesión 05 |
| Perfil de arquitecto y revisiones repetidas; la IA **pregunta** las reglas de negocio que faltan | Presentación de Ángel, sesión 05 |
| Requisitos refinados con IA y divididos en **MVPs**; Quality Gate estricto antes de desplegar | Presentación de Jairo, sesión 05 |
| GitHub Flow, verificación con Jenkins y SonarQube, correcciones de Sonar justificadas en el código | Este proyecto (ADR-0012 a ADR-0015) |

## 4. Plantilla para tareas sin comando

```markdown
# Rol
Actúa como [rol experto] en Java 21, Spring Boot 3, Clean Architecture y DDD.

# Contexto
- Fuentes de verdad: @CLAUDE.md, @docs/architecture.md, @docs/business-rules.md [y los ADR o guías que apliquen].
- Situación actual: [qué existe hoy; confírmalo contra el código, no lo asumas].

# Objetivo
[Qué se quiere lograr]. Se considera terminado cuando [criterio verificable].

# Restricciones
- No inventes reglas de negocio: si falta una, pregúntame.
- No agregues funcionalidades, patrones ni dependencias fuera del alcance; no integres PRs ni apruebes despliegues.
- Fuera de alcance: [lo que no se toca].

# Forma de trabajo
1. Analiza el impacto: qué está bien, qué cambia, qué no se toca, supuestos, archivos y riesgos. No cambies código todavía y espera mi aprobación.
2. Ejecuta un paso a la vez y detente al final de cada uno.
3. Mantén el archivo de sesión en docs/sessions/ (docs/sessions/README.md).

# Verificación e informe final
- `./mvnw clean verify` en verde [y Jenkins con Quality Gate si cambia código de producción o el pipeline].
- Informe: archivos creados, modificados y eliminados; pruebas; decisiones; pendientes; documentación actualizada.
- No afirmes que funciona si no se compiló y probó.
```

Casos frecuentes con esta plantilla:
- **Nuevo endpoint REST:** rol arquitecto. Objetivo: "el método y la ruta responden según `architecture.md` §6, con `@PreAuthorize` por permiso (`business-rules.md` §4.1) y `@WebMvcTest` para el éxito y cada error". Regenerar `docs/api/openapi.*`.
- **Nuevo adaptador de salida:** rol arquitecto. Objetivo: "el puerto `[NombrePort]` tiene su adaptador en `infrastructure`, con prueba de integración (Testcontainers o WireMock)". Incluir la migración Flyway si aplica.
- **Refactorización:** rol arquitecto. El plan debe tener pasos pequeños, cada uno compilable y con pruebas verdes, sin cambiar códigos de error, contratos REST ni esquema.
- **Registrar un ADR:** `docs/adr/NNNN-titulo.md` con estado, contexto, decisión, al menos dos alternativas con pros y contras, y consecuencias. Máximo una página, en español.

## 5. Prompts históricos

Los archivos numerados con tres dígitos son los prompts que se usaron durante el curso, tal como se ejecutaron. Se conservan como evidencia del proceso (los citan `docs/ai-log.md`, `docs/reviews/` y `docs/design/`) y no se usan para trabajo nuevo.

| Archivo | Uso | Reemplazado por |
|---|---|---|
| `001-review-01.md` | Revisión integral (sesión 02) | Ciclo de revisión `/10` … `/18` |
| `002.-refactoring-v1.md` | Refactor del dominio por agregado (sesión 03) | Plantilla §4 (refactorización) |
| `003-security-01.md` | Diseño de la seguridad 3A (sesión 03) | `docs/design/003-security-design.md` y `/14-revisar-seguridad` |
| `004-implement-usecase.md` | Caso de uso paso a paso (sesión 03) | `/02-implementar-caso-uso` |
| `005-security-02.md` | Seguridad fase 1, pasos 1.6–1.8 (sesión 04) | `docs/design/003-security-design.md` |
| `006-cicd.md` | Puesta en marcha del CI/CD con parche (sesión 04) | `/21-cambiar-pipeline` |
