-- Catálogo de roles y permisos (business-rules.md §4.1). Sin usuarios ni contraseñas:
-- el administrador inicial se crea al arrancar desde variables de entorno (BR-USR-007).

INSERT INTO roles (name, description) VALUES
    ('ADMIN',    'Administrador: todas las operaciones'),
    ('HR',       'Recursos Humanos: gestión de empleados y contratos'),
    ('EMPLOYEE', 'Empleado: consulta de sus propios contratos');

INSERT INTO permissions (code, description) VALUES
    ('employee:create',    'Registrar empleados'),
    ('employee:update',    'Actualizar empleados'),
    ('contract:create',    'Crear contratos'),
    ('contract:update',    'Modificar contratos en borrador'),
    ('contract:sign',      'Firmar contratos'),
    ('contract:renew',     'Renovar contratos'),
    ('contract:terminate', 'Terminar contratos'),
    ('contract:cancel',    'Cancelar contratos'),
    ('contract:read:all',  'Consultar cualquier contrato'),
    ('contract:read:own',  'Consultar los contratos propios'),
    ('user:manage',        'Gestionar usuarios y sus roles'),
    ('system:monitor',     'Consultar endpoints de actuator distintos de health');

INSERT INTO role_permissions (role_name, permission_code)
SELECT 'ADMIN', code FROM permissions WHERE code <> 'contract:read:own';

INSERT INTO role_permissions (role_name, permission_code) VALUES
    ('HR', 'employee:create'),
    ('HR', 'employee:update'),
    ('HR', 'contract:create'),
    ('HR', 'contract:update'),
    ('HR', 'contract:sign'),
    ('HR', 'contract:renew'),
    ('HR', 'contract:terminate'),
    ('HR', 'contract:cancel'),
    ('HR', 'contract:read:all'),
    ('EMPLOYEE', 'contract:read:own');
