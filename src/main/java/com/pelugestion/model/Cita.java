package com.pelugestion.model;

import java.time.LocalDate;

public class Cita {

    public static final String[] SERVICIOS = {
            "Corte", "Tinte", "Mechas", "Peinado", "Tratamiento",
            "Keratina", "Alisado", "Recogido", "Lavado y secado",
            "Manicura", "Otro"
    };

    public static final String[] TIEMPOS_ESTIMADOS = {
            "15 min", "30 min", "45 min", "1 h", "1 h 15 min",
            "1 h 30 min", "1 h 45 min", "2 h"
    };

    public static final String[] HORAS = {
            "09:00", "09:15", "09:30", "09:45",
            "10:00", "10:15", "10:30", "10:45",
            "11:00", "11:15", "11:30", "11:45",
            "12:00", "12:15", "12:30", "12:45",
            "13:00", "13:15", "13:30", "13:45",
            "14:00", "14:15", "14:30", "14:45",
            "15:00", "15:15", "15:30", "15:45",
            "16:00", "16:15", "16:30", "16:45",
            "17:00", "17:15", "17:30", "17:45",
            "18:00", "18:15", "18:30", "18:45",
            "19:00", "19:15", "19:30", "19:45",
            "20:00"
    };

    private int id;
    private int clienteId;
    private LocalDate fecha;
    private String hora;
    private String servicio;
    private String notas;
    private String clienteNombre;
    private String tiempoEstimado;

    public Cita() {
        this.fecha = LocalDate.now();
        this.hora = "10:00";
        this.servicio = "";
        this.notas = "";
        this.tiempoEstimado = "";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }

    public String getServicio() { return servicio; }
    public void setServicio(String servicio) { this.servicio = servicio != null ? servicio : ""; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas != null ? notas : ""; }

    public String getTiempoEstimado() { return tiempoEstimado; }
    public void setTiempoEstimado(String tiempoEstimado) { this.tiempoEstimado = tiempoEstimado != null ? tiempoEstimado : ""; }

    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }

    @Override
    public String toString() {
        return hora + " - " + (clienteNombre != null ? clienteNombre : "Sin cliente") + " (" + servicio + ")";
    }
}
