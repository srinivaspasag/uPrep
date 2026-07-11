package com.lms.pojo.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValidateOTPReq {
    public String contactNumber = "";
    public String countryCode = "";
    public String fullName = "";
    public String userOTP = "";
    public String sessionOTP = "";
}
