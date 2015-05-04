package com.youchip.youmobile.model.network.response;

import java.io.Serializable;

import org.ksoap2.serialization.SoapObject;

import com.youchip.youmobile.controller.network.serviceInterface.SOAPResponse;

public class LogSyncSOAPResponse  implements SOAPResponse, Serializable{
    
    private static final long serialVersionUID = 3223573207775918087L;
    
    private boolean result = false; 
    
    public LogSyncSOAPResponse(){}
    
    public LogSyncSOAPResponse(SoapObject response){
        setSOAPResponse(response);
    }

    @Override
    public void setSOAPResponse(SoapObject response) {
        SoapObject keyValuePair = (SoapObject) response.getProperty(0);
        String key = keyValuePair.getPrimitivePropertyAsString("Key");
        if (key.equals(LogSyncFields.LogSyncResult.toString())){
            String value = keyValuePair.getPrimitivePropertyAsString("Value");
            result = value.equals("true");
        } else {
            result = false;
        }
    }

    public boolean getResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }
    
    private enum LogSyncFields{
        LogSyncResult
    }

}
