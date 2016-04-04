package com.alejandro_castilla.heartratetest;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends WearableActivity {

    private TextView textView;
    private ImageButton btnStart;
    private ImageButton btnPause;

    /* Some declarations in order to manage Bluetooth. */
    private final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter bluetoothAdapter = null;
    private Intent bluetoothServiceIntent;
    private BluetoothService bluetoothService;
    private boolean bound = false;

    /* ServiceConnection declaration to connect to BluetoothService */

    private ServiceConnection mBluetoothServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothService.BluetoothServiceBinder mBluetoothServiceBinder =
                    (BluetoothService.BluetoothServiceBinder) service;
            bluetoothService = mBluetoothServiceBinder.getService();
            bound = true;
            bluetoothService.setBluetoothAdapter(bluetoothAdapter);
            bluetoothService.setToastContext(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
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

                    }
                });

                btnPause.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        btnPause.setVisibility(ImageButton.GONE);
                        btnStart.setVisibility(ImageButton.VISIBLE);
                        bluetoothService.stopDiscoveryOfDevices();
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


    @Override
    protected void onStart() {
        super.onStart();
        enableBluetooth();
        // Finish the application if Bluetooth is not supported.
        if (BluetoothService.bluetoothStatus == BluetoothStatus.NOT_SUPPORTED) {
            finish();
        } else {
            // Start BluetoothService
            bluetoothServiceIntent = new Intent(this, BluetoothService.class);
            bindService(bluetoothServiceIntent, mBluetoothServiceConnection,
                    Context.BIND_AUTO_CREATE);

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        bluetoothService.stopDiscoveryOfDevices();
        unbindService(mBluetoothServiceConnection);
        bound = false;
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
