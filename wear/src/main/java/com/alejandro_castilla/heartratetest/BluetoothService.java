package com.alejandro_castilla.heartratetest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by alejandrocq on 16/03/16.
 */

enum BluetoothStatus { IDLE, DISCOVERING, NOT_SUPPORTED }

public class BluetoothService extends WearableActivity {

    //Request code to enable Bluetooth.
    private final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BroadcastReceiver mReceiver;
    private Context mMainContext = null;
    private BluetoothStatus mBluetoothStatus;

    public BluetoothService(Context context) {
        this.mMainContext = context;
    }

    public void enableBluetooth() {

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(mMainContext, "This device doesn't support Bluetooth.",
                    Toast.LENGTH_LONG).show();
            mBluetoothStatus = BluetoothStatus.NOT_SUPPORTED;
        } else if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT);
            Toast.makeText(mMainContext, "Bluetooth has been enabled.", Toast.LENGTH_LONG).show();
            mBluetoothStatus = BluetoothStatus.IDLE;
        } else {
            Toast.makeText(mMainContext, "Bluetooth is already enabled.", Toast.LENGTH_LONG)
                    .show();
            mBluetoothStatus = BluetoothStatus.IDLE;
//            Log.i("Status:", "Bluetooth is already enabled.");
        }

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
        mMainContext.registerReceiver(mReceiver, filter);
        Log.i("Info", "Receiver registered.");

    }

    public void stopDiscoveryOfDevices() {
        mMainContext.unregisterReceiver(mReceiver);
        mBluetoothAdapter.cancelDiscovery();
        Log.i("Info", "Receiver unregistered.");
    }

    public BluetoothStatus getmBluetoothStatus() {
        return mBluetoothStatus;
    }
}
