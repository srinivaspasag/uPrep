package com.lms.models;

import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.Scope;
import com.lms.enums.EnumBasket;
import com.lms.enums.TestMode;
import com.lms.enums.TestResultVisibility;
import com.lms.interfaces.ICMDSModel;
import com.lms.models.tests.AbstractTestCommonModel;
import com.lms.pojos.CMDSAssignmentBasicInfo;
import com.lms.pojos.TestMetadata;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(value = "cmdassignments")
@CompoundIndexes(@CompoundIndex(name = "contentSrc, code"))
public class CMDSAssignment extends AbstractTestCommonModel implements ICMDSModel {

    public boolean publishingInProgress;
    public String globalId;

    public CMDSAssignment() {

        super();
        this.contentType = EntityType.CMDSASSIGNMENT;
    }

    public CMDSAssignment(String userId, String name, String desc, long duration,
                          List<TestMetadata> metadata, EnumBasket.TestType type, TestMode mode, String code, Scope scope,
                          String targetId, TestResultVisibility resultVisibility) {

        this(userId, name, desc, 0, duration, 0, metadata, type, mode, code, scope, targetId,
                resultVisibility);
    }

    public CMDSAssignment(String userId, String name, String desc, int qusCount, long duration,
                          int totalMarks, List<TestMetadata> metadata, EnumBasket.TestType type, TestMode mode, String code,
                          Scope scope, String targetId, TestResultVisibility resultVisibility) {

        super(userId, name, desc, qusCount, duration, totalMarks, metadata, type, mode, code,
                scope, resultVisibility);
        addTarget(targetId);
    }

    @Override
    public ModelBasicInfo toBasicInfo() {
        CMDSAssignmentBasicInfo info =
                new CMDSAssignmentBasicInfo(_getStringId(), name, contentSrc == null ? null
                        : contentSrc.id, timeCreated, lastUpdated, userId, 0, published, completed,
                        globalId, recordState, getExportableSize());
        info.duration = duration;
        info.totalMarks = totalMarks;
        info.qusCount = qusCount;
        info.totalMarks = totalMarks;
        return info;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{globalId:").append(globalId).append(", desc:").append(desc)
                .append(", qusCount:").append(qusCount).append(", duration:").append(duration)
                .append(", totalMarks:").append(totalMarks).append(", metadata:").append(metadata)
                .append(", type:").append(type).append(", mode:").append(mode).append(", code:")
                .append(code).append(", attempts:").append(attempts).append(", published:")
                .append(published).append(", childrenIds:").append(childrenIds)
                .append(", parentId:").append(parentId).append(", sets:").append(sets)
                .append(", upVotes:").append(upVotes).append(", views:").append(views)
                .append(", followers:").append(followers).append(", comments:").append(comments)
                .append(", shares:").append(shares).append(", userId:").append(userId)
                .append(", boardIds:").append(boardIds).append(", targetIds:").append(targetIds)
                .append(", difficulty:").append(difficulty).append(", contentSrc:")
                .append(contentSrc).append(", scope:").append(scope).append(", tags:").append(tags)
                .append(", name:").append(name).append(", id:").append(id).append(", timeCreated:")
                .append(timeCreated).append(", lastUpdated:").append(lastUpdated)
                .append(", recordState:").append(recordState).append("}");
        return builder.toString();
    }

    @Override
    public String getGlobalId() {


        return globalId;
    }

    @Override
    public long getExportableSize() {

        if (size != null) {
            return size.getTotalSize();
        }
        return 0;
    }

}
