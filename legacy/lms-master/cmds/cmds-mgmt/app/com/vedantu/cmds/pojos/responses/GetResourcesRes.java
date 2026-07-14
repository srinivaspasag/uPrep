package com.vedantu.cmds.pojos.responses;

import com.vedantu.commons.pojos.responses.ListResponse;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;

public class GetResourcesRes extends ListResponse<ModelBasicInfo> {
	public ModelBasicInfo	folderInfo;
    public int paraHits;
    public int otherHits;
    public String otherType;
    public int nonParaHits;
}
