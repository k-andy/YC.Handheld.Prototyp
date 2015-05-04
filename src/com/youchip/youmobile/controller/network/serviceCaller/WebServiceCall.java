package com.youchip.youmobile.controller.network.serviceCaller;

import java.io.IOException;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

import com.youchip.youmobile.controller.network.serviceInterface.SOAPRequest;
import com.youchip.youmobile.controller.network.serviceInterface.SOAPResponse;

public class WebServiceCall {

    private String serviceURL;
    
    public WebServiceCall(){
    }
    
    public WebServiceCall(String serviceURL){
        this.serviceURL = serviceURL;
    }
    
    
    public boolean callService(SOAPRequest soapRequestData, SOAPResponse response) throws XmlPullParserException, Exception{
        
        Log.d(WebServiceCall.this.getClass().getName(), "Creating SOAP envelope");
        SoapObject request = new SoapObject(soapRequestData.getNameSpace(), soapRequestData.getMethodName());
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        HttpTransportSE androidHttpTransport = new HttpTransportSE(serviceURL);
        androidHttpTransport.debug = true;

        Log.d(WebServiceCall.this.getClass().getName(), "Adding SOAP properties");
        // add input parameter
        for (int i=0; i< soapRequestData.getPropertyCount(); i++){
            PropertyInfo propertyInfo = new PropertyInfo();
            soapRequestData.getPropertyInfo(i,null,propertyInfo);
            propertyInfo.setValue(soapRequestData.getProperty(i));
            request.addProperty(propertyInfo);
            Log.d(WebServiceCall.this.getClass().getName(), "Property added: " + propertyInfo.getName() );
        }
        
        // call service
        Log.d(WebServiceCall.this.getClass().getName(), "Calling webservice..");
        androidHttpTransport.call(soapRequestData.getAction(), envelope);
        
        // use response
        if (envelope.bodyIn instanceof SoapObject) {
            Log.d(WebServiceCall.this.getClass().getName(), "Processing valid SOAP response");
            SoapObject requestResponse = (SoapObject) envelope.getResponse();
            response.setSOAPResponse(requestResponse);
        } else if (envelope.bodyIn instanceof SoapFault){
            SoapFault soapFault = (SoapFault) envelope.bodyIn;
//            Log.w(WebServiceCall.this.getClass().getName(), soapFault.getMessage());
            Log.w(WebServiceCall.this.getClass().getName(), "Processing invalid SOAP response");
            throw new IOException(soapFault.getMessage());
        }
             
        return true;
    }
    
    public String getServiceURL() {
        return serviceURL;
    }

    public void setServiceURL(String serviceURL) {
        this.serviceURL = serviceURL;
    }
    

}
