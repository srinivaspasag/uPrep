package com.lms.services.serviceImpl;

import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.components.MessageManager;
import com.lms.pojos.GetMessageReq;
import com.lms.pojos.requests.GetConversationReq;
import com.lms.pojos.requests.GetUserMailBoxInfoReq;
import com.lms.pojos.requests.UpdateUserMailBoxInfoReq;
import com.lms.pojos.requests.messages.*;
import com.lms.pojos.response.*;
import com.lms.pojos.response.messages.*;
import com.lms.requests.GetConversationUsersReq;
import com.lms.requests.GetMessageSummariesReq;
import com.lms.services.MessageCenterService;
import com.lms.user.vedantu.user.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageCenterServiceImpl implements MessageCenterService {
	@Autowired
	private MessageManager messageManager;
	@Autowired
	private UserRepo userRepo;

	@Override
	public VedantuResponse getConversationSummary(GetConversationSummaryReq request) {
		GetConversationSummaryRes response = new GetConversationSummaryRes();

		response = messageManager.getConversationSummary(request);

		return new VedantuResponse(response);


	}

	@Override
	public VedantuResponse getMessageSummary(GetMessageSummaryReq request) {


		GetMessageSummaryRes response = new GetMessageSummaryRes();
		response.summary = messageManager.getMessageSummary(request.orgId, request.userId, request.userMessageId);

		return new VedantuResponse(response);

	}

	@Override
	public VedantuResponse getConversationSummaries(GetConversationSummariesReq request) {
		GetConversationSummariesRes response = null;

		try {
			response = messageManager.getConversationSummaries(request);

		} catch (VedantuException e) {
			throw e;

		}

		return new VedantuResponse(response);

	}

	@Override
	public VedantuResponse getMessageSummaries(GetMessageSummariesReq getMessageSummaryReq) {
		GetMessageSummariesRes response = messageManager.getMessageSummaries(getMessageSummaryReq);
		return new VedantuResponse(response);
	}

	@Override
	public VedantuResponse getMessage(GetMessageReq getMessageSummaryReq) {

		GetMessageRes response = messageManager.getMessage(getMessageSummaryReq);

		return new VedantuResponse(response);
	}

	@Override
	public VedantuResponse getUserMailBoxInfo(GetUserMailBoxInfoReq request) {

		GetUserMailBoxInfoRes response = messageManager.getUserMailBoxInfo(request);
		return new VedantuResponse(response);
	}

	@Override
	public VedantuResponse sendMessage(SendMessageReq request) {
		SendMessageRes response = null;

		try {
			response = messageManager.sendMessage(request);

		} catch (VedantuException e) {
			throw e;

		}

		return new VedantuResponse(response);

	}

	@Override
	public VedantuResponse updateMailBoxInfo(UpdateUserMailBoxInfoReq request) {

		UpdatedUserMailBoxInfoRes response = messageManager.updateUsersMailBoxesInfos(request.userIds);
		return new VedantuResponse(response);
	}

	@Override
	public VedantuResponse getConversationUsers(GetConversationUsersReq getConversationUsersReq) {
		GetConversationUsersRes response = messageManager.getConversationUsers(getConversationUsersReq);
		return new VedantuResponse(response);
	}

	@Override
	public VedantuResponse getConversation(GetConversationReq getConversationReq) {

		GetConversationRes response = messageManager.getConversation(getConversationReq);
		return new VedantuResponse(response);
	}

	@Override
	public VedantuResponse markConversation(MarkConversationReq request) {
		MarkConversationRes response = null;

		try {
			response = messageManager.markConversation(request);

		} catch (VedantuException e) {

			throw e;

		}

		return new VedantuResponse(response);

	}

	@Override
	public VedantuResponse deleteConversation(DeleteConversationReq request) {
		DeleteConversationRes response = null;
		try {
			response = messageManager.deleteUserConversation(request);

		} catch (VedantuException e) {
			throw e;

		}

		return new VedantuResponse(response);

	}


}
