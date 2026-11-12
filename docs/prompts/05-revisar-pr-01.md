# 05 · Revisar un PR antes de integrarlo (v01)

| | |
|---|---|
| **Comando** | `/05-revisar-pr [número de PR | rama; vacío = rama actual]` |
| **Archivo** | `docs/prompts/05-revisar-pr-01.md` |
| **Versión anterior** | — |

> `{ARGUMENTO}` es lo que se escribe después del comando. Sin Claude Code, copia este prompt y reemplaza `{ARGUMENTO}` a mano.

## Rol
Actúa como revisor técnico senior de un equipo que trabaja con GitHub Flow. Buscas evidencia en el código; no confirmas lo que ya se cree ni inventas problemas.

## Reglas comunes
Aplica `docs/prompts/09-reglas-revision-01.md`: orden del ciclo, fuentes de verdad, severidades, reglas y formato del informe. Esta revisión no modifica código.

## Alcance
Solo el diff de la rama o PR indicado contra `master` (`git diff master...<rama>` o `gh pr diff <n>`) y su impacto directo. **Aquí `{ARGUMENTO}` es el número de PR o la rama**, no la ronda; si está vacío, revisa la rama actual. **Revisa solo este alcance**; lo que veas fuera de él va como INFO con una línea, para la revisión que le corresponde.

## Comprobaciones
1. Aplica las comprobaciones de los comandos `10` a `15` solo a los archivos del diff.
2. El cambio hace lo que dice el PR y nada más (sin cambios no relacionados).
3. Cada regla, endpoint o error nuevo tiene su prueba; ninguna prueba se debilitó o se eliminó sin motivo.
4. Documentación del mismo cambio: `business-rules.md`, `technical.md`, `docs/api/openapi.*`, ADR y guías, según lo que toque.
5. Checks del PR en verde (`gh pr checks`) y checklist de la plantilla de PR completa y veraz.
6. Riesgos de integración: migraciones, compatibilidad de la API, configuración o secretos nuevos.

## Entrega
Informe `docs/reviews/pr-<número-o-rama>.md` con el formato de las reglas comunes; añade una línea final `Veredicto: APROBAR | CAMBIOS REQUERIDOS` (CAMBIOS REQUERIDOS si hay algún ERROR). Este informe va en la misma rama del PR. **No modifiques ningún otro archivo.**
Siguiente paso: si el veredicto es CAMBIOS REQUERIDOS, corregir en la misma rama y volver a ejecutar `/05-revisar-pr`; si es APROBAR, el autor decide el *Squash and merge*.
