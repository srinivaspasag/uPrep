package com.lms.pojo.responce;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendOTPRes {
    public boolean isNewPhone;
    public boolean hasEmail;
    public String smsReference;
    public String OTP;
    public String fullname;
    public String contactNumber;
    public String countryCode;
    public String progType;
    public Boolean isPhoneVerified;
}
