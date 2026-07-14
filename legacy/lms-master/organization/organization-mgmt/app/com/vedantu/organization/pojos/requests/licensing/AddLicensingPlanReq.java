package com.vedantu.organization.pojos.requests.licensing;

import java.util.ArrayList;
import java.util.List;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

/**
 * 
 * @author vikram
 * 
 */
public class AddLicensingPlanReq extends AbstractAuthCheckReq {

    public AddLicensingPlanReq() {

        super();
    }

   
    @Required
    public String       name;

    public String       desc;
    @Required
    public boolean      peruser;
    public long         users;
    @Required
    public float        cost;
    public float        additionalCost;

    public List<String> features = new ArrayList<String>();

    public int          rank;
}
