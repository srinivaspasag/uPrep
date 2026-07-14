package com.lms.pojos.responce;

import com.lms.common.pojos.MarkDistribution;
import com.lms.common.vedantu.commons.pojos.requests.responces.ListResponse;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GetEntityMarkDistributionRes extends
        ListResponse<MarkDistribution> {

    public float avgScore;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{avgScore:");
        builder.append(avgScore);
        builder.append(", totalHits:");
        builder.append(totalHits);
        builder.append(", list:");
        builder.append(list);
        builder.append("}");
        return builder.toString();
    }

}
