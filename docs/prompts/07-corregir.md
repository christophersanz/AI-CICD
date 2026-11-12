# 07 · Corregir hallazgos (de una revisión o del Quality Gate)

| | |
|---|---|
| **Comando** | `/07-corregir <modo>` — modos: `ronda <NNN> [solo: H-01,H-03 \| excluir: H-05]`, `sonar [regla java:Sxxxx \| archivo]` |
| **Archivo** | `docs/prompts/07-corregir.md` (única versión; los cambios se hacen aquí y quedan en el historial de git) |
| **Origen** | Nuevo en este proyecto: prompt `06-fix-architecture` de los modelos del curso y correcciones del Quality Gate (ADR de `DP.sonarqube`) |

> `{ARGUMENTO}` es lo que se escribe después del comando. `DP.<clave>` es un dato del proyecto: está en `CLAUDE.md` → *Datos para los prompts*. Sin Claude Code, copia este prompt y reemplaza `{ARGUMENTO}` a mano.

> **Es el único prompt del ciclo de revisión autorizado a modificar código** (paso 8 del orden de `/06-revisar`). Corrige hallazgos; no agrega funcionalidades.

## Rol
Actúa como desarrollador senior de Java y Spring Boot experto en Clean Architecture, DDD y calidad y seguridad de código (reglas de SonarQube, OWASP). Corriges solo lo que verificaste: un falso positivo se explica con evidencia, no se "corrige".

## Contexto y fuentes de verdad
- Lee: `CLAUDE.md`, `docs/architecture.md`, `docs/business-rules.md` y los ADR de `docs/adr/`.
- **`ronda <NNN>`:** los informes `docs/reviews/NNN-1-…` a `NNN-7-…` de esa ronda (o los indicados), hechos con `/06-revisar`.
- **`sonar`:** el Quality Gate de `DP.sonarqube` (URL, proyecto, credenciales y dónde se definen sus condiciones). En adelante, `<proyecto>` es la clave del proyecto en SonarQube.

## Validación del argumento
Si `{ARGUMENTO}` está vacío, lista las rondas de `docs/reviews/` con ERROR o WARNING y sin informe `NNN-8-correcciones.md`, y el estado del Quality Gate si está disponible; pide que elija y detente.

## Objetivo
Cada hallazgo seleccionado queda **corregido y verificado** (con una prueba si es un bug), **justificado** en el código, **descartado** como falso positivo con su evidencia o **diferido** con su motivo; `DP.build` en verde y el Quality Gate sin empeorar (en el modo `sonar`, en verde).

## Restricciones
1. No agregues funcionalidades ni cambies la arquitectura definida; no introduzcas patrones fuera del alcance (CQRS, event sourcing, mediator, specification, nuevos módulos) ni dependencias sin justificar.
2. Mantén la separación `domain` / `application` / `infrastructure` y la de modelos (dominio, `Command`/`Query`/`Result`, Request/Response, entidades JPA). **No muevas reglas del dominio** a `application` o a los adaptadores para simplificar, ni adaptes el dominio a JPA.
3. No cambies códigos de error, contratos REST ni el esquema salvo que el hallazgo lo exija; si lo exige, dilo en el plan (impacto en clientes, migración nueva en `DP.migraciones`, `DP.openapi` y `DP.postman`).
4. Si un hallazgo depende de una regla no documentada o de una pregunta abierta (§9), no lo resuelvas suponiendo: deriva a `/01-definir reglas <tema>`.
5. **Modo `sonar`:** no marques hallazgos como "Safe", "Accepted" o "False positive" en el panel (esa decisión vive solo en la base de SonarQube y no se revisa); las excepciones van en el código con `@SuppressWarnings("java:Sxxxx")` sobre el elemento más pequeño posible y un comentario que explique **por qué** no aplica. No desactives reglas, no cambies el Quality Gate ni excluyas archivos para que pase: si una condición parece inadecuada, propón el cambio donde se definen las condiciones y su ADR.
6. Una justificación de seguridad (CSRF, enumeración, criptografía, inyección) siempre requiere mi aprobación explícita.
7. Las credenciales se leen en una variable de shell, nunca se imprimen ni se escriben en archivos versionados. No integres el PR.

## Forma de trabajo
Rama `fix/revision-NNN` o `fix/sonar-<tema>` desde `DP.rama-principal` actualizado. Crea o retoma `docs/sessions/AAAA-MM-DD-07-corregir-<modo>-<tema>.md` (`docs/sessions/README.md`).

### Paso 1 — Leer y verificar (sin modificar)
- **`ronda`:** vuelve a comprobar **contra el código actual** cada ERROR y WARNING de los informes.
- **`sonar`:** lee el estado del gate (`GET /api/qualitygates/project_status?projectKey=<proyecto>`), los hallazgos abiertos (`GET /api/issues/search?componentKeys=<proyecto>&resolved=false&types=BUG,VULNERABILITY`, y `CODE_SMELL` si el rating bajó de A) y los hotspots (`GET /api/hotspots/search?project=<proyecto>`; en versiones recientes el parámetro es `project`, no `projectKey`). Para cada uno, explica qué dice la regla y si el riesgo **existe en este código** (flujo real, configuración, pruebas).

### Paso 2 — Plan
Tabla con la decisión de cada hallazgo:

| Hallazgo o regla | Severidad | Decisión (Corregir / Justificar / Falso positivo / Diferir) | Archivo | Problema | Cambio propuesto o texto de la justificación | Regla o criterio |
|---|---|---|---|---|---|---|

- **Falso positivo:** evidencia de que el código cumple. **Diferir:** motivo (fuera de alcance, requiere decisión de negocio, costo/beneficio) y el comando que lo resolverá.
- Si es un bug: la prueba de regresión que lo reproduce.
- Riesgos y orden de ejecución.
**Detente y espera mi aprobación** (hallazgo por hallazgo si hay alguno de seguridad). Puedo mover hallazgos entre "Corregir" y "Diferir".

### Paso 3 — Corregir
- Un hallazgo (o un grupo pequeño del mismo archivo) a la vez.
- Si es un bug, primero la prueba que lo reproduce, **confirmada en rojo** con el código actual; después la corrección.
- Si una prueba falla tras un cambio, **investiga la causa raíz antes de tocar más código**; corrige solo problemas verificados y no reescribas código no relacionado.

### Paso 4 — Verificar
1. `DP.build`: compilación, pruebas (ejecutadas, exitosas, fallidas, con error, omitidas) y cobertura por módulo.
2. Regla de dependencia de nuevo en los archivos tocados y sin dependencias nuevas no permitidas.
3. Si cambió código de producción, o en el modo `sonar`: build del CI local (`DP.ci-local`) contra la rama (cómo apuntarlo a otra rama: `DP.ci-local`; vuelve a la rama principal al terminar) y todas las condiciones del Quality Gate en `OK`.

### Paso 5 — Informe y PR
- **`ronda`:** guarda `docs/reviews/NNN-8-correcciones.md`. **`sonar`:** el informe va en la descripción del PR.
- Contenido:
  1. Tabla `Hallazgo | Decisión | Archivos modificados | Prueba o justificación | Verificado (sí/no)`.
  2. Archivos creados, modificados, movidos y eliminados, cada uno con su hallazgo.
  3. Resultado exacto de `DP.build` y del Quality Gate (valor de cada condición).
  4. **Diferidos deliberadamente** (para que `/06-revisar final` no los reporte como defectos) y falsos positivos con su evidencia.
  5. Pendientes y por qué; si no hay: "Sin problemas pendientes". Si propusiste cambiar el gate, el ADR pendiente.
- Commit en español, push y PR con la plantilla completa. Siguiente paso: `/06-revisar final` (modo `ronda`).

**No declares resuelto un hallazgo que no pudiste verificar. No afirmes que la aplicación funciona si la compilación o las pruebas fallan.**
