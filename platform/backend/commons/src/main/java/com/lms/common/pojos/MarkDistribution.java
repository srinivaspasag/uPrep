package com.lms.common.pojos;

import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MarkDistribution implements IListResponseObj {

    public double from;
    public double to;
    public int count;

    public MarkDistribution() {
    }

    public MarkDistribution(double from, double to, int count) {
        this.from = from;
        this.to = to;
        this.count = count;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{from:");
        builder.append(from);
        builder.append(", to:");
        builder.append(to);
        builder.append(", count:");
        builder.append(count);
        builder.append("}");
        return builder.toString();
    }

}
