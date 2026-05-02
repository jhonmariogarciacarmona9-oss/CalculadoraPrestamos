-- Usuario administrador por defecto para desarrollo
-- Contraseña: admin (texto plano, compatible con NoOpPasswordEncoder)
INSERT INTO users (username, password, enabled) VALUES ('admin', 'admin', true);
INSERT INTO authorities (username, authority) VALUES ('admin', 'ROLE_USER');
INSERT INTO authorities (username, authority) VALUES ('admin', 'ROLE_ADMIN');
