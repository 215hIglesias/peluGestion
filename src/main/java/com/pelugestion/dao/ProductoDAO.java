package com.pelugestion.dao;

import com.pelugestion.model.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    public List<Producto> findAll() {
        List<Producto> list = new ArrayList<>();
        String sql = "SELECT id, nombre, precio_compra, precio_venta, cantidad FROM productos ORDER BY nombre ASC";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Producto p = new Producto();
                p.setId(rs.getInt("id"));
                p.setNombre(rs.getString("nombre"));
                p.setPrecioCompra(rs.getDouble("precio_compra"));
                p.setPrecioVenta(rs.getDouble("precio_venta"));
                p.setCantidad(rs.getInt("cantidad"));
                list.add(p);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener productos: " + e.getMessage());
        }
        return list;
    }

    public Producto findByName(String nombre) {
        String sql = "SELECT id, nombre, precio_compra, precio_venta, cantidad FROM productos WHERE nombre = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nombre);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Producto p = new Producto();
                    p.setId(rs.getInt("id"));
                    p.setNombre(rs.getString("nombre"));
                    p.setPrecioCompra(rs.getDouble("precio_compra"));
                    p.setPrecioVenta(rs.getDouble("precio_venta"));
                    p.setCantidad(rs.getInt("cantidad"));
                    return p;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar producto: " + e.getMessage());
        }
        return null;
    }

    public void insert(Producto p) {
        String sql = "INSERT INTO productos (nombre, precio_compra, precio_venta, cantidad) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, p.getNombre());
            pstmt.setDouble(2, p.getPrecioCompra());
            pstmt.setDouble(3, p.getPrecioVenta());
            pstmt.setInt(4, p.getCantidad());
            pstmt.executeUpdate();
            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) {
                    p.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar producto: " + e.getMessage());
        }
        DatabaseManager.getInstance().persist();
    }

    public void update(Producto p) {
        String sql = "UPDATE productos SET nombre = ?, precio_compra = ?, precio_venta = ?, cantidad = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, p.getNombre());
            pstmt.setDouble(2, p.getPrecioCompra());
            pstmt.setDouble(3, p.getPrecioVenta());
            pstmt.setInt(4, p.getCantidad());
            pstmt.setInt(5, p.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al actualizar producto: " + e.getMessage());
        }
        DatabaseManager.getInstance().persist();
    }

    public void delete(int id) {
        String sql = "DELETE FROM productos WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al eliminar producto: " + e.getMessage());
        }
        DatabaseManager.getInstance().persist();
    }
}
