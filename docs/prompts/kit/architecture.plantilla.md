# Arquitectura

Referencia técnica del servicio: **cómo** se estructura y escribe el código. Plantilla del kit de prompts. Los prompts citan estas secciones por número (`architecture.md` §2, §3, §5, §6, §7, §9): conserva la numeración.

## 1. Visión general
[Clean Architecture + DDD táctico: el dominio en el centro, sin frameworks; lo técnico en `infrastructure`, conectado mediante puertos. Diagrama Mermaid de capas.]

## 2. Estructura de módulos y paquetes
[Árbol de módulos y de paquetes por capa (`domain/<agregado>/{model,vo,event,exception}`, `application/{port/in,port/out,usecase,dto}`, `infrastructure/{api,persistence,client,security,config}`) y reglas de dependencia entre paquetes del dominio.]

## 3. Reglas de arquitectura (obligatorias)
1. **Regla de dependencia:** `infrastructure` → `application` → `domain`.
2. **`domain` es Java puro:** sin Spring, JPA, Lombok ni Jackson.
3. **`application` no conoce frameworks** salvo, como máximo, `@Transactional`; los casos de uso se registran como beans en `infrastructure/config`.
4. **Nunca exponer entidades** de dominio ni JPA en la API.
5. **Entidad de dominio ≠ entidad JPA.**
6. **Un caso de uso = una clase = una operación de negocio.**
7. **Los puertos de salida los define `application`** y los implementa `infrastructure`.
8. **Las invariantes de negocio viven en el dominio.**
9. **Aislamiento dentro de `infrastructure`:** `api` solo invoca puertos de entrada.
[Ajusta o añade reglas; se verifican con `/06-revisar`.]

## 4. Flujo de una petición
[Controller → puerto de entrada → caso de uso → dominio / puertos de salida → Result → Response; errores a Problem Details.]

## 5. Convenciones de código
[Tabla de nombres (`<Verbo><Entidad>UseCase`, `<Entidad>RepositoryPort`, `<Entidad>JpaEntity`, …) y reglas de estilo (inmutabilidad, value objects, inyección por constructor, `Optional` solo como retorno, sin `null` en el dominio).]

## 6. Diseño de la API REST
[Versionado, recursos, paginación y tabla de códigos HTTP (201 + `Location`, 204, 400, 401/403, 404, 409, 422).]

## 7. Manejo de errores
[El dominio lanza excepciones de negocio; solo la capa API traduce a HTTP; formato RFC 9457 con campo `code` del catálogo de `business-rules.md` §8.]

## 8. Transversales
[Transacciones, seguridad, logs, configuración y perfiles.]

## 9. Estrategia de pruebas
[Tabla por capa (dominio sin mocks, aplicación con mocks de puertos, integración con la base real, slices web), nombres `should<Resultado>When<Condición>` y cobertura mínima.]
