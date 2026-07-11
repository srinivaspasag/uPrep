package com.vedantu.user.pojos.requests;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAppCheckReq;
/**
 * MailCategory = ALL wll subscribe to all emails again
 * @author vikram
 *
 */
public class EmailSubscribeReq extends AbstractAppCheckReq {

    @Required
    public String userId;
    @Required
    public String targetUserId;
    @Required
    public String mailCategory;

}