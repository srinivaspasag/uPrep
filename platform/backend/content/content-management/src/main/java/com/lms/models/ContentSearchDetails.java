package com.lms.models;

import com.lms.common.news.NewsActivity;
import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.interfaces.IReverseImageMapperProcessor;
import com.lms.pojos.search.details.AbstractBoardSearchEntityTagDetails;
import common.utils.JSONUtils;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Setter
@Getter
public class ContentSearchDetails extends AbstractBoardSearchEntityTagDetails implements
        IListResponseObj, IReverseImageMapperProcessor {

    private static final String SUB_TYPE = "subType";

    public String desc;
    public EntityType type;                // DOCUMENT/VIDEO/TEST/ASSIGNMENT etc
    public String subType;             // this will be populated if there is any
    // entity (testMetada in case of
    // test and assignment)
    public String thumbnail;
    // other subType exist such as
    // in case of test {mode: ONLINE/OFFLINE}
    private JSONObject info;                // this will contain any extra info of the

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = super.toJSON();
        json.put(SUB_TYPE, subType);
        json.put(ConstantsGlobal.DESC, desc);
        json.put(ConstantsGlobal.THUMBNAIL, thumbnail);
        if (type != null) {
            json.put(ConstantsGlobal.TYPE, type.name());
        }
        if (info != null) {
            json.put(ConstantsGlobal.INFO, info);
        }
        return json;
    }

    public String getInfo() {

        return info != null ? info.toString() : null;
    }

    public void setInfo(String info) {

        if (info != null) {
            try {
                this.info = new JSONObject(info);
            } catch (JSONException e) {

            }
        }
    }

    public JSONObject __getInfo() {

        return info;
    }

    public void addToInfo(String key, JSONObject infoJson) {

        if (info != null) {
            try {
                this.info.put(key, infoJson);
            } catch (JSONException e) {

            }
        }
    }

    @Override
    public void fromJSON(JSONObject json) {

        super.fromJSON(json);
        subType = JSONUtils.getString(json, SUB_TYPE);
        type = EntityType.valueOfKey(JSONUtils.getString(json, ConstantsGlobal.TYPE));
        desc = JSONUtils.getString(json, ConstantsGlobal.DESC);
        thumbnail = JSONUtils.getString(json, ConstantsGlobal.THUMBNAIL);
        // info = JSONUtils.getJSONObject(json, ConstantsGlobal.INFO);
    }

    @Override
    public SrcEntity __getSrcEntity() {

        return new SrcEntity(type, id);
    }

    @Override
    public NewsActivity toNewsActivity() {

        return null;
    }


    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{description:").append(desc).append(", type:").append(type)
                .append(", info:").append(info).append(", subType:").append(subType)
                .append(", thumbnail:").append(thumbnail).append(", boardTree:").append(boardTree)
                .append(", boards:").append(boards).append(", targets:").append(targets)
                .append(", contentSrc:").append(contentSrc).append(", tags:").append(tags)
                .append(", scope:").append(scope).append(", avgRating:").append(avgRating)
                .append(", views:").append(views).append(", followers:").append(followers)
                .append(", comments:").append(comments).append(", upVotes:").append(upVotes)
                .append(", difficulty:").append(difficulty).append(", name:").append(name)
                .append(", userId:").append(userId).append(", id:").append(id)
                .append(", userAction:").append(userAction).append(", timeCreated:")
                .append(timeCreated).append(", lastUpdated:").append(lastUpdated)
                .append(", lastIndexTime:").append(lastIndexTime)
                .append(", isNotificationEnabled:").append(isNotificationEnabled).append(", user:")
                .append(user).append("}");
        return builder.toString();
    }


    @Override
    public void addImageSrcUrl() {

    }

    @Override
    public void removeImageSrc(boolean moveImages) throws IOException {

    }

    @Override
    public boolean _isIndexable() {

        return !StringUtils.isEmpty(name);
    }


}
