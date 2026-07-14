package com.vedantu.organization.models.ei;

import org.json.JSONObject;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexes;
import com.vedantu.mongo.VedantuBaseMongoModel;

@Entity(value = "orguploadattemptrequeststatus", noClassnameStored = true)
@Indexes({ @Index(value = "uploadId", unique = true), @Index(value = "orgId") })
public class OrgUploadAttemptRequestStatus extends VedantuBaseMongoModel {

    public String     orgId;
    public String     uploadId;
    public JSONObject request;
    public JSONObject response;
    public long       endTime;
    public int        responseTime; // millis taken to complete this request
    public int        responseCode; // http status 200==OK, 404=NOT_FOUNT etc

    public OrgUploadAttemptRequestStatus() {

        super();
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{orgId:").append(orgId).append(", uploadId:").append(uploadId)
                .append(", request:").append(request).append(", response:").append(response)
                .append(", endTime:").append(endTime).append(", responseTime:")
                .append(responseTime).append(", responseCode:").append(responseCode)
                .append(", id:").append(id).append(", timeCreated:").append(timeCreated)
                .append(", lastUpdated:").append(lastUpdated).append(", recordState:")
                .append(recordState).append("}");
        return builder.toString();
    }

}
