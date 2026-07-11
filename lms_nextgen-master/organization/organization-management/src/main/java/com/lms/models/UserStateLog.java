package com.lms.models;

import com.lms.common.vedantu.commons.pojos.requests.Interval;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(value = "userstatelogs")
@Getter
public class UserStateLog extends VedantuBaseMongoModel {

    public String orgId;
    public String userId;
    public String setByUserId;
    public Interval interval;
}
