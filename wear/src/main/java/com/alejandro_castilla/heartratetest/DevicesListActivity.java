package com.alejandro_castilla.heartratetest;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
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
    private Messenger mainActivityMessenger;
    private String[] devicesName = { "SensorTag", "BH" };

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
                    listView.setAdapter(new WearableListViewAdapter(context, devicesName));
                    listView.setClickListener((WearableListView.ClickListener) context);
                    mainActivityMessenger = getIntent()
                            .getParcelableExtra("mainactivitymessenger");
                }
            }
        });
    }

//    public void listenForDevices() {
//        broadcastReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                BluetoothDevice deviceReceived = intent.getParcelableExtra("device");
//                listView.setAdapter(new WearableListViewAdapter(context, deviceReceived));
//                listView.setClickListener((WearableListView.ClickListener) context);
//                listTitleText.setText("Devices found");
//                Log.d(TAG, "listView adapter set.");
//            }
//        };
//
//        IntentFilter filter = new IntentFilter("devicetolist");
//        context.registerReceiver(broadcastReceiver, filter);
//
//    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        Toast.makeText(context, "Clic on device listened.", Toast.LENGTH_LONG);
        Log.d(TAG, "Clic on device.");
        int position = viewHolder.getAdapterPosition();
        Message msg = Message.obtain(null, MainActivity.TARGET_DEVICE);
        Bundle bundle = new Bundle();
        bundle.putString("deviceselected", devicesName[position]);
        msg.setData(bundle);
        try {
            mainActivityMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        finish();
    }

    @Override
    public void onTopEmptyRegionClick() {

    }

    @Override
    protected void onStop() {
        super.onStop();
//        context.unregisterReceiver(broadcastReceiver);
    }
}
