-- ============================================================
-- PeluGestion — Schema v3
-- Base de datos SQLite para gestion de fichas de peluqueria
-- ============================================================

-- Tabla de configuracion (contrasena, preferencias, etc.)
CREATE TABLE IF NOT EXISTS config (
    key   TEXT PRIMARY KEY,
    value TEXT NOT NULL
);

-- Tabla de clientes
CREATE TABLE IF NOT EXISTS clientes (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre         TEXT    NOT NULL,               -- nombre completo del cliente
    telefono       TEXT    DEFAULT '',
    cumpleanos     TEXT    DEFAULT '',             -- fecha ISO yyyy-MM-dd (vacio = sin fecha)
    direccion      TEXT    DEFAULT '',
    ciudad         TEXT    DEFAULT '',
    provincia      TEXT    DEFAULT '',
    codigo_postal  TEXT    DEFAULT '',
    descripcion    TEXT    DEFAULT '',             -- "Detalles y extras" en la interfaz
    fecha_alta     TEXT    NOT NULL DEFAULT (datetime('now','localtime')),
    activo         INTEGER NOT NULL DEFAULT 1
);

-- Tabla de fichas / visitas
CREATE TABLE IF NOT EXISTS fichas (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    cliente_id      INTEGER NOT NULL,
    fecha           TEXT    NOT NULL DEFAULT (datetime('now','localtime')),
    servicio        TEXT    NOT NULL,
    producto        TEXT    DEFAULT '',
    color_formula   TEXT    DEFAULT '',
    precio          REAL    DEFAULT 0.0,
    observaciones   TEXT    DEFAULT '',
    FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE CASCADE
);

-- Control de versiones del schema (para migraciones futuras)
CREATE TABLE IF NOT EXISTS schema_version (
    version    INTEGER PRIMARY KEY,
    applied_at TEXT    NOT NULL DEFAULT (datetime('now','localtime'))
);

-- Tabla de productos (inventario / ventas)
CREATE TABLE IF NOT EXISTS productos (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre        TEXT    NOT NULL,
    precio_compra REAL    DEFAULT 0.0,
    precio_venta  REAL    DEFAULT 0.0,
    cantidad      INTEGER DEFAULT 0
);

-- Tabla de citas
CREATE TABLE IF NOT EXISTS citas (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    cliente_id  INTEGER NOT NULL,
    fecha       TEXT    NOT NULL,
    hora        TEXT    NOT NULL,
    servicio    TEXT    NOT NULL,
    notas       TEXT    DEFAULT '',
    FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE CASCADE
);

-- Indices para busquedas rapidas
CREATE INDEX IF NOT EXISTS idx_clientes_nombre    ON clientes(nombre);
CREATE INDEX IF NOT EXISTS idx_clientes_telefono  ON clientes(telefono);
CREATE INDEX IF NOT EXISTS idx_fichas_cliente_id  ON fichas(cliente_id);
CREATE INDEX IF NOT EXISTS idx_fichas_fecha       ON fichas(fecha);
CREATE INDEX IF NOT EXISTS idx_citas_fecha        ON citas(fecha);
