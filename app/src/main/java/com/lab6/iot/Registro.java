package com.lab6.iot;

import com.google.firebase.firestore.DocumentId;

/** Modelo guardado en Cloud Firestore: usuarios/{uid}/registros/{id} */
public class Registro {
    public static final String[] TIPOS = {"Temperatura", "Humedad", "Luz"};

    @DocumentId
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
