package com.youchip.youmobile.controller.txlog;

import java.io.Serializable;

public class PayResult implements Serializable{
    
    private static final long serialVersionUID = 7482242894643814252L;
    private PayState payState;
    private long amount;
    
    public PayResult(PayState payState, long amount){
        this.setPaymentAmount(amount);
        this.setPayResult(payState);
    }
    
    public PayState getPayState() {
        return payState;
    }

    public void setPayResult(PayState payResult) {
        this.payState = payResult;
    }

    public long getPaymentAmount() {
        return amount;
    }

    public void setPaymentAmount(long amount) {
        this.amount = amount;
    }
    
    public enum PayState implements Serializable{
        PAYABLE,
        NOT_PAYABLE,
        NOT_CHARGABLE
    }

}
