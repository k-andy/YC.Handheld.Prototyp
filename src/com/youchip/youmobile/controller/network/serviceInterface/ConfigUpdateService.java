package com.youchip.youmobile.controller.network.serviceInterface;

import android.content.Context;

public interface ConfigUpdateService {
   
    public void update();
    
    
    public String getServiceUrl();

    public long getEventID();

    public void setServiceUrl(String url);

    public void setEventID(long eventID);
    
    public String getDeviceID();

    public void setDeviceID(String deviceID);

    public boolean isRunning();
    
}
