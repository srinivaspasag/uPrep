package com.vedantu.cmds.models.event.details;

import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.events.apis.IEventDetails;
import com.vedantu.commons.events.apis.JSONAware;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.JSONUtils;

public class FileProcessingDetails implements JSONAware, IEventDetails {

    private static final String ENCRYPT_IF_NEEDED       = "encryptIfNeeded";

    public String               fileId;

    public boolean              encryptIfNeeded;
    public String               jobId;

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = new JSONObject();

        json.put(ENCRYPT_IF_NEEDED, encryptIfNeeded);
        json.put(ConstantsGlobal.FILE_ID, fileId);
        json.put(ConstantsGlobal.JOB_ID, jobId);
        return json;

    }

    @Override
    public void fromJSON(JSONObject json) {

        encryptIfNeeded = JSONUtils.getBoolean(json, ENCRYPT_IF_NEEDED);
        fileId = JSONUtils.getString(json, ConstantsGlobal.FILE_ID);
        jobId = JSONUtils.getString(json, ConstantsGlobal.JOB_ID);
    }

    @Override
    public SrcEntity __getSrcEntity() {

        // TODO Auto-generated method stub
        return new SrcEntity(EntityType.CMDSFILE, fileId);
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
