package com.youchip.youmobile.model.chip.interfaces;

import com.youchip.youmobile.model.chip.mc1kImpl.MC1KChipSpecs.AppType;

public interface BasicChip extends Chip {

    
    /** Extracts the event ID out of the raw byte data of the chip.
     * @return The 1 Byte long ID 
     */
    public long getEventID();
   
    
   /** Modifies the raw data concerning the event ID.
    * @param eventID, The 3 Byte long ID which is uniqe for 65534 events
    */
    public void setEventID(long eventID);
    
    
    public AppType getAppType();
    
    
    public void setAppType(AppType type);

    public boolean isAdmin();

    public boolean isSupervisor();

    public boolean isEmployee();

    public boolean isVisitor();
    
}
