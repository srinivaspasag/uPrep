package com.lms.pojos.requests.analytics;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.lms.common.vedantu.enums.EntityType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class ResetQuestionAttemptReq extends AbstractAuthCheckReq {

    @NotBlank(message = "attemptId should not be empty")
    public String attemptId;

    @NotBlank(message = "qId should not be empty")
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
