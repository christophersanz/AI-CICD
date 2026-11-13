# ADR-0016: Despliegue solo con Docker, sin Render

- **Estado:** Aceptado
- **Fecha:** 2026-11-13
- **Sustituye a:** [ADR-0013](0013-despliegue-en-render.md) (despliegue en Render). Deja sin efecto la parte de despliegue con aprobación de [ADR-0012](0012-github-flow-y-despliegue-con-aprobacion.md); GitHub Flow y los checks obligatorios siguen vigentes.

## Contexto

ADR-0013 añadió un job `Despliegue (production)` que desplegaba la imagen en Render tras aprobar el environment `production`. Ese despliegue **nunca llegó a funcionar**: en GitHub no se configuraron `RENDER_API_KEY`, `RENDER_SERVICE_ID` ni `RENDER_URL`, y el job terminó siempre en *skipped*. Mientras tanto, el despliegue real del proyecto pasó a ser el de Jenkins (ADR-0014): etapa **Despliegue local (Docker)**, con PostgreSQL persistente, prueba de humo y vuelta a la imagen anterior si falla. Mantener un despliegue en la nube sin usar costaba documentación, un script y un job que confundían sobre cómo se despliega de verdad.

## Decisión

- El despliegue es **solo con Docker**:
  - el Jenkins local despliega cada build de `master` en `http://localhost:8090` (ADR-0014), después del Quality Gate (ADR-0015);
  - cualquiera puede ejecutar la imagen publicada con `docker run` (guía de CI/CD §8).
- Se eliminan `render.yaml`, `.github/scripts/render-deploy.sh` y el job `deploy` del workflow.
- GitHub Actions queda como CI: build, pruebas, cobertura, imagen publicada en GHCR y Docker Hub, y verificación de la imagen en un entorno efímero.

## Alternativas consideradas

1. **Configurar Render y mantener el job.** Pro: una URL pública con aprobación humana. Contra: el plan gratuito duerme el servicio y borra la base a los 30 días, y requiere credenciales que el proyecto no necesita para su propósito.
2. **Desplegar desde GitHub Actions a un servidor propio con Docker** (SSH y `docker compose`). Pro: entorno persistente fuera de la máquina local. Contra: hace falta un servidor y gestionar sus secretos. Este sería el punto de partida si algún día se necesita.

## Consecuencias

- No hay despliegue en la nube ni environment `production` en uso. El paso humano es el *Squash and merge* (ADR-0012).
- La prueba de humo (`.github/scripts/smoke-test.sh`) la siguen usando el job de verificación y las etapas de Jenkins.
- Los informes de revisión ya cerrados (`docs/reviews/`) conservan las menciones a Render de su momento.
