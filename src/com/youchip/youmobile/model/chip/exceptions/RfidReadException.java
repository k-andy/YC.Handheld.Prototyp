package com.youchip.youmobile.model.chip.exceptions;

public class RfidReadException extends RfidTransactionException {

	/**
     * 
     */
    private static final long serialVersionUID = -3205115140752411150L;

    public RfidReadException(){
	    super("Reading failed!");
	}

	public RfidReadException(String message){
		super(message);
	}
	
	public RfidReadException(int errorID){
	    super("Reading failed! Error: " + errorID );
	}
	
    public RfidReadException(int errorID, int block){
        super("Reading block '"+ block +"' failed! Error: " + errorID );
    }
}
