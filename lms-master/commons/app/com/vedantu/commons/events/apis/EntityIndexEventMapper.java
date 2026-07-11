package com.vedantu.commons.events.apis;

import java.util.HashMap;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;

public class EntityIndexEventMapper extends HashMap<EntityType, EventType> {

    private static final long                  serialVersionUID = 1L;
    public final static EntityIndexEventMapper INSTANCE         = new EntityIndexEventMapper();

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
