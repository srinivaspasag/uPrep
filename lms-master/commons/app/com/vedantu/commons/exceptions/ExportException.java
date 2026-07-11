package com.vedantu.commons.exceptions;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;

public class ExportException extends VedantuException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ExportException(VedantuErrorCode errorCode) {

        super(errorCode);
        // TODO Auto-generated constructor stub
    }

    public ExportException(VedantuErrorCode errorCode, String ex) {

        super(errorCode, ex);
        // TODO Auto-generated constructor stub
    }
    
    public ExportException(VedantuErrorCode errorCode, Throwable ex) {

        super(errorCode, ex);
        // TODO Auto-generated constructor stub
    }
}
