package com.vedantu.ext.cmds.db.models;

import com.vedantu.ext.cmds.utils.commons.StringUtils;

import java.util.List;
import java.util.ArrayList;

public class Resource extends AbstractDBModel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public static final String TARGET_ID = "targetId";

    public String             id;
    public String             type;
    public String             name;
    public String             cName;
    public String             userId;
    public String             targetId;
    public String             targetType;

    public String             subType;
    public String             thumbnail;
    public long               size;                 // this will be converted or encrypted size +
                                                     // thumbnail size
    public List<String> extraInfo = new ArrayList<String>();

    public Resource() {

        super();
    }

    public Resource(int orgKeyId, String userId, String id, String type, String name,
            String targetId, String targetType, long timeCreated, String subType, String thumbnail,
            long size,List<String>extraInfo) {

        super(orgKeyId);
        this.id = id;
        this.type = type;
        this._setName(name);
        this.userId = userId;
        this.targetId = targetId;
        this.targetType = targetType;
        this.timeCreated = timeCreated;
        this.subType = subType;
        this.thumbnail = thumbnail;
        this.size = size;
        this.extraInfo=extraInfo;
    }


    public void _setName(String name) {

        this.name = name.trim();
        this.cName = StringUtils.toCanonicalName(name.trim());
    }

    
    public String getId() {
    
        return id;
    }

    
    public String getType() {
    
        return type;
    }

    
    public String getName() {
    
        return name;
    }

    
    public String getUserId() {
    
        return userId;
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

    
    public String getThumbnail() {
    
        return thumbnail;
    }

    
    public long getSize() {
    
        return size;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{id:").append(id).append(", type:").append(type).append(", name:")
                .append(name).append(", extraInfo:").append(extraInfo).append(", userId:").append(userId).append(", targetId:")
                .append(targetId).append(", targetType:").append(targetType).append(", subType:")
                .append(subType).append(", thumbnail:").append(thumbnail).append(", size:")
                .append(size).append(", _id:").append(_id).append(", orgKeyId:").append(orgKeyId)
                .append(", timeCreated:").append(timeCreated).append("}");
        return builder.toString();
    }

}
