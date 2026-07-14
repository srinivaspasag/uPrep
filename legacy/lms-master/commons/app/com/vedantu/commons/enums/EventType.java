package com.vedantu.commons.enums;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;

public enum EventType {

    // @formatter:off

    UNKNOWN, INDEX_QUESTION("com.vedantu.content.search.details.QuestionSearchIndexDetails",
            "com.vedantu.eventbus.processors.QuestionProcessor"),

    INDEX_CMDS_MODULE("com.vedantu.cmds.models.event.search.details.CMDSModuleSearchIndexDetails",
            "com.vedantu.eventbus.processors.cmds.CMDSModuleSearchIndexProcessor"),

    INDEX_MODULE("com.vedantu.content.search.details.ModuleSearchIndexDetails",
            "com.vedantu.eventbus.processors.ModuleSearchIndexProcessor"),

    INDEX_TEST("com.vedantu.content.search.details.TestSearchIndexDetails",
            "com.vedantu.eventbus.processors.TestProcessor"),

    INDEX_CMDS_TEST("com.vedantu.cmds.models.event.search.details.CMDSTestSearchIndexDetails",
            "com.vedantu.eventbus.processors.cmds.CMDSTestSearchIndexProcessor"),

    INDEX_ASSIGNMENT("com.vedantu.content.search.details.AssignmentSearchIndexDetails",
            "com.vedantu.eventbus.processors.AssignmentSearchIndexProcessor"),

    INDEX_CMDS_ASSIGNMENT(
            "com.vedantu.cmds.models.event.search.details.CMDSAssignmentSearchIndexDetails",
            "com.vedantu.eventbus.processors.cmds.CMDSAssignmentSearchIndexProcessor"),

    ADD_SOLUTION("com.vedantu.user.social.actions.event.details.SolutionDetails",
            "com.vedantu.eventbus.processors.AddSolutionProcessor"),

    INDEX_DISCUSSION("com.vedantu.content.search.details.DiscussionSearchIndexDetails",
            "com.vedantu.eventbus.processors.discussions.DiscussionProcessor"),

    INDEX_CMDS_QUESTION(
            "com.vedantu.cmds.models.event.search.details.CMDSQuestionSearchIndexDetails",
            "com.vedantu.eventbus.processors.cmds.CmdsQuestionSearchIndexProcessor"),

    SHARE_ENTITY("com.vedantu.comm.event.details.ShareEntityDetails",
            "com.vedantu.eventbus.processors.ShareEntityProcessor"),

    INDEX_CHALLENGE("com.vedantu.content.search.details.ChallengeSearchIndexDetails",
            "com.vedantu.eventbus.processors.challenges.ChallengeProcessor"),

    END_CHALLENGE("com.vedantu.content.search.details.ChallengeSearchIndexDetails",
            "com.vedantu.eventbus.processors.challenges.PostChallengeProcessor"),

    ADD_COMMENT("com.vedantu.user.social.actions.event.details.CommentDetails",
            "com.vedantu.eventbus.processors.entities.CommentProcessor"),

    VOTE_ENTITY("com.vedantu.user.social.actions.event.details.VoteDetails",
            "com.vedantu.eventbus.processors.entities.VoteProcessor"),

    ATTEMPT_ENTITY("com.vedantu.user.social.actions.event.details.AttemptDetails",
            "com.vedantu.eventbus.processors.entities.AttemptProcessor"),

    FOLLOW_ENTITY("com.vedantu.user.social.actions.event.details.FollowDetails",
            "com.vedantu.eventbus.processors.entities.FollowProcessor"),

    PUBLISH_ENTITY("com.vedantu.cmds.models.event.details.EntityPublishingDetails",
            "com.vedantu.eventbus.processors.PublishProcessor"),

    // END_TEST will end the test if the user/ui has not ended the test
    END_TEST("com.vedantu.content.event.details.EndTestDetails",
            "com.vedantu.eventbus.processors.EndTestProcessor"),

    UPLOAD_TEST_RESULT("com.vedantu.cmds.models.event.details.OfflineTestResultUploadDetails",
            "com.vedantu.eventbus.processors.cmds.OfflineTestResultProcessor"),

    SEND_EMAIL("com.vedantu.commons.content.interfaces.AbstractEmailTemplateDetails",
            "com.vedantu.eventbus.processors.comm.EmailProcessor"),

    SEND_INSTANT_EMAIL("com.vedantu.commons.content.interfaces.AbstractEmailTemplateDetails",
            "com.vedantu.eventbus.processors.comm.EmailProcessor"),

    POST_REMARK("com.vedantu.comm.event.details.PostRemarkDetails",
            "com.vedantu.eventbus.processors.comm.PostRemarksProcessor"),

    // Comm services
    REMOVE_NEWS("com.vedantu.content.event.details.NewsRemoveDetails",
            "com.vedantu.eventbus.processors.comm.NewsRemoveProcessor"),

    INDEX_VIDEO("com.vedantu.content.search.details.VideoSearchIndexDetails",
            "com.vedantu.eventbus.processors.video.VideoProcessor"),

    INDEX_CMDS_VIDEO("com.vedantu.cmds.models.event.search.details.CMDSVideoSearchIndexDetails",
            "com.vedantu.eventbus.processors.cmds.CMDSVideoSearchIndexProcessor"),

    INDEX_DOCUMENT("com.vedantu.content.search.details.DocumentSearchIndexDetails",
            "com.vedantu.eventbus.processors.DocumentSearchIndexProcessor"),

    INDEX_CMDS_DOCUMENT(
            "com.vedantu.cmds.models.event.search.details.CMDSDocumentSearchIndexDetails",
            "com.vedantu.eventbus.processors.cmds.CMDSDocumentSearchIndexProcessor"),

    CONVERT_DOCUMENT("com.vedantu.cmds.models.event.details.DocumentEncodingDetails",
            "com.vedantu.eventbus.processors.document.DocumentProcessor"),

    INDEX_FILE("com.vedantu.content.search.details.FileSearchIndexDetails",
            "com.vedantu.eventbus.processors.FileSearchIndexProcessor"),

    INDEX_CMDS_FILE("com.vedantu.cmds.models.event.search.details.CMDSFileSearchIndexDetails",
            "com.vedantu.eventbus.processors.cmds.CMDSFileSearchIndexProcessor"),

    PROCESS_FILE("com.vedantu.cmds.models.event.details.FileProcessingDetails",
            "com.vedantu.eventbus.processors.file.FileProcessor"),

    REINDEX_CMDS_RESOURCE("com.vedantu.cmds.models.event.search.details.ReIndexDetails",
            "com.vedantu.eventbus.processors.cmds.ReIndexProcessor"),

    UPDATE_QUESTION_SET("com.vedantu.cmds.models.event.search.details.ReIndexDetails",
            "com.vedantu.eventbus.processors.cmds.ReIndexProcessor"),

    CONVERT_VIDEO("com.vedantu.cmds.models.event.details.VideoTranscodingDetails",
            "com.vedantu.eventbus.processors.video.VideoTranscodingProcessor"),

    UPLOAD_VIDEO("com.vedantu.content.event.details.UploadingVideoDetails",
            " com.vedantu.eventbus.processors.video.UploadVideoProcessor"),

    MADE_VISIBLE("com.vedantu.content.event.details.MadeVisibleDetails",
            "com.vedantu.eventbus.processors.cmds.MadeVisibleProcessor"),

    EXPORT("com.vedantu.cmds.models.event.details.ExportDetails",
            "com.vedantu.eventbus.processors.cmds.ExportProcessor"),

    MESSAGE_DISTRIBUTE("com.vedantu.comm.event.details.MessageDistributeDetails",
            "com.vedantu.eventbus.processors.comm.MessageDistributeProcessor"),

    REMOVE_BOARD("com.vedantu.board.event.details.BoardRemovalDetails",
            "com.vedantu.eventbus.processors.BoardRemovalProcessor"),

    UPLOAD_ATTEMPT_TO_ORG("com.vedantu.organization.event.ei.details.OrgAttemptUploadDetails",
            "com.vedantu.eventbus.processors.ei.OrgAttemptUploadProcessor"),

    CALCULATE_SIZE("com.vedantu.cmds.models.event.details.CalculateSizeDetails",
            "com.vedantu.eventbus.processors.EntitySizeCalculatorProcessor"),

    SD_CARD_SPLIT("com.vedantu.cmds.models.event.details.SDCardDetails",
            "com.vedantu.eventbus.processors.SDCardSplitter"),

    PROCESS_DOUBTS("com.vedantu.content.event.details.DoubtsProcessingDetails",
            "com.vedantu.eventbus.processors.doubts.DoubtsProcessor");

    // @formatter:on

    private static final ALogger LOGGER                    = Logger.of(EventType.class);
    private String               eventDetailsClass;
    private String               eventProcessorClass;
    private String               eventNotificationProcessorClass;

    private boolean              notificationEnabled       = false;
    private boolean              newsFeedGenerationEnabled = false;

    private EventType() {

        this(null, null);
    }

    private EventType(String eventProcessorClass) {

        this(null, eventProcessorClass);
    }

    private EventType(String eventDetailsClass, String eventProcessorClass) {

        this(eventDetailsClass, eventProcessorClass, null);

    }

    private EventType(String eventDetailsClass, String eventProcessorClass,
            boolean isNewFeedGenerationEnabled) {

        this(eventDetailsClass, eventProcessorClass, null);
        this.newsFeedGenerationEnabled = isNewFeedGenerationEnabled;

    }

    private EventType(String eventDetailsClass, String eventProcessorClass,
            String eventNotificationProcessorClass) {

        this(eventDetailsClass, eventProcessorClass, eventNotificationProcessorClass, false);
    }

    private EventType(String eventDetailsClass, String eventProcessorClass,
            String eventNotificationProcessorClass, boolean isNewsFeedGenerated) {

        this.eventDetailsClass = eventDetailsClass;
        this.eventProcessorClass = eventProcessorClass;
        this.eventNotificationProcessorClass = eventNotificationProcessorClass;
        if (eventNotificationProcessorClass != null) {
            notificationEnabled = true;
        }
        this.newsFeedGenerationEnabled = isNewsFeedGenerated;
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

    public static boolean isValidEventType(String eventTypeValue) {

        boolean result = false;
        for (EventType eventType : values()) {
            if (StringUtils.equals(eventTypeValue, eventType.name())) {
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
            LOGGER.error(e.getLocalizedMessage());
        }
        return entityType;
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
