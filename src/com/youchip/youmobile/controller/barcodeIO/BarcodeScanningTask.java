package com.youchip.youmobile.controller.barcodeIO;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import com.iData.Scan.BarcodeControll;
import com.youchip.youmobile.R;

import java.util.Arrays;
import java.util.Calendar;

/**
 * Created by muelleco on 03.07.2014.
 */
abstract class BarcodeScanningTask extends AsyncTask<Void, Integer, Boolean> {

    private static final String CHAR_SET = "GB2312";
    private static final String LOG_TAG = BarcodeScanningTask.class.getName();
    private static final long READER_RESET_TIMEOUT = 700; //in ms
    private static final long READER_RETRY_DELAY = 50; //in ms

    private ProgressDialog progressDialog;
    private final BarcodeControll bcControll;
    private final Context context;
    private final int timeout;
    private final String charSet;

    private Calendar stopCal;
    private byte[] data;

    private volatile boolean scanning;

    private DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialog) {
            // actually could set running = false; right here, but I'll
            // stick to contract.
            cancel(true);
        }
    };

    BarcodeScanningTask(Context context, BarcodeControll bcControll, int timeout) {
        this.context = context;
        this.bcControll = bcControll;
        this.timeout  = timeout;
        this.charSet  = CHAR_SET;
    }

    BarcodeScanningTask(Context context, BarcodeControll bcControll){
        this(context, bcControll, 0);
    }

    BarcodeScanningTask(BarcodeControll bcControll, int timeout){
        this(null, bcControll, timeout);
    }

    BarcodeScanningTask(BarcodeControll bcControll){
        this(null, bcControll, 0);
    }

    @Override
    protected void onPreExecute() {

        // setup dialog box for progress information if possible
        if (context != null && context instanceof Activity) {
            Activity activity = (Activity) context;
            if (!activity.isFinishing()) {
                this.progressDialog = ProgressDialog.show(context,
                        context.getString(R.string.hint_request_wait),
                        context.getString(R.string.hint_request_wait),
                        true, true, cancelListener);
            }
        }

        // setupt timeout if required
        if (timeout > 0) {

            stopCal = Calendar.getInstance();
            stopCal.add(Calendar.MILLISECOND, timeout);
            Log.i(LOG_TAG, "Barcode Reader Time Out was set.");
        }
    }

    @Override
    protected Boolean doInBackground(Void... params) {

        long reset = Calendar.getInstance().getTimeInMillis()+READER_RESET_TIMEOUT;
        startScanning();

        try {
            while (!isCancelled()  && !isTimedOut()) {

                // reading
                try {
                    reset = restartScanning(reset, READER_RESET_TIMEOUT);
                    Thread.sleep(READER_RETRY_DELAY);
                    String code = read();

                    if (code.length() > 0 ) { // if something was read
                        Log.i(LOG_TAG, "Data received:" + code);
                        this.data = code.getBytes(CHAR_SET);
                        return true;
                    }

                } catch (InterruptedException e) {
                    stopScanning();
                    // TODO Auto-generated catch block
                    Log.d(LOG_TAG, "Reading was interrupted");
                }
            }

            // reading success
            if (isCancelled()) {
                Log.w(LOG_TAG,"Reading barcode was cancelled!");
                return false;
            }else if ( isTimedOut()) {
                Log.w(LOG_TAG,"Reading barcode timed out before reading!");
                return false;
            } else {
                Log.i(LOG_TAG,"Successfully read barcode !");
                return true;
            }

        } catch (Exception e) {
            Log.w(LOG_TAG, "Failed to read barcode", e);
            return false;
        } finally {
            stopScanning();
        }

    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        closeDialogBox();
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        closeDialogBox();
    }

    byte[] getResultData(){
        return this.data;
    }

    private boolean isTimedOut(){
        return this.timeout > 0 && this.stopCal != null && Calendar.getInstance().before(this.stopCal);
    }

    private void closeDialogBox(){
        if (context != null && context instanceof Activity && progressDialog != null && progressDialog.isShowing()) {
            Activity activity = (Activity) context;
            if (!activity.isFinishing()) {
                progressDialog.dismiss();
            }
        }
    }

    private long restartScanning(long resetTime, long timeout) throws InterruptedException {
        if ( Calendar.getInstance().getTimeInMillis() > resetTime){
            stopScanning();
            Thread.currentThread().sleep(READER_RETRY_DELAY);
            startScanning();
            //return new reset time
            return Calendar.getInstance().getTimeInMillis() + timeout;
        } else {
            //return old reset time
            return resetTime;
        }
    }

    private void startScanning() {
        if (!scanning) {
            Log.i(LOG_TAG, "Start scanning..");
            scanning = true;
            bcControll.Barcode_StarScan();
        }
    }

    private void stopScanning() {
        if (scanning) {
            Log.i(LOG_TAG, "Stop scanning..");
            scanning = false;
            bcControll.Barcode_StopScan();
        }
    }

    private String read(){
        String info = "";
        try {
            byte[] buffer = new byte[2048];
            Arrays.fill(buffer, (byte) 0);
            buffer = bcControll.Barcode_Read();

            info = new String(buffer, charSet);


        } catch (java.io.UnsupportedEncodingException e) {
            Log.d(LOG_TAG, "Failed to encode barcode");
        }

        return info;
    }

}
