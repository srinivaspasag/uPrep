package com.vedantu.commons;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.node.ObjectNode;

import play.libs.Json;

public class JSONResponse {

    private Object result;
    private String errorMessage;
    private String errorCode;

    public JSONResponse(Object result, String errorMessage, String errorCode) {

        super();
        this.result = null != result ? result : StringUtils.EMPTY;
        this.errorMessage = null != errorMessage ? errorMessage : StringUtils.EMPTY;
        this.errorCode = null != errorCode ? errorCode : StringUtils.EMPTY;
    }

    public JSONResponse(Object result) {

        this(result, StringUtils.EMPTY, StringUtils.EMPTY);
    }

    public JSONResponse(VedantuException e) {

        this(StringUtils.EMPTY, null != e ? e.getMessage() : StringUtils.EMPTY, null != e
                && null != e.errorCode ? e.errorCode.name() : VedantuErrorCode.SERVICE_ERROR.name());
    }
    
    public JSONResponse(VedantuException e, Object result) {

        this(result, null != e ? e.getMessage() : StringUtils.EMPTY, null != e
                && null != e.errorCode ? e.errorCode.name() : VedantuErrorCode.SERVICE_ERROR.name());
    }

    public Object getResult() {

        return result;
    }

    public void setResult(Object result) {

        this.result = result;
    }

    public String getErrorMessage() {

        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {

        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {

        return errorCode;
    }

    public void setErrorCode(String errorCode) {

        this.errorCode = errorCode;
    }

    public ObjectNode toObjectNode() {

        ObjectNode o = Json.newObject();
        o.put("result", Json.toJson(result));
        o.put("errorMessage", errorMessage);
        o.put("errorCode", errorCode);
        return o;
    }
}
