package com.example.smartlens;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements onOpcionListener {

    private ArrayList<Menu> opciones;
    private RecyclerView listaOpciones;
    private View view;

    // Declarar las variables que se utilizaran
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    //BluetoothSocket mmSocket;
    BluetoothDevice mmDevice = null;
    BluetoothService mmBluetoothService = null;
    static String VideoSeleccionado;
    private MenuAdaptador adaptador;
    private ProgressBar progressBar;
    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
    private AlertDialog enableNotificationListenerAlertDialog;
    private ReceiveBroadcastReceiver imageChangeBroadcastReceiver;
    private ArrayList<Notificacion> notificaciones = new ArrayList<>();
    private Notificacion llamada;

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

        horas = (EditText) findViewById(R.id.etHoras);
        min = (EditText) findViewById(R.id.etMin);
        seg = (EditText) findViewById(R.id.etSegundos);
        tempo = (TextView) findViewById(R.id.tvTempo);
        iniciarP = (Button) findViewById(R.id.btnIniciarP);
        reiniciar = (Button) findViewById(R.id.btnReinicar);

        flag = true;
        flag2 = false;

        opciones = new ArrayList<Menu>();


        opciones.add(new Menu(R.drawable.iconos_aka_reloj_02));
        opciones.add(new Menu(R.drawable.iconos_aka_calendario_02));
        opciones.add(new Menu(R.drawable.iconos_aka_clima_02));
        opciones.add(new Menu(R.drawable.iconos_akanotificaci_n_02));
        opciones.add(new Menu(R.drawable.iconos_aka_galer_a_02));
        opciones.add(new Menu(R.drawable.iconos_aka_temporizador_02));
        opciones.add(new Menu(R.drawable.iconos_aka_configuraci_n_02));
        opciones.add(new Menu(R.drawable.iconos_aka_configuraci_n_02));
        opciones.add(new Menu(R.drawable.iconos_aka_bluetooth_02));
        opciones.add(new Menu(R.drawable.iconos_aka_informaci_n_02));

        listaOpciones = (RecyclerView) findViewById(R.id.rvMenuPrincial);
        GridLayoutManager glm = new GridLayoutManager(this,2);
        listaOpciones.setLayoutManager(glm);
        adaptador = new MenuAdaptador(opciones,this);
        adaptador.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view = v;
            }
        });

        listaOpciones.setAdapter(adaptador);

        //Ayuda visual para mostrar que se esta estableciendo la conexion al BT
        progressBar = (ProgressBar) findViewById(R.id.pbConnectBT);
        // If the user did not turn the notification listener service on we prompt him to do so
        if(!isNotificationServiceEnabled()){
            enableNotificationListenerAlertDialog = buildNotificationServiceAlertDialog();
            enableNotificationListenerAlertDialog.show();
        }
        EncenderBlue();


        imageChangeBroadcastReceiver = new ReceiveBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.ssa_ezra.whatsappmonitoring");
        registerReceiver(imageChangeBroadcastReceiver,intentFilter);

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

    public void llamdaentrantre()
    {
        Log.d("IfMet:", "Entra el if del Metodo");
        if (mmBluetoothService != null)
        {
            Log.d("IfMet:", "Intenta mandar");
            mmBluetoothService.write("Llamada");
            new CountDownTimer(1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    mmBluetoothService.write(llamada.getPaquete() + " ");
                    mmBluetoothService.write(llamada.getRemitente() + " ");
                    mmBluetoothService.write(llamada.getMensaje() + " ");
                    mmBluetoothService.write( "\n");
                }
            }.start();

            new CountDownTimer(1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {


                }

                @Override
                public void onFinish() {
                    mmBluetoothService.write("Fin");
                }
            }.start();


        }
        else
        {
            Toast.makeText(this, "No hay conexion BT llamada entrante", Toast.LENGTH_SHORT) .show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Change Intercepted Notification Image
     * Changes the MainActivity image based on which notification was intercepted
     * @param notificationCode The intercepted notification code
     */

    private static final String TAG = "MyActivity";

    public void onOpcionClick(int position) {

        int posicion = position;
        Log.i(TAG, "Posicion para el switch: " + posicion);

        switch (posicion) {

            case 0:
                if (mmBluetoothService != null)
                {
                    mmBluetoothService.write("Reloj");
                }
                else
                {
                    Toast.makeText(this, "No hay conexion BT", Toast.LENGTH_SHORT) .show();
                }
                break;
            case 1:
                Log.i(TAG, "Entro a la posicion 1 ");
                if (mmBluetoothService != null)
                {
                    mmBluetoothService.write("Calendario");
                }
                else
                {
                    Toast.makeText(this, "No hay conexion BT", Toast.LENGTH_SHORT) .show();
                }
                break;
            case 2:
                if (mmBluetoothService != null)
                {
                    mmBluetoothService.write("Clima");
                }
                else
                {
                    Toast.makeText(this, "No hay conexion BT", Toast.LENGTH_SHORT) .show();
                }
                break;
            case 3:
                if (mmBluetoothService != null)
                {
                    mmBluetoothService.write("Noti");
                    Log.d("btnNoti","entro");

                    new CountDownTimer(1000, 1000) {

                        public void onTick(long millisUntilFinished) {

                        }

                        public void onFinish() {
                            Toast.makeText(MainActivity.this,"Tamaño:" + notificaciones.size(), Toast.LENGTH_SHORT) .show();
                            if (notificaciones.size() > 0)
                            {
                                for (Notificacion notificacion : notificaciones)
                                {
                                    String noti = "";
                                    mmBluetoothService.write(" " + notificacion.getPaquete() + " ");
                                    mmBluetoothService.write(notificacion.getRemitente() + " ");
                                    mmBluetoothService.write(notificacion.getMensaje() + " ");
                                    mmBluetoothService.write(notificacion.getFechahora() + "\n");
                                    mmBluetoothService.write("");

                                    Log.d ("NotiBT","Notificacion: " + notificacion.getPaquete() + " De: " + notificacion.getRemitente() + " Mensaje: " +notificacion.getMensaje() + " a las: " + notificacion.getFechahora());

                                }
                                Log.d("NotiBT","Sale del For");
                                new CountDownTimer(1000, 1000) {
                                    @Override
                                    public void onTick(long millisUntilFinished) {

                                    }

                                    @Override
                                    public void onFinish() {
                                        mmBluetoothService.write("Fin");
                                    }
                                }.start();

                            }
                            else {
                                mmBluetoothService.write("Fin");
                            }

                        }
                    }.start();

                }
                else
                {
                    Toast.makeText(this, "No hay conexion BT", Toast.LENGTH_SHORT) .show();
                }
                break;
            case 4:
                if (mmBluetoothService != null)
                {
                    mmBluetoothService.write("Imagen");
                }
                else
                {
                    Toast.makeText(this, "No hay conexion BT", Toast.LENGTH_SHORT) .show();
                }
                break;
            case 5:
                /*Intent intent = new Intent(this,Temporizador.class);
                //intent.putExtra("BT", mmDevice.getAddress());
                startActivity(intent);*/
                if ((horas.getText().toString().matches("")) || (min.getText().toString().matches("")) || (seg.getText().toString().matches("")))
                {
                    Toast.makeText(MainActivity.this,"Ingresa el tiempo",Toast.LENGTH_SHORT).show();
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
                break;

            case 6:
                EditText edFiguras = (EditText) findViewById(R.id.etFiguras);
                if (mmBluetoothService != null)
                {
                    if (!edFiguras.getText().toString().matches(""))
                    {
                        mmBluetoothService.write("Figura" + edFiguras.getText().toString());
                    }
                    else
                    {
                        Toast.makeText(this, "Ingrese un numero de figura", Toast.LENGTH_SHORT) .show();
                    }

                }
                else
                {
                    Toast.makeText(this, "No hay conexion BT", Toast.LENGTH_SHORT) .show();
                }

                break;

            case 7:

                EditText edTextos = (EditText) findViewById(R.id.etTextos);
                if (mmBluetoothService != null)
                {
                    if (!edTextos.getText().toString().matches("")) {
                        mmBluetoothService.write("Texto" + edTextos.getText().toString());
                    }
                    else
                    {
                        Toast.makeText(this, "Ingrese un numero de texto", Toast.LENGTH_SHORT) .show();
                    }
                }
                else
                {
                    Toast.makeText(this, "No hay conexion BT", Toast.LENGTH_SHORT) .show();
                }

                break;

            case 8:
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


                break;
                case 9:
                //imagen
                break;
            default:
                break;
        }


    }
    /**
     * Is Notification Service Enabled.
     * Verifies if the notification listener service is enabled.
     * Got it from: https://github.com/kpbird/NotificationListenerService-Example/blob/master/NLSExample/src/main/java/com/kpbird/nlsexample/NLService.java
     * @return True if enabled, false otherwise.
     */
    private boolean isNotificationServiceEnabled(){
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Image Change Broadcast Receiver.
     * We use this Broadcast Receiver to notify the Main Activity when
     * a new notification has arrived, so it can properly change the
     * notification image
     * */
  /*  public class ImageChangeBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int receivedNotificationCode = intent.getIntExtra("Notification Code",-1);
            changeInterceptedNotificationImage(receivedNotificationCode);
        }
    }*/


    /**
     * Build Notification Listener Alert Dialog.
     * Builds the alert dialog that pops up if the user has not turned
     * the Notification Listener Service on yet.
     * @return An alert dialog which leads to the notification enabling screen
     */
    private AlertDialog buildNotificationServiceAlertDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.notification_listener_service);
        alertDialogBuilder.setMessage(R.string.notification_listener_service_explanation);
        alertDialogBuilder.setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
                    }
                });
        alertDialogBuilder.setNegativeButton(R.string.no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // If you choose to not enable the notification listener
                        // the app. will not work as expected
                    }
                });
        return(alertDialogBuilder.create());
    }


    /**
     * Receive Broadcast Receiver.
     * */
    public class ReceiveBroadcastReceiver extends BroadcastReceiver {


        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("Entra al metodo");

            int receivedNotificationCode = intent.getIntExtra("Notification Code",-1);
            String packages = intent.getStringExtra("package");
            String title = intent.getStringExtra("title");
            String text = intent.getStringExtra("text");

            if(text != null) {

                if(!text.contains("nuevos mensajes") && !text.contains("WhatsApp Web está actualmente activo") && !text.contains("new messages") && !text.contains("WhatsApp Web is currently active") && !text.contains("WhatsApp Web login")) {

                    String android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                            Settings.Secure.ANDROID_ID);
                    String devicemodel = android.os.Build.MANUFACTURER+android.os.Build.MODEL+android.os.Build.BRAND+android.os.Build.SERIAL;

                    DateFormat df = new SimpleDateFormat("yyyy.MM.dd 'a las' HH:mm:ss");
                    String date = df.format(Calendar.getInstance().getTime());

                    //tvMsg.setText("Notification : " + receivedNotificationCode + "\nPackages : " + packages + "\nTitle : " + title + "\nText : " + text + "\nId : " + date+ "\nandroid_id : " + android_id+ "\ndevicemodel : " + devicemodel);
                    Log.d("If llamada:", "Antes del if : " + packages);
                    if (packages.matches("com.android.incallui")) //"com.android.incallui"
                    {
                        Log.d("If llamada:", "Entra el if de la llamada");
                        llamada = new Notificacion(text, title,packages,date);
                        llamdaentrantre();
                    }
                     Log.d("DetailsEzraatext2 :", "Notification : " + receivedNotificationCode + "\nPackages : " + packages + "\nTitle : " + title + "\nText : " + text + "\nId : " + date+ "\nandroid_id : " + android_id+ "\ndevicemodel : " + devicemodel);
                    if (notificaciones.size()>10)
                    {notificaciones.clear();}
                    notificaciones.add(new Notificacion(text, title,packages,date));
                }
                else
                {
                    Log.d("Main:", "Text contiene mensajes no necesarios");

                }
            }
            Log.d("Main:", "Text es nulo");

        }
    }


    private boolean isAccessibilityOn (Context context, Class<? extends AccessibilityService> clazz) {
        int accessibilityEnabled = 0;
        final String service = context.getPackageName () + "/" + clazz.getCanonicalName ();
        try {
            accessibilityEnabled = Settings.Secure.getInt (context.getApplicationContext ().getContentResolver (), Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException ignored) {  }

        TextUtils.SimpleStringSplitter colonSplitter = new TextUtils.SimpleStringSplitter (':');

        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString (context.getApplicationContext ().getContentResolver (), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                colonSplitter.setString (settingValue);
                while (colonSplitter.hasNext ()) {
                    String accessibilityService = colonSplitter.next ();

                    if (accessibilityService.equalsIgnoreCase (service)) {
                        return true;
                    }
                }
            }
        }

        return false;
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

                flag = true;
                if (mmBluetoothService != null)
                {
                    mmBluetoothService.write("Temporizador");
                    stopTimer();
                }
                else
                {
                    Toast.makeText(MainActivity.this, "No hay conexion BT", Toast.LENGTH_SHORT) .show();

                }


            }
        }.start();

        timerRunning = true;
    }

    public void stopTimer()
    {
        temporizador.cancel();
        timerRunning = false;
    }

}