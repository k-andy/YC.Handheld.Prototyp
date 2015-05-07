package com.youchip.youmobile.controller.txlog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.youchip.youmobile.controller.settings.ConfigAccess;
import com.youchip.youmobile.controller.shop.PaymentMethod;
import com.youchip.youmobile.controller.txlog.TxGateLogger.AccessState;
import com.youchip.youmobile.utils.DataConverter;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static com.youchip.youmobile.controller.shop.PaymentMethod.PAYMENT_NONE;
import static com.youchip.youmobile.controller.txlog.TxLogger.LogLevel.ALL;
import static com.youchip.youmobile.controller.txlog.TxLogger.LogLevel.CRITICAL;
import static com.youchip.youmobile.controller.txlog.TxLogger.LogLevel.INFO;
import static com.youchip.youmobile.controller.txlog.TxType.AREA_CHANGE;
import static com.youchip.youmobile.controller.txlog.TxType.CFG_READ;
import static com.youchip.youmobile.controller.txlog.TxType.DEBUG;
import static com.youchip.youmobile.controller.txlog.TxType.NO_TX_LOG;
import static com.youchip.youmobile.controller.txlog.TxType.STATUS;
import static com.youchip.youmobile.utils.DataConverter.javaDateToServiceFormat;

public class TxLogger {
    
    private static final String firstLevelSeparator = ";";
    public final static String NO_UID = "";

    private volatile String operatorNumber = "";
    private JournalFileLogger androidLogger;
    private static final String logFile = ConfigAccess.getLogMainFile();
    private static final String dateFormat = DataConverter.getServiceDateFormatString();
    private static final String LOG_DATE_FORMAT = "yyyy-MM-dd_HH-mm-ss-SSS";
    private long MAX_LOG_FILE_SIZE;
    private final Context context;

    private final String LOG_TAG;

    /**
     * creates a new TX logger instance
     * @param context
     */
    public TxLogger(Context context, String operatorNumber){
        this.context = context; 
        this.operatorNumber = operatorNumber;
        androidLogger = new JournalFileLogger(context,logFile);
        MAX_LOG_FILE_SIZE = ConfigAccess.getMaxLogFileSize(context);
        LOG_TAG = TxLogger.this.getClass().getName();
    }
    
    /**
     * handles multi file logs, logrotate and backup log
     * @param message
     */
    protected boolean logRotate(String message){
            // write log
            File file = androidLogger.writeExternalLog(message);
            
            if (file != null){
                // if file is bigger than maximum, rename it.
                Log.d(LOG_TAG, "Log file size (" + (file.length()/1000) + " kB)");
                
                if (file != null && file.length() > MAX_LOG_FILE_SIZE){
                    Log.d(LOG_TAG, "Log file size is bigger than the maximum of " + (MAX_LOG_FILE_SIZE/1000) +" kB");
                    renameFile(this.context, file);
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
    }

    protected void reportLogRotate(Activity activity) {
        try {
            androidLogger.writeExternalReportLog(activity);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static File renameFile(Context context, File from){
        String filename = getLogFileName(context); 
        Log.d("TxLogger", "Rename log file to '" + filename +".");
        File newFile = new File(context.getExternalFilesDir(null), filename);
        from.renameTo(newFile);
        
        if (newFile.exists()){
            Log.d("TxLogger", "Renaming successful ("+newFile.toString()+")");
        } else {
            Log.w("TxLogger", "Renaming unsuccessful ("+newFile.toString()+")");
        }
        
        return newFile;
    }

    @SuppressLint("SimpleDateFormat")
    public static String getLogFileName(Context context){
        SimpleDateFormat simpleFormat = new SimpleDateFormat(LOG_DATE_FORMAT);
        String now = simpleFormat.format(Calendar.getInstance().getTime());
        String ip = ConfigAccess.getDeviceID(context);
        return "YC_" + ip +"_" + now +".log";
    }

    /**
     * Logs all messages
     * @param logLevel
     * @param txType
     * @param chipID
     * @param credit1
     * @param credit2
     * @param voucher
     * @param paymentMethod
     * @param plu
     * @param amount
     * @param areaID
     * @param accessState
     * @param message
     */
    protected boolean log(LogLevel logLevel, TxType txType, String chipID, long credit1, long credit2, long voucher, PaymentMethod paymentMethod, long plu, long amount, long areaID, String accessState, String message){
        String time     = getLogTime();
        String plainMsg = createLogString(logLevel, txType, chipID, time, credit1, credit2, voucher, paymentMethod, plu, amount, areaID, accessState, message);

        return logRotate(plainMsg);
    }
    
    protected String getLogTime(){
        return javaDateToServiceFormat(Calendar.getInstance().getTime(), dateFormat);
    }
    
    
    /**
     * Creates the current log string
     */
    protected String createLogString(LogLevel logLevel, TxType txType, String chipID, String time, long credit1, long credit2, long voucher, PaymentMethod paymentMethod, long plu, long amount, long areaID, String accessState, String message){
        String logString = 
                logLevel                         + firstLevelSeparator +
                txType                           + firstLevelSeparator +
                chipID.replaceAll("\\s","")      + firstLevelSeparator +
                ConfigAccess.getDeviceID(context)+ firstLevelSeparator +
                time                             + firstLevelSeparator +
                operatorNumber                   + firstLevelSeparator +
                credit1                          + firstLevelSeparator +
                credit2                          + firstLevelSeparator +
                voucher                          + firstLevelSeparator +
                paymentMethod                    + firstLevelSeparator +
                plu                              + firstLevelSeparator +
                amount                           + firstLevelSeparator +
                areaID                           + firstLevelSeparator +
                accessState                      + firstLevelSeparator +
                message                          + firstLevelSeparator +
                ConfigAccess.getEventID(context);
        
        String sha1     = getSHA1(logString);
        
        return logString + firstLevelSeparator + sha1;
    }
    
    
    public String createNoFileInfoLog(){
        String time     = getLogTime();
        String plainMsg = createLogString(ALL, NO_TX_LOG, NO_UID, time, 0, 0, 0, PAYMENT_NONE, 0, 0, 0, "", "heartbeat");

        return plainMsg;
    }

    
    /**
     * logging events which are used to trace the prorgam workflow
     * @param txType
     * @param chipID
     * @param credit1
     * @param credit2
     * @param voucher
     * @param paymentMethod
     * @param plu
     * @param amount
     * @param areaID
     * @param accessState
     * @param message
     */
    protected boolean d(TxType txType, String chipID, long credit1, long credit2, long voucher, PaymentMethod paymentMethod, long plu, long amount, long areaID, String accessState, String message){
        return log(ALL, txType, chipID, credit1, credit2, voucher, paymentMethod, plu, amount, areaID, accessState, message);
    }
    
    public boolean d(String message){
        return log(ALL, DEBUG, NO_UID, 0, 0, 0, PAYMENT_NONE, 0, 0, 0, "", message);
    }
    
    public boolean status(String message){
        return log(ALL, STATUS, NO_UID, 0, 0, 0, PAYMENT_NONE, 0, 0, 0, "", message);
    }
    
    public boolean configRead(String chipID, String message){
        return log(ALL, CFG_READ, chipID, 0, 0, 0, PaymentMethod.PAYMENT_NONE, 0, 0, 0, "", message);
    }
    
    public boolean areaChange(String chipID, String message){
        return log(ALL, AREA_CHANGE, chipID, 0, 0, 0, PaymentMethod.PAYMENT_NONE, 0, 0, 0, "", message);
    }
    
    public boolean curruptedCRC(String chipID){
        return log(ALL, STATUS, chipID, 0, 0, 0, PaymentMethod.PAYMENT_NONE, 0, 0, 0, "", "CRC Error! Chip is corrupted.");
    }
    
    public boolean invalidAppType(String chipID){
        return log(ALL, STATUS, chipID, 0, 0, 0, PaymentMethod.PAYMENT_NONE, 0, 0, 0, "", "Invalid chip status!");
    }
    
    public boolean invalidEvent(String chipID){
        return log(ALL, STATUS, chipID, 0, 0, 0, PaymentMethod.PAYMENT_NONE, 0, 0, 0, "", "Invalid event!");
    }
    
    public boolean invalidLogin(String chipID, String message){
        return log(ALL, STATUS, chipID, 0, 0, 0, PaymentMethod.PAYMENT_NONE, 0, 0, 0, "", "Invalid login!" + message);
    }
    
    public boolean chipIsBanned(String chipID){
        return log(INFO, STATUS, chipID, 0, 0, 0, PAYMENT_NONE, 0, 0, 0, AccessState.BANNED.toString(), "Chip is banned!");
    }

    /**
     * Everything which is an Technical error must be logged as an error
     * @param txType
     * @param chipID
     * @param credit1
     * @param credit2
     * @param voucher
     * @param paymentMethod
     * @param plu
     * @param amount
     * @param areaID
     * @param accessState
     * @param message
     */
    public boolean e(TxType txType, String chipID, long credit1, long credit2, long voucher, PaymentMethod paymentMethod, long plu, long amount, long areaID, String accessState, String message){
        return log(CRITICAL, txType, chipID, credit1, credit2, voucher, paymentMethod, plu, amount, areaID, accessState, message);
    }
    

    
    public enum LogLevel {
        NONE(0),
        CRITICAL(1),
        INFO(2),
        ALL(3);
        
        private int level;
        
        private LogLevel(int level){
            this.level = level;
        }
        
        public int getLevel(){
            return level;
        }
        
        @Override
        public String toString(){
           return String.valueOf(level);
        }
    }
    
    
    public String getSHA1(String message) {
        try {
            MessageDigest cript;
            cript = MessageDigest.getInstance("SHA-1");
            cript.reset();
            cript.update(message.getBytes("utf8"));
            return DataConverter.byteArrayToHexString(cript.digest()).toLowerCase(Locale.ENGLISH);
        } catch (NoSuchAlgorithmException e) {
            Log.d(LOG_TAG,"Failed to calculate hashvalue",e);
            return "";
        } catch (UnsupportedEncodingException e) {
            Log.d(LOG_TAG,"Failed to calculate hashvalue",e);
            return "";
        }
    }

    public String getOperatorNumber() {
        return operatorNumber;
    }

    public void setOperatorNumber(String operatorNumber) {
        this.operatorNumber = operatorNumber;
    }
    
}
