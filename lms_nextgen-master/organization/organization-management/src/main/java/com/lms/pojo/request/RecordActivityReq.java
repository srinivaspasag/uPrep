package com.lms.pojo.request;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.DeviceType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class RecordActivityReq extends AbstractAuthCheckReq {
    @NotBlank(message = "orgId is missing")
    public String orgId;
    @NotBlank(message = "deviceId is missing")
    public String deviceId;
    @NotNull(message = "deviceType is missing")
    public DeviceType deviceType;
    @NotBlank(message = "page is missing")
    public String page;

    public String userAction;
    public SrcEntity entity;
    public long activityTime = 0;
}
