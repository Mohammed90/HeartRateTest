package com.alejandro_castilla.heartratetest;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.support.wearable.activity.WearableActivity;
import android.widget.Toast;

/**
 * Created by alejandrocq on 16/03/16.
 */
public class BluetoothService extends WearableActivity {

    //Request code to enable Bluetooth.
    private final int REQUEST_ENABLE_BT = 1;

    private BluetoothAdapter mBluetoothAdapter = null;
    private Context mToastContext = null;

    public BluetoothService(Context context) {
        this.mToastContext = context;
    }

    public void enableBluetooth() {

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(mToastContext, "This device doesn't support Bluetooth.",
                    Toast.LENGTH_LONG).show();
        } else if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT);
            Toast.makeText(mToastContext, "Bluetooth has been enabled.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(mToastContext, "Bluetooth is already enabled.", Toast.LENGTH_LONG)
                    .show();
//            Log.i("Status:", "Bluetooth is already enabled.");
        }

    }

}
