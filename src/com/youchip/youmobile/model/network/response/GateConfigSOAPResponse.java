package com.youchip.youmobile.model.network.response;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;

import com.youchip.youmobile.controller.network.serviceInterface.SOAPResponse;
import com.youchip.youmobile.model.gate.AreaConfig;
import com.youchip.youmobile.model.gate.VisitorRole;
import com.youchip.youmobile.utils.DataConverter;


public class GateConfigSOAPResponse implements SOAPResponse, Serializable{
    
    private static final long serialVersionUID = 1487221627999796371L;
    
    private final Map<AreaConfig, AreaConfig> gateConfigs = new LinkedHashMap<>();
    private final String dateFormat    = DataConverter.getServiceDateFormatString();
    
    public GateConfigSOAPResponse(){}
        
    public GateConfigSOAPResponse(SoapObject response){
        setSOAPResponse(response);
    }
    
    @Override
    public void setSOAPResponse(SoapObject response) {
        final int m = response.getPropertyCount();
        for (int i=0; i < m ; i++){
            
            SoapObject soapGateConfig = (SoapObject) response.getProperty(i);
            
            // extract data from soap object
            String areaTitle  = ((SoapPrimitive) soapGateConfig.getProperty(GateConfigFields.AreaTitel.toString())).toString();
            String timeStart  = ((SoapPrimitive) soapGateConfig.getProperty(GateConfigFields.GueltigVon.toString())).toString();
            String timeStop   = ((SoapPrimitive) soapGateConfig.getProperty(GateConfigFields.GueltigBis.toString())).toString();
            String areaID     = ((SoapPrimitive) soapGateConfig.getProperty(GateConfigFields.idArea.toString())).toString();
            String isZone     = ((SoapPrimitive) soapGateConfig.getProperty(GateConfigFields.isZone.toString())).toString();
            String role       = ((SoapPrimitive) soapGateConfig.getProperty(GateConfigFields.Rolle.toString())).toString();
            String eventID    = ((SoapPrimitive) soapGateConfig.getProperty(GateConfigFields.idEvent.toString())).toString();
            String roleID     = ((SoapPrimitive) soapGateConfig.getProperty(GateConfigFields.idRole.toString())).toString();
            
            //set up standard gate entry
            AreaConfig gateConfig           = bakeStandardGateConfig(areaTitle, areaID, isZone, eventID);
            
            //set up additional gate (zone in/out) entry
            AreaConfig gateConfigZoneOut    = bakeZoneOutConfig(gateConfig);

            //set up standard role entry
            VisitorRole visitorRole         = bakeVisitorRole(timeStart, timeStop, role, roleID);
            
            // puts or changes the gate config entry
            if (!gateConfigs.containsKey(gateConfig)) {
                gateConfigs.put(gateConfig, gateConfig);
            }
            gateConfigs.get(gateConfig).addVisitorRole(visitorRole);
            
            
            if (gateConfig.isZone()){
                if (!gateConfigs.containsKey(gateConfigZoneOut)) {
                    gateConfigs.put(gateConfigZoneOut, gateConfigZoneOut);
                }
                gateConfigs.get(gateConfigZoneOut).addVisitorRole(visitorRole);
            }
        }
    }
    
    private AreaConfig bakeStandardGateConfig(String areaTitle, String areaID, String isZone, String eventID){
        //set up standard gate entry
        AreaConfig gateConfig   = new AreaConfig();
        gateConfig.setAreaTitle(areaTitle);
        gateConfig.setAreaID(Long.parseLong(areaID));
        gateConfig.setZone(Long.parseLong(isZone) != 0);
        gateConfig.setEventID(Long.parseLong(eventID));
        return gateConfig;
    }
    
    private VisitorRole bakeVisitorRole(String timeStart, String timeStop, String role, String roleID){        
        //set up standard role entry
        VisitorRole visitorRole = new VisitorRole();
        visitorRole.setValidTimeStart(DataConverter.serviceFormatToJavaDate(timeStart, dateFormat));
        visitorRole.setValidTimeStop(DataConverter.serviceFormatToJavaDate(timeStop,  dateFormat));
        visitorRole.setRoleName(role);
        visitorRole.setRoleID(roleID.charAt(0));      
        return visitorRole;
    }
    
    private AreaConfig bakeZoneOutConfig(AreaConfig gateConfig){
        //set up additional gate (zone in/out) entry
        AreaConfig gateConfigZoneOut = new AreaConfig(gateConfig);
        if (gateConfig.isZone()){               
            gateConfig.setAreaTitle(gateConfig.getAreaTitle() + " (in)");
            gateConfig.setCheckIn(true);
            
            gateConfigZoneOut.setAreaTitle(gateConfigZoneOut.getAreaTitle() + " (out)");
            gateConfigZoneOut.setCheckIn(false);
        }      
        
        return gateConfigZoneOut;
    }
     
     
    public List<AreaConfig> getResult(){
        return new LinkedList<AreaConfig>(this.gateConfigs.values());
    } 
 
    
    private enum GateConfigFields{
        AreaTitel,
        GueltigBis,
        GueltigVon,
        Rolle,
        idArea,
        idEvent,
        idRole,
        isZone,
        Direction
    }
    
}
