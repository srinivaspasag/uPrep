package com.lms.pojos.requests;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.pojo.request.AbstractOrgScopeReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
public class MoveContentReq extends AbstractOrgScopeReq {

	@NotNull(message = "entities should not be empty")
	public List<SrcEntity> entities;
	@NotBlank(message = "targetFolderId should not be empty")
	public String targetFolderId;

}
