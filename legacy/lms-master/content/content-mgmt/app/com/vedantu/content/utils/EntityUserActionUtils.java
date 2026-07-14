package com.vedantu.content.utils;

import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.relationships.EntityUserActionRelationshipSearchDetails;
import com.vedantu.content.daos.TestDAO;
import com.vedantu.content.enums.TestMode;
import com.vedantu.content.managers.AbstractContentManager;
import com.vedantu.content.models.tests.Test;
import com.vedantu.user.models.EntityUserActionMapping;
import com.vedantu.user.pojos.EntityUserActionDAO;
import com.vedantu.user.social.actions.event.details.AttemptDetails;
import com.vedantu.user.social.actions.event.details.FollowDetails;
import com.vedantu.user.social.actions.event.details.UserEntityActionDetails;
import com.vedantu.user.social.actions.event.details.VoteDetails;

public class EntityUserActionUtils extends AbstractContentManager {

    public static boolean addEntityUserAction(String userId, SrcEntity entity, SrcEntity context,
            UserActionType actionType, boolean allowDuplicates) throws VedantuException {

        return addEntityUserAction(userId, entity, context, actionType, allowDuplicates, true);
    }

    public static boolean addEntityUserAction(String userId, SrcEntity entity,
            UserActionType actionType, boolean allowDuplicates) throws VedantuException {

        return addEntityUserAction(userId, entity, null, actionType, allowDuplicates, true);
    }

    public static boolean addEntityUserAction(String userId, SrcEntity entity, SrcEntity context,
            UserActionType actionType, boolean allowDuplicates, boolean updateIndex)
            throws VedantuException {

        isSocialActionAllowed(entity.type, entity.id);
        EntityUserActionMapping actionMapping = EntityUserActionDAO.INSTANCE
                .addEntityUserActionMapping(userId, actionType, entity, context, allowDuplicates);
        if (actionType != UserActionType.VIEWED) {
            EntityUserActionRelationshipSearchDetails userActionDetails = new EntityUserActionRelationshipSearchDetails(
                    userId, entity.id);

            updateUserActionMappintToEs(userActionDetails, entity, actionType, EventActionType.ADD,
                    null);
        }
        updateEntityCount(actionMapping, 1);

        return true;
    }

    public static boolean addEntityUserAction(String userId, SrcEntity entity,
            UserActionType actionType, boolean allowDuplicates, boolean updateIndex)
            throws VedantuException {

        return addEntityUserAction(userId, entity, null, actionType, allowDuplicates, updateIndex);
    }

    public static boolean removeEntityUserAction(String userId, SrcEntity entity,
            UserActionType actionType) throws VedantuException {

        isSocialActionAllowed(entity.type, entity.id);
        EntityUserActionMapping actionMapping = EntityUserActionDAO.INSTANCE
                .removeEntityUserActionMapping(userId, actionType, entity);
        EntityUserActionRelationshipSearchDetails userActionDetails = new EntityUserActionRelationshipSearchDetails(
                userId, entity.id);
        updateUserActionMappintToEs(userActionDetails, entity, actionType, EventActionType.REMOVE,
                null);
        updateEntityCount(actionMapping, -1);
        return true;
    }

    private static void updateEntityCount(EntityUserActionMapping mapping, int inc)
            throws VedantuException {

        UserEntityActionDetails details = null;
        EventType eventType = null;
        switch (mapping.actionType) {
        case FOLLOWING:
            updateParentFollowersCount(mapping.userId, mapping.target, inc);
            details = new FollowDetails();
            eventType = EventType.FOLLOW_ENTITY;
            break;
        case VIEWED:
            updateViewsCount(mapping.userId, mapping.target, inc);
            break;
        case ATTEMPTED:
            updateAttemptsCount(mapping.userId, mapping.target, inc);
            boolean silent = false;
            if (mapping.target.type == EntityType.TEST) {
                Test test = TestDAO.INSTANCE.getById(mapping.target.id);
                if (test.mode == TestMode.OFFLINE) {
                    silent = true;
                }
            }
            details = new AttemptDetails(mapping.userId, eventType, mapping.target,
                    mapping._getStringId(), !silent);
            eventType = EventType.ATTEMPT_ENTITY;
            break;
        case VOTED:
            updateParentUpVotesCount(mapping.userId, mapping.target);
            details = new VoteDetails();
            eventType = EventType.VOTE_ENTITY;
            break;
        default:
            break;
        }

        if (details != null && eventType != null) {
            details.actionId = mapping._getStringId();
            details.target = mapping.target;
            details.userId = mapping.userId;
            details.eventType = eventType;
            if (inc > 0) {
                generateEventAysc(mapping.userId, details, eventType);
            }
        }

    }
}
