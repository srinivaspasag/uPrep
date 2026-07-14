package com.lms.models.event.search.details;

import com.lms.common.news.NewsActivity;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.models.CMDSAssignment;
import com.lms.pojos.search.details.AbstractTestCommonSearchDetails;
import common.utils.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class CMDSAssignmentSearchIndexDetails extends AbstractTestCommonSearchDetails {


    public String globalId;

    @Override
    public void fromMongoModel(VedantuBaseMongoModel mongoModel) {

        super.fromMongoModel(mongoModel);
        CMDSAssignment assignment = (CMDSAssignment) mongoModel;
        globalId = assignment.globalId;
    }

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = super.toJSON();
        json.put(ConstantsGlobal.GLOBAL_ID, globalId);
        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {

        super.fromJSON(json);
        globalId = JSONUtils.getString(json, ConstantsGlobal.GLOBAL_ID);
    }

    @Override
    public SrcEntity __getSrcEntity() {

        return new SrcEntity(EntityType.CMDSASSIGNMENT, id);
    }

    @Override
    public NewsActivity toNewsActivity() {

        return null;
    }

}
