# Prompts reutilizables

Catálogo de prompts para tareas frecuentes en este repositorio. Copia el prompt, rellena los campos `[ ]` y pégalo en Claude.
Todos asumen que Claude ya cargó `CLAUDE.md`, `docs/architecture.md` y `docs/business-rules.md`.

## Índice

1. [Diseño de arquitectura](#1-diseño-de-arquitectura)
2. [Nuevo caso de uso](#2-nuevo-caso-de-uso)
3. [Nuevo endpoint REST](#3-nuevo-endpoint-rest)
4. [Nuevo adaptador de salida](#4-nuevo-adaptador-de-salida)
5. [Revisión de código](#5-revisión-de-código)
6. [Generar pruebas](#6-generar-pruebas)
7. [Refactorización hacia Clean Architecture](#7-refactorización-hacia-clean-architecture)
8. [Registrar un ADR](#8-registrar-un-adr)
9. [Documentar reglas de negocio](#9-documentar-reglas-de-negocio)

## 1. Diseño de arquitectura

Úsalo al iniciar el proyecto o un nuevo bounded context.

```markdown
Actúa como Arquitecto de Software Senior experto en Java 21, Spring Boot 3, Clean Architecture,
Arquitectura Hexagonal y DDD.

Diseña la arquitectura de una API REST para el siguiente contexto:
- Dominio / negocio: [describe el negocio]
- Casos de uso principales: [lista]
- Requisitos no funcionales: [p. ej. 500 req/s, auditoría, alta disponibilidad]
- Restricciones tecnológicas: [p. ej. PostgreSQL, Kafka, Kubernetes]
- Equipo: [tamaño y nivel]

Respeta las reglas de docs/architecture.md. Entrega:
1. Visión general y justificación de decisiones clave.
2. Diagrama de capas en Mermaid.
3. Estructura de módulos y paquetes.
4. Modelo de dominio: entidades, value objects, agregados e invariantes.
5. Casos de uso con sus puertos de entrada y salida.
6. Código Java de un caso de uso completo de extremo a extremo.
7. Diseño de endpoints REST y formato de errores (RFC 9457).
8. Transversales: seguridad, transacciones, observabilidad, configuración.
9. Estrategia de pruebas.
10. 3-5 ADRs con alternativas descartadas.
11. Riesgos y trade-offs.

Si falta información crítica, haz primero un máximo de 5 preguntas concretas.
Evita la sobreingeniería: adapta la complejidad al problema y al equipo.
```

## 2. Nuevo caso de uso

```markdown
Implementa el caso de uso [NombreDelCasoDeUso] siguiendo docs/architecture.md.

- Actor: [quién lo ejecuta]
- Entrada: [datos]
- Salida: [datos]
- Reglas de negocio aplicables: [IDs de docs/business-rules.md, p. ej. BR-CTR-001]
- Puertos de salida necesarios: [repositorios, clientes externos, eventos]

Sigue el orden: dominio → puerto de entrada → caso de uso → puerto de salida → adaptador → controller → pruebas.
Incluye pruebas unitarias del dominio y del caso de uso.
Si alguna regla no está definida en business-rules.md, pregúntame antes de suponerla.
Al terminar, actualiza la tabla de casos de uso en business-rules.md.
```

## 3. Nuevo endpoint REST

```markdown
Expón el caso de uso [NombreUseCase] como endpoint REST.

- Método y ruta: [p. ej. POST /api/v1/contracts]
- Roles autorizados: [p. ej. HR, ADMIN, EMPLOYEE]
- Request: [campos y validaciones de formato]
- Response: [campos]

Cumple la sección 6 de docs/architecture.md (códigos HTTP, Problem Details, OpenAPI).
Incluye test @WebMvcTest para el caso exitoso y para cada error esperado.
```

## 4. Nuevo adaptador de salida

```markdown
Implementa el adaptador [Persistence | RestClient | Messaging] para el puerto [NombrePort].

- Tecnología: [p. ej. Spring Data JPA / RestClient / Kafka]
- Detalles: [tabla, endpoint externo, topic, etc.]
- Manejo de fallos: [reintentos, timeout, circuit breaker si aplica]

El adaptador vive en infrastructure/, mapea entre modelos técnicos y de dominio, y no filtra
tipos técnicos hacia application. Incluye prueba de integración con Testcontainers
(o WireMock para clientes HTTP). Incluye la migración Flyway si aplica.
```

## 5. Revisión de código

```markdown
Revisa los siguientes cambios [pega el diff o indica los archivos] como arquitecto de software.

Evalúa, en este orden:
1. Violaciones a las reglas de arquitectura (docs/architecture.md, sección 3).
2. Reglas de negocio mal ubicadas o incumplidas (docs/business-rules.md).
3. Errores de lógica, concurrencia o transacciones.
4. Cobertura y calidad de las pruebas.
5. Convenciones de nombres y estilo.

Para cada hallazgo indica: archivo y línea, severidad (alta/media/baja), problema y corrección propuesta.
No reportes preferencias de estilo menores si no hay una regla que las respalde.
```

## 6. Generar pruebas

```markdown
Genera las pruebas para [clase o caso de uso].

- Tipo: [unitaria de dominio | unitaria de caso de uso | integración | slice web]
- Cubre: camino feliz, cada regla de negocio aplicable ([IDs]) y casos límite.

Sigue la sección 9 de docs/architecture.md: nombres should<Resultado>When<Condición>,
estructura Given/When/Then, AssertJ, sin Spring en pruebas de dominio.
```

## 7. Refactorización hacia Clean Architecture

```markdown
Analiza [paquete, clase o módulo] y propón cómo migrarlo a la arquitectura de docs/architecture.md.

1. Diagnóstico: qué reglas de arquitectura se violan actualmente.
2. Plan de migración en pasos pequeños e independientes, cada uno compilable y con pruebas verdes.
3. Riesgos de cada paso.

No escribas código todavía; espera mi aprobación del plan.
```

## 8. Registrar un ADR

```markdown
Redacta un ADR en docs/adr/[NNNN]-[titulo-en-kebab-case].md sobre: [decisión].

Estructura:
- Estado: [Propuesto | Aceptado | Reemplazado por ADR-XXXX]
- Contexto: problema y fuerzas en juego.
- Decisión: qué se decidió.
- Alternativas consideradas: al menos 2, con pros y contras.
- Consecuencias: positivas, negativas y acciones de seguimiento.

Máximo una página. En español.
```

## 9. Documentar reglas de negocio

```markdown
A partir de la siguiente descripción funcional, actualiza docs/business-rules.md:

[pega la historia de usuario, requisito o notas de la reunión]

- Añade términos nuevos al glosario.
- Crea reglas con ID BR-[CONTEXTO]-[NNN], indicando capa y código de error.
- Actualiza estados, casos de uso, eventos y catálogo de errores si corresponde.
- Lo ambiguo o contradictorio va a "Preguntas abiertas", no lo supongas.
```
