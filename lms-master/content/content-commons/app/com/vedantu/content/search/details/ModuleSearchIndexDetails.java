package com.vedantu.content.search.details;

import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.content.commons.interfaces.ILibraryContent;
import com.vedantu.content.models.ContentSearchDetails;
import com.vedantu.content.models.Module;
import com.vedantu.mongo.VedantuBaseMongoModel;

public class ModuleSearchIndexDetails extends AbstractModuleModelSearchDetails implements
        ILibraryContent {

    public static final String CMDS_MODULE_ID      = "cmdsModuleId";
    public static final String TOTAL_CONTENT_COUNT = "totalContentCount";

    public String              cmdsModuleId;
    public boolean             published;
    public int                 totalContentCount;

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = super.toJSON();

        json.put(CMDS_MODULE_ID, cmdsModuleId);
        json.put(TOTAL_CONTENT_COUNT, totalContentCount);

        return json;
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
        Module mod = (Module) mongoModel;
        cmdsModuleId = mod.cmdsModuleId;
        totalContentCount = mod.totalContentCount;
        //moduleEntryCompletionRule 
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
