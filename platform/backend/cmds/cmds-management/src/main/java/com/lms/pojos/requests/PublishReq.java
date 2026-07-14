package com.lms.pojos.requests;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
public class PublishReq extends AbstractAuthCheckReq {
	@NotBlank(message = "orgId should not be empty")
	public String orgId;
	@NotNull(message = "entities should not be empty")
	public List<SrcEntity> entities;

}
