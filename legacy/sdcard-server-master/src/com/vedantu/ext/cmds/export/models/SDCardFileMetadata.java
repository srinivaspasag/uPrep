package com.vedantu.ext.cmds.export.models;

import org.json.JSONObject;

import com.vedantu.ext.cmds.web.JSONAware;

public class SDCardFileMetadata implements JSONAware {

    private static final long serialVersionUID = 1L;

    private String            name;
    private String            entityId;
    private String            entityType;

    private String            location;             // Location relative to the card
    private long              size;
    private String            cardId;
    private String            cardName;

    public SDCardFileMetadata() {

        super();
    }

    public SDCardFileMetadata(String entityId, String entityType, String name,
            String location, long size, String cardId, String cardName) {

        this.entityId = entityId;
        this.entityType = entityType;
        this.name = name;
        this.location = location;
        this.size = size;
        this.cardId = cardId;
        this.cardName = cardName;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getEntityId() {

        return entityId;
    }

    public void setEntityId(String entityId) {

        this.entityId = entityId;
    }

    public String getEntityType() {

        return entityType;
    }

    public void setEntityType(String entityType) {

        this.entityType = entityType;
    }

    public String getLocation() {

        return location;
    }

    public void setLocation(String location) {

        this.location = location;
    }

    public long getSize() {

        return size;
    }

    public void setSize(long size) {

        this.size = size;
    }

    public String getCardId() {

        return cardId;
    }

    public void setCardId(String cardId) {

        this.cardId = cardId;
    }

    public String getCardName() {

        return cardName;
    }

    public void setCardName(String cardName) {

        this.cardName = cardName;
    }

    public static long getSerialversionuid() {

        return serialVersionUID;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{userId:").append(", name:").append(name).append(", entityId:")
                .append(entityId).append(", entityType:").append(entityType).append(", location:")
                .append(location).append(", size:").append(size).append(", cardId:").append(cardId)
                .append(", cardName:").append(cardName).append(", active:")
                .append(", timeCreated:").append("}");
        return builder.toString();
    }

    @Override
    public void fromJSON(JSONObject json) {

    }

    @Override
    public JSONObject toJSON() {

        return new JSONObject(this);
    }
}