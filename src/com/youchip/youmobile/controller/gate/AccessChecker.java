package com.youchip.youmobile.controller.gate;

import android.util.Log;

import com.youchip.youmobile.controller.txlog.AccessResult;
import com.youchip.youmobile.controller.txlog.TxGateLogger.AccessState;
import com.youchip.youmobile.model.chip.interfaces.BasicChip;
import com.youchip.youmobile.model.chip.interfaces.VisitorChip;
import com.youchip.youmobile.model.chip.mc1kImpl.MC1KChipSpecs.AppType;
import com.youchip.youmobile.model.gate.AreaConfig;
import com.youchip.youmobile.model.gate.BlockedChip;
import com.youchip.youmobile.model.gate.VisitorRole;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.youchip.youmobile.controller.txlog.TxGateLogger.AccessState.BLOCKED;
import static com.youchip.youmobile.controller.txlog.TxGateLogger.AccessState.PASSED;

public class AccessChecker {
    
   
    private static final String CURRENT_CLASS = AccessChecker.class.getName();
    
 public String failMessage(AccessResult ... results){   
    // generate result
    String accessMessage = "";

    for (AccessResult result : results) {
        if (result.getAccessState() == AccessState.BLOCKED || result.getAccessState() == AccessState.BANNED) {
            accessMessage += result.getAccessMessage() + ". ";
        }
    }
    
    return accessMessage;
 }
 
 
 public boolean validateResult(AccessResult ... results){   
     // validate result
     boolean accessResult = true;

     for (AccessResult result : results) {
         if (result.getAccessState() == AccessState.BLOCKED || result.getAccessState() == AccessState.BANNED) {
             Log.e("TEST", "access = " + result.getAccessMessage());
             accessResult = false;
             Log.w(CURRENT_CLASS, result.getAccessMessage());
         }
     }
     
     return accessResult;
  }
    
    /**
     * Checks the chip crc 
     * 
     * @param chip the Chip to check
     * @param CRC_BLOCKS the blocks which needs to be checked
     * @return AccessResult which contains one of four states and a fail message in case of a access denie
     */
    public AccessResult checkCRC(BasicChip chip, final Set<Integer> CRC_BLOCKS){
        
        boolean isCRCValid = chip.isValid(CRC_BLOCKS);

        if (isCRCValid) 
            return new AccessResult(PASSED);
        else 
            return new AccessResult(BLOCKED, "CRC Error! Chip is currpted.");
    }
    
    /**
     * Checks if the ChipReadService delivers a chip from a valid event
     *
     *            The active configuration
     * @return True if the chip is from the right event
     */
    public AccessResult checkEventID(long chipEventID, long eventID) {
        if (chipEventID == eventID) {
            return new AccessResult(PASSED);
        } else {
            return new AccessResult(BLOCKED, "Invalid event ID " + chipEventID
                    + " does not match " + eventID);
        }
    }
    

    
    public AccessResult checkAppType(BasicChip chip, AppType appType){
        AppType chipAppType = chip.getAppType();
        
            if (chipAppType == appType){
                return new AccessResult(PASSED);
            }
        
        return new AccessResult(BLOCKED, "No valid chip status: " + chipAppType.toString());
    }

    public AccessResult checkBoRoleAdmin(BasicChip chip){
        if (chip.isAdmin()){
            return new AccessResult(PASSED);
        }
        return new AccessResult(BLOCKED, "No valid chip status! Action requires 'admin' rights. ");
    }

    public AccessResult checkBoRoleSupervisor(BasicChip chip){
        if (chip.isSupervisor()){
            return new AccessResult(PASSED);
        }
        return new AccessResult(BLOCKED, "No valid chip status! Action requires 'supervisor' rights. ");
    }

    public AccessResult checkBoRoleEmployee(BasicChip chip){
        if (chip.isEmployee()){
            return new AccessResult(PASSED);
        }
        return new AccessResult(BLOCKED, "No valid chip status! Action requires 'employee' rights. ");
    }


    public AccessResult checkBoRoleVisitor(BasicChip chip){
        if (chip.isVisitor()){
            return new AccessResult(PASSED);
        }
        return new AccessResult(BLOCKED, "No valid chip status! Action requires 'visitor' rights. ");
    }
    
    /**
     * Checks if the chip has the state blocked
     * @param chip
     * @return
     */
    public AccessResult checkChipBlockState(VisitorChip chip) {
        if (chip.isBlocked()) {
            return new AccessResult(AccessState.BLOCKED, "Chip is blocked!");
        } else {
            return new AccessResult(AccessState.PASSED);
        }
    }
    
    /**
     * Checks if the ChipReadService delivers a chip with at least one valid
     * role
     * 
     * @param chip
     *            The current used chip
     * @return True if the chip has a valid role
     */
    public Set<Long> getValidVisitorRoles(VisitorChip chip, AreaConfig settings) {
        Set<Long> validRoles = new HashSet<>();

        for (VisitorRole role:settings.getRoles()){
            for (Long chipRole : chip.getVisitorRoles()) {
                if (chipRole == role.getRoleID()) {
                    validRoles.add(chipRole);
//                    logger.debug("Role on Chip ({}) matches role for Gate ({}).", chipRole, role.getRoleID());
//                    
//                } else {
//                    logger.debug("Role on Chip ({}) does not match role for Gate ({}).", chipRole, role.getRoleID() );
                }
            }
        }
        
        return validRoles;
    }
    
    /**
     * Check if the area is a Zone and if the chip is allowed to enter/leave it
     * 
     * @param chip
     * @param config
     * @return
     */
    public AccessResult checkZoneAccess(VisitorChip chip, AreaConfig config, int zoneCheckInDelay) {
        if (!config.isZone()){
            return new AccessResult(AccessState.PASSED);
        }
        
        AccessResult delayResult = checkZoneTimeOutDelay(chip, zoneCheckInDelay);
        
        if (config.isCheckIn() && ((chip.getInAreaID() == 0) || delayResult.getAccessState() != BLOCKED)){
            return new AccessResult(AccessState.CHECKED_IN);
        } else if (!config.isCheckIn()) {
            return new AccessResult(AccessState.CHECKED_OUT);
        } else {
            String delayMessage = delayResult.getAccessState() == BLOCKED ? "(" + delayResult.getAccessMessage() +")": "";
            return new AccessResult(AccessState.BLOCKED, "Already checked-in " + delayMessage );
        }
    }
    
    /**
     * checks if last check in was a long time ago
     * @param chip
     * @return
     */
    public AccessResult checkZoneTimeOutDelay(VisitorChip chip, int delay){
        
        Calendar plusTolerance = Calendar.getInstance();
        plusTolerance.add(Calendar.MINUTE, delay);
        Calendar minusTolerance = Calendar.getInstance();
        minusTolerance.add(Calendar.MINUTE, -delay);
        
        Calendar lastCheckIn = Calendar.getInstance();
        lastCheckIn.setTime(chip.getInAreaTime());
        
        int lci = lastCheckIn.get(Calendar.HOUR_OF_DAY)*60 + lastCheckIn.get(Calendar.MINUTE);
        int pt  = plusTolerance.get(Calendar.HOUR_OF_DAY)*60 + plusTolerance.get(Calendar.MINUTE);
        int mt  = minusTolerance.get(Calendar.HOUR_OF_DAY)*60 + minusTolerance.get(Calendar.MINUTE);
        
        Log.d(CURRENT_CLASS, "Minus tolerance: " + mt + " m , Last check-in: " + lci + " m , plus tolerance: " + pt);
        
        if (lci > pt || lci < mt){
            return new AccessResult(AccessState.PASSED, "Matching zone check in delay");
        } else {
            return new AccessResult(AccessState.BLOCKED, "Last Check-in less than " + delay + " Minutes ago");
        }
    }
    
    /**
     * checks if there is one of the valid roles in the right time
     * @param validRoles
     * @param settings
     * @return
     */
    public AccessResult checkValidTime(Set<Long> validRoles, AreaConfig settings) {
        boolean isValidTime = false;
        String timeInvalidMessage = "";
        Date systemTime = Calendar.getInstance().getTime();

        outer: 
        for (Long role : validRoles) {
            for (VisitorRole config : settings.getRoles()) {
                if (role != config.getRoleID()) {
                    continue;
                } else {
                    Date roleStart = config.getValidTimeStart();
                    Date roleStop = config.getValidTimeStop();

                    if (roleStart.before(systemTime) && roleStop.after(systemTime)) {
                        isValidTime = true;
                        timeInvalidMessage = "Valid Role period";
                        break outer;
                    } else if (systemTime.before(roleStart)) {
                        timeInvalidMessage = "Invalid Role period. Event starts in the future (in "
                                + roleStart + ")!";
                    } else if (systemTime.after(roleStop)) {
                        timeInvalidMessage = " Invalid Role period. Event already ended (at "
                                + roleStop + ")!";
                    } else {
                        timeInvalidMessage = "Invalid Role period!";
                    }
                }
            }
        }

        
        if (isValidTime) {
            return new AccessResult(PASSED);
        } else {
            return new AccessResult(BLOCKED, timeInvalidMessage);
        }
    }
    
    /**
     * Checking the blacklist for the occurence of the chip which requests access.
     * @param blackList List of chips which are not allowed to pass
     * @return An access result with the access state and the reason as a string.
     */
    public AccessResult checkBlackList(String chipUID, List<BlockedChip> blackList){
        
        Date systemTime = Calendar.getInstance().getTime();
        
        for (BlockedChip blocked: blackList){
            if (chipUID.equals(blocked.getUID())){
                if(blocked.isBanned()){
                    Log.w(CURRENT_CLASS, "Chip is banned!");
                    return new AccessResult(AccessState.BANNED, "Chip (UID:"+chipUID+") is banned!");
                } else if(blocked.getBlockedUntil().after(systemTime)){
                    Log.w(CURRENT_CLASS, "Chip is still blacklisted ("+blocked.getBlockedUntil().after(systemTime)+")");
                    return new AccessResult(AccessState.BLOCKED, "Chip (UID:"+chipUID+") is on block list until " + blocked.getBlockedUntil());
                }
            }
        }
        
        Log.d(CURRENT_CLASS, "Chip (UID: "+chipUID+") is not on block list.");
        return new AccessResult(AccessState.PASSED);
    }
    
    public AccessResult checkBannedList(String chipUID, List<BlockedChip> blackList){
        
        for (BlockedChip blocked: blackList){
            if (chipUID.equals(blocked.getUID())){
                if(blocked.isBanned()){
                    Log.w(CURRENT_CLASS, "Chip is banned!");
                    return new AccessResult(AccessState.BANNED, "Chip (UID:"+chipUID+") is banned!");
                }
            }
        }
        
        Log.d(CURRENT_CLASS, "Chip (UID: "+chipUID+") is not banned.");
        return new AccessResult(AccessState.PASSED);
    }
}
