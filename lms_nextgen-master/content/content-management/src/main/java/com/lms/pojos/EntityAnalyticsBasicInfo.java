package com.lms.pojos;

import com.amazonaws.services.devicefarm.model.TestType;
import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.enums.TestMode;
import com.lms.models.EntityMeasures;
import com.lms.pojos.tests.BoardAnalyticsInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public class EntityAnalyticsBasicInfo implements IListResponseObj {

    public String name;
    public String id;
    public int totalMarks;
    public TestType type;
    public TestMode mode;

    public List<BoardAnalyticsInfo> boards;
    public List<BoardAnalyticsInfo> metadata;// this is used for retreving the
    // totalMarks of boards from the
    // TestSearchIndexDetails object

    public EntityMeasures measures;//
    public long attempts;// attempts==totalAttempts
    public long totalAttempts;

    public void __fillBoardsTotalMarks() {
        Map<String, BoardAnalyticsInfo> infoMap = new HashMap<String, BoardAnalyticsInfo>();
        if (metadata != null) {
            for (BoardAnalyticsInfo info : metadata) {
                infoMap.put(info.id, info);
            }
        }
        if (boards != null) {
            for (BoardAnalyticsInfo info : boards) {
                info.totalMarks = infoMap.get(info.id) == null ? 0 : infoMap
                        .get(info.id).totalMarks;
            }
        }
        infoMap.clear();
        metadata = null;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{name:");
        builder.append(name);
        builder.append(", id:");
        builder.append(id);
        builder.append(", totalMarks:");
        builder.append(totalMarks);
        builder.append(", type:");
        builder.append(type);
        builder.append(", mode:");
        builder.append(mode);
        builder.append(", boards:");
        builder.append(boards);
        builder.append(", measures:");
        builder.append(measures);
        builder.append(", attempts:");
        builder.append(attempts);
        builder.append(", totalAttempts:");
        builder.append(totalAttempts);
        builder.append("}");
        return builder.toString();
    }

}
