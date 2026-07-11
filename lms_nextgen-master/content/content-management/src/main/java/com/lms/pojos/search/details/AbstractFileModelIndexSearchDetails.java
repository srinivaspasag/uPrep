package com.lms.pojos.search.details;

import com.lms.common.news.NewsActivity;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.enums.SrcType;
import com.lms.models.AbstractFileModel;
import com.lms.pojos.LinkInfo;
import common.utils.JSONUtils;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.StringUtils;

@Setter
@Getter
public class AbstractFileModelIndexSearchDetails extends AbstractBoardSearchEntityTagDetails {

    public String thumbnail;
    public String poster;
    public String originalFileName;
    public String description;

    public String extension;
    public String uuid;

    public SrcType.LinkType linkType;
    public String           url;
    public String           backupVideoUrl;
    public String           s3url;
    public String           s3HDurl;

    public boolean          downloadable;

    public LinkInfo linkInfo;

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = super.toJSON();

        json.put(ConstantsGlobal.THUMBNAIL, thumbnail);
        json.put(ConstantsGlobal.LINK_TYPE, linkType.name());
        json.put(ConstantsGlobal.URL, url);
        json.put(ConstantsGlobal.EXTENSION, extension);
        json.put(ConstantsGlobal.UUID, uuid);
        json.put(ConstantsGlobal.ORGINAL_FILE_NAME, originalFileName);
        json.put(ConstantsGlobal.DESCRIPTION, description);
        if (linkInfo != null) {
            json.put(ConstantsGlobal.LINK_INFO, linkInfo.toJSON());
        }
        return json;
    }

    @Override
    public SrcEntity __getSrcEntity() {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NewsActivity toNewsActivity() {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void fromJSON(JSONObject json) {

        super.fromJSON(json);
        JSONUtils.getJSONAware(linkInfo, json, ConstantsGlobal.LINK_INFO);
        url = JSONUtils.getString(json, ConstantsGlobal.URL);

        thumbnail = JSONUtils.getString(json, ConstantsGlobal.THUMBNAIL);
        extension = JSONUtils.getString(json, ConstantsGlobal.EXTENSION);
        uuid = JSONUtils.getString(json, ConstantsGlobal.UUID);
        originalFileName = JSONUtils.getString(json, ConstantsGlobal.ORGINAL_FILE_NAME);
        linkType = SrcType.LinkType.valueOfKey(JSONUtils.getString(json, ConstantsGlobal.LINK_TYPE));
        description = JSONUtils.getString(json, ConstantsGlobal.DESCRIPTION);

    }

    @Override
    public void fromMongoModel(VedantuBaseMongoModel mongoModel) {

        super.fromMongoModel(mongoModel);
        AbstractFileModel fileModel = (AbstractFileModel) mongoModel;

        thumbnail = fileModel.thumbnail;

        originalFileName = fileModel.originalFileName;
        description = fileModel.description;
        extension = fileModel.extension;
        uuid = fileModel.uuid;
        linkType = fileModel.linkType;
        url = fileModel.url;
        linkInfo = fileModel.linkInfo;

    }

    @Override
    public boolean _isIndexable() {

        return !StringUtils.isEmpty(name);
    }

}