package com.youchip.youmobile.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.youchip.youmobile.R;
import com.youchip.youmobile.controller.chipIO.ChipReaderActivity;
import com.youchip.youmobile.controller.gate.AccessChecker;
import com.youchip.youmobile.controller.network.BlackListConfigUpdateService;
import com.youchip.youmobile.controller.network.LogSyncForcer;
import com.youchip.youmobile.controller.network.LogSyncService;
import com.youchip.youmobile.controller.network.UpdateServiceStarter;
import com.youchip.youmobile.controller.network.serviceInterface.ConfigUpdateService;
import com.youchip.youmobile.controller.settings.ConfigAccess;
import com.youchip.youmobile.controller.settings.ResetConfigActivity;
import com.youchip.youmobile.controller.settings.SettingsInfoBox;
import com.youchip.youmobile.controller.txlog.AccessResult;
import com.youchip.youmobile.controller.txlog.TxLogger;
import com.youchip.youmobile.model.chip.interfaces.BasicChip;
import com.youchip.youmobile.model.chip.interfaces.ChipField;
import com.youchip.youmobile.model.chip.interfaces.VisitorChip;
import com.youchip.youmobile.model.chip.mc1kImpl.MC1KChipSpecs.AppType;
import com.youchip.youmobile.model.chip.mc1kImpl.MC1KVisitorChip;
import com.youchip.youmobile.model.gate.BlockedChip;
import com.youchip.youmobile.utils.AlertBox;
import com.youchip.youmobile.utils.DataConverter;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.youchip.youmobile.controller.IntentExtrasKeys.BASIC_CONFIG_UPDATED_INTENT;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_BO_ROLE_ADMIN;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_BO_ROLE_EMPLOYEE;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_BO_ROLE_SUPERVISOR;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_CHIP_KEY_A;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_USER_ID;
import static com.youchip.youmobile.controller.network.BlackListConfigUpdateService.BLACKLIST_UPDATED_INTENT;
import static com.youchip.youmobile.model.chip.mc1kImpl.MC1KChipSpecs.FactoryFields.APPTYPE;
import static com.youchip.youmobile.model.chip.mc1kImpl.MC1KChipSpecs.FactoryFields.EVENT_ID;
import static com.youchip.youmobile.model.chip.mc1kImpl.MC1KChipSpecs.FactoryFields.UID;
import static com.youchip.youmobile.model.chip.mc1kImpl.MC1KVisitorChipField.IN_AREA_ID;
import static com.youchip.youmobile.model.chip.mc1kImpl.MC1KVisitorChipField.VISITOR_ROLES;

public class MainActivity extends ChipReaderActivity {
    
    
    private static final long CHIP_RESULT_DISPLAY_DELAY = 3000;
    
    private static final Set<Integer> STATUS_BLOCKS = DataConverter.getRelevantBlocks(Arrays.asList(new ChipField[] 
            {UID, EVENT_ID, APPTYPE, IN_AREA_ID, VISITOR_ROLES }));
    
    private static final Set<Integer> CRC_BLOCKS = DataConverter.getRelevantBlocks(Arrays.asList(new ChipField[] 
            {EVENT_ID, APPTYPE}));
    
    private TxLogger txLogger;
    
    private volatile List<BlockedChip> blackList;
    
    public static UpdateServiceStarter logSyncServiceStarter = null;
    private UpdateServiceStarter blackListServiceStarter = null;
    
    
    private AccessChecker checker = new AccessChecker();

    private Handler restartReadingHandler = new Handler();
    
    private Runnable restartReadingRunnable = new Runnable() {
        @Override
        public void run() {          
            if (restartService) {
                Log.d(LOG_TAG,"Restarting ChipReadingService with delay");
                stateIndicator.setBackgroundColor(MainActivity.this.getResources().getColor(R.color.idle_background));
                restartChipServiceRead();
            }
        }
    };
    
    private BroadcastReceiver blackListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent service) {
            Log.d(LOG_TAG, "BlackList Update broadcast received");
            blackList = ConfigAccess.getBlackList(MainActivity.this);
        }
    };
    
    private BroadcastReceiver updatedConfigReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent service) {
            Log.d(LOG_TAG, "BlackList Update broadcast received");
            updateServicesConfig();
        }
    };

    private BroadcastReceiver logoutReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if ((intent != null) && (intent.getAction() != null) && intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                Intent newIntent = new Intent(MainActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(newIntent);
            }
        }

    };


    private void updateServicesConfig(){
        blackListServiceStarter.setDelay(ConfigAccess.getBlackListUpdateDelay(this));
        ConfigUpdateService blService = blackListServiceStarter.getService();
        blService.setDeviceID(ConfigAccess.getDeviceID(MainActivity.this));
        blService.setEventID(ConfigAccess.getEventID(this));
        blService.setServiceUrl(ConfigAccess.getServiceURL(this));
        
        logSyncServiceStarter.setDelay(ConfigAccess.getLogSyncDelay(this));
        ConfigUpdateService lsService = logSyncServiceStarter.getService();
        lsService.setDeviceID(ConfigAccess.getDeviceID(MainActivity.this));
        lsService.setServiceUrl(ConfigAccess.getServiceURL(this));
    }

    
    @Override
    protected void onValidChipReadResult(Context context, BasicChip rawChip) {
        
        VisitorChip chip = new MC1KVisitorChip(rawChip);
        String chipUID = chip.getUID();
        long currentEventID = ConfigAccess.getEventID(this);
        boolean accessResult = false;

        // check access
        AccessResult validCRC       = checker.checkCRC(chip, CRC_BLOCKS);
        AccessResult validAppType   = checker.checkAppType(chip, AppType.VISITOR_APP);
        AccessResult validBoRole    = checker.checkBoRoleEmployee(chip);
        AccessResult validEvent     = checker.checkEventID(chip.getEventID(), currentEventID);
        AccessResult blackListed    = checker.checkBlackList(chipUID, blackList);

        // check the results
        if (currentEventID > 0){
            accessResult = checker.validateResult(validCRC, validAppType, validBoRole, validEvent, blackListed);
        } else { 
            // if the handheld has the factory default (no loaded config) ignore the eventID
            accessResult = checker.validateResult(validCRC, validAppType, validBoRole, blackListed);
        }

        if (accessResult){
            Log.d(LOG_TAG, "Access granted.");
            onGrantAccess(chip);
        } else {
            Log.w(LOG_TAG, "Access denied!");
            String resultMessage = checker.failMessage(validCRC, validAppType, validEvent);
            txLogger.invalidLogin(chipUID, resultMessage);
            onDenieAccess();
        }
            
    }
    
    private void onGrantAccess(BasicChip chip){
        String uid = chip.getUID();
        stateIndicator.setBackgroundColor(this.getResources().getColor(R.color.permitted_background));
        stateIndicator.setText(this.getResources().getString(R.string.hint_chip_access_granted));
        
        this.txLogger.setOperatorNumber(uid);
        
        Intent intent = new Intent(this, SelectActionActivity.class);
        intent.putExtra(INTENT_EXTRA_USER_ID, uid);

        if(chip.isAdmin()){
            intent.putExtra(INTENT_EXTRA_BO_ROLE_ADMIN, true);
        } else if (chip.isSupervisor()){
            intent.putExtra(INTENT_EXTRA_BO_ROLE_SUPERVISOR, true);
        } else {
            intent.putExtra(INTENT_EXTRA_BO_ROLE_EMPLOYEE, true);
        }

        startActivity(intent);
    }

    private void onDenieAccess(){
        stateIndicator.setBackgroundColor(this.getResources().getColor(R.color.denied_background));
        stateIndicator.setText(this.getResources().getString(R.string.hint_chip_access_denied));
        restartService();
    }
    
    
    @Override
    protected Set<Integer> getStatusBlocks() {
        return STATUS_BLOCKS;
    }
    
    public MainActivity(){
        super();
    }
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.txLogger = new TxLogger(this,"");
        this.restartReadingHandler = new Handler();
        super.keyA = ConfigAccess.getKeyA(this);

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(logoutReceiver, filter);

        ConfigUpdateService logSyncService = new LogSyncService(this, this.txLogger, ConfigAccess.getLogFileDirectory(this));
        logSyncServiceStarter  = new UpdateServiceStarter(logSyncService, ConfigAccess.getLogSyncDelay(this));
        
        ConfigUpdateService blackListService = new BlackListConfigUpdateService(this, this.txLogger);      
        blackListServiceStarter  = new UpdateServiceStarter(blackListService, ConfigAccess.getBlackListUpdateDelay(this));

        updateServicesConfig();
        registerReceiver(updatedConfigReceiver, new IntentFilter(BASIC_CONFIG_UPDATED_INTENT));

        blackListServiceStarter.startService();

        if (LogSyncForcer.isUninitialized() || LogSyncForcer.isFinished()) {
            logSyncServiceStarter.startService();
        }
        
        super.onCreate(savedInstanceState);
        super.requestChipMessage   = getResources().getString(R.string.hint_request_login_chip);
    }
    
    @Override
    public void onDestroy(){
        super.onDestroy();
        logSyncServiceStarter.stopService();
        blackListServiceStarter.stopService();
        unregisterReceiver(updatedConfigReceiver);
        unregisterReceiver(logoutReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.reset_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_info:
                SettingsInfoBox.showInfoWindow(this);
                break;
            case R.id.action_reset_config:
                Intent intent = new Intent(this, ResetConfigActivity.class);
                intent.putExtra(INTENT_EXTRA_CHIP_KEY_A, ConfigAccess.CONFIG_DEFAULT_KEY_A);
                startActivityForResult(intent,ResetConfigActivity.INTENT_REQUEST_CONFIG_RESET);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch(requestCode) {
            case ResetConfigActivity.INTENT_REQUEST_CONFIG_RESET:
                if(resultCode == RESULT_OK){
                    AlertBox.allertOnInfo(this,R.string.title_activity_reset_config,R.string.hint_config_reset,
                            new DialogInterface.OnClickListener(){

                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    ConfigAccess.reset(MainActivity.this, MainActivity.this);

                                    Intent intentBL = new Intent(BLACKLIST_UPDATED_INTENT);
                                    MainActivity.this.sendBroadcast(intentBL);

                                    Intent intentCFG = new Intent(BASIC_CONFIG_UPDATED_INTENT);
                                    MainActivity.this.sendBroadcast(intentCFG);
                                }
                            }
                            );
                } else {
                    AlertBox.allertOnWarning(this,R.string.failed_title,R.string.hint_config_reset_fail);
                }
                break;
            default:
                break;
        }

    }
    
    @Override
    protected void onResume() {
        super.onResume();
        super.keyA = ConfigAccess.getKeyA(this);
        this.txLogger.setOperatorNumber("");
        blackList = ConfigAccess.getBlackList(MainActivity.this);
        registerReceiver(blackListReceiver, new IntentFilter(BLACKLIST_UPDATED_INTENT));
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(blackListReceiver);
    }
    
    private void restartService(){
        if (checkAppState()) {
            restartReadingHandler.postDelayed(restartReadingRunnable, CHIP_RESULT_DISPLAY_DELAY); 
        } else {
            showDisableMessage();
            disableApp();
        }
    }

    protected void disableApp(){
        //
    }
}
