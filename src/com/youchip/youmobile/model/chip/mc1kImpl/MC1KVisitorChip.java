package com.youchip.youmobile.model.chip.mc1kImpl;

import static com.youchip.youmobile.model.chip.mc1kImpl.MC1KChipSpecs.FactoryFields.*;
import static com.youchip.youmobile.model.chip.mc1kImpl.MC1KVisitorChipField.*;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.youchip.youmobile.model.chip.interfaces.Chip;
import com.youchip.youmobile.model.chip.interfaces.ChipHistory;
import com.youchip.youmobile.model.chip.interfaces.VisitorChip;
import com.youchip.youmobile.utils.DataConverter;



/**
 * Contains all effective data of a MIFARE 1K RFID Chip.
 * It does not include redundant data for transaction safety.
 * Provides human readable access to the data.
 * 
 * @author MuCo
 *
 */
public class MC1KVisitorChip extends MC1KBasicChip implements VisitorChip{

    private static final long serialVersionUID = -1331137372527185384L;


    /**
     * creates a full blocked (64 block a 16 byte)
     * RFID Chip Data Object
     */
    public MC1KVisitorChip(){
        super();
    }
    
	
	/**
	 * saves a copy of the raw data array, widened to full block size (a multiple of 16 byte)
	 */
	public MC1KVisitorChip(Chip chip){
		super(chip);
	}    



	/** Extracts the Credit 1 out of the raw byte data of the chip.
	  * @return The 4 Byte long value, which represents
	  * the Credit 1 in the smallest currency unit (fa. Cent for Euro)
	  */
	public long getCredit1(){
        return DataConverter.byteArrayToInt(retrieveSingleChipData(CREDIT_1));
	}


	/** Extracts the Credit 2 out of the raw byte data of the chip.
	  * @return #number The 4 Byte long value, which represents
	  * the Credit 2 in the smallest currency unit (fa. Cent for Euro)
	  */
	public long getCredit2(){
        return DataConverter.byteArrayToInt(retrieveSingleChipData(CREDIT_2));
	}


	/** Extracts all vouchers out of the raw byte data of the chip.
	  * @return A list of Voucher
	  */ 
	public Map<Long,Long> getVoucher(){
        byte[] rawFieldData         = retrieveSingleChipData(VOUCHER);
        byte[] singleFieldData      = new byte[VOUCHER.getSize()]; 
        Map<Long,Long> elementList   = new HashMap<>();
        
        // for each of the roles do..
        for(int i=0; i < VOUCHER.getMultiplicity(); i++){
            // extract the n byte which belong to ONE role
            System.arraycopy(rawFieldData, i*VOUCHER.getSize(), singleFieldData, 0, VOUCHER.getSize());
            
            long voucherID = DataConverter.uByteToInt(singleFieldData[0]);
            long amount = DataConverter.uByteToInt(singleFieldData[1]);
            if (elementList.containsKey(singleFieldData[0]))
                amount += elementList.get(singleFieldData[0]);
            
            elementList.put(voucherID, amount);
        }
        
        return elementList;
	}


	/** Extracts the blocked state out of the raw byte data of the chip.
	  * @return #boolean true, if the the chip is blocked, so it 
	  *          cant be used for other functiones than to unblock,
	  *          otherwise false
	  */
	public boolean isBlocked(){
	    return (retrieveSingleChipData(APPTYPE)[0] & 0x01) == 0;
	}


	/** Extracts the area ID out of the raw byte data of the chip.
	  * The area ID describes the location of the visitor
	  * @return #number a 1 Byte long ID corresponding to the area
	  */
	public long getInAreaID(){
	    return DataConverter.byteArrayToInt(retrieveSingleChipData(IN_AREA_ID));
	}

    /** Extracts from chip the time  when the area was entered
     * @return corresponding to the area check in time
     */
    public Date getInAreaTime(){
        
        int hour   = (int) DataConverter.byteArrayToInt(retrieveSingleChipData(IN_AREA_TIME_HH));
        int minute = (int) DataConverter.byteArrayToInt(retrieveSingleChipData(IN_AREA_TIME_MM));
        
        Calendar date = Calendar.getInstance();
        date.set(0, Calendar.JANUARY, 0, hour, minute);
        return date.getTime();
    }

    
	/** Extracts the visitor role out of the raw byte data of the chip.
	  * Visitor role is used to connect it with several rights
	  * (fa access to the VIP area)
	  * @return A 1 Byte long ID corresponding to the role
	  */
	public Set<Long> getVisitorRoles(){
	    return retrieveMultipleChipData(VISITOR_ROLES);
	}


	/** Extracts the backoffice role out of the raw byte data of the chip.
	  * Backoffice role is used to connect it with several rights
	  * (fa to use the hendheld)
	  * @return A list of 1 Byte long IDs corresponding to the role
	  */
	public Set<Long> getBackofficeRoles(){
	    return new HashSet<>();
	}


	/** Extracts the history log out of the raw byte data of the chip.
	  * The hitsory log is a protocoll of all transaction and payment activities
	  * @return #ChipHistory\[\] a list of the last visitor acitivities
	  */
	public ChipHistory getHistoryLog(){
	    //TODO get CHIP_HISTORY_LOG
	    return null;
	}



//	/** Returns the string representation of the chip
//	  * @return The chip data as string
//	  */
//	public String toString()
//	    local data = "".. tostring(self:GetUID())        .. "," ..
//	                   tostring(self:GetEventID())       .. "," ..
//	                   tostring(self:GetCredit1())       .. "," ..
//	                   tostring(self:GetCredit2());
//	--    local data = tostring(self:GetUID())           .. "\n" ..
//	--                 tostring(self:GetEventID())       .. "\n" ..
//	--                 tostring(self:GetCredit1())       .. "\n" ..
//	--                 tostring(self:GetCredit2())       .. "\n" ..
//	--                 tostring(self:GetVoucher())       .. "\n" ..
//	--                 tostring(self:GetAreaID())        .. "\n" ..
//	--                 tostring(self:GetVisitorRole())   .. "\n" ..
//	--                 tostring(self:GetBackofficeRoles()) .. "\n" ..
//	--                 tostring(self:GetHistoryLog())    .. "\n" ..
//	--                 tostring(self:IsBlocked())        .. "\n";
//	    return data;  
//	end



	/** Modifies the raw data concerning the Credit 1.
	  * @param credit1, The 3 Byte long value, which represents
	  * the Credit 1 in the smallest currency unit (fa. Cent for Euro)
	  */
	public void setCredit1(long credit1){
	    updateSingleChipData(CREDIT_1, DataConverter.intToByteArray(credit1, CREDIT_1.getSize()));
	}


	/** Modifies the raw data concerning the Credit 2.
	  * @param credit2, The 3 Byte long value, which represents
	  * the Credit 2 in the smallest currency unit (fa. Cent for Euro)
	  */
	public void setCredit2(long credit2){
        updateSingleChipData(CREDIT_2, DataConverter.intToByteArray(credit2, CREDIT_2.getSize()));
	}


	/** Modifies the raw data concerning the vouchers.
	  * @param vouchers A list of voucher
	  */ 
	public void setVoucher(Map<Long,Long> vouchers){
	    byte[] rawFieldData         = new byte[VOUCHER.getTotalSize()];
	    int i = 0;
	    
	    Set<Long> voucherKeys = vouchers.keySet();
	    
       // if there are more voucher than can be written on chip, exit
	    if (voucherKeys.size() > VOUCHER.getMultiplicity()){
	        return;
	    }
	    
	    // transform voucher to binary format
	    for(long key:voucherKeys){
	        rawFieldData[i*2]   = DataConverter.intToByteArray(key,1)[0];
	        rawFieldData[i*2+1] = DataConverter.intToByteArray(vouchers.get(key),1)[0];
	        i++;
	    }

	    updateSingleChipData(VOUCHER, rawFieldData);
	}


	/** Modifies the raw data concerning the blocked.
      * @param blocked set to true to disable 
	  *                  other functioalities than to unblock,
	  *                 true reactivates all functioalities
	  */
	public void setBlocked(boolean blocked){
	    byte[] blockState = retrieveSingleChipData(APPTYPE);
	    
	    if (blocked){
	        blockState[0] = (blockState[0] &= 0xFE);
	    } else {
	        blockState[0] = (blockState[0] |= 0x01);
	    }
 
	    updateSingleChipData(APPTYPE, blockState);
	}

	
	/**  Modifies the raw data concerning the area ID.
	  * The area ID describes the location of the visitor
	  * @return #number areaID a 1 Byte long ID corresponding to the area
	  */
	public void setInAreaID(long areaID){
	    updateSingleChipData(IN_AREA_ID, DataConverter.intToByteArray(areaID, IN_AREA_ID.getSize()));
    }
	
    /** Modifies the raw data concerning the area check in time
     * @param time time (0-23) corresponding to the area check in time
     */
    public void setInAreaTime(Date time){
        Calendar date = Calendar.getInstance();
        date.setTime(time);
        
        long hour   = date.get(Calendar.HOUR_OF_DAY);
        long minute = date.get(Calendar.MINUTE);
        
        updateSingleChipData(IN_AREA_TIME_HH, DataConverter.intToByteArray(hour, IN_AREA_TIME_HH.getSize()));
        updateSingleChipData(IN_AREA_TIME_MM, DataConverter.intToByteArray(minute, IN_AREA_TIME_MM.getSize()));
    }



	/**  Modifies the raw data concerning the visitor role.
	  * Visitor role is used to connect it with several rights
	  * (fa access to the VIP area)
	  * @param roles, A 1 Byte long ID corresponding to the role
	  */
	public void setVisitorRoles(Set<Long> roles){
//	    updateSingleChipData(VISITOR_ROLES, DataConverter.intToByteArray(roles[0], VISITOR_ROLES.getSize()));
	}


	/** Modifies the raw data concerning the backoffice role.
	  * Backoffice role is used to connect it with several rights
	  * (fa to use the hendheld)
	  * @param roles, A list of 1 Byte IDs corresponding to the role
	  */
	public void setBackofficeRoles(Set<Long> roles){
	    //TODO set CHIP_BACKOFFICE_ROLES
	}


	/** Modifies the the raw data concerning the history log.
	  * The hitsory log is a protocoll of all transaction and payment activities.
	  * @param chipHistory, a list of the last visitor acitivities
	  */
	public void setHistoryLog(ChipHistory chipHistory){
	    //TODO set CHIP_HISTORY_LOG
	}
	
	public boolean isValid(Set<Integer> blocks){
	    return super.isValid(blocks);
	}
	

}
