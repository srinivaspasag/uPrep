package com.lms.pojo.request;

import com.lms.enums.PlanState;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class GetLicensingPlansReq {
    public List<String> planIds;
    public PlanState state;
}
