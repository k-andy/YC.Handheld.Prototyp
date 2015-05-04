package com.youchip.youmobile.model.chip.mc1kImpl;

import static com.youchip.youmobile.model.chip.mc1kImpl.MC1KConfigChipField.*;

import java.io.UnsupportedEncodingException;

import android.util.Log;

import com.youchip.youmobile.model.chip.interfaces.Chip;
import com.youchip.youmobile.model.chip.interfaces.ConfigChip;
import com.youchip.youmobile.utils.DataConverter;

public class MC1KConfigChip extends MC1KBasicChip implements ConfigChip{

    private static final long serialVersionUID = -2628347535618757153L;

    /**
     * creates a full blocked (64 block a 16 byte)
     * RFID Chip Data Object
     */
    public MC1KConfigChip(){
        super();
    }
    
    
    
    /**
     * saves a copy of the raw data array, widened to full block size (a multiple of 16 byte)
     * @param rawData
     * @param serialNumber
     */
    public MC1KConfigChip(Chip chip){
        super(chip);
    }    
    
    

    @Override
    public String getServiceHost() {
        try {
            
            StringBuilder svc = new StringBuilder();
            svc.append(DataConverter.byteArrayToString(retrieveSingleChipData(SVC_PROTOCOLL)));
            svc.append("://");
            
            byte[] ip = retrieveSingleChipData(SVC_IP);
            svc.append(DataConverter.uByteToInt(ip[0]));
            svc.append(".");
            svc.append(DataConverter.uByteToInt(ip[1]));
            svc.append(".");
            svc.append(DataConverter.uByteToInt(ip[2]));
            svc.append(".");
            svc.append(DataConverter.uByteToInt(ip[3]));
            
            svc.append(":");
            svc.append(DataConverter.byteArrayToInt(retrieveSingleChipData(SVC_PORT)));
            svc.append("/");
            
            String subdomain = DataConverter.byteArrayToString(retrieveSingleChipData(SVC_SUBDOMAIN));
            if (!subdomain.isEmpty()){
                svc.append(subdomain);
                svc.append("/");
            }
            
            return svc.toString();
        } catch (UnsupportedEncodingException uee) {
            Log.getStackTraceString(uee);
            return "";
        }
    }


    @Override
    public String getServiceName() {
        try {
            return DataConverter.byteArrayToString(retrieveSingleChipData(SVC_NAME));
        } catch (UnsupportedEncodingException uee) {
            Log.getStackTraceString(uee);
            return "";
        }
    }


    @Override
    public void setServiceHost(String value) {
//        try {
//            byte[] fieldData = DataConverter.StringToByteArray(value);
//            setMultiField(fieldData, SERVICE_HOST, SERVICE_HOST_LONG);
//        } catch (UnsupportedEncodingException uee) {
//            Log.getStackTraceString(uee);
//        }        
    }


    @Override
    public void setServiceName(String value) {
//        try {
//            byte[] fieldData;
//            fieldData = DataConverter.StringToByteArray(value);
//            updateSingleChipData(SERVICE_NAME,fieldData);  
//        } catch (UnsupportedEncodingException uee) {
//            Log.getStackTraceString(uee);
//        }   
    }

}
