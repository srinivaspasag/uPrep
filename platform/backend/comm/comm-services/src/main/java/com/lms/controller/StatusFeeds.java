package com.lms.controller;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.requests.AddStatusFeedReq;
import com.lms.services.StatusFeedsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.lms.requests.DeleteStatusFeedReq;
import com.lms.requests.GetStatusFeedReq;

@RestController
@RequestMapping("/statusFeeds")
public class StatusFeeds {
	@Autowired
	private StatusFeedsService statusFeedsService;

	@PostMapping("/addStatusFeed")
	public ResponseEntity<VedantuResponse> addStatusFeed(AddStatusFeedReq addStatusFeedReq) {
		return ResponseEntity.ok(statusFeedsService.addStatusFeed(addStatusFeedReq));
	}
	@PostMapping("/getStatusFeed")
	public ResponseEntity<VedantuResponse> getStatusFeed(GetStatusFeedReq getStatusFeedReq) {
		return ResponseEntity.ok(statusFeedsService.getStatusFeed(getStatusFeedReq));
	}
	@PostMapping("/delete")
	public ResponseEntity<VedantuResponse> delete(DeleteStatusFeedReq deleteStatusFeedReq) {
		return ResponseEntity.ok(statusFeedsService.delete(deleteStatusFeedReq));
	}
	

}
