package com.youchip.youmobile.model.network.response;

import java.util.LinkedList;
import java.util.List;

import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;

import com.youchip.youmobile.controller.network.serviceInterface.SOAPResponse;
import com.youchip.youmobile.model.gate.BlockedChip;
import com.youchip.youmobile.utils.DataConverter;

public class BlackListConfigSOAPResponse implements SOAPResponse {
    
    private final List<BlockedChip> list = new LinkedList<BlockedChip>();
    private final String dateFormat    = DataConverter.getServiceDateFormatString();
    
    public BlackListConfigSOAPResponse(){}
        
    public BlackListConfigSOAPResponse(SoapObject response){
        setSOAPResponse(response);
    }
    
     @Override
    public void setSOAPResponse(SoapObject response) {
        final int m = response.getPropertyCount();
        for (int i=0; i < m ; i++){
            BlockedChip chip = new BlockedChip(); 
            
            SoapObject listEntry = (SoapObject) response.getProperty(i);
            String UID           =  ((SoapPrimitive) listEntry.getProperty(GateConfigFields.UID.toString())).toString();
            String eventID       =  ((SoapPrimitive) listEntry.getProperty(GateConfigFields.idEvent.toString())).toString();
            String banned        =  ((SoapPrimitive) listEntry.getProperty(GateConfigFields.Active.toString())).toString();
            
            Object buNullCheck  = listEntry.getProperty(GateConfigFields.BlockedUntil.toString());
            if (buNullCheck != null){
                String blockUntil   = ((SoapPrimitive) buNullCheck).toString();
                chip.setBlockedUntil(DataConverter.serviceFormatToJavaDate(blockUntil, dateFormat));
            }             
            chip.setUID(UID);
            chip.setEventID(Long.parseLong(eventID));
            chip.setBanned(Long.parseLong(banned) == 0);
            
            
            list.add(chip);
        }
    }
     
     public List<BlockedChip> getResult(){
         return this.list;

     }
    
    private enum GateConfigFields{
        BlockedUntil,
        idEvent,
        idTicket,
        Active,
        UID
    }

}
