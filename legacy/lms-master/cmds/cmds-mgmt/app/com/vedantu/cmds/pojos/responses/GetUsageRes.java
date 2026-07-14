package com.vedantu.cmds.pojos.responses;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.responses.ListResponse;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;

public class GetUsageRes {

    public EntityType                   type;
    public ListResponse<ModelBasicInfo> data = new ListResponse<ModelBasicInfo>();
}
