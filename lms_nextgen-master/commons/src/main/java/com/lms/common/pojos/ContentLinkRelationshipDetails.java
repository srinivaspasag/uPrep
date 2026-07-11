package com.lms.common.pojos;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.EncryptionLevel;
import com.lms.common.vedantu.enums.Scope;

public class ContentLinkRelationshipDetails {

    public SrcEntity entity;
    public String userId;
    public SrcEntity dst;
    public Scope scope;
    public ScheduleInfo schedule;
    public long timeCreated;
    public long lastUpdated;
    public boolean downloadble;
    public EncryptionLevel encLevel;
    public long position;

    public ContentLinkRelationshipDetails(String userId, SrcEntity entity, SrcEntity dst,
                                          Scope scope) {

        this.userId = userId;
        this.dst = dst;
        this.entity = entity;
        this.scope = scope;
        this.timeCreated = System.currentTimeMillis();
        this.lastUpdated = System.currentTimeMillis();
    }
}