package com.youchip.youmobile.model.chip.exceptions;

public class RfidWriteException extends RfidTransactionException {

    /**
     * 
     */
    private static final long serialVersionUID = -3081638906155159241L;

    public RfidWriteException(){
        super("Writing failed!");
    }

	public RfidWriteException(String message) {
		super(message);
	}
	
    public RfidWriteException(int errorID){
        super("Writing failed! Error: " + errorID );
    }
    
    public RfidWriteException(int errorID, int block){
        super("Writing block '"+ block +"' failed! Error: " + errorID );
    }

}
