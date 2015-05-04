package com.youchip.youmobile.controller.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.youchip.youmobile.R;
import com.youchip.youmobile.controller.chipIO.ChipReaderActivity;
import com.youchip.youmobile.model.chip.interfaces.BasicChip;
import com.youchip.youmobile.model.chip.interfaces.ChipField;
import com.youchip.youmobile.model.chip.mc1kImpl.MC1KChipSpecs.AppType;
import com.youchip.youmobile.utils.DataConverter;

import java.util.Arrays;
import java.util.Set;

import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_CHIP_UID;
import static com.youchip.youmobile.model.chip.mc1kImpl.MC1KChipSpecs.FactoryFields.APPTYPE;
import static com.youchip.youmobile.model.chip.mc1kImpl.MC1KChipSpecs.FactoryFields.EVENT_ID;
import static com.youchip.youmobile.model.chip.mc1kImpl.MC1KChipSpecs.FactoryFields.UID;

public class ResetConfigActivity extends ChipReaderActivity {
    
    public static final int INTENT_REQUEST_CONFIG_RESET =  1291243;
    
    private static final Set<Integer> STATUS_BLOCKS = DataConverter.getRelevantBlocks(Arrays.asList(new ChipField[] 
            {UID, EVENT_ID, APPTYPE}));
 
    private static final Set<Integer> CRC_BLOCKS = DataConverter.getRelevantBlocks(Arrays.asList(new ChipField[]
            {EVENT_ID, APPTYPE}));
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resetScreenStateIndicator();
    }
    
    @Override
    public void onValidChipReadResult(Context context, BasicChip rawChip) {
        
        Log.d(this.getClass().getName(), "Handling Chip (UID " + rawChip.getUID() +")");
        boolean isCRCValid  = rawChip.isValid(CRC_BLOCKS);
        AppType chipAppType = rawChip.getAppType();
        
        if (!isCRCValid || chipAppType != AppType.RESET_APP) {
            onChipAccessDenied(rawChip);
        } else {
            onChipAccessGranted(rawChip);
        }
    }


    public void onChipAccessGranted(BasicChip chip){
        try {
            leaveActivity(true, chip.getUID());
        } catch (Exception e){
            Log.w(this.getClass().getName(), "Abort on error", e);
            leaveActivity(false, chip.getUID());
        }
    }


    public void onChipAccessDenied(BasicChip chip){
        leaveActivity(false, chip.getUID());
    }
    
    
    private void leaveActivity(boolean result, String uid){
        Intent intent = new Intent();
        intent.putExtra(INTENT_EXTRA_CHIP_UID, uid);
        
        if (result){
            setResult(RESULT_OK, intent);
        } else {
            setResult(RESULT_CANCELED, intent);            
        }
        this.finish();
    }

    @Override
    protected Set<Integer> getStatusBlocks() {
        return STATUS_BLOCKS;
    }



    protected void resetScreenStateIndicator(){
        super.stateIndicator.setBackgroundColor(this.getResources().getColor(R.color.idle_background));
        super.stateIndicator.setText(R.string.hint_request_reset_chip);
        super.stateIndicator.invalidate();
    }

}
