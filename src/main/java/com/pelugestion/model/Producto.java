package com.pelugestion.model;

public class Producto {

    private int id;
    private String nombre;
    private double precioCompra;
    private double precioVenta;
    private int cantidad;

    public Producto() {
        this.nombre = "";
        this.precioCompra = 0.0;
        this.precioVenta = 0.0;
        this.cantidad = 0;
    }

    public Producto(String nombre) {
        this();
        this.nombre = nombre != null ? nombre : "";
    }

    public Producto(String nombre, double precioCompra, double precioVenta) {
        this(nombre);
        this.precioCompra = precioCompra;
        this.precioVenta = precioVenta;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre != null ? nombre : ""; }

    public double getPrecioCompra() { return precioCompra; }
    public void setPrecioCompra(double precioCompra) { this.precioCompra = precioCompra; }

    public double getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(double precioVenta) { this.precioVenta = precioVenta; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    @Override
    public String toString() {
        return nombre;
    }
}
