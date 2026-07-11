package com.lms.billing.pojo;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CustomerInfo extends SrcEntity {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public ModelBasicInfo info;

}
