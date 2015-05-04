package com.youchip.youmobile.model.chip.exceptions;

import java.io.IOException;

public class RfidTransactionException extends IOException {

	/**
     * 
     */
    private static final long serialVersionUID = 6337360997618463802L;

    /**
	 * 
	 */
	
	public RfidTransactionException(){
	    super();
	}

	public RfidTransactionException(String message){
		super(message);
	}
	
}
