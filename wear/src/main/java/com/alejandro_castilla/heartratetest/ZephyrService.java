package com.alejandro_castilla.heartratetest;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import zephyr.android.BioHarnessBT.BTClient;
import zephyr.android.BioHarnessBT.ConnectListenerImpl;
import zephyr.android.BioHarnessBT.ConnectedEvent;
import zephyr.android.BioHarnessBT.PacketTypeRequest;
import zephyr.android.BioHarnessBT.ZephyrPacketArgs;
import zephyr.android.BioHarnessBT.ZephyrPacketEvent;
import zephyr.android.BioHarnessBT.ZephyrPacketListener;
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
            Log.d (TAG, "Connected to Zephyr device.");
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
        public void handleMessage (Message msg) {
            switch (msg.what) {
                case HEART_RATE:
                    String HeartRatetext = msg.getData().getString("HeartRate");
                    System.out.println("Heart Rate Info is " + HeartRatetext);
                    Intent heartRateIntent = new Intent();
                    sendBroadcast(new Intent("heartrate")
                            .putExtra("heartratestring", HeartRatetext));
                    Log.d(TAG, "Broadcast with Heart Rate sent.");
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


    /**************************************************************************************
     Listener used to handle Zephyr packets. It has been written by Zephyr Technology.
     *************************************************************************************/

    private class NewConnectedListener extends ConnectListenerImpl {

        private Handler _OldHandler;
        private Handler _aNewHandler;
        final int GP_MSG_ID = 0x20;
        final int BREATHING_MSG_ID = 0x21;
        final int ECG_MSG_ID = 0x22;
        final int RtoR_MSG_ID = 0x24;
        final int ACCEL_100mg_MSG_ID = 0x2A;
        final int SUMMARY_MSG_ID = 0x2B;


        private int GP_HANDLER_ID = 0x20;

        private final int HEART_RATE = 0x100;
        private final int RESPIRATION_RATE = 0x101;
        private final int SKIN_TEMPERATURE = 0x102;
        private final int POSTURE = 0x103;
        private final int PEAK_ACCLERATION = 0x104;
        /*Creating the different Objects for different types of Packets*/
        private GeneralPacketInfo GPInfo = new GeneralPacketInfo();
        private ECGPacketInfo ECGInfoPacket = new ECGPacketInfo();
        private BreathingPacketInfo BreathingInfoPacket = new  BreathingPacketInfo();
        private RtoRPacketInfo RtoRInfoPacket = new RtoRPacketInfo();
        private AccelerometerPacketInfo AccInfoPacket = new AccelerometerPacketInfo();
        private SummaryPacketInfo SummaryInfoPacket = new SummaryPacketInfo();

        private PacketTypeRequest RqPacketType = new PacketTypeRequest();

        public NewConnectedListener(Handler handler,Handler _NewHandler) {
            super(handler, null);
            _OldHandler= handler;
            _aNewHandler = _NewHandler;

            // TODO Auto-generated constructor stub

        }

        public void Connected(ConnectedEvent<BTClient> eventArgs) {
            System.out.println(String.format("Connected to BioHarness %s.",
                    eventArgs.getSource().getDevice().getName()));
		/*Use this object to enable or disable the different Packet types*/
            RqPacketType.GP_ENABLE = true;
            RqPacketType.BREATHING_ENABLE = true;
            RqPacketType.LOGGING_ENABLE = true;


            //Creates a new ZephyrProtocol object and passes it the BTComms object
            ZephyrProtocol _protocol = new ZephyrProtocol(eventArgs.getSource().getComms(),
                    RqPacketType);
            //ZephyrProtocol _protocol = new ZephyrProtocol(eventArgs.getSource().getComms(), );
            _protocol.addZephyrPacketEventListener(new ZephyrPacketListener() {
                public void ReceivedPacket(ZephyrPacketEvent eventArgs) {
                    ZephyrPacketArgs msg = eventArgs.getPacket();
                    byte CRCFailStatus;
                    byte RcvdBytes;



                    CRCFailStatus = msg.getCRCStatus();
                    RcvdBytes = msg.getNumRvcdBytes() ;
                    int MsgID = msg.getMsgID();
                    byte [] DataArray = msg.getBytes();
                    switch (MsgID)
                    {

                        case GP_MSG_ID:





                            //***************Displaying the Heart Rate******************************
                            int HRate =  GPInfo.GetHeartRate(DataArray);
                            Message text1 = _aNewHandler.obtainMessage(HEART_RATE);
                            Bundle b1 = new Bundle();
                            b1.putString("HeartRate", String.valueOf(HRate));
                            text1.setData(b1);
                            _aNewHandler.sendMessage(text1);
                            System.out.println("Heart Rate is "+ HRate);

                            //***************Displaying the Respiration Rate************************
                            double RespRate = GPInfo.GetRespirationRate(DataArray);

                            text1 = _aNewHandler.obtainMessage(RESPIRATION_RATE);
                            b1.putString("RespirationRate", String.valueOf(RespRate));
                            text1.setData(b1);
                            _aNewHandler.sendMessage(text1);
                            System.out.println("Respiration Rate is "+ RespRate);

                            //***************Displaying the Skin Temperature************************


                            double SkinTempDbl = GPInfo.GetSkinTemperature(DataArray);
                            text1 = _aNewHandler.obtainMessage(SKIN_TEMPERATURE);
                            //Bundle b1 = new Bundle();
                            b1.putString("SkinTemperature", String.valueOf(SkinTempDbl));
                            text1.setData(b1);
                            _aNewHandler.sendMessage(text1);
                            System.out.println("Skin Temperature is "+ SkinTempDbl);

                            //***************Displaying the Posture*********************************

                            int PostureInt = GPInfo.GetPosture(DataArray);
                            text1 = _aNewHandler.obtainMessage(POSTURE);
                            b1.putString("Posture", String.valueOf(PostureInt));
                            text1.setData(b1);
                            _aNewHandler.sendMessage(text1);
                            System.out.println("Posture is "+ PostureInt);
                            //***************Displaying the Peak Acceleration***********************

                            double PeakAccDbl = GPInfo.GetPeakAcceleration(DataArray);
                            text1 = _aNewHandler.obtainMessage(PEAK_ACCLERATION);
                            b1.putString("PeakAcceleration", String.valueOf(PeakAccDbl));
                            text1.setData(b1);
                            _aNewHandler.sendMessage(text1);
                            System.out.println("Peak Acceleration is "+ PeakAccDbl);

                            byte ROGStatus = GPInfo.GetROGStatus(DataArray);
                            System.out.println("ROG Status is "+ ROGStatus);

                            break;
                        case BREATHING_MSG_ID:
					/*Do what you want. Printing Sequence Number for now*/
                            System.out.println("Breathing Packet Sequence Number is "
                                    +BreathingInfoPacket.GetSeqNum(DataArray));
                            break;
                        case ECG_MSG_ID:
					/*Do what you want. Printing Sequence Number for now*/
                            System.out.println("ECG Packet Sequence Number is "
                                    +ECGInfoPacket.GetSeqNum(DataArray));
                            break;
                        case RtoR_MSG_ID:
					/*Do what you want. Printing Sequence Number for now*/
                            System.out.println("R to R Packet Sequence Number is "
                                    +RtoRInfoPacket.GetSeqNum(DataArray));
                            break;
                        case ACCEL_100mg_MSG_ID:
					/*Do what you want. Printing Sequence Number for now*/
                            System.out.println("Accelerometry Packet Sequence Number is "
                                    +AccInfoPacket.GetSeqNum(DataArray));
                            break;
                        case SUMMARY_MSG_ID:
					/*Do what you want. Printing Sequence Number for now*/
                            System.out.println("Summary Packet Sequence Number is "
                                    +SummaryInfoPacket.GetSeqNum(DataArray));
                            break;

                    }
                }
            });
        }

    }

}