-- Permiso employee:read para ADMIN y HR (business-rules.md §4.1), necesario para exigirlo con
-- @PreAuthorize en la consulta y el listado de empleados (diseño 003, paso 2.1).

INSERT INTO permissions (code, description) VALUES
    ('employee:read', 'Consultar y listar empleados');

INSERT INTO role_permissions (role_name, permission_code) VALUES
    ('ADMIN', 'employee:read'),
    ('HR',    'employee:read');
