# 13 · Ciclo de revisión 4/9 — API REST

| | |
|---|---|
| **Comando** | `/13-revisar-api [NNN de la ronda de revisión; vacío = siguiente]` |
| **Archivo** | `docs/prompts/13-revisar-api.md` (única versión; los cambios se hacen aquí y quedan en el historial de git) |
| **Origen** | Nuevo en este proyecto (fuentes en `docs/prompts/README.md` §3) |

> `{ARGUMENTO}` es lo que se escribe después del comando. `DP.<clave>` es un dato del proyecto: está en `CLAUDE.md` → *Datos para los prompts*. Sin Claude Code, copia este prompt y reemplaza `{ARGUMENTO}` a mano.

## Rol
Actúa como arquitecto experto en diseño de APIs REST, Spring Web y OpenAPI. Buscas evidencia en el código; no confirmas lo que ya se cree ni inventas problemas.

## Reglas comunes
Aplica `docs/prompts/09-reglas-revision.md`: orden del ciclo, fuentes de verdad, severidades, reglas y formato del informe. Esta revisión no modifica código.

## Alcance
`infrastructure/api` (controllers, `dto`, mappers, `rest/error`), el contrato OpenAPI (`DP.openapi`) y las pruebas `@WebMvcTest`. **Revisa solo este alcance**; lo que veas fuera de él va como INFO con una línea, para la revisión que le corresponde.
Ronda: `{ARGUMENTO}` (vacía = el siguiente NNN libre en `docs/reviews/`, o el de la ronda en curso si ya existe un informe `10`–`16` sin su `18`).

## Comprobaciones
1. Controllers como adaptadores de entrada: solo usan puertos `port.in`; no inyectan repositorios, adaptadores ni clases de `security`; sin lógica de negocio.
2. Request/Response en `api/dto`, separados de los modelos de dominio, de `Command`/`Query`/`Result` y de las entidades JPA.
3. Mappers Request → Command y Result → Response, sin lógica de negocio.
4. Bean Validation solo de formato; no sustituye invariantes del dominio.
5. `@PreAuthorize("hasAuthority('<permiso>')")` en cada endpoint según `business-rules.md` §4.1; los públicos son solo los documentados.
6. Rutas en plural y kebab-case, verbos, versionado `/api/v1`, paginación y códigos de `architecture.md` §6 (`201` + `Location`, `204`, `400`, `401`/`403`, `404`, `409`, `422`).
7. Errores RFC 9457 (`application/problem+json`) con `type`, `title`, `status`, `detail` y `code`; todas las excepciones de negocio traducidas; sin stack traces ni detalles internos; 500 sin filtrar el mensaje.
8. OpenAPI completo (`@Operation`, `@ApiResponse` por código, `@Schema`) y el contrato de `DP.openapi` coincide con los endpoints actuales.
9. Pruebas `@WebMvcTest` del éxito, del 400, del 401 sin token, del 403 sin permiso y de cada error de negocio.

## Entrega
Informe `docs/reviews/NNN-13-api.md` con el formato de las reglas comunes. **No modifiques ningún otro archivo.**
Siguiente comando del orden: `/14-revisar-seguridad`.
