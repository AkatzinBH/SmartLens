package com.example.smartlens;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private Context context;
    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context=this;
        initBluetooth();
    }

    private void initBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            //SI SALE JEFEEEEEE
            Toast.makeText(context, "El dispositivo no soporta conexion a Bluetooth", Toast.LENGTH_SHORT).show();
        }
    }

    private void enableBluetooth() {
        if (bluetoothAdapter.isEnabled())
        {Toast.makeText(context, "El Bluetooth está Encendido", Toast.LENGTH_SHORT).show();
        } else {
            bluetoothAdapter.enable();
        }
    }

    //Método para el botón Reloj
    public void MensajeReloj(View view){
        Toast.makeText(this, "Esto es un mensaje para el Reloj", Toast.LENGTH_SHORT) .show();
    }

    //Método para el botón Calendario
    public void MensajeCalendario(View view){
        Toast.makeText(this, "Esto es un mensaje para el Calendario", Toast.LENGTH_SHORT) .show();
    }

    //Método para el botón Clima
    public void MensajeClima(View view){
        Toast.makeText(this, "Esto es un mensaje para el Clima", Toast.LENGTH_SHORT) .show();
    }

    //Método para el botón Notificaciones
    public void MensajeNotificaciones(View view){
        Toast.makeText(this, "Esto es un mensaje para Notificaciones", Toast.LENGTH_SHORT) .show();
    }

    //Método para el botón BT
    public boolean MensajeBT(View view){
        //Toast.makeText(this, "Esto es un mensaje para el BT", Toast.LENGTH_SHORT) .show();
        enableBluetooth();
        return true;
    }

    //Método para el botón Información
    public void MensajeInfo(View view){
        Toast.makeText(this, "Esto es un mensaje para Información", Toast.LENGTH_SHORT) .show();
    }

}