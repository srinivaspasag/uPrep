package com.lms.pojo.request;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendOTPReq {
    public String contactNumber = "";
    public String fullName = "";
    public String progType = "";
    public String countryCode = "";
    public String userId = "";
    public String existingOTP = "";
    public String orgId;
}
