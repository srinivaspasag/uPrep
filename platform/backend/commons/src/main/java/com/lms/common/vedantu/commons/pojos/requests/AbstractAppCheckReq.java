package com.lms.common.vedantu.commons.pojos.requests;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
@Getter
@Setter
public abstract class AbstractAppCheckReq {

    @NotBlank(message = "callingApp is required")
    public String callingApp;
    @NotBlank(message = "callingAppId is required")
    public String callingAppId;

    public AbstractAppCheckReq() {

    }

    public boolean isWebReq() {

        return callingAppId.equalsIgnoreCase("cmds-app") || callingAppId.equalsIgnoreCase("web-app");

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


    /*protected AbstractAppCheckReq(Map<String, String[]> form) {
        callingApp = _getValueFromMultipart(form, "callingApp");
        callingAppId = _getValueFromMultipart(form, "callingAppId");
    }

    protected static String _getValueFromMultipart(Map<String, String[]> form, String key) {
        String[] values = form.get(key);
        if (Validation.isEmptyStringArray(values)) {
            return null;
        }
        String value = values[0];
        return value;
    }

    protected static String[] _getValuesFromMultipart(Map<String, String[]> form, String key) {
        String[] values = form.get(key);
        return values;
    }

//    public String validate() {
//        if (null == callingApp) {
//            return "callingApp missing";
//        }
//        if (null == callingAppId) {
//            return "callingAppId missing";
//        }
//        return null;
//    }

   */

}