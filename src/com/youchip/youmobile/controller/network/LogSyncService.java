package com.youchip.youmobile.controller.network;

import java.io.File;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Calendar;


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
import com.youchip.youmobile.controller.settings.ConfigAccess;
import com.youchip.youmobile.controller.txlog.TxLogger;
import com.youchip.youmobile.model.network.request.LogSyncSOAPRequest;
import com.youchip.youmobile.model.network.response.LogSyncSOAPResponse;
import com.youchip.youmobile.utils.AlertBox;
import com.youchip.youmobile.utils.DataConverter;
import com.youchip.youmobile.utils.SystemInfo;

public class LogSyncService implements ConfigUpdateService {
    
    private final static String LOG_TAG = LogSyncService.class.getName();
    private static final String LOG_MAIN_FILE = ConfigAccess.getLogMainFile();
    
    private File appRootDir;
    private String deviceID = "";
    private Context context;
    private TxLogger txLogger;
    private static final WebServiceCall serviceCall = new WebServiceCall();
    private boolean networkError = false;

    private final int NOTIFY_ID = 1;
    private final NotificationManager mNotificationManager;
    private AsyncTask currentTask = null;


    public LogSyncService(Context context, TxLogger txLogger, File appRootDir) {
        this.context = context;
        this.txLogger = txLogger;
        this.appRootDir = appRootDir;

        this.mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }
    
    public void update(){
        this.networkError = false;
        File file = null;

        Log.d(LogSyncService.class.toString(), "LogSync Service is running.");
        notifyUser();

        // get list of files
        if (appRootDir != null)
            file = SystemInfo.getOldestLogFile(appRootDir);

        // if there is no backup log, try to take the main log file
        if (file == null && appRootDir != null) {
            file = new File(appRootDir, LOG_MAIN_FILE);
            if (file.length() > 0) {
                file = TxLogger.renameFile(context, file);
            } else {
                file = null;
            }
        }

        if (file != null && appRootDir != null) {
            //get connection data and send data
            upLoadToServer(file);
        } else {
            // no file to sync
            sendNoLogFileToSyncMessage();
        }
    }
    
    @Override
    public boolean isRunning(){
        if (currentTask == null || currentTask.getStatus() != AsyncTask.Status.RUNNING){
            return false;
        } else {
            return true;
        }
    }

    /**
     * loading configuration data from service
     * @return
     */
    public boolean upLoadToServer(final File file) {

        final File pushFile = file;

        try {
            final LogSyncSOAPRequest  basicRequest  = new LogSyncSOAPRequest(file);
            final LogSyncSOAPResponse basicResponse = new LogSyncSOAPResponse();

            AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Void... params) {
                    Log.d(LOG_TAG, "Start sending log file of to web service");
                    try {
                        Log.d(LOG_TAG, "Sending " + file.length() + " byte.");

                        boolean result = serviceCall.callService(basicRequest, basicResponse);
                        Log.d(LOG_TAG, "Finished sending log file to web service");
                        return result;
                    } catch(SocketTimeoutException ste){
                        Log.w(LOG_TAG, "Failed to connect to service!" + ste.getMessage());
                        LogSyncService.this.networkError = true;
                        return false;
                    } catch (java.net.SocketException ce) {
                        Log.w(LOG_TAG, "Failed to connect to service!" + ce.getMessage());
                        LogSyncService.this.networkError = true;
                        return false;
                    } catch (Exception e) {
                        Log.w(LOG_TAG, "Failed to sync log!", e);
                        return false;
                    }
                }


                @Override
                protected void onPostExecute(Boolean result) {
                    if (result && !LogSyncService.this.networkError) {
                        Log.d(LOG_TAG, "Upload successfull. Removing local file.");
                        notifyFinishedSync(generateNotifyMessage(true) + generateNotifySubMessage());
                        pushFile.delete();
                    }else if (LogSyncService.this.networkError){
                        Log.w(LOG_TAG, "Connection failed.");
                        notifyFinishedSync(generateNotifyConnectionFailedMessage() + generateNotifySubMessage());
                    } else {
                        Log.w(LOG_TAG, "Upload unsuccessfull. Keeping local file.");
                        notifyFinishedSync(generateNotifyMessage(false) + generateNotifySubMessage());
                    }

                }
            };

            currentTask = task;

            task.execute(new Void[0]);
            return true;

        } catch (OutOfMemoryError oome){
            Log.w(LOG_TAG, "File to huge to process!",oome);
            notifyFinishedSync(generateNotifyFatalMessage());
            return false;
        }
    }
    
    /**
     * sending a message to the server that there is nothing to sync
     */
    public void sendNoLogFileToSyncMessage(){
        
        final LogSyncSOAPRequest  basicRequest  = new LogSyncSOAPRequest(TxLogger.getLogFileName(context), txLogger.createNoFileInfoLog());
        final LogSyncSOAPResponse basicResponse = new LogSyncSOAPResponse();
        
        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                Log.d(LOG_TAG, "Start sending no-file-info to sync-service");
                try{
                    boolean result = serviceCall.callService(basicRequest, basicResponse);
                    Log.d(LOG_TAG, "Finished sending no-file-info to sync-service");
                    return result;
                } catch (IllegalArgumentException iae){
                    Log.w(LOG_TAG, iae.toString());
                    return false;
                } catch(SocketTimeoutException ste){
                    Log.w(LOG_TAG, "Failed to connect to service!" + ste.getMessage());
                    LogSyncService.this.networkError = true;
                    return false;
                } catch (java.net.SocketException ce) {
                    Log.w(LOG_TAG, "Failed to connect to service!" + ce.getMessage());
                    LogSyncService.this.networkError = true;
                    return false;
                } catch (Exception e) {
                    Log.w(LOG_TAG, e.toString());
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (LogSyncService.this.networkError){
                    Log.w(LOG_TAG, "Connection failed.");
                    notifyFinishedSync(generateNotifyConnectionFailedMessage() + generateNotifySubMessage());
                } else {
                    notifyFinishedSync(generateNotifyMessage(result) + generateNotifySubMessage());
                }
            }
        };

        currentTask = task;
        task.execute(new Void[0]);
    }


    public File getAppRootDir() {
        return appRootDir;
    }

    public void setAppRootDir(File appRootDir) {
        this.appRootDir = appRootDir;
    }

    public String getServiceUrl() {
        return serviceCall.getServiceURL();
    }

    public long getEventID() {
//        return eventID;
        return 0;
    }

    public void setServiceUrl(String url) {
        serviceCall.setServiceURL(url);
    }

    public void setEventID(long eventID) {
//        this.eventID = eventID;
    }

    public String getDeviceID() {
        return deviceID;
    }


    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }


    private void notifyUser(){
        final Intent emptyIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, emptyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        final SimpleDateFormat simpleFormat = new SimpleDateFormat(DataConverter.NOTIFY_DATE_FORMAT_STRING);
        String dateText = simpleFormat.format(Calendar.getInstance().getTime());

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_action_refresh)
                        .setContentTitle(context.getResources().getString(R.string.notify_sync_log_running_title))
                        .setContentText(dateText + context.getResources().getString(R.string.notify_sync_running_message))
                        .setTicker(context.getResources().getString(R.string.notify_sync_log_running_title))
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);


        // mId allows you to update the notification later on.
        this.mNotificationManager.notify(NOTIFY_ID, mBuilder.build());
    }

    private void notifyFinishedSync(String message){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_action_refresh)
                        .setContentTitle(context.getResources().getString(R.string.notify_sync_log_running_title))
                        .setContentText(message)
                        .setAutoCancel(true);

        // mId allows you to update the notification later on.
        this.mNotificationManager.notify(NOTIFY_ID, mBuilder.build());
    }

    private String generateNotifyFatalMessage(){
        return context.getResources().getString(R.string.notify_logfile_too_big_title);
    }

    private String generateNotifyConnectionFailedMessage(){
        final SimpleDateFormat simpleFormat = new SimpleDateFormat(DataConverter.NOTIFY_DATE_FORMAT_STRING);
        Calendar now = Calendar.getInstance();
        String finishedTimeText = simpleFormat.format(now.getTime());

        return finishedTimeText + context.getResources().getString(R.string.notify_logfile_connection_failed_message);
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
        now.setTimeInMillis(now.getTimeInMillis() + ConfigAccess.getLogSyncDelay(context));
        String nextAttemptText = simpleFormat.format(now.getTime());
        String notifyText = nextAttemptText + context.getResources().getString(R.string.notify_sync_next_attempt);

        return notifyText;
    }

}
