package com.vedantu.organization.models.ei;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexes;
import com.vedantu.mongo.VedantuBaseMongoModel;

@Entity(value = "extorgrequests", noClassnameStored = true)
@Indexes(@Index(value = "orgId,url"))
public class ExtOrgRequestModel extends VedantuBaseMongoModel {

    public String orgId;
    public String url;
    public String request;
    public String response;
    public long   endTime;
    public int    responseTime; // millis taken to complete this request
    public int    responseCode; // http status 200==OK, 404=NOT_FOUNT etc

    public ExtOrgRequestModel() {

        super();
    }

    public ExtOrgRequestModel(String orgId, String url, String request) {

        this(orgId, url, request, null, 0, 0, 0);

    }

    public ExtOrgRequestModel(String orgId, String url, String request, String response,
            long endTime, int responseTime, int responseCode) {

        super();
        this.orgId = orgId;
        this.url = url;
        this.request = request;
        this.response = response;
        this.endTime = endTime;
        this.responseTime = responseTime;
        this.responseCode = responseCode;
    }

}
