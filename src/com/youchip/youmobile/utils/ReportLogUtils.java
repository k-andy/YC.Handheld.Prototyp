package com.youchip.youmobile.utils;

import android.content.Context;
import android.util.Log;

import com.youchip.youmobile.controller.report.ReportActivity;
import com.youchip.youmobile.controller.settings.ConfigAccess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

public class ReportLogUtils {
    private static final String LOGGER_TAG = ReportActivity.class.getName();
    private static final String reportFile = ConfigAccess.getReportLogMainFile();
    private static File file;

    public static void clearFile(Context context) {
        getFile(context);
        List<String> stringList = new LinkedList<>();

        PrintWriter writer = null;
        try {
            writer = new PrintWriter(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        writer.print("");
        writer.close();

        stringList.add("ts" + 0);
        stringList.add("tswt" + 0);
        stringList.add("tsn" + 0);
        stringList.add("gs" + 0);
        stringList.add("taxexcl" + 0);
        stringList.add("vat" + 0);
        stringList.add("taxincl" + 0);
        stringList.add("wtax" + 0);

        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"));
            for (String s : stringList) {
                out.append(s);
                out.newLine();
            }
            out.flush();
        } catch (IOException ioeExt) {
            Log.d(LOGGER_TAG, "Error writing Log on external storage. Keeping Log in cache.", ioeExt);
        } finally {
            try {
                if (out != null)
                    out.close();
            } catch (IOException ioe) {
                Log.w(LOGGER_TAG, "Error closing log file!", ioe);
            }
        }
    }

    public static void eraseFile(Context context) {
        getFile(context);
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        writer.print("");
        writer.close();
    }

    public static File getFile(Context context){
        if (file == null) {
            file = new File(context.getExternalFilesDir(null), reportFile);
        }
        return file;
    }

    public static String[] readFile(Context context) throws IOException {
        getFile(context);
        BufferedReader br = new BufferedReader(new FileReader(file.getAbsolutePath()));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            return sb.toString().split("\n");
        } finally {
            br.close();
        }
    }
}