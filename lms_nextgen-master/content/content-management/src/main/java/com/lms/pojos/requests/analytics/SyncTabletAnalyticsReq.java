package com.lms.pojos.requests.analytics;

import com.lms.pojos.requests.StartAttemptReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Getter
@Setter
public class SyncTabletAnalyticsReq extends StartAttemptReq {
    @NotBlank
    public long startTime;

    @NotBlank
    public long endTime;

    public List<RecordAttemptReq> qusAttemptReqs;

    public void prepareRecordQuestionAttemptReq(String attemptId) {

        if (qusAttemptReqs != null) {
            for (RecordAttemptReq attemptReq : qusAttemptReqs) {
                attemptReq.attemptId = attemptId;
                attemptReq.callingApp = callingApp;
                attemptReq.callingAppId = callingAppId;
                attemptReq.callingUserId = callingUserId;
                attemptReq.userId = userId;
                attemptReq.entityId = entityId;
                attemptReq.entityType = entityType;
                attemptReq.orgId = orgId;
            }
        }
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{startTime:").append(startTime).append(", endTime:").append(endTime)
                .append(", qusAttemptReqs:").append(qusAttemptReqs).append(", entityId:")
                .append(entityId).append(", entityType:").append(entityType).append(", setName:")
                .append(setName).append(", qIds:").append(qIds).append(", callingUserId:")
                .append(callingUserId).append(", userId:").append(userId).append(", callingApp:")
                .append(callingApp).append(", callingAppId:").append(callingAppId).append("}");
        return builder.toString();
    }

}
