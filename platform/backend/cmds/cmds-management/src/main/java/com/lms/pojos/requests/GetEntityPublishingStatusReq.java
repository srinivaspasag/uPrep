package com.lms.pojos.requests;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetEntityPublishingStatusReq extends AbstractAuthCheckReq {

	public String orgId;
	public List<String> jobIds;

}
