package com.youchip.youmobile.utils;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import android.annotation.SuppressLint;

import com.youchip.youmobile.model.chip.interfaces.ChipField;

@SuppressLint("SimpleDateFormat")
public class DataConverter {

    public static final String SERVICE_DATE_FORMAT_STRING = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String NOTIFY_DATE_FORMAT_STRING = "HH:mm";

    
    public static String byteArrayToSpacedHexString(byte[] byteArray, int size, String spaceSymbol, String spaceSymbol2) {
        int spaceCount = 0;
        if (byteArray == null || byteArray.length < 1)
            throw new IllegalArgumentException(
                    "this byteArray must not be null or empty");
        final StringBuilder hexString = new StringBuilder(2 * size);
        for (int i = 0; i < size; i++) {
            if ((byteArray[i] & 0xff) < 0x10)
                hexString.append("0");
            hexString.append(Integer.toHexString(0xFF & byteArray[i]));
            if ((++spaceCount)%4 == 0)
                    hexString.append(spaceSymbol);
            if (i != (byteArray.length - 1))
                hexString.append(spaceSymbol2);
            if (((++spaceCount)%4 == 0) && (i+1 < size))
                hexString.append(spaceSymbol);
        }
        return hexString.toString().toUpperCase(Locale.ENGLISH);
    }
    
    public static String byteArrayToSpacedHexString(byte[] byteArray, String spaceSymbol, String spaceSymbol2){
        return byteArrayToSpacedHexString(byteArray, byteArray.length, spaceSymbol, spaceSymbol2);
    }
    
    public static String byteArrayToSpacedHexString(byte[] byteArray, String spaceSymbol){
        return byteArrayToSpacedHexString(byteArray, byteArray.length, spaceSymbol, "");
    }    

    
	public static String byteArrayToHexString(byte[] byteArray, int size) {
	    return byteArrayToSpacedHexString(byteArray, size, "", "");
	}
	
	public static String byteArrayToWhiteSpaceHexString(byte[] byteArray){
	    return byteArrayToSpacedHexString(byteArray, " ");
	}
	
    
    public static String byteArrayToHexString(byte[] byteArray){
        return byteArrayToHexString(byteArray, byteArray.length);
    }

    
    public static byte[] hexStringToByteArray(String hexString) {
        String s = hexString.replaceAll("\\s","");
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
	
    /**
     * converts an integer into a byte array
     * @param intValue may be an integer which can be converted to an array of bytes with the same size
     * @param size may be 1 to 4 and will be cut to this borders
     * @return the byte array representation of the integer
     */
    public static byte[] intToByteArray(long intValue, int size)
    {
        byte[] data = new byte[size];
        
        for (int i = 0; i < size; ++i) {
            int shift = i << 3; // i * 8
            data[size-1-i] = (byte)((intValue & (0x000000ff << shift)) >>> shift);
        }
        
        return data;
    }
    
    public static byte[] intToByteArray(long intValue){
        return intToByteArray(intValue, 4);
    }
	
	
    /**
     * converts a byte array into an integer value
     * @param byteArray must be an array of size 4
     * @return the integer representation of the byte array
     */
    public static long byteArrayToInt(byte[] byteArray) 
    {
        int size = Math.min(byteArray.length, 4);
        
        long number = 0;     
        for (int i = 0; i < size; ++i) {
            number |= (((long) byteArray[size-1-i]) & 0x000000ffL) << (i << 3);
        }
        return number;
    }
    
    public static String byteArrayToString(byte[] byteArray) throws UnsupportedEncodingException{
        for (int i=0; i < byteArray.length; i++){
            if (byteArray[i] == 0) byteArray[i] = ' ';
        }
            
        return (new String(byteArray, "UTF-8")).trim();
    }
    
    
    public static byte[] StringToByteArray(String text) throws UnsupportedEncodingException{
        return text.trim().getBytes("UTF-8");
    }
    
    /**
     * calculate a list of all needed blocks
     * @param fields which need blocks
     * @return list of needed blocks
     * TODO duplicated till using jdk8 so interfaces support static methods
     */
    public static Set<Integer> getRelevantBlocks(Iterable<ChipField> fields){
        Set<Integer> blocksToRead = new HashSet<>();
        for(ChipField field: fields){
            //start blocks
            int blockPos1 = field.getBlock1Pos();
            int blockPos2 = field.getBlock2Pos();
            
            blocksToRead.add(blockPos1);
            if (field.isTxnSave()){
                blocksToRead.add(blockPos2);
            }
            
            //blocks from oversize
            int additionaBlocks;
            int totalFieldSize = field.getTotalSize();
            
            additionaBlocks = (field.getBytePos() + totalFieldSize) / field.getNettoBlockSize();
                
            for (int i=1; i <= additionaBlocks; i++){
                blocksToRead.add(blockPos1+i);
                if (field.isTxnSave()){
                    blocksToRead.add(blockPos2+i);
                }
            }
        }
        return blocksToRead;
    }

    
    public static int uByteToInt(byte b) {
        return (int) b & 0xFF;
    }
	
    
    /**
     * Returns a converted calendar object if the format fits, otherwise
     * the day zero
     * @param dotNetDate Date as String
     * @return Date as Calendar
     */
    public static Date serviceFormatToJavaDate(String dotNetDate, String formatString){
        SimpleDateFormat simpleFormat = new SimpleDateFormat(formatString);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(0);
        try{
            calendar.setTime(simpleFormat.parse(dotNetDate));
        } catch (ParseException ex) {
            //do nothing, return day zero
        }
        return calendar.getTime();
    }
    
    public static String javaDateToServiceFormat(Date date, String formatString){
        SimpleDateFormat simpleFormat = new SimpleDateFormat(formatString);
        return simpleFormat.format(date);
    }
    
    public static String getServiceDateFormatString(){
        return SERVICE_DATE_FORMAT_STRING;
    }
    
    public static Long getKeyByValue(Map<Long,String> map, String value){
        for(Long key:map.keySet()){
            if (map.get(key).equals(value)){
                return key;
            }
        }
        
        //if not avilable 
        return null;
    }
    
    public static BigDecimal longToCurrency(long value){
        return new BigDecimal(value).movePointLeft(2);
    }
    
    

}
