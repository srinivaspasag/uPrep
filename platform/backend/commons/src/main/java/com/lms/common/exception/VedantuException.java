package com.lms.common.exception;

public class VedantuException extends RuntimeException {

    private static final long     serialVersionUID = 1L;

    public final VedantuErrorCode errorCode;

    public VedantuException(VedantuErrorCode errorCode) {

        this(errorCode, null, null);
    }

    public VedantuException(VedantuErrorCode errorCode, String message) {

        this(errorCode, message, null);
    }

    public VedantuException(VedantuErrorCode errorCode, Throwable t) {

        this(errorCode, null, null);

    }

    public VedantuException(VedantuErrorCode errorCode, String message, Throwable t) {

        super(message, t);
        this.errorCode = errorCode;
    }

    @Override
    public String toString() {

        return "{errorCode=" + errorCode + ", toString()=" + super.toString() + "}";
    }
}