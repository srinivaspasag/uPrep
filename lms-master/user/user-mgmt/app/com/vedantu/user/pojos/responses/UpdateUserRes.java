package com.vedantu.user.pojos.responses;

import com.vedantu.commons.pojos.responses.ModelBasicInfo;

public class UpdateUserRes {

	public ModelBasicInfo info;
	public String username;

	public UpdateUserRes(ModelBasicInfo info) {
		this.info = info;
	}

}
