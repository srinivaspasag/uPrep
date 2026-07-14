package com.lms.models.events.searchdetails;

import com.lms.common.news.NewsActivity;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.event.api.IEventDetails;
import com.lms.common.vedantu.event.api.JSONAware;
import common.utils.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class DocumentEncodingDetails implements JSONAware, IEventDetails {


    private static final String GENERATE_LINEARIZED_PDF = "generateLinearizedPDF";
    private static final String GENERATE_THUMBNAIL      = "generateThumbnail";
    private static final String ENCRYPT_IF_NEEDED   = "encryptIfNeeded";
    private static final String CONVERT_TO_PDF   = "convertToPDF";
    public String               docId;
    public boolean              generateThumbnail;
    public boolean              generateLinearizedPDF;
    public boolean              encryptIfNeeded;
    public boolean              convertToPDF;
    public String               jobId;

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = new JSONObject();

        json.put(GENERATE_LINEARIZED_PDF, generateLinearizedPDF);
        json.put(GENERATE_THUMBNAIL, generateThumbnail);
        json.put(ENCRYPT_IF_NEEDED, encryptIfNeeded);
        json.put(CONVERT_TO_PDF, convertToPDF);
        json.put(ConstantsGlobal.DOC_ID, docId);
        json.put(ConstantsGlobal.JOB_ID, jobId);
        return json;

    }

    @Override
    public void fromJSON(JSONObject json) {

        generateThumbnail = JSONUtils.getBoolean(json, GENERATE_THUMBNAIL);
        generateLinearizedPDF = JSONUtils.getBoolean(json, GENERATE_LINEARIZED_PDF);
        encryptIfNeeded = JSONUtils.getBoolean(json, ENCRYPT_IF_NEEDED);
        convertToPDF = JSONUtils.getBoolean(json, CONVERT_TO_PDF);
        docId = JSONUtils.getString(json, ConstantsGlobal.DOC_ID);
        jobId = JSONUtils.getString(json, ConstantsGlobal.JOB_ID);
    }

    @Override
    public SrcEntity __getSrcEntity() {

        // TODO Auto-generated method stub
        return new SrcEntity(EntityType.CMDSDOCUMENT, docId);
    }

    @Override
    public NewsActivity toNewsActivity() {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean enableNotifcation(boolean value) {

        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean getNotificationEnabled() {

        // TODO Auto-generated method stub
        return false;
    }
}
