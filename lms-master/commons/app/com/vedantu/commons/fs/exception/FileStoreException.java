package com.vedantu.commons.fs.exception;


@SuppressWarnings("serial")
public class FileStoreException extends Exception {

	public FileStoreException(String message, Throwable cause) {
		super(message, cause);
	//	Logger.log4j.debug(message);
	}

	public FileStoreException(String message) {
		super(message);
	}

}
