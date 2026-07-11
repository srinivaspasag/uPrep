package com.lms.models.event.search.details;


import com.lms.common.news.NewsActivity;
import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.event.api.JSONAware;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.enums.DisplayOrientation;
import com.lms.models.CMDSDocument;
import com.lms.pojos.search.details.AbstractFileModelIndexSearchDetails;
import common.utils.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class CMDSDocumentSearchIndexDetails extends AbstractFileModelIndexSearchDetails implements
        JSONAware, IListResponseObj {

    public static final String CONVERTED     = "converted";
    public static final String GLOBAL_DOC_ID = "globalDocId";
    public static final String ORIENTATION   = "orientation";

    private String             globalDocId;
    public boolean             published;
    public boolean             converted;
    public DisplayOrientation orientation;

    public CMDSDocumentSearchIndexDetails() {

    }

    @Override
    public SrcEntity __getSrcEntity() {

        return new SrcEntity(EntityType.CMDSDOCUMENT, this.id);
    }

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = super.toJSON();
        json.put(GLOBAL_DOC_ID, globalDocId);
        json.put(ConstantsGlobal.PUBLISHED, published);
        json.put(CONVERTED, converted);
        json.put(ORIENTATION, orientation);
        return json;

    }

    @Override
    public void fromJSON(JSONObject json) {

        super.fromJSON(json);

        globalDocId = JSONUtils.getString(json, GLOBAL_DOC_ID);
        published = JSONUtils.getBoolean(json, ConstantsGlobal.PUBLISHED, false);
        converted = JSONUtils.getBoolean(json, CONVERTED, false);
        orientation = DisplayOrientation.valueOfKey(JSONUtils.getString(json, ORIENTATION));

    }

    @Override
    public void fromMongoModel(VedantuBaseMongoModel mongoModel) {

        super.fromMongoModel(mongoModel);

        CMDSDocument document = (CMDSDocument) mongoModel;
        this.globalDocId = document.globalDocId;
        this.converted = document.converted;
        this.published = document.published;
        orientation = document.orientation;
    }

    @Override
    public NewsActivity toNewsActivity() {

        // TODO Auto-generated method stub
        return null;
    }
}
