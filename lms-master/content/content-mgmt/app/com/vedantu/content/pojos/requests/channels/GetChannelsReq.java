package com.vedantu.content.pojos.requests.channels;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractListReq;

public class GetChannelsReq extends AbstractListReq {

	@Required
	public String orgId;

	@Override
	public String toString() {
		return " [orgId=" + orgId + ", toString()=" + super.toString() + "]";
	}

}
