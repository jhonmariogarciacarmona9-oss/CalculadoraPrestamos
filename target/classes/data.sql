-- Usuario administrador por defecto para desarrollo
-- Contraseña: admin (texto plano, compatible con NoOpPasswordEncoder)
-- MERGE INTO evita errores de clave duplicada si los datos ya existen
MERGE INTO users KEY(username) VALUES ('admin', 'admin', true);
MERGE INTO authorities KEY(username, authority) VALUES ('admin', 'ROLE_USER');
MERGE INTO authorities KEY(username, authority) VALUES ('admin', 'ROLE_ADMIN');
