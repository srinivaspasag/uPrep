package com.lms.pojos;

import com.lms.common.vedantu.commons.pojos.requests.CostRate;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConsumableItemInfo {
    public SrcEntity entity;
    public long verifiedTime;
    public boolean verified;
    public CostRate costRate;
    public ModelBasicInfo info;
}
