package com.vedantu.content.search.details;

import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.DisplayOrientation;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.content.commons.interfaces.ILibraryContent;
import com.vedantu.content.models.ContentSearchDetails;
import com.vedantu.content.models.Document;
import com.vedantu.mongo.VedantuBaseMongoModel;

public class DocumentSearchIndexDetails extends AbstractFileModelIndexSearchDetails implements
        ILibraryContent {

    public static final String ORIENTATION = "orientation";

    public static final String CONVERTED   = "converted";

    public static final String CMDS_DOC_ID = "cmdsDocumentId";

    public String              cmdsDocumentId;
    public boolean             published;
    public boolean             converted;
    public DisplayOrientation  orientation;

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = super.toJSON();

        json.put(CMDS_DOC_ID, cmdsDocumentId);
        json.put(ConstantsGlobal.PUBLISHED, published);
        json.put(CONVERTED, converted);
        json.put(ORIENTATION, orientation);

        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {

        super.fromJSON(json);

        published = JSONUtils.getBoolean(json, ConstantsGlobal.PUBLISHED, false);
        converted = JSONUtils.getBoolean(json, CONVERTED);
        cmdsDocumentId = JSONUtils.getString(json, CMDS_DOC_ID);
        orientation = DisplayOrientation.valueOfKey(JSONUtils.getString(json, ORIENTATION));
    }

    @Override
    public void fromMongoModel(VedantuBaseMongoModel mongoModel) {

        super.fromMongoModel(mongoModel);
        Document doc = (Document) mongoModel;
        cmdsDocumentId = doc.getCMDSDocId();
        published = doc.published;
        converted = doc.converted;
        orientation = doc.orientation;

    }

    @Override
    public SrcEntity __getSrcEntity() {

        return new SrcEntity(EntityType.DOCUMENT, id);
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
        contentDetails.desc = description;
        if (linkType != null) {
            contentDetails.subType = linkType.name();
        }

        contentDetails.type = EntityType.DOCUMENT;
        JSONUtils.removeKeys(json, contentDetails.toJSON());
        if (json != null) {
            contentDetails.setInfo(json.toString());
        }
        return contentDetails;
    }

}
