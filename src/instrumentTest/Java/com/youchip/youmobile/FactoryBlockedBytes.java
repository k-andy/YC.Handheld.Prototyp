package instrumentTest.Java.com.youchip.youmobile;

import com.youchip.youmobile.model.chip.interfaces.ChipField;

/**
 * Created by muelleco on 09.05.2014.
 */
public class FactoryBlockedBytes implements ChipField {


    @Override
    public int getBlock1Pos() {
        return 0;
    }

    @Override
    public int getBlock2Pos() {
        return 0;
    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public int getMultiplicity() {
        return 0;
    }

    @Override
    public int getTotalSize() {
        return 0;
    }

    @Override
    public int getBytePos() {
        return 0;
    }

    @Override
    public boolean isTxnSave() {
        return false;
    }

    @Override
    public int getTxnPos() {
        return 0;
    }

    @Override
    public int getCRC() {
        return 0;
    }

    @Override
    public int getNettoBlockSize() {
        return 0;
    }

    @Override
    public boolean usesCRC() {
        return false;
    }
}
