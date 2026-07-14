package com.vedantu.user.daos;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuDBResult;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.user.models.TestUser;
import com.google.code.morphia.query.Query;

import org.bson.types.ObjectId;

import play.Logger;

import java.util.Arrays;
import java.util.List;

public class TestUserDAO extends VedantuBasicDAO<TestUser, ObjectId>{

    private static final Logger.ALogger LOGGER      = Logger.of(TestUserDAO.class);

    public static final TestUserDAO INSTANCE    = new TestUserDAO();

    private TestUserDAO() {

        super(TestUser.class);
    }

    public TestUser getTestUserByMobile(String mobile) {
        DBObject query = new BasicDBObject(TestUser.FIELD_TELEPHONE, mobile);
        DBObject fields = MongoManager.getFieldsDBObject(Arrays.asList(ConstantsGlobal.NAME, TestUser.FIELD_TELEPHONE,
                ConstantsGlobal.USER_ID), MongoManager.INCLUDE_FIELD);
        VedantuDBResult<TestUser> testUserResult = TestUserDAO.INSTANCE.getInfos(query, fields, MongoManager.NO_START,
                1, null);
        if(testUserResult.results == null || testUserResult.results.isEmpty()) {
            return null;
        }
        return testUserResult.results.get(0);
    }

    public TestUser getTestUserByInstitueId(String memberId) {
        DBObject query = new BasicDBObject(TestUser.FIELD_INSTITUTE_ID, memberId);
        DBObject fields = MongoManager.getFieldsDBObject(Arrays.asList(ConstantsGlobal.NAME, TestUser.FIELD_TELEPHONE,
                ConstantsGlobal.USER_ID,TestUser.FIELD_INSTITUTE_ID,ConstantsGlobal.EMAIL), MongoManager.INCLUDE_FIELD);
        VedantuDBResult<TestUser> testUserResult = TestUserDAO.INSTANCE.getInfos(query, fields, MongoManager.NO_START,
                1, null);
        if(testUserResult.results == null || testUserResult.results.isEmpty()) {
            return null;
        }
        return testUserResult.results.get(0);
    }

    public List<TestUser> getTestUsers(String orgId, Long startDate, Long endDate) {
        Query<TestUser> query = getQuery();
        query = query.field("orgId").equal(orgId);
        query = query.field("timeCreated").greaterThanOrEq(startDate);
        query = query.field("timeCreated").lessThanOrEq(endDate);
        query = query.field("recordState").equal(VedantuRecordState.ACTIVE);
        return query.asList();
    }

}
