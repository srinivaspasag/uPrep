package com.lms.pojos.responce;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class GetMultiUsageRes {

    public List<GetUsageRes> usages = new ArrayList<GetUsageRes>();
    public boolean isUsed = true;

}
