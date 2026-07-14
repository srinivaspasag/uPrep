package com.vedantu.content.models.tests;

import java.util.List;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexes;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.content.enums.EnumBasket.TestType;
import com.vedantu.content.enums.TestMode;
import com.vedantu.content.enums.TestResultVisibility;
import com.vedantu.content.pojos.TestInfo;
import com.vedantu.content.pojos.tests.TestMetadata;

@Entity(value = "tests", noClassnameStored = true)
@Indexes(@Index(value = "contentSrc, code"))
public class Test extends AbstractTestCommonModel {

    public String cmdsTestId;
    public boolean regeneratingAnalytics;

    public Test() {

        super();
        contentType = EntityType.TEST;
    }

    public Test(String userId, String name, String desc, int qusCount, long duration,
            int totalMarks, List<TestMetadata> metadata, TestType type, TestMode mode, String code,
            Scope scope, TestResultVisibility resultVisibility) {

        super(userId, name, desc, qusCount, duration, totalMarks, metadata, type, mode, code,
                scope, resultVisibility);
        contentType = EntityType.TEST;
    }

    public Test(String userId, String name, String desc, int qusCount, long duration,
            int totalMarks, List<TestMetadata> metadata, TestType type, TestMode mode, String code,
            Scope scope, TestResultVisibility resultVisibility, String password, String resultPassword) {

        super(userId, name, desc, qusCount, duration, totalMarks, metadata, type, mode, code,
                scope, resultVisibility, null, password, resultPassword);
        contentType = EntityType.TEST;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{name:").append(name).append(", desc:").append(desc).append(", qusCount:")
                .append(qusCount).append(", duration:").append(duration).append(", totalMarks:")
                .append(totalMarks).append(", metadata:").append(metadata).append(", type:")
                .append(type).append(", code:").append(code).append(", cmdsTestId:")
                .append(cmdsTestId).append(", published:").append(published)
                .append(", childrenIds:").append(childrenIds).append(", parentId:")
                .append(parentId).append("}");
        return builder.toString();
    }

    @Override
    public ModelBasicInfo toBasicInfo() {

        // String orgId = (contentSrc != null) ? contentSrc.id : StringUtils.EMPTY;
        TestInfo info = new TestInfo(_getStringId(), name, qusCount, mode);
        return info;
    }

    @Override
    public long getExportableSize() {

        if (size != null) {
            return size.getTotalSize();
        }
        return 0;
    }
}
