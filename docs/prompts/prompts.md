# Prompts del proyecto

Índice de los prompts de `contract-api`. Las tareas frecuentes son **comandos de Claude Code** versionados en `.claude/commands/`: se ejecutan con `/nombre argumentos` y ya incluyen el contexto, el plan, la verificación y el informe. Para tareas que no tienen comando, usa la plantilla de la sección 3, que sigue la misma estructura.

## 1. Comandos

| Comando | Cuándo | Entrega |
|---|---|---|
| `/business-rules <tema>` | Antes de implementar algo cuyo comportamiento no está claro, o para completar las preguntas abiertas | Preguntas a RR.HH./al autor y reglas `BR-*` nuevas en `business-rules.md` |
| `/implement-usecase <NombreUseCase>` | Implementar un caso de uso documentado en `business-rules.md` §6 | Dominio → API → pruebas, paso a paso con aprobación |
| `/tests <clase o caso de uso>` | Completar o mejorar pruebas | Matriz regla → prueba y pruebas nuevas con sus patrones |
| `/review [alcance]` | Antes de integrar un cambio grande o de una entrega | Informe en `docs/reviews/NNN-review-<alcance>.md` |
| `/sonar-fix` | El Quality Gate de SonarQube falla en Jenkins | Cada hallazgo corregido o justificado en el código |
| `/document <alcance>` | JavaDoc, OpenAPI, diagramas, ADR o guías desactualizados | Documentación técnica alineada con el código |
| `/pipeline <cambio>` | Cambiar GitHub Actions, Jenkins, Docker o el despliegue | Cambio verificado de punta a punta, con ADR |

Orden habitual para una funcionalidad nueva: `/business-rules` → `/implement-usecase` → `/tests` → `/review` → `/document`.

## 2. Estructura común de todo prompt

Cada comando y la plantilla de la sección 3 tienen estas partes. Si una falta, el prompt está incompleto.

1. **Rol:** experto concreto para la tarea (arquitecto, especialista en seguridad, QA, DevOps).
2. **Contexto y fuentes de verdad:** qué existe hoy y qué documentos mandan (`CLAUDE.md`, `architecture.md`, `business-rules.md`, `technical.md`, ADR). Si el código contradice un documento, se reporta; no se elige en silencio.
3. **Objetivo verificable:** cómo se sabe que terminó (una prueba, un endpoint que responde, un Quality Gate en verde).
4. **Restricciones (qué NO hacer):** no inventar reglas de negocio; no añadir dependencias sin justificarlas; no integrar PRs ni aprobar despliegues; no tocar archivos fuera del alcance.
5. **Análisis de impacto y plan, sin cambiar código:** primero se presenta qué se toca, en qué orden y con qué riesgos. **Nada se implementa hasta que el autor lo aprueba.**
6. **Pasos pequeños con aprobación:** un paso a la vez; al terminar cada uno, archivos tocados, resumen, resultado de compilación y pruebas, y pausa hasta "continuar".
7. **Verificación:** `./mvnw clean verify` en verde y, si cambia el pipeline o el código de producción, el Jenkins local con el Quality Gate de SonarQube.
8. **Informe final:** qué se hizo, qué se decidió, qué queda pendiente (con su regla o pregunta abierta) y qué documentos se actualizaron.
9. **Archivo de sesión:** el progreso se guarda en `docs/sessions/` (ver su [README](../sessions/README.md)) para retomar sin perder contexto ni repetir trabajo.

Buenas prácticas que aplican a todos:
- Un tema por sesión: seguridad, negocio e infraestructura en sesiones distintas (consumen menos tokens y el contexto no se mezcla).
- No quedarse con el primer resultado: revisar, pedir otra revisión y comparar. Cada iteración se nota en `docs/ai-log.md`.
- Todo cambio va en una rama corta con PR (ADR-0012); el *Squash and merge* y la aprobación de `production` son del autor.

## 3. Plantilla para tareas sin comando

```markdown
# Rol
Actúa como [rol experto] en Java 21, Spring Boot 3, Clean Architecture y DDD.

# Contexto
- Fuentes de verdad: @CLAUDE.md, @docs/architecture.md, @docs/business-rules.md [y ADR o guías que apliquen].
- Situación actual: [qué existe hoy; confírmalo contra el código, no lo asumas].

# Objetivo
[Qué se quiere lograr] — se considera terminado cuando [criterio verificable].

# Restricciones
- No inventes reglas de negocio: si falta una, pregúntame.
- No añadas dependencias, no integres PRs ni apruebes despliegues.
- Fuera de alcance: [lo que no se toca].

# Forma de trabajo
1. Analiza el impacto y presenta un plan (archivos, orden, riesgos). No cambies código todavía.
2. Tras mi aprobación, ejecuta un paso a la vez y detente al final de cada uno.
3. Mantén el archivo de sesión en docs/sessions/ (docs/sessions/README.md).

# Verificación e informe final
- `./mvnw clean verify` en verde [y Jenkins con Quality Gate si cambia código de producción o el pipeline].
- Informe: cambios, decisiones, pendientes y documentación actualizada.
```

Casos frecuentes con esta plantilla:
- **Nuevo endpoint REST:** rol arquitecto; objetivo "método y ruta responden según `architecture.md` §6, con `@PreAuthorize` por permiso (`business-rules.md` §4.1) y `@WebMvcTest` para el éxito y cada error"; regenerar `docs/api/openapi.*`.
- **Nuevo adaptador de salida:** rol arquitecto; objetivo "el puerto `[NombrePort]` tiene adaptador en `infrastructure` con prueba de integración (Testcontainers o WireMock)"; incluir migración Flyway si aplica.
- **Refactorización:** rol arquitecto; el plan debe tener pasos pequeños, cada uno compilable y con pruebas verdes, y no cambiar códigos de error, contratos REST ni esquema.
- **Registrar un ADR:** `docs/adr/NNNN-titulo.md` con estado, contexto, decisión, al menos dos alternativas con pros y contras, y consecuencias; máximo una página, en español.

## 4. Prompts históricos

Los archivos numerados son los prompts que se usaron durante el curso, tal como se ejecutaron. Se conservan como evidencia del proceso (los citan `docs/ai-log.md`, `docs/reviews/` y `docs/design/`); no se usan para trabajo nuevo.

| Archivo | Uso | Reemplazado por |
|---|---|---|
| `001-review-01.md` | Revisión integral (sesión 02) | `/review` |
| `002.-refactoring-v1.md` | Refactor del dominio por agregado (sesión 03) | Plantilla §3 (refactorización) |
| `003-security-01.md` | Diseño de la seguridad 3A (sesión 03) | `docs/design/003-security-design.md` |
| `004-implement-usecase.md` | Caso de uso paso a paso (sesión 03) | `/implement-usecase` |
| `005-security-02.md` | Seguridad fase 1, pasos 1.6–1.8 (sesión 04) | `docs/design/003-security-design.md` |
| `006-cicd.md` | Puesta en marcha del CI/CD con parche (sesión 04) | `/pipeline` |
