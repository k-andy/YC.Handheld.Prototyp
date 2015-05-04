package com.youchip.youmobile.controller.txlog;

import static com.youchip.youmobile.controller.txlog.TxLogger.LogLevel.INFO;
import static com.youchip.youmobile.controller.txlog.TxType.ACCES_CONTROL;
import static com.youchip.youmobile.controller.shop.PaymentMethod.*;

import android.content.Context;

public class TxGateLogger extends TxLogger{

    public TxGateLogger(Context context, String operatorNumber) {
        super(context, operatorNumber);
    }

    /**
     * Gate Specific transaction log.
     * @param chipID
     * @param areaID
     * @param accessState
     * @param message
     */
    public boolean accessControll(String chipID, long areaID, AccessState accessState, String message){
        return log(INFO, ACCES_CONTROL, chipID, 0, 0, 0, PAYMENT_NONE, 0, 0, areaID, accessState.toString(), message);
    }
    
    
    public enum AccessState{
        PASSED("0"),
        CHECKED_IN("1"),
        CHECKED_OUT("2"),
        BLOCKED("3"),
        BANNED("3");
        
        String stateID;
        
        private AccessState(String stateID){
            this.stateID = stateID;
        }
        
        @Override
        public String toString(){
            return String.valueOf(stateID);
        }
    }
}
