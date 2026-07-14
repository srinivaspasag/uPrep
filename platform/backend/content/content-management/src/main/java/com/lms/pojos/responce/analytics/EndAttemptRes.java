package com.lms.pojos.responce.analytics;

import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EndAttemptRes {
    public ModelBasicInfo info;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{info:");
        builder.append(info);
        builder.append("}");
        return builder.toString();
    }
}
