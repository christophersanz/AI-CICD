# Reglas comunes de revisión

Las incluyen todos los comandos de revisión de `.claude/commands/` (`05-revisar-pr`, `10` a `16` y `18-revision-final`). Cambiarlas aquí cambia todas las revisiones.

> **Una revisión no modifica código, pruebas, configuración ni documentación.** El único archivo que crea es su informe. Las correcciones se hacen con `/17-corregir-hallazgos`.

## Orden del ciclo de revisión

| # | Comando | Revisa | Informe |
|---|---|---|---|
| 1 | `/10-revisar-dominio` | `domain` | `docs/reviews/NNN-10-dominio.md` |
| 2 | `/11-revisar-aplicacion` | `application` | `NNN-11-aplicacion.md` |
| 3 | `/12-revisar-infraestructura` | `infrastructure` (persistencia, seguridad, config) | `NNN-12-infraestructura.md` |
| 4 | `/13-revisar-api` | `infrastructure/api` | `NNN-13-api.md` |
| 5 | `/14-revisar-seguridad` | Seguridad transversal | `NNN-14-seguridad.md` |
| 6 | `/15-revisar-cicd` | Pipeline, Docker, Jenkins, despliegues | `NNN-15-cicd.md` |
| 7 | `/16-revisar-arquitectura` | Integral, con los informes 10–15 | `NNN-16-arquitectura.md` |
| 8 | `/17-corregir-hallazgos` | Corrige los ERROR y WARNING confirmados (único que modifica código) | `NNN-17-correcciones.md` |
| 9 | `/18-revision-final` | Verifica las correcciones y decide `READY` / `NOT READY` | `NNN-18-final.md` |

`NNN` es el número de la **ronda** de revisión (001, 002…): todos los informes de una ronda comparten el mismo número. Se puede ejecutar solo una parte del ciclo (por ejemplo, 10 → 17 → 18 tras cambiar el dominio), pero siempre en este orden: de adentro hacia afuera (dominio → aplicación → infraestructura → API) y de lo particular a lo integral. Revisar por capa enfoca la revisión y consume menos contexto que revisar todo de una vez.

## Fuentes de verdad

Leer completas, en este orden:
1. `CLAUDE.md`: stack, flujo de Git y forma de trabajar.
2. `docs/architecture.md`: capas, reglas de arquitectura (§3), nombres, API, errores y pruebas.
3. `docs/business-rules.md`: reglas `BR-*` con la **capa** que las valida, estados, casos de uso, errores y preguntas abiertas (§9).
4. `docs/technical.md` y los ADR de `docs/adr/`: decisiones vigentes.

Los informes anteriores de `docs/reviews/` dan contexto y trazabilidad, pero **no mandan sobre los documentos**. Si el código contradice un documento, se reporta e indica cuál debería corregirse.

## Severidades

Se usan solo estas, **sin inflarlas**:

| Severidad | Cuándo |
|---|---|
| **ERROR** | Viola una regla de negocio, la regla de dependencia, una regla obligatoria de `architecture.md` §3, un comportamiento obligatorio de la API o la corrección del build y de las pruebas |
| **WARNING** | Debilidad relevante de arquitectura, implementación, mantenibilidad o pruebas que hoy no incumple una regla obligatoria |
| **INFO** | Mejora opcional u observación |
| **OK** | Comprobación verificada (solo en la tabla de comprobaciones) |

## Reglas

- Solo hallazgos con evidencia verificable (`archivo:línea`, clase o método). **No inventar problemas si el código cumple.** Antes de reportar un ERROR, volver a verificar la evidencia.
- Respetar la capa de cada regla: **no marcar como error de `domain` una regla que `business-rules.md` asigna a `application`**, ni al revés.
- No son hallazgos: lo que un ADR descartó (por ejemplo, ArchUnit, ADR-0003); lo pendiente por una pregunta abierta de §9 que el código resuelve de forma neutral con su TODO; los diferidos deliberadamente en un informe `17` anterior; una preferencia de estilo sin regla que la respalde. Si alguno contradice una regla obligatoria, sí se reporta con la evidencia exacta.
- Lo que no se pueda evaluar va como **No evaluado**, con el motivo.

## Antes de revisar

Ejecutar `./mvnw clean verify` y anotar compilación y pruebas (ejecutadas, fallidas, con error y omitidas, por módulo) y la cobertura. Si el Jenkins local está activo, anotar el último Quality Gate de SonarQube (`docker/jenkins/README.md`).

## Formato del informe

1. **Resumen** (3–5 líneas) y resultado del build.
2. **Comprobaciones:** `# | Comprobación | Severidad | Archivo/componente | Evidencia | Regla o criterio | Corrección propuesta`.
3. **Trazabilidad de reglas** del alcance: `Regla | Implementación (clase/método) | Prueba | PASS / FAIL / NOT COVERED`. Una regla solo es PASS si su implementación está verificada; si está implementada sin una prueba adecuada, se indica.
4. **Pruebas existentes y faltantes** del alcance.
5. **Hallazgos** agrupados en ERROR, WARNING e INFO. Cada uno con archivo, clase o método, regla afectada, evidencia y explicación; los ERROR, además, con un fragmento antes/después.
6. **Aspectos positivos** que conviene mantener.
7. **Conclusión técnica** y siguiente comando del orden.
