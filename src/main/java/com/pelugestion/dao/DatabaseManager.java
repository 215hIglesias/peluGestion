package com.pelugestion.dao;

import com.pelugestion.util.CryptoStore;

import javax.crypto.AEADBadTagException;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Gestor de la base de datos SQLite CIFRADA — Singleton.
 *
 * En disco los datos viven cifrados (AES-GCM) en "pelugestion.db.enc", que se
 * guarda JUNTO a la aplicacion (modo portatil: copiando la carpeta a un pen se
 * llevan los datos). Al arrancar, con la contrasena se descifra a una copia
 * temporal sobre la que trabaja SQLite; tras cada cambio se vuelve a cifrar.
 */
public class DatabaseManager {

    private static final String STORE_NAME = "pelugestion.db.enc";
    private static final int MAX_BACKUPS = 10;

    private static DatabaseManager instance;

    private final Path storePath;   // fichero cifrado en disco
    private final Path workingPath; // copia temporal descifrada (SQLite trabaja aqui)
    private final SecretKey key;
    private final byte[] salt;

    /** Se lanza cuando la contrasena no descifra la base de datos. */
    public static class BadPasswordException extends Exception {
        public BadPasswordException() { super("Contrasena incorrecta"); }
    }

    private DatabaseManager(Path storePath, char[] password, boolean firstRun)
            throws BadPasswordException, IOException {
        this.storePath = storePath;
        this.workingPath = Files.createTempFile("pelugestion_", ".db");
        this.workingPath.toFile().deleteOnExit();

        if (firstRun) {
            this.salt = CryptoStore.randomSalt();
            this.key = CryptoStore.deriveKey(password, salt);
            // workingPath es un fichero vacio -> SQLite lo tratara como BD nueva
            initializeDatabase();
            persist(); // crea el fichero cifrado
        } else {
            byte[] fileBytes = Files.readAllBytes(storePath);
            this.salt = CryptoStore.extractSalt(fileBytes);
            this.key = CryptoStore.deriveKey(password, salt);
            byte[] plain;
            try {
                plain = CryptoStore.decrypt(fileBytes, key);
            } catch (AEADBadTagException e) {
                Files.deleteIfExists(workingPath);
                throw new BadPasswordException();
            }
            Files.write(workingPath, plain);
            initializeDatabase(); // aplica migraciones si faltan columnas
            persist();            // guarda posibles migraciones
        }

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    /** Inicializa el singleton descifrando (o creando) la base de datos. */
    public static synchronized void initialize(Path storePath, char[] password, boolean firstRun)
            throws BadPasswordException, IOException {
        instance = new DatabaseManager(storePath, password, firstRun);
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("La base de datos no ha sido inicializada");
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + workingPath);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
            // journal DELETE: al cerrar la conexion el fichero .db queda completo,
            // sin ficheros -wal/-shm, listo para volver a cifrarse.
            stmt.execute("PRAGMA journal_mode = DELETE");
        }
        return conn;
    }

    /** Re-cifra la base de datos de trabajo al fichero en disco (atomico). */
    public synchronized void persist() {
        try {
            byte[] plain = Files.readAllBytes(workingPath);
            byte[] enc = CryptoStore.encrypt(plain, key, salt);
            Path tmp = storePath.resolveSibling(storePath.getFileName() + ".tmp");
            Files.write(tmp, enc);
            try {
                Files.move(tmp, storePath,
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicFails) {
                Files.move(tmp, storePath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            System.err.println("Error al guardar (cifrar) la base de datos: " + e.getMessage());
        }
    }

    public Path getStorePath() {
        return storePath;
    }

    // --- Config key-value ---

    public String getConfig(String key) {
        String sql = "SELECT value FROM config WHERE key = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, key);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("value");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al leer config '" + key + "': " + e.getMessage());
        }
        return null;
    }

    public void setConfig(String key, String value) {
        String sql = "INSERT OR REPLACE INTO config (key, value) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, key);
            pstmt.setString(2, value);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al guardar config '" + key + "': " + e.getMessage());
        }
        persist();
    }

    // --- Backups (del fichero cifrado) ---

    public void createBackup() {
        try {
            if (!Files.exists(storePath)) return;

            Path backupDir = storePath.getParent().resolve("backups");
            Files.createDirectories(backupDir);

            String timestamp = LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path backup = backupDir.resolve("pelugestion_" + timestamp + ".db.enc");

            Files.copy(storePath, backup, StandardCopyOption.REPLACE_EXISTING);
            cleanOldBackups(backupDir);
        } catch (IOException e) {
            System.err.println("Error al crear backup: " + e.getMessage());
        }
    }

    // --- Ruta del fichero cifrado (modo portatil: junto a la app) ---

    /**
     * Ubica el fichero cifrado junto al ejecutable (modo portatil). Si esa
     * carpeta no se puede escribir, cae a la carpeta de datos del usuario.
     */
    public static Path resolveStorePath() {
        Path appDir = appDirectory();
        if (appDir != null && isWritable(appDir)) {
            return appDir.resolve(STORE_NAME);
        }
        // Fallback: carpeta de datos del usuario
        String os = System.getProperty("os.name").toLowerCase();
        Path dbDir;
        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            dbDir = (appData != null)
                    ? Path.of(appData, "PeluGestion")
                    : Path.of(System.getProperty("user.home"), "PeluGestion");
        } else {
            dbDir = Path.of(System.getProperty("user.home"), ".pelugestion");
        }
        try {
            Files.createDirectories(dbDir);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el directorio de datos: " + dbDir, e);
        }
        return dbDir.resolve(STORE_NAME);
    }

    /** Carpeta donde reside el .jar en ejecucion (o null si no se puede determinar). */
    private static Path appDirectory() {
        try {
            Path location = Path.of(DatabaseManager.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            // Si es un .jar, la carpeta es su padre; si son 'classes' (dev), esa carpeta
            return Files.isDirectory(location) ? location : location.getParent();
        } catch (URISyntaxException | RuntimeException e) {
            return null;
        }
    }

    private static boolean isWritable(Path dir) {
        try {
            Path probe = Files.createTempFile(dir, ".pgwrite", ".tmp");
            Files.deleteIfExists(probe);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // --- Internals ---

    private void initializeDatabase() {
        try (Connection conn = getConnection()) {
            String schema = loadResource("/db/schema.sql");
            for (String statement : schema.split(";")) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute(trimmed);
                    }
                }
            }
            applyMigrations(conn);
        } catch (SQLException e) {
            throw new RuntimeException("Error al inicializar la base de datos", e);
        }
    }

    private void applyMigrations(Connection conn) throws SQLException {
        int currentVersion = getCurrentSchemaVersion(conn);

        if (currentVersion < 1) {
            setSchemaVersion(conn, 1);
        }

        // v2: campos ampliados del cliente. ensureColumn es idempotente.
        ensureColumn(conn, "clientes", "cumpleanos",    "TEXT DEFAULT ''");
        ensureColumn(conn, "clientes", "direccion",     "TEXT DEFAULT ''");
        ensureColumn(conn, "clientes", "ciudad",        "TEXT DEFAULT ''");
        ensureColumn(conn, "clientes", "provincia",     "TEXT DEFAULT ''");
        ensureColumn(conn, "clientes", "codigo_postal", "TEXT DEFAULT ''");
        if (currentVersion < 2) {
            setSchemaVersion(conn, 2);
        }

        if (currentVersion < 3) {
            setSchemaVersion(conn, 3);
        }

        ensureColumn(conn, "productos", "cantidad", "INTEGER DEFAULT 0");
        if (currentVersion < 4) {
            setSchemaVersion(conn, 4);
        }

        if (currentVersion < 5) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS citas ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "cliente_id INTEGER NOT NULL, "
                    + "fecha TEXT NOT NULL, "
                    + "hora TEXT NOT NULL, "
                    + "servicio TEXT NOT NULL, "
                    + "notas TEXT DEFAULT '', "
                    + "FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE CASCADE)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_citas_fecha ON citas(fecha)");
            }
            setSchemaVersion(conn, 5);
        }

        ensureColumn(conn, "citas", "tiempo_estimado", "TEXT DEFAULT ''");
        if (currentVersion < 6) {
            setSchemaVersion(conn, 6);
        }
    }

    private void ensureColumn(Connection conn, String table, String column, String definition)
            throws SQLException {
        boolean exists = false;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (rs.next()) {
                if (column.equalsIgnoreCase(rs.getString("name"))) {
                    exists = true;
                    break;
                }
            }
        }
        if (!exists) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
            }
        }
    }

    private int getCurrentSchemaVersion(Connection conn) {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT MAX(version) FROM schema_version")) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            // La tabla puede no existir aun
        }
        return 0;
    }

    private void setSchemaVersion(Connection conn, int version) throws SQLException {
        String sql = "INSERT OR REPLACE INTO schema_version (version) VALUES (?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, version);
            pstmt.executeUpdate();
        }
    }

    private void cleanOldBackups(Path backupDir) throws IOException {
        try (var files = Files.list(backupDir)) {
            var backups = files
                    .filter(p -> p.getFileName().toString().endsWith(".db.enc"))
                    .sorted((a, b) -> {
                        try {
                            return Files.getLastModifiedTime(b)
                                    .compareTo(Files.getLastModifiedTime(a));
                        } catch (IOException e) {
                            return 0;
                        }
                    })
                    .toList();
            for (int i = MAX_BACKUPS; i < backups.size(); i++) {
                Files.deleteIfExists(backups.get(i));
            }
        }
    }

    private String loadResource(String path) {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) {
                throw new RuntimeException("Recurso no encontrado: " + path);
            }
            return new String(is.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Error al leer recurso: " + path, e);
        }
    }

    /** Al cerrar: re-cifra y borra la copia temporal descifrada. */
    private void shutdown() {
        persist();
        try {
            Files.deleteIfExists(workingPath);
            Files.deleteIfExists(Path.of(workingPath + "-journal"));
            Files.deleteIfExists(Path.of(workingPath + "-wal"));
            Files.deleteIfExists(Path.of(workingPath + "-shm"));
        } catch (IOException ignored) {
            // best effort
        }
    }
}
