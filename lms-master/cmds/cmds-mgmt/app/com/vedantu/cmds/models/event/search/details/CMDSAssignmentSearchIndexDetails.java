package com.vedantu.cmds.models.event.search.details;

import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.cmds.models.CMDSAssignment;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.content.search.details.AbstractTestCommonSearchDetails;
import com.vedantu.mongo.VedantuBaseMongoModel;

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
