package com.vedantu.cmds.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableLong;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.facet.AbstractFacetBuilder;

import play.Logger;
import play.Play;
import play.Logger.ALogger;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.vedantu.board.daos.BoardDAO;
import com.vedantu.cmds.daos.CMDSDocumentDAO;
import com.vedantu.cmds.daos.CMDSFolderDAO;
import com.vedantu.cmds.daos.CMDSModuleDAO;
import com.vedantu.cmds.daos.CMDSQuestionDAO;
import com.vedantu.cmds.daos.CMDSTestDAO;
import com.vedantu.cmds.daos.CmdsContentLinkDAO;
import com.vedantu.cmds.enums.CmdsContentLinkType;
import com.vedantu.cmds.enums.PublishedStatus;
import com.vedantu.cmds.models.CMDSContentLink;
import com.vedantu.cmds.models.CMDSDocument;
import com.vedantu.cmds.models.CMDSFolder;
import com.vedantu.cmds.models.CMDSQuestion;
import com.vedantu.cmds.models.CMDSTest;
import com.vedantu.cmds.models.event.search.details.CMDSQuestionSearchIndexDetails;
import com.vedantu.cmds.models.event.search.details.CMDSResourceDetails;
import com.vedantu.cmds.models.event.search.details.ReIndexDetails;
import com.vedantu.cmds.pojos.BoardInfo;
import com.vedantu.cmds.pojos.SimplifiedBoardNameObject;
import com.vedantu.cmds.pojos.content.question.CMDSQuestionInfo;
import com.vedantu.cmds.pojos.content.tests.CMDSTestBasicInfo;
import com.vedantu.cmds.pojos.content.tests.Details;
import com.vedantu.cmds.pojos.content.tests.GetCMDSTestQuestionsReq;
import com.vedantu.cmds.pojos.content.tests.Metadata;
import com.vedantu.cmds.pojos.requests.tests.CreateCMDSTestAutoReq;
import com.vedantu.cmds.pojos.requests.tests.CreateCMDSTestReq;
import com.vedantu.cmds.pojos.requests.tests.FinishCMDSTestEditReq;
import com.vedantu.cmds.pojos.requests.tests.GetCMDSTestReq;
import com.vedantu.cmds.pojos.requests.tests.ModifyCMDSTestQuestionsReq;
import com.vedantu.cmds.pojos.requests.tests.SetPasswordForTestReq;
import com.vedantu.cmds.pojos.requests.tests.UpdateTestResultVisibilityReq;
import com.vedantu.cmds.pojos.requests.tests.simplifyBoardNamesReq;
import com.vedantu.cmds.pojos.responses.tests.CreateCMDSTestRes;
import com.vedantu.cmds.pojos.responses.tests.FinishCMDSTestEditRes;
import com.vedantu.cmds.pojos.responses.tests.GetCMDSTestQuestionsRes;
import com.vedantu.cmds.pojos.responses.tests.GetCMDSTestRes;
import com.vedantu.cmds.pojos.responses.tests.GetCMDSTestsRes;
import com.vedantu.cmds.pojos.responses.tests.GetReGenerateAnalyticsRes;
import com.vedantu.cmds.pojos.responses.tests.ModifyCMDSTestQuestionsRes;
import com.vedantu.cmds.pojos.responses.tests.SetPasswordForTestRes;
import com.vedantu.cmds.pojos.responses.tests.UpdateTestResultVisibilityRes;
import com.vedantu.cmds.pojos.responses.tests.simplifyBoardNamesRes;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.commons.enums.boards.BoardType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.ObjectIdUtils;
import com.vedantu.content.daos.TestDAO;
import com.vedantu.content.enums.EnumBasket.TestType;
import com.vedantu.content.enums.QuestionType;
import com.vedantu.content.event.details.EndTestDetails;
import com.vedantu.content.managers.TestManager;
import com.vedantu.content.models.AbstractContentModel;
import com.vedantu.content.models.tests.AbstractTestCommonModel;
import com.vedantu.content.models.tests.Test;
import com.vedantu.content.pojos.requests.EditContentReq;
import com.vedantu.content.pojos.requests.tests.GetTestsReq;
import com.vedantu.content.pojos.tests.BoardQus;
import com.vedantu.content.pojos.tests.SimplifiedBoardNames;
import com.vedantu.content.pojos.tests.TestDetails;
import com.vedantu.content.pojos.tests.TestMetadata;
import com.vedantu.content.search.details.boards.BoardSearchEntity;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuDBResult;
import com.vedantu.search.utils.ElasticSearchUtils;

public class CMDSTestManager extends AbstractCMDSContentManager {

    private static final ALogger  LOGGER   = Logger.of(CMDSTestManager.class);
    public static CMDSTestManager INSTANCE = new CMDSTestManager();
    public static int MAX_PARA_QUESTIONS = 2;

    public static CreateCMDSTestRes createTest(CreateCMDSTestReq req) throws VedantuException {
        if (StringUtils.isEmpty(req.orgId)) {
            LOGGER.error("missing orgId: " + req.orgId);
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, "missing orgId: "
                    + req.orgId);
        }
        // validate test metadata
        List<TestMetadata> metadata = req.metadata;
        Set<String> brdIds = new HashSet<String>();
        for (TestMetadata mdata : metadata) {
            LOGGER.debug(" Metadata " + mdata);
            if (StringUtils.isEmpty(mdata.id) || StringUtils.isEmpty(mdata.name)) {
                String errorMsg = "invalid metadata [name:" + mdata.name + ", id:" + mdata.id + "]";
                LOGGER.error(errorMsg);
                throw new VedantuException(VedantuErrorCode.INVALID_METADATA, errorMsg);
            }
            if (mdata.qIds == null) {
                mdata.qIds = new ArrayList<String>();
            }
            brdIds.add(mdata.id);
            if (mdata.children != null) {
                for (BoardQus topic : mdata.children) {
                    brdIds.add(topic.id);
                    if (topic.qIds == null) {
                        topic.qIds = new ArrayList<String>();
                    }
                }
            }
            if (mdata.details != null) {
                for (TestDetails detail : mdata.details) {
                    if (detail.qIds == null) {
                        detail.qIds = new ArrayList<String>();
                    }
                }
            }
        }

        int availableBoardsCount = (int) BoardDAO.INSTANCE.count(new BasicDBObject(
                ConstantsGlobal._ID, new BasicDBObject(MongoManager.IN_QUERY, ObjectIdUtils
                        .toObjectIds(new ArrayList<String>(brdIds), true).toArray())));
        if (availableBoardsCount != brdIds.size()) {
            LOGGER.error("some boardIds provided in metadata object are not valid brdIds: "
                    + brdIds);
            throw new VedantuException(VedantuErrorCode.INVALID_METADATA);
        }
        CMDSFolder folder = StringUtils.isEmpty(req.folderId)
                || ObjectIdUtils.hasInvalidId(req.folderId) ? CMDSResourcesManager.getRootFolder(
                req.userId, req.orgId) : CMDSFolderDAO.INSTANCE.findById(req.orgId, req.folderId);
               
        CMDSTest cmdsTest = CMDSTestDAO.INSTANCE.addTest(req.userId, req.name, req.code, req.type,
                req.targetId, req.desc, req.metadata, req.duration, new SrcEntity(
                        EntityType.ORGANIZATION, req.orgId), Scope.ORG, req.getResultVisibility(),
                req.resultVisibilityMessage, req.showAIR, req.subjectiveTest,req.isNTAPattern);
        CreateCMDSTestRes createCmdsTestRes = new CreateCMDSTestRes();

        createCmdsTestRes.id = cmdsTest._getStringId();
        LOGGER.info("generating index cmds test event ");
        generateEventAysc(req.userId, cmdsTest, EventActionType.ADD, EventType.INDEX_CMDS_TEST,
                UserActionType.ADDED, false);

        SrcEntity cmdsTestEntity = new SrcEntity(EntityType.CMDSTEST, cmdsTest._getStringId());

        String parentESId = AbstractCMDSContentManager.addAsCMDSResource(cmdsTestEntity,
                EventActionType.ADD, cmdsTest);

        // add this test to the root folder
        CMDSResourcesManager.addToFolder(req.orgId, req.userId, cmdsTestEntity,
                folder._getStringId(), CmdsContentLinkType.ADDED, parentESId);

        return createCmdsTestRes;
    }

    public static CreateCMDSTestRes createTestAuto(CreateCMDSTestAutoReq req) throws VedantuException {
        CreateCMDSTestRes createCmdsTestRes = new CreateCMDSTestRes();
        createCmdsTestRes.id = req.testId;
        if (StringUtils.isEmpty(req.orgId)) {
            LOGGER.error("missing orgId: " + req.orgId);
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, "missing orgId: "
                    + req.orgId);
        }
        // validate test metadata
        CMDSTest cmdsTest = CMDSTestDAO.INSTANCE.getById(req.testId);
        if(cmdsTest == null){
            LOGGER.error("cmdstest is null ");
            throw new VedantuException(VedantuErrorCode.INVALID_ID, "Invalid Test Id");
        }
        // Add auto generated question Ids to this set
        Set<String> qIds = new HashSet<String>();
        // Store count of questions for individual board in a map
        Map<String, Integer> dbEntriesOfTotalQuestionsCountInEachBoard = new HashMap<String, Integer>();
        Map<String, Integer> dbEntriesOfSCQCountInEachBoard = new HashMap<String, Integer>();
        Map<String, Integer> dbEntriesOfMCQCountInEachBoard = new HashMap<String, Integer>();
        Map<String, Integer> dbEntriesOfNumericCountInEachBoard = new HashMap<String, Integer>();
        Map<String, Integer> dbEntriesOfParaCountInEachBoard = new HashMap<String, Integer>();
        Map<String, Integer> dbEntriesOfMATRIXCountInEachBoard = new HashMap<String, Integer>();
        Map<String, Integer> dbEntriesOfSubjectiveCountInEachBoard = new HashMap<String, Integer>();
        if(cmdsTest.metadata != null) {
            for (TestMetadata tMdata : cmdsTest.metadata){
                if(tMdata.qIds != null) {
                    LOGGER.error("This Test already has qIds. It was created in manual process");
                    throw new VedantuException(VedantuErrorCode.CAN_NOT_BE_DELIVERED, "Manual Test, Can't be Automated. Please Add Questions Manually ");
                }
                dbEntriesOfTotalQuestionsCountInEachBoard.put(tMdata.id, tMdata.qusCount);
                for(TestDetails tDetails : tMdata.details){
                    if(tDetails.type == QuestionType.SCQ){
                        dbEntriesOfSCQCountInEachBoard.put(tMdata.id, tDetails.qusCount);
                    }else if (tDetails.type == QuestionType.MCQ) {
                        dbEntriesOfMCQCountInEachBoard.put(tMdata.id, tDetails.qusCount);
                    }else if (tDetails.type == QuestionType.MATRIX){
                        dbEntriesOfMATRIXCountInEachBoard.put(tMdata.id, tDetails.qusCount);
                    }else if (tDetails.type == QuestionType.NUMERIC) {
                        dbEntriesOfNumericCountInEachBoard.put(tMdata.id, tDetails.qusCount);
                    }else if (tDetails.type == QuestionType.PARA) {
                        dbEntriesOfParaCountInEachBoard.put(tMdata.id, tDetails.qusCount);
                    }else if (tDetails.type == QuestionType.SUBJECTIVE){
	                    dbEntriesOfSubjectiveCountInEachBoard.put(tMdata.id, tDetails.qusCount);
                    }else{
                        LOGGER.error("Invalid Question type");
                        throw new VedantuException(VedantuErrorCode.UNKNOWN_QUESTION_TYPE);
                    }
                }
            }
        }else{
            LOGGER.error("cmdstest metadata is null");
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, "Metadata is not found");
        }
        List<Metadata> metadata = req.metadata;
        try {
            for (Metadata mdata : metadata) {
                int questionCount = 0;
                // Check total questions count with respective to DB record of this test
                if(dbEntriesOfTotalQuestionsCountInEachBoard.containsKey(mdata.id)){
                    if(dbEntriesOfTotalQuestionsCountInEachBoard.get(mdata.id) == mdata.qusCount){
                        questionCount = mdata.qusCount;
                    }else{
                        LOGGER.error("Mismatch in Questions Count of Board Id "+mdata.id);
                        throw new VedantuException(VedantuErrorCode.CAN_NOT_EXECUTE, "Mismatch in Questions Count of Board Id "+mdata.name);
                    }
                }else{
                    LOGGER.error("Mismatch in Board Ids "+mdata.id);
                    throw new VedantuException(VedantuErrorCode.INVALID_ID, "Mismatch in Boards Ids of Request and DB record");
                }
                int questionsTypeWiseQuestionsCount = 0;
                for(Details detail : mdata.details) {
                    if(detail.type == QuestionType.SCQ){
                        if(dbEntriesOfSCQCountInEachBoard.get(mdata.id) == detail.qusCount) {
                            questionsTypeWiseQuestionsCount += detail.qusCount;
                        }else{
                            LOGGER.error("Mismatch in SCQ Questions Count of Board Id "+mdata.id);
                            throw new VedantuException(VedantuErrorCode.CAN_NOT_EXECUTE, "Mismatch in SCQ Questions Count of Board Id "+mdata.name);
                        }
                    }else if (detail.type == QuestionType.MCQ) {
                        if(dbEntriesOfMCQCountInEachBoard.get(mdata.id) == detail.qusCount) {
                            questionsTypeWiseQuestionsCount += detail.qusCount;
                        }else{
                            LOGGER.error("Mismatch in MCQ Questions Count of Board Id "+mdata.id);
                            throw new VedantuException(VedantuErrorCode.CAN_NOT_EXECUTE, "Mismatch in MCQ Questions Count of Board Id "+mdata.name);
                        }
                    }else if (detail.type == QuestionType.MATRIX) {
                        if(dbEntriesOfMATRIXCountInEachBoard.get(mdata.id) == detail.qusCount) {
                            questionsTypeWiseQuestionsCount += detail.qusCount;
                        }else{
                            LOGGER.error("Mismatch in MATRIX Questions Count of Board Id "+mdata.id);
                            throw new VedantuException(VedantuErrorCode.CAN_NOT_EXECUTE, "Mismatch in MATRIX Questions Count of Board Id "+mdata.name);
                        }
                    }else if (detail.type == QuestionType.NUMERIC) {
                        if(dbEntriesOfNumericCountInEachBoard.get(mdata.id) == detail.qusCount) {
                            questionsTypeWiseQuestionsCount += detail.qusCount;
                        }else{
                            LOGGER.error("Mismatch in Numeric Questions Count of Board Id "+mdata.id);
                            throw new VedantuException(VedantuErrorCode.CAN_NOT_EXECUTE, "Mismatch in Numeric Questions Count of Board Id "+mdata.name);
                        }
                    }else if (detail.type == QuestionType.PARA) {
                        if(dbEntriesOfParaCountInEachBoard.get(mdata.id) == detail.qusCount) {
                            questionsTypeWiseQuestionsCount += detail.qusCount;
                        }else{
                            LOGGER.error("Mismatch in Para Questions Count of Board Id "+mdata.id);
                            throw new VedantuException(VedantuErrorCode.CAN_NOT_EXECUTE, "Mismatch in Para Questions Count of Board Id "+mdata.name);
                        }
                    }else if (detail.type == QuestionType.SUBJECTIVE) {
                        if(dbEntriesOfSubjectiveCountInEachBoard.get(mdata.id) == detail.qusCount) {
                            questionsTypeWiseQuestionsCount += detail.qusCount;
                        }else{
                            LOGGER.error("Mismatch in Subjective Questions Count of Board Id "+mdata.id);
                            throw new VedantuException(VedantuErrorCode.CAN_NOT_EXECUTE, "Mismatch in Subjective Questions Count of Board Id "+mdata.name);
                        }
                    }else{
                        LOGGER.error("Invalid Question type");
                        throw new VedantuException(VedantuErrorCode.UNKNOWN_QUESTION_TYPE);
                    }
                }

                if(questionsTypeWiseQuestionsCount != questionCount){
                    LOGGER.error("Mismatch in total questions count and Questions type wise count in request for metadata "+mdata.id);
                    throw new VedantuException(VedantuErrorCode.CAN_NOT_EXECUTE, "Mismatch in total questions count and Questions type wise count in request");
                }

                if(mdata.children != null){
                    int childrenTotalQuestionCount = 0;
                    for(Metadata children : mdata.children){
                        if(children.qusCount > 0){
                            childrenTotalQuestionCount += children.qusCount;
                            int totalChildrenQuestionCount = 0;
                            for(Details detail : children.details){
                                if(detail.type == QuestionType.SCQ){
                                    if(detail.qusCount > 0){
                                        totalChildrenQuestionCount += detail.qusCount;
                                        if(detail.difficulty == null){
                                            // Implement logic without having difficulty level
                                            LOGGER.debug("Getting random Questions for "+mdata.name+" --> "+children.name+" of type "+detail.type);
                                            getRandomCmdsQuestions(req.orgId,mdata.published,children.id,detail.type,"UNKNOWN",detail.qusCount,qIds);
                                        }else{
                                            if((detail.difficulty.easy + detail.difficulty.tough + detail.difficulty.moderate) == detail.qusCount){
                                                // Implement logic to get cmds question IDs
                                                if(detail.difficulty.easy > 0){
                                                    LOGGER.debug("Getting random Questions for "+mdata.name+" --> "+children.name+" of type "+detail.type+" with difficulty level EASY");
                                                    getRandomCmdsQuestions(req.orgId,mdata.published,children.id,detail.type,"EASY".toLowerCase(),detail.difficulty.easy,qIds);
                                                }
                                                if(detail.difficulty.tough > 0){
                                                    LOGGER.debug("Getting random Questions for "+mdata.name+" --> "+children.name+" of type "+detail.type+" with difficulty level TOUGH");
                                                    getRandomCmdsQuestions(req.orgId,mdata.published,children.id,detail.type,"TOUGH".toLowerCase(),detail.difficulty.tough,qIds);
                                                }
                                                if(detail.difficulty.moderate > 0){
                                                    LOGGER.debug("Getting random Questions for "+mdata.name+" --> "+children.name+" of type "+detail.type+" with difficulty level MODERATE");
                                                    getRandomCmdsQuestions(req.orgId,mdata.published,children.id,detail.type,"MODERATE".toLowerCase(),detail.difficulty.moderate,qIds);
                                                }
                                            }else{
                                                LOGGER.error("Mismatch in total difficulty count and children detail count of metadata "+mdata.id+" at children "+children.id);;
                                                throw new VedantuException(VedantuErrorCode.CAN_NOT_EXECUTE, "Mismatch in difficulty count and children detail count");
                                            }
                                        }
                                    }
                                }else if (detail.type == QuestionType.MCQ) {
                                    if(detail.qusCount > 0){
                                        totalChildrenQuestionCount += detail.qusCount;
                                        if(detail.difficulty == null){
                                            // Implement logic without having difficulty level
                                            LOGGER.debug("Getting random Questions for "+mdata.name+" --> "+children.name+" of type "+detail.type);
                                            getRandomCmdsQuestions(req.orgId,mdata.published,children.id,detail.type,"UNKNOWN",detail.qusCount,qIds);
                                        }else{
                                            if((detail.difficulty.easy + detail.difficulty.tough + detail.difficulty.moderate) == detail.qusCount){
                                                // Implement logic to get cmds question IDs
                                                if(detail.difficulty.easy > 0){
                                                    LOGGER.debug("Getting random Questions for "+mdata.name+" --> "+children.name+" of type "+detail.type+" with difficulty level EASY");
                                                    getRandomCmdsQuestions(req.orgId,mdata.published,children.id,detail.type,"EASY".toLowerCase(),detail.difficulty.easy,qIds);
                                                }
                                                if(detail.difficulty.tough > 0){
                                                    LOGGER.debug("Getting random Questions for "+mdata.name+" --> "+children.name+" of type "+detail.type+" with difficulty level TOUGH");
                                                    getRandomCmdsQuestions(req.orgId,mdata.published,children.id,detail.type,"TOUGH".toLowerCase(),detail.difficulty.tough,qIds);
                                                }
                                                if(detail.difficulty.moderate > 0){
                                                    LOGGER.debug("Getting random Questions for "+mdata.name+" --> "+children.name+" of type "+detail.type+" with difficulty level MODERATE");
                                                    getRandomCmdsQuestions(req.orgId,mdata.published,children.id,detail.type,"MODERATE".toLowerCase(),detail.difficulty.moderate,qIds);
                                                }
                                            }else{
                                                LOGGER.error("Mismatch in total difficulty count and children detail count of metadata "+mdata.id+" at children "+children.id);;
                                                throw new VedantuException(VedantuErrorCode.CAN_NOT_EXECUTE, "Mismatch in difficulty count and children detail count");
                                            }
                                        }
                                    }
                                }else if (detail.type == QuestionType.MATRIX) {
                                    if(detail.qusCount > 0){
                                        totalChildrenQuestionCount += detail.qusCount;
                                        if(detail.difficulty == null){
                                            // Implement logic without having difficulty level
                                            LOGGER.debug("Getting random Questions for "+mdata.name+" --> "+children.name+" of type "+detail.type);
                                            getRandomCmdsQuestions(req.orgId,mdata.published,children.id,detail.type,"UNKNOWN",detail.qusCount,qIds);
                                        }else{
                                            if((detail.difficulty.easy + detail.difficulty.tough + detail.difficulty.moderate) == detail.qusCount){
                                                // Implement logic to get cmds question IDs
                                                if(detail.difficulty.easy > 0){
                                                    LOGGER.debug("Getting random Questions for "+mdata.name+" --> "+children.name+" of type "+detail.type+" with difficulty level EASY");
                                                    getRandomCmdsQuestions(req.orgId,mdata.published,children.id,detail.type,"EASY".toLowerCase(),detail.difficulty.easy,qIds);
                                                }
                                                if(detail.difficulty.tough > 0){
                                                    LOGGER.debug("Getting random Questions for "+mdata.name+" --> "+children.name+" of type "+detail.type+" with difficulty level TOUGH");
                                                    getRandomCmdsQuestions(req.orgId,mdata.published,children.id,detail.type,"TOUGH".toLowerCase(),detail.difficulty.tough,qIds);
                                                }
                                                if(detail.difficulty.moderate > 0){
                                                    LOGGER.debug("Getting random Questions for "+mdata.name+" --> "+children.name+" of type "+detail.type+" with difficulty level MODERATE");
                                                    getRandomCmdsQuestions(req.orgId,mdata.published,children.id,detail.type,"MODERATE".toLowerCase(),detail.difficulty.moderate,qIds);
                                                }
                                            }else{
                                                LOGGER.error("Mismatch in total difficulty count and children detail count of metadata "+mdata.id+" at children "+children.id);;
                                                throw new VedantuException(VedantuErrorCode.CAN_NOT_EXECUTE, "Mismatch in difficulty count and children detail count");
                                            }
                                        }
                                    }
                                }else if (detail.type == QuestionType.NUMERIC) {
                                    if(detail.qusCount > 0){
                                        totalChildrenQuestionCount += detail.qusCount;
                                        if(detail.difficulty == null){
                                            // Implement logic without having difficulty level
                                            LOGGER.debug("Getting random Questions for "+mdata.name+" --> "+children.name+" of type "+detail.type);
                                            getRandomCmdsQuestions(req.orgId,mdata.published,children.id,detail.type,"UNKNOWN",detail.qusCount,qIds);
                                        }else{
                                            if((detail.difficulty.easy + detail.difficulty.tough + detail.difficulty.moderate) == detail.qusCount){
                                                // Implement logic to get cmds question IDs
                                                if(detail.difficulty.easy > 0){
                                                    LOGGER.debug("Getting random Questions for "+mdata.name+" --> "+children.name+" of type "+detail.type+" with difficulty level EASY");
                                                    getRandomCmdsQuestions(req.orgId,mdata.published,children.id,detail.type,"EASY".toLowerCase(),detail.difficulty.easy,qIds);
                                                }
                                                if(detail.difficulty.tough > 0){
                                                    LOGGER.debug("Getting random Questions for "+mdata.name+" --> "+children.name+" of type "+detail.type+" with difficulty level TOUGH");
                                                    getRandomCmdsQuestions(req.orgId,mdata.published,children.id,detail.type,"TOUGH".toLowerCase(),detail.difficulty.tough,qIds);
                                                }
                                                if(detail.difficulty.moderate > 0){
                                                    LOGGER.debug("Getting random Questions for "+mdata.name+" --> "+children.name+" of type "+detail.type+" with difficulty level MODERATE");
                                                    getRandomCmdsQuestions(req.orgId,mdata.published,children.id,detail.type,"MODERATE".toLowerCase(),detail.difficulty.moderate,qIds);
                                                }
                                            }else{
                                                LOGGER.error("Mismatch in total difficulty count and children detail count of metadata "+mdata.id+" at children "+children.id);;
                                                throw new VedantuException(VedantuErrorCode.CAN_NOT_EXECUTE, "Mismatch in difficulty count and children detail count");
                                            }
                                        }
                                    }
                                }else if (detail.type == QuestionType.PARA) {
                                    if(detail.qusCount > 0){
                                        totalChildrenQuestionCount += detail.qusCount;
                                        if(detail.difficulty == null){
                                            // Implement logic without having difficulty level
                                            LOGGER.debug("Getting random Questions for "+mdata.name+" --> "+children.name+" of type "+detail.type);
                                            getRandomCmdsQuestions(req.orgId,mdata.published,children.id,detail.type,"UNKNOWN",detail.qusCount,qIds);
                                        }else{
                                            if((detail.difficulty.easy + detail.difficulty.tough + detail.difficulty.moderate) == detail.qusCount){
                                                // Implement logic to get cmds question IDs
                                                if(detail.difficulty.easy > 0){
                                                    LOGGER.debug("Getting random Questions for "+mdata.name+" --> "+children.name+" of type "+detail.type+" with difficulty level EASY");
                                                    getRandomCmdsQuestions(req.orgId,mdata.published,children.id,detail.type,"EASY".toLowerCase(),detail.difficulty.easy,qIds);
                                                }
                                                if(detail.difficulty.tough > 0){
                                                    LOGGER.debug("Getting random Questions for "+mdata.name+" --> "+children.name+" of type "+detail.type+" with difficulty level TOUGH");
                                                    getRandomCmdsQuestions(req.orgId,mdata.published,children.id,detail.type,"TOUGH".toLowerCase(),detail.difficulty.tough,qIds);
                                                }
                                                if(detail.difficulty.moderate > 0){
                                                    LOGGER.debug("Getting random Questions for "+mdata.name+" --> "+children.name+" of type "+detail.type+" with difficulty level MODERATE");
                                                    getRandomCmdsQuestions(req.orgId,mdata.published,children.id,detail.type,"MODERATE".toLowerCase(),detail.difficulty.moderate,qIds);
                                                }
                                            }else{
                                                LOGGER.error("Mismatch in total difficulty count and children detail count of metadata "+mdata.id+" at children "+children.id);;
                                                throw new VedantuException(VedantuErrorCode.CAN_NOT_EXECUTE, "Mismatch in difficulty count and children detail count");
                                            }
                                        }
                                    }
                                }else if (detail.type == QuestionType.SUBJECTIVE) {
                                    if(detail.qusCount > 0){
                                        totalChildrenQuestionCount += detail.qusCount;
                                        if(detail.difficulty == null){
                                            // Implement logic without having difficulty level
                                            LOGGER.debug("Getting random Questions for "+mdata.name+" --> "+children.name+" of type "+detail.type);
                                            getRandomCmdsQuestions(req.orgId,mdata.published,children.id,detail.type,"UNKNOWN",detail.qusCount,qIds);
                                        }else{
                                            if((detail.difficulty.easy + detail.difficulty.tough + detail.difficulty.moderate) == detail.qusCount){
                                                // Implement logic to get cmds question IDs
                                                if(detail.difficulty.easy > 0){
                                                    LOGGER.debug("Getting random Questions for "+mdata.name+" --> "+children.name+" of type "+detail.type+" with difficulty level EASY");
                                                    getRandomCmdsQuestions(req.orgId,mdata.published,children.id,detail.type,"EASY".toLowerCase(),detail.difficulty.easy,qIds);
                                                }
                                                if(detail.difficulty.tough > 0){
                                                    LOGGER.debug("Getting random Questions for "+mdata.name+" --> "+children.name+" of type "+detail.type+" with difficulty level TOUGH");
                                                    getRandomCmdsQuestions(req.orgId,mdata.published,children.id,detail.type,"TOUGH".toLowerCase(),detail.difficulty.tough,qIds);
                                                }
                                                if(detail.difficulty.moderate > 0){
                                                    LOGGER.debug("Getting random Questions for "+mdata.name+" --> "+children.name+" of type "+detail.type+" with difficulty level MODERATE");
                                                    getRandomCmdsQuestions(req.orgId,mdata.published,children.id,detail.type,"MODERATE".toLowerCase(),detail.difficulty.moderate,qIds);
                                                }
                                            }else{
                                                LOGGER.error("Mismatch in total difficulty count and children detail count of metadata "+mdata.id+" at children "+children.id);;
                                                throw new VedantuException(VedantuErrorCode.CAN_NOT_EXECUTE, "Mismatch in difficulty count and children detail count");
                                            }
                                        }
                                    }
                                }else{
                                    LOGGER.error("Invalid Question type");
                                    throw new VedantuException(VedantuErrorCode.UNKNOWN_QUESTION_TYPE);
                                }
                            }

                            if(totalChildrenQuestionCount != children.qusCount){
                                LOGGER.error("Mismatch in Questions Count of Board Id "+mdata.id +" in children details");
                                throw new VedantuException(VedantuErrorCode.CAN_NOT_EXECUTE, "Mismatch in Questions Count of Board Id "+mdata.name+" in children details");
                            }
                        }
                    }
                    if(childrenTotalQuestionCount != mdata.qusCount) {
                        LOGGER.error("Mismatch in Questions Count of Board Id "+mdata.id +" in children");
                        throw new VedantuException(VedantuErrorCode.CAN_NOT_EXECUTE, "Mismatch in Questions Count of Board Id "+mdata.name+" in children");
                    }
                }else{
                    // Here get boardId of parent board if there are no children boards for that parent board in response
                    for(Details detail : mdata.details) {
                        if(detail.qusCount > 0){
                            LOGGER.debug("Getting random Questions for "+mdata.name+" of type "+detail.type);
                            getRandomCmdsQuestions(req.orgId,PublishedStatus.BOTH,mdata.id,detail.type,"UNKNOWN",detail.qusCount,qIds);
                        }
                    }
                }
            }

            for(String qId : qIds){
                // Add each question to this test
                LOGGER.debug("Adding question "+qId);
                ModifyCMDSTestQuestionsReq addQuesReq = new ModifyCMDSTestQuestionsReq();
                addQuesReq.testId = req.testId;
                addQuesReq.qId = qId;
                addQuesReq.callingApp = req.callingApp;
                addQuesReq.callingAppId = req.callingAppId;
                addQuesReq.callingUserId = req.callingUserId;
                addQuesReq.userId =  req.userId;
                addQuesReq.orgId = req.orgId;
                ModifyCMDSTestQuestionsRes res = addQuestion(addQuesReq);
                if(res.success){
                    LOGGER.debug(qId+" is successfully added");
                }
            }
        }catch(Exception e){
            LOGGER.error("Exception Occured while auto generating test");
            throw new VedantuException(VedantuErrorCode.UNABLE_TO_CREATE_TEST, "Exception Occured "+e.getMessage());
        }
        return createCmdsTestRes;
    }

    /**
     * @param orgId is Organisation Id of the institute
     * @param published is Question Published State
     * @param childId is chapterId of that Subject
     * @param type is Question Type
     * @param difficulty is Difficulty of the question to select
     * @param count is count of above difficulty questions we need to generate
     * @param qIds is set where we gonna add generated cmds Questions by checking duplicates
     */
    private static void getRandomCmdsQuestions(String orgId, PublishedStatus published, String childId,
            QuestionType type, String difficulty, int count, Set<String> qIds) {
        LOGGER.debug("Getting random Questions : Inside getRandomCmdsQuestions ");
        LOGGER.debug("Getting random Questions : Params are orgId is "+orgId+" published status is "+published+" child Id is "+childId+" question type is "+type+" Difficulty is "+difficulty);
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        // Add Organisation filter
        // If this institute has Learnpedia institute question bank access then add learnpedia orgId in filter
        TermsQueryBuilder organisationBuilder = QueryBuilders.inQuery("contentSrc.id",orgId);
        boolQuery.must(organisationBuilder);
        // Add child Id filter
        TermsQueryBuilder boardBuilder = QueryBuilders.inQuery("boards.id", childId);
        boolQuery.must(boardBuilder);
        // Add question type filter
        if(type == QuestionType.PARA){
            type = QuestionType.TEXT;
        }
        TermsQueryBuilder questionTypeBuilder = QueryBuilders.inQuery("type",type.toString().toLowerCase());
        boolQuery.must(questionTypeBuilder);
        // Add difficulty filter
        if(!difficulty.equals("UNKNOWN")){
            TermsQueryBuilder difficultyBuilder = QueryBuilders.inQuery("difficulty", difficulty);
            boolQuery.must(difficultyBuilder);
        }
        // Add published filter
        if(PublishedStatus.BOTH != published){
            TermsQueryBuilder publishedBuilder;
            publishedBuilder = QueryBuilders.inQuery("published", PublishedStatus.PUBLISHED == published ? true : false);
            boolQuery.must(publishedBuilder);
        }

        String learnpediaId = Play.application().configuration().getString("learnpedia.id");
        if(!orgId.equals(learnpediaId)){
            if(!CMDSResourcesManager.includeLearnpediaQuestions(learnpediaId,orgId)){
                boolQuery.must(QueryBuilders.termQuery("scope", "org"));
            }
        }

        SearchResponse questionsResponse = ElasticSearchUtils.getSearchResponse(
                boolQuery, "", "", 0, 0, EntityType.CMDSQUESTION.getIndexName(),
                EntityType.CMDSQUESTION.getIndexType().toLowerCase(), null, false,
                (AbstractFacetBuilder[]) null);
        if (questionsResponse == null || questionsResponse.getHits().getTotalHits() == 0) {
            LOGGER.error("Getting random Questions : empty search response for questions query : "+type+" with child Id "+childId);
            return;
        }
        int hits = (int) questionsResponse.getHits().totalHits();
        LOGGER.debug("Getting random Questions : Total hits for query with child Id "+childId+" and question type "+type+" is "+hits);
        if(type == QuestionType.TEXT){
            // Get List Of All Questions which match the above criteria
            List<String> allParagraphQIds = getAllQuestionIds(boolQuery,hits,orgId,100);
            LOGGER.debug("Getting random Questions : Total para qIds for the above filter is "+allParagraphQIds.size());
            Set<String> paraQIds = new HashSet<String>();
            Set<Integer> randomNumbers = new HashSet<Integer>();
            while(count > 0){
                if(allParagraphQIds.size() == 0){
                    break;
                }
                int randomQuestionNumber = getRandomNumber(allParagraphQIds.size(),randomNumbers);
                if(paraQIds.contains(allParagraphQIds.get(randomQuestionNumber))){
                    allParagraphQIds.remove(randomQuestionNumber);
                }else{
                    paraQIds.add(allParagraphQIds.get(randomQuestionNumber));
                    List<String> paraQuesIds = getParaQuesIds(allParagraphQIds.get(randomQuestionNumber));
                    LOGGER.debug("Getting random Questions : Total para questions for this paragraph are "+paraQuesIds.size());
                    if(paraQuesIds != null){
                        if(count > MAX_PARA_QUESTIONS){
                            if(paraQuesIds.size() >= MAX_PARA_QUESTIONS){
                                for(int  i = 0; i < MAX_PARA_QUESTIONS; i++){
                                    qIds.add(paraQuesIds.get(i));
                                }
                                count -= MAX_PARA_QUESTIONS;
                            }else{
                                for(int  i = 0; i < paraQuesIds.size(); i++){
                                    qIds.add(paraQuesIds.get(i));
                                    count--;
                                }
                            }
                        } else {
                            if (paraQuesIds.size() >= count) {
                                for (int i = 0; i < count; i++) {
                                    qIds.add(paraQuesIds.get(i));
                                }
                                count -= count;
                            }else{
                                for (int i = 0; i < paraQuesIds.size(); i++) {
                                    qIds.add(paraQuesIds.get(i));
                                    count--;
                                }
                            }
                        }
                        randomNumbers.add(randomQuestionNumber);
                    }
                    allParagraphQIds.remove(randomQuestionNumber);
                }
            }
        }else{
            // There are more questions in bucket to pick
            // So use random function to pick questions
            // Maintaining difference of 10(Its Upto You)
            int diff = 10;
            if(hits > (count+diff)){
                LOGGER.debug("Getting random Questions : Total bucket size is "+hits+" and total questions we need to find is "+count);
                Set<Integer> randomNumbers = new HashSet<Integer>();
                while(count > 0){
                    int randomQuestionNumber = getRandomNumber(hits,randomNumbers);
                    String qId = getQuestionId(boolQuery,orgId,randomQuestionNumber);
                    LOGGER.debug("Getting random Questions : Random question Id picked is "+qId);
                    if(qIds.contains(qId)){
                        LOGGER.debug("Getting random Questions : qIds list already contains the above question");
                        continue;
                    }else if(qId.isEmpty()){
                        LOGGER.debug("Getting random Questions : The above question is EMPTY");
                        continue;
                    }
                    else{
                        LOGGER.debug("Getting random Questions : Adding randomly picked question to qIds list");
                        qIds.add(qId);
                        count--;
                        randomNumbers.add(randomQuestionNumber);
                    }
                }
            }
            // There are less questions in the bucket
            else{
                LOGGER.debug("Getting random Questions : Total bucket size is "+hits+" and total questions we need to find is "+count);
                List<String> allQIds = getAllQuestionIds(boolQuery,hits,orgId,hits);
                LOGGER.debug("Getting random Questions : All questons we got is "+allQIds.size());
                if(allQIds.size() > count){
                    LOGGER.debug("Getting random Questions : Adding sublist from all questions to qIds");
                    qIds.addAll(allQIds.subList(0, count));
                }
                else{
                    LOGGER.debug("Getting random Questions : Adding all questions to qIds");
                    qIds.addAll(allQIds);
                }
            }
        }

        LOGGER.debug("Getting random Questions : Questions picked are "+qIds);
    }

    public static List<String> getParaQuesIds(String id) {
        // TODO Auto-generated method stub
        try {
            CMDSQuestion paragraph = CMDSQuestionDAO.INSTANCE.getQuestionById(id);
            if(paragraph == null){
                return null;
            }
            return paragraph.paraIds;

        } catch (VedantuException e) {
            LOGGER.error("Exception occured while getting paragraph question Ids from paragraph");
        }
        return null;
    }

    public static List<String> getAllQuestionIds(BoolQueryBuilder boolQuery, int hits, String orgId,int maxQuestionsToPick) {
        // TODO Auto-generated method stub
        List<String> questionIds = new ArrayList<String>();
        int maxQuestionsSlot = maxQuestionsToPick;
        int start = 0;
        String folderId = CMDSFolderDAO.INSTANCE.getRootFolder(orgId)._getStringId();
        while(hits > 0){
            List<CMDSResourceDetails> details = new ArrayList<CMDSResourceDetails>();
            SearchResponse response = ElasticSearchUtils.getSearchResponse(
                    boolQuery, "", "", start, maxQuestionsSlot, EntityType.CMDSQUESTION.getIndexName(),
                    EntityType.CMDSQUESTION.getIndexType().toLowerCase(), null, false,
                    (AbstractFacetBuilder[]) null);
            AbstractCMDSContentManager.getBasicInfoFromESSearch(response,
                    details,"CMDSQUESTION");
            List<CMDSContentLink> links = new ArrayList<CMDSContentLink>();

            MutableLong totalHits = new MutableLong(0L);
            CMDSContentLink link = null;
            for (CMDSResourceDetails detail : details) {
                List<CMDSContentLink> testLinks = CmdsContentLinkDAO.INSTANCE.getCmdsContentLinks(
                        detail.content, null, null, null, 0, 1,
                        totalHits);
                if (CollectionUtils.isNotEmpty(testLinks)) {
                    link = testLinks.get(0);
                    link.target.type = EntityType.FOLDER;
                    link.target.id = folderId;
                    links.add(link);
                } else {
                    LOGGER.debug(" Mismatch content is : "+detail.content);
                    LOGGER.error(" Mismatch in ES and MONGODB results ");
                }
            }
            for(CMDSContentLink contentLink : links){
                questionIds.add(contentLink.source.id);
            }
            hits = hits - ((int) response.getHits().totalHits());
            start = start + ((int) response.getHits().totalHits());
        }
        return questionIds;
    }

    public static String getQuestionId(BoolQueryBuilder boolQuery, String orgId, int randomNumber) {
        // TODO Auto-generated method stub
        int size = 1;
        int start = randomNumber;
        String questionId = StringUtils.EMPTY;
        String folderId = CMDSFolderDAO.INSTANCE.getRootFolder(orgId)._getStringId();
        List<CMDSResourceDetails> details = new ArrayList<CMDSResourceDetails>();
        SearchResponse response = ElasticSearchUtils.getSearchResponse(
                boolQuery, "", "", start, size, EntityType.CMDSQUESTION.getIndexName(),
                EntityType.CMDSQUESTION.getIndexType().toLowerCase(), null, false,
                (AbstractFacetBuilder[]) null);
        AbstractCMDSContentManager.getBasicInfoFromESSearch(response,
                details,"CMDSQUESTION");
        List<CMDSContentLink> links = new ArrayList<CMDSContentLink>();

        MutableLong totalHits = new MutableLong(0L);
        CMDSContentLink link = null;
        for (CMDSResourceDetails detail : details) {
            List<CMDSContentLink> testLinks = CmdsContentLinkDAO.INSTANCE.getCmdsContentLinks(
                    detail.content, null, null, null, 0, 1,
                    totalHits);
            if (CollectionUtils.isNotEmpty(testLinks)) {
                link = testLinks.get(0);
                link.target.type = EntityType.FOLDER;
                link.target.id = folderId;
                links.add(link);
            } else {
                LOGGER.debug(" Mismatch content is : "+detail.content);
                LOGGER.error(" Mismatch in ES and MONGODB results ");
            }
        }
        for(CMDSContentLink contentLink : links){
            questionId = contentLink.source.id;
        }
        return questionId;
    }

    public static int getRandomNumber(int max, Set<Integer> randomNumbers){
     // create instance of Random class
        Random rand = new Random();
        int number = rand.nextInt(max);
        return randomNumbers.contains(number) ? getRandomNumber(max, randomNumbers) : number;
    }

    public static GetCMDSTestRes getTestInfo(GetCMDSTestReq req) throws VedantuException {

        CMDSTest test = CMDSTestDAO.INSTANCE.getTest(req.id);
        GetCMDSTestRes getTestRes = new GetCMDSTestRes();
        CMDSDocument document = null;
        if(!StringUtils.isEmpty(test.pdfId)){
            document = CMDSDocumentDAO.INSTANCE.getById(test.pdfId);
            getTestRes.pdf = document;
            getTestRes.pdfId = test.pdfId;
        }
        if(!StringUtils.isEmpty(test.password)){
            getTestRes.password = test.password;
        }
        if(!StringUtils.isEmpty(test.resultPassword)){
            getTestRes.resultPassword = test.resultPassword;
        }
        getTestRes.enablePartialMarks = test.enablePartialMarks;
        getTestRes.partialMarksQTypes = test.partialMarksQTypes;
        getTestRes.oneOrMoreMarksQTypes = test.oneOrMoreMarksQTypes;
        getTestRes.enableSectionLocking = test.enableSectionLocking;
        getTestRes.enableAutoResumeTest = test.autoResumeTest;
        getTestRes.subjectiveTest = test.subjectiveTest;
        getTestRes.isNTAPattern=test.isNTAPattern;
        getTestRes.fromMongoModel(test);
        getUserInfoMap(req.orgId, Arrays.asList(test.userId));
        getTestRes.user = getUserInfoMap(req.orgId, Arrays.asList(test.userId)).get(test.userId);
        return getTestRes;
    }

    public static GetCMDSTestQuestionsRes getTestQuestions(GetCMDSTestQuestionsReq req)
            throws VedantuException {

        GetCMDSTestQuestionsRes questions = new GetCMDSTestQuestionsRes();

        CMDSTest test = CMDSTestDAO.INSTANCE.getTest(req.testId);

        List<String> qIds = test.__getAllQIds(req.brdId);
        if (CollectionUtils.isEmpty(qIds)) {
            LOGGER.error("no question found for brdId: " + req.brdId);
            return questions;
        }
        Map<String, CMDSQuestionInfo> questionsMap = CMDSQuestionDAO.INSTANCE
                .toBasicInfosMap(CMDSQuestionDAO.INSTANCE.getByIds(ObjectIdUtils.toObjectIds(qIds,
                        true)));
        LOGGER.debug("cmds question map : " + questionsMap);
        questions.totalHits = qIds.size();
        for (String qid : qIds) {
            questions.list.add(questionsMap.get(qid));
        }
        return questions;
    }

    public static FinishCMDSTestEditRes finishTestEditing(FinishCMDSTestEditReq req)
            throws VedantuException {

        CMDSTest cmdsTest = CMDSTestDAO.INSTANCE.getById(req.testId);
        cmdsTest._finishEditing();
        CMDSTestDAO.INSTANCE.save(cmdsTest);
        ReIndexDetails details = new ReIndexDetails();
        details.type = EntityType.CMDSTEST;
        details.userId = req.userId;
        details.ids = Arrays.asList(new String[] { req.testId });
        generateEventAysc(req.userId, details, EventType.REINDEX_CMDS_RESOURCE);
        FinishCMDSTestEditRes res = new FinishCMDSTestEditRes(cmdsTest._getStringId(), true);
        return res;
    }

    /**
     *
     * @param assignmentId
     * @param qid
     * @param assignmentId
     *            --> {@linkplain brdId is the id of courseId(i.e id of Physics)}
     * @param childBrdId
     *            {@linkplain childBrdId is the id of topic this question belongs too}
     */
    public static ModifyCMDSTestQuestionsRes addQuestion(ModifyCMDSTestQuestionsReq req)
            throws VedantuException {

        return modifyTestQuestions(req, false);
    }

    public static ModifyCMDSTestQuestionsRes removeQuestion(ModifyCMDSTestQuestionsReq req)
            throws VedantuException {

        return modifyTestQuestions(req, true);
    }

    public static ModifyCMDSTestQuestionsRes modifyTestQuestions(ModifyCMDSTestQuestionsReq req,
            boolean remove) throws VedantuException {

        CMDSTest test = CMDSTestDAO.INSTANCE.getTest(req.testId);
        if (test.published || test.scope == Scope.PUBLIC) {
            String errorMsg = "test[" + req.testId
                    + "] can not be edited as it's being published or shared with some users";
            LOGGER.error(errorMsg);
            throw new VedantuException(VedantuErrorCode.ALREADY_PUBLISHED, errorMsg);
        }
        CMDSQuestion cmdsquestion = CMDSQuestionDAO.INSTANCE.getQuestionById(req.qId);
        CMDSQuestionSearchIndexDetails question = new CMDSQuestionSearchIndexDetails();
        question.fromMongoModel(cmdsquestion);
        if (!StringUtils.equals(req.userId, question.userId) && question.scope == Scope.PRIVATE) {
            LOGGER.error("question [id:" + req.qId + "] with Scope[" + question.scope
                    + "] only visible to user[" + question.userId + "] as owner");
            throw new VedantuException(VedantuErrorCode.CMDS_QUESTION_NOT_FOUND);
        }

        BoardSearchEntity courseBoard = question.__getBoard(BoardType.COURSE);
        if (courseBoard == null) {
            String errorMsg = "no course is being tagged on question [" + req.qId + "]";
            LOGGER.error(errorMsg);
            throw new VedantuException(VedantuErrorCode.BOARD_NOT_FOUND, errorMsg);
        }
        LOGGER.info("testMetadata : " + test.metadata);
        TestMetadata metadata = test.__getTestMetadata(courseBoard.id);
        if (metadata == null) {
            LOGGER.error("metadata for [brdId:" + courseBoard.id + ",type: " + courseBoard.type
                    + "] for testId [" + req.testId + "] not found");
            throw new VedantuException(VedantuErrorCode.METADATA_NOT_FOUND);
        }

        boolean correctQuestionType = false;

        List<TestDetails> details = metadata.details;
        for(TestDetails testDetail : details){
            if(testDetail.type == question.type){
                correctQuestionType = true;
                break;
            }
        }

        if (!correctQuestionType) {
            LOGGER.error("Question Type for [brdId:" + courseBoard.id + ",type: " + courseBoard.type
                    + "] for testId [" + req.testId + "] not found");
            throw new VedantuException(VedantuErrorCode.INVALID_QUESTION_TYPE);
        }

        boolean updated = false;
        List<BoardSearchEntity> topicBoards = question.__getBoards(BoardType.TOPIC);
        if (topicBoards.isEmpty() && test.type == TestType.TEST) {
            String errorMsg = "cmdsquestion[" + question.id
                    + "] does not seem to have a board with TYPE:TOPIC" + topicBoards;
            LOGGER.error(errorMsg);
            throw new VedantuException(VedantuErrorCode.BOARD_NOT_FOUND, errorMsg);
        }
        try {
            LOGGER.info("child board for question : " + topicBoards);
            boolean addOnlyToBoard = false;
            for (BoardSearchEntity topicBoard : topicBoards) {
                updated = remove ? metadata.removeQuestion(question.id, question.type, topicBoard,
                        req.testId, test.type) : metadata.addQuestion(question.id, question.type,
                        topicBoard, req.testId, test.type, addOnlyToBoard);
                addOnlyToBoard = true;
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new VedantuException(VedantuErrorCode.QUESTION_MAX_COUNT_EXCEED);
        }

        ModifyCMDSTestQuestionsRes res = new ModifyCMDSTestQuestionsRes(courseBoard.name,
                courseBoard.id, courseBoard.type, question.id, question.type);
        res.success = updated;
        if (!topicBoards.isEmpty()) {
            // TODO: return list of topics instead single board
            res.child = topicBoards.get(0);
        }

        if (res.success) {
            test.completed = CMDSTestDAO.INSTANCE.isReadyToPublished(test);
            SrcEntity entity = new SrcEntity();
            entity.type = EntityType.TEST;
            entity.id = test._getStringId();
            CMDSModuleDAO.INSTANCE.updateModuleStatus(entity);
            test.completed = CMDSTestDAO.INSTANCE.isReadyToPublished(test);
            CMDSTestDAO.INSTANCE.save(test);
            generateEventAysc(test.userId, test, EventActionType.UPDATE, EventType.INDEX_CMDS_TEST,
                    UserActionType.UPDATED, false);
        }

        LOGGER.info("returning modify question res : " + res + ", remove:" + remove);
        return res;
    }

    public static GetCMDSTestsRes getTests(GetTestsReq request) throws VedantuException {

        GetCMDSTestsRes response = new GetCMDSTestsRes();
        MutableLong totalHits = new MutableLong(0L);
        List<CMDSTest> tests = CMDSTestDAO.INSTANCE.getCMDSTests(request.query, request.orgId,
                request.includeTypes, request.excludeTypes, request.start, request.size,
                request.published, request.state, totalHits);
        if (CollectionUtils.isNotEmpty(tests)) {
            for (CMDSTest test : tests) {

                int programsAddedTo = CMDSLibraryManager.getAllProgramsAddedTo(new SrcEntity(
                        EntityType.CMDSTEST, test._getStringId()), CmdsContentLinkType.ADDED);
                CMDSTestBasicInfo testInfo = new CMDSTestBasicInfo(test._getStringId(), test.name,
                        test.contentSrc != null ? test.contentSrc.id : null, test.timeCreated,
                        test.lastUpdated, test.userId, programsAddedTo, test.published,
                        test.completed, true, test.globalTestId, test.recordState);
                testInfo.subType = test.type.name();

                response.list.add(testInfo);

            }
            response.totalHits = totalHits.longValue();
        }
        return response;
    }

    public static int fixTestBoardMarks(String mode) throws VedantuException {

        DBObject query = new BasicDBObject("mode", mode);
        query.put("globalTestId", new BasicDBObject(MongoManager.NE_QUERY, null));
        VedantuDBResult<CMDSTest> cmdsTests = CMDSTestDAO.INSTANCE.getInfos(query, null,
                MongoManager.NO_START, MongoManager.NO_LIMIT, null);
        for (CMDSTest cmdsTest : cmdsTests.results) {
            cmdsTest.computeTotalQusAndMarks();
            CMDSTestDAO.INSTANCE.save(cmdsTest);
            generateEventAysc(cmdsTest.userId, cmdsTest, EventActionType.UPDATE,
                    EventType.INDEX_CMDS_TEST, UserActionType.UPDATED, false);
            if (StringUtils.isNotEmpty(cmdsTest.globalTestId)) {
                Test globalTest = TestDAO.INSTANCE.getById(cmdsTest.globalTestId);
                if (globalTest != null) {
                    globalTest.computeTotalQusAndMarks();
                    TestDAO.INSTANCE.save(globalTest);
                    generateEventAysc(globalTest.userId, globalTest, EventActionType.UPDATE,
                            EventType.INDEX_TEST, UserActionType.UPDATED, false);
                }
            }
        }
        return cmdsTests.totalHits;
    }

    @Override
    public boolean update(EditContentReq request) throws VedantuException {

        LOGGER.debug("......................Entering Test Update......................");
        CMDSTest content = CMDSTestDAO.INSTANCE.getById(request.entity.id);

        if (content == null) {
            throw new VedantuException(VedantuErrorCode.CONTENT_NOT_FOUND);
        }

        List<String> updateList = new ArrayList<String>();
        if (StringUtils.isNotEmpty(request.name)
                && request.updateList.contains(EditContentReq.NAME)) {
            content.name = request.name;
            updateList.add(AbstractContentModel.NAME);
        }

        if (request.updateList.contains(EditContentReq.DESCRIPTION)) {
            content.desc = request.description;
            updateList.add(AbstractTestCommonModel.DESC);
        }

        boolean updated = true;
        if (content.globalTestId != null) {
            request.entity = new SrcEntity(EntityType.TEST, content.globalTestId);
            updated = TestManager.INSTANCE.update(request);
        }

        if (updated) {
            updateContent(request.userId, content, updateList);
        }

        return true;
    }

    public UpdateTestResultVisibilityRes updateTestResultVisibility(
            UpdateTestResultVisibilityReq req) throws VedantuException {

        UpdateTestResultVisibilityRes res = new UpdateTestResultVisibilityRes();
        CMDSTest test = CMDSTestDAO.INSTANCE.getTest(req.entity.id);
        List<String> updateList = new ArrayList<String>();
        if (req.resultVisibilityMessage == null) {
            req.resultVisibilityMessage = StringUtils.EMPTY;
        }
        test.resultVisibility = req.getResultVisibility();
        test.resultVisibilityMessage = req.resultVisibilityMessage;
        updateList.add(ConstantsGlobal.RESULT_VISIBILITY);
        updateList.add(ConstantsGlobal.RESULT_VISIBILITY_MESSAGE);
        LOGGER.debug("updating fields: " + updateList + ", resultVisibility: "
                + test.resultVisibility + ", resultVisibilityMessage:"
                + test.resultVisibilityMessage);
        updateContent(req.userId, test, updateList);
        if (StringUtils.isNotEmpty(test.globalTestId)) {
            TestManager.INSTANCE.updateTestResultVisibility(req.userId, new SrcEntity(
                    EntityType.TEST, test.globalTestId), test.resultVisibility,
                    test.resultVisibilityMessage);
        }
        res.resultVisibility = test.resultVisibility;
        return res;
    }

    private void updateContent(String userId, CMDSTest content, List<String> updateList)
            throws VedantuException {

        CMDSTestDAO.INSTANCE.updateModel(content, updateList);
        SrcEntity entity = new SrcEntity(EntityType.CMDSTEST, content._getStringId());
        generateEventAysc(userId, content, EventActionType.UPDATE, EventType.INDEX_CMDS_TEST,
                UserActionType.UPDATED, false);

        addAsCMDSResource(entity, EventActionType.UPDATE, content);
    }

    @Override
    public boolean calculate(String id,boolean recalculate, VedantuBaseMongoModel... contents) throws VedantuException {

        List<CMDSTest> tests = new ArrayList<CMDSTest>();

        if (StringUtils.isNotEmpty(id)) {
            CMDSTest question = CMDSTestDAO.INSTANCE.getById(id);

            if (question == null) {
                return false;
            }
            tests.add(question);
        }

        if (contents != null && contents.length != 0) {
            for (VedantuBaseMongoModel content : contents) {
                if (content instanceof CMDSTest) {
                    tests.add((CMDSTest) content);
                }
            }
        }

        // calculate question image size;

        for (CMDSTest test : tests) {
            if( test.size.isFinalized() && !recalculate){
                continue;
            }
            test.size.reset();
            LOGGER.debug(" question ids" + test.__getAllQIds());
            List<CMDSQuestion> questions = CMDSQuestionDAO.INSTANCE.getByIds(ObjectIdUtils
                    .toObjectIds(test.__getAllQIds()));
            if(CollectionUtils.isEmpty(questions)){
                continue;
            }
            for (CMDSQuestion question : questions) {
                if (!question.size.isFinalized()) {
                    CMDSQuestionManager.INSTANCE.calculate(null,true, question);
                }
                test.size.add(question.size);
            }

            test.size.finalize();
            CMDSTestDAO.INSTANCE.updateModel(test, Arrays.asList(CMDSTest.SIZE));
            if (test.globalTestId != null ) {
                TestManager.INSTANCE.calculate(test.globalTestId,true );
            }
        }
        return true;
    }

    public boolean addPdfIdToTest(String testId , String pdfId) throws VedantuException {
        CMDSTest cmdstest = CMDSTestDAO.INSTANCE.getTest(testId);
        if(cmdstest != null){
            cmdstest.pdfId = pdfId;
        }
        CMDSTestDAO.INSTANCE.save(cmdstest);
        return true;
    }

    public SetPasswordForTestRes setPasswordForTest(SetPasswordForTestReq request) {
        SetPasswordForTestRes response = new SetPasswordForTestRes();
        CMDSTest cmdsTest = CMDSTestDAO.INSTANCE.getById(request.testId);
        cmdsTest.password = request.password;
        cmdsTest.resultPassword = request.resultPassword;
        CMDSTestDAO.INSTANCE.save(cmdsTest);
        response.success = true;
        return response;
    }

    public simplifyBoardNamesRes simplifyBoardNames(simplifyBoardNamesReq request) {
        // TODO Auto-generated method stub
        simplifyBoardNamesRes resp = new simplifyBoardNamesRes();
        CMDSTest cmdsTest = CMDSTestDAO.INSTANCE.getById(request.cmdsTestId);
        List<BoardInfo> brdInfos = new ArrayList<BoardInfo>();
        List<SimplifiedBoardNameObject> simplifiedBrdInfos = new ArrayList<SimplifiedBoardNameObject>();
        Map<String,String> brdMappings = new HashMap<String, String>();
        if(cmdsTest.metadata != null && !cmdsTest.metadata.isEmpty()){
            for(TestMetadata metadata : cmdsTest.metadata){
                BoardInfo brdInfo = new BoardInfo();
                brdInfo.boardId = metadata.id;
                brdInfo.boardName = metadata.name;
                brdMappings.put(metadata.id, metadata.name);
                brdInfos.add(brdInfo);
            }

            if(cmdsTest.simplifiedBoardNames != null && !cmdsTest.simplifiedBoardNames.isEmpty()){
              List<SimplifiedBoardNames> simplifiedBrdNames = cmdsTest.simplifiedBoardNames;
              for(SimplifiedBoardNames simplifiedBrdName : simplifiedBrdNames){
                  SimplifiedBoardNameObject simplifiedBoardObjects = new SimplifiedBoardNameObject();
                  List<BoardInfo> simplifiedBrdInfo = new ArrayList<BoardInfo>();
                  List<String> brdIds = simplifiedBrdName.brdIds;
                  for(String brdId : brdIds){
                      brdInfos = removeBoardIdFromBoardInfoIfPresent(brdId,brdInfos);
                      BoardInfo brdInfo = new BoardInfo();
                      brdInfo.boardId = brdId;
                      brdInfo.boardName = brdMappings.get(brdId);
                      simplifiedBrdInfo.add(brdInfo);
                  }
                  simplifiedBoardObjects.simplifiedName = simplifiedBrdName.simplifiedName;
                  simplifiedBoardObjects.brdInfo = simplifiedBrdInfo;
                  simplifiedBrdInfos.add(simplifiedBoardObjects);
              }
          }
        }
        resp.testBrdIdsWithNames = brdInfos;
        resp.simplifiedBrdInfo = simplifiedBrdInfos;
        return resp;
    }

    private List<BoardInfo> removeBoardIdFromBoardInfoIfPresent(String brdId,
            List<BoardInfo> brdInfos) {
        List<BoardInfo> newBrdInfos = new ArrayList<BoardInfo>();
        for(BoardInfo brdInfo : brdInfos){
            if(!brdInfo.boardId.equals(brdId)){
                newBrdInfos.add(brdInfo);
            }
        }
        return newBrdInfos;
    }

    public simplifyBoardNamesRes addSimplifiedBoardNames(simplifyBoardNamesReq request) {
        // TODO Auto-generated method stub
        CMDSTest cmdsTest = CMDSTestDAO.INSTANCE.getById(request.cmdsTestId);
        if(request.simplifiedBoards != null){
            if(cmdsTest.simplifiedBoardNames == null){
                cmdsTest.simplifiedBoardNames = new ArrayList<SimplifiedBoardNames>();
                cmdsTest.simplifiedBoardNames.add(request.simplifiedBoards);
            }else{
                cmdsTest.simplifiedBoardNames.add(request.simplifiedBoards);
            }
        }
        CMDSTestDAO.INSTANCE.save(cmdsTest);
        return simplifyBoardNames(request);
    }

    public simplifyBoardNamesRes removeSimplifiedBoardNames(simplifyBoardNamesReq request) throws VedantuException {
        // TODO Auto-generated method stub
        CMDSTest cmdsTest = CMDSTestDAO.INSTANCE.getById(request.cmdsTestId);
        if(request.simplifiedBoards != null){
            if(cmdsTest.simplifiedBoardNames == null){
                throw new VedantuException(VedantuErrorCode.CONTENT_NOT_FOUND);
            }else{
                cmdsTest.simplifiedBoardNames.remove(request.simplifiedBoards);
            }
        }
        CMDSTestDAO.INSTANCE.save(cmdsTest);
        return simplifyBoardNames(request);
    }

    public SetPasswordForTestRes enableOrDisablePartialMarks(SetPasswordForTestReq request) throws VedantuException {
        // TODO Auto-generated method stub
        SetPasswordForTestRes response = new SetPasswordForTestRes();
        CMDSTest cmdsTest = CMDSTestDAO.INSTANCE.getById(request.testId);
        if(request.enablePartialMarks){
            if(CollectionUtils.isNotEmpty(request.qTypes)){
                cmdsTest.enablePartialMarks = request.enablePartialMarks;
                for(String qType : request.qTypes){
                    if(!request.oneOrMoreMarksQTypes.contains(qType)){
                        throw new VedantuException(VedantuErrorCode.ACTION_NOT_ALLOWED, "Please select the checkbox corresponding to one/more answers of type "+qType);
                    }
                }
                cmdsTest.partialMarksQTypes = request.qTypes;
                cmdsTest.oneOrMoreMarksQTypes = request.oneOrMoreMarksQTypes;
                CMDSTestDAO.INSTANCE.save(cmdsTest);
            }else{
                throw new VedantuException(VedantuErrorCode.ACTION_NOT_ALLOWED, "Please choose atleast one question type, to which partial marking should be enabled");
            }
        }else{
            cmdsTest.enablePartialMarks = request.enablePartialMarks;
            cmdsTest.partialMarksQTypes = request.qTypes;
            cmdsTest.oneOrMoreMarksQTypes = request.oneOrMoreMarksQTypes;
            CMDSTestDAO.INSTANCE.save(cmdsTest);
        }
        response.success = true;
        return response;
    }

    public SetPasswordForTestRes enableOrDisableSectionLocking(SetPasswordForTestReq request) throws VedantuException {
        // TODO Auto-generated method stub
        SetPasswordForTestRes response = new SetPasswordForTestRes();
        CMDSTest cmdsTest = CMDSTestDAO.INSTANCE.getById(request.testId);
        cmdsTest.enableSectionLocking = request.enableSectionLocking;
        CMDSTestDAO.INSTANCE.save(cmdsTest);
        response.success = true;
        return response;
    }

    public SetPasswordForTestRes enableAutoResumeTest(SetPasswordForTestReq request) throws VedantuException {
        // TODO Auto-generated method stub
        SetPasswordForTestRes response = new SetPasswordForTestRes();
        CMDSTest cmdsTest = CMDSTestDAO.INSTANCE.getById(request.testId);
        cmdsTest.autoResumeTest = request.enableAutoResumeTest;
        CMDSTestDAO.INSTANCE.save(cmdsTest);
        response.success = true;
        return response;
    }

    public static GetReGenerateAnalyticsRes regenerateAnalytics(GetCMDSTestReq getTestReq)
            throws VedantuException {
        // TODO Auto-generated method stub
        GetReGenerateAnalyticsRes response = new GetReGenerateAnalyticsRes();
        Test test = TestDAO.INSTANCE.getById(getTestReq.id);
        if (test == null) {
            throw new VedantuException(VedantuErrorCode.ENTITY_NOT_FOUND);
        } else {
            if (test.regeneratingAnalytics) {
                throw new VedantuException(VedantuErrorCode.ANALYTICS_GENERATION_UNDER_PROCESS,
                        "Analytics are still being generated, Kindly wait ");
            } else {
                test.regeneratingAnalytics = true;
                TestDAO.INSTANCE.save(test);
                EndTestDetails endTestDetails = new EndTestDetails(null, getTestReq.userId, getTestReq.id,
                        EntityType.TEST, null, 0, 0, getTestReq.orgId, "TEST");
                generateEventAysc(getTestReq.userId, endTestDetails, EventType.END_TEST, 0);
                response.message = "Analytics regeneration request successfully received";
                response.success = true;
            }
        }
        return response;
    }

}
