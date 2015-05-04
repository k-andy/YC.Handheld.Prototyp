package com.youchip.youmobile.controller.barcodeIO;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;


import com.iData.Scan.BarcodeControll;

/**
 * Created by muelleco on 03.07.2014.
 */
public class NXPBarcodeScanner implements BarcodeScanner {

    private static final String LOG_TAG = NXPBarcodeScanner.class.getName();

    private static volatile NXPBarcodeScanner scanner;
    private final BarcodeControll bcControll;
    private volatile boolean deviceEnabled;

    private BarcodeScanningTask scannTask;
    private ResultListener resultListener;

    private NXPBarcodeScanner(){
        this.bcControll = new BarcodeControll();
        this.bcControll.Barcode_Close();
        this.deviceEnabled = false;
    }

    public static NXPBarcodeScanner getInstance(){
        if (scanner == null){
            scanner = new NXPBarcodeScanner();
        }

        return scanner;
    }


    @Override
    public synchronized void enableDevice() {
        if (!this.deviceEnabled){
            this.bcControll.Barcode_open();
            this.deviceEnabled = true;
        }

    }

    @Override
    public synchronized void disableDevice() {
        if (this.deviceEnabled){

            if(isScanning()){
                this.scannTask.cancel(true);

            }

            this.bcControll.Barcode_Close();
            this.deviceEnabled = false;
        }

    }

    public synchronized boolean isDeviceEnabled() {
        return this.deviceEnabled;
    }


    @Override
    public synchronized void scan() throws IllegalScannerStateException {
        scan(0);
    }

    @Override
    public synchronized void scan(int timeout) throws IllegalScannerStateException {
        scan(null, timeout);
    }

    @Override
    public synchronized void scan(Context context, int timeout) throws IllegalScannerStateException {
        if (!isDeviceEnabled()) {
            throw new IllegalScannerStateException("Device is not enabled! Enable Barcode Reader before reading!");
        }
        if (isScanning()) {
            throw new IllegalScannerStateException("Device is still in use!");
        } else {
            scannTask = new BarcodeScanningTask(context, bcControll, timeout) {
                @Override
                protected void onPostExecute(Boolean result) {

                    super.onPostExecute(result);

                    if (resultListener != null){
                        if (result) {
                            resultListener.onReadSuccessful(getResultData());
                        } else {
                            resultListener.onReadFailed();
                        }
                    }
                }

                @Override
                protected void onCancelled() {
                    super.onCancelled();

                    if (resultListener != null){
                        resultListener.onReadFailed();
                    }
                }


            };

            scannTask.execute(new Void[0]);
        }
    }

    @Override
    public boolean isScanning(){
        return (scannTask!= null && !scannTask.getStatus().equals(AsyncTask.Status.FINISHED));
    }

    @Override
    public synchronized void stopScan(){
        this.scannTask.cancel(true);
    }


    @Override
    public void setResultListener(ResultListener resultListener){
        this.resultListener = resultListener;
    }

    @Override
    protected void finalize() throws Throwable {
        if (isDeviceEnabled()){
            this.disableDevice();
            Log.w(LOG_TAG, "Barcode Reader was not correctly closed by user!");
        }

        super.finalize();
    }

    public class IllegalScannerStateException extends Exception{

        public IllegalScannerStateException(String message){
            super(message);
        }

    }


}
