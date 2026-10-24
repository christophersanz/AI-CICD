# ADR-0011: Análisis estático con SonarCloud en el pipeline

- **Estado:** Aceptado
- **Fecha:** 2026-10-24

## Contexto

Los requisitos del proyecto final piden "análisis estático sin issues críticos" con un **reporte de Sonar** como evidencia, y medir los issues antes y después de los refactors asistidos por IA. El pipeline de ADR-0009 compila, prueba y mide cobertura, pero no analiza la calidad del código.

## Decisión

- Añadir al job `Build, pruebas y cobertura` un paso que ejecuta `sonar-maven-plugin` contra **SonarCloud** después de `./mvnw clean verify`, reutilizando las clases compiladas y los informes `jacoco.xml` de cada módulo.
- El paso se ejecuta solo si existe el secreto `SONAR_TOKEN`; en un fork o sin configurar se omite y el pipeline no falla.
- Se usa `-Dsonar.qualitygate.wait=true`: si no se cumple el Quality Gate, el job falla y no se publica la imagen de ese push. Con el flujo de una sola rama (ADR-0009) el análisis se ejecuta después del push a `master`, así que no lo bloquea: el fallo se corrige con un commit nuevo.
- Organización y clave del proyecto se toman de las variables `SONAR_ORGANIZATION` / `SONAR_PROJECT_KEY` o, por defecto, del dueño y nombre del repositorio.
- El plugin se invoca por coordenadas en el workflow: no se añade nada al `pom.xml`.

## Alternativas consideradas

1. **SonarQube autoalojado (Docker).** Pro: sin cuenta externa; útil también desde Jenkins. Contra: un servidor más que mantener y que no está disponible desde GitHub Actions.
2. **Solo SonarLint en el IDE.** Pro: sin configuración. Contra: no deja evidencia ni bloquea integraciones.
3. **Análisis automático de SonarCloud (sin CI).** Pro: cero configuración. Contra: no importa la cobertura de JaCoCo ni detiene la publicación de la imagen.

## Consecuencias

- Positivas: cada push a `master` queda analizado; el historial de SonarCloud da los datos de issues y cobertura "antes y después" para la presentación.
- Negativas: depende de un servicio externo y de un token; el análisis añade aproximadamente un minuto al job; hay que desactivar el análisis automático en SonarCloud.
- Seguimiento: si el Quality Gate por defecto resulta demasiado estricto al inicio, ajustarlo en SonarCloud y documentarlo aquí.
