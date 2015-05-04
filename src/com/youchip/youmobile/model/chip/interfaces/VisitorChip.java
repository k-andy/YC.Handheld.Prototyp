package com.youchip.youmobile.model.chip.interfaces;

import java.util.Date;
import java.util.Map;
import java.util.Set;

public interface VisitorChip extends BasicChip {
    

    /** Extracts the Credit 1 out of the raw byte data of the chip.
      * @return The 4 Byte long value, which represents
      * the Credit 1 in the smallest currency unit (fa. Cent for Euro)
      */
    public long getCredit1();


    /** Extracts the Credit 2 out of the raw byte data of the chip.
      * @return #number The 4 Byte long value, which represents
      * the Credit 2 in the smallest currency unit (fa. Cent for Euro)
      */
    public long getCredit2();


    /** Extracts all vouchers out of the raw byte data of the chip.
      * @return A list of Voucher
      */ 
    public Map<Long,Long> getVoucher();


    /** Extracts the blocked state out of the raw byte data of the chip.
      * @return #boolean true, if the the chip is blocked, so it 
      *          cant be used for other functiones than to unblock,
      *          otherwise false
      */
    public boolean isBlocked();


    /** Extracts the checked-in zone ID out of the raw byte data of the chip.
      * The area ID describes the location of the visitor
      * @return an ID corresponding to the area
      */
    public long getInAreaID();
    

    /** Extracts from chip the time (hour) when the area was entered
     * @return  corresponding to the area check in time
     */
    public Date getInAreaTime();


    /** Extracts the visitor role out of the raw byte data of the chip.
      * Visitor role is used to connect it with several rights
      * (fa access to the VIP area)
      * @return An ID corresponding to the role
      */
    public Set<Long> getVisitorRoles();


    /** Extracts the backoffice role out of the raw byte data of the chip.
      * Backoffice role is used to connect it with several rights
      * (fa to use the hendheld)
      * @return A list of 1 Byte long IDs corresponding to the role
      */
    public Set<Long> getBackofficeRoles();
  
    

    /** Extracts the history log out of the raw byte data of the chip.
      * The hitsory log is a protocoll of all transaction and payment activities
      * @return A list of the last visitor acitivities
      */
    public ChipHistory getHistoryLog();
    

    /** Modifies the raw data concerning the event ID.
      * @param eventID, The 3 Byte long ID which is uniqe for 65534 events
      */
    public void setEventID(long eventID);



    /** Modifies the raw data concerning the Credit 1.
      * @param credit, The 3 Byte long value, which represents
      * the Credit 1 in the smallest currency unit (fa. Cent for Euro)
      */
    public void setCredit1(long credit1);


    /** Modifies the raw data concerning the Credit 2.
      * @param credit, The 3 Byte long value, which represents
      * the Credit 2 in the smallest currency unit (fa. Cent for Euro)
      */
    public void setCredit2(long credit2);


    /** Modifies the raw data concerning the vouchers.
      * @param vouchers A list of voucher
      */ 
    public void setVoucher(Map<Long,Long> voucher);


    /** Modifies the raw data concerning the blocked.
      * @param blocked set to true to disable 
      *                  other functioalities than to unblock,
      *                 true reactivates all functioalities
      */
    public void setBlocked(boolean blocked);

    
    
    /**  Modifies the raw data concerning the area ID.
      * The area ID describes the location of the visitor
      * @return #number areaID a 1 Byte long ID corresponding to the area
      */
    public void setInAreaID(long areaID);
    
    
    /** 
     * Modifies the raw data concerning the area check in time
     */
    public void setInAreaTime(Date time);



    /**  Modifies the raw data concerning the visitor role.
      * Visitor role is used to connect it with several rights
      * (fa access to the VIP area)
      * @param role, An ID corresponding to the role
      */
    public void setVisitorRoles(Set<Long> roles);


    /** Modifies the raw data concerning the backoffice role.
      * Backoffice role is used to connect it with several rights
      * (fa to use the hendheld)
      * @param roles, An IDs corresponding to the role
      */
    public void setBackofficeRoles(Set<Long> roles);


    /** Modifies the the raw data concerning the history log.
      * The hitsory log is a protocoll of all transaction and payment activities.
      * @param chipHistory, a list of the last visitor acitivities
      */
    public void setHistoryLog(ChipHistory chipHistory);

}
