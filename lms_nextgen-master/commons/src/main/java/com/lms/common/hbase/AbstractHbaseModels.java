package com.lms.common.hbase;

public abstract class AbstractHbaseModels {
    protected long timestamp;

    public AbstractHbaseModels() {

        this.timestamp = System.currentTimeMillis();
    }

    public void setTimestamp(long timestamp) {

        this.timestamp = timestamp;
    }

    public abstract String getKey();

    public long getTimestamp() {

        return timestamp;
    }

    @Override
    public String toString() {

        return "AbstractHbaseModels [timestamp=" + timestamp + "]";
    }
}
