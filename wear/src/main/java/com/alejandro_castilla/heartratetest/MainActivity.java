package com.alejandro_castilla.heartratetest;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends WearableActivity {

    private TextView mTextView;
    private ImageButton mBtnStart;
    private ImageButton mBtnPause;
    private BluetoothService mBluetoothService;

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
                        mTextView.setText("Please wait...");
                        mBluetoothService.enableBluetooth();

                    }
                });

            }
        });

        setAmbientEnabled();
        mBluetoothService = new BluetoothService(this);

    }
}
