package com.vedantu.comm.requests.messages;

import play.data.validation.Constraints.Required;

import com.vedantu.comm.models.hbase.messages.Message;
import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class SendMessageReq extends AbstractAuthCheckReq {

    public String  orgId;
    @Required
    public Message message;

    public String validate() {

        if (null != message) {
            String validation = message.validate();
            if (null != validation) {
                return validation;
            }
        }

        return null;
    }

}
