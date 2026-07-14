package com.lms.pojos;

import com.lms.common.news.NewsActivity;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.EventType;
import com.lms.common.vedantu.enums.UserActionType;
import com.lms.user.vedantu.user.social.actions.event.details.UserEntityActionDetails;
import common.utils.JSONUtils;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONException;
import org.json.JSONObject;

@Setter
@Getter
public class CommentDetails extends UserEntityActionDetails {

    // in case of comment {added comment is src/target entity--> it's parent will be considered as
    // actual target}
    public final static String COMMENT_TEXT = "commentText";
    public String id;
    public String commentText;

    public CommentDetails() {

        super();
    }

    public CommentDetails(String userId, EventType eventType, SrcEntity target, String actionId,
                          String id) {

        super(userId, eventType, target, actionId);
        this.id = id;
    }

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = super.toJSON();
        json.put(ConstantsGlobal.ID, id);
        json.put(COMMENT_TEXT, commentText);
        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {

        super.fromJSON(json);
        id = JSONUtils.getString(json, ConstantsGlobal.ID);
        commentText = JSONUtils.getString(json, COMMENT_TEXT);
    }

    @Override
    public NewsActivity toNewsActivity() {
        NewsActivity activity = super.toNewsActivity();
        CommentEntityNewsInfo info = new CommentEntityNewsInfo();
        info.actionType = UserActionType.COMMENTED;
        info.className = CommentEntityNewsInfo.class.getName();
        info.comment = new SrcEntity(EntityType.COMMENT, id);
        info.commentText = commentText;
        return activity;

    }

    @Override
    public boolean enableNotifcation(boolean value) {

        notificationEnabled = true;
        return true;
    }

    @Override
    public boolean getNotificationEnabled() {

        return true;
    }

}
