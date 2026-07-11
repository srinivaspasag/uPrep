package com.lms.pojo.responce.device.mgmt;

import com.lms.common.vedantu.commons.pojos.requests.responces.ListResponse;
import com.lms.pojo.DeviceInfo;
import com.lms.pojo.OrgMemberExtendedInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetUserDeviceStatusesRes  extends ListResponse<DeviceInfo>

{
    OrgMemberExtendedInfo memberInfo;

}
