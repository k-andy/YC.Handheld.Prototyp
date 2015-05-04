package com.youchip.youmobile.model.chip.interfaces;

import java.util.Set;

import com.youchip.youmobile.controller.chipIO.SimpleProgressListener;
import com.youchip.youmobile.model.chip.exceptions.RfidReadException;
import com.youchip.youmobile.model.chip.exceptions.RfidSecurityException;
import com.youchip.youmobile.model.chip.exceptions.RfidWriteException;
import com.youchip.youmobile.model.chip.mc1kImpl.MC1KChipSpecs.AccessMode;

public interface ChipIO {

    public int getDeviceAddress();

    /**
     * Setting the serial port of the device. The default is zero.
     * 
     * @param address
     *            serial port
     */
    public void setDeviceAddress(int address);

    public AccessMode getAccessMode();

    public void setAccessMode(AccessMode mode);

    public byte[] getKeyA();
    
    public String getKeyAAsString();

    public void setKeyA(byte[] key);
    
    public void setKeyA(String key);

    public byte[] getKeyB();

    public void setKeyB(byte[] key);
    
    public void openIO();
    
    public void closeIO();


    /**
     * Reads the RFID Data from a MIFARE Standard 1K RFID Chip
     *
     * @return
     * @throws RfidWriteException
     */
    public <T extends Chip> T readDataFromChip(T chipData, Set<ChipField> fieldsToRead)
            throws RfidReadException, RfidSecurityException;

    public <T extends Chip> T readDataFromChipByBlockNumber(T chipData,
            Set<Integer> blocksToRead) throws RfidReadException, RfidSecurityException;

    /**
     * Writes data to a chip, only writing changed fields.
     * 
     * @param chipData
     *            the Chip data you want to write to a real chip
     * @return Chip UID as a hexadecimal representation
     * @throws RfidWriteException
     */
    public String writeDataToChip(Chip chipData) throws RfidWriteException, RfidSecurityException;

    /**
     * Writes data to a chip, only writing the fields, given to the method
     * 
     * @param chipData
     *            the Chip data you want to write to a real chip
     * @param fieldsToUpdate
     *            fields you want to write on chip
     * @return Chip UID as a hexadecimal representation
     * @throws RfidWriteException
     */
    public String writeDataToChip(Chip chipData, Set<ChipField> fieldsToUpdate)
            throws RfidWriteException, RfidSecurityException;

    // public String writeDataToChipByBlockNumber(Chip chipData, Set<Integer>
    // blocksToUpdate) throws RfidWriteException;
    
    public void addChipReadListener(SimpleProgressListener chipReadListener);

}
