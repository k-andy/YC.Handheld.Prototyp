package com.youchip.youmobile.model.network.response;

import java.util.LinkedHashMap;
import java.util.Map;

import org.ksoap2.serialization.SoapObject;

import com.youchip.youmobile.controller.network.serviceInterface.SOAPResponse;

public class BasicConfigSOAPResponse implements SOAPResponse{
    
    private final Map<String, String> list = new LinkedHashMap<String, String>();
    
    public BasicConfigSOAPResponse(){}
        
    public BasicConfigSOAPResponse(SoapObject response){
        setSOAPResponse(response);
    }
    
     @Override
    public void setSOAPResponse(SoapObject response) {
        final int m = response.getPropertyCount();
        for (int i=0; i < m ; i++){
            SoapObject keyValuePair = (SoapObject) response.getProperty(i);
            String key = keyValuePair.getPrimitivePropertyAsString("Key");
            String value = keyValuePair.getPrimitivePropertyAsString("Value");
            this.list.put(key, value);
        }
    }
     
    public Map<String, String> getResultMap(){
        return list;
    }

}

