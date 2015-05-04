package com.youchip.youmobile.controller.txlog;

import static com.youchip.youmobile.controller.shop.PaymentMethod.PAYMENT_NONE;
import static com.youchip.youmobile.controller.txlog.TxLogger.LogLevel.INFO;
import static com.youchip.youmobile.controller.txlog.TxGateLogger.AccessState.CHECKED_OUT;
import static com.youchip.youmobile.controller.txlog.TxType.*;

import android.content.Context;

public class TxHelpDeskLogger extends TxLogger{

    public TxHelpDeskLogger(Context context, String operatorNumber) {
        super(context, operatorNumber);
    }
    
    /**
     * Gate Specific transaction log.
     * @param chipID
     * @param areaID
     * @param accessState
     * @param message
     */
    public boolean i(String chipID, long areaID, String message){
        return log(INFO, HELPDESK, chipID, 0, 0, 0, PAYMENT_NONE, 0, 0, areaID, CHECKED_OUT.toString(), message);
    }

}
