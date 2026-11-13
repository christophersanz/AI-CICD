# Prompts

Prompts genéricos para desarrollar con Claude Code una API Java con Clean Architecture y DDD (este archivo es la portada de la carpeta, no un prompt). Nacieron en `contract-api` y forman un **kit reutilizable** (§6).

- **Nueve prompts, un archivo cada uno: `docs/prompts/NN-nombre.md`.** `NN` es el **orden** en que se usan (01 a 09). No hay versiones paralelas ni copias: para mejorar un prompt se edita **ese mismo archivo**, en un commit propio que explique el cambio. La historia queda en git (`git log -p docs/prompts/NN-nombre.md`).
- **Cada prompt tiene un comando de Claude Code** con el mismo nombre (`/02-implementar-caso-uso`) en `.claude/commands/`. El comando tiene tres líneas y solo carga ese archivo, como el `06.0.-fix.md` de los modelos del curso ("Lee y ejecuta las instrucciones de @prompts/…"). Sin Claude Code, se copia el prompt y se reemplaza `{ARGUMENTO}` a mano.
- **Los datos del proyecto no están en los prompts.** Cuando un prompt dice `DP.<clave>` (por ejemplo, `DP.build` o `DP.rama-principal`), el valor sale de `CLAUDE.md` → *Datos para los prompts*. Es lo único que cambia de un proyecto a otro.

## 1. Los 9 prompts, en orden

| # | Archivo | Comando | Para qué | Modifica código |
|---|---|---|---|:-:|
| 01 | [`01-definir.md`](01-definir.md) | `/01-definir requisitos <idea>` · `/01-definir reglas <tema>` | Convertir una idea en MVPs e historias, o cerrar reglas de negocio preguntando | No (solo documentos) |
| 02 | [`02-implementar-caso-uso.md`](02-implementar-caso-uso.md) | `/02-implementar-caso-uso <NombreUseCase>` | Implementar un caso de uso de `business-rules.md` §6, capa por capa | Sí |
| 03 | [`03-pruebas.md`](03-pruebas.md) | `/03-pruebas <clase o caso de uso>` | Completar pruebas con matriz regla → prueba y patrones de prueba | Solo pruebas |
| 04 | [`04-documentar.md`](04-documentar.md) | `/04-documentar <alcance>` | JavaDoc, OpenAPI, diagramas, ADR y guías alineados con el código | Solo documentación |
| 05 | [`05-refactorizar.md`](05-refactorizar.md) | `/05-refactorizar <objetivo>` | Reorganizar código sin cambiar el comportamiento | Sí |
| 06 | [`06-revisar.md`](06-revisar.md) | `/06-revisar <modo> [ronda]` | Revisar por capa, un PR o la entrega final (`READY` / `NOT READY`) | **No** |
| 07 | [`07-corregir.md`](07-corregir.md) | `/07-corregir ronda <NNN>` · `/07-corregir sonar` | Corregir lo que encontró una revisión o el Quality Gate | Sí |
| 08 | [`08-cambiar-pipeline.md`](08-cambiar-pipeline.md) | `/08-cambiar-pipeline <cambio>` | Cambiar el CI, el CI local, la imagen o el despliegue | Sí (pipeline) |
| 09 | [`09-mejorar-prompt.md`](09-mejorar-prompt.md) | `/09-mejorar-prompt <tarea o archivo>` | Crear un prompt o mejorar uno existente | Solo prompts |

### Orden de la revisión (dentro de `/06-revisar`)

`dominio` → `aplicacion` → `infraestructura` → `api` → `seguridad` → `cicd` → `arquitectura` → **`/07-corregir ronda NNN`** → `final`

Cada ronda tiene su carpeta `docs/reviews/AAAA-MM-DD-ronda-NNN/`, con un `README.md` que resume el estado de la ronda. Cada modo deja allí su informe con un nombre descriptivo (`01-revision-dominio.md` … `09-veredicto-final.md`) y dice cuál es el siguiente paso. `/06-revisar pr <n>` revisa un PR antes de integrarlo.

### Flujos típicos

- **Funcionalidad nueva:** `/01-definir requisitos` → `/01-definir reglas` (por cada duda) → `/02-implementar-caso-uso` → `/03-pruebas` → `/04-documentar` → `/06-revisar pr <n>` → merge.
- **Antes de una entrega:** el orden completo de `/06-revisar`, `/07-corregir ronda NNN` y `/06-revisar final`, hasta `READY`.
- **Después de un cambio acotado:** solo los modos de las capas tocadas, en orden, y luego `/07-corregir` → `/06-revisar final`.
- **Reorganizar código:** `/05-refactorizar` → `/06-revisar <capa tocada>`.
- **Build en rojo por el Quality Gate:** `/07-corregir sonar`.

## 2. Estructura común de todo prompt

Cada prompt tiene estas 10 partes. Si falta una, el prompt está incompleto. `/09-mejorar-prompt` las aplica.

1. **Rol:** experto concreto para la tarea ("actúa como arquitecto de software especializado en Java…", sesión 02).
2. **Contexto y fuentes de verdad:** qué existe hoy y qué documentos mandan (`CLAUDE.md`, `architecture.md`, `business-rules.md`, `technical.md`, ADR). Si el código contradice un documento, se reporta; no se elige en silencio.
3. **Validación del argumento:** qué hacer si `{ARGUMENTO}` falta o no coincide (listar opciones y detenerse).
4. **Objetivo verificable:** cómo se sabe que terminó (una prueba, un endpoint que responde, un Quality Gate en verde, `FINAL STATUS: READY`).
5. **Restricciones (qué NO hacer):** los datos del proyecto se citan como `DP.<clave>`, nunca escritos en el prompt; no inventar reglas de negocio ni nombres o ubicaciones (salen de los documentos o se preguntan); no agregar funcionalidades, patrones ni dependencias fuera del alcance; no integrar PRs ni aprobar despliegues; no tocar archivos fuera del alcance.
6. **Análisis de impacto y plan, sin cambiar código:** con código que ya funciona, **nunca se ejecuta en bloque** (sesión 03). Primero se identifica qué está bien y se mantiene, qué cambia, qué queda igual, qué es obsoleto, los supuestos, los archivos a crear, modificar o eliminar y los riesgos, incluida la sobreingeniería. **Nada se implementa hasta que el autor lo aprueba.**
7. **Pasos pequeños con aprobación:** un paso a la vez. Al terminar cada uno se muestran los archivos tocados, un resumen y el resultado de compilación y pruebas, y se espera "continuar".
8. **Verificación:** `DP.build` en verde y, si cambia código de producción o el pipeline, el CI local (`DP.ci-local`) con su Quality Gate. Ante un fallo, primero la causa raíz. Lo generado por IA no se da por bueno sin un comando ejecutado que lo demuestre.
9. **Informe final:** archivos creados, modificados, movidos y eliminados; pruebas (ejecutadas, exitosas, fallidas, con error, omitidas); decisiones; pendientes con su regla o pregunta abierta; documentación actualizada. **Nunca afirmar que funciona sin compilar y probar.**
10. **Archivo de sesión:** el progreso queda en `docs/sessions/` (ver su [README](../sessions/README.md)) para retomar sin perder contexto ni gastar tokens.

En las revisiones se añade: **separar revisar de corregir** (`/06-revisar` nunca modifica código; corrige `/07-corregir`), **no inventar problemas** y **no inflar la severidad**.

Buenas prácticas para todos:
- Prompts cortos y enfocados que remiten a los documentos, no prompts de "15 páginas" (sesión 01). Un tema por sesión: seguridad, negocio e infraestructura por separado (sesión 05).
- No quedarse con el primer resultado: revisar, pedir otra revisión y comparar. Cada iteración se registra en `docs/ai-log.md`.
- Si un cambio de código mueve la arquitectura o el contexto, se actualizan en el mismo cambio los documentos y los prompts afectados (sesión 03).
- Todo cambio sigue `DP.flujo-git`. El merge y la aprobación de despliegues son del autor.

## 3. De dónde sale cada práctica

| Práctica | Fuente |
|---|---|
| Contextualizar, pedir, validar e iterar; **qué hacer y qué no hacer**; mejorar en versiones sucesivas (aquí, commits sobre el mismo archivo); no prompts de 15 páginas | Docente, sesión 01 |
| Rol especializado al inicio del prompt; pedirle a la IA que cree el prompt; un prompt genérico más prompts por capa; contexto dividido en `CLAUDE.md` / `architecture.md` / `business-rules.md` / `prompts`; fuentes de verdad; informe de revisión por severidad en `docs/reviews/` | Docente, sesión 02 |
| Prompt parametrizado por caso de uso; con código funcionando, primero leer, analizar e evaluar el impacto; la IA no inventa nombres ni ubicaciones; actualizar los prompts si cambia la arquitectura; atender primero los warnings y errores | Docente, sesión 03 |
| Pruebas con contexto → prompt → revisión → ejecución → umbrales (JaCoCo 80 %, SonarQube); builders y datos generados | Docente, sesión 04 |
| Primero un plan que el usuario aprueba ("si el prompt no lo tiene, estás descalificado"); contexto modular; un tema por sesión; saber qué patrones de prueba usaste | Docente, sesión 05 |
| Revisión por capa (01–05), un único prompt de corrección (06), revisión final con `READY` / `NOT READY` (07); severidades ERROR / WARNING / OK; no confundir reglas de `domain` y `application`; diferidos deliberadamente | Modelos del curso, `F2-F4/01.-Modelos/use-case-ca-v1/prompts/` |
| Implementar una capa y validarla antes de pasar a la siguiente | Modelos del curso, `use-case-ca/prompts-sales-order-clean-architecture.md` |
| Qué está bien, qué cambia, qué no se toca, qué es obsoleto y supuestos; riesgo de sobreingeniería; tabla de movimientos; archivos creados, movidos y eliminados; causa raíz antes de corregir; "nunca afirmar que funciona sin compilar y probar" | `F1/api-demo-v1/prompts/` (enterprise-ready y package-by-feature) |
| Los scripts de CI/CD generados por IA no se asumen correctos; `actionlint` y `hadolint`; acciones verificadas y fijadas; imagen sin root ni JDK; fallo del pipeline frente a fallo del código; limpiar contenedores | `CURS-000617-CICD-v2/.../001-devops-validar-cicd.md` |
| Revisión integral con fuentes de verdad y trazabilidad de reglas; implementación de caso de uso paso a paso; refactor del dominio; seguridad 3A | Prompts del curso `001`–`005` (§5) |
| Contexto, objetivo, actividades, restricciones e informe final; **archivo de sesión** que la IA actualiza en cada paso | Presentación de Juan José, sesión 05 |
| Perfil de arquitecto y revisiones repetidas; la IA **pregunta** las reglas de negocio que faltan | Presentación de Ángel, sesión 05 |
| Requisitos refinados con IA y divididos en **MVPs**; Quality Gate estricto antes de desplegar | Presentación de Jairo, sesión 05 |
| GitHub Flow, verificación con Jenkins y SonarQube, correcciones de Sonar justificadas en el código; datos del proyecto separados en `DP.*` | `contract-api`, el proyecto donde nació el kit (ADR-0012 a ADR-0015) |

## 4. Plantilla rápida para tareas sin prompt

Para una tarea puntual que no justifica un prompt propio (si se va a repetir, usa `/09-mejorar-prompt`):

```markdown
## Rol
Actúa como [rol experto] en Java 21, Spring Boot 3, Clean Architecture y DDD.

## Contexto
- Lee: CLAUDE.md, docs/architecture.md, docs/business-rules.md [y los ADR o guías que apliquen].
- Situación actual: [qué existe hoy; confírmalo contra el código, no lo asumas].

## Objetivo
[Qué se quiere lograr]. Se considera terminado cuando [criterio verificable].

## Restricciones
- No inventes reglas de negocio, nombres ni ubicaciones: si faltan, pregúntame.
- No agregues funcionalidades, patrones ni dependencias fuera del alcance; no integres PRs ni apruebes despliegues.

## Forma de trabajo
1. Analiza el impacto (qué está bien, qué cambia, qué no se toca, supuestos, archivos, riesgos) sin cambiar código, y espera mi aprobación.
2. Un paso a la vez, deteniéndote al final de cada uno.

## Verificación e informe final
- `DP.build` en verde. Informe: archivos, pruebas, decisiones y pendientes. No afirmes que funciona sin compilar y probar.
```

## 5. Prompts del curso que dieron origen a estos

Los prompts que se usaron durante el curso no se conservan como copias: están en el historial de git, en el commit [`33f74fc`](https://github.com/christophersanz/AI-CICD/tree/33f74fc/docs/prompts) de `contract-api`, y los citan `docs/ai-log.md`, `docs/reviews/` y `docs/design/` con enlaces permanentes.

| Prompt del curso | Uso | Lo reemplaza |
|---|---|---|
| [`001-review-01.md`](https://github.com/christophersanz/AI-CICD/blob/33f74fc/docs/prompts/001-review-01.md) | Revisión integral (sesión 02); informe en [`ronda-001/07-revision-integral.md`](https://github.com/christophersanz/AI-CICD/blob/master/docs/reviews/2026-09-26-ronda-001/07-revision-integral.md) de `contract-api` | [`06-revisar.md`](06-revisar.md) (todos sus modos) |
| [`002.-refactoring-v1.md`](https://github.com/christophersanz/AI-CICD/blob/33f74fc/docs/prompts/002.-refactoring-v1.md) | Refactor del dominio por agregado (sesión 03) | [`05-refactorizar.md`](05-refactorizar.md) |
| [`003-security-01.md`](https://github.com/christophersanz/AI-CICD/blob/33f74fc/docs/prompts/003-security-01.md) | Diseño de la seguridad 3A (sesión 03) | `DP.seguridad` y [`06-revisar.md`](06-revisar.md) modo `seguridad` |
| [`004-implement-usecase.md`](https://github.com/christophersanz/AI-CICD/blob/33f74fc/docs/prompts/004-implement-usecase.md) | Caso de uso paso a paso (sesión 03) | [`02-implementar-caso-uso.md`](02-implementar-caso-uso.md) |
| [`005-security-02.md`](https://github.com/christophersanz/AI-CICD/blob/33f74fc/docs/prompts/005-security-02.md) | Seguridad fase 1, pasos 1.6–1.8 (sesión 04) | — (seguridad implementada) |
| [`006-cicd.md`](https://github.com/christophersanz/AI-CICD/blob/33f74fc/docs/prompts/006-cicd.md) | Puesta en marcha del CI/CD con parche (sesión 04) | [`08-cambiar-pipeline.md`](08-cambiar-pipeline.md) |

## 6. Usar el kit en otro proyecto

**Requisitos del proyecto destino:** API Java con Spring Boot, Clean Architecture y DDD, repositorio git y Claude Code.
- **Capas:** en módulos (`domain`, `application`, `infrastructure`) o como paquetes de un solo módulo, según `DP.modulos`.
- **Documentos:** donde estén (`DP.doc-arquitectura`, `DP.doc-reglas`, `DP.doc-tecnico`).
- **Estructura de los documentos:** la de las plantillas del kit, o una propia descrita en `DP.secciones`, que indica en qué sección está cada concepto que citan los prompts (reglas, estados, catálogo de errores…) y el formato de los ID de las reglas.
- **Herramientas:** Maven o Gradle, base de datos, CI, SonarQube y despliegue se declaran en `DP.*`; si alguna no existe, se marca «No aplica».

**No sirve** para un proyecto Spring Boot por capas técnicas (`controller` / `service` / `repository`) sin dominio separado: los prompts revisan e implementan contra Clean Architecture, así que todo sería un incumplimiento.

**Probado** en el modelo del curso `F4/01.-Modelos/use-case-ca-v1` (Sales Order). Es un solo módulo, con sus documentos en la raíz y reglas `RN-NNN`. El instalador detectó sus documentos, y `verificar.sh` terminó con 0 errores después de completar `DP` (`ai-log.md` #19). Después, `/06-revisar dominio` se ejecutó completo en ese proyecto: encontró sus reglas `RN-NNN` con el mapa de `DP.secciones`, no reportó como defectos lo que el proyecto no exige (eventos, códigos de error) y no modificó código (`ai-log.md` #21).

| Archivo del kit (`docs/prompts/kit/`) | Para qué |
|---|---|
| `instalar.sh` | Copia los prompts, los comandos, el kit y `docs/sessions/README.md`. Busca `architecture.md`, `business-rules.md` y `technical.md` en todo el proyecto y deja sus rutas en `DP.doc-arquitectura`, `DP.doc-reglas` y `DP.doc-tecnico`; solo crea desde la plantilla los que no existan. **Nunca sobrescribe** `CLAUDE.md` ni los documentos. |
| `verificar.sh` | Comprueba que el proyecto tiene todo lo que usan los prompts: que los documentos de `DP.doc-arquitectura`, `DP.doc-reglas` y `DP.doc-tecnico` existan y no sean la plantilla sin completar; las claves `DP.*` y cuáles faltan completar; las secciones numeradas, si `DP.secciones` es «Estándar del kit»; que exista el wrapper de `DP.build`; los comandos y un solo archivo por prompt. También detecta referencias obsoletas: comandos `/NN-nombre` citados que no existen y archivos de sesión con otro número. Ejecútalo después de cambiar cualquier prompt. |
| `CLAUDE-datos-prompts.md` | Plantilla de la sección *Datos para los prompts*, con qué poner en cada clave. |
| `CLAUDE.plantilla.md`, `architecture.plantilla.md`, `business-rules.plantilla.md` | Documentos base con las secciones que citan los prompts (`architecture.md` §2–§9, `business-rules.md` §1–§9 y §4.1). |

Pasos:

**Qué hace falta para instalarlo:** el repositorio de origen completo, o cualquier proyecto que ya tenga el kit instalado. **No basta con copiar `docs/prompts/kit/`:** ahí están solo las herramientas (instalador, verificador y plantillas). Los 9 prompts (`docs/prompts/NN-*.md`), sus comandos (`.claude/commands/`) y `docs/sessions/README.md` están fuera de esa carpeta, y `instalar.sh` los copia desde el repositorio que lo contiene. Por ejemplo:

```bash
git clone https://github.com/christophersanz/AI-CICD.git /tmp/kit-prompts
/tmp/kit-prompts/docs/prompts/kit/instalar.sh /ruta/al/otro-proyecto
```

```bash
# 1. Desde la raíz de este repositorio (o de cualquier proyecto que ya tenga el kit)
docs/prompts/kit/instalar.sh /ruta/al/otro-proyecto

# 2. En el otro proyecto: completar CLAUDE.md → "Datos para los prompts" (cada [...])
#    (si los documentos no siguen las plantillas del kit, DP.secciones con el mapa de sus secciones)
#    y los documentos de arquitectura y negocio (o usar /01-definir requisitos y /01-definir reglas)

# 3. Comprobar
cd /ruta/al/otro-proyecto && docs/prompts/kit/verificar.sh

# 4. Abrir Claude Code en la raíz del proyecto y escribir / para ver los comandos
```

Para **actualizar** un proyecto con mejoras del kit: `instalar.sh /ruta/al/otro-proyecto --actualizar`. Reemplaza los prompts y comandos del kit, pero no toca los documentos del proyecto. Si un proyecto mejora un prompt, el cambio se lleva al kit (este repositorio) y desde ahí a los demás, para que no haya versiones distintas del mismo prompt.
