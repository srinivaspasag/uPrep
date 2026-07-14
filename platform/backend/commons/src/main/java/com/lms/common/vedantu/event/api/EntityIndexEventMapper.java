package com.lms.common.vedantu.event.api;

import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.EventType;

import java.util.HashMap;

public class EntityIndexEventMapper extends HashMap<EntityType, EventType> {

    public final static EntityIndexEventMapper INSTANCE = new EntityIndexEventMapper();
    private static final long serialVersionUID = 1L;

    public EntityIndexEventMapper() {

        super();
        this.put(EntityType.DISCUSSION, EventType.INDEX_DISCUSSION);
        this.put(EntityType.QUESTION, EventType.INDEX_QUESTION);
        this.put(EntityType.TEST, EventType.INDEX_TEST);
        this.put(EntityType.ASSIGNMENT, EventType.INDEX_ASSIGNMENT);
        this.put(EntityType.VIDEO, EventType.INDEX_VIDEO);
        this.put(EntityType.DOCUMENT, EventType.INDEX_DOCUMENT);
        this.put(EntityType.FILE, EventType.INDEX_FILE);

        this.put(EntityType.CHALLENGE, EventType.INDEX_CHALLENGE);

        this.put(EntityType.CMDSQUESTION, EventType.INDEX_CMDS_QUESTION);
        this.put(EntityType.CMDSVIDEO, EventType.INDEX_CMDS_VIDEO);
        this.put(EntityType.CMDSASSIGNMENT, EventType.INDEX_CMDS_ASSIGNMENT);
        this.put(EntityType.CMDSTEST, EventType.INDEX_CMDS_TEST);
    }

}