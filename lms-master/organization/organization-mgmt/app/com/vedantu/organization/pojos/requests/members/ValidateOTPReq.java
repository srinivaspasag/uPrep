package com.vedantu.organization.pojos.requests.members;

import org.apache.commons.lang3.StringUtils;

public class ValidateOTPReq {
    public String   contactNumber = StringUtils.EMPTY;
    public String   countryCode =   StringUtils.EMPTY;
    public String   fullName =      StringUtils.EMPTY;
    public String   userOTP =       StringUtils.EMPTY;
    public String   sessionOTP =    StringUtils.EMPTY;
}
