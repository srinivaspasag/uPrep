package com.lms.pojos;

import com.lms.common.ShareWithEntity;
import com.lms.common.exception.VedantuException;
import com.lms.common.news.NewsActivity;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.EventType;
import com.lms.common.vedantu.enums.Scope;
import com.lms.common.vedantu.enums.UserActionType;
import com.lms.pojos.search.details.EntityDetails;
import common.utils.JSONUtils;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

@Setter
@Getter
public class MadeVisibleDetails extends EntityDetails {

    public final static String ORG_ENTITY = "orgEntity";
    public final static String MADE_VISIBLE = "madeVisible";
    private static final Logger logger = LoggerFactory.getLogger(MadeVisibleDetails.class);
    public SrcEntity orgEntity;
    public boolean madeVisible = false;
    public String orgId;

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

       /* @SuppressWarnings("unchecked")
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
        }*/

        activity.actor = new SrcEntity(EntityType.USER, this.userId);
        logger.debug("Shared with entity" + this.orgEntity);
        ShareWithEntity target = new ShareWithEntity(this.orgEntity);
        activity.sharedWith = Arrays.asList(target);
        activity.info = new MadeVisibleNewsInfo();
        activity.info.actionType = UserActionType.MADE_VISIBLE;
        ((MadeVisibleNewsInfo) activity.info).target = this.orgEntity;

        return activity;

    }

}
