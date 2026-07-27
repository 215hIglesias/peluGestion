package com.pelugestion.dao;

import com.pelugestion.model.Cliente;
import com.pelugestion.util.DateUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object para Clientes.
 */
public class ClienteDAO {

    private static final String COLS =
            "nombre, telefono, cumpleanos, direccion, ciudad, provincia, codigo_postal, descripcion";
    private static final String PLACEHOLDERS = "?, ?, ?, ?, ?, ?, ?, ?";

    private final DatabaseManager db;

    public ClienteDAO() {
        this.db = DatabaseManager.getInstance();
    }

    /** Rellena los 8 primeros parametros del statement con los datos del cliente. */
    private void bindCliente(PreparedStatement pstmt, Cliente c) throws SQLException {
        pstmt.setString(1, c.getNombre());
        pstmt.setString(2, c.getTelefono());
        pstmt.setString(3, DateUtil.toIso(c.getCumpleanos()));
        pstmt.setString(4, c.getDireccion());
        pstmt.setString(5, c.getCiudad());
        pstmt.setString(6, c.getProvincia());
        pstmt.setString(7, c.getCodigoPostal());
        pstmt.setString(8, c.getDescripcion());
    }

    public int insert(Cliente cliente) {
        String sql = "INSERT INTO clientes (" + COLS + ") VALUES (" + PLACEHOLDERS + ")";
        int id = -1;
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindCliente(pstmt, cliente);
            pstmt.executeUpdate();

            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) {
                    id = keys.getInt(1);
                    cliente.setId(id);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar cliente", e);
        }
        db.persist();
        return id;
    }

    /** Inserta muchos clientes en una sola transaccion. Devuelve cuantos se insertaron. */
    public int insertAll(List<Cliente> clientes) {
        String sql = "INSERT INTO clientes (" + COLS + ") VALUES (" + PLACEHOLDERS + ")";
        int count = 0;
        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (Cliente c : clientes) {
                    bindCliente(pstmt, c);
                    pstmt.addBatch();
                }
                int[] res = pstmt.executeBatch();
                for (int r : res) {
                    if (r >= 0 || r == Statement.SUCCESS_NO_INFO) count++;
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al importar clientes", e);
        }
        db.persist();
        return count;
    }

    public void update(Cliente cliente) {
        String sql = "UPDATE clientes SET nombre=?, telefono=?, cumpleanos=?, direccion=?, "
                   + "ciudad=?, provincia=?, codigo_postal=?, descripcion=?, activo=? WHERE id=?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            bindCliente(pstmt, cliente);
            pstmt.setInt(9, cliente.isActivo() ? 1 : 0);
            pstmt.setInt(10, cliente.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar cliente", e);
        }
        db.persist();
    }

    public void delete(int id) {
        String sql = "DELETE FROM clientes WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar cliente", e);
        }
        db.persist();
    }

    public Cliente findById(int id) {
        String sql = "SELECT * FROM clientes WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar cliente", e);
        }
        return null;
    }

    public List<Cliente> findAll() {
        String sql = "SELECT * FROM clientes WHERE activo = 1 ORDER BY nombre ASC";
        List<Cliente> list = new ArrayList<>();
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar clientes", e);
        }
        return list;
    }

    public List<Cliente> search(String queryText) {
        if (queryText == null || queryText.isBlank()) {
            return findAll();
        }
        String sql = "SELECT * FROM clientes WHERE activo = 1 AND ("
                   + "nombre LIKE ? OR telefono LIKE ? OR descripcion LIKE ? "
                   + "OR direccion LIKE ? OR ciudad LIKE ? OR provincia LIKE ? OR codigo_postal LIKE ?) "
                   + "ORDER BY nombre ASC";
        String pattern = "%" + queryText.trim() + "%";

        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 1; i <= 7; i++) {
                pstmt.setString(i, pattern);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                List<Cliente> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
                return results;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar clientes", e);
        }
    }

    private Cliente mapRow(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setId(rs.getInt("id"));
        c.setNombre(rs.getString("nombre"));
        c.setTelefono(rs.getString("telefono"));
        c.setCumpleanos(DateUtil.parseFlexible(rs.getString("cumpleanos")));
        c.setDireccion(rs.getString("direccion"));
        c.setCiudad(rs.getString("ciudad"));
        c.setProvincia(rs.getString("provincia"));
        c.setCodigoPostal(rs.getString("codigo_postal"));
        c.setDescripcion(rs.getString("descripcion"));
        String fechaStr = rs.getString("fecha_alta");
        if (fechaStr != null) {
            c.setFechaAlta(LocalDateTime.parse(fechaStr, Cliente.FORMATTER));
        }
        c.setActivo(rs.getInt("activo") == 1);
        return c;
    }
}
