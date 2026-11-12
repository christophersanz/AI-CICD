# CLAUDE.md

Contexto base del repositorio. Es breve y enlaza a la documentación detallada. Plantilla del kit de prompts: completa cada `[...]`.

## Documentación del proyecto

| Archivo | Contenido | Cuándo consultarlo |
|---|---|---|
| @docs/architecture.md | Capas, módulos, reglas de dependencia, convenciones, API REST, errores y pruebas | Siempre que se cree o modifique código |
| @docs/business-rules.md | Glosario, agregados, reglas `BR-*`, estados, casos de uso, errores y preguntas abiertas | Antes de implementar o cambiar lógica de negocio |
| docs/technical.md | Estado implementado: endpoints, seguridad, configuración, esquema y CI/CD | Al cambiar endpoints, configuración, migraciones o el pipeline |
| @docs/prompts/README.md | Prompts por orden de uso y sus comandos (`/NN-nombre`) | Al iniciar una tarea recurrente |

## 1. Descripción del proyecto

- **Nombre:** `[mi-servicio]`
- **Propósito:** [API REST para ...]
- **Estilo arquitectónico:** Clean Architecture + DDD táctico en módulos `domain`, `application`, `infrastructure`. Detalle en `docs/architecture.md`.
- **Paquete base:** `[com.empresa.servicio]`
- **Idioma:** código en inglés; documentación, ADR y explicaciones en español.

## 2. Stack tecnológico

| Área | Tecnología |
|---|---|
| Lenguaje | [Java 21] |
| Framework | [Spring Boot 3.x] |
| Build | [Maven] |
| Persistencia | [Spring Data JPA + PostgreSQL, migraciones con Flyway] |
| Seguridad | [Spring Security, JWT] |
| Documentación API | [springdoc-openapi] |
| Pruebas | [JUnit 5, AssertJ, Mockito, Testcontainers; cobertura con JaCoCo] |
| CI/CD | [GitHub Actions, imagen Docker] |

## 3. Comandos habituales

```bash
[./mvnw clean verify]            # Compilar, probar y verificar la cobertura (igual que el CI)
[./mvnw spring-boot:run -pl infrastructure]
[docker compose up -d]           # Dependencias locales
```

## 3.1 Flujo de Git

- [Nunca push directo a la rama principal: rama corta y Pull Request con los checks en verde.]
- Claude no integra PRs ni aprueba despliegues: lo decide el desarrollador.
- Commits en español, en imperativo, explicando el porqué.

## 4. Cómo debe trabajar Claude en este repo

- Antes de crear código, identifica en qué capa va y respeta la regla de dependencia (`docs/architecture.md` §3).
- Antes de tocar lógica de negocio, verifica `docs/business-rules.md`. **No inventes reglas de negocio:** si falta una, pregunta.
- Orden para una funcionalidad: dominio → puerto de entrada → caso de uso → puerto de salida → adaptador → controller → pruebas.
- Para tareas recurrentes usa los prompts de `docs/prompts/` con su comando `/NN-nombre`: presentan un plan y esperan aprobación, y guardan su progreso en `docs/sessions/`.
- Las decisiones arquitectónicas se registran como ADR en `docs/adr/NNNN-titulo.md`.
- No añadas dependencias sin indicarlo y justificarlo. Ejecuta el comando de build antes de dar una tarea por terminada.
