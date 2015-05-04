package com.youchip.youmobile.model.chip.mc1kImpl;

import static com.youchip.youmobile.model.chip.mc1kImpl.MC1KChipSpecs.FactoryFields.APPTYPE;
import static com.youchip.youmobile.model.chip.mc1kImpl.MC1KChipSpecs.FactoryFields.EVENT_ID;

import com.youchip.youmobile.model.chip.interfaces.BasicChip;
import com.youchip.youmobile.model.chip.interfaces.Chip;
import com.youchip.youmobile.model.chip.mc1kImpl.MC1KChipSpecs.AppType;
import com.youchip.youmobile.utils.DataConverter;

public class MC1KBasicChip extends AbstractMC1KChip implements BasicChip {

    private static final long serialVersionUID = -6757569200002946998L;

    public MC1KBasicChip(){
        super();
    } 
    
    public MC1KBasicChip(Chip chip){
        super(chip);
    } 


    /** Modifies the raw data concerning the event ID.
      * @param eventID, The 3 Byte long ID which is uniqe for 65534 events
      */
    public void setEventID(long eventID){
        byte[] fieldData = DataConverter.intToByteArray(eventID, EVENT_ID.getSize());
        updateSingleChipData(EVENT_ID, fieldData);
    }
    
    /** Extracts the event ID out of the raw byte data of the chip.
     * @return The 3 Byte long ID which is unique for 65534 events
     */
   public long getEventID(){
       return DataConverter.byteArrayToInt(retrieveSingleChipData(EVENT_ID));
   }

    @Override
    public AppType getAppType() {
        return AppType.fromInteger( DataConverter.byteArrayToInt(retrieveSingleChipData(APPTYPE)) );
    }

    @Override
    public void setAppType(AppType type) {
        updateSingleChipData(APPTYPE, DataConverter.intToByteArray(type.getValue()));
    }

    @Override
    public boolean isAdmin(){
        return (retrieveSingleChipData(APPTYPE)[0] & 0x08) > 0;
    }

    @Override
    public boolean isSupervisor(){
        return (retrieveSingleChipData(APPTYPE)[0] & 0x0C) > 0;
    }

    @Override
    public boolean isEmployee(){
        return (retrieveSingleChipData(APPTYPE)[0] & 0x0E) > 0;
    }

    @Override
    public boolean isVisitor(){
        return (retrieveSingleChipData(APPTYPE)[0] & 0x01) == 1;
    }

}
