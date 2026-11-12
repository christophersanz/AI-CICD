# 20 · Corregir el Quality Gate de SonarQube (v01)

| | |
|---|---|
| **Comando** | `/20-corregir-sonar [regla java:Sxxxx | archivo | vacío = todos]` |
| **Archivo** | `docs/prompts/20-corregir-sonar-01.md` |
| **Versión anterior** | — |

> `{ARGUMENTO}` es lo que se escribe después del comando. Sin Claude Code, copia este prompt y reemplaza `{ARGUMENTO}` a mano.

## Rol
Actúa como desarrollador senior de Java 21 y especialista en calidad y seguridad de código (reglas de SonarQube, OWASP). Distingues un riesgo real de un falso positivo y lo demuestras.

## Contexto
- SonarQube local en <http://localhost:9000>, proyecto y Quality Gate `contract-api` (ADR-0015, `docker/jenkins/README.md`): 0 bugs, 0 vulnerabilidades, hotspots revisados al 100 %, rating A y cobertura ≥ 80 % sobre todo el código.
- La etapa *Análisis SonarQube* del Jenkins local corta el build si el gate falla.
- Fuentes de verdad: `CLAUDE.md`, `docs/architecture.md`, `docs/business-rules.md` y los ADR.
- Alcance: **{ARGUMENTO}** (vacío = todos los hallazgos abiertos que hacen fallar el gate).

## Objetivo
El Quality Gate `contract-api` en verde en un build del Jenkins local, con cada hallazgo **corregido** (con prueba si es un bug) o **justificado** en el código.

## Restricciones
- Las credenciales salen de `docker/jenkins/.env` (`SONAR_ADMIN_PASSWORD`): léelas en una variable de shell, nunca las imprimas ni las escribas en archivos versionados.
- **No marques hallazgos como "Safe", "Accepted" o "False positive" en el panel:** esa decisión vive solo en la base local y no se revisa. Las excepciones van en el código con `@SuppressWarnings("java:Sxxxx")` sobre el elemento más pequeño posible y un comentario que explique **por qué** no aplica.
- No desactives reglas, no cambies el Quality Gate ni excluyas archivos del análisis para que pase. Si una condición del gate parece inadecuada, propón el cambio en `docker/jenkins/scripts/sonar-analysis.sh` y su ADR, y espera mi decisión.
- Una justificación de seguridad (CSRF, enumeración, criptografía, inyección) siempre requiere mi aprobación explícita.

## Forma de trabajo
Crea o retoma `docs/sessions/AAAA-MM-DD-20-corregir-sonar-<alcance>.md` (docs/sessions/README.md).

### Paso 1 — Leer el estado (sin cambiar código)
1. Estado del gate: `GET /api/qualitygates/project_status?projectKey=contract-api`.
2. Hallazgos abiertos: `GET /api/issues/search?componentKeys=contract-api&resolved=false&types=BUG,VULNERABILITY` (y `CODE_SMELL` si el rating bajó de A), y hotspots: `GET /api/hotspots/search?project=contract-api`.
3. Tabla: `# | Regla | Tipo/severidad | Archivo:línea | Mensaje`.

### Paso 2 — Análisis y propuesta
Para cada hallazgo:
- qué dice la regla y si el riesgo **existe en este código** (evidencia: flujo real, configuración, pruebas);
- decisión propuesta: **Corregir** (cómo, sin cambiar comportamiento ni contratos) o **Justificar** (texto exacto del comentario);
- si es un bug: la prueba de regresión que lo reproduce **antes** de corregirlo.
Espera mi aprobación, hallazgo por hallazgo si hay alguno de seguridad.

### Paso 3 — Aplicar y verificar
1. Escribe primero la prueba de regresión y confírmala en rojo con el código actual; después corrige.
2. `./mvnw clean verify` en verde.
3. Rama `fix/sonar-<tema>`, push y lanza el job `contract-api` del Jenkins local contra esa rama (cambia temporalmente `REPO_BRANCH` en `docker/jenkins/.env` y vuelve a `master` al terminar), o espera al merge.
4. Confirma en el log de la etapa *Análisis SonarQube* que todas las condiciones están en `OK`.

## Informe final
Tabla `Regla | Archivo | Corregido/Justificado | Prueba o justificación`, resultado del gate (valores de cada condición), PR abierto y, si propusiste cambiar el gate, el ADR pendiente.
