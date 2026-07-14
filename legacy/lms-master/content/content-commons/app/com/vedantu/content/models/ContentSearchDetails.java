package com.vedantu.content.models;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.entity.storage.EntityFileStorageException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.IListResponseObj;
import com.vedantu.commons.utils.IReverseImageMapperProcessor;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.commons.utils.image.ImageHTMLUtils;
import com.vedantu.content.search.details.AbstractBoardSearchEntityTagDetails;

public class ContentSearchDetails extends AbstractBoardSearchEntityTagDetails implements
        IListResponseObj, IReverseImageMapperProcessor {

    private static final String SUB_TYPE = "subType";

    public String               desc;
    public EntityType           type;                // DOCUMENT/VIDEO/TEST/ASSIGNMENT etc
    private JSONObject          info;                // this will contain any extra info of the
    // entity (testMetada in case of
    // test and assignment)

    public String               subType;             // this will be populated if there is any
                                                      // other subType exist such as
                                                      // in case of test {mode: ONLINE/OFFLINE}

    public String               thumbnail;

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

    public JSONObject __getInfo() {

        return info;
    }

    public void setInfo(String info) {

        if (info != null) {
            try {
                this.info = new JSONObject(info);
            } catch (JSONException e) {

            }
        }
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

    public QueryBuilder _getEsQuery() {

        QueryBuilder query = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery(ConstantsGlobal.TYPE, type.name().toLowerCase()))
                .must(QueryBuilders.termQuery(ConstantsGlobal.ID, id));
        return query;
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

        if (type == EntityType.QUESTION) {
            desc = ImageHTMLUtils.addImageSrcUrl(type, desc);
            List<String> options = JSONUtils.getList(info, ConstantsGlobal.OPTIONS);
            if (options != null) {
                List<String> newOptions = new ArrayList<String>();
                for (String option : options) {
                    option = ImageHTMLUtils.addImageSrcUrl(EntityType.QUESTION, option);
                    newOptions.add(option);
                }
                options = newOptions;
                try {
                    info.put(ConstantsGlobal.OPTIONS, options);
                } catch (JSONException e) {}
            }
            JSONObject matrix = JSONUtils.getJSONObject(info, "matrix");
            if (matrix != null) {
                Map<String, List<String>> newMatrix = new LinkedHashMap<String, List<String>>();
                @SuppressWarnings("unchecked")
                Iterator<String> entry = matrix.keys();
                while (entry.hasNext()) {
                    List<String> newOptions = new ArrayList<String>();
                    String key = entry.next();
                    for (String option : JSONUtils.getList(matrix, key)) {
                        option = ImageHTMLUtils.addImageSrcUrl(EntityType.QUESTION, option);
                        newOptions.add(option);
                    }
                    newMatrix.put(key, newOptions);
                    try {
                        info.put("matrix", newMatrix);
                    } catch (JSONException e) {}
                }
            }
        }
    }

    @Override
    public void removeImageSrc(boolean moveImages) throws IOException, EntityFileStorageException {

    }

    @Override
    public boolean _isIndexable() {

        return StringUtils.isNotEmpty(name);
    }

}
