# 05-refactorizar ids

- **Inicio:** 2026-11-13 · **Rama:** `refactor/ids-sin-parseo-de-texto` · **Estado:** terminado

## Objetivo
Eliminar la duplicación de `ContractId.of(String)` y `EmployeeId.of(String)` sin cambiar el comportamiento observable (mismos contratos REST y códigos de error); `./mvnw clean verify` en verde con el mismo número de pruebas.

## Plan aprobado
1. [x] Eliminar `ContractId.of(String)` y `EmployeeId.of(String)` (código muerto) en lugar de extraerlos a un helper, y ajustar las aserciones de las pruebas que los usaban.

## Decisiones
- **Eliminar en lugar de extraer** (aprobado por el autor): extraer el parseo a `domain/common` crearía una abstracción para código sin usos; es el riesgo de sobreingeniería del paso 1.
- Fuera del alcance: `new EmployeeId(x)` y `EmployeeId.of(x)` conviven para lo mismo.

## Registro
- 2026-11-13 — Paso 1: diagnóstico. `of(String)` existe desde el primer commit (`8de5e00`) y no tiene ningún uso en `main`: la API recibe `@PathVariable UUID` y Spring convierte el texto. Solo lo usan 4 aserciones de `ContractValueObjectsTest` y `EmployeeValueObjectsTest`. El JavaDoc de `/04-documentar` («por ejemplo el de una ruta de la API») es incorrecto.

- 2026-11-13 — Paso 3: eliminados los dos métodos y 5 aserciones; 2 aserciones de igualdad pasan a `new XxxId(uuid)`. El diagnóstico había omitido un uso (`ContractValueObjectsTest:33`, `of(" " + uuid + " ")`), y se ajustó antes de compilar.
- 2026-11-13 — Paso 4: `./mvnw clean verify` en verde con 409 pruebas, antes y después; regla de dependencia cumplida (solo `java.*` y `domain.*`).

## Pendiente al retomar
- 94 bloques JavaDoc en 54 archivos (del PR #16) sin línea en blanco entre la descripción y los tags: corrección de estilo aparte (`/04-documentar`).
