package com.lms.common.relationships;

public class EntityUserActionRelationshipSearchDetails {

    public String userId;
    public String dstId;
    public long timeCreated;

    public EntityUserActionRelationshipSearchDetails(String userId, String dstId) {
        this.userId = userId;
        this.dstId = dstId;
        this.timeCreated = System.currentTimeMillis();
    }


}
