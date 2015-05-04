package com.youchip.youmobile.controller.helpdesk;

import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_CHIP_OBJECT;
import static com.youchip.youmobile.model.chip.mc1kImpl.MC1KChipSpecs.FactoryFields.*;
import static com.youchip.youmobile.model.chip.mc1kImpl.MC1KVisitorChipField.*;

import java.util.Arrays;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.youchip.youmobile.controller.chipIO.ChipReaderActivity;
import com.youchip.youmobile.controller.chipIO.ChipReaderService;
import com.youchip.youmobile.model.chip.interfaces.BasicChip;
import com.youchip.youmobile.model.chip.interfaces.ChipField;
import com.youchip.youmobile.model.chip.interfaces.VisitorChip;
import com.youchip.youmobile.model.chip.mc1kImpl.MC1KVisitorChip;
import com.youchip.youmobile.utils.DataConverter;

public class HelpDeskModifyActivity extends ChipReaderActivity{
    
    private VisitorChip oldChip;

    private static final Set<Integer> STATUS_BLOCKS = DataConverter.getRelevantBlocks(Arrays.asList(new ChipField[] 
            {UID, EVENT_ID, APPTYPE, IN_AREA_ID, VISITOR_ROLES}));

    
    @Override
    protected void onValidChipReadResult(Context context, BasicChip rawChip){
        
        VisitorChip chip = new MC1KVisitorChip(rawChip);
        
        Log.d(LOG_TAG, "Validating.. UID: " + chip.getUID() + " ?= " + this.oldChip.getUID() +
                ", EventID: " + chip.getEventID() + " ?= " + this.oldChip.getEventID());
        
        if (chip.getUID().equals(this.oldChip.getUID()) && chip.getEventID() == this.oldChip.getEventID()){
            Log.d(LOG_TAG, "Chip is identical. Updating Chip Data");
            upDateChipZoneInfo(chip);
            setResult(RESULT_OK);
        } else {
            Log.d(LOG_TAG, "Error! Chip is not identical!");
            setResult(RESULT_CANCELED);
        }
        
        finish();
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        this.oldChip = (VisitorChip) intent.getSerializableExtra(INTENT_EXTRA_CHIP_OBJECT);
        super.onCreate(savedInstanceState);
    }
    

    @Override
    protected Set<Integer> getStatusBlocks() {
        return STATUS_BLOCKS;
    }
    
    private boolean upDateChipZoneInfo(VisitorChip chipData){
        chipData.setInAreaID(0);
        return ChipReaderService.writeDataToChip(chipData);
    }

}
