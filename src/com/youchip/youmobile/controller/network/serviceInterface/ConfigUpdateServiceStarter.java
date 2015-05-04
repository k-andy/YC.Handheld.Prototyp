package com.youchip.youmobile.controller.network.serviceInterface;

import com.youchip.youmobile.controller.network.BlackListConfigUpdateService;

public interface ConfigUpdateServiceStarter {

    public void startService();
    
    public void stopService();
    
    
    public long getDelay();

    public void setDelay(long updateDelay);

    public ConfigUpdateService getService();

    public void setService(BlackListConfigUpdateService service);

    public boolean isRunning();
    
}
