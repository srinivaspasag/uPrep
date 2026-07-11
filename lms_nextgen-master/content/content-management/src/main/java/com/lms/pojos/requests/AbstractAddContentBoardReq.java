package com.lms.pojos.requests;


import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Getter
@Setter
public class AbstractAddContentBoardReq extends AbstractAddContentReq {
	@NotNull
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

