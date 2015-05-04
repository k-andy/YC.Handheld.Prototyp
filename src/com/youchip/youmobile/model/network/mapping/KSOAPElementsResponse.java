package com.youchip.youmobile.model.network.mapping;

import java.util.Hashtable;

import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.PropertyInfo;

public class KSOAPElementsResponse implements KvmSerializable{

    @SuppressWarnings("rawtypes")
    public static final Class RESPONSE_CLASS = KSOAPElementsResponse.class;
    private KSOAPElements elements = new KSOAPElements();

    @Override
    public Object getProperty(int arg0) {
        switch (arg0){
        case 0:
             return this.elements;
        default:
            return null;
        }
    }

    @Override
    public int getPropertyCount() {
        return 1;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void getPropertyInfo(int arg0, Hashtable arg1, PropertyInfo info) {
        switch(arg0){
        case 0:
            info.name = "elements";
            info.type = KSOAPElements.VECTOR_CLASS;
            break;
        default: break;
    }
    }

    @Override
    public void setProperty(int arg0, Object value) {
        switch(arg0){
        case 0: 
            this.elements = (KSOAPElements) value;
        default:
            break;
        }  
    }
    
}
