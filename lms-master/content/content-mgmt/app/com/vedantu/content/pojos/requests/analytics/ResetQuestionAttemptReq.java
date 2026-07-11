package com.vedantu.content.pojos.requests.analytics;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class ResetQuestionAttemptReq extends AbstractAuthCheckReq {

    @Required
    public String attemptId;

    @Required
    public String qId;

    public EntityType entityType;

    public String entityId;

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{attemptId:").append(attemptId).append(", qId:").append(qId)
                .append(", callingUserId:").append(callingUserId).append(", userId:")
                .append(userId).append(", callingApp:").append(callingApp)
                .append(", callingAppId:").append(callingAppId).append("}");
        return builder.toString();
    }

}
