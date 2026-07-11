package com.lms.common.vedantu.entity.storage;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;


public class EntityFileStorageException extends VedantuException
{


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
