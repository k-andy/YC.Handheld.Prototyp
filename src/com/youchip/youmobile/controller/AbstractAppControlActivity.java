package com.youchip.youmobile.controller;

import com.youchip.youmobile.utils.AlertBox;

import android.app.Activity;
import android.os.Bundle;


public abstract class AbstractAppControlActivity extends Activity {
    
    private AppStateChecker appStateChecker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.appStateChecker = new AppStateChecker(this);
    }

    @Override
    protected void onPause(){
        super.onPause();
        AlertBox.dismiss();
    }

    protected boolean checkAppState(){
        return this.appStateChecker.checkAppState();
    }

    protected String getAppStateMessage(){
        return this.appStateChecker.getAppStateMessage();
    }

    protected void showDisableMessage(){
        this.appStateChecker.showDisableMessage();
    }

    protected  void disableApp(){
        this.appStateChecker.disableApp();
    }

    protected void showWiFiSettingsDialog(){
        this.appStateChecker.showWiFiSettingsDialog();
    }

}
