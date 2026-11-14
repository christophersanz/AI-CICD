# Guía rápida de los prompts

Para usar los prompts no hace falta leerlos: son instrucciones para la IA. Basta con saber qué comando escribir en Claude Code.

## Qué comando uso

| Quiero… | Escribo… | Qué cambia en el proyecto |
|---|---|---|
| Convertir una idea en historias de usuario | `/01-definir requisitos <idea>` | Solo documentos |
| Cerrar una regla de negocio que falta o es dudosa | `/01-definir reglas <tema>` | Solo documentos |
| Programar un caso de uso nuevo | `/02-implementar-caso-uso <NombreUseCase>` | Código y pruebas |
| Mejorar las pruebas de algo que ya existe | `/03-pruebas <Clase o NombreUseCase>` | Solo pruebas |
| Documentar el código o la API | `/04-documentar <javadoc, openapi, diagramas, adr, guias o todo>` | Solo documentación |
| Ordenar el código sin cambiar lo que hace | `/05-refactorizar <qué y por qué>` | Código |
| Revisar una capa del proyecto | `/06-revisar <dominio, aplicacion, infraestructura, api, seguridad, cicd o arquitectura>` | **Nada:** solo escribe un informe |
| Revisar un Pull Request antes de integrarlo | `/06-revisar pr <número>` | **Nada:** solo escribe un informe |
| Corregir lo que encontró una revisión | `/07-corregir ronda <NNN>` | Código |
| Arreglar el Quality Gate de SonarQube | `/07-corregir sonar` | Código |
| Cambiar el CI/CD, Docker o el despliegue | `/08-cambiar-pipeline <cambio>` | Pipeline |
| Crear un prompt o mejorar uno | `/09-mejorar-prompt <tarea o archivo>` | Solo prompts |

Si no sabes qué escribir después del comando, escríbelo solo (por ejemplo, `/06-revisar`): el prompt te muestra las opciones y espera a que elijas.

## Cómo trabaja cada comando

1. **Analiza y te muestra un plan.** No cambia nada todavía.
2. **Espera tu aprobación.** Respondes «sí», o pides ajustes.
3. **Avanza paso a paso**, compilando y probando en cada paso.
4. **Abre un Pull Request.** La integración la decides tú: la IA solo integra si se lo pides.

Si una tarea larga se corta, vuelve a escribir el mismo comando: retoma desde donde quedó (`docs/sessions/`).

## Combinaciones habituales

| Situación | Orden |
|---|---|
| Funcionalidad nueva | `/01-definir requisitos` → `/02-implementar-caso-uso` → `/06-revisar pr <n>` |
| Antes de una entrega | `/06-revisar` por capas (dominio → … → arquitectura) → `/07-corregir ronda <NNN>` → `/06-revisar final` |
| Falló el Quality Gate | `/07-corregir sonar` |
| Código desordenado | `/05-refactorizar` → `/06-revisar <capa tocada>` |

## Dónde queda cada resultado

| Resultado | Carpeta |
|---|---|
| Informes de revisión | `docs/reviews/` (una carpeta por ronda, con su resumen) |
| Progreso de las tareas | `docs/sessions/` |
| Requisitos e historias | `docs/requirements/` |

Los datos de cada proyecto (cómo se compila, dónde están sus documentos…) están en `CLAUDE.md` → *Datos para los prompts*. Para instalar los prompts en otro proyecto, ver `README.md` §6.
