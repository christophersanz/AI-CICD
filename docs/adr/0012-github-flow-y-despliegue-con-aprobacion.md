# ADR-0012: GitHub Flow con checks obligatorios y despliegue con aprobación

- **Estado:** Aceptado
- **Fecha:** 2026-11-11
- **Reemplaza:** el flujo de una sola rama con push directo de [ADR-0009](0009-ci-cd-github-actions.md) (revisión del 2026-10-24). El resto de ADR-0009 (repositorio, jobs `build` e `image`, cobertura, registros) sigue vigente.

## Contexto

Con push directo a `master`, el CI validaba **después** de integrar: un cambio roto podía llegar a `master` y a la imagen `latest`. Además, el pipeline terminaba al publicar la imagen; nada demostraba que esa imagen arrancara y respondiera. En la revisión del curso (sesiones 4 y 5) se pidió un flujo con aprobación humana en cada paso y la evidencia de que la imagen publicada funciona.

## Decisión

1. **GitHub Flow:** todo cambio entra por una rama corta (`feature/*`, `fix/*`, `docs/*`) y un Pull Request a `master`. El ruleset `master-protegida` exige:
   - Pull Request (sin push directo), integrado solo con **squash**;
   - los checks **Build, pruebas y cobertura** e **Imagen Docker** en verde;
   - la rama al día con `master`;
   - sin force push ni borrado (como antes).

   Las aprobaciones mínimas del PR son 0: con un único desarrollador, GitHub no permite aprobar el PR propio. La decisión humana es el **Squash and merge** tras revisar el diff con los checks en verde.
2. **Despliegue con aprobación:** nuevo job `deploy` (`Despliegue (production)`), solo tras un push a `master`. Usa el environment `production`, que exige la aprobación de un revisor antes de ejecutarse.
3. **Entorno efímero:** como no hay un servidor persistente, el job despliega la imagen recién publicada, por su `sha-<corto>`, en el runner junto con PostgreSQL. Usa claves JWT y una contraseña de administrador generadas en el momento, y ejecuta una prueba de humo:
   - `health` en UP;
   - login del administrador inicial;
   - `GET /auth/me` y `GET /employees` responden 200 con token;
   - `GET /employees` responde 401 sin token.

   Después descarta el entorno.

## Alternativas consideradas

1. **Mantener el push directo** y poner la aprobación solo en el despliegue. Pro: menos ceremonia. Contra: el CI sigue validando después de integrar.
2. **Exigir 1 aprobación en el PR.** Pro: revisión por pares real. Contra: bloquea a un único desarrollador; se activa cuando haya un segundo revisor.
3. **Desplegar en un servidor real** (VM, Render, Fly.io). Pro: entorno persistente. Contra: costo, credenciales y operación que el proyecto aún no necesita. Este job es el punto donde se agregaría.

## Consecuencias

- Positivas:
  - ningún cambio llega a `master` sin el CI en verde;
  - cada despliegue queda registrado con quién lo aprobó (*Actions → Deployments*);
  - la imagen publicada se prueba de forma automática.
- Negativas:
  - cada cambio, incluida la documentación, pasa por PR y espera el CI;
  - la ejecución en `master` queda en *Waiting* hasta aprobar el despliegue.
- El ruleset y el environment se configuran en GitHub. El JSON de `.github/ruleset/` es la referencia y se reimporta a mano si cambia.
- Seguimiento: subir las aprobaciones del PR a 1 cuando haya un segundo revisor, y sustituir el entorno efímero por uno persistente cuando exista.
