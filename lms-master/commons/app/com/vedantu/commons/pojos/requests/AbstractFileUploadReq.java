package com.vedantu.commons.pojos.requests;

import java.io.File;

import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;

public abstract class AbstractFileUploadReq extends AbstractAuthCheckReq {

	public String fileName;
	public String contentType;
	public File inputFile;

	public AbstractFileUploadReq(MultipartFormData body) {
		super(body.asFormUrlEncoded());

		FilePart inputFilePart = body.getFile("inputFile");
		if (null != inputFilePart) {
			this.fileName = inputFilePart.getFilename();
			this.contentType = inputFilePart.getContentType();
			this.inputFile = inputFilePart.getFile();
		}
	}

	public String validate() {
		String superValidate = super.validate();
		if (null != superValidate) {
			return superValidate;
		}
		if (null == fileName) {
			return "fileName missing";
		}
		if (null == contentType) {
			return "contentType missing";
		}
		if (null == inputFile) {
			return "inputFile missing";
		}
		return null;
	}

}
