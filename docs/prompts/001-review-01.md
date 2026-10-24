# Rol
Actúa como Arquitecto de Software Senior y revisor técnico experto en Java 21, Spring Boot 3,
Clean Architecture, DDD táctico, seguridad de APIs y buenas prácticas de ingeniería.

# Objetivo
Realiza una revisión integral del proyecto `contract-api` (paquete base `pe.edu.galaxy.training.ia.ca`)
evaluando arquitectura, reglas de negocio, calidad de código y buenas prácticas.

- Alcance: [todo el repositorio | módulo X | rama/PR Y | archivos: ...]
- Contexto adicional: [p. ej. "antes de pasar a QA", "primer sprint"]

# Fuentes de verdad
Antes de revisar, lee y usa como criterio:
1. `CLAUDE.md` → stack, convenciones generales y forma de trabajo.
2. `docs/architecture.md` → capas, reglas de dependencia (sección 3), nombres, API REST, errores y pruebas.
3. `docs/business-rules.md` → glosario, reglas `BR-*`, estados, casos de uso y catálogo de errores.

Si el código contradice la documentación, repórtalo e indica cuál de los dos debería corregirse.

# Dimensiones a evaluar

## 1. Arquitectura
- Regla de dependencia `infrastructure → application → domain`; `domain` sin Spring, JPA, Lombok ni Jackson.
- `application` sin frameworks alguno (sin Spring, Lombok y otras librerias); casos de uso registrados en `infrastructure/config`.
- `api` solo invoca puertos de entrada; no usa `persistence` ni `client` directamente.
- Entidad de dominio separada de la entidad JPA; mappers en su lugar.
- Un caso de uso = una operación de negocio; sin "servicios dios".
- Existencia y cobertura de `ArchitectureTest` con ArchUnit.

## 2. Reglas de negocio
- Para cada regla `BR-*`: ¿está implementada?, ¿en la capa correcta?, ¿tiene prueba?
- Transiciones de estado del contrato según el diagrama; transiciones inválidas lanzan `CTR-008`.
- Códigos de error y HTTP alineados con el catálogo.
- Uso del lenguaje ubicuo del glosario en clases, métodos y variables.
- Detecta reglas implementadas en el código que NO están documentadas y suposiciones sobre las
  "Preguntas abiertas".

## 3. Calidad de código
- SOLID, cohesión, acoplamiento, métodos largos, duplicación y complejidad.
- Uso idiomático de Java 21: `record`, `sealed`, pattern matching, inmutabilidad.
- Value objects validados en constructor; entidades con comportamiento, sin setters.
- Manejo de `null` y `Optional` según convenciones; excepciones no silenciadas.
- Transacciones: límites correctos, sin lógica transaccional en controllers.
- Concurrencia: riesgo de condiciones de carrera (p. ej. BR-CTR-005 contratos superpuestos,
  generación de `ContractNumber`); uso de bloqueo optimista (`@Version`).

## 4. API REST
- Rutas, verbos, versionado, paginación y códigos HTTP según `architecture.md` sección 6.
- Errores en RFC 9457 con campo `code`; sin stack traces ni detalles internos.
- Validación de entrada con Bean Validation; documentación OpenAPI completa.

## 6. Persistencia
- Migraciones Flyway versionadas y consistentes con las entidades JPA.
- Restricciones en BD: unicidad de `ContractNumber` y `DocumentNumber`, `NOT NULL`, claves foráneas.
- Índices para búsquedas frecuentes (empleado, estado, fechas).
- Problemas N+1, `FetchType.EAGER` innecesario, consultas sin paginar.

## 7. Pruebas
- Estrategia por capa según `architecture.md` sección 9.
- Pruebas de dominio sin Spring ni mocks; casos de uso mockeando solo puertos.
- Integración con Testcontainers (no H2); slices `@WebMvcTest` para cada error esperado.
- Nombres `should<Resultado>When<Condición>`, Given/When/Then, cobertura ≥ 80 % en domain y application.

## 8. Operación y configuración
- Perfiles `local/dev/prod`, logs JSON con `traceId`, métricas y health checks.
- Dependencias no justificadas o desactualizadas en el `pom.xml`.

# Forma de trabajar
- Ejecuta `./mvnw clean verify` y reporta el resultado (compilación, pruebas, cobertura).
- Reporta solo hallazgos con evidencia verificable en el código; nada especulativo.
- Distingue entre **violación** (incumple una regla documentada) y **recomendación** (mejora opcional).
- No modifiques código: solo analiza y propone.
- Si algo no puede evaluarse (p. ej. falta un módulo), indícalo como "No evaluado" con el motivo.

# Formato de entrega

## 1. Resumen ejecutivo
Estado general en 3–5 líneas y tabla de puntuación:

| Dimensión | Nota (1–5) | Hallazgos críticos/altos | Comentario |
|---|---|---|---|

## 2. Trazabilidad de reglas de negocio

| Regla | Implementada en (clase/método) | Capa correcta | Prueba | Estado ✅ ⚠️ ❌ |
|---|---|---|---|---|

## 3. Hallazgos
Ordenados por severidad (Crítica → Alta → Media → Baja):

| # | Severidad | Dimensión | Tipo (Violación/Recomendación) | Archivo:línea | Problema | Regla/principio | Corrección propuesta |
|---|---|---|---|---|---|---|---|

Para cada hallazgo Crítico o Alto incluye un fragmento de código "antes / después".

## 4. Aspectos positivos
Lo que está bien resuelto y conviene mantener.

## 5. Plan de acción
Tareas priorizadas en tres grupos: **Inmediato** (bloquea release), **Corto plazo**, **Mejora continua**.
Indica esfuerzo estimado (S/M/L) por tarea.

## 6. Actualizaciones sugeridas a la documentación
Cambios propuestos a `architecture.md` o `business-rules.md` si la revisión revela vacíos o contradicciones.