package com.vedantu.content.search.details;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.news.EntityNewsInfo;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.commons.utils.ObjectMapperUtils;
import com.vedantu.content.enums.Difficulty;
import com.vedantu.content.enums.challenges.BidType;
import com.vedantu.content.enums.challenges.ChallengeStatus;
import com.vedantu.content.enums.challenges.ChallengeType;
import com.vedantu.content.models.AbstractBoardEntityTagModel;
import com.vedantu.content.models.challenges.Challenge;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuBasicDAO;

public class ChallengeSearchIndexDetails extends AbstractBoardSearchEntityTagDetails {

    public String          channelId;
    public String          name;
    public ChallengeType   type;
    public ChallengeStatus status;
    public long            endTime;
    public int             lifeTime;
    public int             duration;
    public int             maxBid;
    public int             bidPool;
    public BidType         bidType;
    public Scope           publishType;
    public Difficulty      difficulty;
    public List<SrcEntity> entities;
    public List<Integer>   hintsDeductionValues;
    public int             attempts;
    public int             minTargets;
    public Set<String>     qTypes;
    public List<String>    topperIds;

    public ChallengeSearchIndexDetails() {

        super();
    }

    @Override
    public SrcEntity __getSrcEntity() {

        return new SrcEntity(EntityType.CHALLENGE, id);
    }

    @Override
    public UniqueId _getUniqueId() {

        return new UniqueId(ConstantsGlobal.ID, id);
    }

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = new JSONObject(ObjectMapperUtils.convertValue(this, Map.class));
        JSONUtils.addJSONAwareObjectList(ConstantsGlobal.ENTITIES, entities, json);
        return json;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void fromJSON(JSONObject json) {

        super.fromJSON(json);
        channelId = JSONUtils.getString(json, ConstantsGlobal.CHANNEL_ID);
        name = JSONUtils.getString(json, ConstantsGlobal.NAME);
        type = ChallengeType.valueOfKey(JSONUtils.getString(json, ConstantsGlobal.TYPE));
        status = ChallengeStatus.valueOfKey(JSONUtils.getString(json, ConstantsGlobal.STATUS));
        endTime = JSONUtils.getLong(json, ConstantsGlobal.END_TIME);
        lifeTime = JSONUtils.getInt(json, ConstantsGlobal.LIFE_TIME);
        duration = JSONUtils.getInt(json, ConstantsGlobal.DURATION);
        maxBid = JSONUtils.getInt(json, ConstantsGlobal.MAX_BID);
        bidPool = JSONUtils.getInt(json, ConstantsGlobal.BID_POOL);
        bidType = BidType.valueOf(JSONUtils.getString(json, ConstantsGlobal.BID_TYPE));
        publishType = Scope.valueOfKey(JSONUtils.getString(json, ConstantsGlobal.PUBLISH_TYPE));
        difficulty = Difficulty.valueOfKey(JSONUtils.getString(json, ConstantsGlobal.DIFFICULTY));
        entities = (List<SrcEntity>) JSONUtils.getJSONAwareCollection(SrcEntity.class, json,
                ConstantsGlobal.ENTITIES);
        attempts = JSONUtils.getInt(json, ConstantsGlobal.ATTEMPTS);
        minTargets = JSONUtils.getInt(json, ConstantsGlobal.MIN_TARGETS);
        topperIds = JSONUtils.getList(json, ConstantsGlobal.TOPPER_IDS);
        hintsDeductionValues = JSONUtils.getIntegerList(json, "hintsDeductionValues");
        qTypes = JSONUtils.getSet(json, ConstantsGlobal.Q_TYPES);
    }

    @Override
    public void fromMongoModel(VedantuBaseMongoModel mongoModel) {

        super.fromMongoModel(mongoModel);
        Challenge challenge = (Challenge) mongoModel;
        channelId = challenge._getStringId();
        name = challenge.name;
        type = challenge.type;
        status = challenge.status;
        lifeTime = challenge.lifeTime;
        endTime = challenge.endTime;
        duration = challenge.duration;
        maxBid = challenge.maxBid;
        bidPool = challenge.bidPool;
        bidType = challenge.bidType;
        publishType = challenge.publishType;
        difficulty = challenge.difficulty;
        entities = challenge.entities;
        scope = challenge.scope;
        minTargets = challenge.minTargets;
        attempts = challenge.attempts;
        topperIds = challenge.topperIds;
        hintsDeductionValues = challenge.hintsDeductionValues;
        qTypes = challenge.qTypes;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{channelId:");
        builder.append(channelId);
        builder.append(", name:");
        builder.append(name);
        builder.append(", type:");
        builder.append(type);
        builder.append(", status:");
        builder.append(status);
        builder.append(", endTime:");
        builder.append(endTime);
        builder.append(", lifeTime:");
        builder.append(lifeTime);
        builder.append(", duration:");
        builder.append(duration);
        builder.append(", maxBid:");
        builder.append(maxBid);
        builder.append(", bidPool:");
        builder.append(bidPool);
        builder.append(", bidType:");
        builder.append(bidType);
        builder.append(", publishType:");
        builder.append(publishType);
        builder.append(", difficulty:");
        builder.append(difficulty);
        builder.append(", entities:");
        builder.append(entities);
        builder.append(", hintsDeductionValues:");
        builder.append(hintsDeductionValues);
        builder.append(", attempts:");
        builder.append(attempts);
        builder.append(", minTargets:");
        builder.append(minTargets);
        builder.append(", qTypes:");
        builder.append(qTypes);
        builder.append(", topperIds:");
        builder.append(topperIds);
        builder.append("}");
        return builder.toString();
    }

    @Override
    public NewsActivity toNewsActivity() {

        NewsActivity activity = new NewsActivity();
        activity.actor = new SrcEntity(EntityType.USER, userId);
        activity.src = new SrcEntity(EntityType.CHALLENGE, id);
        @SuppressWarnings("unchecked")
        VedantuBasicDAO<VedantuBaseMongoModel, ObjectId> basicDAO = EntityTypeDAOFactory.INSTANCE
                .get(EntityType.CHALLENGE);
        VedantuBaseMongoModel model = basicDAO.getById(id);
        if (model != null) {
            if (model instanceof AbstractBoardEntityTagModel) {
                AbstractBoardEntityTagModel entityModel = (AbstractBoardEntityTagModel) model;
                activity.srcOwner = new SrcEntity(EntityType.USER, entityModel.userId);
            }
        }
        activity.sendNewsFeed=true;
        activity.sharedWith = null;
        activity.comments = null;
        activity.involved = null;
        activity.eType = null;
        activity.info = new EntityNewsInfo();
        activity.info.actionType = this.userAction;
        activity.scope = Scope.ORG;
        return activity;
    }

    @Override
    public boolean _isIndexable() {

        return StringUtils.isNotEmpty(name);
    }

}
