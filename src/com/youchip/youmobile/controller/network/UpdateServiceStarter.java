package com.youchip.youmobile.controller.network;

import com.youchip.youmobile.R;
import com.youchip.youmobile.controller.network.serviceInterface.ConfigUpdateService;
import com.youchip.youmobile.controller.network.serviceInterface.ConfigUpdateServiceStarter;
import com.youchip.youmobile.utils.AlertBox;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Random;

public class UpdateServiceStarter implements ConfigUpdateServiceStarter {
    
    private final static String CURRENT_CLASS = UpdateServiceStarter.class.getName();
    
    private Handler restartLoaderHandler = new Handler();
    private volatile long updateDelay = 3000;
    public static long DEFAULT_UPDATE_DELAY = 3000;
    private ConfigUpdateService service;

    
    public UpdateServiceStarter(ConfigUpdateService service, long updateDelay){
        this.service = service;
        this.updateDelay = updateDelay;

    }
    
    private UpdateServiceStarter(ConfigUpdateService service) {
        this(service, DEFAULT_UPDATE_DELAY);
    }
    

    private Runnable serviceLoader = new Runnable() {
        public void run() {
                service.update();
                restartLoaderHandler.postDelayed(this, updateDelay);
        }
    };
    
    public void startService(){
        Log.d(CURRENT_CLASS, "Starting Service " + service.getClass().getName());
        restartLoaderHandler.post(serviceLoader);
    }
    
    public void stopService(){
        Log.d(CURRENT_CLASS, "Stopping Service " + service.getClass().getName());
        restartLoaderHandler.removeCallbacks(serviceLoader);
    }

    public boolean isRunning(){
        return this.service.isRunning();
    }

    
    public long getDelay() {
        return updateDelay;
    }

    public void setDelay(long updateDelay) {
        this.updateDelay = updateDelay;
    }

    public ConfigUpdateService getService() {
        return service;
    }

    public void setService(BlackListConfigUpdateService service) {
        this.service = service;
    }

}
