package com.vedantu.content.pojos.tests;

import com.vedantu.user.pojos.UserInfo;

public class EntityTopper {

	public UserInfo user;
	public float percentage;

	public EntityTopper() {
	}

	public EntityTopper(UserInfo user, float percentage) {
		this.user = user;
		this.percentage = percentage;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{user:");
		builder.append(user);
		builder.append(", percentage:");
		builder.append(percentage);
		builder.append("}");
		return builder.toString();
	}

}
