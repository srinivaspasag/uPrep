package com.lms.models;

import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.Scope;
import com.lms.enums.EnumBasket.TestType;
import com.lms.enums.TestMode;
import com.lms.enums.TestResultVisibility;
import com.lms.models.tests.AbstractTestCommonModel;
import com.lms.pojos.TestInfo;
import com.lms.pojos.TestMetadata;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "tests")
@CompoundIndexes(@CompoundIndex(name = "contentSrc, code"))
@Setter
@Getter
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
