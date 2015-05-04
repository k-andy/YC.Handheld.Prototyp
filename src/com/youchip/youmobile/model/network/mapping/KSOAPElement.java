package com.youchip.youmobile.model.network.mapping;

import java.util.Hashtable;

import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.PropertyInfo;

public class KSOAPElement implements KvmSerializable{
    
    @SuppressWarnings("rawtypes")
    public static final Class KSOAP_ELEMENT_CLASS = (new KSOAPElement()).getClass();
    
    private String Key="";
    private String Value="";
    
    public KSOAPElement(){
        
    }
    
    public KSOAPElement(String key, String value){
        this.Key = key;
        this.Value = value;
    }

    @Override
    public Object getProperty(int arg0) {
        switch (arg0){
        case 0:
            return Key;
        case 1:
            return Value;
        default:
            return null;
        }
    }

    @Override
    public int getPropertyCount() {
        return 2;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void getPropertyInfo(int arg0, Hashtable arg1, PropertyInfo info) {
        switch(arg0){
        case 0:
            info.name = "Key";
            info.type = PropertyInfo.STRING_CLASS;
            break;
        case 1:
            info.name = "Value";
            info.type = PropertyInfo.STRING_CLASS;
            break;
        default: break;
        }
    }

    @Override
    public void setProperty(int arg0, Object value) {
        switch(arg0){
        case 0:
            this.Key = value.toString();
            break;
        case 1:
            this.Value = value.toString();
            break;
        default: break;
        }
    }

    public String getKey() {
        return Key;
    }

    public void setKey(String key) {
        this.Key = key;
    }

    public String getValue() {
        return Value;
    }

    public void setValue(String value) {
        this.Value = value;
    }
    
    

}
