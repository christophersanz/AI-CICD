---
description: Genera o actualiza la documentación técnica de contract-api (JavaDoc, OpenAPI, diagramas, ADR, guías)
argument-hint: <javadoc | openapi | diagramas | adr | guias | todo> [módulo o tema]
---

# Rol
Actúa como arquitecto de software y redactor técnico. Documentas lo que el código **hace**, verificado contra el código, para tres lectores: quien mantiene el código, quien consume la API y quien evalúa la arquitectura.

# Contexto y fuentes de verdad
- @CLAUDE.md (tabla de documentación), @docs/architecture.md, @docs/business-rules.md.
- Documentos existentes: `docs/technical.md`, `docs/api/` (OpenAPI generado con springdoc), `docs/guides/` (arquitectura y CI/CD), `docs/adr/`, `docs/design/`, `README.md`.
- Alcance: **$ARGUMENTS** (vacío = muestra qué documentación falta o está desfasada y pide que elija).

# Objetivo
Que la documentación del alcance coincida con el código actual y responda, sin leer el código: qué hace, por qué se decidió así y cómo se usa o se opera.

# Restricciones
- **No cambies el comportamiento del código.** Solo comentarios JavaDoc, anotaciones OpenAPI (`@Operation`, `@ApiResponse`, `@Schema`) y archivos de documentación.
- No documentes lo que no puedes verificar; si el código y un documento se contradicen, repórtalo y pregunta cuál corregir.
- JavaDoc en español (como el resto de la documentación), sin repetir el nombre del método: explica la regla (`BR-*`), los errores que lanza y las precondiciones.
- Nada de secretos, contraseñas ni URLs internas en la documentación.

# Forma de trabajo
Crea o retoma `docs/sessions/AAAA-MM-DD-document-<alcance>.md` (docs/sessions/README.md).

## Paso 1 — Inventario (sin escribir)
Tabla `Elemento | Estado (falta / desfasado / correcto) | Evidencia` según el alcance:
- **javadoc:** clases y métodos públicos de `domain` (agregados, value objects, excepciones) y de `application` (puertos `UseCase`, servicios, DTOs) sin JavaDoc o con JavaDoc que no menciona su regla `BR-*`.
- **openapi:** endpoints y DTOs sin `@Operation`/`@ApiResponse`/`@Schema`, respuestas de error no documentadas (400, 401, 403, 404, 409, 422) y diferencias entre `docs/api/openapi.yaml` y el código.
- **diagramas:** diagramas Mermaid de `docs/guides/arquitectura.md`, `docs/guides/ci-cd.md` y `business-rules.md` §5 que no coinciden con el código (módulos, puertos, estados, pipeline). Faltantes útiles: secuencia de login/refresh, modelo de datos (ER desde las migraciones Flyway), contexto C4.
- **adr:** decisiones presentes en el código sin ADR (librerías, patrones, configuraciones de seguridad o del pipeline).
- **guias:** `README.md`, `technical.md` y las guías con comandos, puertos o variables que ya no existen.
Propón el plan priorizado y espera mi aprobación.

## Paso 2 — Escribir
Un bloque a la vez (un módulo de JavaDoc, un diagrama, un ADR), mostrando el diff y esperando "continuar".
- JavaDoc: propósito, regla `BR-*` que aplica, `@throws` con el código de error del catálogo.
- OpenAPI: regenera `docs/api/openapi.json` y `.yaml` siguiendo `docs/api/README.md`.
- Diagramas: Mermaid, verificados contra el código (nombres de clases y paquetes reales).
- ADR: estado, contexto, decisión, al menos dos alternativas con pros y contras, consecuencias; una página.

## Paso 3 — Verificar
1. `./mvnw clean verify` en verde (el JavaDoc mal formado o las anotaciones OpenAPI pueden romper la compilación).
2. Enlaces relativos entre documentos válidos y `CLAUDE.md` actualizado si se añadió un documento nuevo.

# Informe final
Elementos documentados por tipo (antes/después), documentos nuevos, contradicciones encontradas y cómo se resolvieron, y lo que queda pendiente.
