package com.youchip.youmobile.controller.txlog;


import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.youchip.youmobile.controller.shop.PaymentMethod;
import com.youchip.youmobile.model.shop.ShopItemForReport;

import java.util.ArrayList;
import java.util.List;

import static com.youchip.youmobile.controller.txlog.TxLogger.LogLevel.CRITICAL;

public class TxShopLogger extends TxLogger {
    private List<String> logBuffer = new ArrayList<>();

    public TxShopLogger(Context context, String operatorNumber) {
        super(context, operatorNumber);
    }

    public void addToTempLog(TxType txType, String chipID, long credit1, long credit2, long voucherUsed,
                             PaymentMethod paymentMethod, long plu, long amount, double vat, String title, long price) {
        String time = getLogTime();
        logBuffer.add(createLogString(CRITICAL, txType, chipID, time, credit1, credit2, voucherUsed, paymentMethod, plu, amount, 0, "", ""));

        if (txType.equals(TxType.CANCELATION)) {
            ShopItemForReport.addTmpCancelation(plu, amount);
        } else {
            ShopItemForReport.addTmpAmount(plu, amount);
        }

        ShopItemForReport.addTmpPrice(plu, price);
        ShopItemForReport.addTmpVAT(plu, (float) vat);
        ShopItemForReport.addTmpTitle(plu, title);

        Log.d(TxShopLogger.class.getName(), "Added Entry to Log buffer. Size is: " + logBuffer.size());
    }

    public void clearLog() {
        logBuffer.clear();
        Log.d(TxShopLogger.class.getName(), "Log buffer cleared.");
    }

    public void saveLog(Activity activity) {
        for (String logEntry : logBuffer) {
            logRotate(logEntry);
        }
        reportLogRotate(activity);

        Log.d(TxShopLogger.class.getName(), "Log buffer commited");
        clearLog();
    }
}
