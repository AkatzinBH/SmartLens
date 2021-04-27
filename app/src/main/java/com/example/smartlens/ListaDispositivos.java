package com.example.smartlens;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class ListaDispositivos extends AppCompatActivity {

    private ListView listaEmparejados, listaDisponibles;
    private ArrayAdapter<String> adaptadorDisponibles, adaptadorEmparejados;

    private BluetoothAdapter bluetoothAdapter;
    private View vista;
    private ProgressBar progressBar;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_dispositivos);
        context = this;

        inicializar();

        listaEmparejados.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length() - 17);

                finishAffinity();

                // Realiza un intent para iniciar la siguiente actividad
                // mientras toma un EXTRA_DEVICE_ADDRESS que es la dirección MAC.
                Intent intend = new Intent(ListaDispositivos.this, MainActivity.class);
                intend.putExtra("deviceAddress", address);
                Log.d("MAC", " "+address);
                startActivity(intend);

            }
        });


    }

    private BroadcastReceiver bluetoothListener = new BroadcastReceiver() {
        @Override

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action))
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED)
                {
                    //adaptadorDisponibles.add(device.getName() + "\n" + device.getAddress());
                }
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {

            }
        }
    };

    // Configura un (on-click) para la lista
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView av, View v, int arg2, long arg3) {

            // Obtener la dirección MAC del dispositivo, que son los últimos 17 caracteres en la vista
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);


        }
    };

    public void inicializar()
    {
        listaEmparejados = findViewById(R.id.lvEmparejados);

        adaptadorEmparejados = new ArrayAdapter<String>(this, R.layout.lista_dispositivos_item);

        listaEmparejados.setAdapter(adaptadorEmparejados);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> emparejados = bluetoothAdapter.getBondedDevices();

        if (emparejados != null && emparejados.size()>0)
        {
            for (BluetoothDevice device : emparejados)
            {
                adaptadorEmparejados.add(device.getName() + '\n' + device.getAddress());
            }
        }

        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bluetoothListener,intentFilter);
        IntentFilter intentFilter2 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(bluetoothListener,intentFilter2);

    }
}