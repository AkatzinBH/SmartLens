package com.example.smartlens;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

public class BTUtils {

    private Context context;
    private final Handler handler;
    private ConnectThread connectThread;
    private BluetoothAdapter bluetoothAdapter;
    private AcceptThread acceptThread;


    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;

    private final UUID APP_UUID = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");
    private final String APP_NAME = "CloudBlackBox";

    private int state;

    public BTUtils(Context context, Handler handler)
    {
        this.context = context;
        this.handler = handler;

        state = STATE_NONE;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public synchronized void setState(int state)
    {
        this.state = state;
        handler.obtainMessage(MainActivity.MESSAGE_STATE_CHANGED,state,-1).sendToTarget();
    }

    private synchronized void start()
    {
        if (connectThread != null)
        {
            connectThread.cancel();
            connectThread = null;
        }

        if (acceptThread == null)
        {
            acceptThread = new AcceptThread();
            acceptThread.start();
        }

        setState(STATE_LISTEN);
    }

    public synchronized void stop()
    {
        if (connectThread != null)
        {
            connectThread.cancel();
            connectThread = null;
        }
        if (acceptThread != null)
        {
            acceptThread.cancel();
            acceptThread = null;
        }

        setState(STATE_NONE);

    }

    public void connect2 (BluetoothDevice device)
    {
        if (state == STATE_CONNECTING)
        {
            connectThread.cancel();
            connectThread = null;
        }

        connectThread = new ConnectThread(device);
        connectThread.start();

        setState(STATE_CONNECTING);
    }

    private class AcceptThread extends Thread
    {
        private BluetoothServerSocket serverSocket;

        public AcceptThread()
        {
            BluetoothServerSocket tmp = null;

            try {
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME,APP_UUID);
            }catch (IOException e)
            {
                Log.e("Accept->Constructor", e.toString());
            }

            serverSocket = tmp;
        }

        public void run()
        {
            BluetoothSocket socket = null;
            try {
                socket = serverSocket.accept();
            }catch (IOException e)
            {
                Log.e("Accept->Run", e.toString());
                try {
                    serverSocket.close();
                }catch (IOException e1)
                {
                    Log.e("Accept->Close", e.toString());
                }
            }

            if (socket != null)
            {
                switch (state)
                {
                    case STATE_LISTEN:
                        break;
                    case STATE_CONNECTING:
                        connect(socket.getRemoteDevice());
                        break;
                    case STATE_NONE:
                        break;
                    case STATE_CONNECTED:
                        try {
                            socket.close();
                        } catch (IOException e) {
                            Log.e("Accept>State->Close", e.toString());
                        }
                        break;
                }
            }

        }

        public void cancel()
        {
            try {
                serverSocket.close();
            }catch (IOException e)
            {
                Log.e("Accept->CloseSSocket", e.toString());
            }
        }
    }



    private class ConnectThread extends Thread
    {
        private final BluetoothSocket socket;
        private final BluetoothDevice device;

        public ConnectThread(BluetoothDevice device)
        {
            this.device = device;
            BluetoothSocket tmp = null;

            try {
                tmp = device.createRfcommSocketToServiceRecord(APP_UUID);
            }catch (IOException e)
            {
                Log.e("Connect->Constructor", e.toString());
            }

            socket = tmp;
        }

        public void run()
        {
            try {
                socket.connect();

            }catch (IOException e) {
                Log.e("Connect->Run", e.toString());
                try
                {
                    socket.close();
                }catch (IOException e1)
                {
                    Log.e("Connect->CloseSocket", e.toString());
                }
            }

            synchronized (BTUtils.this)
            {
                connectThread = null;
            }

            connect(device);
        }

        public void cancel()
        {
            try {
                socket.close();
            }catch (IOException e)
            {
                Log.e("Connect->Cancel", e.toString());
            }
        }

    }

    private synchronized void connectionFailed()
    {
        Message message = handler.obtainMessage(MainActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.TOAST,"Can't connect to the service");
        handler.sendMessage(message);

        BTUtils.this.start();
    }

    private synchronized void connect(BluetoothDevice device)
    {
        if (connectThread != null)
        {
            connectThread.cancel();
            connectThread = null;
        }

        Message message = handler.obtainMessage(MainActivity.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.DEVICE_NAME,device.getName());
        message.setData(bundle);
        handler.sendMessage(message);
        setState(STATE_CONNECTED);
    }
}