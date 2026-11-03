package com.lab6.iot;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChartsActivity extends AppCompatActivity {

    private static final String TODOS = "Todos";

    private final List<Registro> datos = new ArrayList<>();
    private LineChart lineChart;
    private BarChart barChart;
    private Spinner spFiltro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts);

        lineChart = findViewById(R.id.lineChart);
        barChart = findViewById(R.id.barChart);
        spFiltro = findViewById(R.id.spFiltro);

        List<String> opciones = new ArrayList<>();
        opciones.add(TODOS);
        Collections.addAll(opciones, Registro.TIPOS);
        spFiltro.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, opciones));
        spFiltro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                dibujar();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference("registros").child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        datos.clear();
                        for (DataSnapshot c : snapshot.getChildren()) {
                            Registro r = c.getValue(Registro.class);
                            if (r != null) datos.add(r);
                        }
                        Collections.sort(datos, (a, b) -> Long.compare(a.fecha, b.fecha));
                        dibujar();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void dibujar() {
        String filtro = (String) spFiltro.getSelectedItem();
        if (filtro == null) filtro = TODOS;

        // ---- Gráfico 1: líneas, valores en el tiempo (filtrados) ----
        List<Entry> puntos = new ArrayList<>();
        int i = 0;
        for (Registro r : datos) {
            if (filtro.equals(TODOS) || filtro.equals(r.tipo)) {
                puntos.add(new Entry(i++, (float) r.valor));
            }
        }
        LineDataSet lineSet = new LineDataSet(puntos, filtro);
        lineSet.setLineWidth(2f);
        lineSet.setCircleRadius(4f);
        lineChart.setData(new LineData(lineSet));
        lineChart.getDescription().setEnabled(false);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.invalidate();

        // ---- Gráfico 2: barras, promedio por tipo (filtrado) ----
        List<BarEntry> barras = new ArrayList<>();
        List<String> etiquetas = new ArrayList<>();
        int idx = 0;
        for (String tipo : Registro.TIPOS) {
            if (!filtro.equals(TODOS) && !filtro.equals(tipo)) continue;
            double suma = 0;
            int n = 0;
            for (Registro r : datos) {
                if (tipo.equals(r.tipo)) {
                    suma += r.valor;
                    n++;
                }
            }
            barras.add(new BarEntry(idx++, n == 0 ? 0 : (float) (suma / n)));
            etiquetas.add(tipo);
        }
        BarDataSet barSet = new BarDataSet(barras, "Promedio");
        barSet.setColors(new int[]{0xFF1565C0, 0xFF2E7D32, 0xFFF9A825});
        barChart.setData(new BarData(barSet));
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(etiquetas));
        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getDescription().setEnabled(false);
        barChart.invalidate();
    }
}
