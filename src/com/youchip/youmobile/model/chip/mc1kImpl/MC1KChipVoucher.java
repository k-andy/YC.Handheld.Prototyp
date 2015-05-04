package com.youchip.youmobile.model.chip.mc1kImpl;

import com.youchip.youmobile.model.chip.interfaces.ChipVoucher;

public class MC1KChipVoucher implements ChipVoucher {

    /**
     * 
     */
    private static final long serialVersionUID = 4733976001041020828L;
    private int voucherID = 0;
    private int count     = 0;
    
    public MC1KChipVoucher() {

    }
    
    public MC1KChipVoucher(int voucherID, int count) {
        this.voucherID = voucherID;
        this.count     = count;
    }

    public int getVoucherID() {
        return voucherID;
    }

    public void setVoucherID(int voucherID) {
        this.voucherID = voucherID;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

}
