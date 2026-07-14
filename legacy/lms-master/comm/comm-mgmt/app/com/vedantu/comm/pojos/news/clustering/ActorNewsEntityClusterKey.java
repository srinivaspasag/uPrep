package com.vedantu.comm.pojos.news.clustering;

import play.Logger;
import play.Logger.ALogger;
import play.Play;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;

public class ActorNewsEntityClusterKey implements IClusterable {

    private final static ALogger LOGGER               = Logger.of(ActorNewsEntityClusterKey.class);
    public EntityType            entityType;
    public EventType             eventType;
    public long                  dayAggregatedTimestamp;
    public static final long     CLUSTER_SIZE_IN_TIME = Play.application()
                                                              .configuration()
                                                              .getLong(
                                                                      "vedantu.newsfeed.maxclustertimeinsec",
                                                                      new Long(100000));

    public ActorNewsEntityClusterKey(EntityType actorNewsEntityType, EventType eventType,
            long timestamp) {

        this.entityType = actorNewsEntityType;
        this.eventType = eventType;
        this.dayAggregatedTimestamp = timestamp / (CLUSTER_SIZE_IN_TIME * 1000);
    }

    @Override
    public int hashCode() {

        return (this.entityType + "_" + this.eventType + "_" + this.dayAggregatedTimestamp)
                .hashCode();
    }

    @Override
    public boolean equals(Object o) {

        if (!(o instanceof ActorNewsEntityClusterKey)) {
            return false;
        }
        ActorNewsEntityClusterKey feedKey = (ActorNewsEntityClusterKey) o;

        LOGGER.info(" Input key entity type " + feedKey.entityType + " current key actionType "
                + this.eventType);

        return feedKey.entityType.equals(this.entityType)
                && (feedKey.eventType == this.eventType)
                && (feedKey.dayAggregatedTimestamp == this.dayAggregatedTimestamp);
    }

    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append(" EntityType: " + entityType);
        builder.append(" ActionType: " + eventType);
        return builder.toString();
    }
}
