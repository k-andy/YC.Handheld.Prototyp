package com.youchip.youmobile.model.chip.interfaces;

import java.io.Serializable;


/**
 * This class contains the information about the
 * logical structure and position of RFID chip data,
 * @copyright (c)2013 YouChip
 * @author: CoMu
 * @date: 10/2013
 */
public interface ChipField extends Serializable {

    static final int txCount = 14;
    static final int crc = 15;
    
    /**
     * main block for the data, the only one if there are no 
     * redundant data for transaction safety checks
     * @return the absolute block position on a chip (0-63)
     */
    public int getBlock1Pos();

    
    /**
     * second block for the data, used only if there are 
     * redundant data for transaction safety checks
     * @return the absolute block position on a chip (0-63)
     */
    public int getBlock2Pos();

    
    /**
     * the size of the data at each of the used positions in byte
     * @return size in byte
     */
    public int getSize();

    
    /**
     * number of data, if multiple in a row. may fill at a maximum a full block
     * @return number of data (1-15).
     */
    public int getMultiplicity();

    
    /**
     * returns the total number of bytes, a field is using.
     * Respecting size and multiplicity 
     * @return
     */
    public int getTotalSize();
    
    /**
     * starting position of the data inside a block
     * @return starting position (0-15)
     */
    public int getBytePos();
    
    
    /**
     * Returns true, if transaction safety is used, other wise false
     * @return
     */
    public boolean isTxnSave();
    
    /**
     * Position of the transaction number to indicate the most current value,
     * if there are redundant data for transaction safety checks
     * @return -1 if not used, other wise the byte position in block (0-15)
     */
    public int getTxnPos();
    
    
    /**
     * position of the CRC check sum to detect data changes.
     * @return -1 if not used, other wise the byte position in block (0-15)
     */
    public int getCRC();
    
    
    /**
     * returns the nett block size (eventually excluding crc and txc field size) 
     * @return netto block size may be 14 to 16 (depending on the usage of crc and txc)
     */
    public int getNettoBlockSize();
    
    
    /**
     * returns if the block uses crc check. should be used 
     * consequent for each field which is stored in this block
     * @return true if crc is used
     */
    public boolean usesCRC();

    
}
