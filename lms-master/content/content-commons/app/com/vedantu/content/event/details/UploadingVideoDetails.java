package com.vedantu.content.event.details;

import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.events.apis.IEventDetails;
import com.vedantu.commons.events.apis.JSONAware;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.JSONUtils;

public class UploadingVideoDetails implements IEventDetails, JSONAware {

    private static final String ID               = "id";
    private static final String TYPE             = "type";
    private static final String LOCATION_ON_SRC  = "locationOnSrc";
    private static final String SRC_MACHINE_PORT = "srcMachinePort";
    private static final String SRC_MACHINE_NAME = "srcMachineName";
    public String               srcMachineName;
    public int                  srcMachinePort;
    public String               locationOnSrc;
    public EntityType           type;
    public String               id;
    public String               jobId;
    public String               uuid;

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = new JSONObject();

        json.put(SRC_MACHINE_NAME, srcMachineName);
        json.put(SRC_MACHINE_PORT, srcMachinePort);
        json.put(LOCATION_ON_SRC, locationOnSrc);
        json.put(TYPE, type);
        json.put(ID, id);
        json.put(ConstantsGlobal.JOB_ID, jobId);
        json.put(ConstantsGlobal.UUID, uuid);

        return json;

    }

    @Override
    public void fromJSON(JSONObject json) {

        srcMachineName = JSONUtils.getString(json, SRC_MACHINE_NAME);
        srcMachinePort = JSONUtils.getInt(json, SRC_MACHINE_PORT);
        locationOnSrc = JSONUtils.getString(json, LOCATION_ON_SRC);
        type = EntityType.valueOfKey(JSONUtils.getString(json, TYPE));
        id = JSONUtils.getString(json, ID);
        jobId = JSONUtils.getString(json, ConstantsGlobal.JOB_ID);
        uuid = JSONUtils.getString(json, ConstantsGlobal.UUID);
    }

    @Override
    public SrcEntity __getSrcEntity() {

        return new SrcEntity(type, id);
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
