# ADR-0005: Incorporar seguridad (autenticación, autorización y auditoría)

- **Estado:** Aceptado
- **Fecha:** 2026-10-03
- **Reemplaza a:** ADR-0001

## Contexto

ADR-0001 difirió la seguridad en la versión 1, dejando BR-CTR-011, BR-CTR-012 y `AUTH-001` en pausa. La API no debía exponerse fuera de un entorno controlado. Ahora se requiere autenticar a los usuarios, autorizar por permisos y auditar los eventos de seguridad (`docs/prompts/003-security-01.md`). El diseño completo está en `docs/design/003-security-design.md`.

## Decisión

- La API **emite sus propios tokens**: access token JWT (RS256, 15 min) y refresh token opaco (7 días) con rotación y detección de reutilización. Las vidas son configurables.
- Los usuarios, roles y permisos se guardan en PostgreSQL. La autorización es RBAC sobre permisos `<recurso>:<acción>[:<alcance>]`.
- Se agregan al `pom.xml` de `infrastructure`: `spring-boot-starter-security`, `spring-boot-starter-oauth2-resource-server` (incluye Nimbus) y `spring-security-test` (test). `domain` y `application` **no** dependen de Spring Security.
- Se implementa por fases: (1) autenticación y usuarios, (2) autorización y CORS, (3) auditoría, (4) endurecimiento.
- Se mantiene ADR-0003: no se agrega ArchUnit. La ausencia de Spring Security en los `pom.xml` de `domain` y `application` impide importarlo allí.

## Alternativas consideradas

1. **Servidor de autorización externo (Keycloak, Spring Authorization Server).** Pro: estándar OAuth2/OIDC completo, SSO. Contra: otro componente que desplegar y operar, y no hay requisito de SSO ni de clientes de terceros.
2. **Mantener ADR-0001 (sin seguridad).** Pro: sin costo. Contra: BR-CTR-011/012 siguen sin cumplirse y la API no puede exponerse.

## Consecuencias

- Positivas: se cumplen BR-CTR-011 y BR-CTR-012; los endpoints dejan de ser anónimos.
- Negativas: la API gestiona claves de firma y secretos; aumenta el alcance y las pruebas.
- Mientras no termine la fase 2, cualquier usuario autenticado puede invocar todos los endpoints: no desplegar fuera de entornos controlados hasta entonces.
- Seguimiento: ADRs de estrategia de tokens, resource server frente a filtro propio y modelo RBAC (fase 4).
