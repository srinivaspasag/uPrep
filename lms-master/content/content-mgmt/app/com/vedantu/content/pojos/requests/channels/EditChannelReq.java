package com.vedantu.content.pojos.requests.channels;

import play.data.validation.Constraints.Required;

public class EditChannelReq extends AddChannelReq {

	@Required
	public String id;

	@Override
	public String toString() {
		return " [id=" + id + ", toString()=" + super.toString() + "]";
	}

}
