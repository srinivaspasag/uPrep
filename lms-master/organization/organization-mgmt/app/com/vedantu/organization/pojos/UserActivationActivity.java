package com.vedantu.organization.pojos;

import play.data.validation.Constraints.Required;



public class UserActivationActivity {

	@Required
	public String userName;

	@Required
	public UserState userState = UserState.ACTIVE;

	public enum UserState {
	    ACTIVE, BLOCKED 
	}

	@Override
	public String toString() {
	/*	StringBuilder builder = new StringBuilder();
		builder.append("Location [country=");
		builder.append(country);
		builder.append(", state=");
		builder.append(state);
		builder.append(", city=");
		builder.append(city);
		builder.append("]");
		return builder.toString();*/
	    return null;
	}

}
