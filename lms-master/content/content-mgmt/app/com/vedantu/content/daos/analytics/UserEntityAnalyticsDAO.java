package com.vedantu.content.daos.analytics;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableLong;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryImpl;
import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.models.analytics.AcademicDimension;
import com.vedantu.content.models.analytics.AcademicDimensionType;
import com.vedantu.content.models.analytics.EntityMeasures;
import com.vedantu.content.models.analytics.UserEntityAnalytics;
import com.vedantu.content.models.analytics.UserEntityAttempt;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.MongoManager.SortOrder;
import com.vedantu.mongo.VedantuBasicDAO;

public class UserEntityAnalyticsDAO extends VedantuBasicDAO<UserEntityAnalytics, ObjectId> {

    private static final ALogger               LOGGER   = Logger.of(UserEntityAnalyticsDAO.class);

    public static final UserEntityAnalyticsDAO INSTANCE = new UserEntityAnalyticsDAO();

    private UserEntityAnalyticsDAO() {

        super(UserEntityAnalytics.class);
    }

    public UserEntityAnalytics addAnalytics(String userId, SrcEntity entity,
            AcademicDimensionType acadDimType, String acadDimId, EntityMeasures measures,
                                            double percentageScore, String orgId) {

        Query<UserEntityAnalytics> query = getQuery().filter(ConstantsGlobal.USER_ID, userId)
                .filter(ConstantsGlobal.ENTITY_DOT_ID, entity.id)
                .filter(ConstantsGlobal.ENTITY_DOT_TYPE, entity.type)
                .filter(ACAD_DIM_TYPE, acadDimType).filter(ACAD_DIM_ID, acadDimId);

        UserEntityAnalytics userEntityAnalytics = findOne(query);
        if (userEntityAnalytics == null) {
            userEntityAnalytics = new UserEntityAnalytics(userId, new AcademicDimension(
                    acadDimType, acadDimId), measures, null, entity, percentageScore, orgId);
        } else {
            userEntityAnalytics.measures.attempts += measures.attempts;
            userEntityAnalytics.measures.correct += measures.correct;
            userEntityAnalytics.measures.partial += measures.partial;
            userEntityAnalytics.measures.incorrect += measures.incorrect;
            userEntityAnalytics.measures.left += measures.left;
            userEntityAnalytics.measures.score += measures.score;
            userEntityAnalytics.measures.timeTaken += measures.timeTaken;
            // if ((userEntityAnalytics.percentage > 0 && percentageScore < 0)
            // || (userEntityAnalytics.percentage < 0 && percentageScore > 0)) {
            // // this is just to reset the percentage score in case of duplicate score
            // userEntityAnalytics.percentage = Math.abs(percentageScore);
            // } else

            // get total test attempted by the user
            Query<UserEntityAttempt> attemptCountQuery = UserEntityAttemptDAO.INSTANCE
                    .createQuery().filter(ConstantsGlobal.USER_ID, userId)
                    .field(ConstantsGlobal.ENTITY_DOT_ID).notEqual(entity.id)
                    .filter(ConstantsGlobal.ENTITY_DOT_TYPE, entity.type).filter("finished", true);
            long attemptCount = UserEntityAttemptDAO.INSTANCE.count(attemptCountQuery);
            if (attemptCount == 0) {
                attemptCount = 1;// 1st attempt
            }
            userEntityAnalytics.percentage = ((userEntityAnalytics.percentage * (attemptCount - 1)) + percentageScore)
                    / attemptCount;
            userEntityAnalytics.orgId = orgId;
        }
        userEntityAnalytics.lastUpdated = System.currentTimeMillis();
        save(userEntityAnalytics);
        // update analytics to corresponding overall data
        // entity{TYPE:TEST,id:OVERALL}
        if (entity.id != AcademicDimensionType.OVERALL.name()) {
            addAnalytics(userId, new SrcEntity(entity.type, AcademicDimensionType.OVERALL.name()),
                    acadDimType, acadDimId, measures, percentageScore, orgId);
        }

        return userEntityAnalytics;
    }

    public UserEntityAnalytics getAnalytics(String userId, String attemptId, SrcEntity entity,
            AcademicDimensionType acadDimType, String acadDimId) {

        LOGGER.debug("getAnalytics userId: " + userId + ", acadDimType: " + acadDimType
                + ", acadDimId: " + acadDimId);
        Query<UserEntityAnalytics> query = getQuery();
        if (StringUtils.isNotEmpty(attemptId)) {
            query.filter(ConstantsGlobal.ATTEMPT_ID, attemptId);
        }
        UserEntityAnalytics entityUserAnalytics = query.filter(ConstantsGlobal.USER_ID, userId)
                .filter(ConstantsGlobal.ENTITY_DOT_ID, entity.id)
                .filter(ACAD_DIM_TYPE, acadDimType).filter(ACAD_DIM_ID, acadDimId).get();

        LOGGER.info("getAnalytics entityUserAnalytics: " + entityUserAnalytics);
        return entityUserAnalytics;
    }

    public long getAnalyticsCount(String orgId, SrcEntity entity, String acadDimId) {

        return count(getQuery().filter(ConstantsGlobal.ORG_ID, orgId)
                .filter(ConstantsGlobal.ENTITY_DOT_ID, entity.id)
                .filter(ACAD_DIM_ID, acadDimId));
    }

    public long getAnalyticsCount(SrcEntity entity, String acadDimId) {

        return count(getQuery().filter(ConstantsGlobal.ENTITY_DOT_ID, entity.id)
                .filter(ACAD_DIM_ID, acadDimId));
    }

    public int getRank(String entityId, double score, long timeTaken, String acadDimId) {

        QueryImpl<UserEntityAnalytics> query = (QueryImpl<UserEntityAnalytics>) getQuery()
                .filter(ConstantsGlobal.ENTITY_DOT_ID, entityId)
                .filter(ConstantsGlobal.ACAD_DIM_DOT_ID, acadDimId).field("measures.score")
                .greaterThanOrEq(score);

        DBObject matchQuery = new BasicDBObject("$match", query.getQueryObject());

        String groupQueryString = "{\"$group\" : {_id: {score: \"$measures.score\", timeTaken : \"$measures.timeTaken\"}}}";

        DBObject groupQuery = (DBObject) JSON.parse(groupQueryString);

        DBObject sortQuery = MongoManager.getSortQuery("_id.score", SortOrder.DESC.name());
        sortQuery.putAll(MongoManager.getSortQuery("_id.timeTaken", SortOrder.ASC.name()));

        AggregationOutput aggregationOutput = aggregate(matchQuery, groupQuery, new BasicDBObject(
                "$sort", sortQuery));

        int rank = 1;
        for (DBObject result : aggregationOutput.results()) {
            DBObject _id = (DBObject) result.get("_id");
            if (_id.get("score").equals(score) && (Long) _id.get("timeTaken") >= timeTaken) {
                break;
            }
            rank++;
        }
        return rank;
    }

    public int getRank(String orgId, String entityId, double score, long timeTaken, String acadDimId) {

        QueryImpl<UserEntityAnalytics> query = (QueryImpl<UserEntityAnalytics>) getQuery()
                .filter(ConstantsGlobal.ORG_ID, orgId)
                .filter(ConstantsGlobal.ENTITY_DOT_ID, entityId)
                .filter(ConstantsGlobal.ACAD_DIM_DOT_ID, acadDimId).field("measures.score")
                .greaterThanOrEq(score);

        DBObject matchQuery = new BasicDBObject("$match", query.getQueryObject());

        String groupQueryString = "{\"$group\" : {_id: {score: \"$measures.score\", timeTaken : \"$measures.timeTaken\"}}}";

        DBObject groupQuery = (DBObject) JSON.parse(groupQueryString);

        DBObject sortQuery = MongoManager.getSortQuery("_id.score", SortOrder.DESC.name());
        sortQuery.putAll(MongoManager.getSortQuery("_id.timeTaken", SortOrder.ASC.name()));

        AggregationOutput aggregationOutput = aggregate(matchQuery, groupQuery, new BasicDBObject(
                "$sort", sortQuery));

        LOGGER.debug("leader board results : "+aggregationOutput.results());
        int rank = 1;
        final double TOLERANCE = 0.0000001d;
        for (DBObject result : aggregationOutput.results()) {
            DBObject _id = (DBObject) result.get("_id");
            if (Math.abs(score - Double.parseDouble(String.valueOf(_id.get("score")))) <= TOLERANCE /*&& (Long) _id.get("timeTaken") >= timeTaken*/) {
                break;
            }
            rank++;
        }
        return rank;
    }


    public List<UserEntityAnalytics> getAllAnalytics(String entityId,
            AcademicDimensionType acadDimType, String acadDimId, String orgId) {
        Query<UserEntityAnalytics> entityUserAnalytics = getDS().find(entityClazz).order("orderId");
        entityUserAnalytics.filter(ConstantsGlobal.ENTITY_DOT_ID, entityId).filter("orgId", orgId)
                .filter(ACAD_DIM_TYPE, acadDimType).filter(ACAD_DIM_ID, acadDimId).get();
        List<UserEntityAnalytics> entityUserAnalyticsList = entityUserAnalytics.asList();
        return entityUserAnalyticsList;
    }

    private static final String ACAD_DIM_TYPE = "acadDim.type";
    private static final String ACAD_DIM_ID   = "acadDim.id";

    public long getAnalyticsCount(SrcEntity entity, String acadDimId, String orgId) {
        return count(getQuery().filter(ConstantsGlobal.ENTITY_DOT_ID, entity.id).filter(
                ACAD_DIM_ID, acadDimId).filter("orgId", orgId));
    }

    public List<UserEntityAnalytics> getUserEntityAnalyticsList(SrcEntity entity, String acadDimId, String orgId, MutableLong hits){
        Query<UserEntityAnalytics> query = getQuery().filter(ConstantsGlobal.ENTITY_DOT_ID, entity.id).filter(
                ACAD_DIM_ID, acadDimId).filter("orgId", orgId);
        if (hits != null) {
            hits.setValue(query.countAll());
        }
        return query.asList();
    }

    public long getTotalTimeTaken(SrcEntity entity, String acadDimId, String orgId) {
        List<UserEntityAnalytics> users = getQuery().filter(ConstantsGlobal.ENTITY_DOT_ID, entity.id).filter(
                ACAD_DIM_ID, acadDimId).filter("orgId", orgId).asList();
        long timeTaken = 0;
        for(UserEntityAnalytics user: users){
            timeTaken += user.measures.timeTaken;
        }
        return timeTaken;
    }

    public int getTotalScore(SrcEntity entity, String acadDimId, String orgId) {
        List<UserEntityAnalytics> users = getQuery().filter(ConstantsGlobal.ENTITY_DOT_ID, entity.id).filter(
                ACAD_DIM_ID, acadDimId).filter("orgId", orgId).asList();
        int score = 0;
        for(UserEntityAnalytics user: users){
            score += user.measures.score;
        }
        return score;
    }

    public List<UserEntityAnalytics> getAnalyticsList(SrcEntity entity, String acadDimId, String orgId) {
        List<UserEntityAnalytics> users = getQuery().filter(ConstantsGlobal.ENTITY_DOT_ID, entity.id).filter(
                ACAD_DIM_ID, acadDimId).filter("orgId", orgId).asList();
        return users;
    }

    public void removeUserEntityAnalytics(String userId, SrcEntity target) throws VedantuException {

        List<UserEntityAnalytics> userEntityAnalytics = getQuery().filter(ConstantsGlobal.USER_ID, userId).filter(ConstantsGlobal.ENTITY,
                        target).asList();
        LOGGER.debug("deleted userEntityAnalytics ");
        if (userEntityAnalytics.isEmpty()) {
//            throw new VedantuException(VedantuErrorCode.ATTEMPT_NOT_FOUND);
        }else {
            for(UserEntityAnalytics userEntityAnalytic : userEntityAnalytics){
                delete(userEntityAnalytic);
            }
        }
    }

    public void updateParticularTestOverallAnalytics(String userId, String testId) throws VedantuException {

        SrcEntity ent = new SrcEntity();
        ent.type = EntityType.TEST;
        ent.id = testId;
        UserEntityAnalytics presentTestAnalytics = UserEntityAnalyticsDAO.INSTANCE.getAnalytics(userId, "", ent, AcademicDimensionType.OVERALL, AcademicDimensionType.OVERALL.name());
        ent.id = AcademicDimensionType.OVERALL.name();
        UserEntityAnalytics overAllAnalytics = UserEntityAnalyticsDAO.INSTANCE.getAnalytics(userId, "", ent, AcademicDimensionType.OVERALL, AcademicDimensionType.OVERALL.name());
        if(presentTestAnalytics == null || overAllAnalytics == null){
            throw new VedantuException(VedantuErrorCode.ANALYTICS_NOT_FOUND);
        }else{
            Query<UserEntityAttempt> attemptCountQuery = UserEntityAttemptDAO.INSTANCE
                    .createQuery().filter(ConstantsGlobal.USER_ID, userId)
                    .filter(ConstantsGlobal.ENTITY_DOT_TYPE, ent.type).filter("finished", true);
            long attemptCount = UserEntityAttemptDAO.INSTANCE.count(attemptCountQuery);
            double percentage = Math.round(((overAllAnalytics.percentage * attemptCount)-presentTestAnalytics.percentage)/(attemptCount-1));
            overAllAnalytics.percentage = percentage;
            overAllAnalytics.measures.attempts -= presentTestAnalytics.measures.attempts;
            overAllAnalytics.measures.correct -= presentTestAnalytics.measures.correct;
            overAllAnalytics.measures.partial -= presentTestAnalytics.measures.partial;
            overAllAnalytics.measures.incorrect -= presentTestAnalytics.measures.incorrect;
            overAllAnalytics.measures.left -= presentTestAnalytics.measures.left;
            overAllAnalytics.measures.score -= presentTestAnalytics.measures.score;
            overAllAnalytics.measures.timeTaken -= presentTestAnalytics.measures.timeTaken;
            save(overAllAnalytics);
        }
    }
}
