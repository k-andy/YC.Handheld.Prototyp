package com.youchip.youmobile.controller.txlog;

import com.youchip.youmobile.controller.txlog.TxGateLogger.AccessState;


/**
 * This class represents a result of a acces request for Areas and Zones
 * @author muelleco
 *
 */
public class AccessResult {

    private AccessState accessState;
    private String accessMessage;
    
    public AccessResult(AccessState accessState, String accessMessage){
        this.setAccessMessage(accessMessage);
        this.setAccessState(accessState);
    }
    
    public AccessResult(AccessState accessState){
        this.setAccessMessage("");
        this.setAccessState(accessState);
    }

    public AccessState getAccessState() {
        return accessState;
    }

    public void setAccessState(AccessState accessState) {
        this.accessState = accessState;
    }

    public String getAccessMessage() {
        return accessMessage;
    }

    public void setAccessMessage(String accessMessage) {
        this.accessMessage = accessMessage;
    }
}
