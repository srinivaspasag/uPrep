package com.vedantu.commons.pojos.requests;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.utils.VedantuStringUtils;

public abstract class AbstractAppCheckReq {

    @Required
    public String callingApp;
    @Required
    public String callingAppId;

    public AbstractAppCheckReq() {

    }

    protected AbstractAppCheckReq(Map<String, String[]> form) {

        callingApp = _getValueFromMultipart(form, "callingApp");
        callingAppId = _getValueFromMultipart(form, "callingAppId");
    }

    protected static String _getValueFromMultipart(Map<String, String[]> form, String key) {

        String[] values = form.get(key);
        if (VedantuStringUtils.isEmpty(values)) {
            return null;
        }
        String value = values[0];
        return value;
    }

    protected static String[] _getValuesFromMultipart(Map<String, String[]> form, String key) {

        String[] values = form.get(key);
        return values;
    }

    public String validate() {

        if (null == callingApp) {
            return "callingApp missing";
        }
        if (null == callingAppId) {
            return "callingAppId missing";
        }
        return null;
    }

    public boolean isWebReq() {

        return StringUtils.equalsIgnoreCase("cmds-app", callingAppId)
                || StringUtils.equalsIgnoreCase("web-app", callingAppId);
    }

    public String getCallingApp() {
        return callingApp;
    }

    public void setCallingApp(String callingApp) {
        this.callingApp = callingApp;
    }

    public String getCallingAppId() {
        return callingAppId;
    }

    public void setCallingAppId(String callingAppId) {
        this.callingAppId = callingAppId;
    }

}
