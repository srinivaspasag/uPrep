package com.lms.pojos.requests;

import com.lms.common.vedantu.enums.UserRatingType;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class AddEntityInfoReq extends GetEntityInfoForAppReq {


    public UserRatingType rating;
    public String feedback;

}
