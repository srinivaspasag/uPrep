package com.vedantu.cmds.pojos.content.question;

import com.vedantu.cmds.models.event.search.details.CMDSQuestionSearchIndexDetails;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.responses.IListResponseObj;
import com.vedantu.content.enums.QuestionStatus;
import com.vedantu.mongo.VedantuRecordState;

public class CMDSQuestionInfo extends CMDSResourceInfo implements IListResponseObj {

    public QuestionStatus                 status;
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
