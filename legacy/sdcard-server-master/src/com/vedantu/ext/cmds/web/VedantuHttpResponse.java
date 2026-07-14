package com.vedantu.ext.cmds.web;

import org.json.JSONObject;

import com.vedantu.ext.cmds.utils.web.WebCommunicator;

public class VedantuHttpResponse implements JSONAware {

    public String      errorCode;
    public String      errorMessage;
    private JSONObject result;
    public int         responseCode;

    public String getErrorCode() {

        return errorCode;
    }

    public String getErrorMessage() {

        return errorMessage;
    }

    public JSONObject getResult() {

        return result;
    }

    public int getResponseCode() {

        return responseCode;
    }

    @Override
    public void fromJSON(JSONObject json) {

        errorCode = JSONUtils.getString(json, WebCommunicator.KEY_ERROR_CODE);
        errorMessage = JSONUtils.getString(json, WebCommunicator.KEY_ERROR_MESSAGE);
        result = JSONUtils.getJSONObject(json, WebCommunicator.KEY_RESULT);
    }

    @Override
    public JSONObject toJSON() {

        return new JSONObject(this);
    }

    public void populateResult(JSONAware jAware) {

        jAware.fromJSON(result);
    }

    @Override
    public String toString() {

        return "{errorCode : " + errorCode + ", errorMessage : " + errorMessage + ", result : "
                + result + ", responseCode : " + responseCode + "}";
    }

}
