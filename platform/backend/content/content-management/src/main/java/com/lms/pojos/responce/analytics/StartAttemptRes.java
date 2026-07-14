package com.lms.pojos.responce.analytics;

import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StartAttemptRes {

    public ModelBasicInfo info;
    public long startTime;
    public boolean isReattempt;
    public List<String> qIds;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{info:").append(info).append(", timeCreated:")
                .append(startTime).append(", isReattempt:")
                .append(isReattempt).append(", qIds:").append(qIds).append("}");
        return builder.toString();
    }

}
