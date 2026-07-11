package com.lms.event.details;

import com.lms.common.exception.VedantuException;
import com.lms.common.news.NewsActivity;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.event.api.IEventDetails;
import common.utils.JSONUtils;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONException;
import org.json.JSONObject;
 @Setter
  @Getter
public class DoubtsProcessingDetails implements IEventDetails {

    public String discussionId;

    public DoubtsProcessingDetails() {
        super();
    }

    public DoubtsProcessingDetails(String discussionId) {
        super();
        this.discussionId = discussionId;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
       // return new JSONObject(ObjectMapperUtils.convertValue(this, Map.class));
        return null;
    }

    @Override
    public void fromJSON(JSONObject json) {
        discussionId = JSONUtils.getString(json, "discussionId");
    }

    @Override
    public SrcEntity __getSrcEntity() {
        return new SrcEntity(EntityType.DISCUSSION, discussionId);
    }

    @Override
    public String toString() {
        return "DoubtsProcessingDetails [discussionId=" + discussionId + "]";
    }

    @Override
    public NewsActivity toNewsActivity() throws VedantuException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean enableNotifcation(boolean value) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean getNotificationEnabled() {
        // TODO Auto-generated method stub
        return false;
    }

}
