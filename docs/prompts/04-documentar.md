# 04 · Documentación técnica

| | |
|---|---|
| **Comando** | `/04-documentar <javadoc | openapi | diagramas | adr | guias | todo> [módulo o tema]` |
| **Archivo** | `docs/prompts/04-documentar.md` (única versión; los cambios se hacen aquí y quedan en el historial de git) |
| **Origen** | Nuevo en este proyecto (fuentes en `docs/prompts/README.md` §3) |

> `{ARGUMENTO}` es lo que se escribe después del comando. `DP.<clave>` es un dato del proyecto: está en `CLAUDE.md` → *Datos para los prompts*. Sin Claude Code, copia este prompt y reemplaza `{ARGUMENTO}` a mano.

## Rol
Actúa como arquitecto de software y redactor técnico. Documentas lo que el código **hace**, verificado contra el código, para tres lectores: quien mantiene el código, quien consume la API y quien evalúa la arquitectura.

## Contexto y fuentes de verdad
- Lee: `CLAUDE.md` (tabla de documentación), `docs/architecture.md`, `docs/business-rules.md`.
- Documentos existentes: `docs/technical.md`, el contrato OpenAPI (`DP.openapi`), las guías (`DP.guias`), `docs/adr/`, `docs/design/` y `README.md`.
- Alcance: **{ARGUMENTO}** (vacío = muestra qué documentación falta o está desfasada y pide que elija).

## Objetivo
Que la documentación del alcance coincida con el código actual y responda, sin leer el código: qué hace, por qué se decidió así y cómo se usa o se opera.

## Restricciones
- **No cambies el comportamiento del código.** Solo comentarios JavaDoc, anotaciones OpenAPI (`@Operation`, `@ApiResponse`, `@Schema`) y archivos de documentación.
- No documentes lo que no puedes verificar; si el código y un documento se contradicen, repórtalo y pregunta cuál corregir.
- JavaDoc en español (como el resto de la documentación), sin repetir el nombre del método: explica la regla (`BR-*`), los errores que lanza y las precondiciones.
- Nada de secretos, contraseñas ni URLs internas en la documentación.

## Forma de trabajo
- Rama `docs/<alcance>` desde `DP.rama-principal` actualizado; al terminar, commit en español, push y PR. No integres el PR.
- Crea o retoma `docs/sessions/AAAA-MM-DD-04-documentar-<alcance>.md` (docs/sessions/README.md).

### Paso 1 — Inventario (sin escribir)
Tabla `Elemento | Estado (falta / desfasado / correcto) | Evidencia` según el alcance:
- **javadoc:** clases y métodos públicos de `domain` (agregados, value objects, excepciones) y de `application` (puertos `UseCase`, servicios, DTOs) sin JavaDoc o con JavaDoc que no menciona su regla `BR-*`.
- **openapi:** endpoints y DTOs sin `@Operation`/`@ApiResponse`/`@Schema`, respuestas de error no documentadas (400, 401, 403, 404, 409, 422) y diferencias entre el contrato de `DP.openapi` y el código.
- **diagramas:** diagramas Mermaid de `DP.guias` y de `business-rules.md` §5 que no coinciden con el código (módulos, puertos, estados, pipeline). Faltantes útiles: secuencia de login/refresh, modelo de datos (ER desde las migraciones de `DP.migraciones`), contexto C4.
- **adr:** decisiones presentes en el código sin ADR (librerías, patrones, configuraciones de seguridad o del pipeline).
- **guias:** `README.md`, `technical.md` y las guías con comandos, puertos o variables que ya no existen.
Propón el plan priorizado y espera mi aprobación.

### Paso 2 — Escribir
Un bloque a la vez (un módulo de JavaDoc, un diagrama, un ADR), mostrando el diff y esperando "continuar".
- JavaDoc: propósito, regla `BR-*` que aplica, `@throws` con el código de error del catálogo.
- OpenAPI: regenera el contrato como indica `DP.openapi`.
- Diagramas: Mermaid, verificados contra el código (nombres de clases y paquetes reales).
- ADR: estado, contexto, decisión, al menos dos alternativas con pros y contras, consecuencias; una página.

### Paso 3 — Verificar
1. `DP.build` en verde (el JavaDoc mal formado o las anotaciones OpenAPI pueden romper la compilación).
2. Enlaces relativos entre documentos válidos y `CLAUDE.md` actualizado si se añadió un documento nuevo.

## Informe final
Elementos documentados por tipo (antes/después), documentos nuevos, contradicciones encontradas y cómo se resolvieron, resultado de `DP.build`, PR abierto y lo que queda pendiente.
