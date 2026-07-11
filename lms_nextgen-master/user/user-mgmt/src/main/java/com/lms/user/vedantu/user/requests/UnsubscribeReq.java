package com.lms.user.vedantu.user.requests;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class UnsubscribeReq extends AbstractAppCheckReq {
    @NotBlank(message = "User ID is required")
    public String  userId;
    @NotBlank(message = "Target user ID is required")
    public String  targetUserId;
    @NotBlank(message = "Mail Category is required")
    public String  mailCategory;
    public boolean external = false;
    public String  reason;

}
