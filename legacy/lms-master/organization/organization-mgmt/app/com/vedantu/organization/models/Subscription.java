package com.vedantu.organization.models;

import com.google.code.morphia.annotations.Embedded;
import com.vedantu.commons.pojos.Interval;

@Embedded
public class Subscription {

    public String   planId;
    public Interval validity;

}
