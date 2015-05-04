package com.youchip.youmobile.controller.network;


import java.io.IOException;
import java.util.Map;

import com.youchip.youmobile.R;
import com.youchip.youmobile.controller.IntentExtrasKeys;
import com.youchip.youmobile.controller.network.serviceCaller.WebServiceCall;
import com.youchip.youmobile.controller.network.serviceInterface.SOAPResponse;
import com.youchip.youmobile.controller.settings.ConfigAccess;
import com.youchip.youmobile.controller.txlog.TxLogger;
import com.youchip.youmobile.model.network.BasicSOAPConfigFields;
import com.youchip.youmobile.model.network.request.BasicConfigSOAPRequest;
import com.youchip.youmobile.model.network.request.ConfigSOAPRequest;
import com.youchip.youmobile.model.network.request.GateConfigSOAPRequest;
import com.youchip.youmobile.model.network.request.ShopConfigSOAPRequest;
import com.youchip.youmobile.model.network.request.VoucherNamesSOAPRequest;
import com.youchip.youmobile.model.network.response.BasicConfigSOAPResponse;
import com.youchip.youmobile.model.network.response.GateConfigSOAPResponse;
import com.youchip.youmobile.model.network.response.ShopConfigSOAPResponse;
import com.youchip.youmobile.model.network.response.VoucherNamesSOAPResponse;
import com.youchip.youmobile.utils.AlertBox;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

public class ChainConfigServiceCaller extends AsyncTask<Void, Integer, Boolean> {
    

    private static final String LOG_TAG = ChainConfigServiceCaller.class.getName();
    private final Context context;
    private final TxLogger logger;
    private ProgressDialog progressDialog;

    private final WebServiceCall serviceCall;
    private String displayMessage;
    private String deviceID = "";
    private String chipUID  = "";
    private long   eventID  =  0;


    private interface ConfigUpdater{
        public void updateConfig(SOAPResponse response);
    }

       
    private final DialogInterface.OnClickListener onSubmitOKListener = new DialogInterface.OnClickListener(){

        @Override
        public void onClick(DialogInterface dialog, int which) {
            Log.d(ChainConfigServiceCaller.class.getName(), "Sending onsite/shop/gate config Update broadcast..");
            Intent intent = new Intent(IntentExtrasKeys.BASIC_CONFIG_UPDATED_INTENT);
            context.sendBroadcast(intent);
        }
        
    };
    
    
    public ChainConfigServiceCaller(Context context, TxLogger txLogger, String serviceURL){
        this.context = context;
        this.logger = txLogger;
        this.serviceCall = new WebServiceCall(serviceURL);
    }
    
    @Override
    protected void onPreExecute() {
        if ( context instanceof Activity) {
            Activity activity = (Activity) context;
            if (!activity.isFinishing()) {
                progressDialog = ProgressDialog.show(context, context.getString(R.string.hint_request_wait), context.getString(R.string.hint_request_wait), true, false);
            }
        }
    }


    @Override
    protected Boolean doInBackground(Void... arg0) {
        boolean result = true;

        publishProgress(1);

        result &= loadConfig(
                chipUID,
                new ConfigSOAPRequest(new BasicConfigSOAPRequest(), deviceID, eventID),
                new BasicConfigSOAPResponse(),
                BASIC_CONFIG_UPDATER,
                context.getString(R.string.success_load_onside_config),
                context.getString(R.string.error_load_onside_config)
        );

        publishProgress(26);

        result &= loadConfig(
                chipUID,
                new ConfigSOAPRequest(new GateConfigSOAPRequest(), deviceID, eventID),
                new GateConfigSOAPResponse(),
                GATE_CONFIG_UPDATER,
                context.getString(R.string.success_load_gate_config),
                context.getString(R.string.error_load_gate_config)
        );

        publishProgress(51);

        result &= loadConfig(
                chipUID,
                new ConfigSOAPRequest(new ShopConfigSOAPRequest(), deviceID, eventID),
                new ShopConfigSOAPResponse(),
                SHOP_CONFIG_UPDATER,
                context.getString(R.string.success_load_shop_config),
                context.getString(R.string.error_load_shop_config)
        );

        publishProgress(76);

        result &= loadConfig(
                chipUID,
                new ConfigSOAPRequest(new VoucherNamesSOAPRequest(), deviceID, eventID),
                new VoucherNamesSOAPResponse(),
                VOUCHER_NAMES_UPDATER,
                context.getString(R.string.success_load_voucher_names),
                context.getString(R.string.error_load_voucher_names)
        );

        publishProgress(100);

        return result;
    }


    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        
        String explanation = "";

        if (progress[0] < 26){
            explanation = context.getString(R.string.hint_loading_onsite_config);
        } else if (progress[0] < 51){
            explanation = context.getString(R.string.hint_loading_gate_config);
        } else if (progress[0] < 76){
            explanation = context.getString(R.string.hint_loading_shop_config);
        } else if (progress[0] < 100){
            explanation = context.getString(R.string.hint_loading_voucher_names);
        } else{ 
            explanation = context.getString(R.string.value_done);
        }
        
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.setMessage(explanation);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if ( context instanceof Activity && progressDialog != null && progressDialog.isShowing()) {
            Activity activity = (Activity) context;
            if (!activity.isFinishing()) {
                progressDialog.dismiss();
            }
        }
        
        if (result == null || !result) {
            AlertBox.allertOnWarning(context, R.string.failed_title, displayMessage);
        } else {
            AlertBox.allertOnInfo(context, R.string.success_title, displayMessage, onSubmitOKListener);
        }
        
        super.onPostExecute(result);
        
    }
    
    private void putDisplayMessage(String msg){
        if(displayMessage == null || displayMessage.length() <= 0){
            displayMessage = msg;
        } else {
            displayMessage += "\n\n" + msg;
        }
    }


    private boolean loadConfig(String chipUID, final ConfigSOAPRequest request, final SOAPResponse response, final ConfigUpdater updater, String successMessage, String errorMessage){
        Log.d(LOG_TAG, "Start loading config data from web service");
        boolean result;

        try{
            result = serviceCall.callService(request, response);
            updater.updateConfig(response);

            if (result){
                Log.d(LOG_TAG, successMessage);
                logger.configRead(chipUID, successMessage);
                putDisplayMessage(successMessage);
                return true;
            } else {
                Log.d(LOG_TAG, errorMessage);
                logger.configRead(chipUID, errorMessage);
                putDisplayMessage(errorMessage);
                return false;
            }

        } catch (IllegalArgumentException iae){
            Log.e(LOG_TAG, errorMessage, iae);
            putDisplayMessage(errorMessage + " (" + context.getString(R.string.error_reason_url) +")");
            return false;
        } catch (IOException e) {
            String errorMsg = e.getMessage();
            errorMsg = errorMsg != null ? "(" + errorMsg + ")" : "";
            Log.e(LOG_TAG, errorMsg);
            putDisplayMessage(errorMessage + errorMsg);
            return false;
        } catch (Exception e) {
            Log.e(LOG_TAG, errorMessage, e);
            String errorMsg = e.getMessage();
            errorMsg = errorMsg != null ? "(" + errorMsg + ")" : "";
            putDisplayMessage(errorMessage + errorMsg);
            return false;
        }
    }


    private ConfigUpdater SHOP_CONFIG_UPDATER = new ConfigUpdater(){
        @Override
        public void updateConfig(SOAPResponse response) {
                ConfigAccess.storeShopArticleConfig(context, ((ShopConfigSOAPResponse) response).getArticleConfig());
        }
    };

    private ConfigUpdater GATE_CONFIG_UPDATER = new ConfigUpdater(){
        @Override
        public void updateConfig(SOAPResponse response) {
                ConfigAccess.storeAreaConfig(context, ((GateConfigSOAPResponse) response).getResult());
        }
    };

    private ConfigUpdater VOUCHER_NAMES_UPDATER = new ConfigUpdater(){
        @Override
        public void updateConfig(SOAPResponse response) {
            ConfigAccess.storeVoucherGroups(context, ((VoucherNamesSOAPResponse) response).getResultMap());
        }
    };

    private ConfigUpdater BASIC_CONFIG_UPDATER = new ConfigUpdater(){
        @Override
        public void updateConfig(SOAPResponse response) {
                BasicConfigSOAPResponse basicResponse = (BasicConfigSOAPResponse) response;
                Log.d(LOG_TAG, "Saving basic config data");
                Map<String, String> resultMap = basicResponse.getResultMap();
                for (BasicSOAPConfigFields field:BasicSOAPConfigFields.values()){
                    String value = resultMap.get(field.toString());
                    if (value != null){
                        ConfigAccess.putRawKeyValuePair(context, field.toString(),value);
                    }
                }
        }
    };

    public String getDeviceID() {
        return deviceID;
    }

    public String getChipUID() {
        return chipUID;
    }

    public long getEventID() {
        return eventID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public void setChipUID(String chipUID) {
        this.chipUID = chipUID;
    }

    public void setEventID(long eventID) {
        this.eventID = eventID;
    }


    public void dismissDialog(){

        if (this.progressDialog != null){
            this.progressDialog.dismiss();
        }

    }
}
