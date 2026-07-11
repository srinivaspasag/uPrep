package com.lms.controller;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.GetMessageReq;
import com.lms.pojos.requests.GetConversationReq;
import com.lms.pojos.requests.GetUserMailBoxInfoReq;
import com.lms.pojos.requests.UpdateUserMailBoxInfoReq;
import com.lms.pojos.requests.messages.*;
import com.lms.requests.GetConversationUsersReq;
import com.lms.requests.GetMessageSummariesReq;
import com.lms.services.MessageCenterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/messageCenter")
public class MessageCenter {
	@Autowired
	private MessageCenterService messageCenterService;

	@PostMapping("/getConversationSummary")
	public ResponseEntity<VedantuResponse> getConversationSummary(GetConversationSummaryReq getConversationSummaryReq) {
		return ResponseEntity.ok(messageCenterService.getConversationSummary(getConversationSummaryReq));
	}

	@PostMapping("/getConversationSummaries")
	public ResponseEntity<VedantuResponse> getConversationSummaries(GetConversationSummariesReq getConversationSummariesReq) {
		return ResponseEntity.ok(messageCenterService.getConversationSummaries(getConversationSummariesReq));
	}

	@PostMapping("/getMessageSummary")
	public ResponseEntity<VedantuResponse> getMessageSummary(@Valid GetMessageSummaryReq getMessageSummaryReq) {
		return ResponseEntity.ok(messageCenterService.getMessageSummary(getMessageSummaryReq));
	}

	@PostMapping("/getMessageSummaries")
	public ResponseEntity<VedantuResponse> getMessageSummaries(@Valid GetMessageSummariesReq getMessageSummaryReq) {
		return ResponseEntity.ok(messageCenterService.getMessageSummaries(getMessageSummaryReq));
	}

	@PostMapping("/getMessage")
	public ResponseEntity<VedantuResponse> getMessage(@Valid GetMessageReq getMessageSummaryReq) {
		return ResponseEntity.ok(messageCenterService.getMessage(getMessageSummaryReq));
	}

	@PostMapping("/getUserMailBoxInfo")
	public ResponseEntity<VedantuResponse> getUserMailBoxInfo(@Valid GetUserMailBoxInfoReq getUserMailBoxInfoReq) {
		return ResponseEntity.ok(messageCenterService.getUserMailBoxInfo(getUserMailBoxInfoReq));
	}

	@PostMapping("/sendMessage")
	public ResponseEntity<VedantuResponse> sendMessage(@Valid SendMessageReq sendMessageReq) {
		return ResponseEntity.ok(messageCenterService.sendMessage(sendMessageReq));
	}

	@PostMapping("/markConversation")
	public ResponseEntity<VedantuResponse> markConversation(@Valid MarkConversationReq markConversationReq) {
		return ResponseEntity.ok(messageCenterService.markConversation(markConversationReq));
	}

	@PostMapping("/deleteConversation")
	public ResponseEntity<VedantuResponse> deleteConversation(@Valid DeleteConversationReq deleteConversationReq) {
		return ResponseEntity.ok(messageCenterService.deleteConversation(deleteConversationReq));
	}

	@PostMapping("/updateMailBoxInfo")
	public ResponseEntity<VedantuResponse> updateMailBoxInfo(@Valid UpdateUserMailBoxInfoReq updateUserMailBoxInfoReq) {
		return ResponseEntity.ok(messageCenterService.updateMailBoxInfo(updateUserMailBoxInfoReq));
	}

	@PostMapping("/getConversationUsers")
	public ResponseEntity<VedantuResponse> getConversationUsers(@Valid GetConversationUsersReq getConversationUsersReq) {
		return ResponseEntity.ok(messageCenterService.getConversationUsers(getConversationUsersReq));
	}

	@PostMapping("/getConversation")
	public ResponseEntity<VedantuResponse> getConversation(@Valid GetConversationReq getConversationReq) {
		return ResponseEntity.ok(messageCenterService.getConversation(getConversationReq));
	}


}
