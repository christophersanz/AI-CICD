---
description: Ciclo de revisión 5/9: revisión transversal de seguridad de contract-api (sin modificar código)
argument-hint: [NNN de la ronda de revisión; vacío = siguiente]
---

# Rol
Actúa como especialista en seguridad de aplicaciones (OWASP ASVS y API Security Top 10) y Spring Security 6. Buscas evidencia en el código; no confirmas lo que ya se cree ni inventas problemas.

# Reglas comunes
@docs/prompts/review-rules.md

Fuentes de verdad: @CLAUDE.md, @docs/architecture.md, @docs/business-rules.md, `docs/technical.md` y `docs/adr/`.

# Alcance
Seguridad en todas las capas: `infrastructure/security`, `@PreAuthorize` de la API, reglas de identidad (BR-USR-*), configuración, dependencias y pipeline. Diseño de referencia: `docs/design/003-security-design.md` y ADR-0005. **Revisa solo este alcance**; lo que veas fuera de él va como INFO con una línea, para la revisión que le corresponde.
Ronda: `$ARGUMENTS` (vacía = el siguiente NNN libre en `docs/reviews/`, o el de la ronda en curso si ya existe un informe `10`–`16` sin su `18`).

# Comprobaciones
1. Autenticación: login sin enumeración de usuarios (siempre `AUTH-002`), política de contraseñas BR-USR-003, usuario deshabilitado (BR-USR-004).
2. Autorización: permiso correcto en cada endpoint (§4.1), alcance de lectura (BR-CTR-012), sin endpoints accidentalmente públicos (`anyRequest().denyAll()`), actuator protegido.
3. Tokens: RS256 con claves fuera del repositorio, expiración, `iss`/`aud`, refresh tokens opacos hasheados, de un solo uso y revocables (BR-USR-005, 006).
4. Entrada y salida: validación, tamaños máximos, errores sin información interna, cabeceras de seguridad, CORS restringido.
5. Secretos: ninguno en el código, en `application*.yml`, en el historial de git, en la imagen Docker ni en los logs del pipeline (enmascarados).
6. Dependencias: versiones con vulnerabilidades conocidas en el `pom.xml` y en las imágenes base.
7. Excepciones de SonarQube: cada `@SuppressWarnings("java:S…")` tiene una justificación válida y vigente.
8. Pruebas de seguridad: 401, 403 por permiso, tokens expirados, reutilizados o manipulados.

# Entrega
Informe `docs/reviews/NNN-14-seguridad.md` con el formato de las reglas comunes; añade una tabla `Riesgo OWASP | Estado | Evidencia`. **No modifiques ningún otro archivo.**
Siguiente comando del orden: `/15-revisar-cicd`.
