package com.vedantu.organization.pojos;

import java.util.List;

import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.organization.enums.PlanState;

public class LicensingInfo extends ModelBasicInfo {

    public String       name;
    public String       desc;
    public boolean      peruser;
    public float        cost;
    public boolean      isMonthly;
    public long         users;
    public List<String> features;
    public float        additionalCost;
    public PlanState    planState;

    public LicensingInfo() {

    }

    public LicensingInfo(String id, VedantuRecordState state) {

        super(id, state);
    }

    @Override
    public String toString() {

        return "LicensingInfo [name=" + name + ", desc=" + desc + ", peruser=" + peruser
                + ", cost=" + cost + ", isMonthly=" + isMonthly + ", users=" + users
                + ", features=" + features + ", additionalCost=" + additionalCost + "]";
    }

}
