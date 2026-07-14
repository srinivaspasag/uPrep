package com.lms.common.vedantu.dto.response;

import com.lms.common.vedantu.constants.HardCodedConstants;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VedantuResponse  {

    private Object result;
    private String errorMessage;
    private String errorCode;

    public VedantuResponse(Object result, String errorMessage, String errorCode) {
        super();
        this.result = null != result ? result : HardCodedConstants.emptyString;
        this.errorMessage = null != errorMessage ? errorMessage : HardCodedConstants.emptyString;
        this.errorCode = null != errorCode ? errorCode : HardCodedConstants.emptyString;
    }

    public VedantuResponse(Object result) {
        this(result, HardCodedConstants.emptyString, HardCodedConstants.emptyString);
    }

}
