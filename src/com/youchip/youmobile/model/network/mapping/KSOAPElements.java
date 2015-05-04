package com.youchip.youmobile.model.network.mapping;

import java.util.Hashtable;
import java.util.Vector;

import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.PropertyInfo;

public class KSOAPElements extends Vector<Object> implements KvmSerializable{
    
    private static final long serialVersionUID = 5237899307861291473L;
    
    @SuppressWarnings("rawtypes")
    public static final Class VECTOR_CLASS = KSOAPElements.class;


    @Override
    public Object getProperty(int arg0) {
        return this.get(arg0);
    }

    @Override
    public int getPropertyCount() {
        return 1;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void getPropertyInfo(int arg0, Hashtable arg1, PropertyInfo info) {
        info.name = "a:KeyValueOfstringstring";
        info.type = PropertyInfo.OBJECT_CLASS;
    }


    @Override
    public void setProperty(int arg0, Object value) {
//        SoapObject soapObject = new SoapObject();
//        soapObject = (SoapObject) value;
//
//        KSOAPElement daten = new KSOAPElement();
//        daten.setProperty(0, soapObject.getProperty("Key"));
//        daten.setProperty(1, soapObject.getProperty("Value"));

        this.add(value);
    }

}
