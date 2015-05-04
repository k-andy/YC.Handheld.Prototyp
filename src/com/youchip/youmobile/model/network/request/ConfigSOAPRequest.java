package com.youchip.youmobile.model.network.request;

import java.util.Hashtable;

import org.ksoap2.serialization.PropertyInfo;

import com.youchip.youmobile.controller.network.serviceInterface.SOAPRequest;

public class ConfigSOAPRequest implements SOAPRequest{
    
    /** method */
    private final String NAMESPACE;
    private final String METHOD_NAME;
    private final String ACTION_PACKAGE;
    
    /** method parameter */
    private final String DEVICE_ID;
    private final String SHARED_SECRET = "?";
    private final String EVENT_ID;
    
    
    public ConfigSOAPRequest(SoapRequestData data, String deviceID, long eventID){
        this.NAMESPACE = data.getNamespace();
        this.METHOD_NAME = data.getMethodName();
        this.ACTION_PACKAGE = data.getActionPackage();
        this.EVENT_ID = String.valueOf(eventID);
        this.DEVICE_ID = deviceID;
    }
    
    public ConfigSOAPRequest(SoapRequestData data, String deviceID){
        this(data, deviceID, 0);
    }
    
    @Override
    public final String getNameSpace(){
        return NAMESPACE;
    }
    
    @Override
    public final String getMethodName(){
        return METHOD_NAME;
    }
    
    @Override
    public final String getAction() {
        return getNameSpace() + ACTION_PACKAGE + "/" + getMethodName();
    }
    
    @Override
    public Object getProperty(int arg0) {
        switch(arg0)
        {
        case 1:
            return DEVICE_ID;
        case 2:
            return SHARED_SECRET;
        case 0:
            return EVENT_ID;
        }
        
        return null;
    }

    @Override
    public int getPropertyCount() {
        return 3;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void getPropertyInfo(int index, Hashtable arg1, PropertyInfo info) {
        switch(index){
        case 1:
            info.type = PropertyInfo.STRING_CLASS;
            info.name = "IP";
            break;
        case 2:
            info.type = PropertyInfo.STRING_CLASS;
            info.name = "SharedSecret";
            break;
        case 0:
            info.type = PropertyInfo.STRING_CLASS;
            info.name = "idEvent";
            break;
        default:break;
        }
    }

    @Override
    public void setProperty(int arg0, Object arg1) {
        throw new UnsupportedOperationException();
    }


    public String getIp() {
        return DEVICE_ID;
    }
    
    public String getSharedSecret(){
        return SHARED_SECRET;
    }
    
    public String getEventID(){
        return EVENT_ID;
    }
}
