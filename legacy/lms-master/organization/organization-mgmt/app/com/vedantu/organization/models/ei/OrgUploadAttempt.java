package com.vedantu.organization.models.ei;

import java.util.ArrayList;
import java.util.List;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexes;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.organization.enums.UploadState;

@Entity(value = "orguploadattempts", noClassnameStored = true)
@Indexes({ @Index(value = "uploadId"), @Index(value = "orgId, entity, userId") })
public class OrgUploadAttempt extends VedantuBaseMongoModel {

    public String       uploadId;
    public String       attemptId;      // _id of UserEntityAttempt
    public String       userId;
    public SrcEntity    entity;
    public UploadState  state;
    public int          httpStatus;
    public int          tryCount;
    public String       orgId;
    public String       errorCode;
    public long         uploadStartTime;

    // whenever a failed event happen it's uploadId is pushed to this list
    public List<String> failedUploadIds;

    public void addFailedUploadId(String uploadId) {

        if (this.failedUploadIds == null) {
            this.failedUploadIds = new ArrayList<String>();
        }
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{uploadId:").append(uploadId).append(", attemptId:").append(attemptId)
                .append(", userId:").append(userId).append(", entity:").append(entity)
                .append(", status:").append(state).append(", httpStatus:").append(httpStatus)
                .append(", tryCount:").append(tryCount).append(", orgId:").append(orgId)
                .append(", errorCode:").append(errorCode).append(", uploadStartTime:")
                .append(uploadStartTime).append(", failedUploadIds:").append(failedUploadIds)
                .append(", id:").append(id).append(", timeCreated:").append(timeCreated)
                .append(", lastUpdated:").append(lastUpdated).append(", recordState:")
                .append(recordState).append("}");
        return builder.toString();
    }

}
