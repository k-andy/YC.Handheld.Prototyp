package com.youchip.youmobile.model.chip.mc1kImpl;


import static com.youchip.youmobile.model.chip.mc1kImpl.MC1KChipSpecs.Structure.BYTES_PER_BLOCK;
import com.youchip.youmobile.model.chip.interfaces.ChipField;
import java.io.Serializable;


public abstract class MC1KChipSpecs {

    public enum AccessMode {

        IDLE_KEYA(0x00), ALL_KEYA(0x01), IDLE_KEYB(0x02), ALL_KEYB(0x03);

        private int value;

        private AccessMode(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum OpResult {

        SUCCESS(0x00), LENGTH_NOT_MATCH(0x02), SEND_FAIL(0x03), PORT_NOT_RECEIVING_DATA(0x04), DEVICE_ADDRESS_NOT_MATCH(
                0x05), CHECKSUM_NOT_CORRECT(0x07), INVALID_PARAMETER(0x08);

        private int value;

        private OpResult(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum Structure {

        SECTOR_COUNT(16), BLOCKS_PER_SECTOR(4), BYTES_PER_BLOCK(16);

        private int value;

        private Structure(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }

    public enum Security {

        DEFAULT_KEY_A((byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF), DEFAULT_KEY_B(
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF);

        private final byte[] key = new byte[6];

        private Security(byte a, byte b, byte c, byte d, byte e, byte f) {
            this.key[0] = a;
            this.key[1] = b;
            this.key[2] = c;
            this.key[3] = d;
            this.key[4] = e;
            this.key[5] = f;
        }

        public byte[] getValue() {
            return this.key;
        }

        public int getSize() {
            return this.key.length;
        }
    }

    public enum FactoryFields implements ChipField {
        UID      (0, 0, 0, 4, 1, false, false),
        EVENT_ID (1, 0, 1, 1, 1, false, true),
        APPTYPE  (1, 0, 0, 1, 1, false, true);

        private int block1;
        private int block2;
        private int size;
        private int bytePos;
        private int multiplicity;
        private boolean useTxc;
        private boolean useCrc;

        /**
         * all the fields a chip may have and their position on chip

         * @param blockPos1
         *            first occurrence of the data
         * @param blockPos2
         *            redundant occurrence of the data if transaction safety is
         *            used
         * @param bytePos
         *            absolute byte position 0 - (16*4*16)
         * @param size
         *            size of the date
         */
        private FactoryFields(int blockPos1, int blockPos2, int bytePos, int size,
                int multiplicity, boolean useTxc, boolean useCrc) {

//            int blockOffset = (bytePos + (size * multiplicity) - 1)
//                    / getNettoBlockSize(useCrc, useTxc);

//            assert !(1 > size) :
//                "Field size of field '" + this.toString()
//                        + "' must be greater than '0'";
//            assert !(1 > multiplicity) :
//                "Multiplicity of field '" + this.toString()
//                        + "' must be greater than '0'";
//            assert !((blockPos1 < 0) || (blockPos1 % 4 == 3)) :
//                "Disallowed meta data block usage (Sector:"
//                        + (blockPos1 / 4) + ", Block:" + (blockPos1 % 4) + ") by field '"
//                        + this.toString() + "'";
//            assert !((useTxc && blockPos2 < 0) || (blockPos2 % 4 == 3)) :
//                "Disallowed meta data block usage (Sector:"
//                        + (blockPos2 / 4) + ", Block:" + (blockPos2 % 4) + ") by field '"
//                        + this.toString() + "'";
//            assert !((!useTxc && blockPos2 > 0)) :
//                        "Inconsistent declaration of transaction safety by field '"
//                                + this.toString()
//                                + "'! If using transaction safety, the backup block must be set to"
//                                + "a value between [1 .. 63].";
//            assert !(bytePos > getNettoBlockSize(useCrc, useTxc) - 1) :
//                "Invalid start byte position of field'" + this.toString()
//                        + "'! Start position must be in range of [0 .. "
//                        + (BYTES_PER_BLOCK.getValue() - 1) + "]";
//            assert !((blockPos1 + blockOffset) % 4 == 3) :
//                "Disallowed meta data block '" + (blockPos1 + blockOffset)
//                        + "' usage by field '" + this.toString()
//                        + "'! Check field size and multiplicity.";
//            assert !((blockPos2 > 0) && ((blockPos2 + blockOffset) % 4 == 3)) :
//                "Disallowed meta data block '" + (blockPos2 + blockOffset)
//                        + "' usage by field '" + this.toString()
//                        + "'! Check field size and multiplicity.";

            this.block1 = blockPos1;
            this.block2 = blockPos2;
            this.bytePos = bytePos;
            this.size = size;
            this.multiplicity = multiplicity;
            this.useCrc = useCrc;
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
        public int getBytePos() {
            return bytePos;
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
        public int getCRC() {
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
    
    public enum AppType implements Serializable {

        NO_APP(0x00, "none"),
        VISITOR_APP(0x01, "visitor"),
        CONFIG_LOAD_APP(0x10,"config"),
        GATE_SWITCHER_APP(0x20,"areaswitch"),
        RESET_APP(0x40,"reset"),
        GATE_LIGHT_APP(0x80,"gatelight");
        
        private final int type;
        private final String name;
        
        private AppType(int typeValue,String name){
            this.type = typeValue;
            this.name = name;
        }
        
        public int getValue(){
            return type;
        }

        @Override
        public String toString(){
            return this.name;
        }
        
        public static AppType fromInteger(long x) {
            return fromInteger((int) x);
        }
        
        public static AppType fromInteger(int x) {
            if (x == 0){
                return NO_APP;
            } else if ((x & 0x0F) > 0 ) {
                return VISITOR_APP;
            } else if (x == 0x10){
                return CONFIG_LOAD_APP;
            } else if (x == 0x20) {
                return GATE_SWITCHER_APP;
            } else if (x == 0x40){
                return RESET_APP;
            } else if (x == 0x80){
                return GATE_LIGHT_APP;
            } else {
                return NO_APP;
            }
        }
    }

}
