package com.youchip.youmobile.controller.shop;

import java.io.Serializable;

public enum PaymentMethod implements Serializable{
    PAYMENT_NONE(0),
    PAYMENT_CHIP(1),
    PAYMENT_CASH(2),
    PAYMENT_CARD(3);
    
    private final int txID;
    
    private PaymentMethod(int txID){
        this.txID = txID;
    }
    
    public String toString(){
        return String.valueOf(txID);
    }
    
    public static PaymentMethod paymentMethodByInt(int id){
        switch(id){
            case 0 : return PaymentMethod.PAYMENT_NONE;
            
            case 1 : return PaymentMethod.PAYMENT_CHIP;

            case 2 : return PaymentMethod.PAYMENT_CASH;

            case 3 : return PaymentMethod.PAYMENT_CARD;
            
            default : return PaymentMethod.PAYMENT_NONE;
        }
    }
}
