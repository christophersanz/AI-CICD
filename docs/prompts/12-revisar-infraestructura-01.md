# 12 · Ciclo de revisión 3/9 — Infraestructura (v01)

| | |
|---|---|
| **Comando** | `/12-revisar-infraestructura [NNN de la ronda de revisión; vacío = siguiente]` |
| **Archivo** | `docs/prompts/12-revisar-infraestructura-01.md` |
| **Versión anterior** | — |

> `{ARGUMENTO}` es lo que se escribe después del comando. Sin Claude Code, copia este prompt y reemplaza `{ARGUMENTO}` a mano.

## Rol
Actúa como arquitecto experto en Spring Boot 3, Spring Data JPA, Flyway y Spring Security. Buscas evidencia en el código; no confirmas lo que ya se cree ni inventas problemas.

## Reglas comunes
Aplica `docs/prompts/09-reglas-revision-01.md`: orden del ciclo, fuentes de verdad, severidades, reglas y formato del informe. Esta revisión no modifica código.

## Alcance
`infrastructure` **excepto** `infrastructure/api`: `persistence`, `client`, `security`, `config`, migraciones Flyway (`src/main/resources/db/migration`), `application*.yml` y sus pruebas. **Revisa solo este alcance**; lo que veas fuera de él va como INFO con una línea, para la revisión que le corresponde.
Ronda: `{ARGUMENTO}` (vacía = el siguiente NNN libre en `docs/reviews/`, o el de la ronda en curso si ya existe un informe `10`–`16` sin su `18`).

## Comprobaciones
1. Cada puerto de salida tiene un adaptador que lo implementa; los adaptadores dependen de los puertos y no al revés; `persistence`, `client` y `security` no dependen de `api`.
2. Entidades JPA (`<Entidad>JpaEntity`) separadas del dominio; mappers dominio ↔ JPA sin lógica de negocio; el dominio y los puertos no se adaptaron a JPA.
3. Repositorios Spring Data como mecanismo técnico, no como puertos; sin entidades JPA fuera de `persistence`.
4. Migraciones coherentes con las entidades: `NOT NULL`, únicos (número de contrato, documento, username), claves foráneas e índices para las consultas de las reglas; migraciones aplicadas que no se editaron.
5. Adaptadores de seguridad (`infrastructure/security`): implementan los puertos de identidad de `application` y no tienen reglas de negocio; el análisis de seguridad en sí (tokens, autorización, secretos) es de `/14-revisar-seguridad`.
6. Configuración: perfiles `local`/`dev`/`prod`, secretos solo por variables de entorno, `open-in-view` desactivado, logs JSON sin datos sensibles.
7. Rendimiento: N+1, `FetchType.EAGER` innecesario, consultas sin paginar.
8. Pruebas de integración con Testcontainers (PostgreSQL real, nunca H2) para los adaptadores y las restricciones de la base.

## Entrega
Informe `docs/reviews/NNN-12-infraestructura.md` con el formato de las reglas comunes. **No modifiques ningún otro archivo.**
Siguiente comando del orden: `/13-revisar-api`.
