package com.vedantu.organization.pojos.requests.members;

import org.apache.commons.lang3.StringUtils;

public class SendOTPReq {
    public String contactNumber = StringUtils.EMPTY;
    public String fullName      = StringUtils.EMPTY;
    public String progType      = StringUtils.EMPTY;
    public String countryCode   = StringUtils.EMPTY;
    public String userId        = StringUtils.EMPTY;
    public String existingOTP   = StringUtils.EMPTY;
    public String orgId;
}
