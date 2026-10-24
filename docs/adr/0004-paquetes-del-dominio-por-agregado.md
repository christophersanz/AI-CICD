# ADR-0004: Organizar el módulo `domain` por agregado con `common` y `shared`

- **Estado:** Aceptado
- **Fecha:** 2026-10-03

## Contexto

`domain` estaba organizado por tipo técnico (`model/`, `event/`, `exception/`). `model/` mezclaba 19 clases de dos agregados, lo que dificultaba ver qué pertenece a `Contract` y qué a `Employee`. Tampoco había bloques DDD reutilizables: `Contract` gestionaba sus eventos a mano y los IDs no compartían contrato.

## Decisión

- Un paquete por agregado (`contract`, `employee`) con los subpaquetes `model`, `vo`, `event` y `exception`. Los enums van en `vo`.
- `common`: abstracciones DDD sin significado de negocio (`AggregateRoot`, `Entity`, `Identifier`, `DomainEvent`, `DomainException`, `InvalidValueException`, `Guard`).
- `shared`: conceptos de negocio transversales; hoy solo `Money`. `MinimumWage` queda en `contract` porque solo lo usa BR-CTR-004.
- `Identifier` es una interfaz: los IDs son `record` y no pueden extender clases.
- `DomainEvent` es una interfaz abierta. Cada agregado sella su jerarquía en su propio paquete (`ContractEvent`), porque sin `module-info` un `sealed` solo admite subtipos del mismo paquete.
- Los eventos llevan `eventId`, `occurredOn` y `aggregateId`. `occurredOn` sale de un `Clock` recibido por los métodos del agregado.
- No se crea la interfaz marcadora `ValueObject`: `record` ya da igualdad por valor e inmutabilidad, y la interfaz no añadiría comportamiento.
- Se mantiene ADR-0003: no se añade ArchUnit. Las reglas de dependencia entre paquetes (`docs/architecture.md`, sección 2) se verifican en la revisión de código.

## Alternativas consideradas

1. **Mantener la organización por tipo técnico.** Pro: no hay que mover nada. Contra: no escala con más agregados y oculta los límites entre ellos.
2. **Un paquete plano por agregado, sin subpaquetes.** Pro: conserva la visibilidad package-private entre el agregado y sus VOs. Contra: vuelve a juntar muchos archivos en un mismo paquete.
3. **Un módulo Maven por agregado.** Pro: el compilador haría cumplir los límites. Contra: más módulos sin una necesidad justificada (CLAUDE.md).

## Consecuencias

- Positivas: límites de agregado visibles; ningún agregado depende de otro salvo por `EmployeeId`; eventos con metadatos homogéneos; los enums concentran las reglas (`ContractStatus.canTransitionTo`, `ContractType.isRenewable`, `DocumentType.isValidFormat`).
- Negativas:
  - Se pierde la visibilidad package-private. `ContractPeriod.extendTo()` pasa a ser público; valida por sí mismo que la nueva fecha sea posterior a la actual, pero las condiciones de estado y tipo de BR-CTR-007 solo las aplica `Contract.renew()`.
  - `Contract.restore(...)` es público y no valida invariantes; queda reservado a los adaptadores de persistencia.
  - Cambian las firmas de `create`, `sign`, `renew`, `terminate` y `expire` (reciben `Clock`). Los casos de uso de contratos deberán recibir un `Clock` como bean.
  - Las reglas de dependencia dentro de `domain` no se verifican automáticamente.

## Seguimiento: propuesta para `application` e `infrastructure` (no implementada)

Alinearlos por agregado cuando existan los casos de uso de contratos, sin cambiar las reglas de la sección 3:

```
application/
  contract/{port/in, port/out, usecase, dto, exception}
  employee/{port/in, port/out, usecase, dto, exception}
  common/exception/                 # ApplicationException, NotFoundException, ConflictException
infrastructure/
  contract/{api/rest, api/dto, persistence/{adapter, entity, repository, mapper}}
  employee/{api/rest, api/dto, persistence/{adapter, entity, repository, mapper}}
  common/{api/rest/error, config}   # GlobalExceptionHandler, UseCaseConfig, Clock bean
```

- Registrar un bean `Clock.systemUTC()` en `infrastructure/config` para los casos de uso de contratos.
- Revisar `GlobalExceptionHandler`: hoy decide el 409 de CTR-008 con `instanceof`; podría mapear por `code`.
