package com.vedantu.comm.requests;

import java.io.File;

import play.Logger;
import play.data.validation.Constraints.Required;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.requests.AbstractAppCheckReq;

public class UploadStatusFileReq extends AbstractAppCheckReq {
	@Required
	public String	orgId;
	@Required
	public EntityType	type;
	@Required
	public File		imageFile;
	

	public String		imageFileName;
	
	public UploadStatusFileReq(MultipartFormData body) {
		super(body.asFormUrlEncoded());

		FilePart imageFilePart = body.getFile("imageFile");
		if (null != imageFilePart) {

			this.imageFile = imageFilePart.getFile();
			this.imageFileName = imageFilePart.getFilename();
		}
		type = EntityType.valueOfKey( _getValueFromMultipart(body.asFormUrlEncoded(),"type"));
	}

	public String validate() {
		String superValidate = super.validate();
		if (null != superValidate) {
			return superValidate;
		}
		

		if (null == imageFile) {
			Logger.info("Uploaded file "+ imageFileName);
			return "iamgeFile missing";
		}
		return null;
	}
}
