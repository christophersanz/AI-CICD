# Revisión 002 · 5 — Seguridad

- **Prompt:** `docs/prompts/06-revisar.md`, modo `seguridad` (paso 5 de 9 de la ronda 002)
- **Fecha:** 2026-11-12 · **Commit revisado:** `183d054` (`master`)
- **Alcance:** seguridad en todas las capas: `infrastructure/security`, `@PreAuthorize` de la API, reglas de identidad (BR-USR-*), configuración, historial de git y pipeline. Referencia: `DP.seguridad` (`docs/design/seguridad.md`, ADR-0005)

## 1. Resumen

La seguridad de la fase 1 está bien implementada:
- Login sin enumeración de usuarios.
- JWT RS256 con `iss`, `aud` y expiración validados.
- Refresh tokens opacos, hasheados, de un solo uso y revocables por familia.
- Permisos por endpoint.
- Cadena de filtros que niega todo lo no declarado.
- Sin secretos reales en el código ni en el historial de git.

No hay ERROR ni WARNING. Quedan tres INFO, uno de ellos un control que no se pudo evaluar: las dependencias con vulnerabilidades conocidas, porque no hay ningún escáner instalado. CORS, la auditoría y el bloqueo por intentos son pendientes documentados.

**Build:** el de la ronda (paso 1) sobre `183d054`: BUILD SUCCESS. **Quality Gate:** Jenkins #16, 0 vulnerabilidades.

## 2. Comprobaciones

| # | Comprobación | Severidad | Archivo/componente | Evidencia | Regla o criterio | Corrección propuesta |
|---|---|---|---|---|---|---|
| 1 | Autenticación: sin enumeración, política de contraseñas, usuario deshabilitado | OK | `CredentialsAuthenticatorAdapter`, `DbUserDetailsService`, `PasswordPolicy`, `AuthenticateUserService` | Usuario inexistente, contraseña errónea, formato inválido o usuario deshabilitado → siempre `AUTH-002`. `DaoAuthenticationProvider` mantiene `hideUserNotFoundExceptions` y la mitigación de tiempos. 12–64 caracteres y ≤ 72 bytes | BR-USR-003, BR-USR-004 | — |
| 2 | Autorización: permiso por endpoint, nada público por accidente, actuator protegido | OK | `SecurityFilterChainConfig` | Cadena pública solo para `PUBLIC_PATHS` (login, refresh, OpenAPI, `/actuator/health`); la cadena privada exige autenticación en `/api/v1/**`, `system:monitor` en `/actuator/**` y `anyRequest().denyAll()`; `@PreAuthorize` en cada endpoint (paso 4). `health` responde solo `{"status":"UP"}` | `business-rules.md` §4.1 | — |
| 3 | Tokens | OK | `JwtConfig`, `TokenIssuerAdapter`, `RefreshTokenService`, V4 | RS256 con claves por variable de entorno; se validan `iss`, `aud` y `exp`; refresh token opaco guardado como hash (`uk_refresh_tokens_token_hash`), de un solo uso, con rotación y revocación de la familia al reutilizarlo, al hacer logout o si el usuario está deshabilitado | BR-USR-006; ADR-0005 | — |
| 4 | Entrada y salida | OK | `api/dto/*`, `GlobalExceptionHandler` | Tamaños máximos con `@Size` en los 6 Request; errores sin stack trace ni mensaje interno en el 500; `Cache-Control: no-store` en las respuestas con tokens; cabeceras de seguridad por defecto de Spring Security (no se desactivan) | OWASP API8 | — |
| 5 | CORS | INFO | — | No hay configuración de CORS: el navegador bloquea las peticiones de otro origen, lo cual es seguro, pero una SPA no podría consumir la API. Está planificado en `docs/design/seguridad.md` paso 2.4 (fase 2), y `technical.md` §3.3 deja pendientes las fases 2–4 | Diseño 003, paso 2.4 | Ver I-03 |
| 6 | Secretos fuera del código, de la imagen y de los logs | OK | `application*.yml`, `.gitignore`, `.dockerignore`, historial de git | Claves y contraseñas por variable de entorno; `*.pem`, `*.key` y `.env` ignorados por git y por Docker; los scripts de CI generan secretos efímeros y enmascaran el token. Historial: 0 claves privadas reales (las 2 coincidencias son la descripción del formato PEM en la documentación) y 0 tokens de GitHub o Docker Hub | OWASP ASVS V6 | Ver I-04 |
| 7 | Dependencias con vulnerabilidades conocidas | No evaluado | `pom.xml` (Spring Boot 3.5.16), imagen base | No hay ningún escáner instalado (Trivy, OSV-Scanner u OWASP Dependency-Check). SonarQube no analiza CVE de dependencias | OWASP A06 | Ver I-05 |
| 8 | Excepciones del análisis estático justificadas | OK | `SecurityFilterChainConfig.stateless` (`java:S4502`), `DbUserDetailsService.findUser` (`java:S5804`) | Siguen siendo válidas: no hay autenticación por cookies (el refresh token viaja en el body, §9) y la API es stateless con Bearer; el `UsernameNotFoundException` no llega al cliente | ADR-0015 | — |
| 9 | Pruebas de seguridad | OK | `SecurityFilterChainTest`, `AuthenticationFlowIntegrationTest`, `*ControllerTest` | 401 sin token, 403 por permiso, token expirado (`AUTH-004`), `iss` y `aud` distintos, firma con otra clave RSA (token falsificado), refresh token reutilizado (revoca la familia) y usuario deshabilitado | ADR-0005 | — |

## 3. Riesgos OWASP API Security Top 10 (2023)

| Riesgo | Estado | Evidencia |
|---|---|---|
| API1 Broken Object Level Authorization | No aplica todavía | Las reglas de alcance por objeto (BR-CTR-012, `contract:read:own`) llegan con los contratos |
| API2 Broken Authentication | Mitigado | JWT RS256 con `iss`, `aud` y `exp`; refresh de un solo uso; sin enumeración |
| API3 Broken Object Property Level Authorization | Mitigado | DTO propios; no se exponen entidades ni el hash de la contraseña |
| API4 Unrestricted Resource Consumption | Parcial | Paginación y `@Size`; no hay límite de peticiones ni bloqueo por intentos (§9: «Mientras tanto, sin bloqueo») |
| API5 Broken Function Level Authorization | Mitigado | `@PreAuthorize` por permiso y `denyAll()` por defecto |
| API6 Unrestricted Access to Sensitive Business Flows | Parcial | Login sin límite de intentos (pendiente de §9) |
| API7 Server Side Request Forgery | No aplica | La API no llama a URL externas |
| API8 Security Misconfiguration | Mitigado | Swagger y OpenAPI desactivados en `prod`; `health` sin detalles; CORS sin configurar (I-03) |
| API9 Improper Inventory Management | Mitigado | Contrato OpenAPI versionado y coherente con el código (paso 4) |
| API10 Unsafe Consumption of APIs | No aplica | Sin integraciones externas |

## 4. Pruebas existentes y faltantes

- **Existentes:** las del punto 9 de la tabla anterior.
- **Faltantes:** CORS (preflight) cuando se implemente el paso 2.4 del diseño.

## 5. Hallazgos

### ERROR
Ninguno.

### WARNING
Ninguno.

### INFO

**I-03 · CORS sin configurar (pendiente documentado).** Hoy es lo seguro, porque niega otros orígenes, pero el diseño (`docs/design/seguridad.md` §2, paso 2.4) muestra `CorsFilter` en las cadenas como si ya existiera. Al implementarlo, usar orígenes por propiedad y perfil, fallar al arrancar si hay `*` con `allowCredentials=true` y exponer `Location`.

**I-04 · Contraseñas de prueba en el historial de git.** `Admin-Prueba-2026` (del antiguo `docs/up/commands.txt`, commit `88a5f6d`), `Admin-Local-2026` (`.env.example`) y `Rrhh-Demo-Local-2026` (entorno de Postman) son credenciales de prueba local. No son un riesgo mientras no se usen en un entorno real: si alguna se usa o se usó en Render o en otro entorno compartido, hay que cambiarla allí.

**I-05 · Dependencias sin escaneo de CVE (No evaluado).** No es posible verificar este control con las herramientas instaladas. Se recomienda agregar al pipeline un escaneo de la imagen y de las dependencias (Trivy, por ejemplo con `aquasecurity/trivy-action` sobre la imagen publicada) con `/08-cambiar-pipeline agregar escaneo de vulnerabilidades`.

## 6. Aspectos positivos

- Dos cadenas de filtros con `denyAll()` al final: lo que no se declara queda cerrado.
- El refresh token nunca se guarda en claro y su reutilización revoca toda la familia, con la revocación confirmada aunque la petición falle (`noRollbackFor`).
- La prueba del token falsificado firma con otra clave RSA generada en la propia prueba.
- Las excepciones del análisis estático están justificadas en el código, junto a la línea que afectan.

## 7. Conclusión y siguiente paso

La seguridad de la fase 1 cumple el diseño y las reglas obligatorias. Los puntos abiertos son pendientes documentados (CORS, auditoría, bloqueo por intentos) y un control sin herramienta (CVE).

**Siguiente paso del orden:** `/06-revisar cicd` (paso 6 de 9, ronda 002).
