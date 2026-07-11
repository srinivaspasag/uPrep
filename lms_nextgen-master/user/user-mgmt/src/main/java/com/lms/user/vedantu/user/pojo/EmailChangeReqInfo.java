package com.lms.user.vedantu.user.pojo;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EmailChangeReqInfo {

    public String email;
    public String verificationCode;

    public EmailChangeReqInfo() {
        super();
    }

    public EmailChangeReqInfo(String email, String verificationCode) {
        super();
        this.email = email;
        this.verificationCode = verificationCode;
    }
}
