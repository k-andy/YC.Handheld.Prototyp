package com.youchip.youmobile.model.chip.mc1kImpl;

import static com.youchip.youmobile.model.chip.mc1kImpl.MC1KChipSpecs.Structure.BYTES_PER_BLOCK;

import com.youchip.youmobile.model.chip.interfaces.ChipField;

/**
 * This class holds constants to address "MIFARE Classic 1K" RFID chip data,
 * 
 * @copyright (c)2013 YouChip
 * @author: CoMu
 * @date: 09/2013
 */
public enum MC1KVisitorChipField implements ChipField {
                     
    IN_AREA_ID      (1, 0, 12, 1,  1, false, true),
    IN_AREA_TIME_HH (1, 0, 13, 1,  1, false, true),
    IN_AREA_TIME_MM (1, 0, 14, 1,  1, false, true),
    
    VISITOR_ROLES   (2, 0,  0, 1, 13, false, true),
    
    CREDIT_1        (4, 5,  0, 4,  1, true,  true),
    CREDIT_2        (4, 5,  4, 4,  1, true,  true),
    VOUCHER         (6, 0,  0, 2,  7, false, true);

    private int block1;
    private int block2;
    private int size;
    private int bytePos;
    private int multiplicity;
    private boolean useTxc;
    private boolean useCrc;

    /**
     * all the fields a chip may have and their position on chip
     *
     * @param blockPos1
     *            first occurrence of the data
     * @param blockPos2
     *            redundant occurrence of the data if transaction safety is used
     * @param bytePos
     *            absolute byte position [0 - 15]
     * @param size
     *            size of the date
     */
    private MC1KVisitorChipField(int blockPos1, int blockPos2,
            int bytePos, int size, int multiplicity, boolean useTxc, boolean useCrc) {
        
//        int blockOffset = (bytePos+(size*multiplicity)-1) / getNettoBlockSize(useCrc, useTxc);

//            assert !(1 > size) :
//                "Field size of field '" + this.toString() + "' must be greater than '0'";
//            assert !(1 > multiplicity) :
//                "Multiplicity of field '" + this.toString() + "' must be greater than '0'";
//            assert !((blockPos1 <= 0) || (blockPos1 % 4 == 3)) :
//                "Disallowed meta data block usage (Sector:"
//                        + (blockPos1 / 4) + ", Block:" + (blockPos1 % 4) + ") by field '" + this.toString() + "'";
//            assert !((useTxc && blockPos2 <= 0) || (blockPos2 % 4 == 3)) :
//                "Disallowed meta data block usage (Sector:"
//                        + (blockPos2 / 4) + ", Block:" + (blockPos2 % 4) + ") by field '" + this.toString() + "'";
//            assert !((!useTxc && blockPos2 > 0)) :
//                "Inconsistent declaration of transaction safety by field '" +
//                        this.toString() + "'! If using transaction safety, the backup block must be set to" +
//                        "a value between [1 .. 63].";
//            assert !(bytePos > getNettoBlockSize(useCrc, useTxc) - 1) :
//                "Invalid start byte position of field'" +
//                        this.toString() + "'! Start position must be in range of [0 .. " + (BYTES_PER_BLOCK.getValue() - 1) + "]";
//            assert !((blockPos1 + blockOffset) % 4 == 3) :
//                "Disallowed meta data block '" + (blockPos1 + blockOffset) + "' usage by field '" + this.toString() +
//                        "'! Check field size and multiplicity.";
//            assert !((blockPos2 > 0) && ((blockPos2 + blockOffset) % 4 == 3)) :
//                "Disallowed meta data block '" + (blockPos2 + blockOffset) + "' usage by field '" + this.toString() +
//                        "'! Check field size and multiplicity";


            this.block1 = blockPos1;
            this.block2 = blockPos2;
            this.bytePos = bytePos;
            this.size = size;
            this.multiplicity = multiplicity;
            this.useCrc   = useCrc;
            this.useTxc = useTxc;

    }

    @Override
    public int getBlock1Pos() {
        return block1;
    }

    @Override
    public int getBlock2Pos() {
        return block2;
    }

    @Override
    public int getBytePos() {
        return bytePos;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public int getMultiplicity() {
        return multiplicity;
    }

    @Override
    public int getTotalSize() {
        return getSize() * getMultiplicity();
    }

    @Override
    public boolean isTxnSave(){
        return useTxc;
    }
    
    @Override
    public int getTxnPos() {
        if (useTxc)
            return txCount;
        else
            return -1;
    }
    
    @Override
    public int getCRC(){
        if (useCrc)
            return crc;
        else
            return -1;
    }
    
    @Override
    public boolean usesCRC(){
        return useCrc;
    }
    
    @Override
    public int getNettoBlockSize(){
        return getNettoBlockSize(useCrc, useTxc);
    }
    
    private static int getNettoBlockSize(boolean crc, boolean txc){
        if (txc) {
            return BYTES_PER_BLOCK.getValue() - 2;
        } else if (crc){
            return BYTES_PER_BLOCK.getValue() - 1;
        } else {
            return BYTES_PER_BLOCK.getValue();
        }    
    }
    
    
}
