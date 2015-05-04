package com.youchip.youmobile.model.shop;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by muelleco on 01.07.2014.
 */
public class VoucherInfo implements Serializable{


    private static final long serialVersionUID = -497968061323324808L;

    private final String title;
    private final long id;
    private final Date validFrom;
    private final Date validTo;


    public static final int[] CHECK_VALS = new int[]{Calendar.YEAR, Calendar.DAY_OF_YEAR, Calendar.HOUR_OF_DAY, Calendar.MINUTE};
    public static final String DATE_FORMAT = "dd.MM.'`'yy";


    /**
     * Auto corrects validity if the "validTo" time is before the "valid from" time.
     * @param id
     * @param title
     * @param validFrom
     * @param validTo
     */
    public VoucherInfo(long id, String title, Date validFrom, Date validTo){
        this.id = id;
        this.title = title;

        if (validFrom.before(validTo)) {
            this.validFrom = validFrom;
            this.validTo = validTo;
        } else {
            this.validFrom = validTo;
            this.validTo = validFrom;
        }
    }

    public String getTitle() {
        return title;
    }

//    public void setTitle(String title) {
//        this.title = title;
//    }

    public long getId() {
        return id;
    }

//    public void setId(long id) {
//        this.id = id;
//    }

    public Date getValidFrom() {
        return validFrom;
    }

//    public void setValidFrom(Date validFrom) {
//        this.validFrom = validFrom;
//    }

    public Date getValidTo() {
        return validTo;
    }

//    public void setValidTo(Date validTo) {
//        this.validTo = validTo;
//    }


    /**
     * Shows the current time validity of the voucher.
     * Should be used after isFuture() and before isOld() to make
     * sure, you have no runtime miss-checks
     * @return returns true if the current time is in between (inclusive)
     * the validFrom and validTo time span.
     */
    public boolean isValid(){

        Date now = Calendar.getInstance().getTime();

        return !now.before(getValidFrom()) && !now.after(getValidTo());
    }


    /**
     * Shows the current time validity of the voucher.
     * Should be used after isValid() and isFuture() to make
     * sure, you have no runtime miss-checks
     * @return returns true if the current time is later
     * (exclusive) the validTo time.
     */
    public boolean isOld(){

        Date now = Calendar.getInstance().getTime();

        return now.after(getValidTo());
    }

    /**
     * Shows the current time validity of the voucher.
     * Should be used before isValid() and isOld() to make
     * sure, you have no runtime miss-checks
     * @return returns true if the current time is before
     * (exclusive) the validFrom time.
     */
    public boolean isFuture(){
        Date now = Calendar.getInstance().getTime();

        return now.before(getValidFrom());
    }

    /**
     * Checks if the voucher is still valid for years, days, hours, minutes or seconds
     * @return Calendar.YEAR, Calendar.DAY_OF_YEAR, Calendar.HOUR_OF_DAY,
     *          Calendar.MINUTE or Calendar.SECOND
     * @throws IllegalStateException if isValid() is false
     */
    private int validityCountdown() throws IllegalStateException{
        if (!isValid()) {
            throw new IllegalStateException("Voucher is not valid!");
        }

        Calendar until = Calendar.getInstance();
        until.setTime(getValidTo());

        return validitySpan(until);
    }

    /**
     * Checks if the voucher will be valid in years, days, hours, minutes or seconds
     * @return Calendar.YEAR, Calendar.DAY_OF_YEAR, Calendar.HOUR_OF_DAY,
     *          Calendar.MINUTE or Calendar.SECOND
     * @throws IllegalStateException if isFuture() is false
     */
    private int availabilityCountdown(){
        if (!isFuture()) {
            throw new IllegalStateException("Voucher validity does not start in the future!");
        }

        Calendar from = Calendar.getInstance();
        from.setTime(getValidFrom());

        return validitySpan(from);
    }

    /**
     * Checks if the voucher was invalid since years, days, hours, minutes or seconds
     * @return Calendar.YEAR, Calendar.DAY_OF_YEAR, Calendar.HOUR_OF_DAY,
     *          Calendar.MINUTE or Calendar.SECOND
     * @throws IllegalStateException if isOld() is false
     */
    private int invalidityCountdown(){
        if (!isOld()) {
            throw new IllegalStateException("Voucher is not yet invalid!");
        }

        Calendar from = Calendar.getInstance();
        from.setTime(getValidTo());

        return validitySpan(from);
    }

    private int validitySpan(Calendar diff){
        Calendar now = Calendar.getInstance();

        for(int val: CHECK_VALS){
            if (now.get(val) != diff.get(val)) {
                return val;
            }
        }

        return Calendar.SECOND;
    }

    public String getValidityInfoAsString(){
        return getValidityInfoAsString(Calendar.getInstance());
    }

    private String getValidityInfoAsString(Calendar now){

        try {
            if (isFuture()) {
                return getValidityInfoString(now, this.validFrom, availabilityCountdown(), " (valid from {})", " (valid in {})");
            } else if (isValid()) {
                return getValidityInfoString(now, this.validTo, validityCountdown(), " (valid until {})", " (valid for {})");
            } else if (isOld()) {
                return getValidityInfoString(now, this.validTo, invalidityCountdown(), " (invalid since {})", " (invalid since {})");
            } else {
                return "";
            }
        } catch(IllegalStateException ise) {
            return "";
        }

    }


    private String getValidityInfoString(Calendar now, Date validCompare, int validScope, String validFullText, String validLeftText){
        Calendar comp = Calendar.getInstance();
        comp.setTime(validCompare);

        switch(validScope){
            case Calendar.YEAR:
                return validFullText.replace("{}", (new SimpleDateFormat(DATE_FORMAT)).format(this.validFrom));
            case Calendar.DAY_OF_YEAR:
                int daysDiff = Math.abs(comp.get(Calendar.DAY_OF_YEAR) - now.get(Calendar.DAY_OF_YEAR));
                if (daysDiff > 2 ){
                    return validFullText.replace("{}", (new SimpleDateFormat(DATE_FORMAT)).format(this.validFrom));
                } else {
                    return  validLeftText.replace("{}", String.valueOf(daysDiff)) + " days";
                }
            case Calendar.HOUR_OF_DAY:
                int hoursDiff = Math.abs(comp.get(Calendar.HOUR_OF_DAY) - now.get(Calendar.HOUR_OF_DAY));
                return  validLeftText.replace("{}", String.valueOf(hoursDiff)) + " hours";
            case Calendar.MINUTE:
                int minutesDiff = Math.abs(comp.get(Calendar.MINUTE) - now.get(Calendar.MINUTE));
                return  validLeftText.replace("{}", String.valueOf(minutesDiff)) + " minutes";
            default:
                return "";
        }
    }
}
