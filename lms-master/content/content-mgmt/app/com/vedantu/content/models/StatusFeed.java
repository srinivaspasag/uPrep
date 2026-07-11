package com.vedantu.content.models;

import com.google.code.morphia.annotations.Embedded;
import com.google.code.morphia.annotations.Entity;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.content.pojos.Source;
import com.vedantu.content.pojos.responses.StatusFeedInfo;

@Entity(value = "statusfeeds", noClassnameStored = true)
public class StatusFeed extends AbstractContentStatsModel {

    @Embedded
    public Source sourceContent;
    public String statusMessage;

    public StatusFeed() {

    }

    public StatusFeed(String userId, String statusMessage) {

        super();
        this.userId = userId;
        this.statusMessage = statusMessage;
        this.contentType = EntityType.STATUSFEED;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("StatusFeed [ userId:");
        builder.append(userId);
        builder.append(", statusMessage:");
        builder.append(statusMessage);
        builder.append(", sourceContent:");
        builder.append(sourceContent);
        //
        return builder.toString();
    }

    @Override
    public ModelBasicInfo toBasicInfo() {

        StatusFeedInfo info = new StatusFeedInfo(this);
        return info;
    }
}