package com.youchip.youmobile.controller.chipIO;

import android.util.Log;

import com.android.RfidControll;
import com.youchip.youmobile.model.chip.exceptions.RfidReadException;
import com.youchip.youmobile.model.chip.exceptions.RfidSecurityException;
import com.youchip.youmobile.model.chip.exceptions.RfidWriteException;
import com.youchip.youmobile.model.chip.interfaces.Chip;
import com.youchip.youmobile.model.chip.interfaces.ChipField;
import com.youchip.youmobile.model.chip.interfaces.ChipIO;
import com.youchip.youmobile.model.chip.mc1kImpl.MC1KChipSpecs;
import com.youchip.youmobile.utils.DataConverter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static com.youchip.youmobile.model.chip.mc1kImpl.MC1KChipSpecs.AccessMode;
import static com.youchip.youmobile.model.chip.mc1kImpl.MC1KChipSpecs.AccessMode.ALL_KEYA;
import static com.youchip.youmobile.model.chip.mc1kImpl.MC1KChipSpecs.FactoryFields.UID;
import static com.youchip.youmobile.model.chip.mc1kImpl.MC1KChipSpecs.Security.DEFAULT_KEY_A;
import static com.youchip.youmobile.model.chip.mc1kImpl.MC1KChipSpecs.Structure.BYTES_PER_BLOCK;

//import com.iData.RfidControll;

public class MC1KChipIO implements ChipIO {

    private static final int BLOCKS_PER_READ = 1;
    private int deviceAddress = 0;
    private AccessMode accessMode = ALL_KEYA;
    private byte[] keyA = MC1KChipSpecs.Security.DEFAULT_KEY_A.getValue();
    private final RfidControll rfidControll;

    private Set<SimpleProgressListener> chipReadListener = new HashSet<>();
    private Set<SimpleProgressListener> chipWriteListener = new HashSet<>();

    private static final String CURRENT_CLASS = MC1KChipIO.class.getName();


    public MC1KChipIO() {
        rfidControll = new RfidControll();
    }

    public int getDeviceAddress() {
        return this.deviceAddress;
    }

    public void setDeviceAddress(int address) {
        if (address < 0 || address > 254) {
            throw new IllegalArgumentException(
                    "Device address must be between 0 and 254!");
        }

        this.deviceAddress = address;
    }

    public AccessMode getAccessMode() {
        return this.accessMode;
    }

    public void setAccessMode(AccessMode mode) {
        if (mode == null) {
            throw new IllegalArgumentException("AccessMode must not be 'null'");
        }
        this.accessMode = mode;
    }


    /**
     * Reads the RFID Data from a MIFARE Standard 1K RFID Chip
     * Reads all necessary blocks from the chip, to get the desired
     * field data.
     *
     * @param chipData     The chip object you want to update
     * @param fieldsToRead The chip fields you want to scan.
     * @return a chip object containing information about all red fields
     * @throws RfidSecurityException
     * @throws RfidWriteException    if the reading process was interrupted
     */
    public <T extends Chip> T readDataFromChip(T chipData, Set<ChipField> fieldsToRead)
            throws RfidReadException, RfidSecurityException {
        return readDataFromChipByBlockNumber(chipData,
                DataConverter.getRelevantBlocks(fieldsToRead));
    }

    /**
     * Reads the RFID Data from a MIFARE Standard 1K RFID Chip
     *
     * @param chipData     The chip object you want to update
     * @param blocksToRead the blocks you want to scan.
     * @return a chip object containing information about all red fields
     * @throws RfidSecurityException
     * @throws RfidWriteException
     */
    public <T extends Chip> T readDataFromChipByBlockNumber(T chipData,
                                                            Set<Integer> blocksToRead) throws RfidReadException, RfidSecurityException {

        byte[] rawBlock = new byte[BYTES_PER_BLOCK.getValue() * 2];
        byte[] rawBlockFinal;
        byte[] serialNumberAndKeyA;
        byte[] serialNumberFinal = new byte[6];

        // needed for progress recognition
        int total = blocksToRead.size() - 1;
        int step = 0;
        if (blocksToRead.contains(0)) total--;

        for (Integer block : blocksToRead) {
            // if block is the factory info block, or the metadata block, ignore it
            if ((block == 0) || (block % 4 == 3)) {
                continue;
            }

            serialNumberAndKeyA = this.getKeyA();

            int result = rfidControll.API_MF_Read(this.getDeviceAddress(),
                    this.getAccessMode().getValue(), block, 2,
                    serialNumberAndKeyA, rawBlock);

            serialNumberFinal = Arrays.copyOfRange(rawBlock, 0, 4);
            rawBlockFinal = Arrays.copyOfRange(rawBlock, 4, 16 + 4);

            if (checkIOOperation(result, serialNumberFinal)) {
                chipData.setRawBlock(rawBlockFinal, block);
                informReadListener(total, step++);
            } else {
                informReadListener(total, -1);
                throw new RfidReadException();
            }
        }

        chipData.setRawBlock(serialNumberFinal, UID.getBlock1Pos());
        chipData.resetChangedBlocks();
        return chipData;
    }

    private byte[] getChipUID() {
        byte[] serialNumberAndKeyA = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF};
        byte[] dataToRead = new byte[BYTES_PER_BLOCK.getValue()];

        rfidControll.API_MF_Read(this.getDeviceAddress(),
                this.getAccessMode().getValue(), 1, 1, serialNumberAndKeyA, dataToRead);

        return Arrays.copyOf(dataToRead, this.getKeyA().length);
    }

    private boolean checkIOOperation(int result, byte[] serialNumber) {
        if (result > 0) {
            // result has error id
            Log.d(CURRENT_CLASS, "Chip IO operation failed. Error ID: " + result);
            return false;
        } else if (Arrays.equals(serialNumber, this.getKeyA())) {
            // serial number is key A (did not scan/write anything)
            Log.d(CURRENT_CLASS, "Chip IO operation failed: No chip to scan");
            return false;
        } else if ((serialNumber[0] == 0) && (serialNumber[1] == 0) && (serialNumber[2] == 0) && (serialNumber[3] == 0)) {
            // serial number is 0
            Log.d(CURRENT_CLASS, "Chip IO operation failed: Serial Number is 0000 0000");
            return false;
        } else if (checkIfItsNotDefaultChip()) {
            Log.d(CURRENT_CLASS, "Chip IO operation failed: default virtual chip");
            return false;
        } else {
            Log.w(CURRENT_CLASS, "Chip IO succeded: " + DataConverter.byteArrayToHexString(serialNumber, 4));
            return true;
        }
    }

    private boolean checkIfItsNotDefaultChip() {
        if (DataConverter.byteArrayToHexString(getChipUID()).contains("8380BB00"))
            return true;
        return false;
    }

    /**
     * Writes data to a chip, only writing changed fields.
     *
     * @param chipData the Chip data you want to write to a real chip
     * @return Chip UID as a hexadecimal representation
     * @throws RfidWriteException
     * @throws RfidSecurityException
     */
    @Override
    public String writeDataToChip(Chip chipData) throws RfidWriteException, RfidSecurityException {
        chipData.getChangedBlocks().remove(0);//TODO remove this statement ..
        return writeDataToChipByBlockNumber(chipData,
                chipData.getChangedBlocks());
    }

    /**
     * Writes data to a chip, only writing the fields, given to the method
     *
     * @param chipData       the Chip data you want to write to a real chip
     * @param fieldsToUpdate fields you want to write on chip
     * @return Chip UID as a hexadecimal representation
     * @throws RfidWriteException
     * @throws RfidSecurityException
     */
    @Override
    public String writeDataToChip(Chip chipData, Set<ChipField> fieldsToUpdate)
            throws RfidWriteException, RfidSecurityException {
        chipData.resetChangedBlocks();
        return writeDataToChipByBlockNumber(chipData,
                DataConverter.getRelevantBlocks(fieldsToUpdate));
    }

    /**
     * Writes data to a chip, only writing the fields, given to the method
     *
     * @param chipData       the Chip data you want to write to a real chip
     * @param blocksToUpdate blocks you want to write on the chip
     * @return Chip UID as a hexadecimal representation
     * @throws RfidWriteException
     * @throws RfidSecurityException
     */
    public String writeDataToChipByBlockNumber(Chip chipData,
                                               Set<Integer> blocksToUpdate) throws RfidWriteException, RfidSecurityException {

        setupChipForWriting(chipData, blocksToUpdate);

        String uid = checkChipUID(chipData);
        if (uid != null) {
            writeBlocksToChip(chipData, blocksToUpdate);
        } else {
            throw new RfidWriteException("UID on real world chip does not match!");
        }

        return uid;
    }

    /**
     * Prepare for writing. update transation counter. update crc and check uid from virtual chip with real world chip
     *
     * @param chipData
     * @param blocksToUpdate
     * @throws RfidWriteException
     * @throws RfidSecurityException
     */
    private void setupChipForWriting(Chip chipData, Set<Integer> blocksToUpdate) {
        // updating transaction counter of changed fields
        chipData.updateTransactionCounter();
        // update crc
        chipData.updateCRC();
        // removing block 0, for you cant write to it
        blocksToUpdate.remove(0);
    }


    private String checkChipUID(Chip chipData) throws RfidWriteException, RfidSecurityException {
        // check uid-validity
        String realChipUID = readChipUID();
        String virtChipUID = chipData.getUID();

        Log.d(CURRENT_CLASS, "Real Chip UID = " + realChipUID);
        Log.d(CURRENT_CLASS, "Virt Chip UID = " + virtChipUID);

        if (virtChipUID.equals(realChipUID) && !virtChipUID.equals("00000000") && !virtChipUID.equals("")) {
            return realChipUID;
        } else {
            return null;
        }
    }

    /**
     * Writes data to chip and returns the UID
     *
     * @param chipData       data which will be written when mentioned in blocksToUpdate
     * @param blocksToUpdate blocks which should be updated
     * @return UID as String
     * @throws RfidWriteException
     * @throws RfidSecurityException
     */
    private String writeBlocksToChip(Chip chipData, Set<Integer> blocksToUpdate) throws RfidWriteException, RfidSecurityException {

        // needed for progress recognition
        int total = blocksToUpdate.contains(0) ? blocksToUpdate.size() - 2 : blocksToUpdate.size() - 1;
        int step = 0;

        // needed for buffering data
        byte[] dataToWrite;
        byte[] serialNumberAndKeyA = this.getKeyA();

        // writing blocks to chip
        Iterator<Integer> it = blocksToUpdate.iterator();
                Log.e("TEST", "---- BEGINNING ----");
        while (it.hasNext()) {
            int blockPos = it.next();
            if ((blockPos == 0) || (blockPos % 4 == 3)) {
                continue;
            }

            serialNumberAndKeyA = this.getKeyA();

            dataToWrite = chipData.getRawBlock(blockPos);

            for (byte b : dataToWrite) {
                Log.e("TEST", "dataToWrite = " + b);
            }

            int result = rfidControll
                    .API_MF_Write(this.getDeviceAddress(), this.getAccessMode()
                                    .getValue(), blockPos, BLOCKS_PER_READ,
                            serialNumberAndKeyA, dataToWrite);

            for (byte b : serialNumberAndKeyA) {
                Log.e("TEST", "serialNumberAndKeyA = " + b);
            }

            if ((result > 0)) {
                informWriteListener(total, -1);
                throw new RfidWriteException();
            } else {
                informWriteListener(total, step++);
            }
            it.remove();
        }

        String s = DataConverter.byteArrayToHexString(serialNumberAndKeyA, 4);

        for (byte b : serialNumberAndKeyA) {
            Log.e("TEST", "serialNumberAndKeyA last = " + b);
        }
            Log.e("TEST", "serialNumberAndKeyA as string = " + s);
        Log.e("TEST", "---- AFTER ----");

        return s;
    }

    /**
     * Reading uid from the real world chip
     *
     * @return uid as string
     */
    private String readChipUID() {
        byte[] serialNumberAndKeyA = this.getKeyA();
        byte[] dataToRead = new byte[BYTES_PER_BLOCK.getValue()];

        int result = rfidControll.API_MF_Read(this.getDeviceAddress(),
                this.getAccessMode().getValue(), 1, 1,
                serialNumberAndKeyA, dataToRead);

        return DataConverter.byteArrayToHexString(serialNumberAndKeyA, UID.getSize());
    }

    /**
     * returns a copy of the current used key a
     *
     * @return
     */
    public byte[] getKeyA() {
        byte[] copy = new byte[this.keyA.length];
        System.arraycopy(this.keyA, 0, copy, 0, this.keyA.length);
        return copy;
    }

    @Override
    public String getKeyAAsString() {
        return DataConverter.byteArrayToHexString(getKeyA());
    }

    @Override
    public void setKeyA(byte[] key) {
        if (key.length > DEFAULT_KEY_A.getSize()) {
            // error if too long
            throw new IllegalArgumentException("Key A must not be longer then " + DEFAULT_KEY_A.getSize() + " byte, but is " + key.length + " (" + DataConverter.byteArrayToHexString(key) + ")!");
        } else if (key.length < DEFAULT_KEY_A.getSize()) {
            // stretch if too small
            byte[] normalized = DEFAULT_KEY_A.getValue();
            System.arraycopy(key, 0, normalized, 0, key.length);
            key = normalized;
        }
        System.arraycopy(key, 0, this.keyA, 0, key.length);
    }

    @Override
    public void setKeyA(String key) {
        setKeyA(DataConverter.hexStringToByteArray(key));
    }

    @Override
    public byte[] getKeyB() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setKeyB(byte[] key) {
        // TODO Auto-generated method stub
    }

    @Override
    public void openIO() {
        rfidControll.API_OpenComm();
    }

    @Override
    public void closeIO() {
        rfidControll.API_CloseComm();
    }

    public boolean removeChipReadListener(SimpleProgressListener chipReadListener) {
        return this.chipReadListener.remove(chipReadListener);
    }

    public void addChipReadListener(SimpleProgressListener chipReadListener) {
        this.chipReadListener.clear();
        this.chipReadListener.add(chipReadListener);
    }

    public boolean removeChipWriteListener(SimpleProgressListener chipWriteListener) {
        return this.chipReadListener.remove(chipWriteListener);
    }

    public void addChipWriteListener(SimpleProgressListener chipWriteListener) {
        this.chipWriteListener.clear();
        this.chipReadListener.add(chipWriteListener);
    }

    private void informReadListener(int total, int current) {
        for (SimpleProgressListener listener : this.chipReadListener) {
            listener.listen(total, current);
        }
    }

    private void informWriteListener(int total, int current) {
        for (SimpleProgressListener listener : this.chipWriteListener) {
            listener.listen(total, current);
        }
    }

}
