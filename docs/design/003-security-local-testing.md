# Probar la seguridad en local (fase 1)

Complemento de `003-security-design.md`. Explica cómo arrancar `contract-api` con el perfil `local`, iniciar sesión con el administrador inicial y llamar a los endpoints privados desde PowerShell o Postman.

> En `local`, si no hay claves configuradas (`SECURITY_JWT_PUBLIC_KEY`/`SECURITY_JWT_PRIVATE_KEY`), la API genera un **par RSA efímero** al arrancar: cada reinicio invalida todos los access tokens emitidos. Los refresh tokens (en BD) siguen existiendo, pero el access token hay que pedirlo de nuevo.

## 1. Arranque

```powershell
docker compose up -d                       # PostgreSQL en localhost:5432

# Administrador inicial (BR-USR-007). Solo por variables de entorno; nunca en application*.yml.
# La contraseña debe tener 12–64 caracteres y ≤ 72 bytes (BR-USR-003).
$env:SECURITY_BOOTSTRAP_ADMIN_USERNAME = 'admin'
$env:SECURITY_BOOTSTRAP_ADMIN_PASSWORD = '<elige-una-contraseña-de-12+>'

./mvnw install -DskipTests                 # instala domain y application en ~/.m2
./mvnw spring-boot:run -pl infrastructure  # http://localhost:8081
```

- En el log debe aparecer `Initial administrator created.` (o `An ADMIN user already exists…` en arranques posteriores).
- El administrador se crea **una sola vez**: cambiar luego la variable no cambia su contraseña. Si la olvidas, la única vía en la fase 1 es recrear la base local (`docker compose down -v`, **borra todos los datos locales**).
- Si la contraseña no cumple la política o el username ya lo usa un usuario no `ADMIN`, la aplicación **no arranca** y el error indica el código (`AUTH-008`, `AUTH-007`, `VAL-001`) sin mostrar la contraseña.
- Sin las dos variables, la API arranca igual y registra un aviso, pero no habrá con quién iniciar sesión.

## 2. PowerShell

```powershell
$base = 'http://localhost:8081/api/v1'

# Login (público)
$login = Invoke-RestMethod -Method Post -Uri "$base/auth/login" -ContentType 'application/json' `
    -Body (@{ username = 'admin'; password = $env:SECURITY_BOOTSTRAP_ADMIN_PASSWORD } | ConvertTo-Json)
$headers = @{ Authorization = "Bearer $($login.accessToken)" }

# Endpoint privado: registrar un empleado (201). Se envían bytes UTF-8 por las tildes (PowerShell 5.1).
$body = @{ documentType = 'DNI'; documentNumber = '12345678'; firstNames = 'Ana'
           lastNames = 'Pérez'; email = 'ana.perez@example.com' } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri "$base/employees" -Headers $headers `
    -ContentType 'application/json; charset=utf-8' -Body ([Text.Encoding]::UTF8.GetBytes($body))

# Usuario actual
Invoke-RestMethod -Uri "$base/auth/me" -Headers $headers

# Rotar el refresh token: guarda SIEMPRE el nuevo; reutilizar el anterior revoca la sesión (BR-USR-006)
$login = Invoke-RestMethod -Method Post -Uri "$base/auth/refresh" -ContentType 'application/json' `
    -Body (@{ refreshToken = $login.refreshToken } | ConvertTo-Json)
$headers = @{ Authorization = "Bearer $($login.accessToken)" }

# Logout (204): revoca la familia del refresh token
Invoke-RestMethod -Method Post -Uri "$base/auth/logout" -Headers $headers -ContentType 'application/json' `
    -Body (@{ refreshToken = $login.refreshToken } | ConvertTo-Json)
```

Tras el logout el access token sigue siendo válido hasta que expira (≤ 15 min): es stateless (ver §6 del diseño).

## 3. Postman

### Variables

| Dónde | Variable | Valor |
|---|---|---|
| Colección | `baseUrl` | `http://localhost:8081` |
| Colección | `accessToken`, `accessTokenExpiresAt`, `refreshToken` | vacías (las rellena el script) |
| Entorno *local* | `adminUsername` | `admin` |
| Entorno *local* | `adminPassword` | la de `SECURITY_BOOTSTRAP_ADMIN_PASSWORD`, tipo **secret** y solo en *Current value* (no se sincroniza) |

### Autorización

- Colección → *Authorization* → **Bearer Token** con `{{accessToken}}`.
- Peticiones `POST {{baseUrl}}/api/v1/auth/login` y `/auth/refresh` → *Authorization* → **No Auth**.

### Pre-request script de la colección

Pide un token si no hay uno o está por expirar. Como la clave efímera cambia en cada reinicio, el script de post-respuesta lo descarta ante un `401` de token (`AUTH-003`/`AUTH-004`) y la siguiente petición vuelve a hacer login.

```javascript
const path = pm.request.url.getPath();
if (path.endsWith('/auth/login') || path.endsWith('/auth/refresh')) {
    return;
}
const token = pm.collectionVariables.get('accessToken');
const expiresAt = Number(pm.collectionVariables.get('accessTokenExpiresAt') || 0);
if (token && Date.now() < expiresAt - 30000) {
    return;
}
pm.sendRequest({
    url: pm.variables.get('baseUrl') + '/api/v1/auth/login',
    method: 'POST',
    header: { 'Content-Type': 'application/json' },
    body: {
        mode: 'raw',
        raw: JSON.stringify({
            username: pm.variables.get('adminUsername'),
            password: pm.variables.get('adminPassword')
        })
    }
}, (err, res) => {
    if (err || res.code !== 200) {
        console.error('Login failed:', err || res.code);
        return;
    }
    const body = res.json();
    pm.collectionVariables.set('accessToken', body.accessToken);
    pm.collectionVariables.set('refreshToken', body.refreshToken);
    pm.collectionVariables.set('accessTokenExpiresAt', String(Date.now() + body.expiresIn * 1000));
});
```

### Post-response script de la colección

```javascript
if (pm.response.code === 401) {
    let code = null;
    try { code = pm.response.json().code; } catch (e) { /* sin cuerpo JSON */ }
    if (code === 'AUTH-003' || code === 'AUTH-004') {
        pm.collectionVariables.unset('accessToken');
    }
}
```

## 4. Swagger UI

`http://localhost:8081/swagger-ui.html` → ejecuta `POST /api/v1/auth/login` → copia `accessToken` → botón **Authorize** (`bearerAuth`) → pega el token sin el prefijo `Bearer`.
