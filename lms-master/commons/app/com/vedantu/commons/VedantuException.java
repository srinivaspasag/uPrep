package com.vedantu.commons;

/**
 * To be used by sub-classes of {@link AbstractVedantuManager}.
 * 
 * @author ujjawal
 * 
 */
public class VedantuException extends Throwable {

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
