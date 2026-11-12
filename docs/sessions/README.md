# Archivos de sesión

Cada prompt de `docs/prompts/` (o su comando de Claude Code) guarda aquí su progreso. Así una tarea larga se puede retomar en otra sesión de Claude Code (o después de compactar el contexto) sin volver a explicar todo ni repetir trabajo: al empezar, Claude lee el archivo y continúa desde el último paso.

`/06-revisar` no crea archivo de sesión: no modifican nada y su informe en `docs/reviews/` cumple esa función.

## Nombre

`AAAA-MM-DD-<comando>-<tema>.md`, por ejemplo `2026-11-12-02-implementar-caso-uso-DeactivateEmployeeUseCase.md`.

## Contenido

```markdown
# <comando> <tema>

- **Inicio:** AAAA-MM-DD · **Rama:** <rama> · **Estado:** en curso | terminado | abandonado

## Objetivo
<criterio verificable de terminado>

## Plan aprobado
1. [x] Paso 0 — ...
2. [ ] Paso 1 — ...

## Decisiones
- <decisión> — <motivo> (<quién la tomó: autor | Claude, aprobada por el autor>)

## Preguntas abiertas
- <pregunta> — <regla o sección afectada>

## Registro
- AAAA-MM-DD — Paso N: <qué se hizo>; archivos: <rutas>; verificación: <resultado de ./mvnw clean verify / Jenkins>.

## Pendiente al retomar
<siguiente acción concreta>
```

## Reglas

- Se actualiza al terminar **cada** paso, no solo al final.
- Al retomar: si existe un archivo con el mismo comando y tema en estado *en curso*, se lee primero y se continúa desde *Pendiente al retomar*.
- Solo hechos verificables: archivos, resultados de comandos, decisiones aprobadas. Nada de secretos, tokens ni datos personales.
- Se versiona junto con el cambio (va en el mismo PR): es la evidencia de cómo se trabajó con IA y alimenta `docs/ai-log.md`.
