---
description: Ciclo de revisión 1/9: revisa solo la capa domain de contract-api (sin modificar código)
argument-hint: [NNN de la ronda de revisión; vacío = siguiente]
---

# Rol
Actúa como arquitecto experto en DDD táctico y Java 21. Buscas evidencia en el código; no confirmas lo que ya se cree ni inventas problemas.

# Reglas comunes
@docs/prompts/review-rules.md

Fuentes de verdad: @CLAUDE.md, @docs/architecture.md, @docs/business-rules.md, `docs/technical.md` y `docs/adr/`.

# Alcance
Paquete `pe.edu.galaxy.training.ia.ca.domain` y sus pruebas (`domain/src/test`). **Revisa solo este alcance**; lo que veas fuera de él va como INFO con una línea, para la revisión que le corresponde.
Ronda: `$ARGUMENTS` (vacía = el siguiente NNN libre en `docs/reviews/`, o el de la ronda en curso si ya existe un informe `10`–`16` sin su `18`).

# Comprobaciones
1. Java puro: sin Spring, JPA, Jakarta, Lombok, Jackson, HTTP ni imports de `application` o `infrastructure` (revisa los imports y `domain/pom.xml`).
2. Cada agregado (`Contract`, `Employee`, `User`) protege las invariantes que `business-rules.md` §4 asigna a `domain`; sin setters públicos ni fábricas (`restore`, `reconstitute`) que permitan saltarse invariantes sin advertirlo.
3. Value objects (`record`) validados en el constructor con `Guard` o excepciones de dominio, nunca `NullPointerException` ni `IllegalArgumentException`; normalización al crear; `Money` con `BigDecimal` y escala 2, nunca `float`/`double`.
4. Transiciones de estado según §5: las no dibujadas lanzan `CTR-008`/`EMP-005`, con las excepciones documentadas (`renew()` → `CTR-005`; `expire(fechaCorte)`).
5. Eventos de §7 emitidos por el agregado dentro de sus métodos, con `eventId`, `occurredOn` desde un `Clock` y `aggregateId`; ningún `now()` en el dominio; no hay eventos que §7 no documente.
6. Dependencias entre paquetes según ADR-0004: `common` y `shared` no importan agregados; un agregado solo usa el identificador de otro.
7. `DomainException` y subclases con el `code` del catálogo (§8), sin conceptos HTTP, JPA ni SQL.
8. Sin abstracciones ni patrones fuera del alcance.
9. Pruebas de dominio sin Spring ni mocks para cada invariante, transición y caso límite.

# Entrega
Informe `docs/reviews/NNN-10-dominio.md` con el formato de las reglas comunes. **No modifiques ningún otro archivo.**
Siguiente comando del orden: `/11-revisar-aplicacion`.
