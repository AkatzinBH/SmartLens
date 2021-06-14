package com.example.smartlens;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class Figuras extends AppCompatActivity {

    BluetoothService bluetoothService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_figuras);

        bluetoothService = getIntent().getExtras().getParcelable("Disp");

        if (bluetoothService != null)
        {
            Toast.makeText(Figuras.this, "Disp listo", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(Figuras.this, "Disp NO listo", Toast.LENGTH_SHORT).show();
        }
    }
}