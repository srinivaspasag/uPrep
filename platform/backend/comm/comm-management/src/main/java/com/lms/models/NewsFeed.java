package com.lms.models;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.enums.NotificationReason;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Setter
@Getter
@Document(collection = "newsfeeds")
public class NewsFeed extends VedantuBaseMongoModel {
    public SrcEntity actor;
    public String aid;
    public NotificationReason why;

    public NewsFeed() {
    }

    public NewsFeed(String aid, NotificationReason why) {
        this.aid = aid;
        this.why = why;
    }


}
