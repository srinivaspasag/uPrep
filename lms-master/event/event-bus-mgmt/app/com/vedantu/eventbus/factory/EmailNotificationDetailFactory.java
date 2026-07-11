package com.vedantu.eventbus.factory;

import java.util.HashMap;
import java.util.Map;

import play.Logger;

import com.vedantu.commons.enums.EventType;
import com.vedantu.eventbus.email.details.AbstractEmailNotificationDetails;
import com.vedantu.eventbus.email.details.AddChallengeEmailNotificationDetails;
import com.vedantu.eventbus.email.details.AddSolutionEmailNotificationDetails;
import com.vedantu.eventbus.email.details.AttemptEmailNotificationDetails;
import com.vedantu.eventbus.email.details.CommentEmailNotificationDetails;
import com.vedantu.eventbus.email.details.EndChallengeEmailNotificationDetails;
import com.vedantu.eventbus.email.details.FollowEmailNotificationDetails;
import com.vedantu.eventbus.email.details.MadeVisibleEntityEmailNotificationDetails;
import com.vedantu.eventbus.email.details.RemarkEmailNotificationDetails;
import com.vedantu.eventbus.email.details.UploadTestResultEmailNotificationDetails;
import com.vedantu.eventbus.email.details.VoteEmailNotificationDetails;

public class EmailNotificationDetailFactory {

    private Map<EventType, Class<? extends AbstractEmailNotificationDetails>> detailsMap;
    public static final EmailNotificationDetailFactory                INSTANCE = new EmailNotificationDetailFactory();

    private EmailNotificationDetailFactory() {

        detailsMap = new HashMap<EventType, Class<? extends AbstractEmailNotificationDetails>>();
        detailsMap.put(EventType.VOTE_ENTITY, VoteEmailNotificationDetails.class);
        detailsMap.put(EventType.ATTEMPT_ENTITY, AttemptEmailNotificationDetails.class);
        detailsMap.put(EventType.FOLLOW_ENTITY, FollowEmailNotificationDetails.class);
        detailsMap.put(EventType.PUBLISH_ENTITY, MadeVisibleEntityEmailNotificationDetails.class);

        detailsMap.put(EventType.ADD_SOLUTION, AddSolutionEmailNotificationDetails.class);
        detailsMap.put(EventType.END_CHALLENGE, EndChallengeEmailNotificationDetails.class);
        detailsMap.put(EventType.POST_REMARK, RemarkEmailNotificationDetails.class);
        detailsMap
                .put(EventType.UPLOAD_TEST_RESULT, UploadTestResultEmailNotificationDetails.class);
        
        detailsMap
        .put(EventType.MADE_VISIBLE, MadeVisibleEntityEmailNotificationDetails.class);
        
        detailsMap.put(EventType.ADD_COMMENT,CommentEmailNotificationDetails.class);
        detailsMap.put(EventType.INDEX_CHALLENGE,AddChallengeEmailNotificationDetails.class);
        detailsMap.put(EventType.END_CHALLENGE,EndChallengeEmailNotificationDetails.class);

    }

    public AbstractEmailNotificationDetails getEmailNotificationDetails(EventType eventType) {

        Class<?> clazz = detailsMap.get(eventType);
        if (clazz == null) {
            return null;
        }
        AbstractEmailNotificationDetails details = null;
        try {
            details = (AbstractEmailNotificationDetails) clazz.newInstance();
        } catch (InstantiationException e) {
            Logger.error(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            Logger.error(e.getMessage(), e);
        }
        return details;
    }
}
