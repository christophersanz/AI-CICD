# ADR-0003: No usar ArchUnit para verificar las reglas de arquitectura

- **Estado:** Aceptado
- **Fecha:** 2026-09-26

## Contexto

`architecture.md` exigía verificar las reglas de la sección 3 con ArchUnit. El stack de `CLAUDE.md` retiró ArchUnit y se decidió no incorporarlo.

## Decisión

No se agrega ArchUnit. Las reglas de arquitectura se garantizan por:

- La **estructura de módulos Maven**: `domain` no declara dependencias de frameworks y `application` solo depende de `domain` (y de `spring-tx` para `@Transactional`). Una importación prohibida no compila.
- La **revisión de código** con el prompt de la sección 5 de `docs/prompts/prompts.md`.

## Alternativas consideradas

1. **ArchUnit en `infrastructure/src/test`.** Pro: detecta automáticamente violaciones dentro de `infrastructure` (regla 9) y de nombres. Contra: dependencia y pruebas adicionales que el equipo decidió no mantener.
2. **Módulos Maven separados por adaptador.** Pro: el compilador también haría cumplir la regla 9. Contra: más módulos; `CLAUDE.md` pide no crear módulos sin necesidad.

## Consecuencias

- Positivas: menos dependencias y pruebas que mantener.
- Negativas: la regla 9 (aislamiento dentro de `infrastructure`) y las convenciones de nombres solo se verifican en revisión.
- Seguimiento: reconsiderar si aparecen violaciones repetidas en las revisiones.
