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

    private TextView mTextView;
    private ImageButton mBtnStart;
    private ImageButton mBtnPause;

    /* Some declarations in order to manage Bluetooth. */
    private final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter mBluetoothAdapter = null;
    private Intent mBluetoothServiceIntent;
    private BluetoothService mBluetoothService;
    private boolean mBound = false;

    /* ServiceConnection declaration to connect to BluetoothService */

    private ServiceConnection mBluetoothServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothService.BluetoothServiceBinder mBluetoothServiceBinder =
                    (BluetoothService.BluetoothServiceBinder) service;
            mBluetoothService = mBluetoothServiceBinder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };


    public void enableBluetooth() {

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "This device doesn't support Bluetooth.",
                    Toast.LENGTH_LONG).show();
            BluetoothService.mBluetoothStatus = BluetoothStatus.NOT_SUPPORTED;
        } else if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT);
        } else {
            Toast.makeText(this, "Bluetooth is already enabled.", Toast.LENGTH_LONG)
                    .show();
            BluetoothService.mBluetoothStatus = BluetoothStatus.IDLE;
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.heartRateText);
                mBtnStart = (ImageButton) stub.findViewById(R.id.btnStart);
                mBtnPause = (ImageButton) stub.findViewById(R.id.btnPause);

                mBtnStart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mBtnStart.setVisibility(ImageButton.GONE);
                        mBtnPause.setVisibility(ImageButton.VISIBLE);
                        mBluetoothService.setmBluetoothAdapter(mBluetoothAdapter);
                        mBluetoothService.startDiscoveryOfDevices();

                    }
                });

            }
        });

        setAmbientEnabled();

    }


    @Override
    protected void onStart() {
        super.onStart();
        enableBluetooth();
        // Finish the application if Bluetooth is not supported.
        if (BluetoothService.mBluetoothStatus == BluetoothStatus.NOT_SUPPORTED) {
            finish();
        } else {
            // Start BluetoothService
            mBluetoothServiceIntent = new Intent(this, BluetoothService.class);
            bindService(mBluetoothServiceIntent, mBluetoothServiceConnection,
                    Context.BIND_AUTO_CREATE);

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBluetoothService.stopDiscoveryOfDevices();
        unbindService(mBluetoothServiceConnection);
        mBound=false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth has been enabled.", Toast.LENGTH_LONG).show();
                    BluetoothService.mBluetoothStatus = BluetoothStatus.IDLE;
                } else {
                    Toast.makeText(this, "Error while activating Bluetooth.",
                            Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
        }
    }
}
