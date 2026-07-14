package com.vedantu.user.pojos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.FollowType;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.VedantuDBResult;
import com.vedantu.user.daos.AbstractUserActionDAO;
import com.vedantu.user.models.EntityUserActionMapping;

public class EntityUserActionDAO extends AbstractUserActionDAO<EntityUserActionMapping, ObjectId> {

    private static final ALogger                         LOGGER   = Logger.of(EntityUserActionDAO.class);

    public static final EntityUserActionDAO              INSTANCE = new EntityUserActionDAO();
    private static Map<UserActionType, VedantuErrorCode> alreadyPresentErrorCodeMap;
    private static Map<UserActionType, VedantuErrorCode> notPresentErrorCodeMap;

    static {
        initMappingAlreadyPresentErrorMap();
        initMappingNotPresentErrorMap();
    }

    private EntityUserActionDAO() {

        super(EntityUserActionMapping.class);
    }

    private static void initMappingAlreadyPresentErrorMap() {

        alreadyPresentErrorCodeMap = new HashMap<UserActionType, VedantuErrorCode>();
        alreadyPresentErrorCodeMap.put(UserActionType.VOTED, VedantuErrorCode.ALREADY_VOTED);
        alreadyPresentErrorCodeMap.put(UserActionType.COMPLETED, VedantuErrorCode.ALREADY_COMPLETED);
        alreadyPresentErrorCodeMap
                .put(UserActionType.FOLLOWING, VedantuErrorCode.ALREADY_FOLLOWING);
    }

    private static void initMappingNotPresentErrorMap() {

        notPresentErrorCodeMap = new HashMap<UserActionType, VedantuErrorCode>();
        notPresentErrorCodeMap.put(UserActionType.FOLLOWING, VedantuErrorCode.NOT_FOLLOWING);
    }

    public EntityUserActionMapping
            addEntityUserActionMapping(String userId, UserActionType actionType, SrcEntity target,
                    SrcEntity context, boolean allowDuplicates) throws VedantuException {

        Query<EntityUserActionMapping> query = getQuery().filter(ConstantsGlobal.USER_ID, userId)
                .filter(ConstantsGlobal.ACTION_TYPE, actionType)
                .filter(ConstantsGlobal.TARGET, target);

        if (context != null) {
            query = query.filter(ConstantsGlobal.CONTEXT, context);
        }

        EntityUserActionMapping userActionMapping = query.get();
        if (userActionMapping != null && !allowDuplicates) {
            throw new VedantuException(alreadyPresentErrorCodeMap.get(actionType));
        }
        userActionMapping = new EntityUserActionMapping(userId, actionType, target, context);
        LOGGER.debug("saving userActionMapping : " + userActionMapping);

        save(userActionMapping);
        return userActionMapping;
    }

    public boolean getUserModuleEntryStatus(String userId, SrcEntity target, SrcEntity context)
            throws VedantuException {

        EntityUserActionMapping userActionMapping = getQuery()
                .filter(ConstantsGlobal.USER_ID, userId)
                .filter(ConstantsGlobal.ACTION_TYPE, UserActionType.COMPLETED)
                .filter(ConstantsGlobal.TARGET, target)
                .filter(ConstantsGlobal.CONTEXT, context).get();
        return (userActionMapping != null);
    }

    public EntityUserActionMapping addEntityUserActionMapping(String userId,
            UserActionType actionType, SrcEntity target, boolean allowDuplicates)
            throws VedantuException {

        return addEntityUserActionMapping(userId, actionType, target, null, allowDuplicates);
    }

    public List<SrcEntity> sync(String userId, String moduleId, List<SrcEntity> entities)
            throws VedantuException {

        LOGGER.debug(".......Inside sync function......." + moduleId);

        List<EntityUserActionMapping> entitiesUserActionMapping = getQuery()
                .filter("userId", userId).filter("context.type", EntityType.MODULE)
                .filter("context.id", moduleId).filter("actionType", UserActionType.COMPLETED)
                .asList();

        List<SrcEntity> gobalEntities = new ArrayList<SrcEntity>();

        if (!CollectionUtils.isEmpty(entitiesUserActionMapping)) {
            for (EntityUserActionMapping entityUserActionMapping : entitiesUserActionMapping) {
                gobalEntities.add(entityUserActionMapping.target);
            }
        }

        if (!CollectionUtils.isEmpty(entities)) {
            LOGGER.debug(".......Entities not null.......");
            for (SrcEntity entity : entities) {
                if (CollectionUtils.isEmpty(gobalEntities) || !gobalEntities.contains(entity)) {
                    LOGGER.debug(".......Inside if statement.......");
                    addEntityUserActionMapping(userId, UserActionType.COMPLETED, entity,
                            new SrcEntity(EntityType.MODULE, moduleId), false);
                }
            }
        }
        // if (!CollectionUtils.isEmpty(gobalEntities)) {
        // gobalEntities.removeAll(entities);
        // }
        return gobalEntities;
    }

    public EntityUserActionMapping removeEntityUserActionMapping(String userId,
            UserActionType actionType, SrcEntity target) throws VedantuException {
        LOGGER.debug("UserId is "+userId+" actionType is "+actionType+" target is "+target);
        Query<EntityUserActionMapping> query = getQuery().filter(ConstantsGlobal.USER_ID, userId)
                .filter(ConstantsGlobal.ACTION_TYPE, actionType)
                .filter(ConstantsGlobal.TARGET, target);

        EntityUserActionMapping userActionMapping = query.get();
        LOGGER.debug("deleted userActionMapping : " + userActionMapping);
        if (userActionMapping == null) {
            LOGGER.debug("EntityUserActionMapping not found");
        }else {
            delete(userActionMapping);
        }
        return userActionMapping;
    }

    public List<String> getEntityUserActionByIds(SrcEntity target, UserActionType actionType,
            int start, int size, String orderBy, String sortOrder, MutableInt totalHits) {

        DBObject query = new BasicDBObject(ConstantsGlobal.TARGET_DOT_ID, target.id);
        query.put(ConstantsGlobal.TARGET_DOT_TYPE, target.type.name());
        query.put(ConstantsGlobal.ACTION_TYPE, actionType.name());
        if (StringUtils.isEmpty(orderBy)) {
            orderBy = ConstantsGlobal.TIME_CREATED;
        }
        DBObject sortQuery = MongoManager.getSortQuery(orderBy, sortOrder);
        VedantuDBResult<EntityUserActionMapping> results = getInfos(query,
                MongoManager.getFieldsDBObject(Arrays.asList(ConstantsGlobal.USER_ID),
                        MongoManager.INCLUDE_FIELD), start, size, sortQuery);
        if (totalHits != null) {
            totalHits.setValue(results.totalHits);
        }
        List<String> userIds = new ArrayList<String>();
        for (EntityUserActionMapping e : results.results) {
            userIds.add(e.userId);
        }
        return userIds;
    }

    public List<String> getUserEntityActionEntityIds(String userId, EntityType entityType,
            UserActionType actionType, int start, int size, String orderBy, String sortOrder,
            MutableInt totalHits) {

        DBObject query = new BasicDBObject(ConstantsGlobal.USER_ID, userId);
        query.put(ConstantsGlobal.ACTION_TYPE, actionType.name());
        query.put(ConstantsGlobal.TARGET + "." + ConstantsGlobal.TYPE, entityType.name());

        if (StringUtils.isEmpty(orderBy)) {
            orderBy = ConstantsGlobal.TIME_CREATED;
        }
        DBObject sortQuery = MongoManager.getSortQuery(orderBy, sortOrder);
        VedantuDBResult<EntityUserActionMapping> results = getInfos(query,
                MongoManager.getFieldsDBObject(Arrays.asList(ConstantsGlobal.TARGET),
                        MongoManager.INCLUDE_FIELD), start, size, sortQuery);
        totalHits.setValue(results.totalHits);
        List<String> entityIds = new ArrayList<String>();
        for (EntityUserActionMapping e : results.results) {
            entityIds.add(e.target.id);
        }
        return entityIds;
    }

    public boolean getEntityUpvote(String userId, String id) {

        Set<String> entitySet = new HashSet<String>();
        entitySet.add(id);

        Map<String, Boolean> upVoteMap = getEntityUpVoteMap(userId, entitySet);
        if (upVoteMap.get(id) != null) {
            return upVoteMap.get(id).booleanValue();
        }
        return false;
    }

    public Map<String, Boolean> getEntityUpVoteMap(String userId, Set<String> ids) {

        Map<String, Boolean> userActionMap = new HashMap<String, Boolean>();
        if (StringUtils.isEmpty(userId) || CollectionUtils.isEmpty(ids)) {
            return userActionMap;
        }
        DBObject query = new BasicDBObject(ConstantsGlobal.USER_ID, userId);
        query.put(ConstantsGlobal.ACTION_TYPE, UserActionType.VOTED.name());
        query.put(ConstantsGlobal.TARGET + "." + ConstantsGlobal.ID, new BasicDBObject(
                MongoManager.IN_QUERY, ids.toArray()));

        VedantuDBResult<EntityUserActionMapping> results = getInfos(query,
                MongoManager.getFieldsDBObject(Arrays.asList(ConstantsGlobal.TARGET),
                        MongoManager.INCLUDE_FIELD), MongoManager.NO_START, MongoManager.NO_LIMIT,
                null);

        for (EntityUserActionMapping e : results.results) {
            userActionMap.put(e.target.id, true);
        }
        return userActionMap;
    }

    public Map<String, Boolean> getEntityActionMap(String userId, Set<String> ids,
            UserActionType type) {

        Map<String, Boolean> userActionMap = new HashMap<String, Boolean>();
        if (StringUtils.isEmpty(userId) || CollectionUtils.isEmpty(ids)) {
            return userActionMap;
        }
        DBObject query = new BasicDBObject(ConstantsGlobal.USER_ID, userId);
        query.put(ConstantsGlobal.ACTION_TYPE, type.name());
        query.put(ConstantsGlobal.TARGET + "." + ConstantsGlobal.ID, new BasicDBObject(
                MongoManager.IN_QUERY, ids.toArray()));

        VedantuDBResult<EntityUserActionMapping> results = getInfos(query,
                MongoManager.getFieldsDBObject(Arrays.asList(ConstantsGlobal.TARGET),
                        MongoManager.INCLUDE_FIELD), MongoManager.NO_START, MongoManager.NO_LIMIT,
                null);

        for (EntityUserActionMapping e : results.results) {
            userActionMap.put(e.target.id, true);
        }
        return userActionMap;
    }

    public Map<String, FollowType> getEntityFollowTypeMap(String userId, EntityType entityType,
            Set<String> ids) {

        Map<String, FollowType> userEntityFollowTypeMap = new HashMap<String, FollowType>();
        if (StringUtils.isEmpty(userId) || CollectionUtils.isEmpty(ids)) {
            return userEntityFollowTypeMap;
        }
        DBObject query = new BasicDBObject(ConstantsGlobal.USER_ID, userId);
        query.put(ConstantsGlobal.ACTION_TYPE, UserActionType.FOLLOWING.name());
        query.put(ConstantsGlobal.TARGET + "." + ConstantsGlobal.ID, new BasicDBObject(
                MongoManager.IN_QUERY, ids.toArray()));

        VedantuDBResult<EntityUserActionMapping> results = getInfos(query,
                MongoManager.getFieldsDBObject(Arrays.asList(ConstantsGlobal.TARGET),
                        MongoManager.INCLUDE_FIELD), MongoManager.NO_START, MongoManager.NO_LIMIT,
                null);
        Set<String> followingEntityIds = new HashSet<String>();
        for (EntityUserActionMapping eMapping : results.results) {
            followingEntityIds.add(eMapping.target.id);
        }

        Set<String> followerEntityIds = new HashSet<String>();

        if (entityType == EntityType.USER) {
            // this block ensure addition of followType, entityType=USER
            query.put(ConstantsGlobal.USER_ID,
                    new BasicDBObject(MongoManager.IN_QUERY, ids.toArray()));
            query.put(ConstantsGlobal.TARGET + "." + ConstantsGlobal.ID, userId);
            VedantuDBResult<EntityUserActionMapping> rsp = getInfos(query,
                    MongoManager.getFieldsDBObject(Arrays.asList(ConstantsGlobal.USER_ID),
                            MongoManager.INCLUDE_FIELD), MongoManager.NO_START,
                    MongoManager.NO_LIMIT, null);
            for (EntityUserActionMapping eMapping : rsp.results) {
                followerEntityIds.add(eMapping.userId);
            }
        }

        for (String id : ids) {
            boolean isFollowing = followingEntityIds.contains(id);
            boolean isFollower = followerEntityIds.contains(id);
            FollowType followType = StringUtils.equals(userId, id) ? FollowType.YOU : (isFollowing
                    && isFollower ? FollowType.BOTH_WAYS : (isFollowing ? FollowType.FOLLOWING
                    : (isFollower ? FollowType.FOLLOWER : FollowType.NONE)));
            userEntityFollowTypeMap.put(id, followType);
        }
        return userEntityFollowTypeMap;
    }



}
