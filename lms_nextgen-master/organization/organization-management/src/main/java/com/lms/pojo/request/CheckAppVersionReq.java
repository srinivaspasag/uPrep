package com.lms.pojo.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CheckAppVersionReq {
    public int appVersion;
    public String orgId;
}
