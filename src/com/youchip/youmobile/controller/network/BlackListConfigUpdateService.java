package com.youchip.youmobile.controller.network;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.youchip.youmobile.R;
import com.youchip.youmobile.controller.network.serviceCaller.WebServiceCall;
import com.youchip.youmobile.controller.network.serviceInterface.ConfigUpdateService;
import com.youchip.youmobile.controller.network.serviceInterface.SOAPRequest;
import com.youchip.youmobile.controller.settings.ConfigAccess;
import com.youchip.youmobile.controller.txlog.TxLogger;
import com.youchip.youmobile.model.gate.BlockedChip;
import com.youchip.youmobile.model.network.request.BlackListConfigSOAPRequest;
import com.youchip.youmobile.model.network.request.ConfigSOAPRequest;
import com.youchip.youmobile.model.network.response.BlackListConfigSOAPResponse;
import com.youchip.youmobile.utils.DataConverter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class BlackListConfigUpdateService implements ConfigUpdateService {

    public static final String BLACKLIST_UPDATED_INTENT = "com.youchip.youmobile.service.blacklist.updated"; 
    
    private final static String CURRENT_CLASS = BlackListConfigUpdateService.class.getName();
    private static final String blackListSuccessMsg = "Blacklist received.";
    private static final WebServiceCall serviceCall = new WebServiceCall();
    private final TxLogger txLogger;
    private final Context context;
    
    
    private volatile long eventID = 0;
    private volatile String deviceID = "";
    private volatile boolean isRunning = false;


   private boolean networkError = false;

    private final int NOTIFY_ID = 2;
    private final NotificationManager mNotificationManager;

    public BlackListConfigUpdateService(Context context, TxLogger txLogger) {
        this.context = context;
        this.txLogger = txLogger;

        this.mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void update() {
        this.isRunning = true;
        notifyStartSync();

        this.networkError = false;

        Log.d(CURRENT_CLASS, "Preparing blacklist update..");

        final SOAPRequest basicRequest = new ConfigSOAPRequest(new BlackListConfigSOAPRequest(), deviceID, eventID);
        final BlackListConfigSOAPResponse basicResponse = new BlackListConfigSOAPResponse();

        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {

                boolean result;

                Log.d(CURRENT_CLASS, "Start loading blacklist data from web service");
                try {
                    result = serviceCall.callService(basicRequest, basicResponse);
                    if (!result) {
                        return false;
                    }
                    Log.d(CURRENT_CLASS, "Finished loading blacklist data from web service");
                    txLogger.status(blackListSuccessMsg);
                    List<BlockedChip> config = basicResponse.getResult();
                    ConfigAccess.storeBlackList(context, config);
                } catch (IllegalArgumentException iae) {
                    Log.w(CURRENT_CLASS, "Failed with error!", iae);
                    return false;
                } catch (java.lang.ClassCastException cce) {
                    Log.w(CURRENT_CLASS,
                            "Error in blacklist configuration! Please check for correctness.");
                    return false;
                } catch (MalformedURLException mue) {
                    Log.w(CURRENT_CLASS, "No valid service URL!");
                    return false;
                } catch(SocketTimeoutException ste){
                    Log.w(CURRENT_CLASS, "Failed to connect to service!" + ste.getMessage());
                    BlackListConfigUpdateService.this.networkError = true;
                    return false;
                } catch (java.net.SocketException ce) {
                    Log.w(CURRENT_CLASS, "Failed to connect to service!", ce);
                    BlackListConfigUpdateService.this.networkError = true;
                    return false;
                } catch (IOException e) {
                    Log.w(CURRENT_CLASS, "Webservice request failed! " + e.getMessage());
                } catch (Exception e) {
                    Log.w(CURRENT_CLASS, "Failed with error!", e);
                    return false;
                }

                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (result && !BlackListConfigUpdateService.this.networkError)  {
                    Log.d(CURRENT_CLASS, "Blacklist successfully updated");
                   
                    Intent intent = new Intent(BLACKLIST_UPDATED_INTENT);
                    Log.d(CURRENT_CLASS, "Sending blacklist update broadcast..");
                    context.sendBroadcast(intent);
                    notifyFinishedSync(generateNotifyMessage(true) + generateNotifySubMessage());
                }else if (BlackListConfigUpdateService.this.networkError){
                    Log.w(CURRENT_CLASS, "Connection failed.");
                    notifyFinishedSync(generateNotifyConnectionFailedMessage() + generateNotifySubMessage());
                } else {
                    Log.w(CURRENT_CLASS, "Failed to update blacklist!");
                    notifyFinishedSync(generateNotifyMessage(false) + generateNotifySubMessage());
                }

                BlackListConfigUpdateService.this.isRunning = false;
            }
        };

        task.execute(new Void[0]);
    }

    public String getServiceUrl() {
        return serviceCall.getServiceURL();
    }

    public long getEventID() {
        return eventID;
    }

    public void setServiceUrl(String url) {
        serviceCall.setServiceURL(url);
    }

    public void setEventID(long eventID) {
        this.eventID = eventID;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    @Override
    public boolean isRunning(){
        return this.isRunning;
    }

    private void notifyStartSync(){
        final SimpleDateFormat simpleFormat = new SimpleDateFormat(DataConverter.NOTIFY_DATE_FORMAT_STRING);
        String dateText = simpleFormat.format(Calendar.getInstance().getTime());

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_action_refresh)
                        .setContentTitle(context.getResources().getString(R.string.notify_sync_blacklist_running_title))
                        .setContentText(dateText + context.getResources().getString(R.string.notify_sync_running_message))
                        .setTicker(context.getResources().getString(R.string.notify_sync_blacklist_running_title))
                        .setAutoCancel(true);

        // mId allows you to update the notification later on.
        this.mNotificationManager.notify(NOTIFY_ID, mBuilder.build());
    }

    private void notifyFinishedSync(String message){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_action_refresh)
                        .setContentTitle(context.getResources().getString(R.string.notify_sync_blacklist_running_title))
                        .setContentText(message)
                        .setAutoCancel(true);

        // mId allows you to update the notification later on.
        this.mNotificationManager.notify(NOTIFY_ID, mBuilder.build());
    }

    private String generateNotifyMessage(boolean success){
        final SimpleDateFormat simpleFormat = new SimpleDateFormat(DataConverter.NOTIFY_DATE_FORMAT_STRING);

        Calendar now = Calendar.getInstance();
        String finishedTimeText = simpleFormat.format(now.getTime());

        String notifyText = "";

        if (success) {
            notifyText = finishedTimeText + context.getResources().getString(R.string.notify_sync_finished_success);
        } else {
            notifyText = finishedTimeText + context.getResources().getString(R.string.notify_sync_finished_fail);
        }

        return notifyText;
    }


    private String generateNotifySubMessage(){
        final SimpleDateFormat simpleFormat = new SimpleDateFormat(DataConverter.NOTIFY_DATE_FORMAT_STRING);

        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(now.getTimeInMillis() + ConfigAccess.getBlackListUpdateDelay(context));
        String nextAttemptText = simpleFormat.format(now.getTime());
        String notifyText = nextAttemptText + context.getResources().getString(R.string.notify_sync_next_attempt);

        return notifyText;
    }

    private String generateNotifyConnectionFailedMessage(){
        final SimpleDateFormat simpleFormat = new SimpleDateFormat(DataConverter.NOTIFY_DATE_FORMAT_STRING);
        Calendar now = Calendar.getInstance();
        String finishedTimeText = simpleFormat.format(now.getTime());

        return finishedTimeText + context.getResources().getString(R.string.notify_logfile_connection_failed_message);
    }
}
