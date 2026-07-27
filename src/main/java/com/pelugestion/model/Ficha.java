package com.pelugestion.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Modelo de Ficha / Visita de peluqueria.
 */
public class Ficha {

    public static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static final String[] SERVICIOS_PREDEFINIDOS = {
            "Corte", "Tinte", "Mechas", "Peinado", "Tratamiento",
            "Keratina", "Alisado", "Recogido", "Lavado y secado",
            "Manicura", "Otro"
    };

    private int id;
    private int clienteId;
    private LocalDateTime fecha;
    private String servicio;
    private String producto;
    private String colorFormula;
    private double precio;
    private String observaciones;

    public Ficha() {
        this.fecha = LocalDateTime.now();
        this.producto = "";
        this.colorFormula = "";
        this.observaciones = "";
    }

    public Ficha(int clienteId, String servicio, double precio) {
        this();
        this.clienteId = clienteId;
        this.servicio = servicio;
        this.precio = precio;
    }

    // --- Getters y Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getServicio() { return servicio; }
    public void setServicio(String servicio) { this.servicio = servicio; }

    public String getProducto() { return producto; }
    public void setProducto(String producto) { this.producto = producto != null ? producto : ""; }

    public String getColorFormula() { return colorFormula; }
    public void setColorFormula(String colorFormula) { this.colorFormula = colorFormula != null ? colorFormula : ""; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones != null ? observaciones : ""; }

    @Override
    public String toString() {
        return fecha.format(FORMATTER) + " - " + servicio;
    }
}
