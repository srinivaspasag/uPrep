package com.lms.services.serviceImpl;

import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.components.StatusFeedsComponent;
import com.lms.pojos.response.DeleteStatusFeedRes;
import com.lms.pojos.response.GetStatusFeedRes;
import com.lms.requests.AddStatusFeedReq;
import com.lms.requests.DeleteStatusFeedReq;
import com.lms.requests.GetStatusFeedReq;
import com.lms.response.newsfeed.AddStatusFeedRes;
import com.lms.services.StatusFeedsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatusFeedsServiceImpl implements StatusFeedsService {
	@Autowired
	private StatusFeedsComponent statusFeedsComponent;

	@Override
	public VedantuResponse addStatusFeed(AddStatusFeedReq addStatusFeedReq) {
		AddStatusFeedRes reponse;
		try {
			reponse = statusFeedsComponent.addStatusFeed(addStatusFeedReq);

		} catch (VedantuException e) {

			throw e;
		}
		return new VedantuResponse(reponse);

	}
	@Override
	public VedantuResponse getStatusFeed(GetStatusFeedReq getStatusFeedReq) {
		GetStatusFeedRes reponse;
		try {
			reponse = statusFeedsComponent.getStatusFeed(getStatusFeedReq);

		} catch (VedantuException e) {

			throw e;
		}
		return new VedantuResponse(reponse);
	}
	@Override
	public VedantuResponse delete(DeleteStatusFeedReq deleteStatusFeedReq) {
		DeleteStatusFeedRes reponse;
		try {
			reponse = statusFeedsComponent.delete(deleteStatusFeedReq);

		} catch (VedantuException e) {

			throw e;
		}
		return new VedantuResponse(reponse);
	}

}
