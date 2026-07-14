package com.vedantu.comm.requests.remarks;

import org.apache.commons.lang.StringUtils;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class GetRemarksReq extends AbstractAuthCheckReq {

	@Required
	public String	orgId;

	public String	targetUserId;

	public String	providerId;

	public int		start;
	public int		size;

	public boolean	sortAscending;

	@Override
	public String validate() {
		String superValidate = super.validate();
		if (null != superValidate) {
			return superValidate;
		}
		if (StringUtils.isNotEmpty(targetUserId)&& StringUtils.isNotEmpty(providerId)) {
			return "targetUserId / providerId missing";
		}
		if (null == orgId) {
			return "orgId missing";
		}
		return null;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public void setTargetUserId(String targetUserId) {
		this.targetUserId = targetUserId;
	}

	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public void setSortAscending(boolean sortAscending) {
		this.sortAscending = sortAscending;
	}

	public String getOrgId() {
		return orgId;
	}

	public String getTargetUserId() {
		return targetUserId;
	}

	public String getProviderId() {
		return providerId;
	}

	public int getStart() {
		return start;
	}

	public int getSize() {
		return size;
	}

	public boolean isSortAscending() {
		return sortAscending;
	}

}
