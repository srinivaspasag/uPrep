package com.lms.pojos.requests;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
@Getter
@Setter
public class GetEntityReviewsReq extends GetEntityInfoForAppReq{
    @NotNull
    public int size;
    @NotNull
    public int start;

    public String ratingType;
}
