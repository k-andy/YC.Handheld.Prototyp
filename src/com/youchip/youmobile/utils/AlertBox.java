package com.youchip.youmobile.utils;

import com.youchip.youmobile.R;
import com.youchip.youmobile.controller.settings.ConfigAccess;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;

public class AlertBox {

    private static AlertDialog dialogBox = null;

    public static void dismiss(){
        if (dialogBox != null){
            dialogBox.dismiss();
        }
    }

    /**
     * Send error message if something failed
     * @param message
     */
    public static void allertOnWarning(final Context context, final int titleResource, final CharSequence message, OnClickListener onClick){

        if ( context instanceof Activity) {
            Activity activity = (Activity) context;
            if (!activity.isFinishing()) {

                // Creating alert Dialog with one Button
                Builder alertDialog = new AlertDialog.Builder(context);
                alertDialog.setTitle(context.getResources().getText(titleResource));
                alertDialog.setMessage(message);
                alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
                alertDialog.setPositiveButton(android.R.string.ok, onClick);
                dialogBox = alertDialog.show();
            }
        }
    }

    
    public static void allertOnWarning(final Context context, final int titleResource, final int messageResource, OnClickListener onClick){
        // Creating alert Dialog with one Button
        allertOnWarning(context,titleResource,context.getResources().getText(messageResource), onClick);
    }
   
    
    public static void allertOnWarning(final Context context, final int titleResource, final int messageResource){
       allertOnWarning(context, titleResource, messageResource, null);
    }
    
    
    public static void allertOnWarning(final Context context, final int titleResource, final CharSequence message){
        allertOnWarning(context, titleResource, message, null);
    }
    
    


    
    
    
    /**
     * Send error message if something failed
     * @param message
     */
    public static void allertOnInfo(final Context context, final int titleResource, final CharSequence message, OnClickListener onClick){
        if ( context instanceof Activity) {
            Activity activity = (Activity) context;
            if (!activity.isFinishing()) {
                // Creating alert Dialog with one Button
                Builder alertDialog = new AlertDialog.Builder(context);
                alertDialog.setTitle(context.getResources().getText(titleResource));
                alertDialog.setMessage(message);
                alertDialog.setIcon(android.R.drawable.ic_dialog_info);
                alertDialog.setPositiveButton(android.R.string.ok, onClick);
                dialogBox = alertDialog.show();
            }
        }
    }

    
    public static void allertOnInfo(final Context context, final int titleResource, final int messageResource, OnClickListener onClick){
        // Creating alert Dialog with one Button
        allertOnInfo(context,titleResource,context.getResources().getText(messageResource), onClick);
    }
   
    
    public static void allertOnInfo(final Context context, final int titleResource, final int messageResource){
        allertOnInfo(context, titleResource, messageResource, null);
    }
    
    
    public static void allertOnInfo(final Context context, final int titleResource, final CharSequence message){
        allertOnInfo(context, titleResource, message, null);
    }
    
    public static void allertOnRequest(final Context context, final int titleResource, final int messageResource, OnClickListener onClick){
        // Creating alert Dialog with one Button
        allertOnRequest(context, titleResource, context.getResources().getText(messageResource), onClick);
    }
    
    public static void allertOnRequest(final Context context, final int titleResource, final CharSequence message, OnClickListener onClick){

        if ( context instanceof Activity) {
            Activity activity = (Activity) context;
            if (!activity.isFinishing()) {
                // Creating alert Dialog with one Button
                Builder alertDialog = new AlertDialog.Builder(context);
                alertDialog.setTitle(context.getResources().getText(titleResource));
                alertDialog.setMessage(message);
                alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
                alertDialog.setPositiveButton(android.R.string.ok, onClick);
                alertDialog.setNegativeButton(android.R.string.cancel, onClick);
                dialogBox = alertDialog.show();
            }
        }
    }


   
}
