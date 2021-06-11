package com.example.smartlens;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MenuAdaptador extends RecyclerView.Adapter<MenuAdaptador.MenuAdaptadorViewHolder>{

    ArrayList<Menu> opciones;
    private onOpcionListener mListener;

    public MenuAdaptador(ArrayList<Menu> opciones, onOpcionListener listener)
    {
        this.opciones = opciones;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public MenuAdaptador.MenuAdaptadorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.opciones_cardview,parent,false);

        return new MenuAdaptador.MenuAdaptadorViewHolder(v,mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuAdaptadorViewHolder holder, int position) {
        Menu opcion = opciones.get(position);

        holder.imgOpcion.setImageResource(opcion.getIcono());

    }

    @Override
    public int getItemCount() { //cantiad de elementos que contiene mi lista
        return opciones.size();
    }

    public static class MenuAdaptadorViewHolder extends RecyclerView.ViewHolder implements onOpcionListener, View.OnClickListener {
        private ImageView imgOpcion;

        onOpcionListener listener;

        public MenuAdaptadorViewHolder (View itemView, onOpcionListener listener)
        {
            super(itemView);
            imgOpcion = (ImageView) itemView.findViewById(R.id.imgOpcion);

            this.listener = listener;
            itemView.setOnClickListener(this);

        }

        @Override
        public void onOpcionClick(int position) {
            listener.onOpcionClick(getAdapterPosition());

        }

        @Override
        public void onClick(View view) {
            listener.onOpcionClick(getAdapterPosition());

        }
    }
}
