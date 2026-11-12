# 18 · Ciclo de revisión 9/9 — Revisión final (v01)

| | |
|---|---|
| **Comando** | `/18-revision-final [NNN de la ronda; vacío = la última ronda con informe 17]` |
| **Archivo** | `docs/prompts/18-revision-final-01.md` |
| **Versión anterior** | — |

> `{ARGUMENTO}` es lo que se escribe después del comando. Sin Claude Code, copia este prompt y reemplaza `{ARGUMENTO}` a mano.

## Rol
Actúa como Arquitecto de Software Senior responsable de aprobar la entrega. Tu decisión se basa en evidencia verificada hoy, no en lo que dicen los informes anteriores.

## Reglas comunes
Aplica `docs/prompts/09-reglas-revision-01.md`: orden del ciclo, fuentes de verdad, severidades, reglas y formato del informe. Esta revisión no modifica código.

## Alcance
El estado **actual** del repositorio tras `/17-corregir-hallazgos`. Ronda: `{ARGUMENTO}` (vacía = la última ronda que tenga informe `17`). Lee todos los informes `NNN-1*` de la ronda.

## Comprobaciones
1. **Build y pruebas:** `./mvnw clean verify`, con pruebas por módulo (ejecutadas, exitosas, fallidas, con error, omitidas) y cobertura. Si el build o las pruebas fallan, el estado es NOT READY.
2. **Correcciones reportadas:** no asumas que un hallazgo marcado como corregido en el informe `17` lo está. Verifícalo contra el código y las pruebas actuales:

   | Hallazgo (informe de origen) | Corrección reportada | Verificación actual | PASS / FAIL |
   |---|---|---|---|

3. **Revisión por capa** resumida (comprobaciones de `10` a `15`) para detectar regresiones introducidas por las correcciones.
4. **Regla de dependencia** con las dependencias reales (imports y `pom.xml`).
5. **Trazabilidad completa** de `business-rules.md` §4: `Regla | Implementación | Prueba | PASS / FAIL / NOT COVERED`.
6. **Quality Gate** de SonarQube del Jenkins local, si está disponible (`docker/jenkins/README.md`).
7. **Diferidos deliberadamente:** los hallazgos que el informe `17` marcó como diferidos y los pendientes de §9. No se reportan como defectos, salvo que contradigan una regla obligatoria; en ese caso, con la evidencia exacta.

## Entrega
Informe `docs/reviews/NNN-18-final.md` con esta estructura:

```text
# Revisión final — ronda NNN
## 1. Resumen ejecutivo
## 2. Build, pruebas y Quality Gate
## 3. Revisión por capa (domain, application, infrastructure, api)
## 4. Regla de dependencia
## 5. Trazabilidad de reglas de negocio
## 6. Verificación de las correcciones (informe 17)
## 7. Hallazgos restantes
### ERROR
### WARNING
### INFO
## 8. Diferidos deliberadamente
## 9. Decisión final
FINAL STATUS: READY | NOT READY
```

Termina con **exactamente una** de estas líneas, sin estados intermedios:

```text
FINAL STATUS: READY
FINAL STATUS: NOT READY
```

- **READY:** ningún ERROR, regla de dependencia cumplida, reglas obligatorias implementadas, comportamiento de la API correcto, compilación y pruebas en verde y Quality Gate en verde (si está disponible). Pueden quedar WARNING e INFO.
- **NOT READY:** queda al menos un ERROR. Indica qué comando lo resolvería (`/17-corregir-hallazgos`, `/01-reglas-negocio`, `/20-corregir-sonar`…).

**No modifiques ningún otro archivo.**
