package com.lms.pojos.requests.analytics;

import com.lms.common.vedantu.enums.EntityType;
import com.lms.pojos.requests.StartAttemptReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class EndAttemptReq extends StartAttemptReq {
    @NotBlank(message = "attemptid should not be empty")
    public String attemptId;

    public EndAttemptReq() {
        super();
    }

    public EndAttemptReq(String callingUserId, String userId, String entityId,
                         EntityType entityType, String setName, String attemptId, String orgId) {
        super(callingUserId, userId, entityId, entityType, setName);
        this.attemptId = attemptId;
        this.orgId = orgId;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{attemptId:");
        builder.append(attemptId);
        builder.append(", entityId:");
        builder.append(entityId);
        builder.append(", entityType:");
        builder.append(entityType);
        builder.append(", setName:");
        builder.append(setName);
        builder.append(", callingUserId:");
        builder.append(callingUserId);
        builder.append(", userId:");
        builder.append(userId);
        builder.append(", callingApp:");
        builder.append(callingApp);
        builder.append(", callingAppId:");
        builder.append(callingAppId);
        builder.append("}");
        return builder.toString();
    }
}
