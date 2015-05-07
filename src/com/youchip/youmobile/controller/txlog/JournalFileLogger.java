package com.youchip.youmobile.controller.txlog;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.youchip.youmobile.utils.ReportLogUtils;
import com.youchip.youmobile.utils.SystemInfo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class JournalFileLogger {

    private String logFile;
    private Context context;
    private static final String LOGGER_TAG = JournalFileLogger.class.getName();

    public JournalFileLogger(Context context, String logFile) {
        this.context = context;
        this.logFile = logFile;
    }


    public File writeExternalLog(String message) {
        BufferedWriter out = null;
        File file = null;

        if (SystemInfo.getFreeExternalMemory() > 0) {
            try {
                file = new File(context.getExternalFilesDir(null), logFile);
                out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"));
                out.append(message);
                out.newLine();
                out.flush();
            } catch (IOException ioeExt) {
                Log.e(LOGGER_TAG, "Error writing Log on external storage. Keeping Log in cach.", ioeExt);
            } finally {
                try {
                    if (out != null)
                        out.close();
                } catch (IOException ioe) {
                    Log.w(LOGGER_TAG, "Error closing log file!", ioe);
                }
            }
        } else {
            Log.w(LOGGER_TAG, "Not enough disk space for writing Transaction Log");
        }

        return file;
    }

    protected void writeExternalReportLog(Activity shopMainActivity) throws IOException {
        ReportLogUtils.saveReport(shopMainActivity);
    }
}
