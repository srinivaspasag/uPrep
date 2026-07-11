package com.lms.pojo.request;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class AddLicensingPlanReq extends AbstractAuthCheckReq {

    public AddLicensingPlanReq() {

        super();
    }


    @NotBlank(message = "name should not be null")
    public String       name;

    public String       desc;
    public boolean      peruser;
    public long         users;
    @NotBlank(message = "cost should not be null")
    public float        cost;
    public float        additionalCost;

    public List<String> features = new ArrayList<String>();

    public int          rank;
}
