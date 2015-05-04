package com.youchip.youmobile.controller.gate;

import static com.youchip.youmobile.controller.IntentExtrasKeys.*;
import static com.youchip.youmobile.model.chip.mc1kImpl.MC1KChipSpecs.FactoryFields.*;
import static com.youchip.youmobile.controller.network.BlackListConfigUpdateService.BLACKLIST_UPDATED_INTENT;
import static com.youchip.youmobile.model.chip.mc1kImpl.MC1KVisitorChipField.IN_AREA_ID;
import static com.youchip.youmobile.model.chip.mc1kImpl.MC1KVisitorChipField.VISITOR_ROLES;
import static com.youchip.youmobile.controller.txlog.TxGateLogger.AccessState.*;

import com.youchip.youmobile.model.gate.AreaConfig;
import com.youchip.youmobile.model.gate.BlockedChip;
import com.youchip.youmobile.controller.settings.ConfigAccess;
import com.youchip.youmobile.controller.txlog.AccessResult;
import com.youchip.youmobile.controller.txlog.TxGateLogger.AccessState;

import java.util.Date;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import com.youchip.youmobile.R;
import com.youchip.youmobile.controller.chipIO.ChipReaderActivity;
import com.youchip.youmobile.controller.chipIO.ChipReaderService;
import com.youchip.youmobile.controller.txlog.TxGateLogger;
import com.youchip.youmobile.model.chip.interfaces.BasicChip;
import com.youchip.youmobile.model.chip.interfaces.ChipField;
import com.youchip.youmobile.model.chip.interfaces.VisitorChip;
import com.youchip.youmobile.model.chip.mc1kImpl.MC1KChipSpecs.AppType;
import com.youchip.youmobile.model.chip.mc1kImpl.MC1KVisitorChip;
import com.youchip.youmobile.utils.DataConverter;

import android.os.Bundle;
import android.os.Handler;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Menu;

public class GateAccessActivity extends ChipReaderActivity {
  
    private static final Set<Integer> STATUS_BLOCKS = DataConverter.getRelevantBlocks(Arrays.asList(new ChipField[] 
            {UID, EVENT_ID, APPTYPE, IN_AREA_ID, VISITOR_ROLES }));

    private static final Set<Integer> CRC_BLOCKS = DataConverter.getRelevantBlocks(Arrays.asList(new ChipField[]
            {EVENT_ID, APPTYPE, IN_AREA_ID, VISITOR_ROLES }));
    
    private long accessResultDisplayDuration = 1000;
    
    private TxGateLogger txLogger;

    private int accessResultMessageID = 0;

    private AreaConfig activeArea;
    
    private volatile List<BlockedChip> blackList;
    
    private final AccessChecker checker = new AccessChecker();

    private Handler restartReadingHandler = new Handler();

    private final Runnable restartReadingRunnable = new Runnable() {
        @Override
        public void run() {          
            if (restartService) {
                Log.d(LOG_TAG,"Restarting ChipReadingService with delay");
                stateIndicator.setBackgroundColor(requestChipBackGroundColor);
                restartChipServiceRead();
                stateIndicator.setText(requestChipMessage);
            }
        }
    };
    
    
    private final BroadcastReceiver blackListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent service) {
            Log.d(LOG_TAG, "BlackList Update broadcast received");
            blackList = ConfigAccess.getBlackList(GateAccessActivity.this);
        }
    };

    @Override
    protected Set<Integer> getStatusBlocks() {
        return STATUS_BLOCKS;
    }

    @Override
    protected void onValidChipReadResult(Context context, BasicChip rawChip) {
        
        VisitorChip chip = new MC1KVisitorChip(rawChip);
        
        String chipUID = chip.getUID();
        boolean accessResult = true;

        // collect all access results
        AccessResult validCRC       = checker.checkCRC(chip, CRC_BLOCKS);
        AccessResult validAppType   = checker.checkAppType(chip, AppType.VISITOR_APP);
        AccessResult validBoRole    = checker.checkBoRoleVisitor(chip);
        AccessResult validEvent     = checker.checkEventID(chip.getEventID(), ConfigAccess.getEventID(this));
        AccessResult blackListed    = checker.checkBlackList(chipUID, blackList);
        AccessResult chipBlockState = checker.checkChipBlockState(chip);
        AccessResult zoneAccess     = checker.checkZoneAccess(chip, activeArea, ConfigAccess.getZoneCheckInDelay(this));
        Set<Long> validRoleIDs      = checker.getValidVisitorRoles(chip, activeArea);
        AccessResult validTime      = checker.checkValidTime(validRoleIDs, activeArea);
        AccessResult validRoleID    = validRoleIDs.size() > 0 ? new AccessResult(PASSED) :
                new AccessResult(BLOCKED, "Has no valid ticket roles!");

        // generate result
        AccessResult[] results = null;
        if(activeArea.isZone() && !activeArea.isCheckIn()){
            if (blackListed.getAccessState() == BANNED)
                results = new AccessResult[] {validCRC, validAppType, validBoRole, validEvent, blackListed, chipBlockState};
            else
                results = new AccessResult[] {validCRC, validAppType, validBoRole, validEvent, chipBlockState};
        } else {
            results = new AccessResult[] {validCRC, validAppType, validEvent, validBoRole, blackListed, validEvent, chipBlockState, validRoleID, validTime, zoneAccess};
        }
        
        // check the results
        accessResult = checker.validateResult(results);

        
        // update chip if neccessery
        AccessResult updateChipResult = new AccessResult(PASSED);
        if (accessResult && (zoneAccess.getAccessState() == CHECKED_IN || zoneAccess.getAccessState() == CHECKED_OUT)){
            //set zone id for check in, and 0 for check out
            long areaInID = activeArea.isCheckIn() ? activeArea.getAreaID() : 0;
            Log.d(LOG_TAG, "Updating Chip Zone information to Zone '"+ areaInID +"'");
            
            if (!upDateChipZoneInfo(chip, areaInID)){
                accessResult = false;
                updateChipResult = new AccessResult(BLOCKED, "Chip could not be updated!");
            }
        }

        // react on result
        if (accessResult) {
            Log.d(LOG_TAG, "Access granted.");
            this.accessResultMessageID = R.string.hint_chip_access_granted;
            onGrantAccess(chip.getUID(), activeArea.getAreaID(), zoneAccess.getAccessState(), "");
        } else {
            Log.w(LOG_TAG, "Access denied!");
            
            String resultMessage = checker.failMessage(validCRC, validAppType, validEvent, blackListed, 
                    chipBlockState, validRoleID, validTime, zoneAccess, updateChipResult);
            
            if (validCRC.getAccessState() == BLOCKED){
                Log.w(LOG_TAG, "CRC Error! Chip is currpted.");
                txLogger.curruptedCRC(chip.getUID());
                this.accessResultMessageID = R.string.hint_chip_access_denied_fatal;
                onCorruption(chip.getUID(), activeArea.getAreaID(), BLOCKED, resultMessage);
            } else if (validAppType.getAccessState() == BLOCKED){
                Log.w(LOG_TAG, "Invalid App Type!");
                txLogger.invalidAppType(chip.getUID());
                this.accessResultMessageID = R.string.hint_chip_access_denied_fatal;
                onCorruption(chip.getUID(), activeArea.getAreaID(), BLOCKED, resultMessage);
            } else if (blackListed.getAccessState() == BANNED){
                this.accessResultMessageID = R.string.hint_chip_access_denied_banned;
                onBanned(chip.getUID(), activeArea.getAreaID(), BLOCKED, resultMessage);
            } else {
                this.accessResultMessageID = R.string.hint_chip_access_denied;
                onDenieAccess(chip.getUID(), activeArea.getAreaID(), BLOCKED, resultMessage);
            }
        }
    }
    

    /**
     * acts when access is granted
     */
    private void onGrantAccess(String chipID, long areaID, AccessState accessState, String message) {
        txLogger.accessControll(chipID, areaID, accessState, message);
        stateIndicator.setBackgroundColor(this.getResources().getColor(R.color.permitted_background));
        stateIndicator.setText(activeArea.getAreaTitle() + "\n" + getResources().getString(accessResultMessageID));
        restartService();
    }

    /**
     * acts when access is denied
     */
    private void onDenieAccess(String chipID, long areaID, AccessState accessState, String message) {
        txLogger.accessControll(chipID, areaID, accessState, message);
        stateIndicator.setBackgroundColor(this.getResources().getColor(R.color.denied_background));
        stateIndicator.setText(activeArea.getAreaTitle() + "\n" + getResources().getString(accessResultMessageID));
        restartService();
    }
    
    /**
     * acts when access is denied
     */
    private void onBanned(String chipID, long areaID, AccessState accessState, String message) {
        txLogger.accessControll(chipID, areaID, accessState, message);
        stateIndicator.setBackgroundColor(this.getResources().getColor(R.color.denied_background));
        stateIndicator.setText(activeArea.getAreaTitle() + "\n" + getResources().getString(accessResultMessageID));
        restartService();
    }
    
    
    /**
     * acts when access is denied
     */
    private void onCorruption(String chipID, long areaID, AccessState accessState, String message) {
        txLogger.accessControll(chipID, areaID, accessState, message);
        stateIndicator.setBackgroundColor(this.getResources().getColor(R.color.denied_background));
        stateIndicator.setText(activeArea.getAreaTitle() + "\n" + getResources().getString(accessResultMessageID));
        restartService();
    }
    
    
    private void restartService(){
        if (checkAppState()) {
            restartReadingHandler.postDelayed(restartReadingRunnable, accessResultDisplayDuration); 
        } else {
            showDisableMessage();
            disableApp();
        }
    }

    
    /**
     * updates the chip data with zone entry info and writes it to the chip
     * @param chipData The chip to update
     * @param areaID The area to enter
     * @return true if the updating process was successful, else false
     */
    private boolean upDateChipZoneInfo(VisitorChip chipData, long areaID){
        Date date = Calendar.getInstance().getTime();
        chipData.setInAreaID(areaID);
        chipData.setInAreaTime(date);
        return ChipReaderService.writeDataToChip(chipData);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.restartReadingHandler = new Handler();

        Intent intent = getIntent();
        this.activeArea = (AreaConfig) intent.getSerializableExtra(INTENT_EXTRA_GATE_CONFIG);
        String userID     = intent.getStringExtra(INTENT_EXTRA_USER_ID);
        this.blackList  = ConfigAccess.getBlackList(this);

        this.txLogger = new TxGateLogger(this, userID);
        this.accessResultMessageID = R.string.hint_chip_invalid_key;
        this.requestChipMessage   = activeArea.getAreaTitle() + "\n" + getResources().getString(R.string.hint_request_chip);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.reset_menu, menu);
        return true;
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        this.accessResultDisplayDuration = ConfigAccess.getLightDuration(this);
        registerReceiver(blackListReceiver, new IntentFilter(BLACKLIST_UPDATED_INTENT));
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(blackListReceiver);
    }

}
