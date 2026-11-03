package com.lab6.iot;

/** Modelo guardado en Realtime Database: registros/{uid}/{id} */
public class Registro {
    public static final String[] TIPOS = {"Temperatura", "Humedad", "Luz"};

    public String id;
    public String tipo;
    public double valor;
    public long fecha;

    // Constructor vacío requerido por Firebase
    public Registro() {
    }

    public Registro(String id, String tipo, double valor, long fecha) {
        this.id = id;
        this.tipo = tipo;
        this.valor = valor;
        this.fecha = fecha;
    }
}
