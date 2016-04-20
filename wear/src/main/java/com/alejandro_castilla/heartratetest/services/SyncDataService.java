package com.alejandro_castilla.heartratetest.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by alejandrocq on 17/04/16.
 */
public class SyncDataService extends Service implements
        DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private final String TAG = SyncDataService.class.getSimpleName();
    private final IBinder syncDataServiceBinder = new SyncDataServiceBinder();

    GoogleApiClient googleApiClient;


    public void syncHeartRateData(String heartRateString) {
        PutDataMapRequest putHeartRateMapReq = PutDataMapRequest.create("/heartrate");
        putHeartRateMapReq.getDataMap().putString("heartratestring", heartRateString);
        putHeartRateMapReq.getDataMap().putLong("timestamp", System.currentTimeMillis());
        PutDataRequest putHeartRateReq = putHeartRateMapReq.asPutDataRequest();
        putHeartRateReq.setUrgent();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(googleApiClient, putHeartRateReq);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        googleApiClient.connect(); //Connect to the google API
        return syncDataServiceBinder;
    }

    @Override
    public void onDestroy() {
        Wearable.DataApi.removeListener(googleApiClient, this);
        googleApiClient.disconnect();
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onConnected(@Nullable Bundle connectionHint) {
        Log.d(TAG, "onConnected: " + connectionHint);
        //TODO Actions when the connection is established
        Wearable.DataApi.addListener(googleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "onConnectionSuspended: " + cause);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        //Nothing to do here for now.

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: " + connectionResult);
    }

    /* Class used to bind with the client (MainActivity.java) */

    public class SyncDataServiceBinder extends Binder {
        public SyncDataService getService() {
            return SyncDataService.this;
        }
    }
}
