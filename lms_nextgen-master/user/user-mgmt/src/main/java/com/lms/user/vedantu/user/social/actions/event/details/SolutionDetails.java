package com.lms.user.vedantu.user.social.actions.event.details;

import com.lms.common.news.NewsActivity;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.event.api.IMongoAware;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import common.utils.JSONUtils;
import org.json.JSONObject;


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

       /* if (!(mongoModel instanceof Solution)) {
            return;
        }
        Solution solution = (Solution) mongoModel;
        id = solution._getStringId();
        qId = solution.qId;
        userId = solution.userId;
        content = solution.content;
        target = new SrcEntity(EntityType.QUESTION, solution.qId);*/
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

     /*   activity.actor = new SrcEntity(EntityType.USER, userId);
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
        activity.srcOwner = new SrcEntity(EntityType.USER, baseModel.userId);*/
        return activity;
    }
}
