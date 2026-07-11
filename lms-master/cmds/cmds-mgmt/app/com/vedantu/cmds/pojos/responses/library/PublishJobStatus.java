package com.vedantu.cmds.pojos.responses.library;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.pojos.responses.IListResponseObj;

public class PublishJobStatus implements IListResponseObj {

	public String jobId;
	public VedantuErrorCode errorCode;

	public PublishJobStatus() {
		super();
	}

	public PublishJobStatus(String jobId, VedantuErrorCode errorCode) {
		super();
		this.jobId = jobId;
		this.errorCode = errorCode;
	}

}
