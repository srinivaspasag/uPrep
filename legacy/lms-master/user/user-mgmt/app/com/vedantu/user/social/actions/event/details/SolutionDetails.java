package com.vedantu.user.social.actions.event.details;

import org.bson.types.ObjectId;
import org.json.JSONObject;

import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.events.apis.IMongoAware;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.content.models.AbstractBoardEntityTagModel;
import com.vedantu.content.models.Solution;
import com.vedantu.content.news.pojos.AddSolutionInfo;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuBasicDAO;

public class SolutionDetails extends UserEntityActionDetails implements IMongoAware {

    public String content;
    public String qId;
    public String id;

    @Override
    public void fromJSON(JSONObject json) {

        super.fromJSON(json);
        content = JSONUtils.getString(json, ConstantsGlobal.CONTENT);
        qId = JSONUtils.getString(json, ConstantsGlobal.QID);
    }

    @Override
    public SrcEntity __getSrcEntity() {

        return new SrcEntity(EntityType.QUESTION, qId);
    }

    @Override
    public void fromMongoModel(VedantuBaseMongoModel mongoModel) {

        if (!(mongoModel instanceof Solution)) {
            return;
        }
        Solution solution = (Solution) mongoModel;
        id = solution._getStringId();
        qId = solution.qId;
        userId = solution.userId;
        content = solution.content;
        target = new SrcEntity(EntityType.QUESTION, solution.qId);
    }

    @Override
    public boolean enableNotifcation(boolean value) {

        return true;
    }

    @Override
    public boolean getNotificationEnabled() {

        return true;
    }

    @Override
    public NewsActivity toNewsActivity() {

        NewsActivity activity = new NewsActivity();

        activity.actor = new SrcEntity(EntityType.USER, userId);
        activity.scope = Scope.PUBLIC;
        activity.eType = EventType.ADD_SOLUTION;
        activity.src = target;
        AddSolutionInfo info = new AddSolutionInfo();
        info.actionType = UserActionType.ADDED;
        info.solution = new SrcEntity(EntityType.SOLUTION, id);
        activity.info = info;
        activity.sendNewsFeed = true;

        @SuppressWarnings("unchecked")
        VedantuBasicDAO<? extends AbstractBoardEntityTagModel, ObjectId> vedantuBasicDAPO = EntityTypeDAOFactory.INSTANCE
                .get(target.type);
        AbstractBoardEntityTagModel baseModel = vedantuBasicDAPO.getById(target.id);
        if (baseModel == null) {
            return null;
        }
        activity.srcOwner = new SrcEntity(EntityType.USER, baseModel.userId);
        return activity;
    }
}
