# 11 · Ciclo de revisión 2/9 — Aplicación

| | |
|---|---|
| **Comando** | `/11-revisar-aplicacion [NNN de la ronda de revisión; vacío = siguiente]` |
| **Archivo** | `docs/prompts/11-revisar-aplicacion.md` (única versión; los cambios se hacen aquí y quedan en el historial de git) |
| **Origen** | Nuevo en este proyecto (fuentes en `docs/prompts/README.md` §3) |

> `{ARGUMENTO}` es lo que se escribe después del comando. `DP.<clave>` es un dato del proyecto: está en `CLAUDE.md` → *Datos para los prompts*. Sin Claude Code, copia este prompt y reemplaza `{ARGUMENTO}` a mano.

## Rol
Actúa como arquitecto experto en Clean Architecture y casos de uso. Buscas evidencia en el código; no confirmas lo que ya se cree ni inventas problemas.

## Reglas comunes
Aplica `docs/prompts/09-reglas-revision.md`: orden del ciclo, fuentes de verdad, severidades, reglas y formato del informe. Esta revisión no modifica código.

## Alcance
Paquete `<DP.paquete-base>.application` y sus pruebas (módulo `application` de `DP.modulos`). **Revisa solo este alcance**; lo que veas fuera de él va como INFO con una línea, para la revisión que le corresponde.
Ronda: `{ARGUMENTO}` (vacía = el siguiente NNN libre en `docs/reviews/`, o el de la ronda en curso si ya existe un informe `10`–`16` sin su `18`).

## Comprobaciones
1. Depende solo de `domain` y, como máximo, de `@Transactional` (`application/pom.xml` y imports); sin JPA, REST, Spring Security ni `infrastructure`.
2. Puertos de entrada en `port/in` con el patrón `<Verbo><Entidad>UseCase`; puertos de salida en `port/out` (`<Entidad>RepositoryPort`, `<Sistema>ClientPort`, puertos de identidad) con métodos en términos del dominio y sin tipos técnicos.
3. `Command`/`Query`/`Result` como records inmutables en `dto`, con validación mínima de presencia (no reglas de negocio).
4. Un servicio por caso de uso (`<Verbo><Entidad>Service`), sin `@Service`, registrado como bean en `infrastructure/config`.
5. Los servicios orquestan (cargar → delegar al dominio → validar reglas de `application` → persistir) sin absorber reglas que §4 asigna a `domain`.
6. Reglas que §4 asigna a `application` implementadas (típicamente existencia, unicidad, reglas entre agregados, límites acumulados y autorización por alcance), según lo implementado.
7. Límites transaccionales: un `@Transactional` por caso de uso, sin llamadas externas dentro de la transacción que no lo requieran.
8. Excepciones de aplicación (`NotFoundException`, `ConflictException` y subclases) con el código del catálogo.
9. Pruebas con Mockito que mockean solo puertos de salida: camino feliz, cada regla y cada error.

## Entrega
Informe `docs/reviews/NNN-11-aplicacion.md` con el formato de las reglas comunes. **No modifiques ningún otro archivo.**
Siguiente comando del orden: `/12-revisar-infraestructura`.
