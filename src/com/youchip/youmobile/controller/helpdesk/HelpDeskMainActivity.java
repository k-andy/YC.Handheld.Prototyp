package com.youchip.youmobile.controller.helpdesk;

import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_BO_ROLE_ADMIN;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_BO_ROLE_EMPLOYEE;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_BO_ROLE_SUPERVISOR;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_MODE_NAME;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_SHOW_BALANCE_ONLY;
import static com.youchip.youmobile.model.chip.mc1kImpl.MC1KChipSpecs.FactoryFields.*;
import static com.youchip.youmobile.model.chip.mc1kImpl.MC1KVisitorChipField.*;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_CHIP_KEY_A;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_CHIP_OBJECT;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_USER_ID;

import com.youchip.youmobile.R;
import com.youchip.youmobile.controller.chipIO.ChipReaderActivity;
import com.youchip.youmobile.controller.settings.ConfigAccess;
import com.youchip.youmobile.controller.txlog.TxLogger;
import com.youchip.youmobile.model.chip.interfaces.BasicChip;
import com.youchip.youmobile.model.chip.interfaces.ChipField;
import com.youchip.youmobile.model.chip.interfaces.VisitorChip;
import com.youchip.youmobile.model.chip.mc1kImpl.MC1KVisitorChip;
import com.youchip.youmobile.model.chip.mc1kImpl.MC1KChipSpecs.AppType;
import com.youchip.youmobile.utils.AlertBox;
import com.youchip.youmobile.utils.DataConverter;

import java.util.Arrays;
import java.util.Set;

import android.os.Bundle;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;

public class HelpDeskMainActivity extends ChipReaderActivity {

    private static final Set<Integer> STATUS_BLOCKS = DataConverter.getRelevantBlocks(Arrays.asList(new ChipField[] 
            { UID, EVENT_ID, APPTYPE, IN_AREA_ID, IN_AREA_TIME_HH, IN_AREA_TIME_MM,
            VISITOR_ROLES, CREDIT_1, CREDIT_2, VOUCHER})); 
    
    private static final Set<Integer> CRC_BLOCKS = DataConverter.getRelevantBlocks(Arrays.asList(new ChipField[] 
            {EVENT_ID, APPTYPE, CREDIT_1, CREDIT_2, VOUCHER, IN_AREA_ID, VISITOR_ROLES}));
    
    private TxLogger txLogger;
    private String userID;
    private boolean showBalanceOnly = false;
    private boolean wasJustCreated  = true;

    private boolean isBoAdmin = false;
    private boolean isBoSupervisor = false;
    
    private final DialogInterface.OnClickListener onSubmitError = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            HelpDeskMainActivity.this.finish();
        }
    };

    @Override
    protected void onValidChipReadResult(Context context, BasicChip basicChip){
        
        AppType chipAppType = basicChip.getAppType();
        
        if (!basicChip.isValid(CRC_BLOCKS)){
            Log.w(LOG_TAG, "CRC Error! Chip is currpted.");
            txLogger.curruptedCRC(basicChip.getUID());
            AlertBox.allertOnWarning(this, R.string.error_reading_failed_title, R.string.hint_chip_invalid_crc, onSubmitError);   
        } else if (chipAppType != AppType.VISITOR_APP) {
            Log.w(LOG_TAG, "Invalid App Type");
            txLogger.invalidAppType(basicChip.getUID());
            AlertBox.allertOnWarning(this, R.string.error_reading_failed_title, R.string.hint_chip_invalid_app, onSubmitError);
        } else if (basicChip.getEventID() != ConfigAccess.getEventID(this)){
            Log.w(LOG_TAG, "Invalid EventID");
            txLogger.invalidEvent(basicChip.getUID());
            AlertBox.allertOnWarning(this, R.string.error_reading_failed_title, R.string.hint_chip_invalid_eventID, onSubmitError);            
        } else {
            VisitorChip chip = new MC1KVisitorChip(basicChip);
            Intent intent = new Intent(this, HelpDeskDetailsExtendedActivity.class);
            intent.putExtra(INTENT_EXTRA_CHIP_OBJECT, chip);
            intent.putExtra(INTENT_EXTRA_CHIP_KEY_A, keyA);
            intent.putExtra(INTENT_EXTRA_USER_ID, userID);
            intent.putExtra(INTENT_EXTRA_SHOW_BALANCE_ONLY, showBalanceOnly);

            if(getIntent().hasExtra(INTENT_EXTRA_MODE_NAME))
                intent.putExtra(INTENT_EXTRA_MODE_NAME, getIntent().getStringExtra(INTENT_EXTRA_MODE_NAME));

            if(isBoAdmin){
                intent.putExtra(INTENT_EXTRA_BO_ROLE_ADMIN, true);
            } else if (isBoSupervisor){
                intent.putExtra(INTENT_EXTRA_BO_ROLE_SUPERVISOR, true);
            } else {
                intent.putExtra(INTENT_EXTRA_BO_ROLE_EMPLOYEE, true);
            }

            Log.d(HelpDeskMainActivity.this.getClass().getName(), "Loading Activity 'HelpDeskDetailsExtendedActivity'");
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent intent = getIntent();
        this.userID     = intent.getStringExtra(INTENT_EXTRA_USER_ID);
        this.showBalanceOnly= intent.getBooleanExtra(INTENT_EXTRA_SHOW_BALANCE_ONLY, false);

        this.txLogger = new TxLogger(this, userID);

        if( intent.getBooleanExtra(INTENT_EXTRA_BO_ROLE_ADMIN,false) ){
            this.isBoAdmin = true; this.isBoSupervisor = true;
        }else if (intent.getBooleanExtra(INTENT_EXTRA_BO_ROLE_SUPERVISOR, false)) {
            this.isBoSupervisor = true;
        }
    }

    @Override
    public void onStart(){
        super.onStart();

        if(showBalanceOnly){
            if(wasJustCreated){
               wasJustCreated = false;
            } else {
                finish();
            }
        }
    }
    
    @Override
    protected Set<Integer> getStatusBlocks() {
        return STATUS_BLOCKS;
    }

}
