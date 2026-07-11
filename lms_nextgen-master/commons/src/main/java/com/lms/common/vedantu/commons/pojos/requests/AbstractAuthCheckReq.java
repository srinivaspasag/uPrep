package com.lms.common.vedantu.commons.pojos.requests;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public abstract class AbstractAuthCheckReq extends AbstractAppCheckReq {
    @NotBlank(message = "Calling user ID is required")
    public String callingUserId;
    @NotBlank(message = "User ID is required")
    public String userId;

    public AbstractAuthCheckReq(String callingUserId, String userId) {

        super();
        this.callingUserId = callingUserId;
        this.userId = userId;
    }

    public AbstractAuthCheckReq() {

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
}
