package com.lms.pojos.responses;

import com.lms.common.vedantu.commons.pojos.requests.responces.ListResponse;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetResourcesRes extends ListResponse<ModelBasicInfo> {
	public ModelBasicInfo folderInfo;
	public int paraHits;
	public int otherHits;
	public String otherType;
	public int nonParaHits;
}
