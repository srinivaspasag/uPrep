package com.vedantu.content.event.details;

import java.util.Arrays;

import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.ShareWithEntity;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.content.models.AbstractContentModel;
import com.vedantu.content.news.pojos.MadeVisibleNewsInfo;
import com.vedantu.content.search.details.EntityDetails;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuRecordState;

public class MadeVisibleDetails extends EntityDetails {

    private static final ALogger LOGGER       = Logger.of(MadeVisibleDetails.class);
    public final static String   ORG_ENTITY   = "orgEntity";
    public final static String   MADE_VISIBLE = "madeVisible";

    public SrcEntity             orgEntity;
    public boolean               madeVisible  = false;
    public String                orgId;

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = super.toJSON();
        json.put(ORG_ENTITY, orgEntity.toJSON());
        json.put(MADE_VISIBLE, madeVisible);
        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {

        super.fromJSON(json);
        JSONObject orgEntityJSON = (JSONObject) JSONUtils.getObject(json, ORG_ENTITY);
        orgEntity = new SrcEntity();
        orgEntity.fromJSON(orgEntityJSON);
        madeVisible = JSONUtils.getBoolean(json, MADE_VISIBLE);

    }

    @Override
    public NewsActivity toNewsActivity() throws VedantuException {

        NewsActivity activity = new NewsActivity();

        activity.eType = EventType.MADE_VISIBLE;
        activity.src = __getSrcEntity();
        activity.scope = Scope.ORG;

        @SuppressWarnings("unchecked")
        VedantuBasicDAO<VedantuBaseMongoModel, ObjectId> basicDAO = EntityTypeDAOFactory.INSTANCE
                .get(entity.type);
        if (basicDAO == null) {
            Logger.debug("");
        }
        VedantuBaseMongoModel model = basicDAO.getById(entity.id, VedantuRecordState.ACTIVE);
        if (model == null) {
            throw new VedantuException(VedantuErrorCode.NO_CONTENT_FOUND);
        }
        if (model instanceof AbstractContentModel) {

            activity.srcOwner = new SrcEntity(EntityType.USER,
                    ((AbstractContentModel) model).userId);
        }

        activity.actor = new SrcEntity(EntityType.USER, this.userId);
        LOGGER.debug("Shared with entity" + this.orgEntity);
        ShareWithEntity target = new ShareWithEntity(this.orgEntity);
        activity.sharedWith = Arrays.asList(target);
        activity.info = new MadeVisibleNewsInfo();
        activity.info.actionType = UserActionType.MADE_VISIBLE;
        ((MadeVisibleNewsInfo) activity.info).target = this.orgEntity;

        return activity;

    }

}
