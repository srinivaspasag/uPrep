package com.lms.pojos.responce;

import com.lms.common.vedantu.commons.pojos.requests.responces.ListResponse;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.enums.EntityType;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GetUsageRes {

    public EntityType type;
    public ListResponse<ModelBasicInfo> data = new ListResponse<ModelBasicInfo>();
}
