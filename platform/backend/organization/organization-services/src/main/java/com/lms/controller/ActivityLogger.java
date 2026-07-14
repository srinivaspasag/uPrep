package com.lms.controller;


import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojo.request.GetUserStatusReq;
import com.lms.pojo.request.RecordActivityReq;
import com.lms.pojo.request.device.mgmt.DeviceLoginReq;
import com.lms.pojo.request.device.mgmt.DeviceLogoutReq;
import com.lms.pojo.request.device.mgmt.GetUserDeviceStatusReq;
import com.lms.service.UserStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/activitylogger")
public class ActivityLogger {
    @Autowired
    private UserStatusService userStatusServiceImpl;

    @PostMapping("/record")
    public ResponseEntity<VedantuResponse> record(@Valid RecordActivityReq recordActivityReq) {
        return ResponseEntity.ok(userStatusServiceImpl.recordActivity(recordActivityReq));
    }

    @PostMapping("/login")
    public ResponseEntity<VedantuResponse> login(@Valid DeviceLoginReq deviceLoginReq) {
        return ResponseEntity.ok(userStatusServiceImpl.newLogin(deviceLoginReq));
    }

    @PostMapping("/logout")
    public ResponseEntity<VedantuResponse> login(@Valid DeviceLogoutReq deviceLogoutReq) {
        return ResponseEntity.ok(userStatusServiceImpl.newLogout(deviceLogoutReq));
    }

    @PostMapping("/checkIfUserExists")
    public ResponseEntity<VedantuResponse> checkIfUserExists(@Valid DeviceLogoutReq deviceLogoutReq) {
        return ResponseEntity.ok(userStatusServiceImpl.checkIfUserExists(deviceLogoutReq));
    }

    //Need to test and check hits.
    @PostMapping("/getUsers")
    public ResponseEntity<VedantuResponse> getUsers(@Valid GetUserStatusReq getUserStatusReq) {
        return ResponseEntity.ok(userStatusServiceImpl.getUsers(getUserStatusReq));
    }

    @PostMapping("/getUserStatus")
    public ResponseEntity<VedantuResponse> getUserStatus(@Valid GetUserDeviceStatusReq getUserDeviceStatusReq)
    {
        return ResponseEntity.ok(userStatusServiceImpl.getUserStatus(getUserDeviceStatusReq));
    }
}
