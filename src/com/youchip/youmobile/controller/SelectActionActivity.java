package com.youchip.youmobile.controller;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.youchip.youmobile.R;
import com.youchip.youmobile.controller.gate.GateMainActivity;
import com.youchip.youmobile.controller.helpdesk.HelpDeskMainActivity;
import com.youchip.youmobile.controller.network.ChainConfigServiceCaller;
import com.youchip.youmobile.controller.network.LogSyncForcer;
import com.youchip.youmobile.controller.report.ReportActivity;
import com.youchip.youmobile.controller.settings.ConfigAccess;
import com.youchip.youmobile.controller.settings.ConfigChipLoadActivity;
import com.youchip.youmobile.controller.settings.SettingsInfoBox;
import com.youchip.youmobile.controller.shop.ShopMainActivity;
import com.youchip.youmobile.controller.ticket.SelectTicketActivity;
import com.youchip.youmobile.controller.txlog.TxLogger;
import com.youchip.youmobile.utils.AlertBox;
import com.youchip.youmobile.utils.NetWorkInfo;

import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_BO_ROLE_ADMIN;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_BO_ROLE_EMPLOYEE;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_BO_ROLE_SUPERVISOR;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_CHIP_KEY_A;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_CHIP_UID;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_USER_ID;
import static com.youchip.youmobile.controller.settings.ConfigChipLoadActivity.INTENT_REQUEST_CONFIG_CHIP_LOAD;

public class SelectActionActivity extends AbstractAppControlActivity {
    private boolean isBoAdmin = false;
    private boolean isBoSupervisor = false;

    private String userID = "";

    private TextView noConfigHint;
    private Button cashPointButton;
    private Button shopModeButton; 
    private Button gateModeButton;
    private Button helpDeskButton;
    private Button ticketXCButton;
    private Button seeReport;


    private ChainConfigServiceCaller chainConfigServiceLoader = null;

    private WifiManager wifiManager;
    private TxLogger txLogger;
    
    private OnClickListener onClickSwitchToHelpDeskMode = new OnClickListener(){
        public void onClick(View view){
            startIntent(HelpDeskMainActivity.class);
        }
    };

    private OnClickListener onClickSwitchToGateMode = new OnClickListener(){
        public void onClick(View view){
            startIntent(GateMainActivity.class);
        }
    };
    
    private OnClickListener onClickSwitchToShopMode = new OnClickListener(){
        public void onClick(View view){
            startIntent(ShopMainActivity.class);
        }
    };
    
    private OnClickListener onClickSwitchToCashPointMode = new OnClickListener(){
        public void onClick(View view){
            startIntent(ShopMainActivity.class);
        }
    };

    private OnClickListener onClickSwitchToTicketXCMode = new OnClickListener(){
        public void onClick(View view){
            startIntent(SelectTicketActivity.class);
        }
    };

    private OnClickListener onClickSwitchToReport = new OnClickListener(){
        public void onClick(View view){
            startIntent(ReportActivity.class);
        }
    };

    private void startIntent(Class<?> cls){
        if (checkAppState()) {
            Intent intent = new Intent(this, cls);
            String keyA = ConfigAccess.getKeyA(this);
            intent.putExtra(INTENT_EXTRA_CHIP_KEY_A, keyA);
            intent.putExtra(INTENT_EXTRA_USER_ID, this.userID);

            if(isBoAdmin){
                intent.putExtra(INTENT_EXTRA_BO_ROLE_ADMIN, true);
            } else if (isBoSupervisor){
                intent.putExtra(INTENT_EXTRA_BO_ROLE_SUPERVISOR, true);
            } else {
                intent.putExtra(INTENT_EXTRA_BO_ROLE_EMPLOYEE, true);
            }

            startActivity(intent);
        } else {
            Toast toast = Toast.makeText(this, getAppStateMessage(), Toast.LENGTH_SHORT);
            toast.show();
        }
    }
    
    
    protected void loadConfigChip(){
        Intent intent = new Intent(this, ConfigChipLoadActivity.class);
        intent.putExtra(INTENT_EXTRA_CHIP_KEY_A, ConfigAccess.getKeyAForConfig(this));
        startActivityForResult(intent, INTENT_REQUEST_CONFIG_CHIP_LOAD);   
    }
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_action);

        wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        
        gateModeButton  = ((Button) findViewById(R.id.action_start_gate));
        helpDeskButton  = ((Button) findViewById(R.id.action_start_helpdesk)); 
        shopModeButton  = ((Button) findViewById(R.id.action_start_shop));
        cashPointButton = ((Button) findViewById(R.id.action_start_cashpoint));
        ticketXCButton  = ((Button) findViewById(R.id.action_start_ticketxchange));
        seeReport  = ((Button) findViewById(R.id.action_start_report));
        noConfigHint    = ((TextView) findViewById(R.id.hint_no_config_loaded));

        seeReport.setVisibility(View.GONE);

        Intent intent = getIntent();

        this.userID         = intent.getStringExtra(INTENT_EXTRA_USER_ID);

        if( intent.getBooleanExtra(INTENT_EXTRA_BO_ROLE_ADMIN,false) ){
            this.isBoAdmin = true; this.isBoSupervisor = true;
        }else if (intent.getBooleanExtra(INTENT_EXTRA_BO_ROLE_SUPERVISOR, false)) {
            this.isBoSupervisor = true;
        }

        this.txLogger       = new TxLogger(this, this.userID);
        
        updateRoleDependendPermissions();
        
        gateModeButton.setOnClickListener(onClickSwitchToGateMode);
        helpDeskButton.setOnClickListener(onClickSwitchToHelpDeskMode);
        shopModeButton.setOnClickListener(onClickSwitchToShopMode);
        cashPointButton.setOnClickListener(onClickSwitchToCashPointMode);
        ticketXCButton.setOnClickListener(onClickSwitchToTicketXCMode);
        seeReport.setOnClickListener(onClickSwitchToReport);
    }

    @Override
    public void onResume(){
        super.onResume();
        updateEventDependendPermission(ConfigAccess.getEventID(this));
    }

    @Override
    protected void onPause(){
        super.onPause();

        if (this.chainConfigServiceLoader != null){
            this.chainConfigServiceLoader.dismissDialog();
        }

        LogSyncForcer.dismissDialogs();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.select_action, menu);
        MenuItem debugModeButton = menu.findItem(R.id.action_start_debug);
        MenuItem forceLogSync    = menu.findItem(R.id.action_force_logsync);

        if (isBoAdmin){
            debugModeButton.setVisible(true);
            forceLogSync.setVisible(true);
        } else {
            debugModeButton.setVisible(false);
            forceLogSync.setVisible(false);
        }
        return true;
    }
    
    private void updateRoleDependendPermissions(){
        String appTitle   = getResources().getString(R.string.app_name);
        String loggedInAs = getResources().getString(R.string.title_logged_in_as);

        if (isBoAdmin){
            setTitle(appTitle + " - "+ loggedInAs + " "+ getResources().getString(R.string.title_admin));
        } else if (isBoSupervisor){
            setTitle(appTitle + " - "+ loggedInAs + " "+ getResources().getString(R.string.title_supervisor));
        } else {
            setTitle(appTitle + " - "+ loggedInAs + " "+ getResources().getString(R.string.title_employer));
        }

        if (isBoAdmin || isBoSupervisor) {
            seeReport.setVisibility(View.VISIBLE);
        }
    }
    
    private void updateEventDependendPermission(long eventID){
        if (eventID <= 0){
            noConfigHint.setVisibility(View.VISIBLE);
            gateModeButton.setVisibility(View.INVISIBLE);
            shopModeButton.setVisibility(View.INVISIBLE);
            cashPointButton.setVisibility(View.INVISIBLE);
            helpDeskButton.setVisibility(View.INVISIBLE);
        } else {
            noConfigHint.setVisibility(View.INVISIBLE);
            gateModeButton.setVisibility(View.VISIBLE);
            shopModeButton.setVisibility(View.VISIBLE);
            cashPointButton.setVisibility(View.VISIBLE);
            helpDeskButton.setVisibility(View.VISIBLE);
        }
    }
    


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_info:
                SettingsInfoBox.showInfoWindow(this);
                break;
            case R.id.action_load_settings_from_server:
                if(!wifiManager.isWifiEnabled()) {
                    super.showWiFiSettingsDialog();
                } else {
                    loadConfigChip();
                }
                break;
            case R.id.action_start_debug:
                break;
            case R.id.action_force_logsync:
                if (ConfigAccess.getLogFileCount(this) <= 0){
                    Toast.makeText(this, R.string.hint_sync_not_neccessery, Toast.LENGTH_SHORT).show();
                } else if(!wifiManager.isWifiEnabled()) {
                    super.showWiFiSettingsDialog();
                } else {
                    LogSyncForcer.createNew(this, MainActivity.logSyncServiceStarter);
                    LogSyncForcer.executeService();
                  }
                break;
            case R.id.action_log_off:
                finish();
                break;
            default:
                break;
        }
        return true;
    }




    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch(requestCode) {
            case INTENT_REQUEST_CONFIG_CHIP_LOAD:
                if(resultCode == RESULT_OK){
                    // get ip and save it to the config file
                    ConfigAccess.storeDeviceID(SelectActionActivity.this, NetWorkInfo.getIPAddress(true));
                    
                    String chipUID = intent.getStringExtra(INTENT_EXTRA_CHIP_UID);
                    String serviceURL = ConfigAccess.getServiceURL(this);

                    chainConfigServiceLoader = new ChainConfigServiceCaller(this, this.txLogger, serviceURL);
                    chainConfigServiceLoader.setDeviceID(ConfigAccess.getDeviceID(SelectActionActivity.this));
                    chainConfigServiceLoader.setEventID(ConfigAccess.getEventID(SelectActionActivity.this));
                    chainConfigServiceLoader.setChipUID(chipUID);
                    chainConfigServiceLoader.execute(new Void[0]);
                } else {
                     AlertBox.allertOnWarning(this, R.string.error_load_config_failed_title, R.string.error_reading_failed_title);
                }
                break;
            default: 
                break;
        }
        
    }
    
}
