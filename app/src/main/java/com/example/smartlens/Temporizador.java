package com.example.smartlens;
//>, <
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.UUID;

public class Temporizador extends AppCompatActivity {

    private EditText horas;
    private EditText min;
    private EditText seg;
    private TextView tempo;
    private Button iniciarP;
    private Button reiniciar;
    private long tiempo;
    private boolean timerRunning;
    private CountDownTimer temporizador;
    private long tiempoRestante;
    private boolean flag,flag2;
    private BluetoothService mmBluetoothService;
    private BluetoothDevice mmDevice;
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    /*private Handler mHandler= new Temporizador.MyHandler2(this);

    private class MyHandler2 extends Handler{
        //crea un contexto para la clase de la cual se recibiran los mensajes
        private WeakReference<Temporizador> mActivity;
        //Constructo de la clase, obtiene como parametro la actividad
        public MyHandler2(Temporizador activity) {
            mActivity = new WeakReference<Temporizador>(activity);
            //context=activity.getApplicationContext();
        }
        //SE soobreescribe el metodo para manejar los mensajes, que hacer en caso de que llegue un mensaje nuevo
        @Override
        public void handleMessage(Message msg) {
            byte[] buffer = (byte[]) msg.obj;
            switch (msg.what){
                case 1:
                    String MensajeCPU=new String(buffer,0,msg.arg1);
                    Toast.makeText(Temporizador.this, "El Bluetooth ya esta encendido", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

    int REQUEST_ENABLE_BL = 1;
*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temporizador);
  /*      UUID uuid=UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");
        Set<BluetoothDevice> DispositivosVinculados = bluetoothAdapter.getBondedDevices();
        //obtener la direccion MAC de la raspberry
        if (DispositivosVinculados.size() > 0) {
            for (BluetoothDevice device : DispositivosVinculados) {
                if (device.getName().equals("raspberrypi")) {
                    mmDevice = device;

                }
            }
        }

        mmBluetoothService=new BluetoothService(Temporizador.this,mmDevice,uuid, mHandler);
*/
        horas = (EditText) findViewById(R.id.etHoras);
        min = (EditText) findViewById(R.id.etMin);
        seg = (EditText) findViewById(R.id.etSegundos);
        tempo = (TextView) findViewById(R.id.tvTempo);
        iniciarP = (Button) findViewById(R.id.btnIniciarP);
        reiniciar = (Button) findViewById(R.id.btnReinicar);

        flag = true;
        flag2 = false;


        iniciarP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if ((horas.getText().toString().matches("")) || (min.getText().toString().matches("")) || (seg.getText().toString().matches("")))
                {
                    Toast.makeText(Temporizador.this,"Ingresa el tiempo",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    if (flag)
                    {
                        tiempo = Integer.parseInt(horas.getText().toString())*3600 + Integer.parseInt(min.getText().toString())*60 + Integer.parseInt(seg.getText().toString());
                        tiempo = tiempo*1000;
                        flag = false;
                    }

                    startStop();

                }
            }
        });

        reiniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flag2)
                {
                    temporizador.cancel();
                    flag2 = false;
                }
                tiempo = Integer.parseInt(horas.getText().toString())*3600 + Integer.parseInt(min.getText().toString())*60 + Integer.parseInt(seg.getText().toString());
                tiempo = tiempo*1000;
                flag = false;
                startTimer();

            }
        });
    }

    public void  startStop()
    {
        if(timerRunning)
        {
            stopTimer();
        }
        else
        {
            startTimer();
        }
    }

    public void startTimer()
    {
        flag2 = true;
        temporizador = new CountDownTimer(tiempo, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tiempoRestante = millisUntilFinished;
                String horasR, minR, segR;
                System.out.println("tiempo: "+tiempoRestante);
                int horasT = (int) (millisUntilFinished/3600000);
                int minT = (int) (millisUntilFinished%3600000)/60000;
                int segT = (int)  (millisUntilFinished%60000)/1000;

                if (horasT < 10)
                {
                    horasR = "0"+ horasT;
                }
                else
                {
                    horasR = String.valueOf(horasT);
                }

                if (minT < 10)
                {
                    minR = "0"+ minT;
                }
                else
                {
                    minR = String.valueOf(minT);
                }

                if (segT < 10)
                {
                    segR = "0"+ segT;
                }
                else
                {
                    segR = String.valueOf(segT);
                }

                String restante = horasR + ":" + minR + ":" + segR;

                tempo.setText(restante);

            }

            @Override
            public void onFinish() {

                iniciarP.setText("Iniciar");

                flag = true;

            }
        }.start();

        iniciarP.setText("Pausar");
        timerRunning = true;
    }

    public void stopTimer()
    {
        temporizador.cancel();
        timerRunning = false;
        iniciarP.setText("Reanudar");
        tiempo = tiempoRestante;
    }
}