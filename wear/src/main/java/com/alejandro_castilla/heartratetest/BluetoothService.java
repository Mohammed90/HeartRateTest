package com.alejandro_castilla.heartratetest;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import zephyr.android.BioHarnessBT.BTClient;

/**
 * Created by alejandrocq on 16/03/16.
 */

enum BluetoothStatus { IDLE, DISCOVERING, NOT_SUPPORTED }

public class BluetoothService extends Service {

    private static final String TAG = "BluetoothService";
    public static BluetoothStatus bluetoothStatus;

    private final IBinder bluetoothServiceBinder = new BluetoothServiceBinder();
    private BluetoothAdapter bluetoothAdapter = null;
    private BroadcastReceiver broadcastReceiver;
    private BTClient zephyrBTClient;

    private BluetoothDevice device;
    private String bluetoothMACAddress;

    private Context toastContext = null;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "BluetoothService has been started.");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    // We don't want this service to be binded for the moment, so we return null.
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return bluetoothServiceBinder;
    }

    public void setBluetoothAdapter(BluetoothAdapter bluetoothAdapter) {
        this.bluetoothAdapter = bluetoothAdapter;
    }

    public void startDiscoveryOfDevices() {
        bluetoothAdapter.startDiscovery();
        bluetoothStatus = BluetoothStatus.DISCOVERING;

        //BroadcastReceiver to receive data
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice deviceFound = intent
                            .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String deviceData = deviceFound.getName() + " " + deviceFound.getAddress();
                    Log.d("Device found", deviceData);
                    Toast.makeText(toastContext, deviceData, Toast.LENGTH_LONG).show();
                    // Temporary stuff for Zephyr sensor
                    if (deviceFound.getName().contains("BH")) {
                        device = deviceFound;
                        bluetoothMACAddress = deviceFound.getAddress();
                        Toast.makeText(toastContext, "Zephyr sensor found!",
                                Toast.LENGTH_LONG).show();
                    }

                }
            }
        };

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(broadcastReceiver, filter);
        Log.i(TAG, "Receiver registered.");

    }

    public void stopDiscoveryOfDevices() {
        if (bluetoothStatus == bluetoothStatus.DISCOVERING ) {
            this.unregisterReceiver(broadcastReceiver);
            bluetoothStatus = BluetoothStatus.IDLE;
            bluetoothAdapter.cancelDiscovery();
            Log.i(TAG, "Receiver unregistered.");
        }
    }

    public void setToastContext(Context toastContext) {
        this.toastContext = toastContext;
    }

    /* Class used to bind with the client (MainActivity.java) */

    public class BluetoothServiceBinder extends Binder {
        public BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    /* Thread used to connect with the target Bluetooth device */

//    private class BluetoothConnectThread extends Thread {
//        private final BluetoothSocket mSocket;
//        private final BluetoothDevice device;
//
//        public BluetoothConnectThread(BluetoothSocket mSocket, BluetoothDevice device) {
//            // We create a temporary socket because mSocket is final
//            BluetoothSocket tmpSocket = mSocket;
//            this.device = device;
//
//            // Get a BluetoothSocket to connect with the given BluetoothDevice
//            try {
//                // MY_UUID is the app's UUID string, also used by the server code
//                tmpSocket = device.createRfcommSocketToServiceRecord(UUID);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            this.mSocket = tmpSocket;
//
//        }
//
//
//
//
//    }

}
