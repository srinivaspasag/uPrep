package com.vedantu.content.pojos.requests;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import play.data.validation.Constraints.Required;

public class AbstractAddContentBoardReq extends AbstractAddContentReq{
	@Required
	public List<String> brdIds;
	public List<String> targetIds;
	
	
	public Set<String> _getAllBoardIds() {
		Set<String> brdIds = new HashSet<String>();
		if (this.brdIds != null) {
			brdIds.addAll(this.brdIds);
		}
		if (this.targetIds != null) {
			brdIds.addAll(this.targetIds);
		}
		return brdIds;
	}
}
