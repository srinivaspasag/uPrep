package com.vedantu.commons.entity.storage;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;

public class EntityFileStorageException extends VedantuException {

    /**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	public EntityFileStorageException(String message) {
        super(VedantuErrorCode.STORAGE_EXCEPTION,message);
    }

    public EntityFileStorageException(Throwable cause) {
        super(VedantuErrorCode.STORAGE_EXCEPTION,"",cause);
    }

    public EntityFileStorageException(String message, Throwable cause) {
        super(VedantuErrorCode.STORAGE_EXCEPTION,message, cause);
    }

}
