package com.youchip.youmobile.model.chip.mc1kImpl;

import android.annotation.SuppressLint;
import android.util.Log;

import com.youchip.youmobile.model.chip.interfaces.Chip;
import com.youchip.youmobile.model.chip.interfaces.ChipField;
import com.youchip.youmobile.utils.DataConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static com.youchip.youmobile.model.chip.mc1kImpl.MC1KChipSpecs.FactoryFields.UID;
import static com.youchip.youmobile.model.chip.mc1kImpl.MC1KChipSpecs.Structure.BYTES_PER_BLOCK;

@SuppressLint("UseSparseArrays")
public abstract class AbstractMC1KChip implements Chip {

    private static final long serialVersionUID = 8699359192064631985L;
    private static final int TX_LOG_MAX = 4;

    private Map<Integer, byte[]> rawData;
    private final Set<ChipField> txToUpdate = new HashSet<>();
    private final Set<Integer> changedBlocks = new HashSet<>();
    private final Set<Integer> crcToUpdate = new HashSet<>();

    /**
     * creates a full blocked (64 block a 16 byte) RFID Chip Data Object
     */
    public AbstractMC1KChip() {
        this.rawData = new HashMap<>();
    }


    /**
     * saves a copy of the raw data array, widened to full block size (a
     * multiple of 16 byte)
     */
    public AbstractMC1KChip(Chip chip) {
        setRawData(chip.getRawData());
    }

    /**
     * returns all block number which contain loaded data
     */
    public SortedSet<Integer> getActiveBlocks() {
        SortedSet<Integer> blocks = new TreeSet<>();
        blocks.addAll(rawData.keySet());
        return blocks;
    }

    /**
     * This method resets the memory of the chip, so it thinks, not to have
     * changed anything
     */
    public void resetChangedBlocks() {
        this.changedBlocks.removeAll(this.changedBlocks);
    }

    public void setUID(String uid) {
        byte[] fieldData = DataConverter.hexStringToByteArray(uid);
        updateSingleChipData(UID, fieldData);
    }

    /**
     * Extracts the UID out of the raw byte data of the chip.
     *
     * @return #number The 4 Byte long UID of the Chip, which is unique for one
     * event
     */
    public String getUID() {
        return DataConverter.byteArrayToHexString(getUIDBytes(),
                UID.getSize());
    }

    protected byte[] getUIDBytes() {
        return retrieveSingleChipData(UID);
    }


    // /**
    // * returns a byte array with the desired data. regarding transaction
    // safety
    // * and crc check
    // *
    // * @param field
    // * the ChipField you want to scan
    // * @return the data as byte array
    // */
    // protected byte[] retrieveSingleChipData(ChipField field) {
    // int blockPos = field.getBlock1Pos();
    // int dataSize = field.getSize() * field.getCount();
    // byte[] fieldData = new byte[dataSize];
    //
    // if (isFieldTxSave(field)) { // if redundant data is used get the most
    // // recent
    // blockPos = getTxActiveBlock(field);
    // }
    //
    // System.arraycopy(this.rawData[blockPos], field.getBytePos(), fieldData,
    // 0, dataSize);
    // return fieldData;
    // }

    /**
     * @param field
     * @return
     */
    protected byte[] retrieveSingleChipData(ChipField field) {

        int blockPos = getTxActiveBlock(field);
        int fieldSize = field.getSize() * field.getMultiplicity();
        int byteStartPos = field.getBytePos();
        int nettoBlockSize = field.getNettoBlockSize();
        int i = 0;
        byte[] fieldData = new byte[fieldSize];

        // split up if data uses several blocks
        do {
            // correcting length to fit to block
            int partialSize = fieldSize;
            int byteEndPos = byteStartPos + fieldSize;
            if (byteEndPos > nettoBlockSize) {
                partialSize -= byteEndPos - nettoBlockSize;
            }

            // copy the needed data
            System.arraycopy(getBlock(blockPos), byteStartPos, fieldData, i, partialSize);

            // set up for next round
            blockPos++;
            i += partialSize;
            fieldSize -= partialSize;
            byteStartPos = 0;

        } while (fieldSize > 0);

        return fieldData;
    }

    /**
     * returns a byte array with the desired data. regarding transaction safety
     * and crc check
     *
     * @param field the ChipField you want to scan
     * @return the data as byte array
     */
    protected Set<Long> retrieveMultipleChipData(ChipField field) {
        byte[] rawFieldData = retrieveSingleChipData(field);
        byte[] singleFieldData = new byte[field.getSize()];
        Set<Long> elementList = new HashSet<>();

        // for each of the roles do..
        for (int i = 0; i < field.getMultiplicity(); i++) {
            // extract the n byte which belong to ONE role
            System.arraycopy(rawFieldData, i * field.getSize(), singleFieldData, 0, field.getSize());

            // convert all bytes of one role to an int and save them in an int
            // array
            elementList.add(DataConverter.byteArrayToInt(singleFieldData));
        }
        return elementList;
    }

    /**
     * copies the raw data to the chip. this is the most basic functionality and
     * does not do any plausibility checks.
     */
    public void setRawData(Map<Integer, byte[]> rawData) {
        this.rawData = rawData;
    }

    /**
     * gives a copy of the chips raw data. this is the most basic functionality
     * and does not do any plausibility checks.
     */
    public Map<Integer, byte[]> getRawData() {
        Map<Integer, byte[]> rawDataCopy = new HashMap<>(this.rawData.size());
        for (Integer key : rawData.keySet()) {
            rawDataCopy.put(key, getRawBlock(key));
        }
        return rawDataCopy;
    }

    /**
     * copies a full raw data block to the chip. this is the most basic
     * functionality and does not do any plausibility checks. Can copy parts of
     * blocks too
     *
     * @param rawBlock Byte Array with length from 1-16
     * @param blockPos absolute block position (0-63)
     */
    public void setRawBlock(byte[] rawBlock, int blockPos) {
        int optLength = Math.min(rawBlock.length, BYTES_PER_BLOCK.getValue());
        byte[] block = new byte[BYTES_PER_BLOCK.getValue()];

        System.arraycopy(rawBlock, 0, block, 0, optLength);
        rawData.put(blockPos, block);
        Log.d("AbstractMC1KChip",
                "Set raw data " + DataConverter.byteArrayToHexString(block)
                        + " to virtual block " + blockPos + ".");
        changedBlocks.add(blockPos);
    }

    /**
     * Returns a deep copy of the desired block
     *
     * @param blockPos absolute block on chip (0-63)
     * @return a full block (16 byte)
     */
    @Override
    public byte[] getRawBlock(int blockPos) {
        byte[] blockData = new byte[BYTES_PER_BLOCK.getValue()];
        System.arraycopy(getBlock(blockPos), 0, blockData, 0, BYTES_PER_BLOCK.getValue());
        return blockData;
    }


    /**
     * update complete chip field
     *
     * @param field     FildType (containes metadata)
     * @param fieldData (field data)
     */
    protected void updateSingleChipData(ChipField field, byte[] fieldData) {

        fieldData = validateFieldData(field, fieldData);

        int backupBlock = getTxBackUpBlock(field);
        int activeBlock = getTxActiveBlock(field);
        int fieldSize = field.getTotalSize();
        final boolean txSave = isFieldTxSave(field);

        final int maxBytePos = field.getNettoBlockSize();
        int j = field.getBytePos();
        int i = 0;

        // split up if data uses several blocks
        do {
            // correcting length to fit to block
            int partialSize = fieldSize;
            int byteEndPos = j + fieldSize;
            if (byteEndPos > maxBytePos) {
                partialSize -= byteEndPos - maxBytePos;
            }

            // check if data really has changed
            if (hasFieldDataChanged(fieldData, i, activeBlock, j, partialSize)) {
                // copy the desired data
                if (txSave) {
                    // copy non changed data to the new block, to not lose them.
                    // dont copy txc or crc
                    System.arraycopy(getBlock(activeBlock), 0, getBlock(backupBlock), 0,
                            field.getNettoBlockSize());
                }

                // change data
                System.arraycopy(fieldData, i, getBlock(backupBlock), j, partialSize);

                if (txSave) {
                    // additionally saving the same to the current block, not to
                    // lose them
                    System.arraycopy(fieldData, i, getBlock(activeBlock), j, partialSize);
                    txToUpdate.add(field);
                }

                // if crc should be calculated
                if (field.usesCRC()) {
                    crcToUpdate.add(backupBlock);
                }

                // only save the new block
                changedBlocks.add(backupBlock);
            }

            // set up for next round
            backupBlock++; // block ends, start with the next
            activeBlock++; // block ends, start with the next
            i += partialSize; // increase next start position in field data
            fieldSize -= partialSize; // total field size decrease, for it is
            // done
            j = 0; // increase next start position in raw data

        } while (fieldSize > 0);
    }

    /**
     * validating and correcting data length if possible.
     *
     * @param field
     * @param fieldData
     * @return field data at length of the field specification
     * @throws IllegalArgumentException if fieldData is bigger than specified
     */
    protected byte[] validateFieldData(ChipField field, byte[] fieldData)
            throws IllegalArgumentException {
        int fieldSize = field.getSize() * field.getMultiplicity();
        if (fieldSize < fieldData.length) {
            // if size does not match the field specification return
            throw new IllegalArgumentException("Data length (" + fieldData.length
                    + ") does not match the specification (" + fieldSize + ")");
        } else if (fieldSize > fieldData.length) {
            // stretch if too small (to overwrite old data correctly)
            byte[] normalized = new byte[fieldSize];
            System.arraycopy(fieldData, 0, normalized, 0, fieldData.length);
            return normalized;
        } else
            return fieldData;
    }

    protected boolean hasFieldDataChanged(byte[] newFieldData, int startPos, int blockPos,
                                          int byteStartPos, int size) {
        byte[] oldBlock = getBlock(blockPos);
        for (int i = 0; i < size; i++) {
            if (oldBlock[byteStartPos + i] != newFieldData[startPos + i]) {
                return true;
            }
        }
        return false;
    }

    /**
     * returns a list of block numbers which got changed
     */
    public Set<Integer> getChangedBlocks() {
        return this.changedBlocks;
    }

    /**
     * proves whether a field uses transaction safety or not
     *
     * @return true if the field uses transaction safety
     */
    protected boolean isFieldTxSave(ChipField field) {
        return (field.getBlock2Pos() > 0) && (field.getTxnPos() >= 0);
    }

    /**
     * returns the block which is the backup block (the next you should write
     * to)
     *
     * @param field which blocks update block you want to determine
     * @return the number of the block position
     */
    protected int getTxBackUpBlock(ChipField field) {
        if (!isFieldTxSave(field)) {
            return field.getBlock1Pos();
        } else {
            int blockPos1 = field.getBlock1Pos();
            int blockPos2 = field.getBlock2Pos();

            if (getTxActiveBlock(field) == blockPos1) {
                return blockPos2;
            } else {
                return blockPos1;
            }
        }
    }

    protected int getTxActiveBlock(ChipField field) {
        if (!isFieldTxSave(field)) {
            return field.getBlock1Pos();
        } else {
            int blockPos1 = field.getBlock1Pos();
            int blockPos2 = field.getBlock2Pos();

            int txc1 = DataConverter.uByteToInt(getBlock(blockPos1)[field.getTxnPos()]);
            int txc2 = DataConverter.uByteToInt(getBlock(blockPos2)[field.getTxnPos()]);

            if (((txc1 == txc2) && txc1 == 0)
                    || ((txc1 > txc2) && !((txc2 == 0) && (txc1 >= TX_LOG_MAX)))
                    || ((txc1 == 0) && (txc2 >= TX_LOG_MAX))) {
                return blockPos1;
            } else {
                return blockPos2;
            }
        }
    }

    /**
     * Updates transaction counter if necessary. This must be done to accept
     * changes for transaction save actions.
     */
    public void updateTransactionCounter() {
        for (ChipField field : this.txToUpdate) {
            if (field.isTxnSave()) {
                int block = getTxBackUpBlock(field);
                int txcActive = DataConverter
                        .uByteToInt(getBlock(getTxActiveBlock(field))[field.getTxnPos()]);

                if (txcActive >= TX_LOG_MAX) {
                    getBlock(block)[field.getTxnPos()] = (byte) 0;
                } else {
                    getBlock(block)[field.getTxnPos()] = (byte) (txcActive + 1);
                }

            }
        }
    }


    /**
     * splitting up multifield-fields
     */
    protected void setMultiField(byte[] fieldData, ChipField... fields) {

        int fullLength = 0;
        for (ChipField field : fields) {
            fullLength += field.getTotalSize();
        }
        byte[] fullData = Arrays.copyOf(fieldData, fullLength);

        int startPos = 0;
        for (ChipField field : fields) {
            int maxSize = startPos + field.getTotalSize() - 1;
            int minSize = fullData.length - 1;
            if (startPos > minSize) {
                break;
            }
            byte[] tmp = Arrays.copyOfRange(fullData, startPos, Math.min(maxSize, minSize));
            updateSingleChipData(field, tmp);
            startPos += field.getTotalSize();
        }
    }


    /**
     * returns a block of mifare classic rfid chip data
     *
     * @param blockPos position of the desired block ( 0 .. 16*4-1 )
     * @return Array of 16 byte
     */
    protected byte[] getBlock(int blockPos) {
        // if yet not existing.. create it.
        if (!this.rawData.containsKey(blockPos)) {
            this.rawData.put(blockPos, new byte[BYTES_PER_BLOCK.getValue()]);
        }
        return this.rawData.get(blockPos);
    }


    public void updateCRC() {
        byte uidCRC = calcByteCRC(getUIDBytes());

        for (int block : this.crcToUpdate) {
            byte[] data = getBlock(block);
            data[ChipField.crc] = calcByteCRC(uidCRC, data, ChipField.crc);
        }

        crcToUpdate.clear();
    }


    /**
     * Checks if the containing data is valid.
     *
     * @return true if all data on the chip is valid
     */
    public boolean isValid(ChipField field) {
        ArrayList<ChipField> list = new ArrayList<>();
        list.add(field);

        return isValid(DataConverter.getRelevantBlocks(list));
    }


    public boolean isValid(Set<Integer> blocks) {
        byte uidCRC = calcByteCRC(getUIDBytes());

        for (Integer block : blocks) {
            if (rawData.containsKey(block)) {
                byte[] data = rawData.get(block);
                byte calcCRC = calcByteCRC(uidCRC, data, ChipField.crc);
                byte blckCRC = data[ChipField.crc];

//                Log.e("TEST", "------------------");
//                    Log.e("TEST", "block = " + block);
//                for (byte b : data) {
//                    Log.e("TEST", "data = " + b);
//
//                }
//
//                Log.e("TEST", "------------------");
                Log.e("TEST", "block = " + block);
                Log.e("TEST", "blckCRC = " + blckCRC);
                Log.e("TEST", "calcCRC = " + calcCRC);

                Log.e("TEST", "uidCRC = " + uidCRC);

                if (blckCRC != calcCRC) {
                    String val1 = String.format("%02X", DataConverter.uByteToInt(blckCRC));
                    String val2 = String.format("%02X", DataConverter.uByteToInt(calcCRC));
                    Log.w(AbstractMC1KChip.class.getName(), "CRC Result for block " + block + ": on-Chip:" + val1 + ", calculated:" + val2);
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    protected byte calcByteCRC(byte start, byte[] data, int size) {
        if (size <= 0)
            return start;
        else {
            for (int i = 0; i < size; i++) {
                start = (byte) (start ^ data[i]);
            }
            return start;
        }
    }

    protected byte calcByteCRC(byte[] data) {
        int length = data.length;
        if (length <= 0)
            return (byte) 0;
        else {
            byte start = data[0];
            for (int i = 1; i < length; i++) {
                start = (byte) (start ^ data[i]);
            }
            return start;
        }
    }
}
