# Reglas de negocio

Fuente de verdad del **qué** hace el sistema. Plantilla del kit de prompts. Los prompts citan estas secciones por número (`business-rules.md` §1 a §9): conserva la numeración. Para completarla, usa `/01-definir requisitos` y `/01-definir reglas`.

> Cada regla lleva un identificador `BR-<CTX>-NNN` para referenciarla en código, pruebas y PRs.

## 1. Contexto del dominio
- **Negocio:** [qué resuelve el sistema]
- **Actores:** [roles y qué hace cada uno]
- **Bounded context de este servicio:** [nombre]
- **Fuera de alcance:** [lo que es de otros sistemas o no se hará]

## 2. Glosario (lenguaje ubicuo)

| Término (ES) | Término en código | Definición |
|---|---|---|
| [Pedido] | `Order` | [...] |

## 3. Entidades y agregados
[Por cada agregado, tabla `Atributo | Tipo | Reglas`.]

## 4. Invariantes y reglas

| ID | Regla | Capa | Error |
|---|---|---|---|
| BR-[CTX]-001 | [...] | domain / application | `[CTX]-001` |

### 4.1 Roles y permisos
[Tabla `Permiso | Rol A | Rol B` con permisos `<recurso>:<acción>[:<alcance>]`. Si todavía no hay seguridad, indícalo.]

## 5. Estados y transiciones
[Diagrama `stateDiagram-v2` por agregado con estados; qué error lanza una transición no dibujada.]

## 6. Casos de uso

| Caso de uso | Actor | Entrada | Salida | Reglas |
|---|---|---|---|---|
| `[Create]OrderUseCase` | [...] | `[...]Command` | `[...]Result` | BR-[CTX]-001 |

[Detalle numerado de los casos de uso no triviales.]

## 7. Eventos de dominio
[Tabla `Evento | Se emite cuando | Datos`, o "No hay eventos".]

## 8. Catálogo de errores de negocio

| Código | Excepción | HTTP | Mensaje |
|---|---|---|---|
| `[CTX]-001` | `[...]Exception` | 422 | [...] |

## 9. Preguntas abiertas
Reglas pendientes de confirmar. Claude no implementa suposiciones sobre estos puntos.
- [ ] [...]
