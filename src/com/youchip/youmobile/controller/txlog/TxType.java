package com.youchip.youmobile.controller.txlog;

import java.io.Serializable;


public enum TxType implements Serializable{
    LOAD_CREDIT(10),
    UNLOAD_CREDIT(40),
    BUY_ARTICLE(20),
    CANCELATION(30),
    ACCES_CONTROL(50),
    HELPDESK(60),
    STATUS(70),
    AREA_CHANGE(71),
    CFG_READ(72),
    NO_TX_LOG(88),
    DEBUG(99);
    
    private int txTypeID;
    
    private TxType(int typeID){
        this.txTypeID = typeID;
    }
    
    public int getType(){
        return txTypeID;
    }
    
    public static TxType TxTypeFromInteger(int x){
        switch(x){
            case 10: return LOAD_CREDIT;
            case 40: return UNLOAD_CREDIT;
            case 20: return BUY_ARTICLE;
            case 30: return CANCELATION;
            case 50: return ACCES_CONTROL;
            case 60: return HELPDESK;
            case 70: return STATUS;
            case 71: return AREA_CHANGE;
            case 72: return CFG_READ;
            case 88: return NO_TX_LOG;
            case 99: return DEBUG;
            default: return null;
        }
    }
    
    @Override
    public String toString(){
       return String.valueOf(txTypeID);
    }
}
