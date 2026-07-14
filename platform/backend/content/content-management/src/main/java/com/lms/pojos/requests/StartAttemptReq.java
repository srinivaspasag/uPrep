package com.lms.pojos.requests;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.EntityType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class StartAttemptReq extends AbstractAuthCheckReq {
    @NotBlank
    public String entityId;
    @NotBlank
    public EntityType entityType;
    public SrcEntity target = new SrcEntity();
    public String orgId;
    public String studentUserId;
    public String sectionId;

    public String setName; // this field is optional--> will be used when there
    // are multiple sets for a test

    public List<String> qIds = new ArrayList<String>();// if qids are provide than qids order will not be
    // computed, this will be helpfull for challenge

    public String attemptId; // This field is for other POJOS
    public long timeLeft;
    public String testState;

    public StartAttemptReq() {
        super();
    }

    public StartAttemptReq(String callingUserId, String userId,
                           String entityId, EntityType entityType, String setName) {
        super(callingUserId, userId);
        this.entityId = entityId;
        this.entityType = entityType;
        this.setName = setName;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{entityId:");
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
