package com.pelugestion.dao;

import com.pelugestion.model.Cita;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CitaDAO {

    private final DatabaseManager db;

    public CitaDAO() {
        this.db = DatabaseManager.getInstance();
    }

    public int insert(Cita cita) {
        String sql = "INSERT INTO citas (cliente_id, fecha, hora, servicio, notas, tiempo_estimado) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, cita.getClienteId());
            pstmt.setString(2, cita.getFecha().toString());
            pstmt.setString(3, cita.getHora());
            pstmt.setString(4, cita.getServicio());
            pstmt.setString(5, cita.getNotas());
            pstmt.setString(6, cita.getTiempoEstimado());
            pstmt.executeUpdate();

            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    cita.setId(id);
                    db.persist();
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar cita", e);
        }
        db.persist();
        return -1;
    }

    public void update(Cita cita) {
        String sql = "UPDATE citas SET cliente_id=?, fecha=?, hora=?, servicio=?, notas=?, tiempo_estimado=? WHERE id=?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cita.getClienteId());
            pstmt.setString(2, cita.getFecha().toString());
            pstmt.setString(3, cita.getHora());
            pstmt.setString(4, cita.getServicio());
            pstmt.setString(5, cita.getNotas());
            pstmt.setString(6, cita.getTiempoEstimado());
            pstmt.setInt(7, cita.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar cita", e);
        }
        db.persist();
    }

    public void delete(int id) {
        String sql = "DELETE FROM citas WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar cita", e);
        }
        db.persist();
    }

    public List<Cita> findByFecha(LocalDate fecha) {
        String sql = "SELECT c.*, cl.nombre AS cliente_nombre FROM citas c "
                   + "JOIN clientes cl ON c.cliente_id = cl.id "
                   + "WHERE c.fecha = ? ORDER BY c.hora ASC";
        List<Cita> list = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fecha.toString());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar citas por fecha", e);
        }
        return list;
    }

    public List<Cita> findByClienteId(int clienteId) {
        String sql = "SELECT c.*, cl.nombre AS cliente_nombre FROM citas c "
                   + "JOIN clientes cl ON c.cliente_id = cl.id "
                   + "WHERE c.cliente_id = ? ORDER BY c.fecha DESC, c.hora ASC";
        List<Cita> list = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, clienteId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar citas del cliente", e);
        }
        return list;
    }

    public List<Cita> findAll() {
        String sql = "SELECT c.*, cl.nombre AS cliente_nombre FROM citas c "
                   + "JOIN clientes cl ON c.cliente_id = cl.id "
                   + "ORDER BY c.fecha DESC, c.hora ASC";
        List<Cita> list = new ArrayList<>();
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar citas", e);
        }
        return list;
    }

    private Cita mapRow(ResultSet rs) throws SQLException {
        Cita c = new Cita();
        c.setId(rs.getInt("id"));
        c.setClienteId(rs.getInt("cliente_id"));
        String fechaStr = rs.getString("fecha");
        if (fechaStr != null && !fechaStr.isEmpty()) {
            c.setFecha(LocalDate.parse(fechaStr));
        }
        c.setHora(rs.getString("hora"));
        c.setServicio(rs.getString("servicio"));
        c.setNotas(rs.getString("notas"));
        c.setTiempoEstimado(rs.getString("tiempo_estimado"));
        c.setClienteNombre(rs.getString("cliente_nombre"));
        return c;
    }
}
