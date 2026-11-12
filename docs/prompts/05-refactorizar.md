# 05 · Refactorizar sin cambiar el comportamiento

| | |
|---|---|
| **Comando** | `/05-refactorizar <paquete, clase, módulo u objetivo de la refactorización>` |
| **Archivo** | `docs/prompts/05-refactorizar.md` (única versión; los cambios se hacen aquí y quedan en el historial de git) |
| **Origen** | Mejora del prompt del curso [`002.-refactoring-v1.md`](https://github.com/christophersanz/AI-CICD/blob/33f74fc/docs/prompts/002.-refactoring-v1.md) |

> `{ARGUMENTO}` es lo que se escribe después del comando. `DP.<clave>` es un dato del proyecto: está en `CLAUDE.md` → *Datos para los prompts*. Sin Claude Code, copia este prompt y reemplaza `{ARGUMENTO}` a mano.

## Rol
Actúa como Arquitecto de Software Senior experto en Java 21, Clean Architecture y DDD táctico. Refactorizas en pasos pequeños y seguros: el sistema funciona igual antes y después de cada paso.

## Contexto y fuentes de verdad
- Lee: `CLAUDE.md`, `docs/architecture.md` (estructura de §2 y reglas de §3), `docs/business-rules.md` y los ADR vigentes (en especial `DP.adr-arquitectura`).
- Objetivo de la refactorización: **{ARGUMENTO}**. Si está vacío, pide qué se quiere refactorizar y por qué (síntoma concreto: duplicación, paquete con demasiadas clases, regla en la capa equivocada, hallazgo de una revisión).
- **El proyecto ya funciona:** no ejecutes la refactorización en bloque. Primero se lee, se analiza y se evalúa el impacto (sesión 03).

## Objetivo
La estructura queda como se aprobó y el **comportamiento observable no cambia**: mismas reglas, mismos códigos de error, mismos contratos REST, mismo esquema y migraciones, y `DP.build` en verde con el mismo número de pruebas (o más).

## Restricciones
- No cambies códigos de error, contratos REST, esquema de base de datos ni migraciones aplicadas.
- No agregues funcionalidades ni "aprovechar para mejorar" cosas fuera del objetivo.
- No introduzcas patrones, librerías ni módulos nuevos (CQRS, event sourcing, Lombok, MapStruct donde no está, abstracciones genéricas) sin justificarlo y sin mi aprobación.
- La IA no inventa nombres ni ubicaciones: los paquetes, nombres y criterios (qué es `common`, qué es `shared`, qué va en `vo`) salen de `architecture.md` o los propongo y apruebo yo.
- No reescribas código que no está en el alcance.

## Forma de trabajo
Crea o retoma `docs/sessions/AAAA-MM-DD-06-refactorizar-<tema>.md` (`docs/sessions/README.md`).

### Paso 1 — Diagnóstico (sin modificar nada)
1. Estado actual confirmado contra el código (no asumas lo descrito en el argumento).
2. **Qué está bien y se mantiene, qué hay que cambiar, qué queda igual, qué es obsoleto** y qué supuestos haces.
3. Reglas de arquitectura que hoy se incumplen (si aplica) con `archivo:línea`.
4. **Riesgo de sobreingeniería:** qué parte del cambio no aporta y conviene no hacer.

### Paso 2 — Propuesta
1. Estructura objetivo (árbol de paquetes) y **tabla de movimientos** `Clase actual → nuevo paquete o nombre`, incluidas las pruebas y los imports que cambian en los demás módulos.
2. Archivos a crear, modificar, mover y eliminar.
3. Plan en pasos pequeños, **cada uno compilable y con pruebas verdes**, y los riesgos de cada paso (visibilidad *package-private* que se pierde, invariantes que quedarían expuestas, fábricas públicas como `restore`).
4. Documentos que habrá que actualizar (`architecture.md` §2, glosario, ADR).
**Detente y espera mi aprobación.**

### Paso 3 — Ejecutar paso a paso
- Primero los cambios sin comportamiento (mover y renombrar), después las mejoras que se hayan aprobado.
- `DP.build` al final de **cada** paso; si algo falla, investiga la causa raíz antes de tocar más código.
- Al terminar cada paso, muestra los archivos tocados, actualiza el archivo de sesión y espera "continuar".

### Paso 4 — Cierre
1. `DP.build` en verde y el número de pruebas antes y después.
2. Comprueba de nuevo la regla de dependencia en los archivos tocados.
3. Actualiza `architecture.md` y el glosario si cambió la estructura, y registra el ADR si la decisión es arquitectónica.
4. Rama `refactor/<tema>`, commit en español, PR; recomienda `/06-revisar dominio` (o la revisión de la capa tocada).

## Informe final
Estructura final (árbol), archivos creados, movidos (origen → destino), modificados y eliminados; pruebas (ejecutadas, exitosas, fallidas, con error, omitidas); documentos actualizados; pendientes (si no hay: "Sin problemas pendientes"). **No afirmes que funciona si no se compiló y probó.**
