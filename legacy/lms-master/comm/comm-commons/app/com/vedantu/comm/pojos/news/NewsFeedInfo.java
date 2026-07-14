package com.vedantu.comm.pojos.news;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.comm.enums.NotificationReason;
import com.vedantu.commons.events.apis.JSONAware;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.responses.IListResponseObj;
import com.vedantu.commons.utils.ObjectMapperUtils;

public class NewsFeedInfo extends NewsActivityInfo implements IListResponseObj, JSONAware {

    public String             newsFeedId;
    public NotificationReason why;

    public NewsFeedInfo() {

        super();
    }

    public NewsFeedInfo(NewsFeedInfo n) {

        super(n);
        this.newsFeedId = n.newsFeedId;
        this.why = n.why;
    }

    public NewsFeedInfo(NewsActivity n) {

        super(n);
        this.newsFeedId = "";
        this.why = NotificationReason.UNKNOWN;
    }

    @Override
    public int hashCode() {

        return "newsFeedInfo".hashCode() + this.newsFeedId.hashCode() + why.hashCode();
    }

    @Override
    public JSONObject toJSON() throws JSONException {

        return new JSONObject(ObjectMapperUtils.convertValue(this, Map.class));
    }

    @Override
    public void fromJSON(JSONObject json) {

        // from json method is not needed as we are using the JSON directly

    }

    @Override
    public boolean equals(Object object) {

        if (!(object instanceof NewsFeedInfo)) {
            return false;
        }
        NewsFeedInfo input = (NewsFeedInfo) object;

        return (newsFeedId.equalsIgnoreCase(input.newsFeedId));
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{newsFeedId:").append(newsFeedId).append(", why:").append(why)
                .append(", newsActivityId:").append(newsActivityId).append(", eType:")
                .append(eType).append(", time:").append(time).append(", src:").append(src)
                .append(", srcOwner:").append(srcOwner).append(", actor:").append(actor)
                .append(", sharedWith:").append(sharedWith).append(", comments:").append(comments)
                .append(", involved:").append(involved).append(", info:").append(info)
                .append(", sendNewsFeed:").append(sendNewsFeed).append(", scope:").append(scope)
                .append(", timestamp:").append(timestamp).append("}");
        return builder.toString();
    }

}
