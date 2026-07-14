package com.lms.models.tests;

import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.Scope;
import com.lms.enums.TestMode;
import com.lms.enums.TestResultVisibility;
import com.lms.pojos.TestInfo;
import com.lms.pojos.TestMetadata;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import com.lms.enums.EnumBasket.TestType;

import java.util.List;

@Document(collection = "assignments")
@CompoundIndexes(@CompoundIndex(name = "contentSrc, code"))
public class Assignment extends AbstractTestCommonModel {

    public String cmdsId;

    public Assignment() {

        super();
        contentType = EntityType.ASSIGNMENT;
    }

    public Assignment(String userId, String name, String desc, int qusCount, long duration,
                      int totalMarks, List<TestMetadata> metadata, TestType type, TestMode mode, String code,
                      Scope scope, TestResultVisibility resultVisibility) {

        super(userId, name, desc, qusCount, duration, totalMarks, metadata, type, mode, code,
                scope, resultVisibility);
        contentType = EntityType.ASSIGNMENT;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{name:").append(name).append(", desc:").append(desc).append(", qusCount:")
                .append(qusCount).append(", duration:").append(duration).append(", totalMarks:")
                .append(totalMarks).append(", metadata:").append(metadata).append(", type:")
                .append(type).append(", code:").append(code).append(", cmdsId:").append(cmdsId)
                .append(", published:").append(published).append(", childrenIds:")
                .append(childrenIds).append(", parentId:").append(parentId).append("}");
        return builder.toString();
    }

    @Override
    public ModelBasicInfo toBasicInfo() {

        // String orgId = (contentSrc != null) ? contentSrc.id : StringUtils.EMPTY;
        TestInfo info = new TestInfo(_getStringId(), name, qusCount, mode);
        return info;
    }

}
