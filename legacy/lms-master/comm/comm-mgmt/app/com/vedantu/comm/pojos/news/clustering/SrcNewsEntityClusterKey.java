package com.vedantu.comm.pojos.news.clustering;

import play.Logger;
import play.Logger.ALogger;
import play.Play;

import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.pojos.SrcEntity;

public class SrcNewsEntityClusterKey implements IClusterable {

    private final static ALogger LOGGER               = Logger.of(SrcNewsEntityClusterKey.class);
    public String                id;
    public EventType             eType;

    public long                  dayAggregatedTimestamp;
    public static final long     CLUSTER_SIZE_IN_TIME = Play.application()
                                                              .configuration()
                                                              .getLong(
                                                                      "vedantu.newsfeed.maxclustertimeinsec",
                                                                      new Long(10000));

    public SrcNewsEntityClusterKey(SrcEntity srcNewsEntity, EventType actionType, long timestamp) {

        this.id = srcNewsEntity.id;
        this.eType = actionType;

        this.dayAggregatedTimestamp = timestamp / (CLUSTER_SIZE_IN_TIME * 1000); // as tme is in
                                                                                 // millseconds
        LOGGER.info(" timestamp original " + timestamp + " aggregating size "
                + CLUSTER_SIZE_IN_TIME + " after aggregation " + dayAggregatedTimestamp);
    }

    @Override
    public int hashCode() {

        LOGGER.debug(this.id + this.eType + ":" + dayAggregatedTimestamp);
        return (this.id + ":" + this.eType.hashCode() + ":" + dayAggregatedTimestamp)
                .hashCode();
    }

    @Override
    public boolean equals(Object o) {

        if (!(o instanceof SrcNewsEntityClusterKey)) {
            return false;
        }
        SrcNewsEntityClusterKey feedKey = (SrcNewsEntityClusterKey) o;

        LOGGER.info(" Input key entity " + feedKey.id + " current key entity " + this.id);

        return feedKey.id.equals(this.id) && (feedKey.eType.equals(this.eType))
                && (feedKey.dayAggregatedTimestamp == this.dayAggregatedTimestamp);
    }

    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append(" NewsEntityId " + id);
        builder.append(" EventType" + eType);
        builder.append(" timestamp :  " + dayAggregatedTimestamp);
        return builder.toString();
    }
}
