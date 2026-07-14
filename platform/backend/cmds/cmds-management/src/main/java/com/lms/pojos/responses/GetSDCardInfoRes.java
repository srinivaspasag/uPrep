package com.lms.pojos.responses;

import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetSDCardInfoRes implements IListResponseObj {
    public ModelBasicInfo recordInfo;

}
