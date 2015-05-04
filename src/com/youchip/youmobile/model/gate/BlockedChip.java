package com.youchip.youmobile.model.gate;

import java.io.Serializable;
import java.util.Date;

public class BlockedChip implements Serializable{

    private static final long serialVersionUID = -373175575199559629L;
    
    private Date blockedUntil;
    private String UID;
    private long eventID;
    private boolean banned;
    
    public Date getBlockedUntil() {
        return blockedUntil;
    }
    
    public String getUID() {
        return this.UID;
    }
    
    public long getEventID() {
        return eventID;
    }
    
    public void setBlockedUntil(Date blockedUntil) {
        this.blockedUntil = blockedUntil;
    }
    
    public void setUID(String UID) {
        this.UID = UID;
    }
    
    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    public void setEventID(long eventID) {
        this.eventID = eventID;
    }

    
    @Override
    public String toString(){
        return  "\n----\n" +
                "UID: \t "          + UID               +  "\n" +
                "EventID: \t "      + eventID           +  "\n" +
                "Limit:\t "         + blockedUntil      +  "\n" +
                "Banned:\t "        + banned;

    }
    
}
