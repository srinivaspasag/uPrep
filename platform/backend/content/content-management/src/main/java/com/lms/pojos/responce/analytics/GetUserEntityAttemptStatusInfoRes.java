package com.lms.pojos.responce.analytics;

import com.lms.enums.EnumBasket;
import com.lms.enums.TestMode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetUserEntityAttemptStatusInfoRes {
    public boolean completed;
    public boolean attempted;
    public long startTime;
    public long endTime;
    public EnumBasket.TestType type;
    public TestMode mode;     // ONLINE/OFFLINE--> used in ui for showing only POST TEST PAGE
    // for off-line test

    public GetUserEntityAttemptStatusInfoRes() {

    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{completed:").append(completed).append(", attempted:").append(attempted)
                .append(", startTime:").append(startTime).append(", endTime:").append(endTime)
                .append(", type:").append(type).append("}");
        return builder.toString();
    }
}
