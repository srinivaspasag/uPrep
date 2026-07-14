package com.vedantu.organization.pojos.responses.device.mgmt;

import com.vedantu.commons.pojos.responses.ListResponse;
import com.vedantu.organization.pojos.OrgMemberExtendedInfo;
import com.vedantu.organization.pojos.device.mgmt.DeviceInfo;

public class GetUserDeviceStatusesRes extends ListResponse<DeviceInfo> {

    OrgMemberExtendedInfo memberInfo;

}
