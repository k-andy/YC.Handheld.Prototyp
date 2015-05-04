package com.youchip.youmobile.controller.settings;

import static com.youchip.youmobile.model.chip.mc1kImpl.MC1KChipSpecs.FactoryFields.*;
import static com.youchip.youmobile.model.chip.mc1kImpl.MC1KConfigChipField.*;
import static com.youchip.youmobile.controller.IntentExtrasKeys.*;

import java.util.Arrays;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.youchip.youmobile.R;
import com.youchip.youmobile.controller.chipIO.ChipReaderActivity;
import com.youchip.youmobile.model.chip.interfaces.BasicChip;
import com.youchip.youmobile.model.chip.interfaces.ChipField;
import com.youchip.youmobile.model.chip.interfaces.ConfigChip;
import com.youchip.youmobile.model.chip.mc1kImpl.MC1KChipSpecs.AppType;
import com.youchip.youmobile.model.chip.mc1kImpl.MC1KConfigChip;
import com.youchip.youmobile.utils.DataConverter;

public class ConfigChipLoadActivity extends ChipReaderActivity{
    
    public static final int INTENT_REQUEST_CONFIG_CHIP_LOAD =  1091273;
    
    private static final Set<Integer> STATUS_BLOCKS = DataConverter.getRelevantBlocks(Arrays.asList(new ChipField[] 
            {UID, EVENT_ID, APPTYPE, SVC_IP, SVC_NAME, SVC_PORT, SVC_PROTOCOLL, SVC_SUBDOMAIN}));
 
    private static final Set<Integer> CRC_BLOCKS = DataConverter.getRelevantBlocks(Arrays.asList(new ChipField[]
            {EVENT_ID, APPTYPE}));
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.requestChipMessage   = getResources().getString(R.string.hint_request_config_chip);
    }
    
    @Override
    protected void onValidChipReadResult(Context context, BasicChip rawChip) {
        
        Log.d(this.getClass().getName(), "Handling Config Chip (UID " + rawChip.getUID() +")");
        String chipID       = rawChip.getUID();
        boolean isCRCValid  = rawChip.isValid(CRC_BLOCKS);
        AppType chipAppType = rawChip.getAppType();
        long chipEventID    = rawChip.getEventID();
        
        try {
            if (!isCRCValid || chipAppType != AppType.CONFIG_LOAD_APP) {
                leaveActivity(false, chipID);
            } else {
                ConfigChip chip = new MC1KConfigChip(rawChip);
                Log.d(this.getClass().getName(),"Updated URL to: " + chip.getServiceHost() + chip.getServiceName());
                ConfigAccess.storeServiceURL(context, chip.getServiceHost(), chip.getServiceName());
                ConfigAccess.storeEventID(context, chipEventID);
                leaveActivity(true, chipID);
            }
            
        } catch (Exception e) {
            Log.w(this.getClass().getName(), "Abort on error", e);
            leaveActivity(false, chipID);
        }
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
    
    

}
