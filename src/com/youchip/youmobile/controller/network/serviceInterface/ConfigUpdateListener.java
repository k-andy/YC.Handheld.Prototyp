package com.youchip.youmobile.controller.network.serviceInterface;

import java.io.Serializable;

public interface ConfigUpdateListener extends Serializable{
    
    public static final String INTENT_CONFIG_UPDATE_LISTENER ="com.youchip.youmobile.service.config.update.listener";
    
    public boolean onConfigUpdateSuccess();
    
    public boolean onConfigUpdateFail();

}
