package com.vedantu.cmds.models.event.search.details;

import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.cmds.models.CMDSModule;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.content.search.details.AbstractModuleModelSearchDetails;
import com.vedantu.mongo.VedantuBaseMongoModel;

public class CMDSModuleSearchIndexDetails extends AbstractModuleModelSearchDetails {

    public String globalModuleId;

    @Override
    public void fromMongoModel(VedantuBaseMongoModel mongoModel) {

        super.fromMongoModel(mongoModel);
        CMDSModule module = (CMDSModule) mongoModel;
        globalModuleId = module.globalModuleId;
    }

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = super.toJSON();
        json.put(ConstantsGlobal.GLOBAL_MODULE_ID, globalModuleId);
        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {

        super.fromJSON(json);
        globalModuleId = JSONUtils.getString(json, ConstantsGlobal.GLOBAL_MODULE_ID);
    }

    @Override
    public SrcEntity __getSrcEntity() {

        return new SrcEntity(EntityType.CMDSMODULE, id);
    }

    @Override
    public NewsActivity toNewsActivity() {

        return null;
    }

}
