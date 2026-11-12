---
description: Descubre y documenta reglas de negocio de contract-api preguntando al autor (no las inventa)
argument-hint: <tema, caso de uso o pregunta abierta>
---

# Rol
Actúa como analista de negocio y arquitecto DDD con experiencia en gestión de contratos laborales en Perú. Tu trabajo es **preguntar** hasta que la regla quede clara, no suponerla.

# Contexto y fuentes de verdad
- @docs/business-rules.md: glosario, reglas `BR-*`, estados, casos de uso, eventos, errores y **§9 Preguntas abiertas**.
- @docs/architecture.md (§3 reglas de arquitectura, §7 errores) y @CLAUDE.md.
- El código manda sobre lo implementado: confirma cada regla contra `domain` y `application` antes de proponer cambios.
- Tema de esta sesión: **$ARGUMENTS**

# Validación del argumento
Si `$ARGUMENTS` está vacío, no hagas nada más: lista las preguntas abiertas de §9 agrupadas por agregado (Contract, Employee, User) y los casos de uso de §6 que aún no tienen código, y pide que vuelva a ejecutar el comando con uno de ellos.

# Objetivo
Que el tema quede especificado sin ambigüedad en `business-rules.md`: cada regla con ID `BR-<CTX>-NNN`, capa donde se valida, código de error y caso de uso que la aplica; las preguntas resueltas marcadas `[x]` con su respuesta.

# Restricciones
- **No inventes reglas.** Puedes proponer opciones con sus pros y contras, pero la decisión es del autor.
- Las reglas con base en normativa laboral peruana se marcan con ⚖️ y la nota de validar con RR.HH./asesoría legal; no cites artículos de ley que no puedas verificar.
- No escribas código en este comando: el resultado es documentación. La implementación va después con `/implement-usecase`.
- No borres reglas existentes: si una cambia, explica el impacto en el código y las pruebas que la cubren.

# Forma de trabajo
Crea o retoma el archivo de sesión `docs/sessions/AAAA-MM-DD-business-rules-<tema>.md` (docs/sessions/README.md) y actualízalo al terminar cada paso.

## Paso 1 — Diagnóstico (sin cambiar nada)
1. Qué dicen hoy `business-rules.md` y el código sobre el tema (reglas, estados, errores, casos de uso afectados).
2. Vacíos y contradicciones: reglas que el código aplica y no están documentadas, reglas documentadas sin implementar, preguntas abiertas relacionadas.
3. Escenarios concretos que hoy no tienen respuesta (p. ej. "¿qué pasa si se renueva un contrato el mismo día que vence?").

## Paso 2 — Preguntas
Hazme como máximo **7 preguntas**, cada una con:
- el escenario concreto que la motiva;
- 2–3 opciones de respuesta con su consecuencia (en el dominio, la API y los datos);
- tu recomendación, marcada como tal.
**Detente y espera mis respuestas.** Si una respuesta abre otra duda, pregúntala antes de seguir.

## Paso 3 — Propuesta de cambios
Con mis respuestas, presenta el diff propuesto de `business-rules.md`:
- glosario (términos nuevos, en español y en código);
- reglas nuevas o modificadas en §4, con ID consecutivo, capa y error;
- estados y transiciones (§5, diagrama Mermaid incluido) si cambian;
- casos de uso (§6 y su detalle numerado), eventos (§7) y catálogo de errores (§8);
- §9: preguntas resueltas `[x]` con su respuesta y preguntas nuevas `[ ]`.
**Detente y espera mi aprobación.**

## Paso 4 — Aplicar y cerrar
1. Aplica el diff aprobado en `business-rules.md`.
2. Lista el impacto en el código: clases, pruebas y endpoints que habrá que cambiar, y el comando sugerido (`/implement-usecase <Nombre>` o `/tests <Clase>`).
3. Si la decisión es arquitectónica (no solo de negocio), propón el ADR.

# Informe final
Reglas añadidas/cambiadas (IDs), preguntas resueltas y pendientes, impacto en el código y siguiente comando recomendado.
