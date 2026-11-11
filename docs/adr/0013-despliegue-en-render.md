# ADR-0013: Despliegue persistente en Render

- **Estado:** Aceptado
- **Fecha:** 2026-11-11
- **Amplía:** [ADR-0012](0012-github-flow-y-despliegue-con-aprobacion.md). El entorno efímero deja de ser el despliegue y pasa a ser la verificación previa a la aprobación.

## Contexto

Con ADR-0012, el job `Despliegue (production)` levantaba la imagen en el runner, la probaba y la descartaba: demostraba que la imagen funciona, pero no quedaba ninguna API accesible. ADR-0012 dejó como seguimiento sustituir el entorno efímero por uno persistente. Se busca un destino sin costo y sin servidores que operar, que despliegue una imagen ya publicada (no que la vuelva a construir) y que se pueda automatizar desde GitHub Actions.

## Decisión

1. **Render**, plan gratuito, región Virginia, definido como código en [`render.yaml`](../../render.yaml) (Blueprint):
   - servicio web `contract-api` con `runtime: image`, health check en `/actuator/health` y `autoDeploy: false`;
   - base `contract-db` (PostgreSQL 16) sin acceso externo (`ipAllowList: []`).
2. **Imagen de Docker Hub** (`docker.io/<usuario>/ai-cicd:sha-<corto>`): es pública y Render la descarga sin credenciales. En GHCR el paquete exigiría guardar un token de lectura en Render.
3. **Pipeline en cuatro jobs:** `build` → `image` → `verify` → `deploy`.
   - `verify` (*Verificación de la imagen*) es el entorno efímero de ADR-0012, automático y sin aprobación. Así la aprobación se decide sobre una imagen que ya se sabe que arranca.
   - `deploy` (*Despliegue (production)*) espera la aprobación del environment `production` y llama a la API de Render (`POST /v1/services/{id}/deploys` con `imageUrl`). Después consulta el deploy hasta `live` y repite la prueba de humo contra la URL pública.
   - La prueba de humo es un script común ([`.github/scripts/smoke-test.sh`](../../.github/scripts/smoke-test.sh)). El despliegue está en [`.github/scripts/render-deploy.sh`](../../.github/scripts/render-deploy.sh).
4. **Secretos fuera del repositorio:**
   - en Render: claves JWT y contraseña del administrador (`sync: false`);
   - en el environment `production` de GitHub: `RENDER_API_KEY` y `SMOKE_ADMIN_PASSWORD`;
   - como variables del repositorio: `RENDER_SERVICE_ID` y `RENDER_URL`. Sin `RENDER_SERVICE_ID` (p. ej. en un fork), el job `deploy` se omite.

## Alternativas consideradas

1. **Fly.io.** Pro: no duerme el servicio, más memoria. Contra: exige tarjeta y la base gestionada es de pago.
2. **VM propia por SSH** (on-premise o nube). Pro: control total. Contra: operar el servidor, parches, TLS y una llave SSH en GitHub.
3. **Render Deploy Hook** en lugar de la API. Pro: una sola URL secreta. Contra: no devuelve el id del deploy, así que no se puede esperar a `live` ni detectar un fallo.
4. **Que Render construya desde el `Dockerfile`.** Pro: sin registro. Contra: se desplegaría un artefacto distinto del que probó el CI.

## Consecuencias

- Positivas:
  - la API queda accesible en una URL pública con HTTPS;
  - lo desplegado es exactamente la imagen verificada, identificada por su sha;
  - el pipeline falla si Render no la pone en vivo o si la prueba de humo falla en production;
  - la infraestructura está versionada en `render.yaml`.
- Negativas (plan gratuito):
  - el servicio duerme tras 15 min sin tráfico y la primera petición tarda alrededor de un minuto;
  - 512 MB de memoria, lo que obliga a ajustar la JVM;
  - la base gratuita caduca a los 30 días y hay que recrearla o pasar a un plan de pago;
  - la prueba de humo usa la contraseña real del administrador (secreto del environment) y deja un inicio de sesión en la auditoría.
- Seguimiento:
  - plan de pago o rollback automático (redesplegar el sha anterior) si la API pasa a uso real;
  - un usuario de solo lectura para la prueba de humo cuando existan roles con ese alcance.
