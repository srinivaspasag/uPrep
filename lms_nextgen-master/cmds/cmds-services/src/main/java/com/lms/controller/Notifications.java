package com.lms.controller;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.requests.GetRegIdsReq;
import com.lms.pojos.requests.NotificationRegIDReq;
import com.lms.services.NotificationsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
public class Notifications {
    @Autowired
    private NotificationsService notificationsServiceImpl;

    @PostMapping("/getRegIds")
    public ResponseEntity<VedantuResponse> getRegIds(GetRegIdsReq getRegIdsReq) {
        return ResponseEntity.ok(notificationsServiceImpl.getRegIds(getRegIdsReq));
    }

    @PostMapping
    public ResponseEntity<VedantuResponse> registerById(NotificationRegIDReq notificationRegIDReq) {
        return ResponseEntity.ok(notificationsServiceImpl.registerById(notificationRegIDReq));
    }
}
