package com.vedantu.content.models;

import com.google.code.morphia.annotations.Entity;
import com.vedantu.mongo.VedantuBaseMongoModel;

@Entity(value = "virtualclassrooms", noClassnameStored = true)
public class VirtualClassroom extends VedantuBaseMongoModel {

    public String  description;
    public long    startTime;
    public long    endTime;
    public boolean recordClass;
    public boolean cancelled;
    public boolean audioOnly;
    public String  uuid;
    public String  userId;
    public String  orgId;

    public VirtualClassroom() {
        super();
    }

    public VirtualClassroom(String description, long startTime, long endTime,
            boolean recordClass, boolean cancelled, boolean audioOnly, String userId, String orgId) {
        super();
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.recordClass = recordClass;
        this.audioOnly = audioOnly;
        this.cancelled = cancelled;
        this.userId = userId;
        this.orgId = orgId;
    }
}
