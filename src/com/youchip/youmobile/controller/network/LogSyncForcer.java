package com.youchip.youmobile.controller.network;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.youchip.youmobile.R;
import com.youchip.youmobile.controller.network.serviceCaller.WebServiceCall;
import com.youchip.youmobile.controller.network.serviceInterface.ConfigUpdateServiceStarter;
import com.youchip.youmobile.controller.settings.ConfigAccess;
import com.youchip.youmobile.controller.txlog.TxLogger;
import com.youchip.youmobile.model.network.request.LogSyncSOAPRequest;
import com.youchip.youmobile.model.network.response.LogSyncSOAPResponse;
import com.youchip.youmobile.utils.AlertBox;
import com.youchip.youmobile.utils.DataConverter;
import com.youchip.youmobile.utils.SystemInfo;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * Created by muelleco on 08.05.2014.
 */
public class LogSyncForcer extends AsyncTask<Void, Integer, Boolean>{

    private static LogSyncForcer current = null;

    private Context context;
    private final File logFileDirectory;
    private File logMainFile = null;
    private List<File> logFiles;
    private ProgressDialog progressDialog;
    private AlertDialog alertBox;
    private Toast cancelToast;
//    private static final String LOG_MAIN_FILE = ConfigAccess.getLogMainFile();
    private final WebServiceCall serviceCall;
    private boolean removingSuccessful;
    private ConfigUpdateServiceStarter logSyncService;
    private boolean isFullyInitialized = false;
    private boolean outOfMemory = false;


    private final int NOTIFY_ID = 3;
    private final NotificationManager mNotificationManager;

    private boolean networkError = false;

    private static final String LOG_TAG = LogSyncForcer.class.getName();

    private final PendingIntent pendingIntent;
    private final String dateStartText;


    private DialogInterface.OnCancelListener cancelLogSync = new DialogInterface.OnCancelListener(){

        @Override
        public void onCancel(DialogInterface dialogInterface) {
            LogSyncForcer.this.cancel(false);
        }
    };

    /**
     * creates and internally stores a LogSync-AsyncTask
     * @param context context which will be use to receive conig data and to show status
     * @param logSyncService Another service which may or may not be running
     * @return true if creating a new service was successfull, false if another is still running or pending
     */
    public static boolean createNew(Context context, ConfigUpdateServiceStarter logSyncService){
        if (!isRunning()) {
            LogSyncForcer.current = new LogSyncForcer(context, logSyncService);
            return true;
        } else {
            return false;
        }
    }

    public static boolean isRunning(){
        if (!isUninitialized() && LogSyncForcer.current.getStatus() == Status.RUNNING){
            return true;
        } else {
            return false;
        }
    }

    public static boolean isPending(){
        if (!isUninitialized() && LogSyncForcer.current.getStatus() == Status.PENDING){
            return true;
        } else {
            return false;
        }
    }

    public static boolean isFinished(){
        if (!isUninitialized() && LogSyncForcer.current.getStatus() == Status.FINISHED){
            return true;
        } else {
            return false;
        }
    }

    public static boolean isUninitialized(){
        return null == LogSyncForcer.current;
    }

    public static boolean executeService(){
        if (null != LogSyncForcer.current && isPending() && !isRunning() && !isFinished()){
            LogSyncForcer.current.execute();
            return true;
        } else {
            return false;
        }
    }

    private LogSyncForcer(Context context, ConfigUpdateServiceStarter logSyncService){
        this.context = context;
        this.logFileDirectory = ConfigAccess.getLogFileDirectory(context);
        this.serviceCall = new WebServiceCall(ConfigAccess.getServiceURL(context));
        this.logSyncService = logSyncService;
        this.logMainFile = ConfigAccess.getLogMainFile(this.context);
        this.mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        this.pendingIntent = PendingIntent.getActivity(context, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);

        final SimpleDateFormat simpleFormat = new SimpleDateFormat(DataConverter.NOTIFY_DATE_FORMAT_STRING);
        this.dateStartText = simpleFormat.format(Calendar.getInstance().getTime());
    }

    @Override
    protected void onPreExecute(){
        super.onPreExecute();

        notifyStartSync();
        if (this.logSyncService != null) {
            this.logSyncService.stopService();
        }

        this.progressDialog = new ProgressDialog(context);
        this.progressDialog.setTitle(R.string.hint_request_wait);
        this.progressDialog.setCancelable(true);
        this.progressDialog.setCanceledOnTouchOutside(false);
        this.progressDialog.setOnCancelListener(cancelLogSync);
        this.progressDialog.setIndeterminate(false);
        this.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        this.progressDialog.setMax(0);
        this.progressDialog.setProgress(0);

        if(this.logSyncService.isRunning()){
            this.isFullyInitialized = false;
            this.progressDialog.setMessage(context.getResources().getString(R.string.hint_request_wait_service));
        } else {
            finalizeInit();
        }

        this.progressDialog.show();
    }


    @Override
    protected Boolean doInBackground(Void... voids) {
        if(!this.isFullyInitialized) {
            if (!checkLogSyncState()) {
                return false;
            } else {
                finalizeInit();
            }
        }

        LogSyncForcer.this.networkError = false;

        Log.d(LOG_TAG, "Start sending " + logFiles.size() + " log files to web service");

        boolean result = true;
        this.removingSuccessful = true;

        for (File file:logFiles){
            if (isCancelled()) {
                return false;
            }

            Log.d(LOG_TAG, "Sending log file '"+file.getName()+"' (" + file.length() + " bytes)");

            try{
                boolean uploadResult = serviceCall.callService(new LogSyncSOAPRequest(file), new LogSyncSOAPResponse());
                result &= uploadResult;

                if (uploadResult && !file.delete()){
                    this.removingSuccessful &= false;
                } else if(!uploadResult) {
                    Log.w(LOG_TAG, "Upload unsuccessful. Keeping local file.");
                } else {
                    // nothing to do
                }
            } catch (java.net.SocketException ce) {
                Log.w(LOG_TAG, "Failed to connect to service!", ce);
                LogSyncForcer.this.networkError = true;
                return false;
            } catch (Exception e) {
                Log.w(LOG_TAG, "Failed to sync log", e);
                result &= false;
            } catch (OutOfMemoryError oome){
                Log.w(LOG_TAG, "File to huge to process!",oome);
                outOfMemory = true;
                result &= false;
            }

            super.publishProgress();
        }

        return result;
    }



    @Override
    protected void onProgressUpdate(Integer... progress) {
        if ( context instanceof Activity && this.progressDialog != null) {
            /*Activity activity = (Activity) context;
            if (!activity.isFinishing()) {*/
               this.progressDialog.incrementProgressBy(1);
            /*}*/
        }

        notifyStartSync("(" + this.progressDialog.getProgress() + "/" + this.progressDialog.getMax() + ")");
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        logFiles = null;

        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();

        int titleID = 0;
        int msgID = 0;

        if (!isFullyInitialized || logSyncService.isRunning()) {
            notifyFinishedSync(generateNotifyMessage(false));
            titleID = R.string.failed_title;
            msgID   = R.string.hint_log_sync_failed_interfere;
        } else if (this.outOfMemory){
            notifyFinishedSync(generateNotifyFatalMessage());
            titleID = R.string.failed_title;
            msgID   = R.string.notify_logfile_too_big_title;
        } else if(result && this.removingSuccessful && !LogSyncForcer.this.networkError) {
            notifyFinishedSync(generateNotifyMessage(true));
            titleID = R.string.success_title;
            msgID   = R.string.hint_log_sync_finished_successful;
        } else if (result && !LogSyncForcer.this.networkError) {
            notifyFinishedSync(generateNotifyMessage(false));
            titleID = R.string.failed_title;
            msgID   = R.string.hint_log_sync_ok_local_failed;
        } else if ( LogSyncForcer.this.networkError) {
            notifyFinishedSync(generateNotifyConnectionFailedMessage());
            titleID =  R.string.failed_title;
            msgID   = R.string.hint_logfile_connection_failed_message;
        } else {
            notifyFinishedSync(generateNotifyMessage(false));
            titleID = R.string.failed_title;
            msgID   = R.string.hint_log_sync_failed;
        }

        if ( context instanceof Activity ) {
            Activity activity = (Activity) context;
            if (!activity.isFinishing()) {
                AlertBox.allertOnWarning(this.context, titleID, msgID);
            }
        }

        //restart service after finishing
        if (this.logSyncService != null) {
            this.logSyncService.startService();
        }
    }

    @Override
    protected void onCancelled(){
        super.onPostExecute(false);

        Log.w(LOG_TAG,"LogSync cancelled by user!");

        logFiles = null;

        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();

        if ( context instanceof Activity ) {
            Activity activity = (Activity) context;
            if (!activity.isFinishing()) {
                cancelToast = Toast.makeText(this.context, R.string.hint_log_sync_cancelled, Toast.LENGTH_SHORT);
                cancelToast.show();
            }
        }

        notifyFinishedSync(generateNotifyCanceledMessage());
        this.logSyncService.startService();
    }

    private void prepareFileList(){
            this.logFiles = SystemInfo.getLogFiles(logFileDirectory);
            if (this.logMainFile.exists() && logMainFile.length() > 0) {
                File file = TxLogger.renameFile(context, logMainFile);
                this.logFiles.add(file);
            }
            Collections.sort(this.logFiles);
    }


    private boolean checkLogSyncState(){
        //wait for logsync service to finish
        if (this.logSyncService != null) {
            if (this.logSyncService.isRunning()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.w(LOG_TAG, "Failed, waiting for LogSync service to finish", e);
                    return false;
                }
            } else {
                return true;
            }

            // if not ready yet, quit
            if (this.logSyncService.isRunning()) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    private void finalizeInit(){
        this.isFullyInitialized = true;
        prepareFileList();
        this.progressDialog.setMessage(context.getResources().getString(R.string.hint_request_wait));
        this.progressDialog.setMax(this.logFiles.size());
        super.publishProgress(0);
    }

    private void notifyStartSync(){
        notifyStartSync("");
    }

    private void notifyStartSync(String info){

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_action_refresh)
                        .setContentTitle(context.getResources().getString(R.string.notify_sync_force_running_title))
                        .setContentText(this.dateStartText + context.getResources().getString(R.string.notify_sync_running_message) + info)
                        .setTicker(context.getResources().getString(R.string.notify_sync_log_running_title))
                        .setAutoCancel(true)
                        .setContentIntent(this.pendingIntent);


        // mId allows you to update the notification later on.
        this.mNotificationManager.notify(NOTIFY_ID, mBuilder.build());
    }

    private void notifyFinishedSync(String message){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_action_refresh)
                        .setContentTitle(context.getResources().getString(R.string.notify_sync_force_running_title))
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

    private String generateNotifyCanceledMessage(){
        return context.getResources().getString(R.string.hint_log_sync_cancelled);
    }

    public static void dismissDialogs(){
        if (null != LogSyncForcer.current) {

            AlertBox.dismiss();

            if (LogSyncForcer.current.progressDialog != null && LogSyncForcer.current.progressDialog.isShowing()) {
                LogSyncForcer.current.progressDialog.dismiss();
            }

            if (LogSyncForcer.current.cancelToast != null) {
                LogSyncForcer.current.progressDialog.dismiss();
            }
        }
    }

}
