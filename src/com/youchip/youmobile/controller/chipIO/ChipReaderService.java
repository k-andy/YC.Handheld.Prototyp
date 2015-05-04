package com.youchip.youmobile.controller.chipIO;

import java.io.Serializable;
import java.util.Set;

import com.youchip.youmobile.model.chip.exceptions.RfidReadException;
import com.youchip.youmobile.model.chip.interfaces.BasicChip;
import com.youchip.youmobile.model.chip.interfaces.Chip;
import com.youchip.youmobile.model.chip.interfaces.ChipIO;
import com.youchip.youmobile.model.chip.mc1kImpl.MC1KBasicChip;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import static com.youchip.youmobile.controller.IntentExtrasKeys.*;

public class ChipReaderService extends IntentService {

    public static final String RESULT_NOTIFICATION = "com.youchip.youmobile.service.chipreader.result";
    public static final String PROGRESS_NOTIFICATION = "com.youchip.youmobile.service.chipreader.progress";
    public static final String PROGRESS_STEP = "com.youchip.youmobile.service.chipreader.progress.now";
    public static final String PROGRESS_TOTAL = "com.youchip.youmobile.service.chipreader.progress.total";    
    public static final String INTENT_EXTRA_SERVICE_RUN_MODE = "com.youchip.youmobile.service.chipreader.runmode";
    
    private static final long CHIP_IO_ENABLE_DELAY = 400;
    private static final long CHIP_IO_RETRY_DELAY = 100;

    private static ChipIO chipIO = new MC1KChipIO();
    private ServiceIOMode serveMode = ServiceIOMode.MODE_DISABLE;
    private String keyA;
    private Set<Integer> chipBlocks;
    private boolean lastProgresValid = true;
    
    private static final String CLASS_NAME = ChipReaderService.class.getName();

    public enum ServiceIOMode implements Serializable{
        MODE_READ,
        MODE_WRITE,
        MODE_DISABLE
    }
    
    private final SimpleProgressListener progressListener = new SimpleProgressListener() {

        @Override
        public void listen(int total, int step) {
            if (serveMode != ServiceIOMode.MODE_READ) {
                return;
            }
            
            //only when at least two blocks in a row where be scan, it might be valid progress
            if (step > 0) {
                Log.d(CLASS_NAME , "Reading Progress:" + step + " from " + total + " steps.");
                lastProgresValid = true;
            } else {
                Log.w(CLASS_NAME, "Reading progress aborted!");
            }
            
            if (lastProgresValid){
                Log.d(CLASS_NAME, "Sending progress broadcast");
//                if (step <=0) lastProgresValid = false;
                Intent intent = new Intent(PROGRESS_NOTIFICATION);
                intent.putExtra(PROGRESS_STEP, step);
                intent.putExtra(PROGRESS_TOTAL, total);
                sendBroadcast(intent);
            }
       }

    };

    
    public ChipReaderService() {
        super("VisitorChipReaderService");
        chipIO.addChipReadListener(progressListener);
        Log.d(CLASS_NAME, "Service created");
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int mode = super.onStartCommand(intent, flags, startId);
        
        ServiceIOMode oldMode = serveMode;
        serveMode = (ServiceIOMode) intent.getSerializableExtra(INTENT_EXTRA_SERVICE_RUN_MODE); 
        keyA = intent.getStringExtra(INTENT_EXTRA_CHIP_KEY_A);
        chipBlocks = (Set<Integer>) intent.getSerializableExtra(INTENT_EXTRA_CHIP_FIELDS);
        

        
        Log.d(CLASS_NAME, "Set service mode from '"+ oldMode + "' to '" + serveMode + "'");
        return mode;
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(CLASS_NAME, "Handling intent. Service state is '" + serveMode +"'");
        
        if (serveMode == ServiceIOMode.MODE_READ){
            onHandleIntentReadMode(intent);
        } else if (serveMode == ServiceIOMode.MODE_WRITE){
            onHandleIntentWritedMode(intent);
        } else {
            Log.d(CLASS_NAME, "Not handling anything");
        }
    }
    

    protected void onHandleIntentReadMode(Intent intent){
        Log.d(CLASS_NAME, "Running in readmode.");
        BasicChip rfidChip = null;
        boolean result = false;

        chipIO.setKeyA(keyA);

        while ((!result) && serveMode == ServiceIOMode.MODE_READ) {
            try {
                try {
                    rfidChip = chipIO.readDataFromChipByBlockNumber(new MC1KBasicChip(), chipBlocks);
                    Log.d(CLASS_NAME, "Chip data received");
                    result = true;
                } catch (Exception e) {
                    Log.w(CLASS_NAME, e.getMessage());
                    Thread.sleep(CHIP_IO_RETRY_DELAY);
                }
            } catch (InterruptedException ie) {
                Log.e(CLASS_NAME, ie.getMessage());
            }
        }
        publishReadResults(rfidChip, result);
    }

    
    private void onHandleIntentWritedMode(Intent intent){
        Log.d(CLASS_NAME, "Running in write mode.");
    }
    
    private void publishReadResults(BasicChip result, boolean success) {
        Log.d(CLASS_NAME, "Publishing service result");
        Intent intent = new Intent(RESULT_NOTIFICATION);
        intent.putExtra(INTENT_EXTRA_CHIP_OBJECT, result);
        intent.putExtra(INTENT_EXTRA_CHIP_OBJECT_RESULT, success);
        sendBroadcast(intent);
    }
    
    public static void enableChipIO(){
        try {
            Log.d(CLASS_NAME, "Opening chip RFID port..");
            chipIO = new MC1KChipIO(); // TODO proove
            chipIO.openIO();
            Thread.sleep(CHIP_IO_ENABLE_DELAY);
            Log.d(CLASS_NAME, "Chip RFID port Opened.");
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public static void disableChipIO(){
        chipIO.closeIO();
        Log.d(CLASS_NAME, "Chip RFID port Closed.");
    }
    
    // TODO change to write in background task
    public static boolean writeDataToChip(Chip chipData){
        String uid = "";
        
        Log.d(CLASS_NAME, "Start writing to chip (UID: "+uid+")");
        
        try {
            Log.i(CLASS_NAME, "Use KEY_A = " + chipIO.getKeyAAsString());
            uid = chipIO.writeDataToChip(chipData);
            Log.i(CLASS_NAME, "Wrote to Chip (UID:"+uid+" )");
            return true;
        } catch (Exception e){
            String message = e.getMessage() != null ? e.getMessage() : "";
            Log.w(CLASS_NAME, "Writing chipData failed! " + message);
            return false;
        }
    }
    
//    private String getFieldNames(Set<ChipField> fields){
//        StringBuffer buffer = new StringBuffer("");
//        for (ChipField field:fields){
//            buffer.append(field.toString() + ", ");
//        }
//        return buffer.toString();
//    }
}
