package com.lms.requests.remarks;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class GetRemarksReq extends AbstractAuthCheckReq {

	@NotBlank
	public String orgId;

	public String targetUserId;

	public String providerId;

	public int start;
	public int size;

	public boolean sortAscending;

	/*@Override
	public String validate() {
		String superValidate = super.validate();
		if (null != superValidate) {
			return superValidate;
		}
		if (StringUtils.isNotEmpty(targetUserId) && StringUtils.isNotEmpty(providerId)) {
			return "targetUserId / providerId missing";
		}
		if (null == orgId) {
			return "orgId missing";
		}
		return null;
	}*/

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getTargetUserId() {
		return targetUserId;
	}

	public void setTargetUserId(String targetUserId) {
		this.targetUserId = targetUserId;
	}

	public String getProviderId() {
		return providerId;
	}

	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public boolean isSortAscending() {
		return sortAscending;
	}

	public void setSortAscending(boolean sortAscending) {
		this.sortAscending = sortAscending;
	}

}
