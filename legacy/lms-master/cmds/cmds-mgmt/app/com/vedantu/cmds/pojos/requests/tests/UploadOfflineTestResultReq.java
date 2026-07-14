package com.vedantu.cmds.pojos.requests.tests;

import java.io.File;
import java.util.Map;

import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;

import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class UploadOfflineTestResultReq extends AbstractAuthCheckReq {

	public File resultFile;

	public String orgId;

	public String programId;

	public String testId;

	public boolean merge;

	public String targetId;

	public String targetType;

	public UploadOfflineTestResultReq() {
		super();
	}

	public UploadOfflineTestResultReq(Map<String, String[]> form) {
		super(form);
	}

	public UploadOfflineTestResultReq(MultipartFormData body) {
		super(body.asFormUrlEncoded());
		FilePart resultFilePart = body.getFile("resultFile");
		if (null != resultFilePart) {
			this.resultFile = resultFilePart.getFile();
		}
		this.orgId = _getValueFromMultipart(body.asFormUrlEncoded(),
				ConstantsGlobal.ORG_ID);
		this.programId = _getValueFromMultipart(body.asFormUrlEncoded(),
				ConstantsGlobal.PROGRAM_ID);
		this.testId = _getValueFromMultipart(body.asFormUrlEncoded(),
                ConstantsGlobal.TEST_ID);
		this.targetId = _getValueFromMultipart(body.asFormUrlEncoded(),
                ConstantsGlobal.TARGET_ID);
		this.targetType = _getValueFromMultipart(body.asFormUrlEncoded(),
                ConstantsGlobal.TARGET_TYPE);
		this.merge = Boolean.valueOf(_getValueFromMultipart(
				body.asFormUrlEncoded(), "merge"));
	}
}
