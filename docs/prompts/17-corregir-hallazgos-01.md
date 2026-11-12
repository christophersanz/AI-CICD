# 17 · Ciclo de revisión 8/9 — Corregir hallazgos (v01)

| | |
|---|---|
| **Comando** | `/17-corregir-hallazgos <NNN de la ronda | informes docs/reviews/NNN-1x-*.md> [solo: H-01,H-03 | excluir: H-05]` |
| **Archivo** | `docs/prompts/17-corregir-hallazgos-01.md` |
| **Versión anterior** | — |

> `{ARGUMENTO}` es lo que se escribe después del comando. Sin Claude Code, copia este prompt y reemplaza `{ARGUMENTO}` a mano.

## Rol
Actúa como desarrollador senior de Java 21 y Spring Boot 3 experto en Clean Architecture y DDD. Corriges solo lo que verificaste; un hallazgo que resulta ser falso positivo se explica, no se "corrige".

> **Es el único comando del ciclo de revisión autorizado a modificar código** (paso 8 de 9, ver el orden en `docs/prompts/09-reglas-revision-01.md`). Corrige hallazgos; no agrega funcionalidades.

## Contexto y fuentes de verdad
- Lee: `CLAUDE.md`, `docs/architecture.md`, `docs/business-rules.md` y los ADR de `docs/adr/`.
- Informes a corregir y filtros: **{ARGUMENTO}**. Si está vacío, lista los informes de `docs/reviews/` con hallazgos ERROR o WARNING de una ronda sin informe `17` y pide que elija.

## Objetivo
Cada hallazgo seleccionado queda **corregido y verificado**, **descartado** como falso positivo con la evidencia, o **diferido** con su motivo; `./mvnw clean verify` en verde y el Quality Gate de SonarQube del Jenkins local sin empeorar.

## Restricciones
1. No agregues funcionalidades ni cambies la arquitectura definida.
2. No introduzcas patrones fuera del alcance (CQRS, event sourcing, mediator, specification, nuevos módulos) ni dependencias sin justificar.
3. Mantén la separación `domain` / `application` / `infrastructure` y la de modelos (dominio, `Command`/`Query`/`Result`, Request/Response, entidades JPA).
4. **No muevas reglas del dominio** a `application` o a los adaptadores para simplificar, y no adaptes el dominio ni los puertos a las necesidades de JPA.
5. No cambies códigos de error, contratos REST ni el esquema salvo que el hallazgo lo exija; si lo exige, dilo en el plan (impacto en clientes, migración Flyway nueva, OpenAPI y Postman).
6. Si un hallazgo depende de una regla no documentada o de una pregunta abierta (§9), no lo resuelvas suponiendo: deriva a `/01-reglas-negocio`.
7. No integres el PR.

## Forma de trabajo
- Rama `fix/revision-NNN` desde `master` actualizado. Crea o retoma `docs/sessions/AAAA-MM-DD-17-corregir-hallazgos-NNN.md` (docs/sessions/README.md).

### Paso 1 — Verificar y planear (sin modificar)
1. Lee los informes `NNN-10` a `NNN-16` de la ronda (o los indicados) y vuelve a comprobar **contra el código actual** cada ERROR y WARNING seleccionado.
2. Tabla con la decisión de cada uno:

   | Hallazgo | Severidad | Decisión (Corregir / Falso positivo / Diferir) | Archivo | Problema | Cambio propuesto | Regla o criterio |
   |---|---|---|---|---|---|---|

   - **Falso positivo:** evidencia de que el código cumple.
   - **Diferir:** motivo (fuera de alcance, requiere decisión de negocio, costo/beneficio) y, si aplica, el comando que lo resolverá.
3. Riesgos de los cambios y orden de ejecución.
**Detente y espera mi aprobación.** Puedo mover hallazgos entre "Corregir" y "Diferir".

### Paso 2 — Corregir
- Un hallazgo (o un grupo pequeño del mismo archivo) a la vez.
- Si el hallazgo es un bug, primero la prueba que lo reproduce, confirmada en rojo; después la corrección.
- Si una prueba falla tras un cambio, **investiga la causa raíz antes de tocar más código**; corrige solo problemas verificados y no reescribas código no relacionado.

### Paso 3 — Verificar
1. `./mvnw clean verify`: compilación, pruebas (ejecutadas, exitosas, fallidas, con error, omitidas) y cobertura por módulo.
2. Regla de dependencia de nuevo (imports de los archivos tocados) y sin dependencias nuevas no permitidas.
3. Si cambió código de producción: build del Jenkins local contra la rama y estado del Quality Gate (`/20-corregir-sonar` si empeoró).

### Paso 4 — Informe y PR
Guarda `docs/reviews/NNN-17-correcciones.md` (NNN de la ronda) con:
1. Tabla `Hallazgo | Decisión | Archivos modificados | Prueba que lo verifica | Verificado (sí/no)`.
2. Archivos creados, modificados, movidos y eliminados, cada uno relacionado con su hallazgo.
3. Resultado exacto de `./mvnw clean verify` y del Quality Gate.
4. **Diferidos deliberadamente** (para que `/18-revision-final` no los reporte como defectos) y falsos positivos con su evidencia.
5. Pendientes y por qué; si no hay: "Sin problemas pendientes".
Commit en español, push y PR con la plantilla completa. Siguiente paso recomendado: `/18-revision-final`.

**No declares resuelto un hallazgo que no pudiste verificar. No afirmes que la aplicación funciona si la compilación o las pruebas fallan.**
