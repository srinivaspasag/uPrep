package com.vedantu.user.social.actions.event.details;

import java.util.Map;

import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.events.apis.IEventDetails;
import com.vedantu.commons.news.EntityNewsInfo;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.commons.utils.ObjectMapperUtils;
import com.vedantu.content.models.AbstractBoardEntityTagModel;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.user.models.EntityUserActionMapping;
import com.vedantu.user.pojos.EntityUserActionDAO;

public class UserEntityActionDetails implements IEventDetails {
    public final static String NOTIFICATION_ENABLED="notificationEnabled";
    
    public boolean   notificationEnabled;
    
    public String    userId;
    public EventType eventType;
    public SrcEntity target;
    public String    actionId;
    
    public UserEntityActionDetails() {

        super();
    }

    public UserEntityActionDetails(String userId, EventType eventType, SrcEntity target,
            String actionId) {

        super();
        this.userId = userId;
        this.eventType = eventType;
        this.target = target;
        this.actionId = actionId;
        this.notificationEnabled=false;
    }

    @Override
    public JSONObject toJSON() throws JSONException {

        return new JSONObject(ObjectMapperUtils.convertValue(this, Map.class));
    }

    @Override
    public void fromJSON(JSONObject json) {

        target = (SrcEntity) JSONUtils.getJSONAware(new SrcEntity(), json, ConstantsGlobal.TARGET);
        userId = JSONUtils.getString(json, ConstantsGlobal.USER_ID);
        actionId = JSONUtils.getString(json, ConstantsGlobal.ACTION_ID);
        eventType = EventType.valueOf(JSONUtils.getString(json, ConstantsGlobal.EVENT_TYPE));
        notificationEnabled = JSONUtils.getBoolean(json, NOTIFICATION_ENABLED);
    }

    @Override
    public SrcEntity __getSrcEntity() {

        return target;
    }

    @Override
    public NewsActivity toNewsActivity() {

        NewsActivity activity = new NewsActivity();
        EntityUserActionMapping actionMapping = EntityUserActionDAO.INSTANCE.getById(actionId);
        if (actionMapping == null) {
            return null;
        }
        activity.actor = new SrcEntity(EntityType.USER, userId);
        activity.eType = eventType;
        activity.src = actionMapping.target;
        EntityNewsInfo info = new EntityNewsInfo();
        info.actionType = actionMapping.actionType;
        activity.info = info;
        activity.sendNewsFeed = true;
       
        @SuppressWarnings("unchecked")
        VedantuBasicDAO<? extends AbstractBoardEntityTagModel, ObjectId> vedantuBasicDAO = EntityTypeDAOFactory.INSTANCE
                .get(actionMapping.target.type);
        AbstractBoardEntityTagModel baseModel = vedantuBasicDAO.getById(actionMapping.target.id);
        if (baseModel == null) {
            return null;
        }
        activity.srcOwner = new SrcEntity(EntityType.USER, baseModel.userId);
        return activity;
    }

    @Override
    public boolean enableNotifcation(boolean value) {

        return false;
    }

    @Override
    public boolean getNotificationEnabled() {

        return false;
    }

}
