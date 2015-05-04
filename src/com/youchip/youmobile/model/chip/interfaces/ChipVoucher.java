package com.youchip.youmobile.model.chip.interfaces;

import java.io.Serializable;

public interface ChipVoucher extends Serializable{
    

    public int getVoucherID();

    public void setVoucherID(int voucherID);

    public int getCount();

    public void setCount(int count);

}
