# 03-pruebas ContractPeriod

- **Inicio:** 2026-11-12 · **Rama:** `test/contract-period` · **Estado:** terminado

## Objetivo
Cada regla y error que toca `ContractPeriod` (BR-CTR-002, 003, 005, 007, 008, 009; `CTR-001`, `VAL-001`) tiene al menos una prueba que falla si la regla se rompe; los casos límite y las ramas sin cubrir (líneas 80, 82, 102, 112–118, 123) quedan cubiertos; `./mvnw clean verify` en verde.

## Plan aprobado
1. [x] Verificar el código de error en las 4 pruebas de rechazo existentes
2. [x] Fin anterior e igual al inicio (parametrizada)
3. [x] Solapamiento simétrico con tabla de pares (cubre líneas 80 y 82)
4. [x] `contains` como tabla de fechas
5. [x] Duración mínima de 2 días
6. [x] `extendTo(null)` y fecha anterior (cubre línea 102)
7. [x] `equals`/`hashCode` (cubre líneas 112–118)
8. [x] `contains(null)` y `overlaps(null)` → `VAL-001`
9. [x] `toString` (cubre línea 123)

## Decisiones
- Plan completo aprobado por el autor, incluida la reorganización en `@Nested` y la prueba de `toString`.

## Registro
- 2026-11-12 — Paso 1: diagnóstico (13 pruebas existentes; 7 ramas y `hashCode` sin cubrir; 6 pruebas sin verificar el código de error; 3 pruebas con varias razones de fallo).

- 2026-11-12 — Paso 2: `ContractPeriodTest` reorganizado en 7 grupos `@Nested`; 49 ejecuciones en verde.
- 2026-11-12 — Paso 3: 4 mutaciones temporales del código de producción, todas detectadas y revertidas; `./mvnw clean verify` → 409 pruebas en verde; `ContractPeriod` con 100 % de líneas y de ramas.

## Pendiente al retomar
Nada. PR abierto.
