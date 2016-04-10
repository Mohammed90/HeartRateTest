package com.alejandro_castilla.heartratetest;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class DevicesListActivity extends Activity implements WearableListView.ClickListener {

    private final String TAG = "DevicesListActivity";
    private WearableListView listView;
    private TextView listTitleText;
    private Context context = this;
    private BroadcastReceiver broadcastReceiver;
//    private String[] elements = { "Redmi", "BH" };

@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices_list);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                listView = (WearableListView) stub.findViewById(R.id.wearable_list);
                listTitleText = (TextView) stub.findViewById(R.id.listTitle);
                Log.d(TAG, "listView created.");
                if (listView == null) {
                    Log.d("DevicesListActivity", "ListView is null");
                } else {
                    listenForDevices();
                }
            }
        });
    }

    public void listenForDevices() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                BluetoothDevice deviceReceived = intent.getParcelableExtra("device");
                listView.setAdapter(new WearableListViewAdapter(context, deviceReceived));
                listView.setClickListener((WearableListView.ClickListener) context);
                listTitleText.setText("Devices found");
                Log.d(TAG, "listView adapter set.");
            }
        };

        IntentFilter filter = new IntentFilter("devicetolist");
        context.registerReceiver(broadcastReceiver, filter);

    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        Toast.makeText(this, "Clic realizado", Toast.LENGTH_LONG);
    }

    @Override
    public void onTopEmptyRegionClick() {

    }

    @Override
    protected void onStop() {
        super.onStop();
        context.unregisterReceiver(broadcastReceiver);
    }
}
