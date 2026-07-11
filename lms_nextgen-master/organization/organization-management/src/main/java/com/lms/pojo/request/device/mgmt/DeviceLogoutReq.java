package com.lms.pojo.request.device.mgmt;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.lms.common.vedantu.enums.DeviceType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class DeviceLogoutReq extends AbstractAuthCheckReq {
    @NotBlank(message = "deviceId is missing")
    public String deviceId;
    @NotNull(message = "deviceId is missing")
    public DeviceType deviceType;
}
