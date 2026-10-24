---
description: Completa la fase 1 de seguridad de contract-api (pasos 1.6–1.8): emisión de tokens, login y AuthController
---

Completa la **fase 1 de seguridad** de contract-api implementando los pasos **1.6, 1.7 y 1.8** de
@docs/design/003-security-design.md, siguiendo @CLAUDE.md, @docs/architecture.md,
@docs/business-rules.md y @docs/adr/0005-incorporar-seguridad.md.

## Contexto
- Los pasos 1.0–1.5 ya están hechos: dominio `identity`, casos de uso y puertos en `application`,
  cadenas de seguridad, validación de JWT (`RsaKeys`, `JwtDecoder` con `iss`/`aud`, converter `perms`),
  persistencia (`V2`–`V4` y adaptadores) y `crypto` (`PasswordHasherAdapter`, `RefreshTokenHasher`).
- Hoy la API **valida** tokens pero **no puede emitirlos**: `POST /api/v1/employees` devuelve
  `401 AUTH-003` y no hay forma de obtener un token.
- Objetivo verificable al terminar: arrancar con el perfil `local`, hacer
  `POST /api/v1/auth/login` con el administrador inicial y usar el `accessToken` recibido para
  registrar un empleado (`201`).

## Forma de trabajo
Ejecuta UN paso a la vez. Al terminar cada paso:
- muestra los archivos creados o modificados (ruta completa);
- resume en 3–5 líneas qué hiciste y qué decisiones tomaste;
- indica si compila y si las pruebas del paso pasan;
- **detente y espera mi "continuar"** antes del siguiente paso.
Si detectas un problema en un paso anterior (incluidos los pasos 1.0–1.5), dilo antes de seguir;
no lo corrijas en silencio.

## Pasos

### Paso 0 — Inventario y plan (sin código)
1. Lee el diseño (§1.1, §1.3, §1.4, §2, §4 y §5 fase 1) y la sección de reglas `BR-USR-*`,
   casos de uso y catálogo de errores `AUTH-*` de business-rules.md.
2. Inventario en una tabla: pieza del diseño → existe / falta / hay que ampliar, con su ruta.
   Revisa como mínimo:
   - implementaciones de `TokenIssuerPort` y `CredentialsAuthenticatorPort`;
   - registro como beans de `AuthenticateUserService`, `RefreshTokenService`, `LogoutService`,
     `GetCurrentUserService`, `CreateUserService` y `BootstrapAdministratorService` en `UseCaseConfig`;
   - bean `Clock` (los servicios lo reciben por constructor);
   - traducción de `InvalidCredentialsException`, `InvalidRefreshTokenException`,
     `UnauthenticatedException`, `DuplicateUsernameException`, `WeakPasswordException`,
     `RoleNotFoundException` y `UserNotFoundException` en `GlobalExceptionHandler`;
   - propiedades para el administrador inicial (`SECURITY_BOOTSTRAP_ADMIN_USERNAME` / `_PASSWORD`);
   - esquema `bearerAuth` en OpenAPI.
3. Para cada endpoint de la fase 1 (`login`, `refresh`, `logout`, `me`, `POST /users`): request,
   response, código de éxito y errores con su código HTTP. Marca cada dato como
   **[documentado]** o **[propuesto]**. Lo [propuesto] solo puede ser una decisión técnica
   (nombres de campos JSON, p. ej. `expiresIn` en segundos), nunca una regla de negocio.
4. Preguntas (máximo 5) si algo depende de una pregunta abierta (§7 del diseño) o no está definido.
Espera mi confirmación antes del Paso 1.

### Paso 1 — `jwt`: emisión de tokens (paso 1.6 del diseño)
- Bean `JwtEncoder` (`NimbusJwtEncoder`) en `JwtConfig`, construido con el **mismo** `RsaKeys` que
  el `JwtDecoder` (si no, el token emitido no valida, sobre todo con la clave efímera de `local`).
- `TokenIssuerAdapter implements TokenIssuerPort` en `security/jwt`:
  - RS256, cabecera `kid` = `security.jwt.key-id`;
  - claims: `sub` (id del usuario), `iss`, `aud` (lista), `iat`, `exp`, `jti` (UUID),
    `username`, `roles` y `perms` usando las constantes de `JwtClaims`;
  - vidas útiles desde `SecurityProperties` (`access-token-ttl`, `refresh-token-ttl`) y tiempo
    desde un `Clock` inyectado;
  - el refresh token se genera y hashea **reutilizando** `RefreshTokenHasher`; no dupliques la lógica.
- Pruebas unitarias: claims emitidos, expiración con `Clock` fijo, `kid`, y **ida y vuelta**:
  el token emitido lo acepta el `JwtDecoder` de la aplicación y el converter produce las
  authorities de `perms`. Un token con otro `aud` o `iss` es rechazado.

### Paso 2 — `authn`: verificación de credenciales (paso 1.7 del diseño)
- `DbUserDetailsService` (`UserDetailsService` + `UserDetailsPasswordService`) que usa
  `UserRepositoryPort`, **no** el `JpaRepository`.
- `AuthenticationManager` con `DaoAuthenticationProvider` (encoder: el `DelegatingPasswordEncoder`
  existente) y re-hash transparente cuando `upgradeEncoding()` es verdadero.
- `CredentialsAuthenticatorAdapter implements CredentialsAuthenticatorPort`: traduce
  `BadCredentialsException`, `DisabledException`, `LockedException` y usuario inexistente a
  `InvalidCredentialsException` (BR-USR-004: externamente siempre `AUTH-002`).
- Pruebas unitarias: credenciales válidas, contraseña incorrecta, usuario inexistente,
  usuario deshabilitado, y re-hash cuando cambia el strength o el prefijo del hash.

### Paso 3 — Wiring de aplicación
- Bean `Clock` (`Clock.systemUTC()`) en `infrastructure/config`.
- Registro en `UseCaseConfig` de los casos de uso de identidad que aún no son beans.
- Propiedades del administrador inicial en `SecurityProperties` (p. ej. `security.bootstrap.*`)
  leídas de variables de entorno, sin valores por defecto en `application.yml`.
  En `application-local.yml` puedes proponer valores de desarrollo, pero dímelo explícitamente
  y respeta BR-USR-003 (12–64 caracteres).
- `BootstrapAdministratorRunner` (`ApplicationRunner`) en `infrastructure/config`: llama a
  `BootstrapAdministratorUseCase` si ambas variables existen; si faltan, solo registra un aviso.
  Nunca registres la contraseña en los logs.
- Prueba: el contexto arranca y, con las variables definidas, se crea el administrador una sola vez
  (Testcontainers, reutiliza la configuración de las pruebas de persistencia existentes).

### Paso 4 — API (paso 1.8 del diseño)
- `AuthController` en `api/rest` con `POST /api/v1/auth/login`, `POST /api/v1/auth/refresh`,
  `POST /api/v1/auth/logout` y `GET /api/v1/auth/me`. `UserController` con `POST /api/v1/users`.
- Request/Response en `api/dto`, mappers en `api/rest`. Bean Validation solo de formato.
  Los records con secretos (`password`, `refreshToken`, `accessToken`) sobrescriben `toString()`
  como ya hacen los DTOs de `application`.
- `logout` y `me` obtienen el usuario del `Jwt` (`@AuthenticationPrincipal Jwt`, claim `sub`);
  esa es la única dependencia de `api` hacia Spring Security permitida en esta fase.
- Respuestas con `Cache-Control: no-store` en `login` y `refresh`.
- `GlobalExceptionHandler`: Problem Details con `type`, `title`, `status`, `detail` y `code` para
  los errores `AUTH-002`, `AUTH-005`, `AUTH-007`, `AUTH-008`, `AUTH-009` y `AUTH-404`, con el mismo
  formato que ya usan `ProblemDetailsAuthenticationEntryPoint` y los errores de empleados.
- OpenAPI: esquema `bearerAuth` (HTTP bearer, JWT) aplicado a los endpoints privados, para poder
  probar desde Swagger UI con el botón *Authorize*.
- Verifica que las rutas coinciden con `PUBLIC_PATHS` de `SecurityFilterChainConfig`; no amplíes
  las rutas públicas.
- `@WebMvcTest` por endpoint: éxito, 400 de validación y cada error de negocio. `me` y `logout`
  también sin token (401 `AUTH-003`).

### Paso 5 — Prueba de extremo a extremo
- Prueba de integración (`@SpringBootTest` + Testcontainers) del flujo completo:
  login → `POST /api/v1/employees` con el Bearer → refresh → reutilizar el refresh anterior
  (401 `AUTH-005` y familia revocada, BR-USR-006) → logout.
- Matriz de trazabilidad: regla `BR-USR-*` / error `AUTH-*` de esta fase → prueba(s) que la cubren
  (capa y nombre del test). Crea las que falten.
- Nombres `should<Resultado>When<Condición>`, Given / When / Then.

### Paso 6 — Cierre
- `./mvnw clean verify` en verde.
- Instrucciones para probar en local (PowerShell y Postman): variables de entorno a definir,
  cuerpo del login y cómo configurar el Bearer token (incluye un *pre-request script* de Postman
  que haga login y guarde `{{accessToken}}` en la colección, porque la clave efímera cambia en
  cada reinicio).
- Actualiza `docs/design/003-security-design.md`: estado de los pasos 1.6–1.8 y cualquier
  desviación del diseño con su motivo. Actualiza business-rules.md solo si aclaraste una regla
  o un error.
- Lista de TODOs para la fase 2 (`@PreAuthorize`, `Actor`, CORS), cada uno con su referencia.

## Restricciones
- No implementes la fase 2: sin `@EnableMethodSecurity` ni `@PreAuthorize`. Deja un TODO en
  `EmployeeController` y `UserController` referenciando BR-CTR-011 y el paso 2.1.
- No crees endpoints "de desarrollo" que emitan tokens sin credenciales.
- `domain` y `application` no pueden depender de Spring Security (ADR-0003: lo garantiza el `pom.xml`).
  Si un paso lo exige, detente y explica el conflicto.
- No uses `Instant.now()` ni `LocalDate.now()`: inyecta un `Clock`.
- Nunca registres en logs contraseñas, access tokens ni refresh tokens.
- No añadas dependencias al `pom.xml` sin indicarlo y justificarlo (Nimbus ya viene con
  `spring-boot-starter-oauth2-resource-server`).
- No modifiques migraciones existentes (`V1`–`V4`); si necesitas un cambio de esquema, crea `V5+`
  y avísame antes, porque `V5` está reservada para la auditoría (fase 3).
