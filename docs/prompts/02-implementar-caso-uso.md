# 02 · Implementar un caso de uso

| | |
|---|---|
| **Comando** | `/02-implementar-caso-uso <NombreDelCasoDeUso>` |
| **Archivo** | `docs/prompts/02-implementar-caso-uso.md` (única versión; los cambios se hacen aquí y quedan en el historial de git) |
| **Origen** | Mejora del prompt del curso [`004-implement-usecase.md`](https://github.com/christophersanz/AI-CICD/blob/33f74fc/docs/prompts/004-implement-usecase.md) |

> `{ARGUMENTO}` es lo que se escribe después del comando. `DP.<clave>` es un dato del proyecto: está en `CLAUDE.md` → *Datos para los prompts*. Sin Claude Code, copia este prompt y reemplaza `{ARGUMENTO}` a mano.

## Rol
Actúa como desarrollador senior de Java 21 y Spring Boot 3 experto en Clean Architecture, DDD táctico y Spring Security.

## Contexto y fuentes de verdad
- Lee: `CLAUDE.md`, `docs/architecture.md`, `docs/business-rules.md`; `docs/technical.md` (estado implementado) y los ADR de `docs/adr/`.
- Seguridad y autorización: `DP.seguridad`. `domain` y `application` no dependen de Spring Security.
- Caso de uso: **{ARGUMENTO}**

## Validación del argumento
- Vacío: muestra los casos de uso de `business-rules.md` §6 indicando cuáles ya tienen código y pide que vuelva a ejecutar el comando con uno.
- No coincide con un caso de uso documentado: sugiere el más parecido y espera confirmación.
- Si el caso de uso depende de preguntas abiertas (§9) que bloquean su comportamiento, recomienda primero `/01-reglas-negocio <tema>` y detente.

## Objetivo
El caso de uso responde por su endpoint con los códigos de `architecture.md` §6, cada regla y error de su ficha tiene al menos una prueba, `DP.build` pasa y el Quality Gate (`DP.sonarqube`) sigue en verde.

## Restricciones
- No inventes reglas de negocio: lo no documentado se pregunta.
- No uses `now()` en `domain` ni en el caso de uso: inyecta un `Clock`.
- No añadas dependencias al `pom.xml` sin indicarlo y justificarlo. Respeta `DP.adr-arquitectura`.
- Si un paso obliga a violar una regla de arquitectura (`architecture.md` §3), detente y explica el conflicto con alternativas.
- No integres el PR: el *Squash and merge* es del autor.

## Forma de trabajo
- Trabaja en una rama `feature/<caso-de-uso-en-kebab-case>` creada desde `DP.rama-principal` actualizado.
- Crea o retoma `docs/sessions/AAAA-MM-DD-02-implementar-caso-uso-{ARGUMENTO}.md` (docs/sessions/README.md).
- **Un paso a la vez.** Al terminar cada paso: archivos creados o modificados (ruta completa), resumen de 3–5 líneas con las decisiones, si compila y si pasan las pruebas del paso; actualiza el archivo de sesión y **espera mi "continuar"**.
- Si detectas un problema en un paso anterior, dilo antes de seguir; no lo corrijas en silencio.
- **Autovalidación por capa:** al terminar los pasos 1 (dominio), 3 (caso de uso), 4 (persistencia) y 5 (API), repasa sin modificar nada las comprobaciones del comando de revisión de esa capa (`docs/prompts/10-revisar-dominio.md`, `11-revisar-aplicacion.md`, `12-revisar-infraestructura.md`, `13-revisar-api.md`) solo sobre los archivos del paso, y reporta los ERROR y WARNING antes de pedir "continuar".
- Si una prueba falla, investiga la causa raíz antes de cambiar código; corrige solo problemas verificados y no reescribas código no relacionado.

### Paso 0 — Ficha y análisis de impacto (sin código)
1. Ficha: agregado, actor y **permiso** (`<recurso>:<acción>`), entrada (Command/Query con campos y tipos), salida (Result o sin contenido), reglas con su capa, puertos de salida, endpoint (método, ruta, código de éxito), eventos (§7), errores (§8) con su HTTP, dependencias.
2. Marca cada dato como **[documentado]** o **[propuesto]**. Lo propuesto solo puede ser una decisión técnica, nunca una regla de negocio.
3. Inventario: clases que ya existen y sirven, y las que faltan o hay que ampliar. Si hace falta un permiso nuevo, la migración (`DP.migraciones`) que lo siembre.
4. Comparación con el código actual: qué ya está bien y se reutiliza, qué hay que cambiar, qué no se toca, código obsoleto y supuestos.
5. Flujo numerado indicando en qué paso se valida cada regla.
6. Archivos que se crearán, modificarán o eliminarán, riesgos (concurrencia, transacciones, compatibilidad de la API, **sobreingeniería**) y preguntas (máximo 5).
Espera mi aprobación de la ficha.

### Paso 1 — Dominio
Comportamiento en el agregado con nombres del lenguaje ubicuo; value objects, enums y excepciones nuevas o ampliadas, validados en el constructor; eventos emitidos por el agregado. Pruebas de dominio sin Spring ni mocks: una por invariante y por caso límite.

### Paso 2 — Puertos y DTOs de aplicación
`<Verbo><Entidad>UseCase` en `port/in`; `Command`/`Query`/`Result` como records; métodos de `port/out` expresados en términos del dominio, sin tipos técnicos.

### Paso 3 — Caso de uso
`<Verbo><Entidad>Service` sin anotaciones de Spring salvo `@Transactional`, registrado como bean en `infrastructure/config`. Pruebas con Mockito (solo puertos de salida): camino feliz y una por regla y por error de la ficha.

### Paso 4 — Persistencia
Entidad JPA, repositorio, mapper y adaptador; migración (`DP.migraciones`) con índices para las consultas de las reglas. Prueba de integración según `DP.pruebas-integracion`.

### Paso 5 — API REST
Request/Response en `api/dto`, mappers, Bean Validation solo de formato, `@PreAuthorize("hasAuthority('<permiso>')")`, anotaciones OpenAPI y traducción de excepciones nuevas a Problem Details. `@WebMvcTest`: éxito, 400, 401 sin token, 403 sin permiso y cada error de negocio. Si no hay endpoint (proceso del Sistema), omite el paso y dilo.

### Paso 6 — Pruebas
Aplica los criterios de `/03-pruebas` (o ejecútalo después con `{ARGUMENTO}`): matriz regla/error → prueba, casos límite, parametrizadas, fixtures reutilizables y cobertura según `DP.cobertura-minima`.

### Paso 7 — Cierre
1. `DP.build` en verde.
2. Documentación del mismo cambio: `business-rules.md` (tabla de casos de uso, reglas o errores aclarados), `technical.md` (endpoints, permisos, migraciones), el contrato OpenAPI regenerado (`DP.openapi`) y la colección de `DP.postman` si hay endpoint nuevo.
3. Commit en español en imperativo, push y PR con la plantilla (`.github/pull_request_template.md`) completa.
4. Tras el merge, el CI local (`DP.ci-local`) construye la rama principal; si el Quality Gate falla, `/20-corregir-sonar`.

## Informe final
Archivos creados, modificados, movidos y eliminados; pruebas (ejecutadas, exitosas, fallidas, con error, omitidas); endpoint y permiso, reglas cubiertas con sus pruebas, cobertura medida, documentos actualizados, PR abierto y TODOs pendientes referenciados a su regla o pregunta abierta (si no hay: "Sin problemas pendientes"). Siguiente paso recomendado: `/05-revisar-pr <número>`.

**No afirmes que funciona si no se compiló y probó.**
