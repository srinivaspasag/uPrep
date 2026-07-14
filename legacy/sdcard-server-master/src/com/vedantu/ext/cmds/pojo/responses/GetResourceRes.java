package com.vedantu.ext.cmds.pojo.responses;

import org.json.JSONObject;
import org.json.JSONArray;

import java.util.List;
import java.util.ArrayList;

import com.vedantu.ext.cmds.utils.commons.ConstantGlobal;
import com.vedantu.ext.cmds.web.JSONUtils;

public class GetResourceRes extends AbstractResourceRes {

    public String targetId;
    public String targetType;

    public String subType;
    public long   size;
    public String thumbnail;

    public long   timeCreated;

    public List<String> extraInfo = new ArrayList<String>();

    @Override
    public void fromJSON(JSONObject json) {

        JSONObject targetJSON = JSONUtils.getJSONObject(json, "target");
        targetId = JSONUtils.getString(targetJSON, ConstantGlobal.ID);
        targetType = JSONUtils.getString(targetJSON, ConstantGlobal.TYPE);

        json = JSONUtils.getJSONObject(json, "content");
        super.fromJSON(json);

        if((JSONUtils.getString(json,"type")).equals("TEST"))
        {
            String test_info = JSONUtils.getString(json, "info");
            JSONObject json1 = new JSONObject(test_info);
            JSONArray jsonArr = JSONUtils.getJSONArray(json1, "metadata");
            JSONArray qIdsArr = JSONUtils.getJSONArray(jsonArr.getJSONObject(0),"qIds");
            for (int i=0;i<qIdsArr.length();i++){
                extraInfo.add(qIdsArr.get(i).toString());
            }
        }
        subType = JSONUtils.getString(json, "subType");
        thumbnail = JSONUtils.getString(json, ConstantGlobal.THUMBNAIL);
        timeCreated = JSONUtils.getLong(json, ConstantGlobal.TIME_CREATED);

        JSONObject sizeJSON = JSONUtils.getJSONObject(json, ConstantGlobal.SIZE);
        size += JSONUtils.getLong(sizeJSON, "encrypted");
        size += JSONUtils.getLong(sizeJSON, ConstantGlobal.THUMBNAIL);

    }

    public String getTargetId() {

        return targetId;
    }

    public String getTargetType() {

        return targetType;
    }

    public String getSubType() {

        return subType;
    }

    public long getSize() {

        return size;
    }

    public String getThumbnail() {

        return thumbnail;
    }

    public long getTimeCreated() {

        return timeCreated;
    }

    @Override
    public JSONObject toJSON() {

        return new JSONObject(this);
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{targetId:").append(targetId).append(", targetType:").append(targetType)
                .append(", subType:").append(subType).append(", size:").append(size)
                .append(", thumbnail:").append(thumbnail).append(", timeCreated:")
                .append(timeCreated).append(", id:").append(id).append(", name:").append(name)
                .append(", type:").append(type).append(", userId:").append(userId).append(", extraInfo:").append(extraInfo).append("}");
        return builder.toString();
    }

}
