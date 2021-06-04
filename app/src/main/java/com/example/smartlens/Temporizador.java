package com.example.smartlens;
//>, <
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temporizador);

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