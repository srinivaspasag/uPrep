package com.vedantu.ext.cmds.db.models;

import org.json.JSONArray;

public class SDCardGroup extends AbstractDBModel {

    /**
     * 
     */
    private static final long  serialVersionUID  = 1L;
    public static final String FIELD_TARGET_ID   = "targetId";
    public static final String FIELD_TARGET_TYPE = "targetType";
    public static final String FIELD_SIZE        = SDCard.FIELD_SIZE;
    public static final String FIELD_CARD_SIZE   = "cardSize";
    public static final String FIELD_NO_OF_CARDS = "noOfCards";
    public static final String FIELD_CARD_IDS    = "cardIds";

    public String              name;
    public String              id;
    public String              targetId;                             // SECTION ID
    public String              targetType;                           // SECTION
    public long                size;                                 // in bytes
    public long                cardSize;                             // in bytes
    public int                 noOfCards;
    public JSONArray           cardIds;

    public SDCardGroup() {

        super();
    }

    public SDCardGroup(int orgKeyId, String name, String id, String targetId, String targetType,
            long size, long cardSize, int noOfCards, JSONArray cardIds, long timeCreated) {

        super(orgKeyId);
        this.name = name;
        this.id = id;
        this.targetId = targetId;
        this.targetType = targetType;
        this.size = size;
        this.cardSize = cardSize;
        this.noOfCards = noOfCards;
        this.cardIds = cardIds;
        this.timeCreated = timeCreated;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{name:").append(name).append(", id:").append(id).append(", targetId:")
                .append(targetId).append(", targetType:").append(targetType).append(", size:")
                .append(size).append(", cardSize:").append(cardSize).append(", noOfCards:")
                .append(noOfCards).append(", cardIds:").append(cardIds).append(", _id:")
                .append(_id).append(", orgKeyId:").append(orgKeyId).append(", timeCreated:")
                .append(timeCreated).append("}");
        return builder.toString();
    }

}
