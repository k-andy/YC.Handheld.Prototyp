package com.youchip.youmobile.view;

import com.youchip.youmobile.R;
import com.youchip.youmobile.utils.AlertBox;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

public abstract class NotificationBackGroundTask extends AsyncTask<Void, Void, Boolean> {
    
    private Context context;
    private ProgressDialog progressDialog;
    private String errorMessage="Failed";
    private String errorTitle="Failed!";
    private String successMessage="Success";
    private String waitMessage="Pleas wait while loading.";
    
    public NotificationBackGroundTask(Context context){
        this.context = context;
        waitMessage =  context.getString(R.string.hint_request_wait);
        successMessage = context.getString(R.string.success_title);
        errorTitle = context.getString(R.string.failed_title);
    }
    
    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(waitMessage);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.show();
    }

    
    @Override
    abstract protected Boolean doInBackground(Void... params);
    
    @Override
    protected void onPostExecute(Boolean result) {
        if (progressDialog!=null) {
            progressDialog.dismiss();
        }
        
        if (result){
            onSuccess();
        } else {
            onError();
        }
    }
    
    protected void onSuccess(){
        Toast.makeText(context.getApplicationContext(), successMessage, Toast.LENGTH_SHORT).show();
    }
    
    protected void onError(){
        AlertBox.allertOnWarning(context, R.string.failed_title, this.errorMessage);
    }
    
    public void setErrorMessage(int titleResourceID){
        this.errorMessage = context.getString(titleResourceID);
    }
    
    public void setErrorMessage(String message){
        this.errorMessage = message;
    }
    
    public void setErrorTitle(String title){
        this.errorTitle = title;
    }
    
    public void setErrorTitle(int titleResourceID){
        this.errorTitle = context.getString(titleResourceID);
    }
    
    public void setSuccessMessage(String message){
        this.successMessage = message;
    }
}
