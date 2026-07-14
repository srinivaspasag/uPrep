package com.lms.models;

import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.pojos.Source;
import com.lms.pojos.response.StatusFeedInfo;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(value = "statusfeeds")
public class StatusFeed extends AbstractContentStatsModel {

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