# ADR-0015: SonarQube local con Quality Gate en el pipeline de Jenkins

- **Estado:** Aceptado
- **Fecha:** 2026-11-12
- **Amplía:** [ADR-0014](0014-jenkins-por-etapas-y-despliegue-local.md) (resuelve su seguimiento de SonarQube). Complementa [ADR-0011](0011-analisis-estatico-sonarcloud.md), que usa SonarCloud en GitHub Actions.

## Contexto

El análisis estático del CI oficial depende de SonarCloud y del secreto `SONAR_TOKEN` (ADR-0011), que hoy no está configurado. El pipeline local de Jenkins desplegaba cualquier imagen que pasara las pruebas, sin control de calidad del código. En la presentación final del curso, el pipeline mejor valorado incluía SonarQube con un Quality Gate estricto antes de desplegar.

## Decisión

- El compose de `docker/jenkins/` añade `contract-api-sonarqube` (`sonarqube:26.10.0.133078-community`, versión fijada), publicado en `127.0.0.1:9000`. Usa la base embebida H2 y volúmenes propios, y lleva `SONAR_ES_BOOTSTRAP_CHECKS_DISABLE=true` para Docker Desktop. Jenkins llega a él por la red del compose (`http://sonarqube:9000`).
- Nueva etapa **Análisis SonarQube**, entre *Cobertura* e *Imagen Docker* (`docker/jenkins/scripts/sonar-analysis.sh`). Prepara solo lo que falte:
  - En el primer arranque cambia la contraseña por defecto de `admin` por `SONAR_ADMIN_PASSWORD`.
  - Genera un token de análisis y lo guarda en `$JENKINS_HOME/sonar/token`.
  - Crea el proyecto `contract-api` y el Quality Gate `contract-api`, y se lo asigna.
  - Por último, ejecuta `sonar-maven-plugin` con la cobertura de JaCoCo y espera el resultado (`sonar.qualitygate.wait`).
- El Quality Gate evalúa **todo el código, no solo el nuevo**: 0 bugs, 0 vulnerabilidades, 100 % de *security hotspots* revisados, rating A de mantenibilidad y cobertura ≥ 80 %. Las condiciones están en el script y se reescriben en cada ejecución, así que se cambian por PR.
- Si el Quality Gate falla, la etapa falla y Jenkins no construye ni despliega la imagen. Sin `SONAR_ADMIN_PASSWORD`, la etapa se omite.
- Las excepciones se justifican en el código con `@SuppressWarnings("java:Sxxxx")` y un comentario, no en el panel. Así quedan versionadas, se revisan en el PR y también valen para SonarCloud. Primeras excepciones: `java:S4502` (CSRF en una API stateless con Bearer) y `java:S5804` (el login siempre responde `AUTH-002`).

## Alternativas consideradas

1. **Usar solo SonarCloud (ADR-0011).** Pro: sin contenedores locales. Contra: requiere cuenta y token. Analiza después de GitHub Actions y no controla el despliegue local.
2. **Plugin *SonarQube Scanner* de Jenkins con `withSonarQubeEnv` y `waitForQualityGate`.** Pro: integración nativa en la interfaz. Contra: requiere un webhook de SonarQube hacia Jenkins y configurar el servidor y las credenciales en JCasC. El script con la API REST deja todo en un solo archivo versionado.
3. **SonarQube con PostgreSQL.** Pro: configuración apta para producción y actualizable. Contra: un contenedor más. Para practicar basta H2.
4. **Quality Gate por defecto (*Sonar way*, solo código nuevo).** Pro: sin configuración. Contra: en un SonarQube recién creado no hay periodo de código nuevo y el gate pasa sin evaluar nada.

## Consecuencias

- Positivas: el primer análisis encontró un bug real. La regex de `Permission` podía desbordar la pila con entradas largas (`java:S5998`); se corrigió y quedó cubierto con una prueba de regresión. Ninguna imagen con bugs o vulnerabilidades nuevas llega al despliegue local.
- Negativas:
  - SonarQube consume ~2 GB de memoria y tarda 1–2 minutos en arrancar.
  - La base H2 no permite subir de versión conservando los datos. Para actualizar la imagen hay que borrar los volúmenes `sonarqube-*`; el script vuelve a preparar todo en el siguiente build.
  - Cambiar `SONAR_ADMIN_PASSWORD` después del primer arranque requiere cambiarla también en SonarQube.
- Seguimiento: si se activa SonarCloud (ADR-0011), alinear su Quality Gate con estas condiciones.
