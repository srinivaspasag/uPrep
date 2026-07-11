package com.lms.controller;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.requests.AddScheduleReq;
import com.lms.pojos.requests.GetScheduleReq;
import com.lms.pojos.requests.RemoveScheduleReq;
import com.lms.services.ClassroomConnectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/classroomconnect")
public class ClassroomConnect {
    @Autowired
    private ClassroomConnectService classroomConnectServiceImpl;
    @PostMapping("/getSchedule")
    public ResponseEntity<VedantuResponse> getSchedule(@Valid GetScheduleReq getScheduleReq) throws VedantuException {
        return ResponseEntity.ok(classroomConnectServiceImpl.getschedule(getScheduleReq));
    }
    @PostMapping("/removeDaySchedule")
    public ResponseEntity<VedantuResponse> removeDaySchedule(@Valid GetScheduleReq getScheduleReq) throws VedantuException {
        return ResponseEntity.ok(classroomConnectServiceImpl.removedaySchedule(getScheduleReq));
    }
    @PostMapping("/removeSchedule")
    public ResponseEntity<VedantuResponse> removeSchedule(@Valid RemoveScheduleReq removeScheduleReq) throws VedantuException {
        return ResponseEntity.ok(classroomConnectServiceImpl.removeschedule(removeScheduleReq));
    }
    @PostMapping("/getDaySchedule")
    public ResponseEntity<VedantuResponse> getDaySchedule(@Valid GetScheduleReq getScheduleReq) throws VedantuException {
        return ResponseEntity.ok(classroomConnectServiceImpl.getDayschedule(getScheduleReq));
    }
    @PostMapping("/addSchedule")
    public ResponseEntity<VedantuResponse> addSchedule(@Valid AddScheduleReq addScheduleReq) throws VedantuException {
        if (addScheduleReq.getEntityList().isEmpty() || addScheduleReq.getEntityList() == null)
            throw new VedantuException(VedantuErrorCode.INVALID_CODE, "entity type should not be null");
        if (addScheduleReq.getDay() == 0)
            throw new VedantuException(VedantuErrorCode.INVALID_CODE, "day  should not be null");
        return ResponseEntity.ok(classroomConnectServiceImpl.addschedule(addScheduleReq));
    }
}
