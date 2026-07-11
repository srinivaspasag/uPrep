package com.vedantu.comm.requests.newsfeeed;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class GetOlderNotificationsReq extends AbstractAuthCheckReq {

    @Required
    public String  beforeNotificationId;
    @Required
    public int     size;
    public boolean needClustered;

    public String  orgId;

    public String validate() {

        String superValidate = super.validate();
        if (superValidate != null) {
            return superValidate;
        }
        return null;
    }
}
