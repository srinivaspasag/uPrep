package com.vedantu.user.social.actions.event.details;

import org.bson.types.ObjectId;

import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.news.EntityNewsInfo;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.models.AbstractBoardEntityTagModel;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.user.models.EntityUserActionMapping;
import com.vedantu.user.pojos.EntityUserActionDAO;

public class AttemptDetails extends UserEntityActionDetails {

    public AttemptDetails() {

        super();
    }

    public AttemptDetails(String userId, EventType eventType, SrcEntity target, String actionId,
            boolean silent) {

        this(userId, eventType, target, actionId);
        this.enableNotifcation(silent);
    }

    public AttemptDetails(String userId, EventType eventType, SrcEntity target, String actionId) {

        super(userId, eventType, target, actionId);
    }

    @Override
    public boolean enableNotifcation(boolean value) {

        return notificationEnabled = value;
    }

    @Override
    public boolean getNotificationEnabled() {

        return notificationEnabled;
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

}
