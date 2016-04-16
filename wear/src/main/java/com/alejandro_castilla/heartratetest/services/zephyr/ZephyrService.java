package com.alejandro_castilla.heartratetest.services.zephyr;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.alejandro_castilla.heartratetest.MainActivity;

import zephyr.android.BioHarnessBT.BTClient;
import zephyr.android.BioHarnessBT.ZephyrProtocol;

/**
 * Created by alejandrocq on 4/04/16.
 */
public class ZephyrService extends Service {

    private static final String TAG = "ZephyrService";
    private final IBinder zephyrServiceBinder = new ZephyrServiceBinder();

    private BluetoothAdapter adapter = null;
    private BluetoothDevice device = null;
    BTClient _bt;
    ZephyrProtocol _protocol;
    NewConnectedListener _NConnListener;

    private Messenger mainActivityMessenger;


    private final int HEART_RATE = 0x100;
    private final int RESPIRATION_RATE = 0x101;
    private final int SKIN_TEMPERATURE = 0x102;
    private final int POSTURE = 0x103;
    private final int PEAK_ACCLERATION = 0x104;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeConnection();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Zephyr Service started.");
        mainActivityMessenger = intent.getParcelableExtra("mainactivitymessenger");
        return zephyrServiceBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public boolean connectToZephyr(BluetoothAdapter adapter, BluetoothDevice device) {
        Log.d(TAG, "Starting connection to Zephyr device.");
        this.device = device;
        this.adapter = adapter;
        _bt = new BTClient(this.adapter, this.device.getAddress());
        _NConnListener = new NewConnectedListener(messageHandler, messageHandler);
        _bt.addConnectedEventListener(_NConnListener);
        if (_bt.IsConnected()) {
            Log.d(TAG, "Connected to Zephyr device.");
            _bt.start();
            return true;
        } else {
            return false;
        }
    }

    public void closeConnection() {
        _bt.removeConnectedEventListener(_NConnListener);
        _bt.Close();
        Log.d(TAG, "Connection to Zephyr device terminated.");
    }


    private final Handler messageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HEART_RATE:
                    String heartRatetext = msg.getData().getString("HeartRate");
                    System.out.println("Heart Rate Info is " + heartRatetext);
                    Message heartRateMsg = Message.obtain(null, MainActivity.HEART_RATE_DATA);
                    Bundle heartRateBundle = new Bundle();
                    heartRateBundle.putString("heartratestring", heartRatetext);
                    heartRateMsg.setData(heartRateBundle);
                    try {
                        mainActivityMessenger.send(heartRateMsg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };



    /* Class used to bind with the client (MainActivity.java) */

    public class ZephyrServiceBinder extends Binder {
        public ZephyrService getService() {
            return ZephyrService.this;
        }
    }


//    private class BTBroadcastReceiver extends BroadcastReceiver {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Log.d("BTIntent", intent.getAction());
//            Bundle b = intent.getExtras();
//            Log.d("BTIntent", b.get("android.bluetooth.device.extra.DEVICE").toString());
//            Log.d("BTIntent", b.get("android.bluetooth.device.extra.PAIRING_VARIANT").toString());
//            try {
//                BluetoothDevice device = adapter.
//                        getRemoteDevice(b.get("android.bluetooth.device.extra.DEVICE").toString());
//                Method m = BluetoothDevice.class.
//                        getMethod("convertPinToBytes", new Class[] {String.class} );
//                byte[] pin = (byte[])m.invoke(device, "1234");
//                m = device.getClass().getMethod("setPin", new Class [] {pin.getClass()});
//                Object result = m.invoke(device, pin);
//                Log.d("BTTest", result.toString());
//            } catch (SecurityException e1) {
//                // TODO Auto-generated catch block
//                e1.printStackTrace();
//            } catch (NoSuchMethodException e1) {
//                // TODO Auto-generated catch block
//                e1.printStackTrace();
//            } catch (IllegalArgumentException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            } catch (IllegalAccessException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            } catch (InvocationTargetException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
//    }
}