package com.youchip.youmobile.controller.chipIO;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RfidBroadCastReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
//        intent.getStringExtra("idatachina.SCAN_DATA")
        Log.d("RfidBroadCastReceiver", "Received RFID broadcast.");
    }

}
