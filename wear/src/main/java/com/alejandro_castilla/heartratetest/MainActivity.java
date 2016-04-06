package com.alejandro_castilla.heartratetest;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.alejandro_castilla.heartratetest.services.bluetooth.BluetoothService;
import com.alejandro_castilla.heartratetest.services.bluetooth.BluetoothService.BluetoothStatus;
import com.alejandro_castilla.heartratetest.services.zephyr.ZephyrService;

import java.util.ArrayList;

public class MainActivity extends WearableActivity {

    private static final String TAG = "MainActivity";

    private TextView textView;
    private ImageButton btnStart;
    private ImageButton btnPause;

    /* Some fields used for Bluetooth. */
    private final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothService bluetoothService;
    private ArrayList<BluetoothDevice> devices;
    private BluetoothDevice targetDevice;
    private boolean connectedToDevice = false;

    /* Some fields used for Zephyr Service */
    private ZephyrService zephyrService = null;
    private boolean zephyrServiceBinded = false;

    private BroadcastReceiver broadcastReceiver = null;
    private boolean broadcastReceiverRegistered = false;
    private BroadcastReceiver heartRateBroadcastReceiver = null;
    private boolean heartRateBroadcastReceiverRegistered = false;

    /* ServiceConnection declaration to connect to BluetoothService */

    private ServiceConnection bluetoothServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothService.BluetoothServiceBinder bluetoothServiceBinder =
                    (BluetoothService.BluetoothServiceBinder) service;
            bluetoothService = bluetoothServiceBinder.getService();
            bluetoothService.setBluetoothAdapter(bluetoothAdapter);
            bluetoothService.setToastContext(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "BluetoothService disconnected.");
        }
    };

    /* ServiceConnection declaration to connect to ZephyrService */

    private ServiceConnection zephyrServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ZephyrService.ZephyrServiceBinder zephyrServiceBinder =
                    (ZephyrService.ZephyrServiceBinder) service;
            zephyrService = zephyrServiceBinder.getService();
            connectedToDevice=zephyrService.connectToZephyr(bluetoothAdapter, targetDevice);
            if (connectedToDevice) {
                textView.setText("Connected to Zephyr");
                receiveHeartRateData();
            }
            zephyrServiceBinded = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                textView = (TextView) stub.findViewById(R.id.heartRateText);
                btnStart = (ImageButton) stub.findViewById(R.id.btnStart);
                btnPause = (ImageButton) stub.findViewById(R.id.btnPause);

                btnStart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        btnStart.setVisibility(ImageButton.GONE);
                        btnPause.setVisibility(ImageButton.VISIBLE);
                        textView.setText("Discovering...");
                        bluetoothService.startDiscoveryOfDevices();
                        bluetoothService.findBluetoothDevice("BH");
                        getBluetoothDeviceFromService();


                    }
                });

                btnPause.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        btnPause.setVisibility(ImageButton.GONE);
                        btnStart.setVisibility(ImageButton.VISIBLE);
                        bluetoothService.stopDiscoveryOfDevices();
                        stopZephyrService();
                        textView.setText("Idle");
                    }
                });

            }
        });

        setAmbientEnabled();

    }

    public void enableBluetooth() {

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "This device doesn't support Bluetooth.",
                    Toast.LENGTH_LONG).show();
            BluetoothService.bluetoothStatus = BluetoothStatus.NOT_SUPPORTED;
        } else if (!bluetoothAdapter.isEnabled()) {
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT);
        } else {
            Toast.makeText(this, "Bluetooth is already enabled.", Toast.LENGTH_LONG)
                    .show();
            BluetoothService.bluetoothStatus = BluetoothStatus.IDLE;
        }

    }

    public void getBluetoothDeviceFromService() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                targetDevice = intent.getParcelableExtra("device");
                boolean deviceFound = intent.getBooleanExtra("foundboolean", false);
                if (targetDevice == null || !deviceFound) {
                    Log.d(TAG, "Received a null device");
                    bluetoothService.stopDiscoveryOfDevices();
                    btnPause.setVisibility(ImageButton.GONE);
                    btnStart.setVisibility(ImageButton.VISIBLE);
                    Toast.makeText(MainActivity.this, "Device not found. Try again...",
                            Toast.LENGTH_LONG).show();
                    textView.setText("Idle");
                } else {
                    Log.d(TAG, "Device received on MainActivity: " + targetDevice.getName());
                    textView.setText("Device found!");
                    startZephyrService();
                }
            }
        };
        IntentFilter filter = new IntentFilter("targetdevice");
        this.registerReceiver(broadcastReceiver, filter);
        broadcastReceiverRegistered = true;
        Log.d(TAG, "Broadcast Receiver for device has been registered.");
    }

    public void startZephyrService() {
        this.unregisterReceiver(broadcastReceiver);
        broadcastReceiverRegistered = false;
        Intent zephyrServiceIntent = new Intent(this, ZephyrService.class);
        bindService(zephyrServiceIntent, zephyrServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    public void stopZephyrService() {
        zephyrService.closeConnection();
        this.unregisterReceiver(heartRateBroadcastReceiver);
        heartRateBroadcastReceiverRegistered = false;
        unbindService(zephyrServiceConnection);
        zephyrServiceBinded = false;

    }

    private void receiveHeartRateData() {
        heartRateBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "Heart Rate broadcast received.");
                String heartrate = intent.getStringExtra("heartratestring");
                textView.setText(heartrate);
            }
        };

        IntentFilter heartRatefilter = new IntentFilter("heartrate");
        this.registerReceiver(heartRateBroadcastReceiver, heartRatefilter);
        heartRateBroadcastReceiverRegistered = true;
        Log.d(TAG, "Broadcast Receiver for heart rate has been registered.");
    }

    @Override
    protected void onStart() {
        super.onStart();
        enableBluetooth();
        // Finish the application if Bluetooth is not supported.
        if (BluetoothService.bluetoothStatus == BluetoothStatus.NOT_SUPPORTED) {
            finish();
        } else {
            // Start BluetoothService
            Intent bluetoothServiceIntent = new Intent(this, BluetoothService.class);
            bindService(bluetoothServiceIntent, bluetoothServiceConnection,
                    Context.BIND_AUTO_CREATE);

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        bluetoothService.stopDiscoveryOfDevices();
        unbindService(bluetoothServiceConnection);
        if (zephyrServiceBinded) {
            unbindService(zephyrServiceConnection);
        }
        if (broadcastReceiverRegistered) {
            this.unregisterReceiver(broadcastReceiver);
        }
        if (heartRateBroadcastReceiverRegistered) {
            this.unregisterReceiver(heartRateBroadcastReceiver);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth has been enabled.", Toast.LENGTH_LONG).show();
                    BluetoothService.bluetoothStatus = BluetoothStatus.IDLE;
                } else {
                    Toast.makeText(this, "Error while activating Bluetooth.",
                            Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
        }
    }
}
