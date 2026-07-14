package com.vedantu.user.pojos.requests;

import org.apache.commons.lang3.StringUtils;

import play.data.validation.Constraints.Required;

public class UpdateUsernameReq extends UpdateUserPasswordReq {
	@Required
	private String newUsername;

	public String getNewUsername() {
		return newUsername;
	}

	public void setNewUsername(String newUsername) {
		this.newUsername = StringUtils.lowerCase(newUsername);
	}

}
