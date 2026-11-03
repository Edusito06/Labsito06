package com.lab6.iot;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements RegistroAdapter.Listener {

    private CollectionReference col;
    private final List<Registro> lista = new ArrayList<>();
    private RegistroAdapter adapter;
    private ListenerRegistration registro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            irAlInicio();
            return;
        }

        String nombre = user.getEmail() != null ? user.getEmail() : user.getDisplayName();
        ((TextView) findViewById(R.id.tvUsuario)).setText("Sesión: " + nombre);

        // usuarios/{uid}/registros/{id}
        col = FirebaseFirestore.getInstance()
                .collection("usuarios").document(user.getUid()).collection("registros");

        RecyclerView rv = findViewById(R.id.rvRegistros);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RegistroAdapter(lista, this);
        rv.setAdapter(adapter);

        findViewById(R.id.btnAgregar).setOnClickListener(v -> mostrarDialogo(null));
        findViewById(R.id.btnGraficos).setOnClickListener(v ->
                startActivity(new Intent(this, ChartsActivity.class)));
        findViewById(R.id.btnSalir).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            LoginManager.getInstance().logOut();
            irAlInicio();
        });
    }

    // READ en tiempo real: el listener se activa en onStart y se libera en onStop (ahorra batería)
    @Override
    protected void onStart() {
        super.onStart();
        if (col == null) return;
        registro = col.addSnapshotListener((snapshot, error) -> {
            if (error != null) {
                Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
            lista.clear();
            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                Registro r = doc.toObject(Registro.class);
                if (r != null) lista.add(r);
            }
            // más recientes primero
            Collections.sort(lista, (a, b) -> Long.compare(b.fecha, a.fecha));
            adapter.notifyDataSetChanged();
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (registro != null) {
            registro.remove();
            registro = null;
        }
    }

    /** CREATE (registro == null) o UPDATE (registro != null) */
    private void mostrarDialogo(Registro registro) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_registro, null);
        Spinner sp = view.findViewById(R.id.spTipo);
        EditText etValor = view.findViewById(R.id.etValor);
        sp.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, Registro.TIPOS));

        if (registro != null) {
            sp.setSelection(Arrays.asList(Registro.TIPOS).indexOf(registro.tipo));
            etValor.setText(String.valueOf(registro.valor));
        }

        new AlertDialog.Builder(this)
                .setTitle(registro == null ? "Nuevo registro" : "Editar registro")
                .setView(view)
                .setPositiveButton("Guardar", (d, w) -> {
                    String txt = etValor.getText().toString().trim();
                    double valor;
                    try {
                        valor = Double.parseDouble(txt);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Valor no válido", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String tipo = (String) sp.getSelectedItem();
                    if (registro == null) {
                        // add(): Firestore genera el ID
                        col.add(new Registro(null, tipo, valor, System.currentTimeMillis()))
                                .addOnFailureListener(this::error);
                    } else {
                        registro.tipo = tipo;
                        registro.valor = valor;
                        col.document(registro.id).set(registro)
                                .addOnFailureListener(this::error);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onEditar(Registro r) {
        mostrarDialogo(r);
    }

    /** DELETE */
    @Override
    public void onEliminar(Registro r) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar")
                .setMessage("¿Eliminar este registro?")
                .setPositiveButton("Eliminar", (d, w) ->
                        col.document(r.id).delete().addOnFailureListener(this::error))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void error(Exception e) {
        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }

    private void irAlInicio() {
        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }
}
