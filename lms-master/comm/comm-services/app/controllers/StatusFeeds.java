package controllers;

import play.Logger;
import play.Logger.ALogger;
import play.data.Form;
import play.mvc.Http.MultipartFormData;
import play.mvc.Result;

import com.vedantu.comm.managers.StatusFeedManager;
import com.vedantu.comm.requests.AddStatusFeedReq;
import com.vedantu.comm.requests.DeleteStatusFeedReq;
import com.vedantu.comm.requests.GetStatusFeedReq;
import com.vedantu.comm.requests.UploadStatusFileReq;
import com.vedantu.commons.JSONResponse;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.pojos.response.DeleteStatusFeedRes;
import com.vedantu.pojos.response.GetStatusFeedRes;
import com.vedantu.pojos.response.newsfeed.AddStatusFeedRes;
import com.vedantu.pojos.response.newsfeed.UploadStatusFileRes;

public class StatusFeeds extends AbstractVedantuController {
	private static ALogger	LOGGER	= Logger.of(StatusFeeds.class);

	@Deprecated
	public static Result uploadFile() {
		LOGGER.debug(" Called uploadTest");
		MultipartFormData body = request().body().asMultipartFormData();
		UploadStatusFileReq request = new UploadStatusFileReq(body);

		UploadStatusFileRes uploadImageFileResponse = null;

		try {

			if (request.validate() != null) {
				return ok(getErrorResponse(
						new VedantuException(
								VedantuErrorCode.MISSING_PARAMETERS))
						.toObjectNode());
			}

			uploadImageFileResponse = StatusFeedManager.INSTANCE
					.uploadImageTest(request);

		} catch (VedantuException e) {

			return ok((new JSONResponse(e)).toObjectNode());
		}
		return ok(getResultResponse(uploadImageFileResponse).toObjectNode());
	}

	public static Result addStatusFeed() {
		LOGGER.debug(" Called addStatusFeed");

		AddStatusFeedReq request = null;
		AddStatusFeedRes reponse = null;

		try {

			Form<AddStatusFeedReq> requestForm = Form.form(
					AddStatusFeedReq.class).bindFromRequest();
			request = requestForm.get();
			LOGGER.debug(" StautsFeed request" + requestForm.data());
			if (request.validate() != null) {

				return ok(getErrorResponse(
						new VedantuException(
								VedantuErrorCode.MISSING_PARAMETERS))
						.toObjectNode());
			}

			reponse = StatusFeedManager.INSTANCE.addStatusFeed(request);

		} catch (VedantuException e) {

			return ok((new JSONResponse(e)).toObjectNode());
		}
		return ok(getResultResponse(reponse).toObjectNode());

	}

	public static Result getStatusFeed() {
		LOGGER.debug(" Called addStatusFeed");

		GetStatusFeedReq request = null;
		GetStatusFeedRes reponse = null;

		try {

			Form<GetStatusFeedReq> requestForm = Form.form(
					GetStatusFeedReq.class).bindFromRequest();
			request = requestForm.get();
			LOGGER.debug(" StautsFeed request" + requestForm.data());
			if (request.validate() != null) {

				return ok(getErrorResponse(
						new VedantuException(
								VedantuErrorCode.MISSING_PARAMETERS))
						.toObjectNode());
			}

			reponse = StatusFeedManager.INSTANCE.getStatusFeed(request);

		} catch (VedantuException e) {

			return ok((new JSONResponse(e)).toObjectNode());
		}
		return ok(getResultResponse(reponse).toObjectNode());

	}

	public static Result delete() {
		LOGGER.debug(" Called addStatusFeed");

		DeleteStatusFeedReq request = null;
		DeleteStatusFeedRes reponse = null;

		try {

			Form<DeleteStatusFeedReq> requestForm = Form.form(
					DeleteStatusFeedReq.class).bindFromRequest();
			request = requestForm.get();
			LOGGER.debug(" StautsFeed request" + requestForm.data());
			if (request.validate() != null) {

				return ok(getErrorResponse(
						new VedantuException(
								VedantuErrorCode.MISSING_PARAMETERS))
						.toObjectNode());
			}

			reponse = StatusFeedManager.INSTANCE.delete(request);

		} catch (VedantuException e) {

			return ok((new JSONResponse(e)).toObjectNode());
		}
		return ok(getResultResponse(reponse).toObjectNode());

	}

}
