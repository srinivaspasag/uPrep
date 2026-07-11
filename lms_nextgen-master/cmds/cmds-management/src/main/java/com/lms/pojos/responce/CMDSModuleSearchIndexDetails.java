package com.lms.pojos.responce;

import com.lms.common.news.NewsActivity;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.models.CMDSModule;
import common.utils.JSONUtils;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONException;
import org.json.JSONObject;

@Setter
@Getter
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
