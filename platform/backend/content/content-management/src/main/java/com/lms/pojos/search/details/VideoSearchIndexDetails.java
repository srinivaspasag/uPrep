package com.lms.pojos.search.details;

import com.lms.common.news.NewsActivity;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.enums.SrcType.LinkType;
import com.lms.interfaces.ILibraryContent;
import com.lms.models.ContentSearchDetails;
import com.lms.models.Video;
import common.utils.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;


public class VideoSearchIndexDetails extends AbstractFileModelIndexSearchDetails implements
        ILibraryContent {

    public static final String CONVERTED = "converted";
    public String cmdsVideoId;
    public boolean published;

    public long duration;
    public boolean converted;
    public boolean disableDownload;

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = super.toJSON();
        json.put(ConstantsGlobal.CMDS_VIDEO_ID, cmdsVideoId);
        json.put(ConstantsGlobal.PUBLISHED, published);
        json.put(ConstantsGlobal.LINK_TYPE, linkType.name());
        json.put(ConstantsGlobal.URL, url);
        json.put(ConstantsGlobal.EXTENSION, extension);
        json.put(ConstantsGlobal.UUID, uuid);
        json.put(ConstantsGlobal.ORGINAL_FILE_NAME, originalFileName);
        json.put(ConstantsGlobal.DURATION, duration);
        json.put(CONVERTED, converted);
        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {

        super.fromJSON(json);
        cmdsVideoId = JSONUtils.getString(json, ConstantsGlobal.CMDS_VIDEO_ID);
        published = JSONUtils.getBoolean(json, ConstantsGlobal.PUBLISHED, false);
        linkType = LinkType.valueOfKey(JSONUtils.getString(json, ConstantsGlobal.LINK_TYPE));
        url = JSONUtils.getString(json, ConstantsGlobal.URL);
        extension = JSONUtils.getString(json, ConstantsGlobal.EXTENSION);
        uuid = JSONUtils.getString(json, ConstantsGlobal.UUID);
        originalFileName = JSONUtils.getString(json, ConstantsGlobal.ORGINAL_FILE_NAME);
        duration = JSONUtils.getLong(json, ConstantsGlobal.DURATION);
        converted = JSONUtils.getBoolean(json, CONVERTED);
    }

    @Override
    public void fromMongoModel(VedantuBaseMongoModel mongoModel) {

        super.fromMongoModel(mongoModel);
        Video video = (Video) mongoModel;
        thumbnail = video.thumbnail;

        originalFileName = video.originalFileName;
        description = video.description;
        cmdsVideoId = video.getCmdsVideoId();
        published = video.published;
        extension = video.extension;
        uuid = video.uuid;
        linkType = video.linkType;
        url = video.url;
        duration = video.duration;
        converted = video.converted;

    }

    @Override
    public SrcEntity __getSrcEntity() {

        return new SrcEntity(EntityType.VIDEO, id);
    }

    @Override
    public NewsActivity toNewsActivity() {

        return null;
    }

    @Override
    public ContentSearchDetails __getContentSearchDetails() throws JSONException {

        ContentSearchDetails contentDetails = new ContentSearchDetails();
        JSONObject json = toJSON();
        contentDetails.fromJSON(json);

        contentDetails.name = name;
        contentDetails.desc = description;
        if (linkType != null) {
            contentDetails.subType = linkType.name();
        }

        contentDetails.type = EntityType.VIDEO;
        JSONUtils.removeKeys(json, contentDetails.toJSON());
        if (json != null) {
            contentDetails.setInfo(json.toString());
        }
        return contentDetails;
    }

}
