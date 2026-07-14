package com.vedantu.content.daos.analytics;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.mutable.MutableLong;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.models.analytics.UserEntityAttempt;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuRecordState;

public class UserEntityAttemptDAO extends VedantuBasicDAO<UserEntityAttempt, ObjectId> {

    private static final ALogger             LOGGER   = Logger.of(UserEntityAttemptDAO.class);

    public static final UserEntityAttemptDAO INSTANCE = new UserEntityAttemptDAO();

    private UserEntityAttemptDAO() {

        super(UserEntityAttempt.class);
    }

    public UserEntityAttempt getAttempt(String userId, EntityType entityType, String entityId) {

        LOGGER.debug("getAttempt userId: " + userId + ", entityType: " + entityType + ", entityId"
                + entityId);

        Query<UserEntityAttempt> query = getQuery().filter(ConstantsGlobal.USER_ID, userId)
                .filter("entity.type", entityType).filter("entity.id", entityId);

        UserEntityAttempt userEntityAttempt = query.get();
        LOGGER.info("getAttempt userEntityAttempt: " + userEntityAttempt);

        return userEntityAttempt;
    }
    public UserEntityAttempt getAttemptUsingTestStatus(String userId, EntityType entityType, String entityId,String testStatus) {

        LOGGER.debug("getAttempt userId: " + userId + ", entityType: " + entityType + ", entityId"
                + entityId+", testStatus"+testStatus);

        Query<UserEntityAttempt> query = getQuery().filter(ConstantsGlobal.USER_ID, userId)
                .filter("entity.type", entityType).filter("entity.id", entityId).filter("testStatus", testStatus);
        
        LOGGER.info("query : "+query);
        UserEntityAttempt userEntityAttempt = query.get();
        LOGGER.info("getAttempt userEntityAttempt: " + userEntityAttempt);

        return userEntityAttempt;
    }

    public List<UserEntityAttempt> getUserAttempts( EntityType entityType, String entityId) {

        LOGGER.debug("getAttempts entityType: " + entityType + ", entityId"
                + entityId);

        Query<UserEntityAttempt> query = getQuery().filter("entity.type", entityType).filter("entity.id", entityId);

        List<UserEntityAttempt> userEntityAttempts = query.asList();
        LOGGER.info("getAttempt userEntityAttempt: " + userEntityAttempts);

        return userEntityAttempts;
    }


    public UserEntityAttempt addAttempt(String userId, String orgId, EntityType entityType, String entityId,
            List<String> qIds, SrcEntity parent) {

        return addAttempt(userId, orgId, entityType, entityId, qIds, parent, 0);
    }

    public UserEntityAttempt addAttempt(String userId, String orgId, EntityType entityType, String entityId,
            List<String> qIds, SrcEntity parent, long endTime) {

        LOGGER.debug("addAttempt userId: " + userId + ", entityType: " + entityType + ", entityId"
                + entityId + ", qIds.size: " + (qIds == null ? 0 : CollectionUtils.size(qIds)));

        UserEntityAttempt userEntityAttempt = new UserEntityAttempt(userId, orgId, new SrcEntity(
                entityType, entityId), qIds);

        userEntityAttempt.parent = parent;
        userEntityAttempt.endTime = endTime;
        save(userEntityAttempt);

        LOGGER.info("addAttempt userEntityAttempt: " + userEntityAttempt);

        return userEntityAttempt;
    }

    public UserEntityAttempt endAttempt(String userId, EntityType entityType, String entityId,
            String attemptId) throws VedantuException {

        return endAttempt(userId, entityType, entityId, attemptId, 0);
    }

    public UserEntityAttempt endAttempt(String userId, EntityType entityType, String entityId,
            String attemptId, long endTime) throws VedantuException {

        return endAttempt(userId, entityType, entityId, attemptId, endTime, false);
    }

    public UserEntityAttempt endAttempt(String userId, EntityType entityType, String entityId,
            String attemptId, long endTime, boolean checkIfAlreadyEnded) throws VedantuException {

        return endAttempt(userId, entityType, entityId, attemptId, endTime, checkIfAlreadyEnded,
                false);
    }

    public UserEntityAttempt endAttempt(String userId, EntityType entityType, String entityId,
            String attemptId, long endTime, boolean checkIfAlreadyEnded, boolean ignoreEndTime)
            throws VedantuException {

        LOGGER.debug("endAttempt userId: " + userId + ", entityType: " + entityType + ", entityId"
                + entityId + ", attemptId: " + attemptId);

        UserEntityAttempt userEntityAttempt = getById(attemptId);
        if (null == userEntityAttempt) {
            LOGGER.error("endAttempt userEntityAttempt not found for attemptId: " + attemptId);
            throw new VedantuException(VedantuErrorCode.ATTEMPT_NOT_FOUND);
        }
        if (checkIfAlreadyEnded
                && (userEntityAttempt.finished || (!ignoreEndTime && userEntityAttempt.endTime > 0 && userEntityAttempt.endTime <= endTime))) {
            LOGGER.error("endAttempt userEntityAttempt already ended for attemptId: " + attemptId);
            throw new VedantuException(VedantuErrorCode.ALREADY_ATTEMPTED);
        }

        userEntityAttempt.endTime = endTime > 0 ? endTime : System.currentTimeMillis();
        userEntityAttempt.finished = true;
        userEntityAttempt.testStatus = "FINISHED";
        save(userEntityAttempt);

        LOGGER.info("endAttempt userEntityAttempt: " + userEntityAttempt);

        return userEntityAttempt;
    }

    public List<UserEntityAttempt> getUserAttemptsList( EntityType entityType, String entityId, String orgId, int start, int size, MutableLong hits, List<String> userIds ) {

        LOGGER.debug("getAttempts entityType: " + entityType + ", entityId"
                + entityId);

        Query<UserEntityAttempt> query = getQuery().filter("entity.type", entityType).filter("entity.id", entityId).filter("orgId", orgId).offset(start).limit(size);
        if(!userIds.isEmpty()){
            query.field("userId").in(userIds);
        }
        List<UserEntityAttempt> userEntityAttempts = query.asList();
        LOGGER.info("getAttempt getUserAttemptsList: " + userEntityAttempts);
        if (hits != null) {
            hits.setValue(query.countAll());
        }
        return userEntityAttempts;
    }

    public List<UserEntityAttempt> getAllTestAttemptsList(EntityType entityType, String entityId) {

        LOGGER.debug("getAllTestAttempts entityType: " + entityType + ", entityId" + entityId);

        Query<UserEntityAttempt> query = getQuery().filter("entity.type", entityType)
                .filter("entity.id", entityId).filter("processed", true).filter("finished", true)
                .filter("testStatus", "FINISHED").filter("recordState","ACTIVE");

        List<UserEntityAttempt> userEntityAttempts = query.asList();
        LOGGER.info("getAttempt getUserAttemptsList: " + userEntityAttempts);

        return userEntityAttempts;
    }
    /*
	public List<UserEntityAttempt> getAllSubjectiveTestAttemptsList(
			EntityType entityType, String entityId, String orgId, int start,
			int size, MutableLong hits) {
		LOGGER.debug("getAllSubjectiveTestAttempts entityType: " + entityType
				+ ", entityId" + entityId);

		Query<UserEntityAttempt> query = getQuery()
				.filter("entity.type", entityType)
				.filter("entity.id", entityId)
				.filter("finished", true)
				.filter("testStatus", "FINISHED")
				.filter("recordState", VedantuRecordState.ACTIVE);
		LOGGER.info("getAllSubjectiveTestAttempts size" + size);
		LOGGER.info("getAllSubjectiveTestAttempts start" + start);
		if (size > 0) {
			query.offset(start).limit(size);
		}
		if(!orgId.equals(null)){
			query.filter("orgId",orgId);
		}
		if (hits != null) {
			hits.setValue(query.countAll());
		}
		List<UserEntityAttempt> userEntityAttempts = query.asList();
		LOGGER.info("getAllSubjectiveTestAttempts getUserAttemptsList: "
				+ userEntityAttempts);
		LOGGER.info("getAllSubjectiveTestAttempts hits" + hits.getValue());
		return userEntityAttempts;
	}
	*/

    public UserEntityAttempt removeUserEntityAttempt(String userId, SrcEntity target) throws VedantuException {

        UserEntityAttempt userEntityAttempt = getQuery().filter(ConstantsGlobal.USER_ID, userId)
                        .filter(ConstantsGlobal.ENTITY, target).get();

        LOGGER.debug("deleted userEntityAttempt : " + userEntityAttempt);
        if (userEntityAttempt == null) {
            throw new VedantuException(VedantuErrorCode.ATTEMPT_NOT_FOUND);
        }else {
            delete(userEntityAttempt);
        }
        return userEntityAttempt;
    }

    public long getUserTestAttemptStatusCount(String orgId, String entityId, EntityType entityType, String testStatus) {
        // TODO Auto-generated method stub
        return getQuery().filter("entity.type", entityType).filter("entity.id", entityId).filter("orgId", orgId).filter("testStatus",testStatus).countAll();
    }

}
