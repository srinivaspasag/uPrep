package com.vedantu.cmds.models.event.search.details;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.cmds.enums.CmdsContentLinkType;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.events.apis.IEventDetails;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.JSONUtils;

public class ReIndexDetails implements IEventDetails {

    private static final String IDS            = "ids";
    public static final String  CONTAINER_TYPE = "containerType";
    public List<String>         ids = new ArrayList<String>();
    public EntityType           type;
    public EntityType           containerType;
    public String               userId;
    public CmdsContentLinkType  linkType;

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = new JSONObject();
        if (type != null) {
            json.put(ConstantsGlobal.TYPE, type.name());
        }
        if (containerType != null) {
            json.put(CONTAINER_TYPE, containerType.name());
        }
        json.put(ConstantsGlobal.USER_ID, userId);
        if (linkType != null) {
            json.put(ConstantsGlobal.LINK_TYPE, linkType.name());
        }
        if (CollectionUtils.isNotEmpty(ids)) {
            json.put(IDS, ids);
        }

        return json;

    }

    @Override
    public void fromJSON(JSONObject json) {

        type = EntityType.valueOfKey(JSONUtils.getString(json, ConstantsGlobal.TYPE));
        containerType = EntityType.valueOfKey(JSONUtils.getString(json, CONTAINER_TYPE));
        ids = JSONUtils.getList(json, IDS);
        userId = JSONUtils.getString(json, ConstantsGlobal.USER_ID);
        linkType = CmdsContentLinkType
                .valueOfKey(JSONUtils.getString(json, ConstantsGlobal.LINK_TYPE));

    }

    @Override
    public SrcEntity __getSrcEntity() {

        if( type != null && type != EntityType.UNKNOWN && CollectionUtils.isNotEmpty(ids)){
            return new SrcEntity(type, ids.get(0));
        }
        return null;
    }

    @Override
    public NewsActivity toNewsActivity() {

        return null;
    }

    @Override
    public boolean enableNotifcation(boolean value) {

        return false;
    }

    @Override
    public boolean getNotificationEnabled() {

        return false;
    }

}
