package com.vedantu.organization.pojos;

import org.apache.commons.lang3.StringUtils;

import play.data.validation.Constraints.Required;

public class MemberParentInfo {
	public transient static final String FIELD_NAME = "name";
	public transient static final String FIELD_CONTACTNUMBER = "contactNumber";
	public transient static final String FIELD_EMAIL = "email";
	@Required
	public String name;
	public String contactNumber;
	public String email;

	public String validate() {
		if (StringUtils.isEmpty(name)) {
			return "member parent name is null/empty";
		}
		return null;
	}

	@Override
	public String toString() {
		return "MemberParentInfo [name=" + name + ", contactNumber="
				+ contactNumber + ", email=" + email + "]";
	}

}
