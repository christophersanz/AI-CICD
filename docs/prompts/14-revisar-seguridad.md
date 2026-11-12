# 14 · Ciclo de revisión 5/9 — Seguridad

| | |
|---|---|
| **Comando** | `/14-revisar-seguridad [NNN de la ronda de revisión; vacío = siguiente]` |
| **Archivo** | `docs/prompts/14-revisar-seguridad.md` (única versión; los cambios se hacen aquí y quedan en el historial de git) |
| **Origen** | Mejora del prompt del curso [`003-security-01.md`](https://github.com/christophersanz/AI-CICD/blob/33f74fc/docs/prompts/003-security-01.md) |

> `{ARGUMENTO}` es lo que se escribe después del comando. `DP.<clave>` es un dato del proyecto: está en `CLAUDE.md` → *Datos para los prompts*. Sin Claude Code, copia este prompt y reemplaza `{ARGUMENTO}` a mano.

## Rol
Actúa como especialista en seguridad de aplicaciones (OWASP ASVS y API Security Top 10) y Spring Security 6. Buscas evidencia en el código; no confirmas lo que ya se cree ni inventas problemas.

## Reglas comunes
Aplica `docs/prompts/09-reglas-revision.md`: orden del ciclo, fuentes de verdad, severidades, reglas y formato del informe. Esta revisión no modifica código.

## Alcance
Seguridad en todas las capas: `infrastructure/security`, `@PreAuthorize` de la API, reglas de identidad de `business-rules.md`, configuración, dependencias y pipeline. Diseño de referencia: `DP.seguridad`. **Revisa solo este alcance**; lo que veas fuera de él va como INFO con una línea, para la revisión que le corresponde.
Ronda: `{ARGUMENTO}` (vacía = el siguiente NNN libre en `docs/reviews/`, o el de la ronda en curso si ya existe un informe `10`–`16` sin su `18`).

## Comprobaciones
1. Autenticación: login sin enumeración de usuarios (un único error para credenciales inválidas), política de contraseñas y usuarios deshabilitados según `business-rules.md`.
2. Autorización: permiso correcto en cada endpoint (§4.1), reglas de alcance de lectura de `business-rules.md`, sin endpoints accidentalmente públicos (`anyRequest().denyAll()`), actuator protegido.
3. Tokens: firma asimétrica con claves fuera del repositorio, expiración, `iss`/`aud`, refresh tokens opacos hasheados, de un solo uso y revocables, según `business-rules.md`.
4. Entrada y salida: validación, tamaños máximos, errores sin información interna, cabeceras de seguridad, CORS restringido.
5. Secretos: ninguno en el código, en `application*.yml`, en el historial de git, en la imagen Docker ni en los logs del pipeline (enmascarados).
6. Dependencias: versiones con vulnerabilidades conocidas en el `pom.xml` y en las imágenes base.
7. Excepciones del análisis estático (`DP.sonarqube`): cada `@SuppressWarnings("java:S…")` tiene una justificación válida y vigente.
8. Pruebas de seguridad: 401, 403 por permiso, tokens expirados, reutilizados o manipulados.

## Entrega
Informe `docs/reviews/NNN-14-seguridad.md` con el formato de las reglas comunes; añade una tabla `Riesgo OWASP | Estado | Evidencia`. **No modifiques ningún otro archivo.**
Siguiente comando del orden: `/15-revisar-cicd`.
