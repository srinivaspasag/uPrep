package com.vedantu.content.pojos.requests.socials;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class AddEntityUserActionReq extends AbstractAuthCheckReq {

	@Required
	public SrcEntity entity;
    
    public SrcEntity context;
}
