package com.lms.user.vedantu.user.pojo;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ForgotPasswordReqInfo {
    public String verificationCode;

    public ForgotPasswordReqInfo() {
        super();
    }

    public ForgotPasswordReqInfo(String verificationCode) {
        super();
        this.verificationCode = verificationCode;
    }
}
