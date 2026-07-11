package com.lms.models.messages;

public abstract class AbstractHbaseModels {

    protected long timestamp;

    public AbstractHbaseModels() {

        this.timestamp = System.currentTimeMillis();
    }

    public abstract String getKey();

    public long getTimestamp() {

        return timestamp;
    }

    public void setTimestamp(long timestamp) {

        this.timestamp = timestamp;
    }

    @Override
    public String toString() {

        return "AbstractHbaseModels [timestamp=" + timestamp + "]";
    }

}
