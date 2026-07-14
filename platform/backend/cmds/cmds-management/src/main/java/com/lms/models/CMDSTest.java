package com.lms.models;

import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.Scope;
import com.lms.content.CMDSTestBasicInfo;
import com.lms.enums.EnumBasket;
import com.lms.enums.TestMode;
import com.lms.enums.TestResultVisibility;
import com.lms.interfaces.ICMDSModel;
import com.lms.models.tests.AbstractTestCommonModel;
import com.lms.pojos.TestMetadata;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Setter
@Getter
@Document(value = "cmdstests")
@CompoundIndexes(@CompoundIndex(name = "contentSrc, code"))
public class CMDSTest extends AbstractTestCommonModel implements ICMDSModel {

    public boolean publishingInProgress;
    public String globalTestId;

    // public List<TestMetadata> currentMetadata;

    public CMDSTest() {

        super();
        this.contentType = EntityType.CMDSTEST;
    }

    public CMDSTest(String userId, String name, String desc, long duration,
                    List<TestMetadata> metadata, EnumBasket.TestType type, TestMode mode, String code, Scope scope,
                    String targetId, TestResultVisibility resultVisibility) {

        this(userId, name, desc, 0, duration, 0, metadata, type, mode, code, scope, targetId,
                resultVisibility);
    }

    public CMDSTest(String userId, String name, String desc, int qusCount, long duration,
                    int totalMarks, List<TestMetadata> metadata, EnumBasket.TestType type, TestMode mode, String code,
                    Scope scope, String targetId, TestResultVisibility resultVisibility) {

        super(userId, name, desc, qusCount, duration, totalMarks, metadata, type, mode, code,
                scope, resultVisibility);
        addTarget(targetId);
    }

    @Override
    public ModelBasicInfo toBasicInfo() {

        CMDSTestBasicInfo info = new CMDSTestBasicInfo(_getStringId(), name,
                contentSrc == null ? null : contentSrc.id, timeCreated, lastUpdated, userId, 0,
                published, completed, true, globalTestId, recordState, getExportableSize());
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
    public long getExportableSize() {
        if (size != null) {
            return size.getTotalSize();
        }
        return 0;
    }
}
