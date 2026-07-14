package com.vedantu.content.pojos.requests;

import java.util.List;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class GetContentStateReq extends AbstractAuthCheckReq{
	
	
	@Required
	public String sectionId;
	
	@Required
	public String orgId;
	
	public List<String>     brdIds;
	
	@Required
	public String programId;
	
	@Required
	public String centreId;
	

}
