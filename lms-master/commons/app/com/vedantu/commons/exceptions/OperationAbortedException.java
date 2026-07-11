package com.vedantu.commons.exceptions;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;

public class OperationAbortedException extends VedantuException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public OperationAbortedException(VedantuErrorCode errorCode) {

        super(errorCode);

    }

}
