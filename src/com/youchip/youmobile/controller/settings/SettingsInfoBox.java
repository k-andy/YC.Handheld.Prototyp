package com.youchip.youmobile.controller.settings;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.youchip.youmobile.R;
import com.youchip.youmobile.utils.AlertBox;
import com.youchip.youmobile.utils.SystemInfo;

public class SettingsInfoBox {
    
    
    private static final String MEMORY_UNIT= "MB";

    
    private SettingsInfoBox(){

    }


    public static void showInfoWindow(Context context){
        String deviceIDTitle   = context.getResources().getString(R.string.action_show_device_ip_title);
        String memoryTitle     = context.getResources().getString(R.string.action_show_device_memory_title);
        String appTitle        = context.getResources().getString(R.string.app_version_title);

        String versionName = "";
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionName  =  packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = "?";
            e.printStackTrace();
        }

        String appVersion = versionName;


        String deviceID = ConfigAccess.getDeviceID(context);
        String freeMemory = SystemInfo.getFreeExternalMemory() + " " + MEMORY_UNIT;

        AlertBox.allertOnInfo(context,R.string.app_name,
                appTitle + "\t: " + appVersion + "\n" +
                        deviceIDTitle + "\t: " + deviceID + "\n" +
                        memoryTitle   + "\t: " + freeMemory
        );
    }



}
