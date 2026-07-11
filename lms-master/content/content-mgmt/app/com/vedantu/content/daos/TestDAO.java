package com.vedantu.content.daos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.enums.EnumBasket.TestType;
import com.vedantu.content.enums.TestMode;
import com.vedantu.content.enums.TestResultVisibility;
import com.vedantu.content.models.Question;
import com.vedantu.content.models.Solution;
import com.vedantu.content.models.tests.Test;
import com.vedantu.content.pojos.tests.TestMetadata;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.VedantuDBResult;
import com.vedantu.user.daos.TestUserDAO;
import com.vedantu.user.models.TestUser;

public class TestDAO extends AbstractAttemptableDAO<Test, ObjectId> {

    private static final ALogger LOGGER = Logger.of(TestDAO.class);

    public static final TestDAO INSTANCE = new TestDAO();

    private TestDAO() {

        super(Test.class);
    }

    public Test
            addTest(String userId, String name, String code, String desc, int qusCount,
                    long duration, int totalMarks, List<TestMetadata> metadata, TestType type,
                    TestMode mode, SrcEntity contentSrc, Scope scope,
                    TestResultVisibility resultVisibility) {

        Test test = new Test(userId, name, desc, qusCount, duration, totalMarks, metadata, type,
                mode, code, scope, resultVisibility);
        test.contentSrc = contentSrc;
        save(test);
        return test;
    }

    public Test getTest(String id) throws VedantuException {

        Test test = getById(id);
        if (test == null) {
            throw new VedantuException(VedantuErrorCode.TEST_NOT_FOUND, "no test found with id: "
                    + id);
        }
        return test;
    }

    @Override
    public List<SrcEntity> getChildren(String id) {

        Test test = this.getById(id);

        List<String> qIds = test.__getAllQIds();
        List<SrcEntity> children = new ArrayList<SrcEntity>();
        if (CollectionUtils.isNotEmpty(qIds)) {
            for (String qId : qIds) {
                children.add(new SrcEntity(EntityType.QUESTION, qId));
            }
        }

        return children;
    }

    public Test getByCMDSTestId(String cmdsTestId) {

        Test test = getQuery().filter("cmdsTestId", cmdsTestId).get();
        if (test == null) {
            LOGGER.error("Cannot find test with the cmds test id :" + cmdsTestId);
        }

        return test;
    }

    public boolean isPartialMarkingEnabled(String testId, String qType){
        Test test = this.getById(testId);
        if(test.enablePartialMarks){
            if(test.partialMarksQTypes.contains(qType)){
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }

    }
}
