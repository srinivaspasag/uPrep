package com.vedantu.user.pojos;

import com.google.code.morphia.annotations.Embedded;
import com.vedantu.commons.enums.MailCategory;

@Embedded
public class UserRestrictedEmailCategory {

    public MailCategory category;
    public String       userId;
    public String       reason;
    public long         unsubscribeTime;

    public UserRestrictedEmailCategory() {

    }

    public UserRestrictedEmailCategory(MailCategory category, String userId, String reason) {

        super();
        this.category = category;
        this.userId = userId;
        this.reason = reason;
        this.unsubscribeTime= System.currentTimeMillis();
    }

    
}
