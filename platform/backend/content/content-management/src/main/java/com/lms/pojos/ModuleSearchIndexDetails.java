package com.lms.pojos;

import com.lms.common.news.NewsActivity;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.interfaces.ILibraryContent;
import com.lms.models.ContentSearchDetails;
import com.lms.pojos.responce.AbstractModuleModelSearchDetails;
import common.utils.JSONUtils;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONException;
import org.json.JSONObject;


@Setter
@Getter
public class ModuleSearchIndexDetails extends AbstractModuleModelSearchDetails implements
        ILibraryContent {

    public static final String CMDS_MODULE_ID = "cmdsModuleId";
    public static final String TOTAL_CONTENT_COUNT = "totalContentCount";

    public String cmdsModuleId;
    public boolean published;
    public int totalContentCount;

    @Override
    public JSONObject toJSON() throws JSONException {

        return null;
    }

    @Override
    public void fromJSON(JSONObject json) {

        super.fromJSON(json);

        cmdsModuleId = JSONUtils.getString(json, CMDS_MODULE_ID);
        totalContentCount = JSONUtils.getInt(json, TOTAL_CONTENT_COUNT);
    }

    @Override
    public void fromMongoModel(VedantuBaseMongoModel mongoModel) {
        super.fromMongoModel(mongoModel);
        com.lms.models.Module mod = (com.lms.models.Module) mongoModel;
        cmdsModuleId = mod.cmdsModuleId;
        totalContentCount = mod.totalContentCount;
    }

    @Override
    public SrcEntity __getSrcEntity() {

        return new SrcEntity(EntityType.MODULE, id);
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
        // contentDetails.desc = description;

        contentDetails.type = EntityType.MODULE;
        JSONUtils.removeKeys(json, contentDetails.toJSON());
        if (json != null) {
            contentDetails.setInfo(json.toString());
        }
        return contentDetails;
    }

}