package com.lms.pojos.search.details;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.interfaces.ILibraryContent;
import com.lms.models.ContentSearchDetails;
import common.utils.JSONUtils;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONException;
import org.json.JSONObject;

@Setter
@Getter
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
