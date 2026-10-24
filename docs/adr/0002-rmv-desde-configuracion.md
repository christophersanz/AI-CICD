# ADR-0002: Obtener la RMV vigente desde la configuración

- **Estado:** Aceptado
- **Fecha:** 2026-09-26

## Contexto

BR-CTR-004 exige que la remuneración no sea menor que la RMV vigente. El glosario indica que la RMV es "configurable, no fija en código". `CreateContractUseCase` la obtiene mediante el puerto `MinimumWagePort`, pero no estaba definido su origen.

## Decisión

La RMV se lee de la propiedad `contract.minimum-wage` en `application.yml`, a través de un adaptador en `infrastructure` que implementa `MinimumWagePort`. El dominio solo recibe un `MinimumWage` y no sabe de dónde viene.

## Alternativas consideradas

1. **Tabla parametrizable con vigencias.** Pro: permite histórico y cambios sin redespliegue. Contra: requiere migración, adaptador JPA y administración de datos; innecesario mientras la RMV cambie con poca frecuencia.
2. **Servicio externo.** Pro: fuente única corporativa. Contra: no existe hoy; añade latencia y un punto de fallo.

## Consecuencias

- Positivas: implementación simple; cambiar el origen solo implica otro adaptador del mismo puerto.
- Negativas: actualizar la RMV requiere cambiar la configuración y reiniciar; no hay histórico de valores.
- Seguimiento: confirmar con RR.HH. el valor vigente antes de pasar a producción.
