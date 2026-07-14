package com.vedantu.content.search.details;

import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.content.commons.interfaces.ILibraryContent;
import com.vedantu.content.models.ContentSearchDetails;
import com.vedantu.content.models.tests.Test;
import com.vedantu.mongo.VedantuBaseMongoModel;

public class TestSearchIndexDetails extends AbstractTestCommonSearchDetails implements
        ILibraryContent {

    public String qrTestId;

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = super.toJSON();
        json.put(ConstantsGlobal.QRTESTID, qrTestId);
        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {

        super.fromJSON(json);
        qrTestId = JSONUtils.getString(json, ConstantsGlobal.QRTESTID);
    }

    @Override
    public void fromMongoModel(VedantuBaseMongoModel mongoModel) {

        super.fromMongoModel(mongoModel);
        Test test = (Test) mongoModel;
        qrTestId = test.cmdsTestId;
    }

    @Override
    public SrcEntity __getSrcEntity() {

        return new SrcEntity(EntityType.TEST, id);
    }

    @Override
    public ContentSearchDetails __getContentSearchDetails() throws JSONException {

        ContentSearchDetails contentDetails = new ContentSearchDetails();
        JSONObject json = toJSON();
        contentDetails.fromJSON(json);

        contentDetails.name = name;
        contentDetails.type = EntityType.TEST;
        contentDetails.desc = desc;
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
