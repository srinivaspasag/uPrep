package com.vedantu.user.pojos;

import org.apache.commons.lang3.StringUtils;

import play.data.validation.Constraints.Required;

public class UserBasicInfo {

	@Required
	private String email;
	@Required
	public String firstName;
	public String lastName;
	public String contactNumber;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = StringUtils.lowerCase(email);
	}

	public String validate() {
		if (null == email) {
			return "email missing";
		}
		if (null == firstName) {
			return "firstName missing";
		}
		return null;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("UserBasicInfo [email=");
		builder.append(email);
		builder.append(", firstName=");
		builder.append(firstName);
		builder.append(", lastName=");
		builder.append(lastName);
		builder.append(", contactNumber=");
		builder.append(contactNumber);
		builder.append("]");
		return builder.toString();
	}

}
