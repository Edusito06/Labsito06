package com.lab6.iot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class RegistroAdapter extends RecyclerView.Adapter<RegistroAdapter.VH> {

    public interface Listener {
        void onEditar(Registro r);

        void onEliminar(Registro r);
    }

    private final List<Registro> items;
    private final Listener listener;
    private final SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public RegistroAdapter(List<Registro> items, Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_registro, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Registro r = items.get(position);
        h.tvTitulo.setText(r.tipo + ": " + r.valor);
        h.tvFecha.setText(fmt.format(r.fecha));
        h.btnEditar.setOnClickListener(v -> listener.onEditar(r));
        h.btnEliminar.setOnClickListener(v -> listener.onEliminar(r));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvFecha;
        View btnEditar, btnEliminar;

        VH(View v) {
            super(v);
            tvTitulo = v.findViewById(R.id.tvTitulo);
            tvFecha = v.findViewById(R.id.tvFecha);
            btnEditar = v.findViewById(R.id.btnEditar);
            btnEliminar = v.findViewById(R.id.btnEliminar);
        }
    }
}
