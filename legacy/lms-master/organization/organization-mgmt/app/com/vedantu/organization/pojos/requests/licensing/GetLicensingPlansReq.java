package com.vedantu.organization.pojos.requests.licensing;

import java.util.List;

import com.vedantu.commons.pojos.requests.AbstractAppCheckReq;
import com.vedantu.organization.enums.PlanState;

public class GetLicensingPlansReq extends AbstractAppCheckReq {

    public List<String> planIds;
    public PlanState    state;
}
