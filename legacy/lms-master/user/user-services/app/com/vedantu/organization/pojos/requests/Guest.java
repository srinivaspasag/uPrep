package com.vedantu.organization.pojos.requests;

import play.data.validation.Constraints.Required;

public class Guest {

	@Required
	public String firstName;
	public String lastName;
}
