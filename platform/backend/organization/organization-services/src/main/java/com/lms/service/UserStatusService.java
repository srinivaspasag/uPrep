package com.lms.service;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojo.request.GetUserStatusReq;
import com.lms.pojo.request.RecordActivityReq;
import com.lms.pojo.request.device.mgmt.DeviceLoginReq;
import com.lms.pojo.request.device.mgmt.DeviceLogoutReq;
import com.lms.pojo.request.device.mgmt.GetUserDeviceStatusReq;

public interface UserStatusService {
    VedantuResponse recordActivity(RecordActivityReq recordActivityReq);

    VedantuResponse newLogin(DeviceLoginReq deviceLoginReq);

    VedantuResponse newLogout(DeviceLogoutReq deviceLogoutReq);

    VedantuResponse checkIfUserExists(DeviceLogoutReq deviceLogoutReq);

    VedantuResponse getUsers(GetUserStatusReq getUserStatusReq);

    VedantuResponse getUserStatus(GetUserDeviceStatusReq getUserDeviceStatusReq);


}
