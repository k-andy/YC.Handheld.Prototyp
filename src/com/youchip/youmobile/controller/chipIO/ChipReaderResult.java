package com.youchip.youmobile.controller.chipIO;

import android.content.Context;

import com.youchip.youmobile.model.chip.interfaces.BasicChip;

/**
 * Created by muelleco on 28.04.2014.
 */
public interface ChipReaderResult {

    public boolean onValidChipReadResult(BasicChip basicChip);
    public boolean onInValidChipReadResult();
    public void onChipAccessGranted(BasicChip chip);
    public void onChipAccessDenied(BasicChip chip);
    public void onChipAccessDenied(int resourceID);
}
