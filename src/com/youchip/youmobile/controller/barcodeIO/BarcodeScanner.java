package com.youchip.youmobile.controller.barcodeIO;

import android.content.Context;

/**
 * Created by muelleco on 03.07.2014.
 */
public interface BarcodeScanner {

    /**
     * power on device
     */
    public void enableDevice();

    /**
     * power off device
     */
    public void disableDevice();

    /**
     * checks if the device is enabled
     * @return
     */
    public  boolean isDeviceEnabled();

    /**
     * Tries to scan until successfully scan a barcode
     * @return
     */
    public void scan() throws NXPBarcodeScanner.IllegalScannerStateException;

    /**
     * Tries to scan successfully scan a barcode or
     * until the timeout occurs, what comes first.
     * @param timeout
     * @return
     */
    public void scan(int timeout) throws NXPBarcodeScanner.IllegalScannerStateException;


    /**
     * adds a context. if the context is not null
     * and valid, it will be used to show the progress
     * @param context
     * @param timeout
     */
    public void scan(Context context, int timeout) throws NXPBarcodeScanner.IllegalScannerStateException;

    /**
     * checks if the scanner is in use
     * @return
     */
    public boolean isScanning();

    /**
     * stops scanning
     */
    public void stopScan();

    /**
     * Setts the listener which is called on an reader result
     * @param listener
     */
    public void setResultListener(ResultListener listener);


    /**
     * ResultListener will be called
     * on barcode scanning results.
     */
    public interface ResultListener {

        /**
         * Will be called after successfully scan a barcode
         *
         * @param data
         */
        public void onReadSuccessful(byte[] data);


        /**
         * Will be called after timeout
         */
        public void onReadFailed();
    }

}
