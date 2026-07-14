package com.vedantu.commons.pojos.requests;

import java.util.Map;

import play.data.validation.Constraints.Required;

public abstract class AbstractAuthCheckReq extends AbstractAppCheckReq {

    @Required
    public String callingUserId;
    @Required
    public String userId;

    public AbstractAuthCheckReq() {

    }

    public AbstractAuthCheckReq(String callingUserId, String userId) {

        super();
        this.callingUserId = callingUserId;
        this.userId = userId;
    }

    protected AbstractAuthCheckReq(Map<String, String[]> form) {

        super(form);
        callingUserId = _getValueFromMultipart(form, "callingUserId");
        userId = _getValueFromMultipart(form, "userId");
    }

    @Override
    public String validate() {

        String superValidate = super.validate();
        if (null != superValidate) {
            return superValidate;
        }
        if (null == callingUserId) {
            return "callingUserId missing";
        }
        if (null == userId) {
            return "userId missing";
        }
        return null;
    }

    public String getCallingUserId() {
        return callingUserId;
    }

    public void setCallingUserId(String callingUserId) {
        this.callingUserId = callingUserId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

}
