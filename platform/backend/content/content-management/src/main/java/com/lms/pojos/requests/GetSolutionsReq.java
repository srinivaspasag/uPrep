package com.lms.pojos.requests;

import com.lms.common.vedantu.commons.pojos.requests.AbstractOrgListReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class GetSolutionsReq extends AbstractOrgListReq {

	@NotBlank(message = "qId should not be null")
	public String qId;
}
