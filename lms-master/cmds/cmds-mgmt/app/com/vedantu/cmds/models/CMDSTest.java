package com.vedantu.cmds.models;

import java.util.List;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexes;
import com.vedantu.cmds.mgmt.interfaces.ICMDSModel;
import com.vedantu.cmds.pojos.content.tests.CMDSTestBasicInfo;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.content.enums.EnumBasket.TestType;
import com.vedantu.content.enums.TestMode;
import com.vedantu.content.enums.TestResultVisibility;
import com.vedantu.content.models.tests.AbstractTestCommonModel;
import com.vedantu.content.pojos.tests.TestMetadata;

@Entity(value = "cmdstests", noClassnameStored = true)
@Indexes(@Index(value = "contentSrc, code"))
public class CMDSTest extends AbstractTestCommonModel implements ICMDSModel {

    public boolean publishingInProgress;
    public String globalTestId;

    // public List<TestMetadata> currentMetadata;

    public CMDSTest() {

        super();
        this.contentType = EntityType.CMDSTEST;
    }

    public CMDSTest(String userId, String name, String desc, long duration,
            List<TestMetadata> metadata, TestType type, TestMode mode, String code, Scope scope,
            String targetId, TestResultVisibility resultVisibility) {

        this(userId, name, desc, 0, duration, 0, metadata, type, mode, code, scope, targetId,
                resultVisibility);
    }

    public CMDSTest(String userId, String name, String desc, int qusCount, long duration,
            int totalMarks, List<TestMetadata> metadata, TestType type, TestMode mode, String code,
            Scope scope, String targetId, TestResultVisibility resultVisibility) {

        super(userId, name, desc, qusCount, duration, totalMarks, metadata, type, mode, code,
                scope, resultVisibility);
        addTarget(targetId);
    }

    @Override
    public ModelBasicInfo toBasicInfo() {

        CMDSTestBasicInfo info = new CMDSTestBasicInfo(_getStringId(), name,
                contentSrc == null ? null : contentSrc.id, timeCreated, lastUpdated, userId, 0,
                published, completed, true, globalTestId, recordState,getExportableSize());
        info.duration = duration;
        info.totalMarks = totalMarks;
        info.qusCount = qusCount;
        return info;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{name:").append(name).append(", desc:").append(desc).append(", qusCount:")
                .append(qusCount).append(", duration:").append(duration).append(", totalMarks:")
                .append(totalMarks).append(", metadata:").append(metadata).append(", type:")
                .append(type).append(", code:").append(code).append(", globalTestId:")
                .append(globalTestId).append(", published:").append(published)
                .append(", childrenIds:").append(childrenIds).append(", parentId:")
                .append(parentId).append("}");
        return builder.toString();
    }

    @Override
    public String getGlobalId() {

        return globalTestId;
    }

    @Override
    public long getExportableSize(){
        if( size != null){
            return size.getTotalSize();
        }
        return 0;
    }
}
