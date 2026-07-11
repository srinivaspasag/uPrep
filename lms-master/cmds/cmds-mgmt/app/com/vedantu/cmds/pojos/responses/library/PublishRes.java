package com.vedantu.cmds.pojos.responses.library;

import java.util.HashMap;
import java.util.Map;

import com.vedantu.commons.VedantuErrorCode;

public class PublishRes {

	public Map<String, PublishJobStatus> info;

	public PublishRes() {
		super();
		info = new HashMap<String, PublishJobStatus>();
	}

	public void addStatus(String entityId, String jobId) {
		addStatus(entityId, jobId, null);
	}

	public void addStatus(String entityId, String jobId,
			VedantuErrorCode errorCode) {
		info.put(entityId, new PublishJobStatus(jobId, errorCode));
	}

}
