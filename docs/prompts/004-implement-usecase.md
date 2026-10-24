---
description: Implementa un caso de uso de contract-api paso a paso (dominio → API → pruebas)
argument-hint: <NombreDelCasoDeUso>
---

Implementa el caso de uso **$ARGUMENTS** de contract-api paso a paso, siguiendo
@CLAUDE.md, @docs/architecture.md y @docs/business-rules.md.

## Validación del argumento
- Si no se indicó un caso de uso (argumento vacío), no hagas nada más: muestra la lista de casos
  de uso de business-rules.md (sección 6), indicando cuáles ya están implementados en el código,
  y pide que vuelva a ejecutar el comando con uno de ellos.
- Si el nombre no coincide exactamente con un caso de uso documentado, sugiere el más parecido
  y espera confirmación.

## Forma de trabajo
Ejecuta UN paso a la vez. Al terminar cada paso:
- muestra los archivos creados o modificados (ruta completa);
- resume en 3–5 líneas qué hiciste y qué decisiones tomaste;
- indica si compila y si las pruebas del paso pasan;
- **detente y espera mi "continuar"** antes del siguiente paso.
Si detectas un problema en un paso anterior, dilo antes de seguir; no lo corrijas en silencio.

## Pasos

### Paso 0 — Ficha del caso de uso (sin código)
1. Busca el caso de uso en business-rules.md (sección 6 y su detalle, si existe).
2. Construye la ficha del caso de uso:
   - Dominio: contract | employee
   - Actor
   - Entrada: Command/Query con sus campos y tipos
   - Salida: Result con sus campos, o sin contenido
   - Reglas: IDs con su descripción y la capa donde se valida (domain o application)
   - Puertos de salida
   - Endpoint: método, ruta y código de éxito (según architecture.md sección 6)
   - Eventos de dominio emitidos (sección 7)
   - Errores: códigos del catálogo (sección 8) con su código HTTP
   - Depende de: casos de uso o piezas que deben existir antes
3. Marca cada dato de la ficha como **[documentado]** (sale de los docs) o **[propuesto]**
   (lo infieres tú). Todo lo [propuesto] debe ser una decisión técnica, nunca una regla de negocio.
4. Inventario: qué clases ya existen y sirven (agregados, VOs, enums, excepciones, puertos)
   y cuáles faltan o hay que ampliar.
5. Flujo del caso de uso numerado, indicando en qué paso se valida cada regla.
6. Preguntas: si una regla depende de "Preguntas abiertas" de business-rules.md o no está
   definida, pregúntame (máximo 5). No supongas reglas de negocio.
Espera mi confirmación de la ficha antes de pasar al Paso 1.

### Paso 1 — Dominio
- Comportamiento en el agregado (método con nombre del lenguaje ubicuo, p. ej. `sign()`, `renew()`).
- Value objects, enums y excepciones nuevas o ampliadas; validación en constructores.
- Emisión de eventos de dominio desde el agregado.
- Pruebas unitarias de dominio: una por invariante y por caso límite, sin mocks ni Spring.

### Paso 2 — Puerto de entrada y DTOs de aplicación
- Interfaz `<Verbo><Entidad>UseCase` en `port/in`.
- `Command` / `Query` / `Result` como records inmutables en `dto`, con validación mínima de
  presencia (no reglas de negocio).

### Paso 3 — Puertos de salida
- Define o amplía las interfaces en `port/out` con métodos expresados en términos del dominio
  (p. ej. `existsActiveOverlapping(EmployeeId, ContractPeriod)`), sin tipos técnicos.

### Paso 4 — Caso de uso
- `<Verbo><Entidad>Service` sin anotaciones de Spring salvo `@Transactional`.
- Orquesta: carga → delega al dominio → valida reglas de `application` → persiste → devuelve Result.
- Pruebas unitarias con Mockito (solo se mockean puertos de salida): camino feliz y un test por
  cada regla y cada error de la ficha.

### Paso 5 — Adaptador de persistencia
- Entidad JPA, repositorio Spring Data, mapper dominio ↔ JPA y adaptador que implementa el puerto.
- Migración Flyway si cambia el esquema (índices para las consultas de reglas, p. ej. superposición).
- Prueba de integración con Testcontainers (PostgreSQL real).

### Paso 6 — Controller REST
- Si la ficha indica que no hay endpoint (p. ej. un proceso del Sistema), omite este paso y dilo.
- Request/Response en `api/dto`, mapper Request → Command y Result → Response.
- Bean Validation solo de formato; códigos HTTP según architecture.md sección 6
  (`201` + `Location` al crear).
- Anotaciones OpenAPI.
- Registro del caso de uso como bean en `infrastructure/config`.
- Traducción de las excepciones nuevas a Problem Details en `api/rest/error`.
- `@WebMvcTest`: caso exitoso, error de validación (400) y cada error de negocio de la ficha.

### Paso 7 — Pruebas
Revisa y completa las pruebas de todas las capas del caso de uso.
1. **Matriz de trazabilidad:** tabla regla / error → prueba(s) que la cubren, con capa y nombre
   del test. Toda regla y todo error de la ficha debe tener al menos una prueba; crea las que falten.
2. **Casos límite** no cubiertos: valores en el borde, periodos que se tocan en un día,
   campos opcionales ausentes, transiciones de estado no permitidas desde cada estado.
3. **Pruebas parametrizadas** (`@ParameterizedTest`) cuando una regla se verifique con varios valores.
4. **Fixtures reutilizables:** test data builders u object mothers por agregado
   (p. ej. `ContractMother.activeFixedTerm()`).
5. **Calidad:** nombres `should<Resultado>When<Condición>`, Given / When / Then, una sola razón de
   fallo por test, verificación de eventos emitidos y del Problem Details completo
   (`type`, `status`, `code`).
6. **Cobertura** de `domain` y `application` (mínimo 80 %). Si JaCoCo no está configurado,
   indícalo y justifica añadirlo al pom.xml antes de hacerlo.

### Paso 8 — Cierre
- `./mvnw clean verify` en verde, incluidas las pruebas de ArchUnit y la cobertura del Paso 7.
- Actualiza business-rules.md si se agregó o aclaró una regla, error o evento, y la tabla de
  casos de uso si cambió la entrada o la salida.
- Lista de TODOs pendientes, cada uno referenciado a su regla o pregunta abierta.

## Restricciones
- La seguridad aún no está implementada: no añadas autorización. Deja un TODO referenciando
  BR-CTR-011 / BR-CTR-012 en el caso de uso y el controller.
- No uses `LocalDate.now()` en dominio ni en el caso de uso: inyecta un `Clock`.
- No añadas dependencias al pom.xml sin indicarlo y justificarlo.
- Si un paso obliga a violar una regla de arquitectura, detente y explica el conflicto.
