package com.lms.user.vedantu.user.pojo;

import com.lms.common.vedantu.enums.MailCategory;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
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
