package com.youchip.youmobile.controller;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.widget.Toast;

import com.youchip.youmobile.R;
import com.youchip.youmobile.controller.SelectActionActivity;
import com.youchip.youmobile.utils.SystemInfo;

/**
 * Created by muelleco on 04.07.2014.
 */
public class AppStateChecker {

    private static boolean appOk = true;
    private static final String NOT_ENOUGH_MEMORY_MESSAGE = "No memory left for logging, please contact the technical support";
    private final Context context;

    public AppStateChecker(Context context){
        this.context = context;
    }

    /**
     * Checks if all the preconditions for an error-free working mode are fullfilled
     * @return true if the app may run correctly, else false
     */
    public static boolean checkAppState(){
        if (SystemInfo.getFreeExternalMemory() > 10){
            appOk = false;
            return true;
        } else {
            appOk = true;
            return false;
        }
    }

    /**
     * Returns a streing which explains the app mode
     * @return an empty string if everything is fine, else an explanation about the state.
     */

    public String getAppStateMessage(){
        if (appOk)
            return NOT_ENOUGH_MEMORY_MESSAGE;
        else
            return "";
    }


    public void showDisableMessage(){
        Toast toast = Toast.makeText(context, getAppStateMessage(), Toast.LENGTH_LONG);
        toast.show();
    }

    public void disableApp(){
        Intent intent = new Intent(context, SelectActionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        this.context.startActivity(intent);
    }

    public void showWiFiSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.hint_wifi_disabled);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                context.startActivity(intent);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(context, R.string.hint_sync_not_possible, Toast.LENGTH_SHORT).show();
            }
        });
        builder.create().show();
        return;
    }
}
