Actúa como Arquitecto de Software Senior y especialista en seguridad de aplicaciones,
experto en Java 21, Spring Boot 3, Spring Security 6 y Clean Architecture.

Diseña e implementa la capa de seguridad de contract-api cubriendo las 3A:
Autenticación, Autorización y Auditoría. Respeta CLAUDE.md, docs/architecture.md
y docs/business-rules.md.

## Contexto
- La API es stateless y la consumen clientes web (SPA) y otros servicios.
- Roles de negocio ya definidos en business-rules.md: `ADMIN`, `HR`, `EMPLOYEE`.
- Reglas existentes: BR-CTR-011 (operaciones de escritura solo HR/ADMIN) y BR-CTR-012
  (un empleado solo consulta sus propios contratos), error `AUTH-001`.
- El stack declara "OAuth2 Resource Server (JWT)"; esta API emitirá sus propios tokens.

## Alcance funcional

### 1. Autenticación
- Login con usuario y contraseña contra base de datos (PostgreSQL), mediante un
  `UserDetailsService` y un `AuthenticationProvider` respaldados por el puerto de persistencia.
- **Access token JWT** de vida corta (configurable, p. ej. 15 min), firmado con clave asimétrica
  (RS256 o ES256) cargada desde variables de entorno o keystore; nunca en el repositorio.
  Claims: `sub`, `iss`, `aud`, `iat`, `exp`, `jti`, roles y authorities.
- **Refresh token** opaco de vida larga (configurable), persistido **hasheado** (nunca en claro),
  con:
  - rotación en cada uso (el anterior queda invalidado);
  - detección de reutilización: si se usa un refresh token ya rotado, revocar toda su familia;
  - revocación en logout y al deshabilitar o cambiar la contraseña del usuario.
- Endpoints: `POST /api/v1/auth/login`, `POST /api/v1/auth/refresh`, `POST /api/v1/auth/logout`,
  `GET /api/v1/auth/me`.

### 2. Usuarios y roles
- Creación de usuarios con sus roles (`POST /api/v1/users`), activación/desactivación,
  asignación y retiro de roles, cambio de contraseña.
- Política de contraseñas validada en el dominio (longitud mínima y máxima; considera el
  límite de 72 bytes de BCrypt).
- Usuario administrador inicial creado al arrancar desde variables de entorno (nunca con
  contraseña fija en una migración), solo si no existe.

### 3. Autorización
- Modelo **RBAC con authorities granulares**: un usuario tiene roles; cada rol agrupa
  permisos con formato `<recurso>:<acción>[:<alcance>]`. Propón el catálogo completo a partir
  de los casos de uso de business-rules.md, por ejemplo:
  `contract:create`, `contract:update`, `contract:sign`, `contract:renew`, `contract:terminate`,
  `contract:read:all`, `contract:read:own`, `employee:create`, `user:manage`.
- La relación rol → permisos se persiste en base de datos (no hardcodeada).
- Autorización a nivel de método con `@EnableMethodSecurity` y `@PreAuthorize` sobre
  authorities, no sobre nombres de rol.
- Las reglas de **propiedad del recurso** (BR-CTR-012) se validan en `application`, porque
  necesitan datos: define un puerto de entrada o un campo en los Commands/Queries con el
  usuario autenticado (actor), sin que `application` dependa de Spring Security.

### 4. Contextos públicos y privados
- Dos `SecurityFilterChain` con `securityMatcher` y `@Order`:
  - **Pública:** login, refresh, OpenAPI/Swagger y `actuator/health`.
  - **Privada:** el resto de `/api/v1/**`, autenticación JWT obligatoria.
- Todo lo no declarado se deniega por defecto.
- Sesión `STATELESS`; justifica la configuración de CSRF para una API stateless con tokens
  en cabecera.

### 5. Filtros personalizados
- `JwtAuthenticationFilter` (`OncePerRequestFilter`): extrae el Bearer token, lo valida
  (firma, expiración, emisor, audiencia) y construye el `Authentication`.
- Evalúa y justifica frente a usar `oauth2ResourceServer().jwt()` con un `JwtDecoder` propio
  y un `JwtAuthenticationConverter` para mapear authorities. Si se elige el filtro propio,
  explica qué se gana y qué se pierde; documenta la decisión en un ADR.
- Filtro de correlación que asegure el `traceId` y el usuario en el MDC para los logs.
- `AuthenticationEntryPoint` y `AccessDeniedHandler` propios que respondan en
  **Problem Details (RFC 9457)** con código de negocio, ya que los errores de los filtros
  no pasan por el `@RestControllerAdvice`.

### 6. CORS
- `CorsConfigurationSource` configurado por propiedades y perfil (`local`, `dev`, `prod`):
  orígenes, métodos, cabeceras permitidas y expuestas, `maxAge`.
- Prohibido `*` en orígenes cuando se permiten credenciales.

### 7. Password encoder personalizado
- `DelegatingPasswordEncoder` propio con **BCrypt como algoritmo predeterminado** y soporte
  para Argon2id y PBKDF2, identificados por prefijo (`{bcrypt}`, `{argon2}`, ...).
- Algoritmo por defecto y parámetros (strength de BCrypt, etc.) configurables por propiedades.
- Re-hash transparente en el login cuando cambie el algoritmo o sus parámetros
  (`upgradeEncoding` + `UserDetailsPasswordService`).
- Para `application`, exponerlo mediante un puerto (p. ej. `PasswordHasherPort`), no como
  `PasswordEncoder` de Spring.

### 8. Auditoría
- **Eventos de seguridad persistidos:** login exitoso y fallido, refresh, reutilización de
  refresh token, logout, acceso denegado, creación de usuario y cambios de roles.
  Escuchar los eventos de Spring Security (`AuthenticationSuccessEvent`,
  `AbstractAuthenticationFailureEvent`, `AuthorizationDeniedEvent`) más los propios.
  Datos: tipo de evento, usuario, fecha/hora, IP, user-agent, resultado, `traceId`.
- **Auditoría de datos:** `createdBy`, `createdAt`, `updatedBy`, `updatedAt` en las entidades
  JPA mediante Spring Data JPA Auditing y un `AuditorAware` que lea el usuario autenticado.
- Nunca registrar contraseñas, tokens ni datos personales innecesarios.

## Ubicación en la arquitectura
- `domain/identity` (o el nombre que propongas): agregados `User`, value objects `Username`,
  `Role`, `Permission`, `PasswordPolicy`; Java puro.
- `application`: casos de uso (`CreateUserUseCase`, `AuthenticateUserUseCase`,
  `RefreshTokenUseCase`, `LogoutUseCase`, ...) y puertos de salida (`UserRepositoryPort`,
  `RefreshTokenRepositoryPort`, `PasswordHasherPort`, `TokenIssuerPort`, `SecurityAuditPort`).
- `infrastructure/security/`: filtros, JWT, encoder, CORS, handlers y cadenas de filtros,
  con subpaquetes (`filter`, `jwt`, `crypto`, `config`, `handler`, `audit`).
- Persistencia de usuarios, roles, permisos, refresh tokens y auditoría en
  `infrastructure/persistence`, con migraciones Flyway (incluye la carga de roles y permisos).
- Actualiza la sección 2 de docs/architecture.md con el nuevo paquete `security` y añade
  reglas ArchUnit: `domain` y `application` no importan `org.springframework.security.*`;
  `api` no usa `security` salvo anotaciones de autorización.

## Errores
Amplía el catálogo de business-rules.md con códigos `AUTH-NNN` (credenciales inválidas,
token expirado, token inválido, refresh token revocado o reutilizado, usuario deshabilitado,
usuario duplicado) y su código HTTP (401, 403, 409, 422).

## Restricciones
- No inventes reglas de negocio. Antes de implementar, pregúntame (máximo 5 preguntas) sobre
  lo que no esté definido, en especial:
  - relación entre `User` y `Employee` (necesaria para BR-CTR-012);
  - quién puede crear usuarios y asignar cada rol;
  - bloqueo de cuenta por intentos fallidos (umbral y duración);
  - tiempos de vida de los tokens y política de contraseñas;
  - plazo de retención de los registros de auditoría.
- Lo que quede sin respuesta va a "Preguntas abiertas" de business-rules.md.
- Indica y justifica cada dependencia nueva en el `pom.xml` (prefiere Nimbus, ya incluido en
  `spring-security-oauth2-jose`, antes que una librería JWT adicional).
- Sin sobreingeniería: no introduzcas un servidor de autorización externo (Keycloak,
  Spring Authorization Server) ni OAuth2 completo salvo que lo justifiques.

## Entregables
No escribas código todavía. Primero entrega:
1. Diseño general con diagrama Mermaid de la cadena de filtros y del flujo login → acceso →
   refresh → logout.
2. Modelo de datos (tablas y relaciones) y catálogo de roles y permisos.
3. Matriz endpoint × permiso para los casos de uso actuales.
4. Plan de implementación en pasos pequeños, cada uno compilable y con pruebas verdes.
5. Riesgos y trade-offs (filtro propio vs resource server, rotación de refresh tokens,
   revocación de access tokens).

Tras mi aprobación, implementa paso a paso con pruebas:
- Unitarias de dominio y casos de uso (JUnit 5, Mockito, AssertJ).
- Unitarias del servicio de tokens y del password encoder.
- Integración de persistencia con Testcontainers.
- `@WebMvcTest` con `spring-security-test` para 401, 403, acceso por authority, rutas públicas
  y CORS (preflight).
- Reglas ArchUnit nuevas.
Al final: ADRs (estrategia de tokens, filtro propio vs resource server, modelo RBAC) y
actualización de architecture.md y business-rules.md.