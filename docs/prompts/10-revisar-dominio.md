# 10 · Ciclo de revisión 1/9 — Dominio

| | |
|---|---|
| **Comando** | `/10-revisar-dominio [NNN de la ronda de revisión; vacío = siguiente]` |
| **Archivo** | `docs/prompts/10-revisar-dominio.md` (única versión; los cambios se hacen aquí y quedan en el historial de git) |
| **Origen** | Nuevo en este proyecto (fuentes en `docs/prompts/README.md` §3) |

> `{ARGUMENTO}` es lo que se escribe después del comando. `DP.<clave>` es un dato del proyecto: está en `CLAUDE.md` → *Datos para los prompts*. Sin Claude Code, copia este prompt y reemplaza `{ARGUMENTO}` a mano.

## Rol
Actúa como arquitecto experto en DDD táctico y Java 21. Buscas evidencia en el código; no confirmas lo que ya se cree ni inventas problemas.

## Reglas comunes
Aplica `docs/prompts/09-reglas-revision.md`: orden del ciclo, fuentes de verdad, severidades, reglas y formato del informe. Esta revisión no modifica código.

## Alcance
Paquete `<DP.paquete-base>.domain` y sus pruebas (módulo `domain` de `DP.modulos`). **Revisa solo este alcance**; lo que veas fuera de él va como INFO con una línea, para la revisión que le corresponde.
Ronda: `{ARGUMENTO}` (vacía = el siguiente NNN libre en `docs/reviews/`, o el de la ronda en curso si ya existe un informe `10`–`16` sin su `18`).

## Comprobaciones
1. Java puro: sin Spring, JPA, Jakarta, Lombok, Jackson, HTTP ni imports de `application` o `infrastructure` (revisa los imports y `domain/pom.xml`).
2. Cada agregado de `business-rules.md` §3 protege las invariantes que `business-rules.md` §4 asigna a `domain`; sin setters públicos ni fábricas (`restore`, `reconstitute`) que permitan saltarse invariantes sin advertirlo.
3. Value objects (`record`) validados en el constructor con excepciones de dominio (o un `Guard` común), nunca `NullPointerException` ni `IllegalArgumentException`; normalización al crear; importes con `BigDecimal` y escala fija, nunca `float`/`double`.
4. Transiciones de estado según §5: las no dibujadas lanzan el código de transición inválida del catálogo (§8), con las excepciones que documente §5.
5. Eventos de §7 emitidos por el agregado dentro de sus métodos, con `eventId`, `occurredOn` desde un `Clock` y `aggregateId`; ningún `now()` en el dominio; no hay eventos que §7 no documente.
6. Dependencias entre paquetes según `architecture.md` §2 y `DP.adr-arquitectura`: `common` y `shared` no importan agregados; un agregado solo usa el identificador de otro.
7. `DomainException` y subclases con el `code` del catálogo (§8), sin conceptos HTTP, JPA ni SQL.
8. Sin abstracciones ni patrones fuera del alcance.
9. Pruebas de dominio sin Spring ni mocks para cada invariante, transición y caso límite.

## Entrega
Informe `docs/reviews/NNN-10-dominio.md` con el formato de las reglas comunes. **No modifiques ningún otro archivo.**
Siguiente comando del orden: `/11-revisar-aplicacion`.
