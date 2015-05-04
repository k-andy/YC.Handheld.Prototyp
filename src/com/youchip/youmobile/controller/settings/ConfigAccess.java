package com.youchip.youmobile.controller.settings;


import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.provider.Settings.Secure;
import android.util.Log;

import com.youchip.youmobile.model.gate.AreaConfig;
import com.youchip.youmobile.model.gate.BlockedChip;
import com.youchip.youmobile.model.gate.VisitorRole;
import com.youchip.youmobile.model.shop.ShopItemConfig;
import com.youchip.youmobile.model.shop.VoucherInfo;
import com.youchip.youmobile.utils.SystemInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.youchip.youmobile.model.network.BasicSOAPConfigFields.ARTICLECOLORCANCEL;
import static com.youchip.youmobile.model.network.BasicSOAPConfigFields.ARTICLECOLORDEFAULT;
import static com.youchip.youmobile.model.network.BasicSOAPConfigFields.ARTICLECOLORLOAD;
import static com.youchip.youmobile.model.network.BasicSOAPConfigFields.ARTICLECOLORUNDLOAD;
import static com.youchip.youmobile.model.network.BasicSOAPConfigFields.ARTICLEFONTSIZE;
import static com.youchip.youmobile.model.network.BasicSOAPConfigFields.ARTICLE_PAYABLE_CASH;
import static com.youchip.youmobile.model.network.BasicSOAPConfigFields.BLACKLISTUPDATEINTERVAL;
import static com.youchip.youmobile.model.network.BasicSOAPConfigFields.CHIPRESULTDISPLAYDELAY;
import static com.youchip.youmobile.model.network.BasicSOAPConfigFields.CONFIGKEYA;
import static com.youchip.youmobile.model.network.BasicSOAPConfigFields.FIRSTCURRENCYTEXT;
import static com.youchip.youmobile.model.network.BasicSOAPConfigFields.GATEACCESSDELAY;
import static com.youchip.youmobile.model.network.BasicSOAPConfigFields.IDEVENT;
import static com.youchip.youmobile.model.network.BasicSOAPConfigFields.IGNORE_VOUCHER_VALIDITY;
import static com.youchip.youmobile.model.network.BasicSOAPConfigFields.KEY_A;
import static com.youchip.youmobile.model.network.BasicSOAPConfigFields.LOGFILEMAXSIZE;
import static com.youchip.youmobile.model.network.BasicSOAPConfigFields.LOGSYNCINTERVAL;
import static com.youchip.youmobile.model.network.BasicSOAPConfigFields.MAX1STCASHOUT;
import static com.youchip.youmobile.model.network.BasicSOAPConfigFields.MAX1STCUR;
import static com.youchip.youmobile.model.network.BasicSOAPConfigFields.MAX2NDCUR;
import static com.youchip.youmobile.model.network.BasicSOAPConfigFields.PAY_CREDIT_ONLY_OPTION;
import static com.youchip.youmobile.model.network.BasicSOAPConfigFields.SECONDCURRENCYTEXT;
import static com.youchip.youmobile.model.network.BasicSOAPConfigFields.SVCSTANDBY;
import static com.youchip.youmobile.model.network.BasicSOAPConfigFields.TICKET_XC_SELECT_TAB;
import static com.youchip.youmobile.model.network.BasicSOAPConfigFields.USEPRODUCTGROUPASVOUCHER;
import static com.youchip.youmobile.model.network.BasicSOAPConfigFields.WAKETIMEREADERSCREEN;



public class ConfigAccess extends Application {

    private static final String BASE_CONFIG_FILE = "config.cfg";
    private static final String KEY_CONFIG_FILE = "config.key";
    private static final String GATE_CONFIG_FILE = "gate.cfg";
    private static final String BLACKLIST_FILE = "blacklist.cfg";
    private static final String ARTICLE_CONFIG_FILE = "article.cfg";
    private static final String GROUP_CONFIG_FILE = "group.cfg";
    private static final String LOG_MAIN_FILE = "yc.activitiy.log";
    private static final String REPORT_LOG_MAIN_FILE = "yc.activitiy.reportlog";

    private static final String CONFIG_SERVICE_HOST = "service_host";
    private static final String CONFIG_SERVICE_NAME = "service_name";
    private static final String CONFIG_DEVICE_IP= "DeviceIP";

    private static final String CONFIG_DEFAULT_CHIP_MAX_MAX2NDCUR = "100000";
    private static final int CONFIG_DEFAULT_CHIP_MAX_MAX2NDCUR_VAL = 100000;
    private static final String CONFIG_DEFAULT_CHIP_MAX_MAX1STCUR = "500000";
    private static final int CONFIG_DEFAULT_CHIP_MAX_MAX1STCUR_VAL = 500000;
    private static final String CONFIG_DEFAULT_CHIP_MAX_CASHOUT = CONFIG_DEFAULT_CHIP_MAX_MAX1STCUR;
    private static final long CONFIG_DEFAULT_CHIP_MAX_CASHOUT_VAL = CONFIG_DEFAULT_CHIP_MAX_MAX1STCUR_VAL;
    private static final String CONFIG_DEFAULT_GATE_ACCESSDELAY = "30";
    private static final String CONFIG_DAFAULT_CURRENCY_SYMBOL = "€";
    private static final String CONFIG_DEFAULT_BLACKLIST_UPDATE_DELAY = "3";
    public static final String CONFIG_DEFAULT_KEY_A = "FFFFFFFFFFFF";
    private static final String CONFIG_DEFAULT_SVC_STANDBY ="true";

    private static final String CONFIG_ARTICLE_COLOR_DEFAULT     = "D6D4D5";
    private static final String CONFIG_ARTICLE_COLOR_LOAD        = "4DE727";
    private static final String CONFIG_ARTICLE_COLOR_UNLOAD      = "EA5115";
    private static final String CONFIG_ARTICLE_COLOR_CANCEL      = "E64D11";
    private static final String CONFIG_ARTICLE_FONT_SIZE          = "16.5";


    public static SharedPreferences getSettings(Context context){
        return context.getSharedPreferences(BASE_CONFIG_FILE, MODE_PRIVATE);
    }


    
    public static void putRawKeyValuePair(Context context, String key, String value){

        SharedPreferences settings = context.getSharedPreferences(BASE_CONFIG_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        
        editor.putString(key,value);
        
        editor.commit();
    }

    
    public static boolean getServiceStandby(Context context){
            SharedPreferences settings = getSettings(context);
            String value = settings.getString(SVCSTANDBY.toString(), CONFIG_DEFAULT_SVC_STANDBY);
            Log.d(ConfigAccess.class.getName(), "Loading config " + SVCSTANDBY + " = " + value);
            return Boolean.parseBoolean(value); 
    }
    
    
    
    public static int getZoneCheckInDelay(Context context){
        try {
            SharedPreferences settings = getSettings(context);
            String value = settings.getString(GATEACCESSDELAY.toString(), CONFIG_DEFAULT_GATE_ACCESSDELAY);
            Log.d(ConfigAccess.class.getName(), "Loading config " + GATEACCESSDELAY + " = " + value);
            int checkInDelay = Integer.parseInt(value); 
            return checkInDelay;
        } catch (NumberFormatException nfe){
            return 30;
        }
    }
    
    public static long getMaxLogFileSize(Context context){
        try {
            SharedPreferences settings = getSettings(context);
            String size = settings.getString(LOGFILEMAXSIZE.toString(), "1");
            Log.d(ConfigAccess.class.getName(), "Loading config " + LOGFILEMAXSIZE + " = " + size);
            Long logFileSize = Long.parseLong(size) * 1000; // in kb
            return logFileSize;
        } catch (NumberFormatException e){
            return 1000;
        }
    }


    public static String getServiceURL(Context context) {
        SharedPreferences settings = getSettings(context);
        try {
            String host = settings.getString(CONFIG_SERVICE_HOST,"");
            String name = settings.getString(CONFIG_SERVICE_NAME,"");
            return host + name;
        } catch (Exception e) {
            return "";
        }
    }

    
    public static long getLightDuration(Context context) {       
        try{
            SharedPreferences settings = getSettings(context);
            String val = settings.getString(CHIPRESULTDISPLAYDELAY.toString(), "1000");
            Log.d(ConfigAccess.class.getName(),"Loading config " +  CHIPRESULTDISPLAYDELAY + " = " +  val);
            Long value = Long.parseLong(val); // miliseconds
            return value;
        } catch (Exception e){
            return 1000;
        }
    }

    
    public static long getLogSyncDelay(Context context) {       
        try{
            SharedPreferences settings = getSettings(context);
            String interval = settings.getString(LOGSYNCINTERVAL.toString(), "1");
            Log.d(ConfigAccess.class.getName(), "Loading config " + LOGSYNCINTERVAL + " = " + interval);
            Long logFileSize = Long.parseLong(interval); // from miliseconds in minutes
            return logFileSize * 60000;
        } catch (NumberFormatException nfe){
            return 60000;
        }
    }
    
    public static long getBlackListUpdateDelay(Context context) {
        try{
            SharedPreferences settings = getSettings(context);
            String interval = settings.getString(BLACKLISTUPDATEINTERVAL.toString(), CONFIG_DEFAULT_BLACKLIST_UPDATE_DELAY);

            Log.d(ConfigAccess.class.getName(), "Loading config "  + BLACKLISTUPDATEINTERVAL +" = " + interval);
            Long logFileSize = Long.parseLong(interval); // from miliseconds in minutes
            return logFileSize * 60000;
        } catch (NumberFormatException nfe){
            return 60000;
        }
    }

    public static boolean getIgnoreVoucherValidityTime(Context context) {
        try{
            SharedPreferences settings = getSettings(context);
            String value = settings.getString(IGNORE_VOUCHER_VALIDITY.toString(), "false");
            Log.d(ConfigAccess.class.getName(), "Loading config "  + IGNORE_VOUCHER_VALIDITY +" = " + value);

            boolean boolVal = Boolean.parseBoolean(value);
            return boolVal;
        } catch (NumberFormatException nfe){
            return false;
        }
    }
    
    public static String get1stCurrencySymbol(Context context) {
        SharedPreferences settings = getSettings(context);
        String symbol = settings.getString(FIRSTCURRENCYTEXT.toString(), CONFIG_DAFAULT_CURRENCY_SYMBOL);

        return symbol;
    }
    
    public static String get2ndCurrencySymbol(Context context) {
        SharedPreferences settings = getSettings(context);
        String symbol = settings.getString(SECONDCURRENCYTEXT.toString(), CONFIG_DAFAULT_CURRENCY_SYMBOL);
        Log.d(ConfigAccess.class.getName(), "Loading config " + SECONDCURRENCYTEXT + " = " + symbol);
        return symbol;
    }
    
    public static long getMaxCredit1(Context context) {       
        try{
            SharedPreferences settings = getSettings(context);
            String max = settings.getString(MAX1STCUR.toString(), CONFIG_DEFAULT_CHIP_MAX_MAX1STCUR);
            Log.d(ConfigAccess.class.getName(), "Loading config " + MAX1STCUR + " = " + max);
            Long maxValue = Long.parseLong(max); // from miliseconds in minutes
            return maxValue;
        } catch (NumberFormatException nfe){
            return CONFIG_DEFAULT_CHIP_MAX_MAX1STCUR_VAL;
        }
    }
    
    public static long getMaxCredit2(Context context) {       
        try{
            SharedPreferences settings = getSettings(context);
            String max = settings.getString(MAX2NDCUR.toString(), CONFIG_DEFAULT_CHIP_MAX_MAX2NDCUR);
            Log.d(ConfigAccess.class.getName(), "Loading config " + MAX2NDCUR + " = " + max);
            Long maxValue = Long.parseLong(max);
            return maxValue;
        } catch (NumberFormatException nfe){
            return CONFIG_DEFAULT_CHIP_MAX_MAX2NDCUR_VAL;
        }
    }

    public static long getMaxCashOut(Context context){
        try{
            SharedPreferences settings = getSettings(context);
            String max = settings.getString(MAX1STCASHOUT.toString(), CONFIG_DEFAULT_CHIP_MAX_CASHOUT);
            Log.d(ConfigAccess.class.getName(), "Loading config " + MAX1STCASHOUT + " = " + max);
            Long maxValue = Long.parseLong(max);
            return maxValue;
        } catch (NumberFormatException nfe){
            return CONFIG_DEFAULT_CHIP_MAX_CASHOUT_VAL;
        }
    }


    public static boolean getIsPayableWithCash(Context context) {
        try{
            SharedPreferences settings = getSettings(context);
            String value = settings.getString(ARTICLE_PAYABLE_CASH.toString(), Boolean.TRUE.toString());
            Log.d(ConfigAccess.class.getName(), "Loading config " + ARTICLE_PAYABLE_CASH + " = " + value);
            boolean boolVal = Boolean.parseBoolean(value);
            return boolVal;
        } catch (NumberFormatException nfe){
            return true;
        }
    }

    public static boolean isPayableWithCreditOnly(Context context){
        try{
            SharedPreferences settings = getSettings(context);
            String value = settings.getString(PAY_CREDIT_ONLY_OPTION.toString(), Boolean.FALSE.toString());
            Log.d(ConfigAccess.class.getName(), "Loading config " + PAY_CREDIT_ONLY_OPTION + " = " + value);
            boolean boolVal = Boolean.parseBoolean(value);
            return boolVal;
        } catch (NumberFormatException nfe){
            return false;
        }
    }
    
    public static long getEventID(Context context) {       
        try{
            SharedPreferences settings = getSettings(context);
            Long value = settings.getLong(IDEVENT.toString(), 0);
            Log.d(ConfigAccess.class.getName(), "Loading config " + IDEVENT + " = " + value);
            return value;
        } catch (NumberFormatException nfe){
            return 0;
        }
    }
    
    public static boolean useProductGroupAsVoucherID(Context context){
        try{
            SharedPreferences settings = getSettings(context);
            String value = settings.getString(USEPRODUCTGROUPASVOUCHER.toString(), "1");
            Log.d(ConfigAccess.class.getName(), "Loading config " + USEPRODUCTGROUPASVOUCHER + " = " + value);
            boolean val = Long.parseLong(value) != 0;
            return val;
        } catch (NumberFormatException nfe){
            return true;
        }
    }

    public static long getWakeTimeForReaderScreens(Context context){
        try{
            SharedPreferences settings = getSettings(context);
            String value = settings.getString(WAKETIMEREADERSCREEN.toString(), "30000");
            Log.d(ConfigAccess.class.getName(), "Loading config " + WAKETIMEREADERSCREEN + " = " + value);
            long val = Long.parseLong(value);
            return val;
        } catch (NumberFormatException nfe){
            return 30000;
        }
    }


    public static String getKeyA(Context context) {
        String keyA = CONFIG_DEFAULT_KEY_A;

        Log.d(ConfigAccess.class.getName(), "Loading config " + KEY_A + "..");

        try {
            SharedPreferences settings = getSettings(context);
            String key = settings.getString(KEY_A.toString(), CONFIG_DEFAULT_KEY_A);
            if (key != null && key.length() == 12 && key.toLowerCase().matches("([0-9a-f][0-9a-f]){6}")){
                Log.d(ConfigAccess.class.getName(), "Using valid config " + KEY_A + ".");
                keyA = key;
            } else {
                Log.d(ConfigAccess.class.getName(), "Invalid value! Using fallback");
            }
        } catch (Exception e) {
            Log.d(ConfigAccess.class.getName(), "Invalid value! Using fallback.");
        }

        return keyA;
    }


        public static String getKeyAForConfig(Context context) {
            String keyA = CONFIG_DEFAULT_KEY_A;

            Log.d(ConfigAccess.class.getName(), "Loading config " + CONFIGKEYA + "..");

            try {
                SharedPreferences settings = getSettings(context);
                String key = settings.getString(CONFIGKEYA.toString(), CONFIG_DEFAULT_KEY_A);
                if (key != null && key.length() == 12 && key.toLowerCase().matches("([0-9a-f][0-9a-f]){6}")){
                    Log.d(ConfigAccess.class.getName(), "Using valid config " + CONFIGKEYA + ".");
                    keyA = key;
                } else {
                    Log.d(ConfigAccess.class.getName(), "Invalid value! Using fallback");
                }
            } catch (Exception e) {
                Log.d(ConfigAccess.class.getName(), "Invalid value! Using fallback.");
            }

            return keyA;
        }

    
    public static String getDeviceID(Context context) {       
            SharedPreferences settings = getSettings(context);
            String deviceUID = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
            String deviceID = settings.getString(CONFIG_DEVICE_IP, deviceUID);
            
            if (deviceID == null || deviceID.length() == 0){
                deviceID = deviceUID;
            } 
            
            Log.d(ConfigAccess.class.getName(), "Loading config " + CONFIG_DEVICE_IP + " = " + deviceID);
            return deviceID;
    }
    

    
    public static File getLogFileDirectory(Context context){
        return context.getExternalFilesDir(null);
    }

    public static File getLogMainFile(Context context){
        return new File(getLogFileDirectory(context), LOG_MAIN_FILE);
    }


    public static int getLogFileCount(Context context) {
        int count = 0;

        count = SystemInfo.getLogFiles(getLogFileDirectory(context)).size();
        File file = ConfigAccess.getLogMainFile(context);
        if (file.exists() && file.length() > 0) {
            count++;
        }

        return count;
    }

    public static String getLogMainFile(){
        return LOG_MAIN_FILE;
    }

    public static String getReportLogMainFile() {
        return REPORT_LOG_MAIN_FILE;
    }

    public static void storeServiceURL(Context context, String host, String name) {
        SharedPreferences settings = getSettings(context);

        SharedPreferences.Editor editor = settings.edit();
        editor.putString(CONFIG_SERVICE_HOST, host);
        editor.putString(CONFIG_SERVICE_NAME, name);
        
        editor.commit();
    }
    
    public static void storeEventID(Context context, long eventID){
        SharedPreferences settings = getSettings(context); 
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(IDEVENT.toString(), eventID);
        editor.commit();
    }

    
    public static void storeAreaConfig(Context context, List<AreaConfig> areaConfig){
        ObjectOutputStream os = null;

        try {
            Log.d(ConfigAccess.class.getName(), "Storing  area config..");
            os = new ObjectOutputStream(context.openFileOutput(GATE_CONFIG_FILE, Context.MODE_PRIVATE));
            os.writeObject((Serializable) areaConfig);
            os.close();
        } catch (IOException ioe){
            Log.w(ConfigAccess.class.getName(), "Storing  area config failed!");
        } finally {
                try {
                    if (os != null)
                    os.close();
                } catch (IOException e) {
                    Log.w("Error storing area config", e);
                }
        }
    }
    
    public static void storeBlackList(Context context, List<BlockedChip> blackList) {
        ObjectOutputStream oos = null;
        try{
            oos = new ObjectOutputStream(context.openFileOutput(BLACKLIST_FILE, Context.MODE_PRIVATE));
            oos.writeObject((Serializable) blackList);
            oos.close();
        } catch (IOException ioe) {
            Log.w(ConfigAccess.class.getName(), "Storing  blacklist failed!");
        } finally {

            try {
                if (oos != null)
                    oos.close();
            } catch (IOException e) {
                Log.w("Error storing blacklist!",e);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    public static List<BlockedChip> getBlackList(Context context) {

        ObjectInputStream is = null;
        List<BlockedChip> blackList;

        try {
            is = new ObjectInputStream(context.openFileInput(BLACKLIST_FILE));
            blackList = (List<BlockedChip>) is.readObject();
            is.close();
        } catch (StreamCorruptedException sce) {
            Log.w(ConfigAccess.class.getName(), "No blacklist available.");
            return new LinkedList<>();
        } catch (IOException ioe) {
            Log.w(ConfigAccess.class.getName(), "No blacklist available.");
            return new LinkedList<>();
        } catch (ClassNotFoundException e) {
            Log.w(ConfigAccess.class.getName(), "No blacklist available.");
            return new LinkedList<>();
        } finally {
            try{
            if (is != null)
                is.close();
            } catch (IOException ioe){
                Log.w("Error loading blacklist!",ioe);
            }
        }

        Log.d(ConfigAccess.class.getName(), "Loading Blacklist from configuration.");
        return blackList;
    }

    @SuppressWarnings("unchecked")
    public static List<AreaConfig> getAreaConfig(Context context) {
        ObjectInputStream is = null;
        List<AreaConfig> areaList = null;

        try {
            is = new ObjectInputStream(context.openFileInput(GATE_CONFIG_FILE));
            areaList = (List<AreaConfig>) is.readObject();
            is.close();
        } catch (StreamCorruptedException sce) {
            Log.w(ConfigAccess.class.getName(), "No area config available.");
            return new LinkedList<>();
        } catch (IOException ioe) {
            Log.w(ConfigAccess.class.getName(), "No area config available.");
            return new LinkedList<>();
        } catch (ClassNotFoundException e) {
            Log.w(ConfigAccess.class.getName(), "No area config available.");
            return new LinkedList<>();
        } finally {
            try{
                if (is != null)
                    is.close();
            } catch (IOException ioe){
                Log.w("Error loading area config!",ioe);
            }
        }

        Log.d(ConfigAccess.class.getName(), "Loading  area config from configuration.");
        return areaList;
    }
    
    
    /**
     * Saves the shop configuration to a local file
     * @param context
     * @throws IOException
     */
    public static void storeShopArticleConfig(Context context, Map<Long, ShopItemConfig> shopConfig) {
        ObjectOutputStream os = null;
        try {
            Log.d(ConfigAccess.class.getName(), "Storing  shop article config..");
            FileOutputStream fos = context.openFileOutput(ARTICLE_CONFIG_FILE, Context.MODE_PRIVATE);
            os = new ObjectOutputStream(fos);
            os.writeObject(shopConfig);
            os.close();
        } catch (IOException ioe){
            Log.w(ConfigAccess.class.getName(), "Storing  shop article config failed!");
        }  finally {
            try{
                if (os != null)
                    os.close();
            } catch (IOException ioe){
                Log.w("Error storing shop article config!",ioe);
            }
        }
    }

    public static void storeVoucherGroups(Context context, Map<Long, VoucherInfo> groupConfig) {
        ObjectOutputStream os = null;
        try {
            Log.d(ConfigAccess.class.getName(), "Storing  shop group config..");
            FileOutputStream fos = context.openFileOutput(GROUP_CONFIG_FILE, Context.MODE_PRIVATE);
            os = new ObjectOutputStream(fos);
            os.writeObject(groupConfig);
            os.close();
        } catch (IOException ioe){
            Log.w(ConfigAccess.class.getName(), "Storing  shop group config failed!", ioe);
        }  finally {
            try{
                if (os != null)
                    os.close();
            } catch (IOException ioe){
                Log.w(ConfigAccess.class.getName(),"Error storing shop group config!",ioe);
            }
        }
    }

    
    /**
     * returns null if not successfull
     * @param context
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Map<Long,ShopItemConfig> getArticleConfig(Context context) {
        FileInputStream fis;
        ObjectInputStream is = null;
        Map<Long,ShopItemConfig> persistentObject = null;
        try {
            fis = context.openFileInput(ARTICLE_CONFIG_FILE);
            is = new ObjectInputStream(fis);
            persistentObject = (Map<Long,ShopItemConfig>) is.readObject();
            is.close();
        } catch (FileNotFoundException e) {
            Log.w(ConfigAccess.class.getName(), e.getMessage());
        } catch (ClassNotFoundException | IOException e) {
            Log.w(ConfigAccess.class.getName(), "No shop article configs available.");
        } finally {
            try{
                if (is != null)
                    is.close();
            } catch (IOException ioe){
                Log.w(ConfigAccess.class.getName(),"Error loading shop article config!",ioe);
            }
        }

        if (persistentObject != null)
            return persistentObject;
        else {
            return new HashMap<>();
        }
    }

    public static Map<Long,VoucherInfo> getVoucherGroups(Context context) {
        FileInputStream fis;
        ObjectInputStream is = null;
        Map<Long,VoucherInfo> persistentObject = null;
        try {
            fis = context.openFileInput(GROUP_CONFIG_FILE);
            is = new ObjectInputStream(fis);
            persistentObject = (Map<Long,VoucherInfo>) is.readObject();
            is.close();
        } catch (FileNotFoundException e) {
            Log.w(ConfigAccess.class.getName(), e.getMessage());
        } catch (ClassNotFoundException | IOException e) {
            Log.w(ConfigAccess.class.getName(), "No shop group configs available.");
        } finally {
            try{
                if (is != null)
                    is.close();
            } catch (IOException ioe){
                Log.w(ConfigAccess.class.getName(),"Error loading shop group config!",ioe);
            }
        }

        if (persistentObject != null)
            return persistentObject;
        else {
            return new HashMap<>();
        }
    }

    public static void storeDeviceID(Context context, String deviceID){
        if (! (deviceID != null && deviceID.length() == 0)){
            SharedPreferences settings = context.getSharedPreferences(BASE_CONFIG_FILE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(CONFIG_DEVICE_IP, deviceID);
            editor.apply();
        }
    }
    
    public static float getShoppingCartFontSize(Context context){
        try{
            SharedPreferences settings = getSettings(context);
            String value = settings.getString(ARTICLEFONTSIZE.toString(), CONFIG_ARTICLE_FONT_SIZE);
            Log.d(ConfigAccess.class.getName(), "Loading config " + ARTICLEFONTSIZE.toString() + " = " + value);
            return Float.parseFloat(value);
        } catch (NumberFormatException nfe){
            return Float.parseFloat(CONFIG_ARTICLE_FONT_SIZE);
        }
    }
    
    public static int getArticleColorForBuy(Context context){
        return getArticleColor(context, ARTICLECOLORDEFAULT.toString(), CONFIG_ARTICLE_COLOR_DEFAULT);
    }
    
    public static int getArticleColorForLoad(Context context){
        return getArticleColor(context, ARTICLECOLORLOAD.toString(), CONFIG_ARTICLE_COLOR_LOAD);
    }
    
    public static int getArticleColorForUnload(Context context){
        return getArticleColor(context, ARTICLECOLORUNDLOAD.toString(), CONFIG_ARTICLE_COLOR_UNLOAD);
    }
    
    public static int getArticleColorForCancelation(Context context){
        return getArticleColor(context, ARTICLECOLORCANCEL.toString(), CONFIG_ARTICLE_COLOR_CANCEL);
    }
    
    private static int getArticleColor(Context context, String configKey, String defaultValue){
        try{
            SharedPreferences settings = getSettings(context);
            String value = "#" + settings.getString(configKey, defaultValue);
            Log.d(ConfigAccess.class.getName(), "Loading config " + configKey + " = " + value);
            return Color.parseColor(value);
        } catch (NumberFormatException nfe){
            return Color.parseColor("#"+defaultValue);
        }
    }

    public static void reset(Context context){

        // Clear Article/Shop Config
        context.deleteFile(ARTICLE_CONFIG_FILE);

        // Clear Article Group Config
        context.deleteFile(GROUP_CONFIG_FILE);

        // Clear Blacklist Config
        context.deleteFile(BLACKLIST_FILE);

        // Clear Gate Config
        context.deleteFile(GATE_CONFIG_FILE);

        // clear shared preferences
        SharedPreferences settings = context.getSharedPreferences(BASE_CONFIG_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.commit();
    }

    public static Map<Long,String> getVisitorRoles(Context context){
        Iterable<AreaConfig> config = ConfigAccess.getAreaConfig(context);

        HashMap<Long,String> rolesMap = new HashMap<>();

        for(AreaConfig area:config){
            for (VisitorRole role:area.getRoles()){
                rolesMap.put(Long.valueOf(role.getRoleID()), role.getRoleName());
            }
        }

        return rolesMap;
    }


    public static int getSelectedTabForTicketSelection(Context context){
        try{
            SharedPreferences settings = getSettings(context);
            int value = settings.getInt(TICKET_XC_SELECT_TAB.toString(), 0);
            Log.d(ConfigAccess.class.getName(), "Loading config " + TICKET_XC_SELECT_TAB + " = " + value);
            return value;
        } catch (ClassCastException | NumberFormatException nfe){
            return 0;
        }
    }

    public static void storeSelectedTabForTicketSelection(Context context, int tabID){
        SharedPreferences settings = getSettings(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(TICKET_XC_SELECT_TAB.toString(), tabID);
        editor.commit();
    }

}
