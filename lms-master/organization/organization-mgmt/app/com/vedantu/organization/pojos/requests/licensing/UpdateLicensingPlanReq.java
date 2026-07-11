package com.vedantu.organization.pojos.requests.licensing;

import play.data.validation.Constraints.Required;

/**
 * 
 * @author vikram
 * 
 */
public class UpdateLicensingPlanReq extends AddLicensingPlanReq {

    public UpdateLicensingPlanReq() {

        super();
    }

    @Required
    public String planId;

}
