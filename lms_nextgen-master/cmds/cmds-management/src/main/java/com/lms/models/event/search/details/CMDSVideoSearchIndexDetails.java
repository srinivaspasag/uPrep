package com.lms.models.event.search.details;

import com.lms.common.news.NewsActivity;
import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.event.api.JSONAware;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.enums.SrcType;
import com.lms.models.CMDSVideo;
import com.lms.pojos.search.details.AbstractFileModelIndexSearchDetails;
import common.utils.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class CMDSVideoSearchIndexDetails extends AbstractFileModelIndexSearchDetails implements
        JSONAware, IListResponseObj {

    public static final String CONVERTED = "converted";
    public static final String GLOBAL_VIDEO_ID = "globalVideoId";
    public boolean published;
    public boolean converted;
    private String globalVideoId;

    public CMDSVideoSearchIndexDetails() {

    }

    @Override
    public SrcEntity __getSrcEntity() {

        return new SrcEntity(EntityType.CMDSVIDEO, this.id);
    }

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = super.toJSON();
        json.put(ConstantsGlobal.THUMBNAIL, thumbnail);
        json.put(GLOBAL_VIDEO_ID, globalVideoId);
        json.put(ConstantsGlobal.PUBLISHED, published);
        json.put(ConstantsGlobal.LINK_TYPE, linkType.name());
        json.put(ConstantsGlobal.URL, url);
        json.put(ConstantsGlobal.EXTENSION, extension);
        json.put(ConstantsGlobal.UUID, uuid);
        json.put(ConstantsGlobal.ORGINAL_FILE_NAME, originalFileName);
        json.put(ConstantsGlobal.DESCRIPTION, description);
        json.put(CONVERTED, converted);

        return json;

    }

    @Override
    public void fromJSON(JSONObject json) {

        super.fromJSON(json);
        thumbnail = JSONUtils.getString(json, ConstantsGlobal.THUMBNAIL);
        globalVideoId = JSONUtils.getString(json, GLOBAL_VIDEO_ID);
        published = JSONUtils.getBoolean(json, ConstantsGlobal.PUBLISHED, false);
        linkType = SrcType.LinkType.valueOfKey(JSONUtils.getString(json, ConstantsGlobal.LINK_TYPE));
        url = JSONUtils.getString(json, ConstantsGlobal.URL);
        extension = JSONUtils.getString(json, ConstantsGlobal.EXTENSION);
        uuid = JSONUtils.getString(json, ConstantsGlobal.UUID);
        originalFileName = JSONUtils.getString(json, ConstantsGlobal.ORGINAL_FILE_NAME);
        converted = JSONUtils.getBoolean(json, CONVERTED, false);

    }

    @Override
    public void fromMongoModel(VedantuBaseMongoModel mongoModel) {

        super.fromMongoModel(mongoModel);

        CMDSVideo video = (CMDSVideo) mongoModel;
        this.url = video.url;
        this.uuid = video.uuid;
        this.description = video.description;
        this.globalVideoId = video.globalVideoId;
        this.thumbnail = video.thumbnail;
        this.linkType = video.linkType;
        this.extension = video.extension;
        this.converted = video.converted;
        this.published = video.published;

    }

    @Override
    public NewsActivity toNewsActivity() {

        // TODO Auto-generated method stub
        return null;
    }

}
