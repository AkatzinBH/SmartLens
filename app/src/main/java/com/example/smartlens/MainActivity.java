package com.example.smartlens;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
    private AlertDialog enableNotificationListenerAlertDialog;
    private ReceiveBroadcastReceiver imageChangeBroadcastReceiver;
    private ArrayList<Notificacion> notificaciones = new ArrayList<>();
    private Notificacion llamada;

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
        // If the user did not turn the notification listener service on we prompt him to do so
        if(!isNotificationServiceEnabled()){
            enableNotificationListenerAlertDialog = buildNotificationServiceAlertDialog();
            enableNotificationListenerAlertDialog.show();
        }
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

    public void MensajeTemporizador(View view){
        /*if (mmBluetoothService != null)
        {
            mmBluetoothService.write("Clima");
        }
        else
        {
            Toast.makeText(this, "No hay conexion BT", Toast.LENGTH_SHORT) .show();
        }*/

        Intent intent = new Intent(this,Temporizador.class);
        startActivity(intent);
    }

    public void MensajeImagen(View view){
        if (mmBluetoothService != null)
        {
            mmBluetoothService.write("Imagen");
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
            mmBluetoothService.write("Noti");
            Log.d("btnNoti","entro");
           /* new CountDownTimer(1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    Toast.makeText(MainActivity.this,"Tamaño:" + notificaciones.size(), Toast.LENGTH_SHORT) .show();
                    mmBluetoothService.write(Integer.toString(notificaciones.size()));
                }
            }.start();
            */
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

    public void llamdaentrantre()
    {
        Log.d("IfMet:", "Entra el if del Metodo");
        if (mmBluetoothService != null)
        {
            Log.d("IfMet:", "Intenta mandar");
            mmBluetoothService.write("Llamada");
            mmBluetoothService.write("\n");
            mmBluetoothService.write(" " + llamada.getPaquete() + " ");
            mmBluetoothService.write(llamada.getRemitente() + " ");
            mmBluetoothService.write(llamada.getMensaje() + " ");
            mmBluetoothService.write(llamada.getFechahora() + "\n");
            mmBluetoothService.write("Fin");
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
 /*   private void changeInterceptedNotificationImage(int notificationCode){
        switch(notificationCode){
            case Notificaciones.InterceptedNotificationCode.FACEBOOK_CODE:
                    Toast.makeText(this,"Facebook",Toast.LENGTH_SHORT).show();
                Log.d("NotificacionInt", "La notificacion interceptada fue de: Facebook");
                break;
            case Notificaciones.InterceptedNotificationCode.INSTAGRAM_CODE:
                Toast.makeText(this,"Instagram",Toast.LENGTH_SHORT).show();
                Log.d("NotificacionInt", "La notificacion interceptada fue de: Instagram");
                break;
            case Notificaciones.InterceptedNotificationCode.WHATSAPP_CODE:
                Toast.makeText(this,"Whatsapp",Toast.LENGTH_SHORT).show();
                Log.d("NotificacionInt", "La notificacion interceptada fue de: Whastapp");
                break;
            case Notificaciones.InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE:
                Toast.makeText(this,"Otras apps",Toast.LENGTH_SHORT).show();
                Log.d("NotificacionInt", "La notificacion interceptada fue de: Otro servicio");
                break;
        }
    }
*/
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
                    if (packages.matches("com.samsung.android.incallui")) //"com.android.incallui"
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

}