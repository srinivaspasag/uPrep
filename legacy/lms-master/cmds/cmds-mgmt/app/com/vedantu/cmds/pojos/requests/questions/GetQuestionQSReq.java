package com.vedantu.cmds.pojos.requests.questions;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.vedantu.mongo.VedantuRecordState;

public class GetQuestionQSReq extends AbstractAuthCheckReq {
	@Required
	public SrcEntity			questionSet;
	public VedantuRecordState	state;
	@Required
	public int					start;
	public int					size;

}
