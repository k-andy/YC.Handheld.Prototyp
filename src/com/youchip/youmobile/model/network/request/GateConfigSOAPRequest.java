package com.youchip.youmobile.model.network.request;

public class GateConfigSOAPRequest implements SoapRequestData{
    
    /** method */
    private static final String NAMESPACE         = "http://tempuri.org/";
    private static final String METHOD_NAME       = "GateConfigurationGet"; 
    private static final String ACTION_PACKAGE    = "IHandHeld";
    
    public String getNamespace() {
        return NAMESPACE;
    }
    public String getMethodName() {
        return METHOD_NAME;
    }
    public String getActionPackage() {
        return ACTION_PACKAGE;
    }

}
