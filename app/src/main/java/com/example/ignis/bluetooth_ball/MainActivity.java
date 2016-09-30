package com.example.ignis.bluetooth_ball;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Random;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private ConnectedThread communicationThread;
    private volatile BluetoothSocket mSocket = null;
    private BluetoothAdapter mBluetoothAdapter;
    private final UUID mUUID = new UUID(11, 222);
    private static BouncingBallView bouncingBallView;
    private boolean beingHost = false;
    private boolean beingGuest = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void onDestroy() {
        super.onDestroy();

        communicationThread.cancel();
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        Ball.gravity[0] = event.values[0];
        Ball.gravity[1] = event.values[1];

        Rectangular.gravity[0] = event.values[0];
        Rectangular.gravity[1] = event.values[1];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void toHost(View view) {
        if (!(beingHost || beingGuest)) {
            beingHost = true;
            main_fun();
        }


    }

    public void toGuest(View view) {
        if (!(beingHost || beingGuest)) {
            beingGuest = true;
            main_fun();
        }
    }

    private void main_fun() {
        SensorManager mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_NORMAL);

        // BLUETOOTH PARSING !!!
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            System.out.println("JESTES CIENIAS BO NIE MASZ BLUETOOTH");
        }


        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            int REQUEST_ENABLE_BT = 1;
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        //turn on visibility
        Intent discoverableIntent = new
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
        startActivity(discoverableIntent);


        if (beingHost) {
            Thread acceptThread = new AcceptThread();
            acceptThread.start();
        }


        if (beingGuest) {
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                while (mSocket == null) {
                    for (BluetoothDevice device : pairedDevices) {
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (mSocket != null)
                            break;
                        Thread connectThread = new ConnectThread(device);
                        connectThread.start();
                    }
                }
            }
        }
        while (mSocket == null) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
            }
        }


        communicationThread = new ConnectedThread(mSocket);
        communicationThread.start();


        if (beingHost) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        bouncingBallView = new BouncingBallView(this, communicationThread, beingHost);
        setContentView(bouncingBallView);
        bouncingBallView.setBackgroundColor(Color.BLACK);
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            try {
                tmp = device.createRfcommSocketToServiceRecord(mUUID);
            } catch (IOException e) {
            }
            mmSocket = tmp;
        }

        public void run() {
            mBluetoothAdapter.cancelDiscovery();

            try {
                mmSocket.connect();
            } catch (IOException connectException) {
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                }
                return;
            }

            mSocket = mmSocket;
        }


        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    public class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    if (bouncingBallView != null) {
                        String positionString = new String(buffer, 0, bytes, Charset.forName("UTF-8"));
                        positionString = positionString.replace('[', ' ');
                        positionString = positionString.replace(']', ' ');
                        String[] parts = positionString.split(",");
                        float[] tab = new float[parts.length];
                        for (int i = 0; i < parts.length; ++i)
                            tab[i] = Float.parseFloat(parts[i]);
                        bouncingBallView.setBall(tab[0], tab[1], -tab[2], tab[3]);
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }


    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("Bluetooth_BALLS", mUUID);
            } catch (IOException e) {
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket;
            while (mSocket == null) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                if (socket != null) {
                    mSocket = socket;
                    break;
                }
            }
        }

        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
            }
        }
    }
}


