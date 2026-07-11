package controllers;

import play.data.Form;
import play.mvc.Http.MultipartFormData;
import play.mvc.Result;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.content.managers.ImageUploadManager;
import com.vedantu.content.pojos.requests.UploadImageReq;
import com.vedantu.content.pojos.responses.UploadImageRes;

public class Uploads extends AbstractVedantuController {
	/**
	 * @return this api will be used to upload all images for
	 *         questions/discussion/solution and will be saved in tempFs,
	 *         confirmation call will move to corresponding bucket in
	 *         FileStorage
	 */
	public static Result uploadImage() {
		Form<UploadImageReq> uploadImageForm = Form.form(UploadImageReq.class)
				.bindFromRequest();
		if (uploadImageForm.hasErrors()) {
			return ok(getErrorResponse(
					new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
							getErrorMessege(uploadImageForm))).toObjectNode());
		}
		UploadImageReq uploadImageReq = getUploadOfflineTestResultReq();
		UploadImageRes uploadImageRes = null;
		try {
		    uploadImageReq.folder = "content";
			uploadImageRes = ImageUploadManager
					.uploadContentImage(uploadImageReq);
		} catch (VedantuException e) {
			return ok(getErrorResponse(e).toObjectNode());
		}
		return ok(getResultResponse(uploadImageRes).toObjectNode());
	}

	private static UploadImageReq getUploadOfflineTestResultReq() {
		MultipartFormData body = request().body().asMultipartFormData();
		UploadImageReq uploadTestResultReq = new UploadImageReq(body);
		return uploadTestResultReq;
	}
}
