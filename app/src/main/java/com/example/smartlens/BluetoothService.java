package com.example.smartlens;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.logging.Handler;

public class BluetoothService {
    private static final String TAG = "BluetoothConnectionServ";
    private final BluetoothAdapter bluetoothAdapter;
    Context mcontext;


    private BluetoothDevice mDevice;
    private ConnectThread mmConnectThread;
    private ConnectedThread mmConnectedThread;
    private  static  UUID MyUUID;
    private  android.os.Handler mmHandler;

    public BluetoothService(Context context, BluetoothDevice Device, UUID Uuid, android.os.Handler mHandler) {
        mcontext=context;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mmHandler = mHandler;
        IniciarCliente(Device,Uuid);
    }

   /* public BluetoothService(MainActivity context, BluetoothDevice Device, UUID Uuid, Handler mHandler) {

    }*/


    private class ConnectThread extends  Thread{
        private BluetoothSocket mmSocket;

        public  ConnectThread (BluetoothDevice Device, UUID Uuid){
            mDevice=Device;
            MyUUID=Uuid;
        }

        public void run(){
            try {
                mmSocket=mDevice.createRfcommSocketToServiceRecord(MyUUID);
            } catch (IOException e) {
                e.printStackTrace();
            }

            bluetoothAdapter.cancelDiscovery();

            if(!mmSocket.isConnected()){
                try {
                    mmSocket.connect();
                    Log.d(TAG, "run: ConnectThread connected.");
                } catch (IOException e) {
                    e.printStackTrace();
                    try {
                        mmSocket.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                        Log.d(TAG, "run: Closed Socket.");
                    }
                    Log.d(TAG, "run: ConnectThread: Could not connect to UUID: " + MyUUID );
                }
            }

            Conectado(mmSocket);
        }

        public void cancel() {
            try {
                Log.d(TAG, "cancel: Closing Client Socket.");
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: close() of mmSocket in Connectthread failed. " + e.getMessage());
            }
        }

    }

    private class ConnectedThread extends  Thread{
        private final BluetoothSocket mmSocket;
        private final InputStream mmInputStream;
        private final OutputStream mmOutputStream;

        private ConnectedThread(BluetoothSocket Socket) {
            mmSocket = Socket;
            InputStream intemp=null;
            OutputStream outemp=null;

            try {
                intemp= mmSocket.getInputStream();
                outemp= mmSocket.getOutputStream();
            }catch (IOException e){
                e.printStackTrace();
            }

            mmInputStream=intemp;
            mmOutputStream=outemp;
        }

        public void run(){
            byte[] buffer= new byte[1024];
            int bytes;
            while(true){
                try {
                    bytes=mmInputStream.read(buffer);
                    String MensajeCPU=new String(buffer,0,bytes);
                    if (!MensajeCPU.equals("65280")) {
                        mmHandler.obtainMessage(1, bytes, -1, buffer)
                                .sendToTarget();
                    }else{
                        mmHandler.obtainMessage(2, bytes, -1, buffer)
                                .sendToTarget();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

        public void write(byte [] Bytes){
            try {
                mmOutputStream.write(Bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                Log.d(TAG, "cancel: Closing Client Socket.");
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: close() of mmSocket in Connectthread failed. " + e.getMessage());
            }
        }
    }

    public void IniciarCliente(BluetoothDevice Device, UUID Uuid){
        mmConnectThread=new ConnectThread(Device,Uuid);
        mmConnectThread.start();
    }

    public void Conectado (BluetoothSocket socket){
        mmConnectedThread = new ConnectedThread(socket);
        mmConnectedThread.start();
    }

    public void write(String Msg){
        byte [] Bytes;
        Bytes=Msg.getBytes();
        mmConnectedThread.write(Bytes);
    }





}