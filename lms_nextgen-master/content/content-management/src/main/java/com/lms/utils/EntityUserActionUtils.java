package com.lms.utils;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.relationships.EntityUserActionRelationshipSearchDetails;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.EventType;
import com.lms.common.vedantu.enums.UserActionType;
import com.lms.managers.AbstractContentManager;
import com.lms.models.EntityUserActionMapping;
import com.lms.repository.EntityUserActionMappingRepo;
import com.lms.user.vedantu.user.social.actions.event.details.FollowDetails;
import com.lms.user.vedantu.user.social.actions.event.details.UserEntityActionDetails;
import com.lms.user.vedantu.user.social.actions.event.details.VoteDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class EntityUserActionUtils extends AbstractContentManager {
    private final static Logger logger = LoggerFactory.getLogger(EntityUserActionUtils.class);
    private static Map<UserActionType, VedantuErrorCode> alreadyPresentErrorCodeMap;
    @Autowired
    private EntityUserActionMappingRepo entityUserActionMappingRepo;
    @Autowired
    private MongoTemplate mongoTemplate;

    public boolean addEntityUserAction(String userId, SrcEntity entity, SrcEntity context,
                                       UserActionType actionType, boolean allowDuplicates) throws VedantuException {

        return addEntityUserAction(userId, entity, context, actionType, allowDuplicates, true);
    }

    public boolean addEntityUserAction(String userId, SrcEntity entity,
                                       UserActionType actionType, boolean allowDuplicates) throws VedantuException {

        return addEntityUserAction(userId, entity, null, actionType, allowDuplicates, true);
    }

    public boolean addEntityUserAction(String userId, SrcEntity entity, SrcEntity context,
                                       UserActionType actionType, boolean allowDuplicates, boolean updateIndex)
            throws VedantuException {

        isSocialActionAllowed(entity.type, entity.id);
        EntityUserActionMapping actionMapping = addEntityUserActionMapping(userId, actionType, entity, context, allowDuplicates);
        if (actionType != UserActionType.VIEWED) {
            EntityUserActionRelationshipSearchDetails userActionDetails = new EntityUserActionRelationshipSearchDetails(
                    userId, entity.id);

            updateUserActionMappintToEs(userActionDetails, entity, actionType, UserActionType.EventActionType.ADD,
                    null);
        }
        updateEntityCount(actionMapping, 1);

        return true;
    }

    public boolean addEntityUserAction(String userId, SrcEntity entity,
                                       UserActionType actionType, boolean allowDuplicates, boolean updateIndex)
            throws VedantuException {

        return addEntityUserAction(userId, entity, null, actionType, allowDuplicates, updateIndex);
    }

   /* public void isSocialActionAllowed(EntityType entityType, String entityId)
            throws VedantuException {
        List<EntityUserActionMapping> entityUserActionMapping = entityUserActionMappingRepo.findByTargetIdAndTargetType(entityId, entityType);
        if (entityUserActionMapping.isEmpty()) {
            throw new VedantuException(VedantuErrorCode.ACTION_NOT_ALLOWED, "no " + entityType
                    + " found for id[" + entityId + "]");
        }

    }*/

    public EntityUserActionMapping addEntityUserActionMapping(String userId, UserActionType actionType, SrcEntity target,
                                                              SrcEntity context, boolean allowDuplicates) throws VedantuException {
        Criteria criteria = new Criteria();
        Query query = new Query();
        criteria.and(ConstantsGlobal.USER_ID).is(userId);
        criteria.and(ConstantsGlobal.ACTION_TYPE).is(actionType);
        criteria.and(ConstantsGlobal.TARGET).is(target);
        if (context != null) {
            criteria.and(ConstantsGlobal.CONTEXT).is(context);
        }
        //Need to test
        List<EntityUserActionMapping> userActionMapping = mongoTemplate.find(query, EntityUserActionMapping.class);

        if (userActionMapping != null && !allowDuplicates) {
            throw new VedantuException(alreadyPresentErrorCodeMap.get(actionType));
        }
        userActionMapping.add(new EntityUserActionMapping(userId, actionType, target, context));

        entityUserActionMappingRepo.save(userActionMapping.get(0));
        return userActionMapping.get(0);
    }

    private void updateEntityCount(EntityUserActionMapping mapping, int inc)
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
               /* updateAttemptsCount(mapping.userId, mapping.target, inc);
                boolean silent = false;
                if (mapping.target.type == EntityType.TEST) {
                    Test test = TestDAO.INSTANCE.getById(mapping.target.id);
                    if (test.mode == TestMode.OFFLINE) {
                        silent = true;
                    }
                }
                details = new AttemptDetails(mapping.userId, eventType, mapping.target,
                        mapping._getStringId(), !silent);
                eventType = EventType.ATTEMPT_ENTITY;*/
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

    public EntityUserActionMapping addEntityUserActionMapping(String userId,
                                                              UserActionType actionType, SrcEntity target, boolean allowDuplicates)
            throws VedantuException {

        return addEntityUserActionMapping(userId, actionType, target, null, allowDuplicates);
    }

    public boolean removeEntityUserAction(String userId, SrcEntity entity,
                                          UserActionType actionType) throws VedantuException {

        isSocialActionAllowed(entity.type, entity.id);
        EntityUserActionMapping actionMapping = removeEntityUserActionMapping(userId, actionType, entity);
        EntityUserActionRelationshipSearchDetails userActionDetails = new EntityUserActionRelationshipSearchDetails(
                userId, entity.id);
        updateUserActionMappintToEs(userActionDetails, entity, actionType, UserActionType.EventActionType.REMOVE,
                null);
        updateEntityCount(actionMapping, -1);
        return true;
    }

    public EntityUserActionMapping removeEntityUserActionMapping(String userId,
                                                                 UserActionType actionType, SrcEntity target) throws VedantuException {
        logger.debug("UserId is " + userId + " actionType is " + actionType + " target is " + target);
        Query query = new Query();
        EntityUserActionMapping entityUserActionMapping = entityUserActionMappingRepo.findByUserIdAndActionTypeAndTargetIdAndTargetType(userId, actionType, target.getId(), target.getType());
        if (entityUserActionMapping == null) {
            logger.debug("EntityUserActionMapping not found");
        } else {
            entityUserActionMappingRepo.delete(entityUserActionMapping);
        }
        return entityUserActionMapping;
    }


}
