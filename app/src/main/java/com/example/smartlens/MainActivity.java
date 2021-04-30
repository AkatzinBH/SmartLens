package com.example.smartlens;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {


    // Declarar las variables que se utilizaran
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    //BluetoothSocket mmSocket;
    BluetoothDevice mmDevice = null;
    BluetoothService mmBluetoothService = null;
    static String VideoSeleccionado;
    private ProgressBar progressBar;

    //"Manejador" que ayuda a controlar todos los mensajes enviados por el BT
    private Handler mHandler= new MyHandler(this);

    private class MyHandler extends Handler{
        //crea un contexto para la clase de la cual se recibiran los mensajes
        private WeakReference<MainActivity> mActivity;
        //Constructo de la clase, obtiene como parametro la actividad
        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
            //context=activity.getApplicationContext();
        }
        //SE soobreescribe el metodo para manejar los mensajes, que hacer en caso de que llegue un mensaje nuevo
        @Override
        public void handleMessage(Message msg) {
            byte[] buffer = (byte[]) msg.obj;
            switch (msg.what){
                case 1:
                    String MensajeCPU=new String(buffer,0,msg.arg1);
                    Toast.makeText(MainActivity.this, "El Bluetooth ya esta encendido", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

    int REQUEST_ENABLE_BL = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Lineas por defecto para inicializar la vista
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Ayuda visual para mostrar que se esta estableciendo la conexion al BT
        progressBar = (ProgressBar) findViewById(R.id.pbConnectBT);
        EncenderBlue();

        //Se crea un boton que tendra como refencia el BT y poder implementar los metodos que siguen
        ImageButton bluetooth = (ImageButton) findViewById(R.id.imageButton5);
        //Habilitamos que el boton este al pendiente de ser apretado
        bluetooth.setOnClickListener(new View.OnClickListener() {

            //Al hacer click en el boton BT automaticamente se ejecuta metodo y el codigo que contenga en el
            @Override
            public void onClick(View v) {

                //Metodo para confirmar que la app ya esta emparejada con la Raspberry
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

                //Activamos la progressbar
                progressBar.setVisibility(View.VISIBLE);

                //SE intenta establecer la conexion con la Raspberry
                mmBluetoothService=new BluetoothService(MainActivity.this,mmDevice,uuid, mHandler);

                //Si la conexion fue exitosa, se quita la visibilidad de la progressbar
                if (mmBluetoothService != null)
                {
                    progressBar.setVisibility(View.GONE);
                }

            }
        });

    }


    //Verifica si el BT esta encendido, sino lo enciende
    private void EncenderBlue() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent intentBlEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intentBlEnable, REQUEST_ENABLE_BL);
        } else {
            Toast.makeText(this, "El Bluetooth ya esta encendido", Toast.LENGTH_LONG).show();
        }
    }

    //Metodo que verifica todos los dispositivos emparejados en el smartphone y busca el que llama Raspberry
    private void ObtenerDatosRaspBerry() {

        //Obtiene la lista de todos los dispositivos vinculados
        Set<BluetoothDevice> DispositivosVinculados = bluetoothAdapter.getBondedDevices();
        //obtener la direccion MAC de la raspberry
        if (DispositivosVinculados.size() > 0) {
            for (BluetoothDevice device : DispositivosVinculados) {
                if (device.getName().equals("raspberrypi")) {
                    mmDevice = device;
                }
            }
        }
    }



    //Método para el botón Reloj
    public void MensajeReloj(View view){

        if (mmBluetoothService != null)
        {
            mmBluetoothService.write("Reloj");
        }
        else
        {
            Toast.makeText(this, "No hay conexion BT", Toast.LENGTH_SHORT) .show();
        }


    }

    //Método para el botón Calendario
    public void MensajeCalendario(View view){
        if (mmBluetoothService != null)
        {
            mmBluetoothService.write("Calendario");
        }
        else
        {
            Toast.makeText(this, "No hay conexion BT", Toast.LENGTH_SHORT) .show();
        }
    }

    //Método para el botón Clima
    public void MensajeClima(View view){
        if (mmBluetoothService != null)
        {
            mmBluetoothService.write("Clima");
        }
        else
        {
            Toast.makeText(this, "No hay conexion BT", Toast.LENGTH_SHORT) .show();
        }
    }

    //Método para el botón Notificaciones
    public void MensajeNotificaciones(View view){
        if (mmBluetoothService != null)
        {
            mmBluetoothService.write("Notificaciones");
        }
        else
        {
            Toast.makeText(this, "No hay conexion BT", Toast.LENGTH_SHORT) .show();
        }
    }

    //Método para el botón BT
   /* public boolean MensajeBT(View view){
        Toast.makeText(this, "Esto es un mensaje para el BT", Toast.LENGTH_SHORT) .show();
        return true;
    }*/

    //Método para el botón Información
    public void MensajeInfo(View view){
        if (mmBluetoothService != null)
        {
            mmBluetoothService.write("Info");
        }
        else
        {
            Toast.makeText(this, "No hay conexion BT", Toast.LENGTH_SHORT) .show();
        }
    }



}