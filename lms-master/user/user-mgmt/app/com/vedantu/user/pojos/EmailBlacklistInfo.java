package com.vedantu.user.pojos;

import com.vedantu.user.enums.EmailBlacklistAction;

public class EmailBlacklistInfo {

    public String               reason;
    public EmailBlacklistAction action;
    public long                 time;

    public EmailBlacklistInfo(String reason, EmailBlacklistAction action) {

        super();
        this.reason = reason;
        this.action = action;
        this.time = System.currentTimeMillis();
    }

    public EmailBlacklistInfo() {

        this(null, null);
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{reason:").append(reason).append(", action:").append(action)
                .append(", time:").append(time).append("}");
        return builder.toString();
    }

}
