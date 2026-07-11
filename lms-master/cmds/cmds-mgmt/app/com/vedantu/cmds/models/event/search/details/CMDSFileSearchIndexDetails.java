package com.vedantu.cmds.models.event.search.details;

import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.cmds.models.CMDSFile;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.events.apis.JSONAware;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.IListResponseObj;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.content.search.details.AbstractFileModelIndexSearchDetails;
import com.vedantu.mongo.VedantuBaseMongoModel;

public class CMDSFileSearchIndexDetails extends AbstractFileModelIndexSearchDetails implements
        JSONAware, IListResponseObj {

    public static final String CONVERTED      = "converted";
    public static final String GLOBAL_FILE_ID = "globalFileId";

    private String             globalFileId;
    public boolean             published;

    public CMDSFileSearchIndexDetails() {

    }

    @Override
    public SrcEntity __getSrcEntity() {

        return new SrcEntity(EntityType.CMDSFILE, this.id);
    }

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = super.toJSON();
        json.put(GLOBAL_FILE_ID, globalFileId);
        json.put(ConstantsGlobal.PUBLISHED, published);

        return json;

    }

    @Override
    public void fromJSON(JSONObject json) {

        super.fromJSON(json);

        globalFileId = JSONUtils.getString(json, GLOBAL_FILE_ID);
        published = JSONUtils.getBoolean(json, ConstantsGlobal.PUBLISHED, false);

    }

    @Override
    public void fromMongoModel(VedantuBaseMongoModel mongoModel) {

        super.fromMongoModel(mongoModel);

        CMDSFile document = (CMDSFile) mongoModel;
        this.globalFileId = document.globalFileId;

        this.published = document.published;

    }

    @Override
    public NewsActivity toNewsActivity() {

        // TODO Auto-generated method stub
        return null;
    }

}
