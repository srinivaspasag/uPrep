package com.vedantu.cmds.mgmt.publishers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.vedantu.cmds.daos.CMDSQuestionDAO;
import com.vedantu.cmds.daos.CMDSTestDAO;
import com.vedantu.cmds.models.CMDSDocument;
import com.vedantu.cmds.models.CMDSQuestion;
import com.vedantu.cmds.models.CMDSTest;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.ObjectIdUtils;
import com.vedantu.content.daos.LibraryContentLinksDAO;
import com.vedantu.content.daos.QuestionDAO;
import com.vedantu.content.daos.TestDAO;
import com.vedantu.content.models.LibraryContentLink;
import com.vedantu.content.models.tests.Test;
import com.vedantu.content.pojos.tests.TestQuestionSet;
import com.vedantu.content.search.details.TestSearchIndexDetails;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.VedantuBaseMongoModel;

public class TestPublisher extends AbstractTestPublisher {

    private static final ALogger      LOGGER   = Logger.of(TestPublisher.class);

    public static final TestPublisher INSTANCE = new TestPublisher();

    private TestPublisher() {

        super();
        EntityTypePublisherFactory.INSTANCE.register(EntityType.CMDSTEST, TestPublisher.INSTANCE);
    }

    @Override
    public void prePublish(SrcEntity content) {

    }

    @Override
    protected VedantuBaseMongoModel publish(String userId, String orgId, SrcEntity content)
            throws VedantuException {

        CMDSTest cmdsTest = CMDSTestDAO.INSTANCE.getTest(content.id);
        // if (StringUtils.isNotEmpty(cmdsTest.globalTestId)) {
        //     LOGGER.error("cmdsTest[" + cmdsTest._getStringId() + "] already published");
        //     throw new VedantuException(VedantuErrorCode.ALREADY_PUBLISHED);
        // }
        List<String> cmdsQIds = new ArrayList<String>(cmdsTest.__getAllQIds());
        String errorMsg = null;
        Map<String, CMDSQuestion> cmdsQuestionMap = CMDSQuestionDAO.INSTANCE
                .toInfosMap(CMDSQuestionDAO.INSTANCE.getByIds(ObjectIdUtils.toObjectIds(cmdsQIds)));
        if (cmdsQIds.size() != cmdsQuestionMap.size()) {
            errorMsg = "all question are not valid for cmdstest : " + cmdsTest
                    + ", totalQuestions: " + cmdsQIds.size() + ", avalibaleQuestions: "
                    + cmdsQuestionMap.size();
            LOGGER.error(errorMsg);
            throw new VedantuException(VedantuErrorCode.CMDS_QUESTION_NOT_FOUND, errorMsg);
        }
        cmdsTest.computeTotalQusAndMarks();
        // check if all questions are published
        DBObject query = new BasicDBObject("cmdsQId", new BasicDBObject(MongoManager.IN_QUERY,
                cmdsQIds.toArray()));
        long publishedQuestionCount = QuestionDAO.INSTANCE.count(query);
        if (publishedQuestionCount < cmdsQIds.size()) {
            errorMsg = "all questions of the test are not published [publishedCount: "
                    + publishedQuestionCount + ", actualQidsCount" + cmdsQIds.size() + "]";
            throw new VedantuException(VedantuErrorCode.NOT_PUBLISHED, errorMsg);
        }
        Test test;
        if (!StringUtils.isEmpty(cmdsTest.globalTestId)) {
            test = TestDAO.INSTANCE.getTest(cmdsTest.globalTestId);
        }else{
            test = new Test(cmdsTest.userId, cmdsTest.name, cmdsTest.desc, cmdsTest.qusCount,
                    cmdsTest.duration, cmdsTest.totalMarks, null, cmdsTest.type, cmdsTest.mode,
                    cmdsTest.code, cmdsTest.scope, cmdsTest.resultVisibility , cmdsTest.password, cmdsTest.resultPassword);
        }
        test.resultVisibilityMessage = cmdsTest.resultVisibilityMessage;
        test.isNTAPattern=cmdsTest.isNTAPattern;
        if(cmdsTest.isNTAPattern){
        	test.actualQusCount=cmdsTest.actualQusCount;
        }

        test.metadata = createGlobalMetadata(cmdsTest, cmdsQuestionMap);

        // copy the question sets data
        List<TestQuestionSet> sets = new ArrayList<TestQuestionSet>();
        if (cmdsTest.sets != null) {
            for (TestQuestionSet set : cmdsTest.sets) {
                TestQuestionSet newSet = new TestQuestionSet(set.name);
                if (set.qIds != null) {
                    for (String qId : set.qIds) {
                        newSet.addQid(cmdsQuestionMap.get(qId).globalQid);
                    }
                }
                sets.add(newSet);
            }
        }
        test.sets = sets;

        if (CollectionUtils.isNotEmpty(cmdsTest.childrenIds)) {
            // check if all the test child testAre published
            int publishedCount = (int) TestDAO.INSTANCE.count(new BasicDBObject("cmdsTestId",
                    new BasicDBObject(MongoManager.IN_QUERY, cmdsTest.childrenIds.toArray())));
            if (publishedCount != cmdsTest.childrenIds.size()) {
                errorMsg = "some children[" + cmdsTest.childrenIds + "] of test["
                        + cmdsTest._getStringId() + "] are not published";
                LOGGER.error(errorMsg);
                throw new VedantuException(VedantuErrorCode.NOT_PUBLISHED, errorMsg);
            }
        }

        test.cmdsTestId = cmdsTest._getStringId();
        test.published = true;
        test.contentSrc = cmdsTest.contentSrc;
        test.difficulty = cmdsTest.difficulty;
        test.boardIds = cmdsTest.boardIds;
        test.tags = cmdsTest.tags;
        test.type = cmdsTest.type;
        test.mode = cmdsTest.mode;
        test.targetIds = cmdsTest.targetIds;
        test.size= cmdsTest.size;
        test.enablePartialMarks = cmdsTest.enablePartialMarks;
        test.simplifiedBoardNames = cmdsTest.simplifiedBoardNames;
        test.partialMarksQTypes = cmdsTest.partialMarksQTypes;
        test.oneOrMoreMarksQTypes = cmdsTest.oneOrMoreMarksQTypes;
        test.subjectiveTest = cmdsTest.subjectiveTest;
        test.enableSectionLocking = cmdsTest.enableSectionLocking;
        test.autoResumeTest = cmdsTest.autoResumeTest;
        test.showAIR = cmdsTest.showAIR;

        if (!StringUtils.isEmpty(cmdsTest.pdfId)) {
            SrcEntity documentContent = new SrcEntity(EntityType.CMDSDOCUMENT , cmdsTest.pdfId);
            CMDSDocument cmdsDocument = (CMDSDocument) DocumentPublisher.INSTANCE.publish(userId,
                    orgId,documentContent);
            test.pdfId = cmdsDocument.globalDocId;
        }

        if(!StringUtils.isEmpty(cmdsTest.password)){
            test.password = cmdsTest.password;
        }

        if(!StringUtils.isEmpty(cmdsTest.resultPassword)){
            test.resultPassword = cmdsTest.resultPassword;
        }
        TestDAO.INSTANCE.save(test);
        if (CollectionUtils.isNotEmpty(cmdsTest.childrenIds)) {
            List<String> globalChildren = new ArrayList<String>();
            for (String childTestId : cmdsTest.childrenIds) {

                CMDSTest childTest = CMDSTestDAO.INSTANCE.getById(childTestId);

                globalChildren.add(childTest.globalTestId);
                Test globalTest = TestDAO.INSTANCE.getById(childTest.globalTestId);
                globalTest.parentId = test._getStringId();
                TestDAO.INSTANCE.save(globalTest);
                generateEventAysc(userId, globalTest, EventActionType.UPDATE, EventType.INDEX_TEST,
                        UserActionType.UPDATED, false);
            }
            test.childrenIds = globalChildren;
            TestDAO.INSTANCE.save(test);
        }
        if (!StringUtils.isEmpty(cmdsTest.pdfId)) {
            LibraryContentLink link = LibraryContentLinksDAO.INSTANCE.addLink(
                    new SrcEntity(EntityType.DOCUMENT,test.pdfId),
                    new SrcEntity(EntityType.TEST, test._getStringId()), UserActionType.ADDED,
                    userId, Scope.ORG);
        }
        cmdsTest.globalTestId = test._getStringId();
        cmdsTest.published = true;
        cmdsTest.publishingInProgress = false;
        CMDSTestDAO.INSTANCE.save(cmdsTest);

        // live add global test to search index
        TestSearchIndexDetails details = new TestSearchIndexDetails();
        details.fromMongoModel(test);
        addLiveEntityToSearchIndex(details, EntityType.TEST, true);

        // it is already added to index, right we just need to update the index
        generateEventAysc(userId, cmdsTest, EventActionType.UPDATE, EventType.INDEX_CMDS_TEST,
                UserActionType.UPDATED, false);
        // generate empty question analytics for all the question in this test/assignment
        generateEntityQuestionAnalytics(test, EntityType.TEST);
        return cmdsTest;
    }

    public void postPublish(VedantuBaseMongoModel mongoModel) {

    }

}
