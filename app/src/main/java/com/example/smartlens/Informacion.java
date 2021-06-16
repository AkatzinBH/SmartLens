package com.example.smartlens;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;

public class Informacion extends AppCompatActivity implements onOpcionListener {
    private ArrayList<Info> info;
    private RecyclerView listaOpciones;
    private InfoAdaptador adaptador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informacion);
        info = new ArrayList<>();

        info.add(new Info(R.drawable.informaci_n_smartlens_page_0001));
        info.add(new Info(R.drawable.informaci_n_smartlens_page_0002));
        info.add(new Info(R.drawable.informaci_n_smartlens_page_0003));

        listaOpciones = (RecyclerView) findViewById(R.id.rvInfo);
        GridLayoutManager glm = new GridLayoutManager(this,1);
        listaOpciones.setLayoutManager(glm);
        adaptador = new InfoAdaptador(info,this);


        listaOpciones.setAdapter(adaptador);
    }

    @Override
    public void onOpcionClick(int position) {

    }
}