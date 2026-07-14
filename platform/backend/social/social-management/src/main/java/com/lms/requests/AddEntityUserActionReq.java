package com.lms.requests;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class AddEntityUserActionReq extends AbstractAuthCheckReq {

	@NotNull
	public SrcEntity entity;

	public SrcEntity context;
}
