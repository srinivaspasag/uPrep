package com.vedantu.cmds.pojos.requests;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.vedantu.content.pojos.tests.Metadata;

public class ConfirmQuestionSetUploadReq extends AbstractAuthCheckReq {

	public String	questions;
	public Metadata	metdata;
	public String	questionsSetName;
	public String	filePrefix;
	public String	folderId;
	public String	orgId;

}
