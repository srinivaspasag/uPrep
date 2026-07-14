package controllers;

import play.Logger;
import play.Logger.ALogger;
import play.data.Form;
import play.mvc.Result;

import com.vedantu.comm.managers.RemarksManager;
import com.vedantu.comm.requests.remarks.AddRemarksReq;
import com.vedantu.comm.requests.remarks.GetRemarksReq;
import com.vedantu.comm.response.remarks.AddRemarksRes;
import com.vedantu.comm.response.remarks.GetRemarksRes;
import com.vedantu.commons.JSONResponse;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;

public class Remarks extends AbstractVedantuController {
	private static ALogger LOGGER = Logger.of(Remarks.class);

	public static Result addRemark() {
		LOGGER.debug(" Called adding remarks ");

		AddRemarksReq addRemarksReq = null;
		AddRemarksRes addRemarkRes = null;

		try {
			Form<AddRemarksReq> addRemarkReqForm = Form.form(
					AddRemarksReq.class).bindFromRequest();

			LOGGER.debug("Request " + addRemarkReqForm.data());

			if (addRemarkReqForm.hasErrors()) {
				return ok(getErrorResponse(
						new VedantuException(
								VedantuErrorCode.MISSING_PARAMETERS))
						.toObjectNode());
			}
			addRemarksReq = addRemarkReqForm.get();
			addRemarkRes = RemarksManager.addRemark(addRemarksReq);

		} catch (VedantuException e) {

			return ok((new JSONResponse(e)).toObjectNode());
		}
		return ok(getResultResponse(addRemarkRes).toObjectNode());
	}

	public static Result getRemarksForUser() {
		LOGGER.debug(" Called getting remarks ");

		GetRemarksReq getRemarksReq = null;
		GetRemarksRes getRemarksRes = null;

		try {
			Form<GetRemarksReq> getRemarksReqForm = Form.form(
					GetRemarksReq.class).bindFromRequest();

			LOGGER.debug("Request " + getRemarksReqForm.data());

			if (getRemarksReqForm.hasErrors()) {
				LOGGER.debug("Request Error " + getRemarksReqForm.errorsAsJson());
				
				return ok(getErrorResponse(
						new VedantuException(
								VedantuErrorCode.MISSING_PARAMETERS))
						.toObjectNode());
			}
			getRemarksReq = getRemarksReqForm.get();
			getRemarksRes = RemarksManager.getRemarksForUser(getRemarksReq);

		} catch (VedantuException e) {

			return ok((new JSONResponse(e)).toObjectNode());
		}
		return ok(getResultResponse(getRemarksRes).toObjectNode());
	}
}
