# 16 · Ciclo de revisión 7/9 — Arquitectura integral (v02)

| | |
|---|---|
| **Comando** | `/16-revisar-arquitectura [NNN de la ronda de revisión; vacío = siguiente]` |
| **Archivo** | `docs/prompts/16-revisar-arquitectura-02.md` |
| **Versión anterior** | `docs/prompts/historico/16-revisar-arquitectura-01.md` |

> `{ARGUMENTO}` es lo que se escribe después del comando. Sin Claude Code, copia este prompt y reemplaza `{ARGUMENTO}` a mano.

## Rol
Actúa como Arquitecto de Software Senior experto en Clean Architecture, arquitectura hexagonal y DDD. Buscas evidencia en el código; no confirmas lo que ya se cree ni inventas problemas.

## Reglas comunes
Aplica `docs/prompts/09-reglas-revision-01.md`: orden del ciclo, fuentes de verdad, severidades, reglas y formato del informe. Esta revisión no modifica código.

## Alcance
Todo el repositorio, apoyándote en los informes `10` a `15` de la misma ronda (si alguno falta, indícalo y recomienda ejecutarlo antes). **Revisa solo este alcance**; lo que veas fuera de él va como INFO con una línea, para la revisión que le corresponde.
Ronda: `{ARGUMENTO}` (vacía = el siguiente NNN libre en `docs/reviews/`, o el de la ronda en curso si ya existe un informe `10`–`16` sin su `18`).

## Comprobaciones
1. Regla de dependencia `infrastructure → application → domain` con las dependencias **reales** (imports y `pom.xml` de cada módulo), incluida la regla 9 de `architecture.md` §3 (aislamiento dentro de `infrastructure`).
2. Puertos y adaptadores: entrada (controller → `UseCase`) y salida (`Port` ← adaptador) correctamente orientados.
3. Separación de modelos: dominio, `Command`/`Query`/`Result`, Request/Response y entidades JPA.
4. Nombres y paquetes según `architecture.md` §2 y §5.
5. Cada regla `BR-*` implementada en la capa que indica `business-rules.md` §4: trazabilidad completa del proyecto.
6. Sin lógica de negocio en controllers, mappers, repositorios ni adaptadores.
7. Sin patrones ni módulos fuera del alcance (CQRS, event sourcing, mediator, specification, microservicios) y sin sobreingeniería.
8. Coherencia entre código, `architecture.md`, `business-rules.md`, `technical.md` y las guías (diagramas incluidos).
9. Hallazgos repetidos entre capas que apuntan a una causa común.

## Entrega
Informe `docs/reviews/NNN-16-arquitectura.md` con el formato de las reglas comunes; añade las secciones **Regla de dependencia** (dependencias reales encontradas), **Patrones y alcance** y una **matriz consolidada** de los hallazgos de la ronda (`Severidad | Capa | Componente | Hallazgo | Informe de origen | Acción`) sin duplicados. **No modifiques ningún otro archivo.**
Siguiente comando del orden: `/17-corregir-hallazgos docs/reviews/NNN-*.md`.
