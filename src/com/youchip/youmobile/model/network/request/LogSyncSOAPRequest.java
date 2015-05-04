package com.youchip.youmobile.model.network.request;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;


import org.ksoap2.serialization.PropertyInfo;
import com.youchip.youmobile.controller.network.serviceInterface.SOAPRequest;

public class LogSyncSOAPRequest implements SOAPRequest{
    /** method */
    private final String NAMESPACE = "http://tempuri.org/";
    private final String METHOD_NAME = "LoggingSync";
    private final String ACTION_PACKAGE = "IHandHeld";
    
    /** method parameter */
    private String fileName = "";
    private String fileContent = "";
    private String filehash = "?";

    public LogSyncSOAPRequest(String fileName, String fileContent){
        this.fileName = fileName;
        this.fileContent = fileContent;
    }
    
    public LogSyncSOAPRequest(File file){
        this.fileName = file.getName();
        this.fileContent = loadFile(file);
    }
    
    
    @Override
    public final String getNameSpace(){
        return NAMESPACE;
    }
    
    @Override
    public final String getMethodName(){
        return METHOD_NAME;
    }
    
    @Override
    public final String getAction() {
        return getNameSpace() + ACTION_PACKAGE + "/" + getMethodName();
    }
    
    @Override
    public Object getProperty(int arg0) {
        switch(arg0)
        {
        case 0:
            return fileName;
        case 1:
            return fileContent;
        case 2:
            return filehash;
        }
        
        return null;
    }

    @Override
    public int getPropertyCount() {
        return 3;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void getPropertyInfo(int index, Hashtable arg1, PropertyInfo info) {
        switch(index){
        case 0:
            info.type = PropertyInfo.STRING_CLASS;
            info.name = "Filename";
            break;
        case 1:
            info.type = PropertyInfo.STRING_CLASS;
            info.name = "FileContent";
            break;
        case 2:
            info.type = PropertyInfo.STRING_CLASS;
            info.name = "FileContentHashKey";
            break;
        default:break;
        }
    }

    @Override
    public void setProperty(int arg0, Object arg1) {
        throw new UnsupportedOperationException();
    }

    public String getFilename() {
        return fileName;
    }

    public void setFilename(String Filename) {
        fileName = Filename;
    }

    public String getFileContent() {
        return fileContent;
    }

    public void setFileContent(String FileContent) {
        fileContent = FileContent;
    }


    
    private String loadFile(File file){
        if(file.exists())   // check if file exist
        {
              //Read text from file
            StringBuilder text = new StringBuilder();
            BufferedReader br=null;

            try {
                br = new BufferedReader(new FileReader(file));
                String line;

                while ((line = br.readLine()) != null) {
                    text.append(line);
                    text.append("\n");
                }
                return text.toString();
            }
            catch (IOException e) {
                //You'll need to add proper error handling here
                return "";
            } finally {
                    try {
                        if (br != null)
                        br.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
            }
        } else {
            return "";
        }
    }


}
