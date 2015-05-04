package com.youchip.youmobile.utils;

import java.io.IOException;

import android.os.Environment;
import android.os.StatFs;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SystemInfo {
    
    private static void evaluateExternalStorageState() throws IOException {
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;
        String state = Environment.getExternalStorageState(); 
        
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can scan and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only scan the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Something else is wrong. It may be one of many other states, but
            // all we need to know is we can neither scan nor write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }
        
        if (!mExternalStorageAvailable || !mExternalStorageWriteable) {
            throw new IOException("Can't write to file");
        }
    }

    
    /**
     * Calculates the amount of free external memory
     * @return the amount of free external memory in MByte
     */
    public static long getFreeExternalMemory(){
        try {
            evaluateExternalStorageState();
            StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
            long bytesAvailable = (long)stat.getBlockSize() *(long)stat.getAvailableBlocks();
            long megAvailable = bytesAvailable / 1048576;
            return megAvailable;
        } catch (IOException ioe) {
            return 0;
        }
    }
    
    /**
     * returns a list  of log files
     * @param parentDir
     * @return
     */
    public static List<File> getLogFiles(File parentDir) {
        ArrayList<File> inFiles = new ArrayList<File>();
        
        if (parentDir != null){
            File[] files = parentDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        inFiles.addAll(getLogFiles(file));
                    } else {
                        if (file.getName().startsWith("YC_") && file.getName().endsWith(".log")) {
                            inFiles.add(file);
                        }
                    }
                }
            }
        }
        return inFiles;
    }
    
    /**
     * returns the oldest logfile of the private app dir
     * @param appRootDir
     * @return
     */
    public static File getOldestLogFile(File appRootDir){
        //get file list
        List<File> logFileList = SystemInfo.getLogFiles(appRootDir);
        
        if (logFileList.size() > 0){
        
            //sort list
            File[] sorted = logFileList.toArray(new File[0]);
            // sort list
            Arrays.sort(sorted);
            File oldestFile = sorted[0];
            return oldestFile;
        } else {
            return null;
        }
    }

    

}
