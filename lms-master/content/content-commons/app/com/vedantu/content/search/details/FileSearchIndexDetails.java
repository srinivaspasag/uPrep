package com.vedantu.content.search.details;

import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.content.commons.interfaces.ILibraryContent;
import com.vedantu.content.models.ContentSearchDetails;
import com.vedantu.content.models.File;
import com.vedantu.mongo.VedantuBaseMongoModel;

public class FileSearchIndexDetails extends AbstractFileModelIndexSearchDetails implements
        ILibraryContent {

    public static final String CMDS_FILE_ID = "cmdsFileId";

    public String              cmdsFileId;
    public boolean             published;

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = super.toJSON();

        json.put(CMDS_FILE_ID, cmdsFileId);
        json.put(ConstantsGlobal.PUBLISHED, published);

        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {

        super.fromJSON(json);

        published = JSONUtils.getBoolean(json, ConstantsGlobal.PUBLISHED, false);

        cmdsFileId = JSONUtils.getString(json, CMDS_FILE_ID);
    }

    @Override
    public void fromMongoModel(VedantuBaseMongoModel mongoModel) {

        super.fromMongoModel(mongoModel);
        File file = (File) mongoModel;
        cmdsFileId = file.getCMDSFileId();
        published = file.published;

    }

    @Override
    public SrcEntity __getSrcEntity() {

        return new SrcEntity(EntityType.FILE, id);
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
        contentDetails.type = EntityType.FILE;
        contentDetails.name = name;
        contentDetails.desc = description;
        if (linkType != null) {
            contentDetails.subType = linkType.name();
        }

        JSONUtils.removeKeys(json, contentDetails.toJSON());
        if (json != null) {
            contentDetails.setInfo(json.toString());
        }
        return contentDetails;
    }

}
