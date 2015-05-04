package instrumentTest.Java.com.youchip.youmobile.model.chip.test;

import android.test.InstrumentationTestCase;

import com.youchip.youmobile.model.chip.interfaces.BasicChip;
import com.youchip.youmobile.model.chip.interfaces.Chip;
import com.youchip.youmobile.model.chip.interfaces.ChipField;
import com.youchip.youmobile.model.chip.mc1kImpl.MC1KBasicChip;
import com.youchip.youmobile.model.chip.mc1kImpl.MC1KChipSpecs;
import com.youchip.youmobile.model.chip.mc1kImpl.MC1KConfigChipField;
import com.youchip.youmobile.model.chip.mc1kImpl.MC1KVisitorChipField;
import static com.youchip.youmobile.model.chip.mc1kImpl.MC1KChipSpecs.Structure.BLOCKS_PER_SECTOR;
import static com.youchip.youmobile.model.chip.mc1kImpl.MC1KChipSpecs.Structure.BYTES_PER_BLOCK;
import static com.youchip.youmobile.model.chip.mc1kImpl.MC1KChipSpecs.Structure.SECTOR_COUNT;

import java.util.ArrayList;
import java.util.List;

import instrumentTest.Java.com.youchip.youmobile.FactoryBlockedBytes;

/**
 * Created by muelleco on 08.05.2014.
 */
public class ChipFieldValidityTest extends InstrumentationTestCase {

    public void testVisitorFieldsHaveValidMC1KPositionsAndSize(){
        for (ChipField field : MC1KVisitorChipField.values()){
            standardValidator(field);
        }
    }

    public void testConfigFieldsHaveValidMC1KPositionsAndSize(){
        for (ChipField field : MC1KConfigChipField.values()){
            standardValidator(field);
        }
    }

    public void testBasicFieldsHaveValidMC1KPositionsAndSize(){
        for (ChipField field : MC1KChipSpecs.FactoryFields.values()){
            chipSpecValidator(field);
        }
    }


    public void testVisitorChipFieldCollision(){
        List<ChipField[]> chip = getEmptyChip();
        chipFieldCollisionDetector(chip, MC1KVisitorChipField.values());
        chipFieldCollisionDetector(chip, MC1KChipSpecs.FactoryFields.APPTYPE,MC1KChipSpecs.FactoryFields.EVENT_ID);
    }

    public void testConfigChipFieldCollision(){
        List<ChipField[]> chip = getEmptyChip();
        chipFieldCollisionDetector(chip, MC1KConfigChipField.values());
        chipFieldCollisionDetector(chip, MC1KChipSpecs.FactoryFields.APPTYPE,MC1KChipSpecs.FactoryFields.EVENT_ID);
    }


    private void chipFieldCollisionDetector(List<ChipField[]> chip, ChipField ... chipFields){

        for (ChipField field : chipFields){

            assertTrue(field.getBytePos()+field.getSize() < BYTES_PER_BLOCK.getValue());
            assertTrue(field.getBytePos()+field.getSize() > 0);

            for(int i = 0; i < field.getSize(); i++){
                //check for collision with previous checked fields
                assertNull(chip.get(field.getBlock1Pos())[field.getBytePos()+i]);
                if(field.getBlock2Pos() > 0) { //if used
                    assertNull(chip.get(field.getBlock2Pos())[field.getBytePos() + i]);
                }

                //fill for check with other fields
                chip.get(field.getBlock1Pos())[field.getBytePos()+i] = field;
                if(field.getBlock2Pos() > 0) { //if used
                    chip.get(field.getBlock2Pos())[field.getBytePos()+i] = field;
                }
            }
        }
    }



    private List<ChipField[]> getEmptyChip(){
        List<ChipField[]> chip = new ArrayList<>();
        int blockCount= BLOCKS_PER_SECTOR.getValue() * SECTOR_COUNT.getValue();

        for (int i=0; i < blockCount ; i++){
            ChipField[] block = new ChipField[BYTES_PER_BLOCK.getValue()];
            chip.add(block);

            if(i==0 || i%4==3){//if is block zero or trailer block
                for(int j = 0; j < BYTES_PER_BLOCK.getValue(); j++ ){
                    block[j] = new FactoryBlockedBytes();
                }
            }
        }

        return chip;
    }


    private void standardValidator(ChipField field){
        int blockPos1 = field.getBlock1Pos();
        int blockPos2 = field.getBlock2Pos();
        int bytePos   = field.getBytePos();
        int size      = field.getSize();
        int multiplicity = field.getMultiplicity();
        boolean useTxc = field.isTxnSave();
        boolean useCrc = field.usesCRC();

        int blockOffset = (bytePos+(size*multiplicity)-1) / field.getNettoBlockSize();

        //"Field size of field '" + this.toString() + "' must be greater than '0'"
        assertFalse(1 > size);

        //"Multiplicity of field '" + this.toString() + "' must be greater than '0'"
        assertFalse(1 > multiplicity);

        //"Disallowed meta data block usage (Sector:"+ (blockPos1 / 4) + ", Block:" + (blockPos1 % 4) + ") by field '" + this.toString() + "'"
        assertFalse((blockPos1 <= 0) || (blockPos1 % 4 == 3));

        //"Disallowed meta data block usage (Sector:"+ (blockPos2 / 4) + ", Block:" + (blockPos2 % 4) + ") by field '" + this.toString() + "'"
        assertFalse((useTxc && blockPos2 <= 0) || (blockPos2 % 4 == 3));

        //"Inconsistent declaration of transaction safety by field '" +this.toString() + "'! If using transaction safety, the backup block must be set to" +"a value between [1 .. 63]."
        assertFalse((!useTxc && blockPos2 > 0));

        //"Invalid start byte position of field'" +this.toString() + "'! Start position must be in range of [0 .. " + (BYTES_PER_BLOCK.getValue() - 1) + "]"
        assertFalse(bytePos > field.getNettoBlockSize() - 1);

        //"Disallowed meta data block '" + (blockPos1 + blockOffset) + "' usage by field '" + this.toString() + "'! Check field size and multiplicity."
        assertFalse((blockPos1 + blockOffset) % 4 == 3);

        //"Disallowed meta data block '" + (blockPos2 + blockOffset) + "' usage by field '" + this.toString() + "'! Check field size and multiplicity"
        assertFalse((blockPos2 > 0) && ((blockPos2 + blockOffset) % 4 == 3));
    }


    private void chipSpecValidator(ChipField field){
        int blockPos1 = field.getBlock1Pos();
        int blockPos2 = field.getBlock2Pos();
        int bytePos   = field.getBytePos();
        int size      = field.getSize();
        int multiplicity = field.getMultiplicity();
        boolean useTxc = field.isTxnSave();
        boolean useCrc = field.usesCRC();

        int blockOffset = (bytePos + (size * multiplicity) - 1)/ field.getNettoBlockSize();

        assertFalse(1 > size);

        assertFalse(1 > multiplicity);

        assertFalse((blockPos1 < 0) || (blockPos1 % 4 == 3));

        //"Disallowed meta data block usage (Sector:" + (blockPos2 / 4) + ", Block:" + (blockPos2 % 4) + ") by field '"+ this.toString() + "'";
        assertFalse((useTxc && blockPos2 < 0) || (blockPos2 % 4 == 3));

        //"Inconsistent declaration of transaction safety by field '"+ this.toString()+ "'! If using transaction safety, the backup block must be set to"+ "a value between [1 .. 63]."
        assertFalse((!useTxc && blockPos2 > 0));

        //"Invalid start byte position of field'" + this.toString()+ "'! Start position must be in range of [0 .. "+ (BYTES_PER_BLOCK.getValue() - 1) + "]";
        assertFalse(bytePos > field.getNettoBlockSize() - 1);

        //"Disallowed meta data block '" + (blockPos1 + blockOffset)+ "' usage by field '" + this.toString()+ "'! Check field size and multiplicity."
        assertFalse((blockPos1 + blockOffset) % 4 == 3);

        //"Disallowed meta data block '" + (blockPos2 + blockOffset)+ "' usage by field '" + this.toString()+ "'! Check field size and multiplicity."
        assert !((blockPos2 > 0) && ((blockPos2 + blockOffset) % 4 == 3));
    }


}
