package com.lms.pojo.request.device.mgmt;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.lms.common.vedantu.enums.DeviceType;
import com.lms.user.vedantu.user.enums.UserStatus;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class GetUserDeviceStatusReq extends AbstractAuthCheckReq

{

    @NotBlank(message = "orgId cannot be blank")
    public String     orgId;
    @NotBlank(message = "targetUserId cannot be blank")
    public String     targetUserId;
    public int        start = 0;
    public int        size = 10;
    public UserStatus status;
    public DeviceType deviceType;
}
