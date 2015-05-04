package com.youchip.youmobile.model.chip.exceptions;

public class RfidSecurityException extends RfidTransactionException {

	/**
     * 
     */
    private static final long serialVersionUID = -3205115140752411150L;

    public RfidSecurityException(){
	    super("Access denied!");
	}

	public RfidSecurityException(String message){
		super(message);
	}
		
    public RfidSecurityException(int block){
        super("Accessing block '"+ block +"' denied!");
    }
}
