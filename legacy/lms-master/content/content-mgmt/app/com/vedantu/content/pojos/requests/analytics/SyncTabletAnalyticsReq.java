package com.vedantu.content.pojos.requests.analytics;

import java.util.List;

import play.data.validation.Constraints.Required;

public class SyncTabletAnalyticsReq extends StartAttemptReq {

    @Required
    public long                   startTime;

    @Required
    public long                   endTime;

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
