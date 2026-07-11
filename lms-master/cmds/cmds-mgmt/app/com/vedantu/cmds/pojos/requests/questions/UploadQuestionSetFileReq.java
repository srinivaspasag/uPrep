package com.vedantu.cmds.pojos.requests.questions;

import java.io.File;

import play.data.validation.Constraints.Required;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class UploadQuestionSetFileReq extends AbstractAuthCheckReq {
	@Required
	public String	orgId;
	@Required
	public File		questionSetFile;

	public String	questionSetFileName;

	public UploadQuestionSetFileReq(MultipartFormData body) {
		super(body.asFormUrlEncoded());

		FilePart questionSetFilePart = body.getFile("questionSetFile");
		if (null != questionSetFilePart) {

			this.questionSetFile = questionSetFilePart.getFile();
			this.questionSetFileName= questionSetFilePart.getFilename();
		
		}
		orgId =_getValueFromMultipart( body.asFormUrlEncoded(), "orgId");
	}

	public String validate() {
		String superValidate = super.validate();
		if (null != superValidate) {
			return superValidate;
		}

		if (null == questionSetFile) {
			return "";
		}
		return null;
	}
}
