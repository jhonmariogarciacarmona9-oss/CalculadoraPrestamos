-- Usuario administrador por defecto
-- Contraseña: admin (hash bcrypt de "admin")
-- INSERT ... ON CONFLICT DO NOTHING evita errores de clave duplicada si los datos ya existen
INSERT INTO users (username, password, enabled)
    VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', true)
    ON CONFLICT (username) DO NOTHING;

INSERT INTO authorities (username, authority)
    VALUES ('admin', 'ROLE_USER')
    ON CONFLICT (username, authority) DO NOTHING;

INSERT INTO authorities (username, authority)
    VALUES ('admin', 'ROLE_ADMIN')
    ON CONFLICT (username, authority) DO NOTHING;
