package com.vedantu.comm.enums;

import play.Logger;
import play.Logger.ALogger;

public  enum ConversationStatus {
	READ, UNREAD;
	private static final ALogger LOGGER = Logger.of(ConversationStatus.class);
	public static ConversationStatus getValueOfKey( String key ){
	    ConversationStatus status = ConversationStatus.UNREAD;
		try{ 
		    status =  ConversationStatus.valueOf(key);
		}
		catch( Exception exception ){
			LOGGER.debug("Could not find enum for "+ key, exception );  
		}
		 return status;
	}
}