package com.youchip.youmobile.model.gate;

import java.io.Serializable;
import java.util.Date;

public class VisitorRole implements Serializable{

    private static final long serialVersionUID = 8316301914573755621L;
    
    private String roleName;
    private int roleID;
    private Date validTimeStart;
    private Date validTimeStop;
    
    public VisitorRole(){
    }
    
    public VisitorRole(VisitorRole visitorRole){
        this.roleName = visitorRole.roleName;
        this.roleID = visitorRole.roleID;
        this.validTimeStart = visitorRole.validTimeStart;
        this.validTimeStop = visitorRole.validTimeStop;
    }
    
    
    public Date getValidTimeStart() {
        return validTimeStart;
    }
    
    public Date getValidTimeStop() {
        return validTimeStop;
    }
    
    public String getRoleName() {
        return roleName;
    }
    
    public int getRoleID() {
        return roleID;
    }
    
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public void setRoleID(char roleID) {
        this.roleID = roleID;
    }
    
    public void setRoleID(int roleID) {
        this.roleID = roleID;
    }

    public void setValidTimeStart(Date validTimeStart) {
        this.validTimeStart = validTimeStart;
    }

    public void setValidTimeStop(Date validTimeStop) {
        this.validTimeStop = validTimeStop;
    }

    public String toString(){
        return roleName +"("+ roleID +")";
    }
}
