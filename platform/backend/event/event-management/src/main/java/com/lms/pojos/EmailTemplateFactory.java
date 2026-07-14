package com.lms.pojos;

import com.lms.common.vedantu.enums.EventType;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class EmailTemplateFactory {

    public static final String TEMPLATE_CONFIGURATION_EMAIL_TEMPLATE = "emailTemplate";
    public static final EmailTemplateFactory INSTANCE = new EmailTemplateFactory();
    private static final String TEMPLATE_CONFIGURATION_MADE_VISIBLE_NOTIFICATION = "madeVisibleNotification";
    private static final String TEMPLATE_CONFIGURATION_UPLOAD_TEST_RESULT_NOTIFICATION = "uploadTestResultNotification";
    private static final String TEMPLATE_CONFIGURATION_REMARK_NOTIFICATION = "remarks";
    private static final String TEMPLATE_CONFIGURATION_END_CHALLENGE_NOTIFICATION = "endChallengeNotification";
    private static final String TEMPLATE_CONFIGURATION_ADD_SOLUTION_NOTIFICATION = "addSolutionNotification";
    private static final String TEMPLATE_CONFIGURATION_PUBLISH_NOTIFICATION = "publishNotification";
    private static final String TEMPLATE_CONFIGURATION_FOLLOW_NOTIFICATION = "followNotification";
    private static final String TEMPLATE_CONFIGURATION_ATTEMPT_NOTIFICATION = "attemptNotification";
    private static final String TEMPLATE_CONFIGURATION_VOTE_NOTIFICATION = "voteNotification";
    private static final String TEMPLATE_CONFIGURATION_COMMENT_NOTIFICATION = "commentNotification";
    private static final String TEMPLATE_CONFIGURATION_ADD_CHALLENGE_NOTIFICATION = "addChallengeNotification";
    private static final String TEMPLATE_PREFIX_KEY = "email.template.";
    private static Map<EventType, String> eventTypeToTemplateMap = null;

    private EmailTemplateFactory() {

        super();
        eventTypeToTemplateMap = new HashMap<EventType, String>();
        eventTypeToTemplateMap.put(EventType.VOTE_ENTITY,
                getConfigurationKey(TEMPLATE_CONFIGURATION_VOTE_NOTIFICATION));
        eventTypeToTemplateMap.put(EventType.ATTEMPT_ENTITY,
                getConfigurationKey(TEMPLATE_CONFIGURATION_ATTEMPT_NOTIFICATION));
        eventTypeToTemplateMap.put(EventType.FOLLOW_ENTITY,
                getConfigurationKey(TEMPLATE_CONFIGURATION_FOLLOW_NOTIFICATION));
        eventTypeToTemplateMap.put(EventType.PUBLISH_ENTITY,
                getConfigurationKey(TEMPLATE_CONFIGURATION_PUBLISH_NOTIFICATION));

        eventTypeToTemplateMap.put(EventType.ADD_SOLUTION,
                getConfigurationKey(TEMPLATE_CONFIGURATION_ADD_SOLUTION_NOTIFICATION));
        eventTypeToTemplateMap.put(EventType.END_CHALLENGE,
                getConfigurationKey(TEMPLATE_CONFIGURATION_END_CHALLENGE_NOTIFICATION));
        eventTypeToTemplateMap.put(EventType.POST_REMARK,
                getConfigurationKey(TEMPLATE_CONFIGURATION_REMARK_NOTIFICATION));
        eventTypeToTemplateMap.put(EventType.UPLOAD_TEST_RESULT,
                getConfigurationKey(TEMPLATE_CONFIGURATION_UPLOAD_TEST_RESULT_NOTIFICATION));
        eventTypeToTemplateMap.put(EventType.MADE_VISIBLE,
                getConfigurationKey(TEMPLATE_CONFIGURATION_MADE_VISIBLE_NOTIFICATION));

        eventTypeToTemplateMap.put(EventType.ADD_COMMENT,
                getConfigurationKey(TEMPLATE_CONFIGURATION_COMMENT_NOTIFICATION));

        eventTypeToTemplateMap.put(EventType.INDEX_CHALLENGE,
                getConfigurationKey(TEMPLATE_CONFIGURATION_ADD_CHALLENGE_NOTIFICATION));

    }

    public static String getConfigurationKey(String templateName) {

        return TEMPLATE_PREFIX_KEY + templateName;
    }

    public String getTemplateConfigurationKey(EventType eventType) {

        String confugurationKey = eventTypeToTemplateMap.get(eventType);
        if (StringUtils.isEmpty(confugurationKey)) {
        }
        return confugurationKey;
    }
}
