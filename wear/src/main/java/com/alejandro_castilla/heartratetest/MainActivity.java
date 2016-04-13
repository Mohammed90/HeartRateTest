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

public class MainActivity extends WearableActivity {

    private static final String TAG = "MainActivity";
    public static final String INTENT_STRING = "heartratetestapp";

    /* Enum for intents */
    public enum IntentType {
        TARGET_DEVICE, DEVICE_FOUND, DEVICE_NOT_FOUND, HEART_RATE_DATA
    }

    /* Layout items */
    private TextView textView;
    private ImageButton btnStart;
    private ImageButton btnPause;

    /* Some fields used for Bluetooth. */
    private final int REQUEST_ENABLE_BT = 1;
    private String deviceSelected;
    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothService bluetoothService;
    private BluetoothDevice targetDevice;
    private boolean connectedToDevice = false;

    /* Some fields used for Zephyr Service */
    private ZephyrService zephyrService = null;
    private boolean zephyrServiceBinded = false;

    /* BroadcastReceiver fields to handle intents from services and other activities */
    private BroadcastReceiver broadcastReceiver = null;
    private boolean broadcastReceiverRegistered = false;
    private final IntentFilter filter = new IntentFilter(INTENT_STRING);

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
            }
            zephyrServiceBinded = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "Zephyr Service disconnected.");
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
                        Intent intent = new Intent (MainActivity.this, DevicesListActivity.class);
                        startActivity(intent);
//                        textView.setText("Discovering...");
//                        bluetoothService.startDiscoveryOfDevices();
//                        bluetoothService.findBluetoothDevice("BH");
//                        Intent intent = new Intent (MainActivity.this, DevicesListActivity.class);
//                        startActivity(intent);


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

    public void startBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("TAG", "Intent received");
                IntentType intentType = (IntentType) intent.getSerializableExtra("intenttype");
                switch (intentType) {
                    case TARGET_DEVICE:
                        Log.d(TAG, "Target device string received.");
                        deviceSelected = intent.getStringExtra("deviceselected");
                        bluetoothService.startDiscoveryOfDevices();
                        bluetoothService.findBluetoothDevice(deviceSelected);
                        textView.setText("Looking for device...");
                        break;
                    case DEVICE_FOUND:
                        targetDevice = intent.getParcelableExtra("device");
                        Log.d(TAG, "Device received on MainActivity: " + targetDevice.getName());
                        textView.setText("Device found!");

                        switch (deviceSelected) {
                            case "BH":
                                startZephyrService();
                                break;
                            // TODO more devices
                        }

                        break;
                    case HEART_RATE_DATA:
                        Log.d(TAG, "Heart Rate broadcast received.");
                        String heartrate = intent.getStringExtra("heartratestring");
                        textView.setText(heartrate);
                        break;
                    case DEVICE_NOT_FOUND:
                        Log.d(TAG, "Received a null device");
                        bluetoothService.stopDiscoveryOfDevices();
                        btnPause.setVisibility(ImageButton.GONE);
                        btnStart.setVisibility(ImageButton.VISIBLE);
                        Toast.makeText(MainActivity.this, "Device not found. Try again...",
                                Toast.LENGTH_LONG).show();
                        textView.setText("Idle");
                        break;
                    default:
                        Log.d(TAG, "Error on intent");
                        break;
                }
            }
        };
        this.registerReceiver(broadcastReceiver, filter);
        broadcastReceiverRegistered = true;
        Log.d(TAG, "Broadcast Receiver for device has been registered.");
    }

    public void startZephyrService() {
        Intent zephyrServiceIntent = new Intent(this, ZephyrService.class);
        bindService(zephyrServiceIntent, zephyrServiceConnection,
                Context.BIND_AUTO_CREATE);
        zephyrServiceBinded = true;
    }

    public void stopZephyrService() {
        zephyrService.closeConnection();
        unbindService(zephyrServiceConnection);
        zephyrServiceBinded = false;

    }

    @Override
    protected void onStart() {
        super.onStart();
        startBroadcastReceiver();
        enableBluetooth();
        // Finish the application if Bluetooth is not supported.
        if (BluetoothService.bluetoothStatus == BluetoothStatus.NOT_SUPPORTED) {
            finish();
        } else {
            // Start BluetoothService
            Log.d(TAG, "Starting Bluetooth Service");
            Intent bluetoothServiceIntent = new Intent(this, BluetoothService.class);
            bindService(bluetoothServiceIntent, bluetoothServiceConnection,
                    Context.BIND_AUTO_CREATE);

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "Broadcast Receiver status: " + broadcastReceiverRegistered);
        bluetoothService.stopDiscoveryOfDevices();
        unbindService(bluetoothServiceConnection);
        if (zephyrServiceBinded) {
            unbindService(zephyrServiceConnection);
        }
        if (broadcastReceiverRegistered) {
            Log.d(TAG, "BroadcastReceiver unregistered - onStop()");
            this.unregisterReceiver(broadcastReceiver);
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
