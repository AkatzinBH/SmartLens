package com.example.smartlens;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.UUID;

public class Textos extends AppCompatActivity {

    // Declarar las variables que se utilizaran
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    //BluetoothSocket mmSocket;
    BluetoothDevice mmDevice = null;
    BluetoothService mmBluetoothService = null;

    //"Manejador" que ayuda a controlar todos los mensajes enviados por el BT
    private Handler mHandler= new Textos.MyHandler(this);

    private class MyHandler extends Handler{
        //crea un contexto para la clase de la cual se recibiran los mensajes
        private WeakReference<Textos> mActivity;
        //Constructo de la clase, obtiene como parametro la actividad
        public MyHandler(Textos activity) {
            mActivity = new WeakReference<Textos>(activity);
            //context=activity.getApplicationContext();
        }
        //SE soobreescribe el metodo para manejar los mensajes, que hacer en caso de que llegue un mensaje nuevo
        @Override
        public void handleMessage(Message msg) {
            byte[] buffer = (byte[]) msg.obj;
            switch (msg.what){
                case 1:
                    String MensajeCPU=new String(buffer,0,msg.arg1);
                    Toast.makeText(Textos.this, "El Bluetooth ya esta encendido", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

    int REQUEST_ENABLE_BL = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_textos);

        EncenderBlue();
        ObtenerDatosRaspBerry();
        // Verifica los persimos si la version de android es mayor a la loolilop y si no los tiene los pide
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                System.out.println("Entro al if");


            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE))
                    Toast.makeText(getBaseContext(), "Necesitamos agregar permisos de lectura", Toast.LENGTH_SHORT).show();
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
            }
        }

        //STring que indica el protocolo de comunicacion y el tipo dispositivo a conectar
        UUID uuid=UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");

        System.out.println("Antes de crear la conexion");

        //SE intenta establecer la conexion con la Raspberry
        mmBluetoothService=new BluetoothService(Textos.this,mmDevice,uuid, mHandler);

        System.out.println("Despues de crear la conexion");


    }

    private void EncenderBlue() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent intentBlEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intentBlEnable, REQUEST_ENABLE_BL);
        } else {
            Toast.makeText(this, "El Bluetooth ya esta encendido", Toast.LENGTH_LONG).show();
        }
    }

    private void ObtenerDatosRaspBerry() {

            //Obtiene la lista de todos los dispositivos vinculados
            Set<BluetoothDevice> DispositivosVinculados = bluetoothAdapter.getBondedDevices();
            //obtener la direccion MAC de la raspberry
            if (DispositivosVinculados.size() > 0) {
                for (BluetoothDevice device : DispositivosVinculados) {
                    if (device.getName().equals("raspberrypi")) {
                        mmDevice = device;
                        System.out.println("Encuentra el dispositivo");

                    }
                }
            }
    }
}