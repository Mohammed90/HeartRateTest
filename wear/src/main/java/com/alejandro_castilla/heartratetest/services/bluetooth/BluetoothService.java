package com.alejandro_castilla.heartratetest.services.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.alejandro_castilla.heartratetest.MainActivity;
import com.alejandro_castilla.heartratetest.MainActivity.IntentType;

import java.util.ArrayList;

/**
 * Created by alejandrocq on 16/03/16.
 */



public class BluetoothService extends Service {

    public enum BluetoothStatus { IDLE, NOT_SUPPORTED }

    private static final String TAG = "BluetoothService";
    public static BluetoothStatus bluetoothStatus;

    private final IBinder bluetoothServiceBinder = new BluetoothServiceBinder();
    private BluetoothAdapter bluetoothAdapter = null;
    private BroadcastReceiver broadcastReceiver;
    private ArrayList<BluetoothDevice> devices;
    private BluetoothDevice targetDevice = null;
    private boolean deviceFound;

    private Context toastContext = null;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
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
        Log.d(TAG, "BluetoothService has been started.");
        return bluetoothServiceBinder;
    }

    public void setBluetoothAdapter(BluetoothAdapter bluetoothAdapter) {
        this.bluetoothAdapter = bluetoothAdapter;
    }

    public void startDiscoveryOfDevices() {
        devices = new ArrayList<>();
        bluetoothAdapter.startDiscovery();

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
                    devices.add(deviceFound);
                }
            }
        };

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(broadcastReceiver, filter);
        Log.i(TAG, "Receiver registered.");

    }

    public void stopDiscoveryOfDevices() {
        if (bluetoothAdapter.isDiscovering()) {
            this.unregisterReceiver(broadcastReceiver);
            bluetoothStatus = BluetoothStatus.IDLE;
            bluetoothAdapter.cancelDiscovery();
            Log.i(TAG, "Receiver unregistered and discovery stopped.");
        }
    }

    public void findBluetoothDevice(String deviceName) {
        targetDevice = null;
        deviceFound = false;
        new FindBluetoothDeviceTask().execute(deviceName);
    }

    public BluetoothDevice getTargetDevice() {
        return targetDevice;
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

    /* AsyncTask to find devices on devices list */

    private class FindBluetoothDeviceTask extends AsyncTask<String, Integer, BluetoothDevice> {
        @Override
        protected BluetoothDevice doInBackground(String... params) {
            try {
                // Wait for devices list to be updated.
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (devices != null) {
                for (BluetoothDevice device : devices) {
                    if (device.getName().contains(params[0])) {
                        // We found it, so we stop discovering
                        deviceFound = true;
                        stopDiscoveryOfDevices();
                        targetDevice = device;
                        Log.d(TAG, "The device requested has been found.");
                    }
                }
            }
            return targetDevice;
        }

        @Override
        protected void onPostExecute(BluetoothDevice bluetoothDevice) {
            super.onPostExecute(bluetoothDevice);
            if (targetDevice == null) {
                Log.d(TAG, "targetDevice null");
                sendBroadcast(new Intent(MainActivity.INTENT_STRING)
                        .putExtra("intenttype", IntentType.DEVICE_NOT_FOUND));
            } else {
                //Send the device to MainActivity
                Log.d(TAG, "Sending targetDevice to MainActivity");
                sendBroadcast(new Intent(MainActivity.INTENT_STRING)
                        .putExtra("device", bluetoothDevice).putExtra("intenttype",
                                IntentType.DEVICE_FOUND));
            }


        }
    }

}
