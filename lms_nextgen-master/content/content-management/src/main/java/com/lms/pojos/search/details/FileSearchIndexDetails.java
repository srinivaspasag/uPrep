package com.lms.pojos.search.details;

import com.lms.common.news.NewsActivity;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.interfaces.ILibraryContent;
import com.lms.models.ContentSearchDetails;
import com.lms.models.Files;
import common.utils.JSONUtils;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONException;
import org.json.JSONObject;


@Getter
@Setter
public class FileSearchIndexDetails extends AbstractFileModelIndexSearchDetails implements
        ILibraryContent {

    public static final String CMDS_FILE_ID = "cmdsFileId";

    public String cmdsFileId;
    public boolean published;

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
        Files file = (Files) mongoModel;
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
