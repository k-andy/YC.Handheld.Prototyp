package com.youchip.youmobile.model.gate;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.youchip.youmobile.model.gate.AreaConfig;
import com.youchip.youmobile.model.gate.VisitorRole;

/**
 * POJO to store the received data 
 * @author muelleco
 *
 */
public class AreaConfig implements Serializable{

    private static final long serialVersionUID = 8775390212565042365L;
    
    private String areaTitle;
    private long areaID;
    private long eventID;
    private boolean isZone;
    private boolean isCheckIn;
    private Set<VisitorRole> visitorRoles = new HashSet<>();
    
    public AreaConfig(){
        
    }
    
    public AreaConfig(AreaConfig gateConfig){
        this.areaTitle = gateConfig.areaTitle;

        this.areaID = gateConfig.areaID;
        this.eventID = gateConfig.eventID;

        this.isZone = gateConfig.isZone;
        this.isCheckIn = gateConfig.isCheckIn; 
    }
    
    public String getAreaTitle() {
        return areaTitle;
    }

    
    public long getAreaID() {
        return areaID;
    }
    
    public long getEventID() {
        return eventID;
    }

    
    public boolean isZone() {
        return isZone;
    }
    
    public boolean isCheckIn() {
        return isCheckIn;
    }
    
    public void setAreaTitle(String areaTitle) {
        this.areaTitle = areaTitle;
    }

    public void setAreaID(long areaID) {
        this.areaID = areaID;
    }

    public void setEventID(long eventID) {
        this.eventID = eventID;
    }

    public void setZone(boolean isZone) {
        this.isZone = isZone;
    }

    public void setCheckIn(boolean isCheckIn) {
        this.isCheckIn = isCheckIn;
    }

    public void addVisitorRole(VisitorRole role){
        this.visitorRoles.add(role);
    }
    
    public boolean removeVisitorRole(VisitorRole role){
        return this.visitorRoles.remove(role);
    }
    
    public Set<VisitorRole> getRoles(){
        return visitorRoles;
    }
    
    public void setRoles(Set<VisitorRole> visitorRoles){
        this.visitorRoles = visitorRoles;
    }
    
    @Override
    public String toString(){
        return  "\n----\n" +
                "EventID: \t "   + eventID           +  "\n" +
                "AreaTitle:\t "  + areaTitle         +  "\n" +   
                "Roles:  \t "    + visitorRoles      +  "\n" +
                "Area ID: \t "   + areaID            +  "\n" +
                "isZone?: \t "   + isZone            +  "\n" +
                "isCheckIn?:\t " + isCheckIn         +  "\n";
    }
    
    @Override
    public int hashCode(){
        int hash = 17;
        final int prime = 37;    
        
        hash = prime*hash + (int)(this.areaID   ^ (this.areaID >>> 32));
        hash = prime*hash + (int)(this.eventID  ^ (this.eventID >>> 32));
        hash = prime*hash + (int)(this.isCheckIn ? 0 : 1);
        hash = prime*hash + (int)(this.isZone ? 0 : 1);
        hash = prime*hash + (this.areaTitle != null ? this.areaTitle.hashCode() : 0);
        
        return hash;
    }
    
    @Override
    public boolean equals(Object object){
        if (!AreaConfig.class.isAssignableFrom(object.getClass())){
            return false;
        } else {
            AreaConfig item = (AreaConfig) object;
            return item.areaID == this.areaID && item.eventID == this.eventID &&
                    item.isCheckIn == this.isCheckIn && item.isZone == this.isZone;
        }
    }
}

