package com.lms.pojos.news.clustering;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.EventType;
import com.lms.pojos.news.IClusterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

public class SrcNewsEntityClusterKey implements IClusterable {
    private static final Logger logger = LoggerFactory.getLogger(SrcNewsEntityClusterKey.class);
    @Value("${vedantu.newsfeed.maxclustertimeinsec}")
    public static long CLUSTER_SIZE_IN_TIME;
    public String id;
    public EventType eType;
    public long dayAggregatedTimestamp;

    public SrcNewsEntityClusterKey(SrcEntity srcNewsEntity, EventType actionType, long timestamp) {

        this.id = srcNewsEntity.id;
        this.eType = actionType;

        this.dayAggregatedTimestamp = timestamp / (CLUSTER_SIZE_IN_TIME * 1000); // as tme is in
        // millseconds
        logger.info(" timestamp original " + timestamp + " aggregating size "
                + CLUSTER_SIZE_IN_TIME + " after aggregation " + dayAggregatedTimestamp);
    }

    @Override
    public int hashCode() {

        logger.debug(this.id + this.eType + ":" + dayAggregatedTimestamp);
        return (this.id + ":" + this.eType.hashCode() + ":" + dayAggregatedTimestamp)
                .hashCode();
    }

    @Override
    public boolean equals(Object o) {

        if (!(o instanceof SrcNewsEntityClusterKey)) {
            return false;
        }
        SrcNewsEntityClusterKey feedKey = (SrcNewsEntityClusterKey) o;

        logger.info(" Input key entity " + feedKey.id + " current key entity " + this.id);

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
