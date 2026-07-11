package com.vedantu.cmds.models;

import java.util.List;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexes;
import com.vedantu.cmds.mgmt.interfaces.ICMDSModel;
import com.vedantu.cmds.pojos.content.tests.CMDSAssignmentBasicInfo;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.content.enums.EnumBasket.TestType;
import com.vedantu.content.enums.TestMode;
import com.vedantu.content.enums.TestResultVisibility;
import com.vedantu.content.models.tests.AbstractTestCommonModel;
import com.vedantu.content.pojos.tests.TestMetadata;

@Entity(value = "cmdassignments", noClassnameStored = true)
@Indexes(@Index(value = "contentSrc, code"))
public class CMDSAssignment extends AbstractTestCommonModel implements ICMDSModel {

    public boolean publishingInProgress;
    public String globalId;

    public CMDSAssignment() {

        super();
        this.contentType = EntityType.CMDSASSIGNMENT;
    }

    public CMDSAssignment(String userId, String name, String desc, long duration,
            List<TestMetadata> metadata, TestType type, TestMode mode, String code, Scope scope,
            String targetId, TestResultVisibility resultVisibility) {

        this(userId, name, desc, 0, duration, 0, metadata, type, mode, code, scope, targetId,
                resultVisibility);
    }

    public CMDSAssignment(String userId, String name, String desc, int qusCount, long duration,
            int totalMarks, List<TestMetadata> metadata, TestType type, TestMode mode, String code,
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
                globalId, recordState,getExportableSize());
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
