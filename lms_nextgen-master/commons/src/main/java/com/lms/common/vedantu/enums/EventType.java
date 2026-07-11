package com.lms.common.vedantu.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum EventType {

    // @formatter:off
    // END_TEST will end the test if the user/ui has not ended the test

    UNKNOWN, INDEX_QUESTION("com.lms.pojos.search.details.QuestionSearchIndexDetails",
            ""),
    PROCESS_DOUBTS,


    INDEX_CMDS_MODULE,

    INDEX_MODULE("com.lms.pojos.ModuleSearchIndexDetails",
            ""),

    INDEX_TEST,

    INDEX_CMDS_TEST,

    INDEX_ASSIGNMENT,

    INDEX_CMDS_ASSIGNMENT,

    ADD_SOLUTION("com.lms.user.vedantu.user.social.actions.event.details.SolutionDetails","com.lms.processors.AddSolutionProcessor"),

    INDEX_DISCUSSION,

    INDEX_CMDS_QUESTION,

    SHARE_ENTITY,

    INDEX_CHALLENGE,

    END_CHALLENGE,

    ADD_COMMENT,

    VOTE_ENTITY,

    ATTEMPT_ENTITY,

    FOLLOW_ENTITY,

    PUBLISH_ENTITY,

    // END_TEST will end the test if the user/ui has not ended the test
    END_TEST,

    UPLOAD_TEST_RESULT,

    SEND_EMAIL,

    SEND_INSTANT_EMAIL,

    POST_REMARK,

    // Comm services
    REMOVE_NEWS,

    INDEX_VIDEO,

    INDEX_CMDS_VIDEO,

    INDEX_DOCUMENT,

    INDEX_CMDS_DOCUMENT,

    CONVERT_DOCUMENT,

    INDEX_FILE,

    INDEX_CMDS_FILE,

    PROCESS_FILE,

    REINDEX_CMDS_RESOURCE,

    UPDATE_QUESTION_SET,

    CONVERT_VIDEO,

    UPLOAD_VIDEO,

    MADE_VISIBLE,

    EXPORT,

    MESSAGE_DISTRIBUTE,

    REMOVE_BOARD,

    UPLOAD_ATTEMPT_TO_ORG,

    CALCULATE_SIZE,

    SD_CARD_SPLIT

    ;

    // @formatter:on

    private static final Logger logger = LoggerFactory.getLogger(EventType.class);
    private final String eventDetailsClass;
    private final String eventProcessorClass;
    private final String eventNotificationProcessorClass;

    private boolean notificationEnabled = false;
    private boolean newsFeedGenerationEnabled = false;

    EventType() {

        this(null, null);
    }

    EventType(String eventProcessorClass) {

        this(null, eventProcessorClass);
    }

    EventType(String eventDetailsClass, String eventProcessorClass) {

        this(eventDetailsClass, eventProcessorClass, null);

    }

    EventType(String eventDetailsClass, String eventProcessorClass,
              boolean isNewFeedGenerationEnabled) {

        this(eventDetailsClass, eventProcessorClass, null);
        this.newsFeedGenerationEnabled = isNewFeedGenerationEnabled;

    }

    EventType(String eventDetailsClass, String eventProcessorClass,
              String eventNotificationProcessorClass) {

        this(eventDetailsClass, eventProcessorClass, eventNotificationProcessorClass, false);
    }

    EventType(String eventDetailsClass, String eventProcessorClass,
              String eventNotificationProcessorClass, boolean isNewsFeedGenerated) {

        this.eventDetailsClass = eventDetailsClass;
        this.eventProcessorClass = eventProcessorClass;
        this.eventNotificationProcessorClass = eventNotificationProcessorClass;
        if (eventNotificationProcessorClass != null) {
            notificationEnabled = true;
        }
        this.newsFeedGenerationEnabled = isNewsFeedGenerated;
    }

    public static boolean isValidEventType(String eventTypeValue) {

        boolean result = false;
        for (EventType eventType : values()) {
            if (eventTypeValue.equals(eventType.name())) {
                result = true;
                break;
            }
        }
        return result;
    }

    public static EventType getEventTypeByValue(String eventTypeName) {

        EventType entityType = UNKNOWN;
        try {
            entityType = EventType.valueOf(eventTypeName);
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage());
        }
        return entityType;
    }

    public boolean isSupported(String eventDetailsClass) {

        return null == this.eventDetailsClass || this.eventDetailsClass == eventDetailsClass;
    }

    public String getEventNotificationProcessorClass() {

        return eventNotificationProcessorClass;
    }

    public String getEventProcessorClass() {

        return eventProcessorClass;
    }

    public String getEventDetailsClass() {

        return eventDetailsClass;
    }

    public boolean isNotificationEnabled() {

        return notificationEnabled;
    }

    public boolean isNewsFeedGenerationEnabled() {

        return newsFeedGenerationEnabled;
    }

    public void enableNotification(boolean notificationEnabled) {

        this.notificationEnabled = notificationEnabled;
    }

    public void enableNewsFeedGeneration(boolean newsFeedGenerationEnabled) {

        this.newsFeedGenerationEnabled = newsFeedGenerationEnabled;
    }
}