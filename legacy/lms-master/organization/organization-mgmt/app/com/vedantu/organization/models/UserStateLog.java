package com.vedantu.organization.models;

import com.google.code.morphia.annotations.Entity;
import com.vedantu.commons.pojos.Interval;
import com.vedantu.mongo.VedantuBaseMongoModel;

@Entity(value = "userstatelogs", noClassnameStored = true)

public class UserStateLog extends VedantuBaseMongoModel {

    public String         orgId;
    public String         userId;
    public String         setByUserId;
    public Interval       interval;
}
