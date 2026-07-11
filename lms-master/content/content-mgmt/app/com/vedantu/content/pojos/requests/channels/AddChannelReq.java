package com.vedantu.content.pojos.requests.channels;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.enums.Scope;
import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class AddChannelReq extends AbstractOrgScopeReq {

	@Required
	public String name;
	public Scope scope;

	@Override
	public String toString() {
		return " [name=" + name + ", scope=" + scope + ", toString()="
				+ super.toString() + "]";
	}

}
