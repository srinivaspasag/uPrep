package com.lms.pojos.response.messages;

import com.lms.models.messages.MessageSummary;

public class SendMessageRes {
    public boolean isReceived = false;
    public MessageSummary message;

    public SendMessageRes(boolean isReceived, MessageSummary message) {
        super();
        this.isReceived = isReceived;
        this.message = message;
    }
}
