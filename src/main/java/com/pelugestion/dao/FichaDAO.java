package com.pelugestion.dao;

import com.pelugestion.model.Ficha;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object para Fichas / Visitas.
 */
public class FichaDAO {

    private final DatabaseManager db;

    public FichaDAO() {
        this.db = DatabaseManager.getInstance();
    }

    public int insert(Ficha ficha) {
        String sql = "INSERT INTO fichas (cliente_id, servicio, producto, color_formula, precio, observaciones) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, ficha.getClienteId());
            pstmt.setString(2, ficha.getServicio());
            pstmt.setString(3, ficha.getProducto());
            pstmt.setString(4, ficha.getColorFormula());
            pstmt.setDouble(5, ficha.getPrecio());
            pstmt.setString(6, ficha.getObservaciones());
            pstmt.executeUpdate();

            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    ficha.setId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar ficha", e);
        }
        return -1;
    }

    public void update(Ficha ficha) {
        String sql = "UPDATE fichas SET servicio=?, producto=?, color_formula=?, "
                   + "precio=?, observaciones=? WHERE id=?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ficha.getServicio());
            pstmt.setString(2, ficha.getProducto());
            pstmt.setString(3, ficha.getColorFormula());
            pstmt.setDouble(4, ficha.getPrecio());
            pstmt.setString(5, ficha.getObservaciones());
            pstmt.setInt(6, ficha.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar ficha", e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM fichas WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar ficha", e);
        }
    }

    public List<Ficha> findByClienteId(int clienteId) {
        String sql = "SELECT * FROM fichas WHERE cliente_id = ? ORDER BY fecha DESC";
        List<Ficha> list = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, clienteId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar fichas del cliente", e);
        }
        return list;
    }

    private Ficha mapRow(ResultSet rs) throws SQLException {
        Ficha f = new Ficha();
        f.setId(rs.getInt("id"));
        f.setClienteId(rs.getInt("cliente_id"));
        String fechaStr = rs.getString("fecha");
        if (fechaStr != null) {
            f.setFecha(LocalDateTime.parse(fechaStr, Ficha.FORMATTER));
        }
        f.setServicio(rs.getString("servicio"));
        f.setProducto(rs.getString("producto"));
        f.setColorFormula(rs.getString("color_formula"));
        f.setPrecio(rs.getDouble("precio"));
        f.setObservaciones(rs.getString("observaciones"));
        return f;
    }
}
