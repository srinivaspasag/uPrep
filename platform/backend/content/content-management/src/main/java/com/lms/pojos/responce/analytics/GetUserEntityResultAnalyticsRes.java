package com.lms.pojos.responce.analytics;

import com.lms.enums.TestResultVisibility;
import com.lms.pojos.responce.AbstractContentRes;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GetUserEntityResultAnalyticsRes extends AbstractContentRes {

    public int rank;// rank in the test
    public int AIR; // rank All India wise
    public boolean showAIR;
    public UserAnalyticsInfoRes info;
    public TestResultVisibility resultVisibility;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{rank:");
        builder.append(rank);
        builder.append(", info:");
        builder.append(info);
        builder.append(", ");
        builder.append(super.toString());
        builder.append("}");
        return builder.toString();
    }

}
