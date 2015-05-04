package com.youchip.youmobile.model.chip.interfaces;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public interface Chip extends Serializable{
    
    
    /** Extracts the UID out of the raw byte data of the chip.
     * @return #number The 4 Byte long UID of the Chip, which
     * is unique for one event
     */
   public String getUID();
   
   
   /**
    * returns a list of numbers of changed blocks due to setXY() methods
    * @return list of block numbers
    */
   public Set<Integer> getChangedBlocks();

   
   /**
    * Returns a deep copy of the  desired block
    * @param blockPos absolute block on chip (0-63)
    * @return a full block (16 byte)
    */
   public byte[] getRawBlock(int blockPos);
   

   /**
     * Checks if the containing data is valid.
     * @return true if all data on the chip is valid
     */ 
   public boolean isValid(Set<Integer> blocks);
   
   
   /**
    * Returns the list of numbers of effective loaded blocks.
    * @return A list may containing elements with a values from 0 up to 63.
    */
   public Set<Integer> getActiveBlocks();
   
   
   /** Sets the UID of a chip.
    * This is a scan only setting, so it is set due to chip reading.
    * You can not write it to a real chip this way.
    */
   public void setUID(String UID);

   
   /**
    * copies a full raw data block to the chip.
    * this is the most basic functionality and does not do any
    * plausibility checks.   
    * @param rawBlock
    * @param blockPos
    */
   public void setRawBlock(byte[] rawBlock, int blockPos);
   
   
   /**
    * 
    * @return a copy of the chips raw data 
    */
   public Map<Integer, byte[]> getRawData();
   
   
   /**
    * Sets the chip raw data
    * @param rawData
    */
   public void setRawData(Map<Integer, byte[]> rawData);
   
   
   /**
    * Updates transaction counter (txc) if necessary.
    * This must be done to accept changes for transaction save actions.
    */
   public void updateTransactionCounter();
   
   /**
    * Updates the crc if neccessary.
    * This must be done to accept changes, but after updating 
    * the tx counter
    */
   public void updateCRC();

   
   /**
    * This method resets the memory of the chip,
    * so it thinks, not to have changed anything
    */
   public void resetChangedBlocks();
}
