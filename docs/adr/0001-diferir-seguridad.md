# ADR-0001: Diferir la seguridad (autenticación y autorización) a una versión posterior

- **Estado:** Reemplazado por ADR-0005
- **Fecha:** 2026-09-26

## Contexto

`business-rules.md` define BR-CTR-011 (roles `HR`/`ADMIN` para operaciones de escritura) y BR-CTR-012 (un empleado solo consulta sus contratos), con el error `AUTH-001`. `architecture.md` preveía validar JWT con Spring Security + OAuth2 Resource Server. El stack de `CLAUDE.md` retiró Spring Security y se decidió que la versión 1 no incluye seguridad.

## Decisión

La versión 1 de la API **no autentica ni autoriza**. BR-CTR-011, BR-CTR-012 y `AUTH-001` quedan marcadas como diferidas (⏸️) en `business-rules.md`. No se agrega Spring Security al `pom.xml`.

## Alternativas consideradas

1. **Spring Security + OAuth2 Resource Server (JWT) desde el inicio.** Pro: cumple BR-CTR-011/012 desde el principio. Contra: exige un emisor de tokens (Keycloak u otro) en local y en pruebas; aumenta el alcance inicial.
2. **Cabecera simple con rol (`X-User-Role`) sin validar.** Pro: barato. Contra: da una falsa sensación de seguridad y habría que eliminarla después.

## Consecuencias

- Positivas: menor alcance inicial; los casos de uso se centran en las reglas de dominio.
- Negativas: la API no debe exponerse fuera de un entorno controlado hasta incorporar seguridad.
- Seguimiento: al incorporar seguridad, los `Command`/`Query` recibirán el usuario autenticado para que `application` aplique BR-CTR-012, y los roles se declararán en los controllers.
