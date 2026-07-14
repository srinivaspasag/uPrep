package com.vedantu.cmds.pojos.requests.library;

import java.util.List;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class PublishReq extends AbstractAuthCheckReq {
	@Required 
	public String orgId;
	@Required 
	public List<SrcEntity> entities;

}
