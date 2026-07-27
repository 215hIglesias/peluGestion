package com.pelugestion.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Modelo de Cliente de peluqueria.
 * Campos: nombre completo, telefono, cumpleanos, direccion, ciudad,
 * provincia, codigo postal y detalles/extras (descripcion).
 */
public class Cliente {

    public static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static final String[] PROVINCIAS = {
            "Álava", "Albacete", "Alicante", "Almería", "Asturias", "Ávila",
            "Badajoz", "Barcelona", "Burgos", "Cáceres", "Cádiz", "Cantabria",
            "Castellón", "Ceuta", "Ciudad Real", "Córdoba", "A Coruña", "Cuenca",
            "Girona", "Granada", "Guadalajara", "Gipuzkoa", "Huelva", "Huesca",
            "Illes Balears", "Jaén", "León", "Lleida", "Lugo", "Madrid",
            "Málaga", "Melilla", "Murcia", "Navarra", "Ourense", "Palencia",
            "Las Palmas", "Pontevedra", "La Rioja", "Salamanca",
            "Santa Cruz de Tenerife", "Segovia", "Sevilla", "Soria", "Tarragona",
            "Teruel", "Toledo", "Valencia", "Valladolid", "Bizkaia", "Zamora",
            "Zaragoza"
    };

    private int id;
    private String nombre;        // nombre completo
    private String telefono;
    private LocalDate cumpleanos; // puede ser null (sin fecha)
    private String direccion;
    private String ciudad;
    private String provincia;
    private String codigoPostal;
    private String descripcion;   // "Detalles y extras"
    private LocalDateTime fechaAlta;
    private boolean activo;

    public Cliente() {
        this.fechaAlta = LocalDateTime.now();
        this.activo = true;
        this.telefono = "";
        this.direccion = "";
        this.ciudad = "";
        this.provincia = "";
        this.codigoPostal = "";
        this.descripcion = "";
    }

    public Cliente(String nombre, String telefono, String descripcion) {
        this();
        this.nombre = nombre;
        this.telefono = telefono != null ? telefono : "";
        this.descripcion = descripcion != null ? descripcion : "";
    }

    /** Nombre completo del cliente (alias de getNombre para la UI). */
    public String getNombreCompleto() {
        return nombre;
    }

    // --- Getters y Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono != null ? telefono : ""; }

    public LocalDate getCumpleanos() { return cumpleanos; }
    public void setCumpleanos(LocalDate cumpleanos) { this.cumpleanos = cumpleanos; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion != null ? direccion : ""; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad != null ? ciudad : ""; }

    public String getProvincia() { return provincia; }
    public void setProvincia(String provincia) { this.provincia = provincia != null ? provincia : ""; }

    public String getCodigoPostal() { return codigoPostal; }
    public void setCodigoPostal(String codigoPostal) { this.codigoPostal = codigoPostal != null ? codigoPostal : ""; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion != null ? descripcion : ""; }

    public LocalDateTime getFechaAlta() { return fechaAlta; }
    public void setFechaAlta(LocalDateTime fechaAlta) { this.fechaAlta = fechaAlta; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    @Override
    public String toString() {
        return getNombreCompleto();
    }
}
