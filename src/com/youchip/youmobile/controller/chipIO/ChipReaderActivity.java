package com.youchip.youmobile.controller.chipIO;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Button;

import com.android.RfidControll;
import com.youchip.youmobile.R;
import com.youchip.youmobile.controller.AbstractAppControlActivity;
import com.youchip.youmobile.controller.settings.ConfigAccess;
import com.youchip.youmobile.model.chip.interfaces.BasicChip;
import com.youchip.youmobile.model.chip.interfaces.Chip;

import java.io.Serializable;
import java.util.Set;

import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_CHIP_FIELDS;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_CHIP_KEY_A;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_CHIP_OBJECT;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_MODE_NAME;
import static com.youchip.youmobile.controller.chipIO.ChipReaderService.INTENT_EXTRA_SERVICE_RUN_MODE;
import static com.youchip.youmobile.controller.chipIO.ChipReaderService.PROGRESS_NOTIFICATION;
import static com.youchip.youmobile.controller.chipIO.ChipReaderService.PROGRESS_STEP;
import static com.youchip.youmobile.controller.chipIO.ChipReaderService.PROGRESS_TOTAL;
import static com.youchip.youmobile.controller.chipIO.ChipReaderService.RESULT_NOTIFICATION;
import static com.youchip.youmobile.controller.chipIO.ChipReaderService.ServiceIOMode.MODE_DISABLE;
import static com.youchip.youmobile.controller.chipIO.ChipReaderService.ServiceIOMode.MODE_READ;
import static com.youchip.youmobile.controller.chipIO.ChipReaderService.ServiceIOMode.MODE_WRITE;
import static com.youchip.youmobile.model.chip.mc1kImpl.MC1KChipSpecs.Structure.BYTES_PER_BLOCK;
import static com.youchip.youmobile.model.chip.mc1kImpl.MC1KChipSpecs.FactoryFields.UID;

public abstract class ChipReaderActivity extends AbstractAppControlActivity {
    
    protected final String LOG_TAG;
    protected Button stateIndicator;
    private Intent service;
    protected boolean restartService = false;
    protected String keyA;
    protected String requestChipMessage ="";
    private String processingMessage = "";
    protected int    requestChipBackGroundColor = 0;
    private int    processingBackGroundColor = 0;
    private PowerManager.WakeLock wakelock;
    private long wakeTime = 30000;
    
    
    public ChipReaderActivity(){
        LOG_TAG = ChipReaderActivity.this.getClass().getName();
    }
    
    private final BroadcastReceiver resultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent service) {
            Log.d(LOG_TAG, "result broadcast received");

            // use result
            BasicChip chip = (BasicChip) service.getSerializableExtra(INTENT_EXTRA_CHIP_OBJECT);
            service = null;
            if (chip != null) {
                Log.d(LOG_TAG, "Chip data received");
                onValidChipReadResult(context, chip);
            } else {
                Log.d(LOG_TAG, "Service aborted");
            }
        }
    };
    
    protected final BroadcastReceiver progressReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent service) {
            Log.d(LOG_TAG, "progress broadcast received");

            if (service == null) return;
            
            int total = service.getIntExtra(PROGRESS_TOTAL, 0);
            int step = service.getIntExtra(PROGRESS_STEP, -1);

            if (stateIndicator == null){
                Log.d(LOG_TAG, "null");
                return;
            }
            
            if (total > 0 && step > 0){
                updateWakeScreen();
                stateIndicator.setBackgroundColor(processingBackGroundColor);
                stateIndicator.setText(processingMessage);
            } else {
                stateIndicator.setBackgroundColor(requestChipBackGroundColor);
                stateIndicator.setText(requestChipMessage);
            }
        }
    };
    
    
    protected abstract void onValidChipReadResult(Context context, BasicChip basicChip);
    
    protected abstract Set<Integer> getStatusBlocks();
    
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_chip);
        
        requestChipMessage              = getResources().getString(R.string.hint_request_chip);
        processingMessage               = getResources().getString(R.string.hint_chip_processing_1);
        requestChipBackGroundColor      = getResources().getColor(R.color.idle_background);
        processingBackGroundColor       = getResources().getColor(R.color.processing_background);
        
        stateIndicator = (Button) this.findViewById(R.id.stateIndicator);
        Intent intent = getIntent();
        if (intent.hasExtra(INTENT_EXTRA_CHIP_KEY_A)){
            this.keyA = intent.getStringExtra(INTENT_EXTRA_CHIP_KEY_A);
        }
        
        if(intent.hasExtra(INTENT_EXTRA_MODE_NAME)){
            String name = intent.getStringExtra(INTENT_EXTRA_MODE_NAME);
            if (name != null)
                this.setTitle(name);
        }
        
//        // keep screen on
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
    }
    
    protected void restartChipServiceRead(){
        if ((service != null) && restartService) {
            service.putExtra(INTENT_EXTRA_SERVICE_RUN_MODE, MODE_READ);
            startService(service);
        }
    }
    
    protected void restartChipServiceWrite(Chip chip){
        Log.d(LOG_TAG, "Attempt to restart service..");
        if ((service != null)) {
            ChipReaderService.enableChipIO();
            service.putExtra(INTENT_EXTRA_SERVICE_RUN_MODE, MODE_WRITE);
            service.putExtra(INTENT_EXTRA_CHIP_OBJECT, (Serializable) chip);
            startService(service);
        } else {
            Log.w(LOG_TAG, "Restarting failed");
        }
    }

    protected void startChipReadService() {
        Log.d(LOG_TAG, "Attempt to start service..");
        if ((service == null) && restartService) {

            service = new Intent(this, ChipReaderService.class);
            service.putExtra(INTENT_EXTRA_CHIP_KEY_A, keyA);
            service.putExtra(INTENT_EXTRA_CHIP_FIELDS, (Serializable) getStatusBlocks());
            service.putExtra(INTENT_EXTRA_SERVICE_RUN_MODE, MODE_READ);
            Log.d(LOG_TAG, "Starting ChipReaderService");
            startService(service);
            Log.d(LOG_TAG, "ChipReaderService started");
        } else {
            Log.w(LOG_TAG, "Starting failed");
        }
    }
    
    protected void enableChipReadService(){
        restartService = true;
        ChipReaderService.enableChipIO();
        if (service == null){
            startChipReadService();
        } else {
            restartChipServiceRead();
        }
    }
    
    protected void disableChipReadService(){
        if (service != null) {
            restartService = false;
            service.putExtra(INTENT_EXTRA_SERVICE_RUN_MODE, MODE_DISABLE);
            startService(service);
            ChipReaderService.disableChipIO();
        }
    }
    

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "Resuming Activity");

        this.wakeTime = ConfigAccess.getWakeTimeForReaderScreens(this);
        registerReceiver(resultReceiver, new IntentFilter(RESULT_NOTIFICATION));
        registerReceiver(progressReceiver, new IntentFilter(PROGRESS_NOTIFICATION));
        
        //reset gui
        stateIndicator.setBackgroundColor(requestChipBackGroundColor);
        stateIndicator.setText(requestChipMessage);
//        getChipUID();
        if (checkAppState()) {
            enableChipReadService();
        } else {
            showDisableMessage();
            disableApp();
        }
    }

    private void getChipUID() {
        RfidControll rfidControll = new RfidControll();
        rfidControll.API_OpenComm();

        byte[] serialNumberAndKeyA = { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF };
        byte[] dataToRead = new byte[BYTES_PER_BLOCK.getValue()];

        int res = rfidControll.API_MF_Read(0x00, 0x01, 1, 1, serialNumberAndKeyA, dataToRead);

        String result = toHexString(dataToRead, UID.getSize());
        Log.e("TEST", "result = " + result);
    }

    private String toHexString(byte[] byteArray, int size) {
        if (byteArray == null || byteArray.length < 1)
            throw new IllegalArgumentException(
                    "this byteArray must not be null or empty");
        final StringBuilder hexString = new StringBuilder(2 * size);
        for (int i = 0; i < size; i++) {
            if ((byteArray[i] & 0xff) < 0x10)//
                hexString.append("0");
            hexString.append(Integer.toHexString(0xFF & byteArray[i]));
            if (i != (byteArray.length - 1))
                hexString.append("");
        }
        return hexString.toString().toUpperCase();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "Pausing Activity");
        releaseWakelock();
        unregisterReceiver(resultReceiver);
        unregisterReceiver(progressReceiver);
        disableChipReadService();
    }

    protected void updateWakeScreen(){

        releaseWakelock();
        wakelock = ((PowerManager) getSystemService(Context.POWER_SERVICE))
                .newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "busy rfid");
        wakelock.acquire(wakeTime);
    }

    protected void releaseWakelock(){
        if (wakelock != null && wakelock.isHeld())
            wakelock.release();
    }
    
}
