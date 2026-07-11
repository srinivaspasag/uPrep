package com.lms.pojos;

import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.content.CMDSResourceInfo;
import com.lms.enums.QuestionStatus;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CMDSQuestionInfo extends CMDSResourceInfo implements IListResponseObj {

    public QuestionStatus status;
    public CMDSQuestionSearchIndexDetails detail;

    public CMDSQuestionInfo(String id, String name, String orgId, long timeCreated,
                            long lastUpdated, String addedBy, long programsAddedTo, boolean published,
                            boolean completed, boolean converted, String globalId, VedantuRecordState recordState) {

        this(id, name, orgId, timeCreated, lastUpdated, addedBy, programsAddedTo, published,
                completed, converted, globalId, recordState, 0);
    }

    public CMDSQuestionInfo(String id, String name, String orgId, long timeCreated,
                            long lastUpdated, String addedBy, long programsAddedTo, boolean published,
                            boolean completed, boolean converted, String globalId, VedantuRecordState recordState,
                            long size) {

        super(id, name, EntityType.CMDSQUESTION, orgId, timeCreated, lastUpdated, addedBy,
                programsAddedTo, published, completed, converted, globalId, recordState, size);
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{status:").append(status).append(", detail:").append(detail)
                .append(", published:").append(published).append(", globalId:").append(globalId)
                .append(", addedBy:").append(addedBy).append(", programsAddedTo:")
                .append(programsAddedTo).append(", type:").append(type).append(", name:")
                .append(name).append(", timeCreated:").append(timeCreated).append(", lastUpdated:")
                .append(lastUpdated).append(", id:").append(id).append(", recordState:")
                .append(recordState).append("}");
        return builder.toString();
    }

}
