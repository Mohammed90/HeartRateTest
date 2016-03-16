package com.alejandro_castilla.heartratetest;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;

/**
 * Created by alejandrocq on 16/03/16.
 */
public class BluetoothService extends WearableActivity {

    //Request code to enable Bluetooth.
    private final int REQUEST_ENABLE_BT = 1;

    private BluetoothAdapter mBluetoothAdapter = null;

    public BluetoothService() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void enableBluetooth() {
        if (mBluetoothAdapter == null) {
            Log.e("ERROR: ", "This device does not support Bluetooth.");
        } else if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT);
        } else {
            Log.i("Result: ", "Bluetooth already enabled");
        }

    }

}
