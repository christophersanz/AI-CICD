# 04-documentar javadoc

- **Inicio:** 2026-11-13 · **Rama:** `docs/javadoc` · **Estado:** terminado

## Objetivo
La API pública de `domain` y `application` tiene JavaDoc que explica, sin leer el código, qué hace cada tipo y método, qué regla `BR-*` aplica y qué errores del catálogo lanza; `./mvnw clean verify` y `javadoc` sin errores.

## Plan aprobado
1. [x] Puertos de entrada (10 `UseCase`)
2. [x] Puertos de salida (7)
3. [x] Excepciones de dominio y de aplicación (22)
4. [x] Eventos de dominio (6 + `DomainEvent`)
5. [x] Agregados y value objects (`Contract`, `Employee`, `User`, `RefreshToken`, VO, IDs, `Money`, `common/model`)
6. [x] DTO de aplicación (19)

## Decisiones
- Plan completo aprobado por el autor.
- La medición se hace con `javadoc -Xdoclint:all -Xmaxwarns 10000` directo: el plugin de Maven corta en 100 avisos por módulo y no regenera si nada cambió (la cifra de 200 del inventario estaba truncada; la real era 353 tras el bloque 1).
- Accessors con `{@return …}` (Java 16+), que sirve de descripción y de retorno en una línea.
- Se documentaron también los constructores de los 10 servicios `usecase` (fuera del plan, para cerrar el inventario en 0).

## Registro
- 2026-11-13 — Paso 1: inventario. Tipos públicos con JavaDoc: 53/108 (49 %); métodos públicos: 30/182 (16 %). Paquetes sin nada: `application/port/in` (0/10 tipos).

- 2026-11-13 — Bloques 1–6 aplicados, verificando `javadoc` después de cada uno.
- 2026-11-13 — Verificación: 108 archivos cambian **solo en comentarios** (comparación del código sin comentarios contra `master`); `javadoc` con 0 problemas y 0 faltantes; cada código de error y regla citados existe en `business-rules.md`; `./mvnw clean verify` → 409 pruebas en verde.

## Pendiente al retomar
Nada. PR abierto.
