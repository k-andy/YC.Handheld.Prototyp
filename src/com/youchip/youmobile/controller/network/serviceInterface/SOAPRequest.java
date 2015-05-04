package com.youchip.youmobile.controller.network.serviceInterface;

import org.ksoap2.serialization.KvmSerializable;


public interface SOAPRequest extends KvmSerializable {
    
    public String getNameSpace();
    
    public String getMethodName();
    
    public String getAction();
    
}
