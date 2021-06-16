package com.example.smartlens;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class InfoAdaptador extends RecyclerView.Adapter<InfoAdaptador.InfoAdaptadorViewHolder> implements  View.OnClickListener {

        ArrayList<Info> Infos;
        private onOpcionListener mListener;
        private  View.OnClickListener listener;

        public InfoAdaptador(ArrayList<Info> opciones, onOpcionListener listener)
        {
            this.Infos = opciones;
            this.mListener = listener;
        }

        @NonNull
        @Override
        public InfoAdaptador.InfoAdaptadorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.opciones_cardview,parent,false);

            return new InfoAdaptador.InfoAdaptadorViewHolder(v,mListener);
        }

        @Override
        public void onBindViewHolder(@NonNull InfoAdaptadorViewHolder holder, int position) {
            Info opcion = Infos.get(position);

            holder.imgOpcion.setImageResource(opcion.getImagen());

        }

        @Override
        public int getItemCount() { //cantiad de elementos que contiene mi lista
            return Infos.size();
        }

        @Override
        public void onClick(View v) {

        }

        public static class InfoAdaptadorViewHolder extends RecyclerView.ViewHolder implements onOpcionListener, View.OnClickListener {
            private ImageView imgOpcion;

            onOpcionListener listener;

            public InfoAdaptadorViewHolder (View itemView, onOpcionListener listener)
            {
                super(itemView);
                imgOpcion = (ImageView) itemView.findViewById(R.id.imgInfo);

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
        public  void  setOnClickListener(View.OnClickListener listener)
        {
            this.listener = listener;
        }

    }


