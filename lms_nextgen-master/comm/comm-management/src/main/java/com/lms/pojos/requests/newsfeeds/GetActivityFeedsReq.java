package com.lms.pojos.requests.newsfeeds;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.lms.common.vedantu.enums.EntityType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
public class GetActivityFeedsReq extends AbstractAuthCheckReq {

	@NotNull
	public EntityType eType;

	@NotBlank
	public String eId;

	@NotNull
	public int size;
	public boolean needClustered;
	public List<String> userActions;
	public String orgId;

}
