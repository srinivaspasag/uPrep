package com.vedantu.board.pojos.requests;

import java.util.List;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class GetBoardBasicInfosReq extends AbstractAuthCheckReq {

	@Required
	public String orgId;
	@Required
	public List<String> brdIds;

}
