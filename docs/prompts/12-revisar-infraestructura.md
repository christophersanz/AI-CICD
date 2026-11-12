# 12 · Ciclo de revisión 3/9 — Infraestructura

| | |
|---|---|
| **Comando** | `/12-revisar-infraestructura [NNN de la ronda de revisión; vacío = siguiente]` |
| **Archivo** | `docs/prompts/12-revisar-infraestructura.md` (única versión; los cambios se hacen aquí y quedan en el historial de git) |
| **Origen** | Nuevo en este proyecto (fuentes en `docs/prompts/README.md` §3) |

> `{ARGUMENTO}` es lo que se escribe después del comando. `DP.<clave>` es un dato del proyecto: está en `CLAUDE.md` → *Datos para los prompts*. Sin Claude Code, copia este prompt y reemplaza `{ARGUMENTO}` a mano.

## Rol
Actúa como arquitecto experto en Spring Boot, Spring Data JPA, migraciones de base de datos y Spring Security. Buscas evidencia en el código; no confirmas lo que ya se cree ni inventas problemas.

## Reglas comunes
Aplica `docs/prompts/09-reglas-revision.md`: orden del ciclo, fuentes de verdad, severidades, reglas y formato del informe. Esta revisión no modifica código.

## Alcance
`infrastructure` **excepto** `infrastructure/api`: `persistence`, `client`, `security`, `config`, migraciones (`DP.migraciones`), `application*.yml` y sus pruebas. **Revisa solo este alcance**; lo que veas fuera de él va como INFO con una línea, para la revisión que le corresponde.
Ronda: `{ARGUMENTO}` (vacía = el siguiente NNN libre en `docs/reviews/`, o el de la ronda en curso si ya existe un informe `10`–`16` sin su `18`).

## Comprobaciones
1. Cada puerto de salida tiene un adaptador que lo implementa; los adaptadores dependen de los puertos y no al revés; `persistence`, `client` y `security` no dependen de `api`.
2. Entidades JPA (`<Entidad>JpaEntity`) separadas del dominio; mappers dominio ↔ JPA sin lógica de negocio; el dominio y los puertos no se adaptaron a JPA.
3. Repositorios Spring Data como mecanismo técnico, no como puertos; sin entidades JPA fuera de `persistence`.
4. Migraciones coherentes con las entidades: `NOT NULL`, los únicos que exige `business-rules.md`, claves foráneas e índices para las consultas de las reglas; migraciones aplicadas que no se editaron.
5. Adaptadores de seguridad (`infrastructure/security`): implementan los puertos de identidad de `application` y no tienen reglas de negocio; el análisis de seguridad en sí (tokens, autorización, secretos) es de `/14-revisar-seguridad`.
6. Configuración: perfiles `local`/`dev`/`prod`, secretos solo por variables de entorno, `open-in-view` desactivado, logs JSON sin datos sensibles.
7. Rendimiento: N+1, `FetchType.EAGER` innecesario, consultas sin paginar.
8. Pruebas de integración según `DP.pruebas-integracion` para los adaptadores y las restricciones de la base.

## Entrega
Informe `docs/reviews/NNN-12-infraestructura.md` con el formato de las reglas comunes. **No modifiques ningún otro archivo.**
Siguiente comando del orden: `/13-revisar-api`.
