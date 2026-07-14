package com.lms.user.vedantu.user.dto;

import com.lms.user.vedantu.user.pojo.EmailChangeReqInfo;
import com.lms.user.vedantu.user.pojo.ForgotPasswordReqInfo;
import com.lms.user.vedantu.user.pojo.SocialInfo;
import com.lms.user.vedantu.user.pojo.TnCAcceptance;
import com.lms.common.vedantu.commons.pojos.requests.SecurityCredentials;
import com.lms.common.vedantu.enums.AuthType;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserDto {

    public String userId;
    public String                username;
    public String                password;
    public String                firstName;
    public String                lastName;
    public String                dob;
    public String                thumbnail;
    public String                email;

    public boolean               isEmailVerified   = false;
    public boolean               isSysGenPassword  = false;
    public boolean               isPhoneVerified   = false;
    public boolean               isOTPuser         = false;

    public EmailChangeReqInfo emailChangeReq;
    public ForgotPasswordReqInfo forgotPasswordReq;
    public TnCAcceptance tncAcceptance;
    public SecurityCredentials credentials;
    public SocialInfo socialInfo;
    public AuthType authType;

}
