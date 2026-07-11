package com.lms.pojo;

import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.enums.PlanState;
import com.lms.models.LicensingPlan;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class LicensingInfo extends ModelBasicInfo {

    public PlanState planState;
    public String              name;
    public String              desc;
    public boolean             peruser;
    public float               cost;
    public float               additionalCost;

    public long                users;
    public List<String> features;
    public PlanState state;
    public int                 rank;
    public boolean      isMonthly;


    public LicensingInfo() {

    }

    public LicensingInfo(String id, VedantuRecordState state) {

        super(id, state);
    }
    public  LicensingInfo(LicensingPlan licensingPlan){
        this.name = licensingPlan.getName();
        this.desc = licensingPlan.getDesc();
        this.peruser = licensingPlan.isPeruser();
        this.cost = licensingPlan.getCost();
        this.users = licensingPlan.getUsers();
        this.features = licensingPlan.getFeatures();
        this.additionalCost = licensingPlan.getAdditionalCost();
        this.state = PlanState.DRAFT;

    }

    @Override
    public String toString() {

        return "LicensingInfo [name=" + name + ", desc=" + desc + ", peruser=" + peruser
                + ", cost=" + cost + ", isMonthly=" + isMonthly + ", users=" + users
                + ", features=" + features + ", additionalCost=" + additionalCost + "]";
    }


}
