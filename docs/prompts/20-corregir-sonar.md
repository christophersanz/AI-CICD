# 20 · Corregir el Quality Gate de SonarQube

| | |
|---|---|
| **Comando** | `/20-corregir-sonar [regla java:Sxxxx | archivo | vacío = todos]` |
| **Archivo** | `docs/prompts/20-corregir-sonar.md` (única versión; los cambios se hacen aquí y quedan en el historial de git) |
| **Origen** | Nuevo en este proyecto (fuentes en `docs/prompts/README.md` §3) |

> `{ARGUMENTO}` es lo que se escribe después del comando. `DP.<clave>` es un dato del proyecto: está en `CLAUDE.md` → *Datos para los prompts*. Sin Claude Code, copia este prompt y reemplaza `{ARGUMENTO}` a mano.

## Rol
Actúa como desarrollador senior de Java y especialista en calidad y seguridad de código (reglas de SonarQube, OWASP). Distingues un riesgo real de un falso positivo y lo demuestras.

## Contexto
- SonarQube: `DP.sonarqube` (URL, proyecto, Quality Gate, credenciales y dónde están definidas sus condiciones). En adelante, `<proyecto>` es la clave del proyecto en SonarQube.
- Si hay CI local (`DP.ci-local`), su etapa de análisis corta el build cuando el gate falla.
- Fuentes de verdad: `CLAUDE.md`, `docs/architecture.md`, `docs/business-rules.md` y los ADR.
- Alcance: **{ARGUMENTO}** (vacío = todos los hallazgos abiertos que hacen fallar el gate).

## Objetivo
El Quality Gate del proyecto en verde en un build real (CI local o pipeline), con cada hallazgo **corregido** (con prueba si es un bug) o **justificado** en el código.

## Restricciones
- Las credenciales salen de donde indica `DP.sonarqube`: léelas en una variable de shell, nunca las imprimas ni las escribas en archivos versionados.
- **No marques hallazgos como "Safe", "Accepted" o "False positive" en el panel:** esa decisión vive solo en la base local y no se revisa. Las excepciones van en el código con `@SuppressWarnings("java:Sxxxx")` sobre el elemento más pequeño posible y un comentario que explique **por qué** no aplica.
- No desactives reglas, no cambies el Quality Gate ni excluyas archivos del análisis para que pase. Si una condición del gate parece inadecuada, propón el cambio donde se definen las condiciones (`DP.sonarqube`) y su ADR, y espera mi decisión.
- Una justificación de seguridad (CSRF, enumeración, criptografía, inyección) siempre requiere mi aprobación explícita.

## Forma de trabajo
Crea o retoma `docs/sessions/AAAA-MM-DD-20-corregir-sonar-<alcance>.md` (docs/sessions/README.md).

### Paso 1 — Leer el estado (sin cambiar código)
1. Estado del gate: `GET /api/qualitygates/project_status?projectKey=<proyecto>`.
2. Hallazgos abiertos: `GET /api/issues/search?componentKeys=<proyecto>&resolved=false&types=BUG,VULNERABILITY` (y `CODE_SMELL` si el rating bajó de A), y hotspots: `GET /api/hotspots/search?project=<proyecto>` (en versiones recientes el parámetro es `project`, no `projectKey`).
3. Tabla: `# | Regla | Tipo/severidad | Archivo:línea | Mensaje`.

### Paso 2 — Análisis y propuesta
Para cada hallazgo:
- qué dice la regla y si el riesgo **existe en este código** (evidencia: flujo real, configuración, pruebas);
- decisión propuesta: **Corregir** (cómo, sin cambiar comportamiento ni contratos) o **Justificar** (texto exacto del comentario);
- si es un bug: la prueba de regresión que lo reproduce **antes** de corregirlo.
Espera mi aprobación, hallazgo por hallazgo si hay alguno de seguridad.

### Paso 3 — Aplicar y verificar
1. Escribe primero la prueba de regresión y confírmala en rojo con el código actual; después corrige.
2. `DP.build` en verde.
3. Rama `fix/sonar-<tema>`, push y un build del CI local contra esa rama (cómo apuntarlo a otra rama: `DP.ci-local`; vuelve a `DP.rama-principal` al terminar), o espera al merge.
4. Confirma en el log del análisis que todas las condiciones del gate están en `OK`.

## Informe final
Tabla `Regla | Archivo | Corregido/Justificado | Prueba o justificación`, resultado del gate (valores de cada condición), PR abierto y, si propusiste cambiar el gate, el ADR pendiente.
