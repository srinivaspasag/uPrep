package com.vedantu.ext.cmds.pojo.responses;

import org.json.JSONObject;

import com.vedantu.ext.cmds.db.models.SDCard;
import com.vedantu.ext.cmds.utils.commons.ConstantGlobal;
import com.vedantu.ext.cmds.web.JSONAware;
import com.vedantu.ext.cmds.web.JSONUtils;

public class GetSDCardInfoRes implements JSONAware {

    private String name;
    private String id;
    private String groupId;
    private long   size;
    private long   contentSize;
    private long   timeCreated;
    private long   count;

    @Override
    public void fromJSON(JSONObject json) {

        name = JSONUtils.getString(json, ConstantGlobal.NAME);
        id = JSONUtils.getString(json, ConstantGlobal.ID);
        groupId = JSONUtils.getString(json, SDCard.FIELD_GROUP_ID);
        size = JSONUtils.getLong(json, "maxSize");
        contentSize = JSONUtils.getLong(json, SDCard.FIELD_CONTENT_SIZE);
        timeCreated = JSONUtils.getLong(json, ConstantGlobal.TIME_CREATED);
        count = JSONUtils.getLong(json, SDCard.FIELD_COUNT);
    }

    @Override
    public JSONObject toJSON() {

        return new JSONObject(this);
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public String getGroupId() {

        return groupId;
    }

    public void setGroupId(String groupId) {

        this.groupId = groupId;
    }

    public long getSize() {

        return size;
    }

    public void setSize(long size) {

        this.size = size;
    }

    public long getContentSize() {

        return contentSize;
    }

    public void setContentSize(long contentSize) {

        this.contentSize = contentSize;
    }

    public long getTimeCreated() {

        return timeCreated;
    }

    public void setTimeCreated(long timeCreated) {

        this.timeCreated = timeCreated;
    }

    
    public long getCount() {
    
        return count;
    }

    
    public void setCount(long count) {
    
        this.count = count;
    }

}
