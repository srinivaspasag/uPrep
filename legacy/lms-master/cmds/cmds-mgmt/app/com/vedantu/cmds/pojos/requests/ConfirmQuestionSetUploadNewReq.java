package com.vedantu.cmds.pojos.requests;

import java.util.List;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class ConfirmQuestionSetUploadNewReq extends AbstractAuthCheckReq {

	public List<String>	questionIds;
	public String		questionsSetName;
	public String		filePrefix;
	public String		folderId;
	public String		orgId;
	public String		questionSetId;
	public boolean		shouldConfirm;

}
