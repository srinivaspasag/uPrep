package com.lms.models;

import com.lms.common.vedantu.commons.pojos.requests.Interval;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class Subscription {

    private String   planId;
    private Interval validity;

}
