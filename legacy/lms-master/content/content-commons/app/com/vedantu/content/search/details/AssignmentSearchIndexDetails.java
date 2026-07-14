package com.vedantu.content.search.details;

import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.content.commons.interfaces.ILibraryContent;
import com.vedantu.content.models.ContentSearchDetails;
import com.vedantu.content.models.tests.Assignment;
import com.vedantu.mongo.VedantuBaseMongoModel;

public class AssignmentSearchIndexDetails extends AbstractTestCommonSearchDetails implements
        ILibraryContent {

    public String cmdsId;

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = super.toJSON();
        json.put(ConstantsGlobal.CMDS_ID, cmdsId);
        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {

        super.fromJSON(json);
        cmdsId = JSONUtils.getString(json, ConstantsGlobal.CMDS_ID);
    }

    @Override
    public void fromMongoModel(VedantuBaseMongoModel mongoModel) {

        super.fromMongoModel(mongoModel);
        Assignment assignment = (Assignment) mongoModel;
        cmdsId = assignment.cmdsId;
    }

    @Override
    public SrcEntity __getSrcEntity() {

        return new SrcEntity(EntityType.ASSIGNMENT, id);
    }

    @Override
    public ContentSearchDetails __getContentSearchDetails() throws JSONException {

        ContentSearchDetails contentDetails = new ContentSearchDetails();
        JSONObject json = toJSON();
        contentDetails.fromJSON(json);

        contentDetails.name = name;

        contentDetails.type = EntityType.ASSIGNMENT;
        if (mode != null) {
            contentDetails.subType = mode.name();
        }
        JSONUtils.removeKeys(json, contentDetails.toJSON());
        if (json != null) {
            contentDetails.setInfo(json.toString());
        }
        return contentDetails;
    }

}
