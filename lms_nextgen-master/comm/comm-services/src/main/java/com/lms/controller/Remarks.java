package com.lms.controller;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.requests.remarks.AddRemarksReq;
import com.lms.requests.remarks.GetRemarksReq;
import com.lms.services.RemarksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/remarks")
public class Remarks {
	@Autowired
	RemarksService remarksService;

	@PostMapping("/addRemark")
	public ResponseEntity<VedantuResponse> addRemark(AddRemarksReq recordAttemptReq) {
		return ResponseEntity.ok(remarksService.addRemark(recordAttemptReq));
	}

	@PostMapping("/getRemarksForUser")
	public ResponseEntity<VedantuResponse> getRemarksForUser(GetRemarksReq getRemarksReq) {
		return ResponseEntity.ok(remarksService.getRemarksForUser(getRemarksReq));
	}
}
