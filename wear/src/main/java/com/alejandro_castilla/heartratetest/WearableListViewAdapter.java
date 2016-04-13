package com.alejandro_castilla.heartratetest;

import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class WearableListViewAdapter extends WearableListView.Adapter {

    private final String TAG = "WearableListViewAdapter";

    private String[] deviceName;
    private String deviceAddress = "Not being used.";
    private final Context context;
    private final LayoutInflater inflater;

    public WearableListViewAdapter(Context context, String[] devices) {
//        Log.d(TAG, "WearableListViewAdapter constructor accessed.");
        this.deviceName = devices;
//        this.deviceAddress = device;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
//        Log.d(TAG, "onBindViewHolder");
        ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
        TextView deviceNameText = itemViewHolder.deviceNameText;
        TextView deviceAddressText = itemViewHolder.deviceAddressText;
        deviceNameText.setText(deviceName[position]);
        deviceAddressText.setText(deviceAddress);

        holder.itemView.setTag(position);
        Log.d(TAG, "onBindViewHolder");
    }

    @Override
    public int getItemCount() {
        return deviceName.length;
    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        Log.d(TAG, "onCreateViewHolder");
        return new ItemViewHolder(inflater.inflate(R.layout.list_item, null));
    }

    public static class ItemViewHolder extends WearableListView.ViewHolder {
        private TextView deviceNameText;
        private TextView deviceAddressText;

        public ItemViewHolder (View itemView) {
            super(itemView);
//            Log.d("ItemViewHolder", "ItemViewHolder constructor");
            deviceNameText = (TextView) itemView.findViewById(R.id.deviceNameText);
            deviceAddressText = (TextView) itemView.findViewById(R.id.deviceAddressText);
        }

    }

}
