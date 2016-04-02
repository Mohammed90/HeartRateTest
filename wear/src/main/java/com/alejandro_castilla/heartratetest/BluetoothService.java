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

/**
 * Created by alejandrocq on 16/03/16.
 */

enum BluetoothStatus { IDLE, DISCOVERING, NOT_SUPPORTED }

public class BluetoothService extends Service {

    private static final String TAG = "BluetoothService";
    public static BluetoothStatus mBluetoothStatus;

    private final IBinder mBluetoothServiceBinder = new BluetoothServiceBinder();
    private BluetoothAdapter mBluetoothAdapter = null;
    private BroadcastReceiver mReceiver;

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
        return mBluetoothServiceBinder;
    }

    public void setmBluetoothAdapter(BluetoothAdapter mBluetoothAdapter) {
        this.mBluetoothAdapter = mBluetoothAdapter;
    }

    public void startDiscoveryOfDevices() {
        mBluetoothAdapter.startDiscovery();
        mBluetoothStatus = BluetoothStatus.DISCOVERING;

        //BroadcastReceiver to receive data
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent
                            .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                    Toast.makeText(mMainContext, device.getName() + "\n" + device.getAddress()
//                            , Toast.LENGTH_LONG);
                    Log.d("Device found", device.getName() + " " + device.getAddress());

                }
            }
        };

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);
        Log.i(TAG, "Receiver registered.");

    }

    public void stopDiscoveryOfDevices() {
        this.unregisterReceiver(mReceiver);
        mBluetoothAdapter.cancelDiscovery();
        Log.i(TAG, "Receiver unregistered.");
    }


    /* Class used to bind with the client (MainActivity.java) */

    public class BluetoothServiceBinder extends Binder {

        public BluetoothService getService() {
            return BluetoothService.this;
        }
    }

}
