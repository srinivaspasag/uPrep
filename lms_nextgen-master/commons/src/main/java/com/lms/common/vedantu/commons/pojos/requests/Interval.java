package com.lms.common.vedantu.commons.pojos.requests;

import java.util.Date;

public class Interval {

    public static transient final long NO_END = -1;
    private long                from;
    private long                till;

    public Interval() {

    }
    public Interval(long requestTime, long till) {

        super();
        this.from = requestTime;
        this.till = till;
    }

    public long getFrom() {

        return from;
    }

    public void setFrom(long from) {

        this.from = from;
    }

    public long getTill() {

        return till;
    }

    public void setTill(long till) {

        this.till = till;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (from ^ (from >>> 32));
        result = prime * result + (int) (till ^ (till >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Interval other = (Interval) obj;
        if (from != other.from)
            return false;
        if (till != other.till)
            return false;
        return true;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{from:").append(new Date(from)).append(", till:").append(new Date(till))
                .append("}");
        return builder.toString();
    }

}
