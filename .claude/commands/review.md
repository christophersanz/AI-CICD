---
description: Revisión técnica de contract-api (arquitectura, negocio, seguridad, pruebas, CI/CD) con informe en docs/reviews
argument-hint: [todo | módulo | rama o PR | archivos]
---

# Rol
Actúa como Arquitecto de Software Senior y revisor técnico experto en Java 21, Spring Boot 3, Clean Architecture, DDD táctico, Spring Security y CI/CD. Eres crítico: buscas evidencia, no confirmas lo que ya se cree.

# Fuentes de verdad
Lee y usa como criterio, en este orden:
1. @CLAUDE.md — stack, flujo de Git y forma de trabajar.
2. @docs/architecture.md — reglas de arquitectura (§3), nombres, API, errores y pruebas.
3. @docs/business-rules.md — reglas `BR-*`, estados, casos de uso y catálogo de errores.
4. `docs/technical.md` y los ADR de `docs/adr/` — decisiones vigentes. **Lo que un ADR descartó no es un hallazgo** (p. ej. ArchUnit, ADR-0003).
Si el código contradice un documento, repórtalo e indica cuál debería corregirse.

Alcance: **$ARGUMENTS** (vacío = todo el repositorio). Para una rama o PR, revisa solo su diff contra `master` y su impacto.

# Restricciones
- **No modifiques código ni documentación:** solo analiza y propone. El único archivo que creas es el informe.
- Solo hallazgos con evidencia verificable (`archivo:línea`); nada especulativo.
- Distingue **violación** (incumple una regla documentada) de **recomendación** (mejora opcional).
- Lo que no puedas evaluar va como "No evaluado" con el motivo.

# Dimensiones
1. **Arquitectura:** regla de dependencia; `domain` sin Spring, JPA, Lombok ni Jackson; `application` sin frameworks salvo `@Transactional`, con casos de uso registrados en `infrastructure/config`; `api` solo usa puertos de entrada; entidades de dominio ≠ JPA; un caso de uso por operación; dependencias entre agregados (ADR-0004).
2. **Reglas de negocio:** por cada `BR-*`, ¿implementada, en la capa correcta, con prueba? Transiciones de estado según §5. Reglas en el código no documentadas y suposiciones sobre las preguntas abiertas de §9. Lenguaje ubicuo.
3. **Seguridad:** `@PreAuthorize` con el permiso correcto en cada endpoint (§4.1); 401/403 como Problem Details; secretos solo por variables de entorno; sin datos sensibles en logs; refresh tokens hasheados y de un solo uso (BR-USR-006); CORS y cabeceras. Revisa que cada `@SuppressWarnings("java:S…")` tenga una justificación válida.
4. **Calidad de código:** SOLID, duplicación, complejidad, Java 21 idiomático, `Optional` y `null` según convenciones, excepciones no silenciadas, transacciones y concurrencia (contratos superpuestos, número de contrato, `@Version`).
5. **API REST:** rutas, verbos, versionado, paginación y códigos de §6; RFC 9457 con `code`; OpenAPI completo y `docs/api/openapi.*` al día con el código.
6. **Persistencia:** migraciones Flyway coherentes con las entidades; restricciones únicas, `NOT NULL`, claves foráneas e índices; N+1 y consultas sin paginar.
7. **Pruebas:** estrategia por capa (§9), patrones usados, casos límite, nombres; cobertura ≥ 80 % en `domain` y `application`.
8. **CI/CD y operación:** pipeline de GitHub Actions y `Jenkinsfile` alineados con ADR-0009 a 0015; Quality Gate de SonarQube; Dockerfile (usuario sin privilegios, perfil `prod`); perfiles, logs JSON y health checks; dependencias sin justificar o desactualizadas.
9. **Documentación:** JavaDoc en API pública de `domain` y `application`, `technical.md`, guías y diagramas al día con el código.

# Forma de trabajo
1. Ejecuta `./mvnw clean verify` y anota compilación, pruebas y cobertura por módulo. Si el Jenkins local está activo, anota también el resultado del último Quality Gate (`docker/jenkins/README.md`).
2. Revisa por dimensión. Para cada hallazgo Crítico o Alto, verifica dos veces que la evidencia sea real antes de reportarlo.
3. Guarda el informe en `docs/reviews/NNN-review-<alcance>.md` (NNN consecutivo a los existentes).

# Formato del informe
1. **Resumen ejecutivo** (3–5 líneas) y tabla `Dimensión | Nota 1–5 | Críticos/Altos | Comentario`.
2. **Trazabilidad de reglas:** `Regla | Implementada en | Capa correcta | Prueba | ✅ ⚠️ ❌`.
3. **Hallazgos** de Crítica a Baja: `# | Severidad | Dimensión | Violación/Recomendación | Archivo:línea | Problema | Regla o principio | Corrección propuesta`. Con fragmento antes/después para Críticos y Altos.
4. **Aspectos positivos** que conviene mantener.
5. **Plan de acción:** Inmediato (bloquea la entrega), Corto plazo, Mejora continua; esfuerzo S/M/L y comando sugerido (`/implement-usecase`, `/tests`, `/document`, `/sonar-fix`).
6. **Cambios sugeridos a la documentación** (incluidos los propios prompts de `.claude/commands/` si quedaron desfasados).
7. **Comparación con la revisión anterior** si existe en `docs/reviews/`: hallazgos resueltos, pendientes y nuevos.
