package com.alejandro_castilla.heartratetest;

import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by alejandrocq on 6/04/16.
 */
public class WearableListItemLayout extends LinearLayout
        implements WearableListView.OnCenterProximityListener {

    private ImageView deviceImg;
    private TextView deviceName;
    private TextView deviceAddress;

    public WearableListItemLayout(Context context) {
        this(context, null);
    }

    public WearableListItemLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public WearableListItemLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    // Get references to the icon and text in the item layout definition
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // These are defined in the layout file for list items
        // (see next section)
        deviceImg = (ImageView) findViewById(R.id.deviceImg);
        deviceName = (TextView) findViewById(R.id.deviceNameText);
        deviceAddress = (TextView) findViewById(R.id.deviceAddressText);
    }

    @Override
    public void onCenterPosition(boolean animate) {
        deviceImg.animate().scaleX(1f).scaleY(1f).alpha(1);
        deviceName.animate().scaleX(1f).scaleY(1f).alpha(1);
        deviceAddress.animate().scaleX(1f).scaleY(1f).alpha(1);
    }

    @Override
    public void onNonCenterPosition(boolean animate) {
        deviceImg.animate().scaleX(0.8f).scaleY(0.8f).alpha(0.6f);
        deviceName.animate().scaleX(0.8f).scaleY(0.8f).alpha(0.6f);
        deviceAddress.animate().scaleX(1f).scaleY(1f).alpha(1);
    }
}
