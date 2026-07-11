package com.vedantu.ext.cmds.export.models;

import org.json.JSONArray;
import org.json.JSONObject;

import com.vedantu.ext.cmds.web.JSONAware;

public class SDCardGroupMetadata implements JSONAware {

    private String    targetType;
    private String    targetId;
    private long      cardSize;  // in bytes
    private int       noOfCards;
    private JSONArray cardIds;
    private String    orgId;
    private String    name;
    private String    id;
    private long      size;

    public SDCardGroupMetadata(String targetId, String targetType, long cardSize, int noOfCards,
            JSONArray cardIds, String orgId, String name, String id, long size) {

        super();
        this.targetType = targetType;
        this.targetId= targetId;
        this.cardSize = cardSize;
        this.noOfCards = noOfCards;
        this.cardIds = cardIds;
        this.orgId = orgId;
        this.name = name;
        this.id = id;
        this.size = size;
    }

    public String getTargetType() {

        return targetType;
    }

    public long getCardSize() {

        return cardSize;
    }

    public int getNoOfCards() {

        return noOfCards;
    }

    public JSONArray getCardIds() {

        return cardIds;
    }

    public String getOrgId() {

        return orgId;
    }

    public String getName() {

        return name;
    }

    public String getId() {

        return id;
    }

    public long getSize() {

        return size;
    }

    
    public String getTargetId() {
    
        return targetId;
    }

    
    public void setTargetId(String targetId) {
    
        this.targetId = targetId;
    }

    @Override
    public void fromJSON(JSONObject json) {

        // TODO Auto-generated method stub

    }

    @Override
    public JSONObject toJSON() {

        return new JSONObject(this);
    }
}
