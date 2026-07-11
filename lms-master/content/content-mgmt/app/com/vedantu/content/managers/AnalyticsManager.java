package com.vedantu.content.managers;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.vedantu.content.enums.AnswerCorrectness;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableLong;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.json.JSONException;

import play.Logger;
import play.Logger.ALogger;
import play.Play;

import com.google.code.morphia.query.Query;
import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.vedantu.board.daos.BoardDAO;
import com.vedantu.board.pojos.BoardBasicInfo;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.enums.AuthType;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.pojos.ScheduleInfo;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.commons.pojos.responses.SearchListResponse;
import com.vedantu.commons.utils.ObjectIdUtils;
import com.vedantu.commons.utils.ObjectMapperUtils;
import com.vedantu.content.apis.IAnalyticsBoardMember;
import com.vedantu.content.daos.AnswerDAO;
import com.vedantu.content.daos.AssignmentDAO;
import com.vedantu.content.daos.LibraryContentLinksDAO;
import com.vedantu.content.daos.ModuleSchedulesDAO;
import com.vedantu.content.daos.QuestionDAO;
import com.vedantu.content.daos.TestDAO;
import com.vedantu.content.daos.analytics.EntityAnalyticsDAO;
import com.vedantu.content.daos.analytics.EntityHighScoreDAO;
import com.vedantu.content.daos.analytics.QuestionAnalyticsDAO;
import com.vedantu.content.daos.analytics.UserAnalyticsDAO;
import com.vedantu.content.daos.analytics.UserEntityAnalyticsDAO;
import com.vedantu.content.daos.analytics.UserEntityAttemptDAO;
import com.vedantu.content.daos.analytics.UserQuestionAnalyticsDAO;
import com.vedantu.content.daos.analytics.UserQuestionAttemptDAO;
import com.vedantu.content.enums.AttemptStatus;
import com.vedantu.content.enums.EnumBasket.Judgement;
import com.vedantu.content.enums.EnumBasket.Status;
import com.vedantu.content.enums.QuestionType;
import com.vedantu.content.enums.TestResultVisibility;
import com.vedantu.content.event.details.EndTestDetails;
import com.vedantu.content.models.AbstractContentStatsModel;
import com.vedantu.content.models.Answer;
import com.vedantu.content.models.ContentSearchDetails;
import com.vedantu.content.models.LibraryContentLink;
import com.vedantu.content.models.ModuleSchedules;
import com.vedantu.content.models.Question;
import com.vedantu.content.models.analytics.AcademicDimension;
import com.vedantu.content.models.analytics.AcademicDimensionType;
import com.vedantu.content.models.analytics.EntityAnalytics;
import com.vedantu.content.models.analytics.EntityHighscore;
import com.vedantu.content.models.analytics.EntityMeasures;
import com.vedantu.content.models.analytics.QuestionAnalytics;
import com.vedantu.content.models.analytics.QuestionMeasures;
import com.vedantu.content.models.analytics.UserEntityAnalytics;
import com.vedantu.content.models.analytics.UserEntityAttempt;
import com.vedantu.content.models.analytics.UserQuestionAnalytics;
import com.vedantu.content.models.analytics.UserQuestionAttempt;
import com.vedantu.content.models.tests.AbstractTestCommonModel;
import com.vedantu.content.models.tests.Assignment;
import com.vedantu.content.models.tests.Test;
import com.vedantu.content.pojos.BoardAnalyticsInfo;
import com.vedantu.content.pojos.EntityAnalyticsBasicInfo;
import com.vedantu.content.pojos.SrcType.LinkType;
import com.vedantu.content.pojos.StudentSubjectWiseResult;
import com.vedantu.content.pojos.UserAnalyticsResult;
import com.vedantu.content.pojos.analytics.AnswerGivenCount;
import com.vedantu.content.pojos.analytics.QuestionAnalyticsExtendedInfo;
import com.vedantu.content.pojos.requests.analytics.EndAttemptReq;
import com.vedantu.content.pojos.requests.analytics.GetAttemptedEntitiesReq;
import com.vedantu.content.pojos.requests.analytics.GetEntityMarkDistributionReq;
import com.vedantu.content.pojos.requests.analytics.GetEntityMeasuresReq;
import com.vedantu.content.pojos.requests.analytics.GetEntityMeasuresRes;
import com.vedantu.content.pojos.requests.analytics.GetEntityQuestionsAttemptStatReq;
import com.vedantu.content.pojos.requests.analytics.GetEntityResultAnalyticsReq;
import com.vedantu.content.pojos.requests.analytics.GetEntityScheduleAnalyticsReq;
import com.vedantu.content.pojos.requests.analytics.GetQuestionAnalyticsReq;
import com.vedantu.content.pojos.requests.analytics.GetUserAnalyticsStatsReq;
import com.vedantu.content.pojos.requests.analytics.GetUserEntityAnalyticsBySubjectReq;
import com.vedantu.content.pojos.requests.analytics.GetUserEntityAnalyticsReq;
import com.vedantu.content.pojos.requests.analytics.GetUserEntityAttemptStatusInfoReq;
import com.vedantu.content.pojos.requests.analytics.GetUserEntityMeasuresReq;
import com.vedantu.content.pojos.requests.analytics.GetUserEntityQuestionAttemptStatsReq;
import com.vedantu.content.pojos.requests.analytics.GetUserEntityRankReq;
import com.vedantu.content.pojos.requests.analytics.GetUserEntityResultAnalyticsReq;
import com.vedantu.content.pojos.requests.analytics.GradeTestSubjectiveQuestionReq;
import com.vedantu.content.pojos.requests.analytics.RecordAttemptReq;
import com.vedantu.content.pojos.requests.analytics.ResetQuestionAttemptReq;
import com.vedantu.content.pojos.requests.analytics.StartAttemptReq;
import com.vedantu.content.pojos.requests.analytics.SyncTabletAnalyticsReq;
import com.vedantu.content.pojos.requests.questions.GetSolutionsReq;
import com.vedantu.content.pojos.requests.tests.GetTestInfoReq;
import com.vedantu.content.pojos.responses.analytics.EndAttemptRes;
import com.vedantu.content.pojos.responses.analytics.GetAttemptedEntitiesRes;
import com.vedantu.content.pojos.responses.analytics.GetAttemptedEntityRes;
import com.vedantu.content.pojos.responses.analytics.GetEntityAttemptAnalyticsRes;
import com.vedantu.content.pojos.responses.analytics.GetEntityAttemptsStudentsListRes;
import com.vedantu.content.pojos.responses.analytics.GetEntityMarkDistributionRes;
import com.vedantu.content.pojos.responses.analytics.GetEntityQuestionAttemptInfoListRes;
import com.vedantu.content.pojos.responses.analytics.GetEntityResultAnalyticsRes;
import com.vedantu.content.pojos.responses.analytics.GetEntityScheduleAnalyticsRes;
import com.vedantu.content.pojos.responses.analytics.GetEntityTestStatusRes;
import com.vedantu.content.pojos.responses.analytics.GetQuestionAnalyticsRes;
import com.vedantu.content.pojos.responses.analytics.GetUserAnalyticsStatsRes;
import com.vedantu.content.pojos.responses.analytics.GetUserEntityAnalyticsBySubjectRes;
import com.vedantu.content.pojos.responses.analytics.GetUserEntityAttemptStatusInfoRes;
import com.vedantu.content.pojos.responses.analytics.GetUserEntityMeasuresRes;
import com.vedantu.content.pojos.responses.analytics.GetUserEntityQuestionAttemptInfoListRes;
import com.vedantu.content.pojos.responses.analytics.GetUserEntityQuestionAttemptStatInfoListRes;
import com.vedantu.content.pojos.responses.analytics.GetUserEntityRankRes;
import com.vedantu.content.pojos.responses.analytics.GetUserEntityResultAnalyticsListRes;
import com.vedantu.content.pojos.responses.analytics.GetUserEntityResultAnalyticsRes;
import com.vedantu.content.pojos.responses.analytics.GetUserEntityResultAnalyticsSingleEntityRes;
import com.vedantu.content.pojos.responses.analytics.GradeTestSubjectiveQuestionRes;
import com.vedantu.content.pojos.responses.analytics.IQuestionAnswer;
import com.vedantu.content.pojos.responses.analytics.RecordAttemptRes;
import com.vedantu.content.pojos.responses.analytics.ResetQuestionAttemptRes;
import com.vedantu.content.pojos.responses.analytics.StartAttemptRes;
import com.vedantu.content.pojos.responses.analytics.SyncTabletAnalyticsRes;
import com.vedantu.content.pojos.responses.analytics.UserAnalyticsInfoRes;
import com.vedantu.content.pojos.responses.analytics.UserBoardAnalyticsInfoRes;
import com.vedantu.content.pojos.responses.analytics.answers.BoardWiseQuestionsAttemptInfos;
import com.vedantu.content.pojos.responses.analytics.answers.QuestionAttemptInfo;
import com.vedantu.content.pojos.responses.analytics.answers.QuestionAttemptStatsInfo;
import com.vedantu.content.pojos.responses.analytics.answers.QuestionAttemptStatsInfoDetail;
import com.vedantu.content.pojos.responses.analytics.answers.QuestionListAnswer;
import com.vedantu.content.pojos.responses.analytics.answers.QuestionMatrixAnswer;
import com.vedantu.content.pojos.responses.questions.GetSolutionsRes;
import com.vedantu.content.pojos.responses.tests.GetTestInfoRes;
import com.vedantu.content.pojos.responses.tests.TestBoardWiseQuestions;
import com.vedantu.content.pojos.tests.BoardQus;
import com.vedantu.content.pojos.tests.EntityAnalyticsScheduleInfo;
import com.vedantu.content.pojos.tests.EntityInfo;
import com.vedantu.content.pojos.tests.EntityTopper;
import com.vedantu.content.pojos.tests.MarkDistribution;
import com.vedantu.content.pojos.tests.Marks;
import com.vedantu.content.pojos.tests.QuestionResultStatus;
import com.vedantu.content.pojos.tests.SimplifiedBoardNames;
import com.vedantu.content.pojos.tests.TestDetails;
import com.vedantu.content.pojos.tests.TestMetadata;
import com.vedantu.content.pojos.tests.TestMiniInfo;
import com.vedantu.content.pojos.tests.TestQuestionSet;
import com.vedantu.content.search.details.QuestionSearchIndexDetails;
import com.vedantu.content.utils.AnalyticsUtils;
import com.vedantu.content.utils.EntityQuestionAttemptInfoComparator;
import com.vedantu.content.utils.EntityUserActionUtils;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.MongoManager.SortOrder;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuDBResult;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.organization.daos.OrgMemberDAO;
import com.vedantu.organization.daos.OrganizationDAO;
import com.vedantu.organization.enums.OrgMemberProfile;
import com.vedantu.organization.event.ei.details.OrgAttemptUploadDetails;
import com.vedantu.organization.managers.OrgProgramManager;
import com.vedantu.organization.models.OrgMember;
import com.vedantu.organization.models.Organization;
import com.vedantu.user.pojos.EntityUserActionDAO;
import com.vedantu.user.pojos.UserInfo;

public class AnalyticsManager extends AbstractContentManager {

    private static final ALogger LOGGER               = Logger.of(AnalyticsManager.class);
    private static final long    endTestDefaultBuffer = Play.application()
                                                              .configuration()
                                                              .getLong("end.test.attempt.buffer",
                                                                      (long) (5 * 60 * 1000));

    public static StartAttemptRes startAttempt(StartAttemptReq startAttemptReq,
            boolean incAttemptCount) throws VedantuException {

        return startAttempt(startAttemptReq, incAttemptCount, 0, 0);
    }

    public static StartAttemptRes startAttempt(StartAttemptReq startAttemptReq,
            boolean incAttemptCount, long startTime, long entTime) throws VedantuException {
        //Fix orgId value as PUBLIC from APP side.
        if(!startAttemptReq.userId.equalsIgnoreCase("PUBLIC") && !StringUtils.isEmpty(startAttemptReq.orgId) && startAttemptReq.orgId.equalsIgnoreCase("PUBLIC")){
            startAttemptReq.orgId = OrgMemberDAO.INSTANCE.getByUserId(startAttemptReq.userId).orgId;
        }
        if (startAttemptReq.entityType == EntityType.TEST && !startAttemptReq.callingApp.equalsIgnoreCase("learn-app") && !startAttemptReq.callingApp.equalsIgnoreCase("cmds-app")) {
            if(startAttemptReq.target != null){
                ScheduleInfo schedule = null;
                if (startAttemptReq.target.type == EntityType.MODULE) {
                    LOGGER.error("STA INSIDE MODULE");
                    if(StringUtils.isEmpty(startAttemptReq.sectionId)){
                        throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, "SectionId is missing");
                    }
                    SrcEntity section = new SrcEntity(EntityType.SECTION, startAttemptReq.sectionId);
                    SrcEntity globalEntity = new SrcEntity(EntityType.TEST,
                            startAttemptReq.entityId);
                    //Check whether content link exists for the test.
                    MutableLong moduleHits = new MutableLong();
                    LibraryContentLink moduleLink = LibraryContentLinksDAO.INSTANCE.getLibraryContentLink(startAttemptReq.target,section,UserActionType.ADDED, VedantuRecordState.ACTIVE, moduleHits);
                    if(moduleLink == null){
                        throw new VedantuException(VedantuErrorCode.INVALID_ID,
                                "TargetId/TestId is not valid");
                    }
                    else{
                        MutableLong moduleTestHits = new MutableLong();
                        LibraryContentLink moduleTestLink = LibraryContentLinksDAO.INSTANCE.getLibraryContentLink(globalEntity,startAttemptReq.target,UserActionType.ADDED, VedantuRecordState.ACTIVE, moduleTestHits);
                        if(moduleTestLink == null){
                            throw new VedantuException(VedantuErrorCode.INVALID_ID,
                                    "TargetId/TestId is not valid");
                        }
                    }
                    ModuleSchedules moduleSchedule = ModuleSchedulesDAO.INSTANCE.getGlobalSchedule(section,
                            startAttemptReq.target, globalEntity);
                    if (moduleSchedule != null) {
                        schedule = moduleSchedule.getSchedule();
                    }
            } else {
                    LOGGER.error("STA INSIDE SECTION");
                    MutableLong totalHits = new MutableLong();
                    LibraryContentLink cLink = LibraryContentLinksDAO.INSTANCE
                            .getLibraryContentLink(new SrcEntity(EntityType.TEST,
                                    startAttemptReq.entityId), new SrcEntity(
                                    startAttemptReq.target.type, startAttemptReq.target.id),
                                    UserActionType.ADDED, VedantuRecordState.ACTIVE, totalHits);
                    if (cLink == null) {
                        throw new VedantuException(VedantuErrorCode.INVALID_ID,
                                "TargetId/TestId is not valid");
                    }
                    schedule = cLink.getSchedule();
                }
                LOGGER.error("TIME: testState " + startAttemptReq.testState);
                if (schedule != null) {
                    if (schedule.startTime != null) {
                        LOGGER.error("start TIME: " + schedule.startTime.getTime());
                        if (schedule.startTime.getTime() - System.currentTimeMillis() > 0) {
                            throw new VedantuException(VedantuErrorCode.TEST_IS_NOT_LIVE,
                                    "This test is not live yet");
                        }
                    }
                    if (schedule.endTime != null) {
                        LOGGER.error("end TIME: " + schedule.endTime.getTime());
                        if (StringUtils.isEmpty(startAttemptReq.testState)
                                && System.currentTimeMillis() - schedule.endTime.getTime() > 0) {
                            throw new VedantuException(VedantuErrorCode.TEST_IS_NOT_LIVE,
                                    "This test has expired");
                        }
                    }
                    if (schedule.closeTime != null) {
                        LOGGER.error("close TIME: " + schedule.closeTime.getTime());
                        if (!StringUtils.isEmpty(startAttemptReq.testState)
                                && startAttemptReq.testState.equals("RESUMED")
                                && System.currentTimeMillis() - schedule.closeTime.getTime() > 0) {
                            throw new VedantuException(VedantuErrorCode.TEST_IS_NOT_LIVE,
                                    "This test has expired and cannot be resumed");
                        }
                    }
                }
            }else {
                throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, "SectionId is missing");
            }
        }

        if (!isEntityAttemptAllowed(startAttemptReq)) {
            LOGGER.error("not allowed start attempt of entity: " + startAttemptReq.entityType);
            throw new VedantuException(VedantuErrorCode.UNATTEMPTABLE_ENTITY);
        }

        UserEntityAttempt userEntityAttempt = UserEntityAttemptDAO.INSTANCE.getAttempt(
                startAttemptReq.userId, startAttemptReq.entityType, startAttemptReq.entityId);

        if (null != userEntityAttempt && !isMultiAttemptAllowed(startAttemptReq)) {
            if (userEntityAttempt.endTime == 0 && userEntityAttempt.entity.type == EntityType.TEST) {
                StartAttemptRes startAttemptRes = new StartAttemptRes();
                startAttemptRes.info = userEntityAttempt.toBasicInfo();
                startAttemptRes.startTime = userEntityAttempt.timeCreated;
                startAttemptRes.isReattempt = true;
                startAttemptRes.qIds = userEntityAttempt.qIds;
                userEntityAttempt.testStatus = "ONGOING";
                UserEntityAttemptDAO.INSTANCE.save(userEntityAttempt);
                return startAttemptRes;
            }
            LOGGER.error("disallowing start attempt by userId: " + startAttemptReq.userId
                    + ", entityType: " + startAttemptReq.entityType + ", entityId: "
                    + startAttemptReq.entityId + ", found a previous attempt: " + userEntityAttempt);
            throw new VedantuException(VedantuErrorCode.MULTI_ATTEMPTS_NOT_ALLOWED);
        }

        // Add list of questions and parent (if exist) for this attempt
        List<String> qIds = startAttemptReq.qIds != null ? startAttemptReq.qIds
                : new ArrayList<String>();
        AbstractContentStatsModel entityModel = getAttemptedEntity(new SrcEntity(
                startAttemptReq.entityType, startAttemptReq.entityId));
        AbstractTestCommonModel test = null;
        if (entityModel instanceof AbstractTestCommonModel) {
            test = (AbstractTestCommonModel) entityModel;
            SrcEntity parent = getParentAndUpdateQIds(startAttemptReq, qIds, test);
            userEntityAttempt = addEntityAttempt(startAttemptReq.userId,startAttemptReq.orgId,
                    startAttemptReq.entityType, startAttemptReq.entityId, qIds, parent,
                    incAttemptCount, startTime, entTime, parent == null, test, EventType.INDEX_TEST);
            // Add testStatus and save
            userEntityAttempt.testStatus = "ONGOING";
            userEntityAttempt.timeLeft = test.duration;
            UserEntityAttemptDAO.INSTANCE.save(userEntityAttempt);
            if (parent != null) {
                // mark the parent also as attempt
                // this will be usefull for fetching a user attempted test -->
                // we
                // will only fetch those test which does not has any parent
                UserEntityAttempt parentEntityAttempt = UserEntityAttemptDAO.INSTANCE.getAttempt(
                        startAttemptReq.userId, parent.type, parent.id);
                if (parentEntityAttempt == null) {
                    parentEntityAttempt = addEntityAttempt(startAttemptReq.userId,startAttemptReq.orgId, parent.type,
                            parent.id, null, null, incAttemptCount, startTime, entTime, true, null,
                            EventType.INDEX_TEST);
                }
            }
        } else {
            // in case of challenge
            userEntityAttempt = addEntityAttempt(startAttemptReq.userId,startAttemptReq.orgId,
                    startAttemptReq.entityType, startAttemptReq.entityId, qIds, null,
                    incAttemptCount, startTime, entTime, false, null, null);
        }

        StartAttemptRes startAttemptRes = new StartAttemptRes();
        startAttemptRes.info = userEntityAttempt.toBasicInfo();
        startAttemptRes.qIds = qIds;
        startAttemptRes.startTime = userEntityAttempt.timeCreated;
        startAttemptRes.isReattempt = false;
        return startAttemptRes;
    }

    /**
     *
     * @param userId
     * @param entityType
     * @param entityId
     * @param qIds
     * @param parent
     * @param incAttemptCount
     * @param startTime
     *            --> if >0 than set the start time of the test = startTime
     * @param endTime
     *            --> if >0 than set the end time of the test = endTime
     * @return
     * @throws VedantuException
     */

    private static UserEntityAttempt addEntityAttempt(String userId, String orgId, EntityType entityType,
            String entityId, List<String> qIds, SrcEntity parent, boolean incAttemptCount,
            long startTime, long endTime, boolean updateIndex, AbstractContentStatsModel model,
            EventType eventType) throws VedantuException {

        UserEntityAttempt userEntityAttempt = UserEntityAttemptDAO.INSTANCE.addAttempt(userId,orgId,
                entityType, entityId, qIds, parent);
        LOGGER.info(" entityType : "+entityType);
        if(entityType.equals(EntityType.TEST)){
        	Test test =TestDAO.INSTANCE.getTest(entityId);
        	LOGGER.info("test.isNTAPattern : "+test.isNTAPattern);
        	if(test.isNTAPattern){
            	addEntityAttemptMappingForNTAPattern(userEntityAttempt,test);
        	}
        }
        if (startTime > 0 && endTime > 0) {
            userEntityAttempt.timeCreated = startTime;
            userEntityAttempt.endTime = endTime;
            UserEntityAttemptDAO.INSTANCE.save(userEntityAttempt);
        }
        SrcEntity attemptedEntity = new SrcEntity(entityType, entityId);
        if (incAttemptCount) {
            EntityUserActionUtils.addEntityUserAction(userEntityAttempt.userId, attemptedEntity,
                    UserActionType.ATTEMPTED, false, updateIndex);
        }
        return userEntityAttempt;
    }
    
    private static UserEntityAttempt addEntityAttemptMappingForNTAPattern(UserEntityAttempt userEntityAttempt,Test test){
    	if(test.simplifiedBoardNames!=null){
        	if(test.simplifiedBoardNames.size()>0){
            	addEntityAttemptMappingForNTAPatternForSimplifiedBoards(userEntityAttempt,test);
        	}
    	}
    	else{
        	if(test.isNTAPattern){
        		Map<String, Map<QuestionType,Integer>> mapping=new HashMap<String, Map<QuestionType,Integer>>();
        		for(TestMetadata metadata:test.metadata){
        			LOGGER.info("metadata.id : "+metadata.id);
        			mapping.put(metadata.id, new HashMap<QuestionType, Integer>());
        			LOGGER.info("userEntityAttempt.mapping.containsKey(metadata.id)  : "+mapping.containsKey(metadata.id));
        			for(TestDetails detail:metadata.details){
        				if(detail.qusCount>0 && detail.maxQuestionsTobeAttempted>0){
        					LOGGER.info("detail.type : "+detail.type +"    ,"
        							+ "detail.maxQuestionsTobeAttempted : "+detail.maxQuestionsTobeAttempted);
            				mapping.get(metadata.id).put(detail.type, detail.maxQuestionsTobeAttempted);
        				}
        			}
        		}
        		userEntityAttempt.mapping=mapping;
        	}
        	UserEntityAttemptDAO.INSTANCE.save(userEntityAttempt);

    	}
    	LOGGER.info("inside UserEntityATTEMPT addEntityAttempt method, userEntityAttempt.mapping : "+userEntityAttempt.mapping);
		return userEntityAttempt;
    	
    }
    private static UserEntityAttempt addEntityAttemptMappingForNTAPatternForSimplifiedBoards(UserEntityAttempt userEntityAttempt,Test test){
    	if(test.isNTAPattern){
    		Map<String, Map<QuestionType,Integer>> mapping=new HashMap<String, Map<QuestionType,Integer>>();
    		if(test.simplifiedBoardNames.size()>0){
    			Map<String,Integer> boardIndex = new LinkedHashMap<String,Integer>();
    	        int counter = 0;
    			for (TestMetadata mdata : test.metadata) {
    	            boardIndex.put(mdata.id, counter++);
    	        }
    			List<SimplifiedBoardNames> simplifiedBoardNames=test.simplifiedBoardNames;
    			for(SimplifiedBoardNames s: simplifiedBoardNames){
    				String name=s.simplifiedName;
    				String id  = StringUtils.EMPTY;
    				Map<QuestionType,Integer> detailsMap=new HashMap<QuestionType,Integer>();
    				List<String> brdIds=s.brdIds;
    				for(String dd:brdIds){
    					id += dd+"_";
    					List<TestMetadata>  metadata=test.metadata;
    					for(TestMetadata testMetadata:metadata){
    						if(testMetadata.id.equals(dd)){
    							for(TestDetails tt:testMetadata.details){
    								if(!detailsMap.containsKey(tt.type)){
    									if(tt.maxQuestionsTobeAttempted > 0){
        									detailsMap.put(tt.type, tt.maxQuestionsTobeAttempted);
    									}
    								}
    								else{
    									if(tt.maxQuestionsTobeAttempted > 0){
        									detailsMap.put(tt.type,detailsMap.get(tt.type)+tt.maxQuestionsTobeAttempted);
    									}
    								}
    							}
    						}
    					}
    					boardIndex.remove(dd);
    				} 
					mapping.put(id, detailsMap);
//					LOGGER.info("detailsMap of merged board : "+detailsMap);
//					mapping.put(s, detailsMap);
    			}
    			if(!boardIndex.isEmpty()){
    	            for(String brdId: boardIndex.keySet()) {
    	                TestMetadata mdata = test.metadata.get(boardIndex.get(brdId));
    	                mapping.put(mdata.id, new HashMap<QuestionType, Integer>());
    	    			LOGGER.info("userEntityAttempt.mapping.containsKey(metadata.id)  : "+mapping.containsKey(mdata.id));
    	    			for(TestDetails detail:mdata.details){
    	    				if(detail.qusCount>0 && detail.maxQuestionsTobeAttempted>0){
    	    					LOGGER.info("detail.type : "+detail.type +"    ,"
    	    							+ "detail.maxQuestionsTobeAttempted : "+detail.maxQuestionsTobeAttempted);
    	        				mapping.get(mdata.id).put(detail.type, detail.maxQuestionsTobeAttempted);
    	    				}
    	    			}
    	            }
    	        }
    		}
    		userEntityAttempt.mapping=mapping;
    		UserEntityAttemptDAO.INSTANCE.save(userEntityAttempt);
    		LOGGER.info("mapping of merged board : "+mapping);
    	}
		return userEntityAttempt;
    	
    }

    // this will return the parent of the entity if exist else null
    private static SrcEntity getParentAndUpdateQIds(StartAttemptReq startAttemptReq,
            List<String> qIds, AbstractTestCommonModel test) {

        SrcEntity parent = null;
        if (startAttemptReq.entityType == EntityType.TEST
                && StringUtils.isNotEmpty(startAttemptReq.entityId)) {
            if (StringUtils.isNotEmpty(test.parentId)) {
                parent = new SrcEntity(EntityType.TEST, test.parentId);
            }
            boolean addedQids = false;
            if (StringUtils.isNotEmpty(startAttemptReq.setName)) {
                TestQuestionSet qSet = test.__getQuestionSet(startAttemptReq.setName);
                if (qIds.isEmpty() && qSet != null && CollectionUtils.isNotEmpty(qSet.qIds)) {
                    qIds.addAll(qSet.qIds);
                    addedQids = true;
                }
            }
            if (qIds.isEmpty() && !addedQids) {
                qIds.addAll(test.__getAllQIds());
            }
        }
        LOGGER.info("getParentAndUpdateQIds : qIds : " + qIds + ", parent:" + parent);
        return parent;
    }

    public static RecordAttemptRes recordAttempt(RecordAttemptReq recordAttemptReq)
            throws VedantuException {

        if (!isEntityAttemptAllowed(recordAttemptReq)
                && EntityType.QUESTION != recordAttemptReq.entityType) {
            LOGGER.error("recordAttempt not allowed record attempt of entity: "
                    + recordAttemptReq.entityType);
            throw new VedantuException(VedantuErrorCode.UNATTEMPTABLE_ENTITY);
        }

        if(recordAttemptReq.entityType == EntityType.TEST){
            recordAttemptReq.attemptId = (recordAttemptReq.attemptId.isEmpty() || recordAttemptReq.attemptId == null) ? getAttemptId(
                    recordAttemptReq.userId, recordAttemptReq.entityType, recordAttemptReq.entityId)
                    : recordAttemptReq.attemptId;
            String testStatus = entityStatus(recordAttemptReq.attemptId);
            if(testStatus.equals("FINISHED")){
                LOGGER.error("Entity is Finished");
                throw new VedantuException(VedantuErrorCode.TEST_ENDED);
            }
            if(testStatus.equals("PAUSED")){
                LOGGER.error("Entity is PAUSED");
                throw new VedantuException(VedantuErrorCode.TEST_PAUSED);
            }
            if(testStatus.equals("RESUMED")){
                LOGGER.error("Entity is RESUMED");
                throw new VedantuException(VedantuErrorCode.TEST_PAUSED_RESUME_AGAIN);
            }
        }


        if (EntityType.QUESTION == recordAttemptReq.entityType
                && !StringUtils.equals(recordAttemptReq.entityId, recordAttemptReq.qId)) {
            LOGGER.error("recordAttempt mismatch in id for entityType: "
                    + recordAttemptReq.entityType + ", entityId: " + recordAttemptReq.entityId
                    + ", qId: " + recordAttemptReq.qId);
            throw new VedantuException(VedantuErrorCode.INVALID_QUESTION_ID);
        }

        final SrcEntity parentEntity = new SrcEntity(recordAttemptReq.entityType,
                recordAttemptReq.entityId);

        if (EntityType.QUESTION == recordAttemptReq.entityType) {
            List<UserQuestionAttempt> prevAttempts = UserQuestionAttemptDAO.INSTANCE.getAttempts(
                    recordAttemptReq.userId, parentEntity, recordAttemptReq.qId);
            if (CollectionUtils.isNotEmpty(prevAttempts)) {
                LOGGER.error("recordAttempt found previous attempts for parentEntity: "
                        + parentEntity + ", qId: " + recordAttemptReq.qId + ", numPrevAttempts: "
                        + CollectionUtils.size(prevAttempts));
                throw new VedantuException(VedantuErrorCode.MULTI_ATTEMPTS_NOT_ALLOWED);
            }
        }
        

        // Lets first do recording of attempt of the question
        Question question = QuestionDAO.INSTANCE.getQuestion(recordAttemptReq.qId);
        //Condition check for NTAPattern 
        Test testForNTAPattern=null;
        UserEntityAttempt userEntityAttempt=null;
    	String key="";
    	TestMetadata testMetadata=null;
    	final RecordAttemptRes recordAttemptRes = new RecordAttemptRes();
        if(recordAttemptReq.entityType == EntityType.TEST){
        	testForNTAPattern = TestDAO.INSTANCE.getById(recordAttemptReq.entityId);
        	 if(testForNTAPattern.isNTAPattern){
        		 userEntityAttempt = UserEntityAttemptDAO.INSTANCE.getAttemptUsingTestStatus(
                  		recordAttemptReq.userId, recordAttemptReq.entityType, recordAttemptReq.entityId,
                  		"ONGOING");
        		 LOGGER.info("userEntityAttempt in record attempt : "+userEntityAttempt);
        		 if (userEntityAttempt == null) {
     				throw new VedantuException(VedantuErrorCode.ATTEMPT_NOT_FOUND);
     				}
        		 for(TestMetadata metadata:testForNTAPattern.metadata){
                   	if(metadata.qIds.contains(recordAttemptReq.qId)){
                   		key=metadata.id;
                       	testMetadata=metadata;
                   	}
                  }
        		 if(userEntityAttempt.mapping.get(key)==null){
            		 for(String keys:userEntityAttempt.mapping.keySet()){
            			 String[] list=keys.split("_");
            			 Set<String> mySet = new HashSet<String>(Arrays.asList(list));
            			 if(mySet.contains(key)){
            				 key= keys;
            			 }
            		 }

        		 }
        		 int count=userEntityAttempt.mapping.get(key).get(question.type);
        		 LOGGER.info("count : "+count);
                 if(count==0){
                	 if(userEntityAttempt.attemptedQIds.contains(recordAttemptReq.qId)){
                		 LOGGER.info("attempting same qustion");
                	 }else{
                		 LOGGER.info("attempting different qustion");
                		 recordAttemptRes.quotaExpired=true;
                		 return recordAttemptRes;
                	 }
                 }
         	}
        }
        
        AnswerCorrectness isCorrect = AnswerCorrectness.INCORRECT;
        List<String> correctAnswer = null;
        int score = 0;
        if (question.type.isJudgeable()) {
            LOGGER.debug("recordAttempt : Question is Judgeable");
            Answer answer = AnswerDAO.INSTANCE.getQuestionAnswer(recordAttemptReq.qId);

            if (answer == null) {
                LOGGER.debug("recordAttempt : answer is null");
                if (question.answerId != null && !question.answerId.isEmpty()) {
                    LOGGER.debug("recordAttempt : Getting answer again with help of ID");
                    answer = AnswerDAO.INSTANCE.getById(question.answerId);
                    if (answer == null) {
                        LOGGER.error("user[" + recordAttemptReq.userId + "], question ["
                                + recordAttemptReq.qId + "] does not have a verified answer");
                        throw new VedantuException(VedantuErrorCode.UNATTEMPTABLE_ENTITY);
                    }
                } else {
                    LOGGER.error("user[" + recordAttemptReq.userId + "], question ["
                            + recordAttemptReq.qId + "] does not have a verified answer");
                    throw new VedantuException(VedantuErrorCode.UNATTEMPTABLE_ENTITY);
                }
            }
            correctAnswer = answer.answer;
            LOGGER.info("recordAttempt : question: " + question);
            //if (question.type != QuestionType.MATRIX) {
            LOGGER.debug("recordAttempt : Given question is "+question.type);
            boolean isPartialMarksAllowed = false;
            boolean isOneOrMoreAllowed = true;
            if(recordAttemptReq.entityType == EntityType.TEST){
                Test test = TestDAO.INSTANCE.getById(recordAttemptReq.entityId);
                isPartialMarksAllowed = isPartialMarkingEnabled(test, question.type.name());
                isOneOrMoreAllowed = isPartialMarksAllowed ? true : isOneOrMoreAnswersAllowed(test,question.type.name());
            }
            if(CollectionUtils.isNotEmpty(recordAttemptReq.getAnswerGiven())) {
                LOGGER.debug("recordAttempt : Getting correct answer");
                isCorrect = question.type.isCorrect(Judgement.JUDGE,
                        recordAttemptReq.getAnswerGiven(), answer.answer,
                        Status.COMPLETE, isPartialMarksAllowed,isOneOrMoreAllowed);
                LOGGER.debug("recordAttempt : isCorrect is "+isCorrect.toString());
            }

            if (answer.optionalCorrectAnswers != null && isCorrect == AnswerCorrectness.INCORRECT) {
                LOGGER.debug("recordAttempt : Checking for optional correct answers if answer is incorrect");
                for (Entry<Integer, List<String>> answers : answer.optionalCorrectAnswers
                        .entrySet()) {
                    //CollectionUtils.isNotEmpty(recordAttemptReq.getAnswerGiven()) &&
                    LOGGER.debug("recordAttempt : Found optional correct answers, and iterating inside it");
                    if (CollectionUtils.isNotEmpty(recordAttemptReq.getAnswerGiven())) {
                        isCorrect = question.type.isCorrect(Judgement.JUDGE,
                                        recordAttemptReq.getAnswerGiven(), answers.getValue(),
                                        Status.COMPLETE, isPartialMarksAllowed,isOneOrMoreAllowed);
                    }
                    if (isCorrect == AnswerCorrectness.CORRECT || isCorrect == AnswerCorrectness.PARTIAL) {
                        break;
                    }
                }
            }

//            } else {
//                LOGGER.debug("recordAttempt : Given question is matrix");
//                isCorrect = QuestionType.isEqualMatrix(recordAttemptReq.getMatrixAnswer(),
//                        answer.matrixAnswer);
//            }
        }
        LOGGER.debug("recordAttempt : isCorrect Before Adding Attempt is "+isCorrect.toString());
		if (recordAttemptReq.entityType == EntityType.TEST
				&& question.type == QuestionType.SUBJECTIVE) {
			try {
				LOGGER.info("recordAttempt : Subjective Remove Image Src");
				recordAttemptReq.removeImageSrc(true);
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
		if(EntityType.TEST == recordAttemptReq.entityType && QuestionType.SUBJECTIVE == question.type){
		    List<UserQuestionAttempt> prevAttempts = UserQuestionAttemptDAO.INSTANCE.getAttempts(
                    recordAttemptReq.userId, parentEntity, recordAttemptReq.qId, true);
		    if(CollectionUtils.isNotEmpty(prevAttempts)){
		        for(UserQuestionAttempt prevAttempt: prevAttempts){
		            prevAttempt.isJudgeable = false;
		            UserQuestionAttemptDAO.INSTANCE.save(prevAttempt);
		        }
		    }
		}
        UserQuestionAttempt userQuestionAttempt = UserQuestionAttemptDAO.INSTANCE.addAttempt(
                recordAttemptReq.userId, recordAttemptReq.attemptId, parentEntity,
                recordAttemptReq.qId, recordAttemptReq.getAnswerGiven(),
                recordAttemptReq.getMatrixAnswer(), question.type, question.type.isJudgeable(),
                isCorrect, score, recordAttemptReq.timeTaken);
        LOGGER.debug("recordAttempt : isCorrect After Adding Attempt is "+isCorrect.toString());
        LOGGER.debug("recordAttempt : userQuestionAttempt: " + userQuestionAttempt);
        LOGGER.info("recordAttemptReq.entityType : "+recordAttemptReq.entityType);
        if (EntityType.QUESTION == recordAttemptReq.entityType) {
            LOGGER.debug("recordAttempt : EntityType is QUESTION");
            finalizeQuestionAttempt(userQuestionAttempt, question, recordAttemptReq.orgId);
        } else if (recordAttemptReq.entityType == EntityType.ASSIGNMENT) {
            LOGGER.debug("recordAttempt : EntityType is ASSIGNMENT");
            finalizeAssignmentAttempt(userQuestionAttempt, question, recordAttemptReq.orgId);
        }
        

        //decrementing count value if question type matches for NTAPattern
        if(recordAttemptReq.entityType == EntityType.TEST){
            LOGGER.info("testForNTAPattern.isNTAPattern : "+testForNTAPattern.isNTAPattern);
            if(testForNTAPattern.isNTAPattern){
            	LOGGER.info("key : "+key+"  ,question.type : "+question.type);
            	LOGGER.info("userEntityAttempt.mapping.get(key) : "+userEntityAttempt.mapping.get(key));
            	LOGGER.info("userEntityAttempt : "+userEntityAttempt);
            	if(!(userEntityAttempt.attemptedQIds.contains(recordAttemptReq.qId))){
                	int count=userEntityAttempt.mapping.get(key).get(question.type);
            		LOGGER.info("count : "+count);
                    count=count-1;
                    
                    userEntityAttempt.mapping.get(key).put(question.type,count);
                    
                    LOGGER.info("count after decrementing : "+count);
                    LOGGER.info("userEntityAttempt.mapping.get(key) : "+userEntityAttempt.mapping.get(key));
                    LOGGER.info("question.id.toString() : "+question.id.toString());
                    
                    userEntityAttempt.attemptedQIds.add(question.id.toString());
                    UserEntityAttemptDAO.INSTANCE.save(userEntityAttempt);
                    
                    LOGGER.info("userEntityAttempt.attemptedQIds : "+userEntityAttempt.attemptedQIds);
                    if(count==0){
                    	for(TestDetails details:testMetadata.details){
                    		if(question.type==details.type){
                        		for(String id:details.qIds){
                        			LOGGER.info("inside for loop details.qIds : "+id);
                        			if(!userEntityAttempt.attemptedQIds.contains(id)){
                        				recordAttemptRes.unattemptedQIds.add(id);
                        			}
                        		}
                    		}
                    	}
                    }
                    LOGGER.info("recordAttemptRes.list : "+recordAttemptRes.unattemptedQIds);
            	}
            	else{
            		LOGGER.info("attempting same question");
            	}
            	
            }

        }
        
		if (recordAttemptReq.entityType == EntityType.TEST
				&& question.type == QuestionType.SUBJECTIVE) {
			LOGGER.error("recordAttempt : Subjective Construct answer ");
			recordAttemptRes.userAnswer = QuestionManager
					.constructAnswerText(recordAttemptReq.getAnswerGiven());
		} else {
			recordAttemptRes.userAnswer = recordAttemptReq.getAnswerGiven();
		}
        if (EntityType.TEST != recordAttemptReq.entityType) {
            recordAttemptRes.isJudgeable = question.type.isJudgeable();
            recordAttemptRes.correctAnswer = correctAnswer;
            recordAttemptRes.isUserAnswerCorrect = isCorrect;
        }
        LOGGER.info("recordAttempt : response: " + recordAttemptRes);
        return recordAttemptRes;
    }

    private static boolean isOneOrMoreAnswersAllowed(Test test, String qType) {
        if(CollectionUtils.isNotEmpty(test.oneOrMoreMarksQTypes)){
            if(test.oneOrMoreMarksQTypes.contains(qType)){
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }

    private static boolean isPartialMarkingEnabled(Test test, String qType) {
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

    private static String entityStatus(String attemptId) throws VedantuException {
        // TODO Auto-generated method stub
        UserEntityAttempt user = _entityStatus(attemptId);
        if (user == null){
            throw new VedantuException(VedantuErrorCode.ATTEMPT_NOT_FOUND);
        }
        return user.testStatus;
    }

    private static UserEntityAttempt _entityStatus(String attemptId) throws VedantuException {
        // TODO Auto-generated method stub
        UserEntityAttempt user = UserEntityAttemptDAO.INSTANCE.getById(attemptId);
        return user;
    }

    public static ResetQuestionAttemptRes resetQuestionAttempt(
            ResetQuestionAttemptReq resetAttemptReq) throws VedantuException {

        String testStatus = entityStatus(resetAttemptReq.attemptId);
        if(testStatus.equals("FINISHED")){
            LOGGER.error("Entity is Finished");
            throw new VedantuException(VedantuErrorCode.TEST_ENDED);
        }
        if(testStatus.equals("PAUSED")){
            LOGGER.error("Entity is PAUSED");
            throw new VedantuException(VedantuErrorCode.TEST_PAUSED);
        }
        if(testStatus.equals("RESUMED")){
            LOGGER.error("Entity is RESUMED");
            throw new VedantuException(VedantuErrorCode.TEST_PAUSED_RESUME_AGAIN);
        }

        resetAttemptReq.attemptId = (resetAttemptReq.attemptId.isEmpty() || resetAttemptReq.attemptId == null) ? getAttemptId(
                resetAttemptReq.userId, resetAttemptReq.entityType, resetAttemptReq.entityId)
                : resetAttemptReq.attemptId;

        List<UserQuestionAttempt> attempts = UserQuestionAttemptDAO.INSTANCE.getAttempts(
                resetAttemptReq.userId, resetAttemptReq.attemptId, resetAttemptReq.qId, null);
        if (CollectionUtils.isEmpty(attempts)) {
            LOGGER.equals("no previous attempts found " + resetAttemptReq);
            throw new VedantuException(VedantuErrorCode.ATTEMPT_NOT_FOUND);
        }

        for (UserQuestionAttempt attempt : attempts) {
            attempt.isJudgeable = false;
            UserQuestionAttemptDAO.INSTANCE.save(attempt);
        }
        UserEntityAttempt userEntityAttempt = _entityStatus(resetAttemptReq.attemptId);
        if (userEntityAttempt == null){
            throw new VedantuException(VedantuErrorCode.ATTEMPT_NOT_FOUND);
        }
        LOGGER.info("userEntityAttempt : "+userEntityAttempt.id);
        if(userEntityAttempt.entity.type==EntityType.TEST){
        	Test test1=TestDAO.INSTANCE.getTest(userEntityAttempt.entity.id);
            Question question = QuestionDAO.INSTANCE.getQuestion(resetAttemptReq.qId);
            String key="";
            TestMetadata actualMetadata = null ;
           	if(test1.isNTAPattern){
           		for(TestMetadata metadata:test1.metadata){
           			if(metadata.qIds.contains(resetAttemptReq.qId)){
                       	key=metadata.id;
                       	actualMetadata=metadata;
                       	break;
           			}
           		}
           		int actualMaxCount = 0;
           		for(TestDetails details:actualMetadata.details){
           			if(details.type==question.type){
           				actualMaxCount=details.maxQuestionsTobeAttempted;
           			}
           		}
           		if(userEntityAttempt.mapping.get(key)==null){
           		 for(String keys:userEntityAttempt.mapping.keySet()){
           			 String[] list=keys.split("_");
           			 Set<String> mySet = new HashSet<String>(Arrays.asList(list));
           			 if(mySet.contains(key)){
           				 key= keys;
           			 }
           		 }
           		}
           		int count=userEntityAttempt.mapping.get(key).get(question.type);
           		LOGGER.info("inside reset count : "+count);
           		if(count+1<=actualMaxCount){
           			count++;
               		LOGGER.info("inside reset count after : "+count);
               		userEntityAttempt.mapping.get(key).put(question.type,count);
               		LOGGER.info("inside reset userEntityAttempt.mapping.get(key) : "+userEntityAttempt.mapping.get(key));
               		if(userEntityAttempt.attemptedQIds.contains(resetAttemptReq.qId)){
                   		userEntityAttempt.attemptedQIds.remove(resetAttemptReq.qId);
                   	}
               		UserEntityAttemptDAO.INSTANCE.save(userEntityAttempt);
           		}
           		
           	}
        }
        
       	


        ResetQuestionAttemptRes reSetQuestionAttemptRes = new ResetQuestionAttemptRes(true,
                attempts.size());

        return reSetQuestionAttemptRes;
    }

    public static EndAttemptRes endAttempt(EndAttemptReq endAttemptReq, long endTime)
            throws VedantuException {

        return endAttempt(endAttemptReq, endTime, false);
    }

    public static EndAttemptRes endStudentAttempt(StartAttemptReq endAttemptReq, long endTime)
            throws VedantuException {

        String attemptId = getAttemptId(endAttemptReq.studentUserId, endAttemptReq.entityType, endAttemptReq.entityId);

        EndAttemptReq req = new EndAttemptReq(endAttemptReq.studentUserId, endAttemptReq.studentUserId,
                endAttemptReq.entityId, endAttemptReq.entityType, endAttemptReq.setName, attemptId,
                endAttemptReq.orgId);

        return endAttempt(req, endTime, false);
    }

    private static String getAttemptId(String userId, EntityType entityType, String entityId) {
        // TODO Auto-generated method stub
        String attemptId = UserEntityAttemptDAO.INSTANCE.getAttempt(userId, entityType, entityId)._getStringId();
        return attemptId == null ? "" : attemptId;
    }

    public static EndAttemptRes endAttempt(EndAttemptReq endAttemptReq, long endTime,
            boolean ignoreEndTime) throws VedantuException {

        if (!isEntityAttemptAllowed(endAttemptReq)) {
            LOGGER.error("not allowed end attempt of entity: " + endAttemptReq.entityType);
            throw new VedantuException(VedantuErrorCode.UNATTEMPTABLE_ENTITY);
        }

        String testStatus = entityStatus(endAttemptReq.attemptId);
        if(testStatus.equals("FINISHED")){
            LOGGER.error("Entity is Finished");
            throw new VedantuException(VedantuErrorCode.TEST_ENDED);
        }

        endAttemptReq.attemptId = (endAttemptReq.attemptId.isEmpty() || endAttemptReq.attemptId == null) ? getAttemptId(
                endAttemptReq.userId, endAttemptReq.entityType, endAttemptReq.entityId)
                : endAttemptReq.attemptId;

        // Implement a logic to check whether the given endTime is correct or
        // not and provide with near to correct endTime if it is wrong
        Test test = TestDAO.INSTANCE.getById(endAttemptReq.entityId);
        long duration = test.duration;
        UserEntityAttempt attempt = UserEntityAttemptDAO.INSTANCE.getById(endAttemptReq.attemptId);
        if(endTime == 0 || endTime < attempt.timeCreated){
            endTime = System.currentTimeMillis();
        }
        if((endTime - attempt.timeCreated) > duration){
            endTime = attempt.timeCreated + duration;
        }
//        endTime  = getTestEndTime(endTime, endAttemptReq.entityId, endAttemptReq.attemptId, endAttemptReq.userId, duration);

        EndAttemptRes endAttemptRes = new EndAttemptRes();
        synchronized ((endAttemptReq.entityType + endAttemptReq.entityId + endAttemptReq.userId)) {

            UserEntityAttempt userEntityAttempt = UserEntityAttemptDAO.INSTANCE.endAttempt(
                    endAttemptReq.userId, endAttemptReq.entityType, endAttemptReq.entityId,
                    endAttemptReq.attemptId, endTime, true, ignoreEndTime);
            if (userEntityAttempt.parent != null) {
                UserEntityAttemptDAO.INSTANCE.endAttempt(endAttemptReq.userId,
                        userEntityAttempt.parent.type, userEntityAttempt.parent.id,
                        endAttemptReq.attemptId);
            }
            LOGGER.debug("Test Time ended is: "+ userEntityAttempt.endTime);
            LOGGER.debug("Test Time created is: "+ userEntityAttempt.timeCreated);

            endAttemptRes.info = userEntityAttempt.toBasicInfo();
            if (endAttemptReq.entityType == EntityType.TEST) {
                // generate an endTest event so that, Server wont get parallel requests to process all end tests at once.
                // With this, we shall generate analytics in first come first serve basis
                EndTestDetails endTestDetails = new EndTestDetails(userEntityAttempt._getStringId(),
                        userEntityAttempt.userId, userEntityAttempt.entity.id,
                        userEntityAttempt.entity.type, endAttemptReq.setName,
                        userEntityAttempt.timeCreated, duration, endAttemptReq.orgId, "USER");
                long processTime = userEntityAttempt.timeCreated;
                if(!test.subjectiveTest){
                    generateEventAysc(userEntityAttempt.userId, endTestDetails, EventType.END_TEST,
                        processTime);
                }
            }
        }

        return endAttemptRes;
    }

    public static EndAttemptRes endTest(EndAttemptReq endAttemptReq, long endTime, boolean ignoreEndTime)
            throws VedantuException {
        AbstractContentStatsModel attemptedEntity = getAttemptedEntity(new SrcEntity(
                endAttemptReq.entityType, endAttemptReq.entityId));
        EndAttemptRes endAttemptRes = new EndAttemptRes();

        synchronized ((endAttemptReq.entityType + endAttemptReq.entityId + endAttemptReq.userId)) {
            UserEntityAttempt userEntityAttempt = UserEntityAttemptDAO.INSTANCE
                    .getById(endAttemptReq.attemptId);
            endAttemptRes.info = userEntityAttempt.toBasicInfo();
            long overAllTimeTaken = 0;
            overAllTimeTaken = userEntityAttempt.endTime - userEntityAttempt.timeCreated;
            processEndAttempt(userEntityAttempt, endAttemptReq.orgId);

            // update the overAllTimeTaken to userEntityAnalytics
            DBObject updateEntityUserAnalytics = new BasicDBObject("entity.id",
                    endAttemptReq.entityId);
            updateEntityUserAnalytics.put("entity.type", endAttemptReq.entityType.name());
            updateEntityUserAnalytics.put(ConstantsGlobal.USER_ID, endAttemptReq.userId);
            updateEntityUserAnalytics.put(ConstantsGlobal.ACAD_DIM_DOT_ID,
                    AcademicDimensionType.OVERALL.name());
            updateEntityUserAnalytics.put(ConstantsGlobal.ACAD_DIM_DOT_TYPE,
                    AcademicDimensionType.OVERALL.name());
            LOGGER.debug("Test Time Overall is: " + overAllTimeTaken);
            SrcEntity ent = new SrcEntity();
            ent.type = EntityType.TEST;
            ent.id = endAttemptReq.entityId;
            long timeTaken = UserEntityAnalyticsDAO.INSTANCE.getAnalytics(endAttemptReq.userId, "",
                    ent, AcademicDimensionType.OVERALL, AcademicDimensionType.OVERALL.name()).measures.timeTaken;
            UserEntityAnalyticsDAO.INSTANCE.update(updateEntityUserAnalytics, new BasicDBObject(
                    "$set", new BasicDBObject("measures.timeTaken", overAllTimeTaken)), false,
                    false);
            ent.id = AcademicDimensionType.OVERALL.name();
            UserEntityAnalytics allAnalytics = UserEntityAnalyticsDAO.INSTANCE.getAnalytics(
                    endAttemptReq.userId, "", ent, AcademicDimensionType.OVERALL,
                    AcademicDimensionType.OVERALL.name());
            allAnalytics.measures.timeTaken = (allAnalytics.measures.timeTaken - timeTaken)
                    + overAllTimeTaken;
            UserEntityAnalyticsDAO.INSTANCE.save(allAnalytics);
            userEntityAttempt.processed = true;
            UserEntityAttemptDAO.INSTANCE.save(userEntityAttempt);
            if (attemptedEntity instanceof Test && attemptedEntity.contentSrc != null
                    && attemptedEntity.contentSrc.type == EntityType.ORGANIZATION) {
                // generate upload attempt result to institute server if the
                // institute has provided
                // uploadTestAttempt endPoint
                Organization org = OrganizationDAO.INSTANCE.getById(attemptedEntity.contentSrc.id);
                if (org != null && org.authType == AuthType.EXT_AUTH_ORG && org.endPoint != null
                        && StringUtils.isNotEmpty(org.endPoint.getTestAttemptDataUploadEndpoint())) {
                    OrgAttemptUploadDetails eventDetails = new OrgAttemptUploadDetails();
                    eventDetails.attemptId = userEntityAttempt._getStringId();
                    eventDetails.orgId = org._getStringId();
                    eventDetails.userId = userEntityAttempt.userId;
                    generateEventAysc(userEntityAttempt.userId, eventDetails,
                            EventType.UPLOAD_ATTEMPT_TO_ORG);
                }
            }
        }
        return endAttemptRes;
    }

    /**
     * if a test is attempted in tablet then this method will be used to sync
     * the data from tablet
     */
    public static SyncTabletAnalyticsRes syncTabletAnalytics(SyncTabletAnalyticsReq req)
            throws VedantuException {

        StartAttemptRes startAttemptRes = startAttempt(req, true, req.startTime, req.endTime);
        LOGGER.debug("startAttemptRes :  " + startAttemptRes);

        if (startAttemptRes.isReattempt) {
            String msg = "attempt already is in progress, hence " + req.callingApp
                    + " is not allowed to sync analytics for now, try after some time";
            LOGGER.error(msg);
            throw new VedantuException(VedantuErrorCode.ATTEMPT_IN_PROGRESS, msg);
        }

        req.prepareRecordQuestionAttemptReq(startAttemptRes.info.id);
        LOGGER.debug("submitting question attempt info");

        boolean recordedQuestionAttempt = false;

        SyncTabletAnalyticsRes res = new SyncTabletAnalyticsRes();

        synchronized ((req.entityId + req.entityType + req.userId).intern()) {

            if (req.qusAttemptReqs != null) {
                for (RecordAttemptReq qusAttemptReq : req.qusAttemptReqs) {
                    try {
                        if (CollectionUtils.isNotEmpty(qusAttemptReq.getAnswerGiven())
                                || MapUtils.isNotEmpty(qusAttemptReq.getMatrixAnswer())) {
                            recordAttempt(qusAttemptReq);
                            recordedQuestionAttempt = true;
                        }
                    } catch (VedantuException e) {
                        recordedQuestionAttempt = false;
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            } else {
                // if no question was attempted on tablet
                recordedQuestionAttempt = true;
            }

            if (recordedQuestionAttempt) {
                EndAttemptReq endAttemptReq = new EndAttemptReq(req.callingUserId, req.userId,
                        req.entityId, req.entityType, req.setName, startAttemptRes.info.id,
                        req.orgId);
                endAttemptReq.callingApp = req.callingApp;
                endAttemptReq.callingAppId = req.callingAppId;
                EndAttemptRes endAttemptRes = endAttempt(endAttemptReq, req.endTime, true);
                LOGGER.info("endAttempt res : " + endAttemptRes);
                res.info = endAttemptRes.info;
            }
        }
        res.processed = true;
        return res;

    }

    public static GetAttemptedEntitiesRes getAttemptedEntities(GetAttemptedEntitiesReq req)
            throws VedantuException {

        GetAttemptedEntitiesRes getAttemptedEntitiesRes = new GetAttemptedEntitiesRes();
        String resultForUserId = req._getResultForUserId();
        DBObject query = new BasicDBObject(ConstantsGlobal.USER_ID, resultForUserId);

        if (req.type != null) {
            query.put(ConstantsGlobal.ENTITY_DOT_TYPE, req.type.name());
        }

        if (CollectionUtils.isNotEmpty(req.ids)) {
            query.put(ConstantsGlobal.ENTITY_DOT_ID, new BasicDBObject(MongoManager.IN_QUERY,
                    req.ids.toArray()));
        }

        query.put("finished", true);
        query.put(ConstantsGlobal.END_TIME, new BasicDBObject("$gt", req.attemptedAfter));
        VedantuDBResult<UserEntityAttempt> entityAttempts = UserEntityAttemptDAO.INSTANCE.getInfos(
                query, null, MongoManager.NO_START, MongoManager.NO_LIMIT,
                MongoManager.getSortQuery(ConstantsGlobal.END_TIME, SortOrder.ASC.name()));
        getAttemptedEntitiesRes.totalHits = entityAttempts.totalHits;
        for (UserEntityAttempt entityAttempt : entityAttempts.results) {
            GetAttemptedEntityRes attemptedEntity = new GetAttemptedEntityRes(
                    entityAttempt.entity.type, entityAttempt.entity.id, entityAttempt.endTime);
            getAttemptedEntitiesRes.list.add(attemptedEntity);
        }
        return getAttemptedEntitiesRes;
    }

    public static GetQuestionAnalyticsRes getQuestionAnalytics(
            GetQuestionAnalyticsReq getQuestionAnalyticsReq) throws VedantuException {

        SrcEntity parentEntity = new SrcEntity(getQuestionAnalyticsReq.entityType,
                getQuestionAnalyticsReq.entityId);

        QuestionAnalytics questionAnalytics = QuestionAnalyticsDAO.INSTANCE.getAnalytics(
                getQuestionAnalyticsReq.qId, parentEntity);

        if (null == questionAnalytics) {
            LOGGER.error("questionAnalytics not found for qId: " + getQuestionAnalyticsReq.qId
                    + ", parentEntity: " + parentEntity);
            throw new VedantuException(VedantuErrorCode.ANALYTICS_NOT_FOUND);
        }

        Question question = QuestionDAO.INSTANCE.getQuestion(getQuestionAnalyticsReq.qId);

        if (null == question) {
            LOGGER.error("question not found for qId: " + getQuestionAnalyticsReq.qId
                    + ", parentEntity: " + parentEntity);
            throw new VedantuException(VedantuErrorCode.QUESTION_NOT_FOUND);
        }

        QuestionAnalyticsExtendedInfo quesAnalyticsExtendedInfo = (QuestionAnalyticsExtendedInfo) questionAnalytics
                .toExtendedInfo();
        quesAnalyticsExtendedInfo.type = question.type;

        // set users answer
        UserQuestionAnalytics userQuestionAnalytics = UserQuestionAnalyticsDAO.INSTANCE
                .getAnalytics(getQuestionAnalyticsReq.userId, parentEntity,
                        getQuestionAnalyticsReq.qId);
        if (null != userQuestionAnalytics) {
            quesAnalyticsExtendedInfo.userAnswerGivenCount.answerGiven = userQuestionAnalytics.answerGiven;
            quesAnalyticsExtendedInfo.isUserAnswerCorrect = userQuestionAnalytics.isCorrect;
        }

        if (question.type.isJudgeable()) {
            // set correct answer count
            Answer answer = AnswerDAO.INSTANCE.getQuestionAnswer(getQuestionAnalyticsReq.qId);
            if (null != answer && CollectionUtils.isNotEmpty(answer.answer)) {
                quesAnalyticsExtendedInfo.correctAnswerGivenCount.answerGiven = answer.answer;

                quesAnalyticsExtendedInfo.correctAnswerGivenCount.count = questionAnalytics
                        .findAnswerCount(question, answer.answer, answer.matrixAnswer);
            }

            // set user answer count
            if (null != userQuestionAnalytics) {
                quesAnalyticsExtendedInfo.userAnswerGivenCount.count = questionAnalytics
                        .findAnswerCount(question, userQuestionAnalytics.answerGiven,
                                userQuestionAnalytics.matrixAnswerGiven);
            }
        }

        GetQuestionAnalyticsRes getQuestionAnalyticsRes = new GetQuestionAnalyticsRes();
        getQuestionAnalyticsRes.info = quesAnalyticsExtendedInfo;

        return getQuestionAnalyticsRes;
    }

    public static GetUserEntityAttemptStatusInfoRes getUserEntityAttemptStatusInfo(
            GetUserEntityAttemptStatusInfoReq req) throws VedantuException {

        UserEntityAttempt userEntityAttempt = UserEntityAttemptDAO.INSTANCE.getAttempt(
                req._getResultForUserId(), req.entity.type, req.entity.id);
        GetUserEntityAttemptStatusInfoRes res = new GetUserEntityAttemptStatusInfoRes();
        if (userEntityAttempt != null) {
            res.attempted = true;
            res.completed = userEntityAttempt.endTime > 0;
            res.startTime = userEntityAttempt.timeCreated;
            res.endTime = userEntityAttempt.endTime;

            // below code is being added for allowing UI for showing POST
            // TEST PAGE in case of off-line test

//            if (req.entity.type == EntityType.TEST) {
//                AbstractTestCommonModel test = (AbstractTestCommonModel) getAttemptedEntity(req.entity);
//                res.type = test.type;
//                res.mode = test.mode;
//            }
        }

        return res;
    }

    /**
     * for now this is only limited to test analytics, later on it can be
     * modified to entity
     *
     * @param req
     * @return entity leader board and user detail score analytics
     * @throws VedantuException
     * @NOTE: this api will be used both for fetching leaderBoard and full
     *        result sheet
     */
    public static GetEntityResultAnalyticsRes getEntityResultAnalytics(
            GetEntityResultAnalyticsReq req, boolean addUsersBoardWiseAnalytics)
            throws VedantuException {

        GetEntityResultAnalyticsRes res = new GetEntityResultAnalyticsRes();

        Test test = TestDAO.INSTANCE.getById(req.entity.id);
        if (test == null) {
            LOGGER.error("no entity found entity: " + req.entity);
            throw new VedantuException(VedantuErrorCode.ENTITY_NOT_FOUND);
        }

        Set<String> entityIds = new HashSet<String>();
        res.info = toMiniInfo(test, entityIds);
        EntityAnalytics entityAnalytics = EntityAnalyticsDAO.INSTANCE.getEntityAnalytics(
                req.entity, AcademicDimensionType.OVERALL, AcademicDimensionType.OVERALL.name());
        if (entityAnalytics == null || entityAnalytics.measures == null) {
            return res;
        }

        final String acadDimId = AcademicDimensionType.OVERALL.name();

        Set<String> userIds = new HashSet<String>();

        DBObject analyticQuery = new BasicDBObject(ConstantsGlobal.ENTITY + "."
                + ConstantsGlobal.ID, new BasicDBObject(MongoManager.IN_QUERY,
                new String[] { req.entity.id }));
        analyticQuery.put(ConstantsGlobal.ACAD_DIM_DOT_ID, acadDimId);
        LOGGER.debug("OrgId Before adding into analyticQuery "+req.orgId);
        analyticQuery.put(ConstantsGlobal.ORG_ID, req.orgId);

        int rank = 0;
        long prevTimeTaken = 0;
        double prevScore = -Integer.MAX_VALUE;

        DBObject sortQuery = MongoManager.getSortQuery("measures.score", SortOrder.DESC.name());
        sortQuery.putAll(MongoManager.getSortQuery("measures.timeTaken", SortOrder.ASC.name()));

        if (req.start > 0) {
            // than find out the last user displayed in the result list and
            // her corresponding rank

            VedantuDBResult<UserEntityAnalytics> userEntityAnalytics = UserEntityAnalyticsDAO.INSTANCE
                    .getInfos(analyticQuery, null, req.start - 1, 1, sortQuery);
            if (!userEntityAnalytics.results.isEmpty()) {
                UserEntityAnalytics lastUserAnalytics = userEntityAnalytics.results.get(0);
                prevScore = lastUserAnalytics.measures.score;
                prevTimeTaken = lastUserAnalytics.measures.timeTaken;
                LOGGER.debug("OrgId Before GetRank "+req.orgId);
                rank = UserEntityAnalyticsDAO.INSTANCE.getRank(req.orgId,req.entity.id, prevScore,
                        prevTimeTaken, AcademicDimensionType.OVERALL.name());
            }
        }

        // this will only get the overall acadDimId results
        VedantuDBResult<UserEntityAnalytics> userEntityAnalytics = UserEntityAnalyticsDAO.INSTANCE
                .getInfos(analyticQuery, null, req.start, req.size, sortQuery);
        res.totalHits = userEntityAnalytics.totalHits;
        for (UserEntityAnalytics uA : userEntityAnalytics.results) {

            if (req.isDetailedResultSheet) {
                LOGGER.debug("***********        " + uA.measures.score + "         *************");
                if (uA.measures.score <= req.maxScore && uA.measures.score >= req.minScore) {
                    userIds.add(uA.userId);
                }
            } else {
                userIds.add(uA.userId);
            }
        }

        Map<String, UserEntityAnalytics> userEntityAnalyticsMap = getUserEntityAnalyticsMap(
                entityIds, userIds, addUsersBoardWiseAnalytics);
        LOGGER.debug("collecting data for users : " + userIds);

        Map<String, UserEntityAttempt> userEntityAttemptInfoMap = getUserEntityAttemptInfoMap(
                entityIds, userIds);
        LOGGER.debug("OrgId Before adding into getUserInfoMap "+req.orgId);
        Map<String, ModelBasicInfo> userIdToBasicInfoMap = getUserInfoMap(StringUtils.isEmpty(req.orgId) ? null : req.orgId, userIds);
        int rankFlag = 0;
        for (UserEntityAnalytics uA : userEntityAnalytics.results) {
            // if(req.isDetailedResultSheet){
            // LOGGER.debug("***********        "+uA.measures.score+"         *************");
            // if(uA.measures.score <= req.maxScore && uA.measures.score >=
            // req.minScore){
            // if (uA.measures.score != prevScore || uA.measures.timeTaken !=
            // prevTimeTaken) {
            // rank++;
            // prevScore = uA.measures.score;
            // prevTimeTaken = uA.measures.timeTaken;
            // }
            // GetUserEntityResultAnalyticsRes analytics = new
            // GetUserEntityResultAnalyticsRes();
            // analytics.rank = rank;
            // analytics.id = res.info.id;
            // analytics.user = (UserInfo) userIdToBasicInfoMap.get(uA.userId);
            // analytics.info = __getUserAnalyticsInfo(uA.userId,
            // req.entity.type, res.info,
            // userEntityAnalyticsMap, userEntityAttemptInfoMap, null, true,
            // true, null);
            // res.list.add(analytics);
            // }
            // }else{
            if (uA.measures.score != prevScore || uA.measures.timeTaken != prevTimeTaken) {
                rank++;
                prevScore = uA.measures.score;
                prevTimeTaken = uA.measures.timeTaken;
            }
            if (req.isDetailedResultSheet) {
                if (uA.measures.score <= req.maxScore && uA.measures.score >= req.minScore) {
                    GetUserEntityResultAnalyticsRes analytics = new GetUserEntityResultAnalyticsRes();
                    analytics.rank = UserEntityAnalyticsDAO.INSTANCE.getRank(req.orgId, req.entity.id, uA.measures.score,
                            uA.measures.timeTaken, AcademicDimensionType.OVERALL.name());
                    analytics.id = res.info.id;
                    analytics.user = (UserInfo) userIdToBasicInfoMap.get(uA.userId);
                    analytics.info = __getUserAnalyticsInfo(uA.userId, req.entity.type, res.info,
                            userEntityAnalyticsMap, userEntityAttemptInfoMap, null, true, true,
                            null);
                    res.list.add(analytics);
                }
            } else {
                GetUserEntityResultAnalyticsRes analytics = new GetUserEntityResultAnalyticsRes();
                analytics.rank = UserEntityAnalyticsDAO.INSTANCE.getRank(req.orgId, req.entity.id, uA.measures.score,
                        uA.measures.timeTaken, AcademicDimensionType.OVERALL.name());
                analytics.id = res.info.id;
                analytics.user = (UserInfo) userIdToBasicInfoMap.get(uA.userId);
                analytics.info = __getUserAnalyticsInfo(uA.userId, req.entity.type, res.info,
                        userEntityAnalyticsMap, userEntityAttemptInfoMap, null, true, true, null);
                res.list.add(analytics);
            }

            // }
        }
        return res;
    }



    public static GetEntityAttemptAnalyticsRes getEntityAttemtAnalytics(
            GetEntityResultAnalyticsReq req) throws VedantuException {
        // TODO Auto-generated method stub
        GetEntityAttemptAnalyticsRes res = new GetEntityAttemptAnalyticsRes();
        List<GetEntityAttemptsStudentsListRes> listres = new ArrayList<GetEntityAttemptsStudentsListRes>();
        MutableLong totalHits = new MutableLong();
        Test test = TestDAO.INSTANCE.getById(req.entity.id);
        if (test == null) {
            LOGGER.error("no entity found entity: " + req.entity);
            throw new VedantuException(VedantuErrorCode.ENTITY_NOT_FOUND);
        }
        List<String> studentUserIds = new ArrayList<String>();
        if(StringUtils.isNotEmpty(req.queryText)){
            List<OrgMember> orgMembers = OrgMemberDAO.INSTANCE.getUserIdsFromMemberIdAndNameMatch(req.orgId,req.queryText);
            for(OrgMember orgmember:orgMembers){
                studentUserIds.add(orgmember.userId);
            }
        }
        res.testProgressCount = UserEntityAttemptDAO.INSTANCE.getUserTestAttemptStatusCount(req.orgId,req.entity.id,req.entity.type,"ONGOING");
        res.testCompletedCount = UserEntityAttemptDAO.INSTANCE.getUserTestAttemptStatusCount(req.orgId,req.entity.id,req.entity.type,"FINISHED");
        res.testPausedCount = UserEntityAttemptDAO.INSTANCE.getUserTestAttemptStatusCount(req.orgId,req.entity.id,req.entity.type,"PAUSED");
        res.testResumedCount = UserEntityAttemptDAO.INSTANCE.getUserTestAttemptStatusCount(req.orgId,req.entity.id,req.entity.type,"RESUMED");
        if(!StringUtils.isEmpty(req.queryText)){
            if(studentUserIds.isEmpty()){
                res.list = listres;
                return res;
            }
        }
        List<UserEntityAttempt> entityUserAttempts = UserEntityAttemptDAO.INSTANCE.getUserAttemptsList(
                req.entity.type, req.entity.id, req.orgId, req.start , req.size , totalHits , studentUserIds);
        if (entityUserAttempts == null) {
            LOGGER.error("no attempts found entity: " + req.entity);
            throw new VedantuException(VedantuErrorCode.ATTEMPT_NOT_FOUND);
        }

        Set<String> userIds = new HashSet<String>();
        for(UserEntityAttempt user : entityUserAttempts){
            userIds.add(user.userId);
        }

        Map<String, ModelBasicInfo> userIdToBasicInfoMap = getUserInfoMap(req.orgId, userIds);

        for(UserEntityAttempt user : entityUserAttempts){
            GetEntityAttemptsStudentsListRes resp = new GetEntityAttemptsStudentsListRes();
            if(!user.userId.equals("PUBLIC")){
                resp.memberId = OrgMemberDAO.INSTANCE.getByUserId(user.userId).memberId;
                resp.user = (UserInfo) userIdToBasicInfoMap.get(user.userId);
                resp.testStatus = user.testStatus;
                resp.startTime = user.timeCreated;
                resp.processed = user.processed;
                listres.add(resp);
            }
        }
        LOGGER.info("listres : "+listres);
        res.list = listres;
        res.totalhits = totalHits.longValue();
        LOGGER.info("AnalyticsManager getEntityAttemtAnalytics  : "+res);
        return res;
    }

    public static GetUserEntityResultAnalyticsListRes getUserEntityResultAnalytics(
            GetUserEntityResultAnalyticsReq req) throws VedantuException {

        String userId = req.__getResultForUserId();
        DBObject query = new BasicDBObject(ConstantsGlobal.ENTITY_DOT_TYPE, req.entityType.name());
        query.put(ConstantsGlobal.USER_ID, userId);
        query.put(ConstantsGlobal.PARENT, null);
        // fetch all the entities{entityType} attempted by this user
        VedantuDBResult<UserEntityAttempt> results = UserEntityAttemptDAO.INSTANCE.getInfos(query,
                MongoManager.getFieldsDBObject(Arrays.asList(ConstantsGlobal.QIDS),
                        MongoManager.EXCLUDE_FIELD), req.start, req.size, MongoManager
                        .getSortQuery(ConstantsGlobal.TIME_CREATED, SortOrder.DESC.name()));
        Set<String> entityIds = new HashSet<String>();
        for (UserEntityAttempt uA : results.results) {
            entityIds.add(uA.entity.id);
        }

        DBObject entityQuery = new BasicDBObject(ConstantsGlobal._ID, new BasicDBObject(
                MongoManager.IN_QUERY, ObjectIdUtils.toObjectIds(new ArrayList<String>(entityIds),
                        true).toArray()));

        // TODO: enable this for assignment too
        VedantuDBResult<Test> tests = TestDAO.INSTANCE.getInfos(entityQuery, null,
                MongoManager.NO_START, MongoManager.NO_LIMIT, null);
        Map<String, TestMiniInfo> testMiniInfoMap = new HashMap<String, TestMiniInfo>();
        for (Test t : tests.results) {
            TestMiniInfo miniInfo = toMiniInfo(t, entityIds);
            testMiniInfoMap.put(t._getStringId(), miniInfo);
        }

        Map<String, UserEntityAnalytics> userEntityAnalyticsMap = getUserEntityAnalyticsMap(
                entityIds, Arrays.asList(userId), true);
        Map<String, EntityAnalytics> entityAnalyticsMap = getEntityAnalyticsMap(entityIds);
        Map<String, Integer> entityLastRankMap = getEntityLastRankMap(entityIds);

        GetUserEntityResultAnalyticsListRes res = new GetUserEntityResultAnalyticsListRes();
        res.totalHits = results.totalHits;
        for (UserEntityAttempt uA : results.results) {
            GetUserEntityResultAnalyticsRes analytics = new GetUserEntityResultAnalyticsRes();
            String key = __getUserEntityAnalyticsMapKey(uA.entity.id, uA.userId,
                    AcademicDimensionType.OVERALL.name());
            UserEntityAnalytics userEntityAnalytics = userEntityAnalyticsMap.get(key);
            TestMiniInfo tMiniInfo = testMiniInfoMap.get(uA.entity.id);
            if (userEntityAnalytics == null || tMiniInfo == null) {
                continue;
            }

            if (tMiniInfo.resultVisibility != TestResultVisibility.HIDDEN) {
                analytics.rank = UserEntityAnalyticsDAO.INSTANCE.getRank(req.orgId, uA.entity.id,
                        userEntityAnalytics.measures.score, userEntityAnalytics.measures.timeTaken,
                        userEntityAnalytics.acadDim.id);
            }
            analytics.id = uA.entity.id;
            analytics.resultVisibility = tMiniInfo.resultVisibility;
            analytics.info = __getUserAnalyticsInfo(uA.userId, uA.entity.type, tMiniInfo,
                    userEntityAnalyticsMap, null, entityAnalyticsMap, true, true, entityLastRankMap);
            analytics.info.endTime = uA.endTime;
            analytics.info.startTime = uA.timeCreated;
            res.list.add(analytics);
        }
        return res;
    }

    public static GetUserEntityAnalyticsBySubjectRes getUserEntityAnalyticsBySubject(
            GetUserEntityAnalyticsBySubjectReq request) {
        GetUserEntityAnalyticsBySubjectRes response = new GetUserEntityAnalyticsBySubjectRes();
        List<StudentSubjectWiseResult> results = new ArrayList<StudentSubjectWiseResult>();
        Test test = TestDAO.INSTANCE.getById(request.test.id);
        response.totalMarks = test.totalMarks;
        response.testName = test.name;
        Organization org = OrganizationDAO.INSTANCE.getById(request.orgId);
        response.orgName = org.fullName;
        for (TestMetadata subjectData : test.metadata) {
            response.subjectMaxMarksMap.put(subjectData.name, subjectData.totalMarks);
            List<UserEntityAnalytics> userEntityAnalytics = UserEntityAnalyticsDAO.INSTANCE
                    .getAllAnalytics(request.test.id, AcademicDimensionType.COURSE, subjectData.id,
                            request.orgId);
            for (UserEntityAnalytics userEntityAnalytic : userEntityAnalytics) {
                OrgMember orgMember = OrgMemberDAO.INSTANCE.getByUserId(userEntityAnalytic.userId);
                LOGGER.debug("userEntityAnalytic Id is "+userEntityAnalytic.userId);
                if(orgMember == null){
                    continue;
                }
                StudentSubjectWiseResult results1 = new StudentSubjectWiseResult(
                        orgMember.getFullName(), orgMember.memberId);
                UserAnalyticsResult marks = new UserAnalyticsResult(subjectData.name,
                        userEntityAnalytic.measures.score);
                if (results.contains(results1)) {
                    int index = results.indexOf(results1);
                    results.get(index).results.add(marks);
                } else {
                    results1.results.add(marks);
                    UserEntityAttempt attempt = UserEntityAttemptDAO.INSTANCE.getAttempt(userEntityAnalytic.userId, EntityType.TEST, request.test.id);
                    results1.phoneNumber = OrgMemberDAO.INSTANCE.getByUserId(userEntityAnalytic.userId).contactNumber;
                    results1.endTime = attempt.endTime;
                    results1.startTime = attempt.timeCreated;
                    results.add(results1);
                }
            }
        }
        long marksSumOfAllStudents = 0;
        for (StudentSubjectWiseResult studentSubject : results) {
            UserAnalyticsResult overallResult = new UserAnalyticsResult("OVERALL", 0);
            int marks = 0;
            for (UserAnalyticsResult singleSubject : studentSubject.results) {
                marks += singleSubject.subjectMarks;
            }
            overallResult.subjectMarks = marks;
            marksSumOfAllStudents += marks;
            studentSubject.results.add(overallResult);
        }
        int length = test.metadata.size();
        for (int i = 0; i <= length; i++) {
            try{
                Collections.sort(results, new StudentSubjectWiseResult.SubjectComparator(i));
            }catch(IndexOutOfBoundsException e){
                LOGGER.debug("Exception came "+e);
            }
            int index = 1, rank = 1;
            double currMarks = Integer.MIN_VALUE;
            for (StudentSubjectWiseResult studentSubject : results) {
                LOGGER.debug("Student name "+studentSubject.userName);
                try{
                    double marks = studentSubject.results.get(i).subjectMarks;
                    if (currMarks != marks) {
                        currMarks = marks;
                        rank = index;
                    }
                    studentSubject.results.get(i).rankOfStudent = rank;
                    if (index == results.size() - 1) {
                        studentSubject.results.get(i).rankOfStudent = index;
                    }
                    index++;
                }catch(IndexOutOfBoundsException e){
                    LOGGER.debug("Internal Exception came "+e);
                }
            }
        }
        StudentSubjectWiseResult topper = results.get(0);
        response.highestMarks = topper.results.get(length).subjectMarks;
        Double averageMarks = Double.valueOf(new DecimalFormat("#.##")
                .format((double) marksSumOfAllStudents / (double) results.size()));
        response.averageMarks = averageMarks;
        response.results = results;
        return response;
    }

    public static GetUserEntityResultAnalyticsSingleEntityRes getUserEntityAnalytics(
            GetUserEntityAnalyticsReq req) throws VedantuException {

        AbstractTestCommonModel test = (AbstractTestCommonModel) getAttemptedEntity(req.entity);

        GetUserEntityResultAnalyticsSingleEntityRes res = new GetUserEntityResultAnalyticsSingleEntityRes();
        final String acadDimId = AcademicDimensionType.OVERALL.name();

        // res.totalAttempts == how many users has attempted this test
        res.totalAttempts = UserEntityAnalyticsDAO.INSTANCE
                .getAnalyticsCount(req.orgId, req.entity, acadDimId);
        res.AIAttempts = UserEntityAnalyticsDAO.INSTANCE.getAnalyticsCount(req.entity, acadDimId);
        final String userId = req._getResultForUserId();

        Set<String> entityIds = new HashSet<String>();
        TestMiniInfo testInfo = toMiniInfo(test, entityIds);
        res.id = req.entity.id;
        res.user = getUserInfo(req.orgId, userId);
        Map<String, UserEntityAnalytics> userEntityAnalyticsMap = getUserEntityAnalyticsMap(
                entityIds, Arrays.asList(userId), true);
        Map<String, UserEntityAttempt> userEntityAttemptInfoMap = getUserEntityAttemptInfoMap(
                entityIds, Arrays.asList(userId));
        UserEntityAnalytics userAnalytics = userEntityAnalyticsMap
                .get(__getUserEntityAnalyticsMapKey(req.entity.id, userId, acadDimId));
        if (userAnalytics != null) {
            if(test.showAIR){
                res.showAIR = true;
                res.AIR = UserEntityAnalyticsDAO.INSTANCE.getRank(req.entity.id,
                        userAnalytics.measures.score, userAnalytics.measures.timeTaken, acadDimId);
            }
            res.rank = UserEntityAnalyticsDAO.INSTANCE.getRank(req.orgId, req.entity.id,
                    userAnalytics.measures.score, userAnalytics.measures.timeTaken, acadDimId);
            res.info = __getUserAnalyticsInfo(userId, req.entity.type, testInfo,
                    userEntityAnalyticsMap, userEntityAttemptInfoMap, null, true, true, null);
        }
        return res;
    }

    public static GetUserEntityQuestionAttemptInfoListRes getUserEntityQuestionAttemptInfos(
            GetUserEntityQuestionAttemptStatsReq req) throws VedantuException {

        String userId = req._getResultForUserId();
        AbstractTestCommonModel test = (AbstractTestCommonModel) getAttemptedEntity(req.entity);

        UserEntityAttempt userEntityAttempts = UserEntityAttemptDAO.INSTANCE.getAttempt(userId,
                req.entity.type, req.entity.id);
        if (userEntityAttempts == null && test instanceof Test) {
            LOGGER.error("user: " + userId + " has not attempted entity: " + req.entity);
            throw new VedantuException(VedantuErrorCode.NOT_ATTEMPTED);
        }

        List<String> qIds = userEntityAttempts != null ? userEntityAttempts.qIds : null;
        if (qIds == null) {
            qIds = test.__getAllQIds();
        }
        LOGGER.debug("qIds : " + qIds + ", test: " + test);
        // TODO: check if the endAttempt is not done --> should we show the
        // analytics or send an erroCode=ATTEMPT_NOT_COMPLETED

        Map<String, UserQuestionAnalytics> questionAnalyticsMap = getQuestionAnalyticsMap(
                req.entity, userId);
        Map<String, Answer> answerMap = AnswerDAO.INSTANCE.getQuestionAnswerMap(qIds);
        Map<String, QuestionSearchIndexDetails> questionInfoMap = QuestionManager
                .getQuestionsMap(qIds);

        GetUserEntityQuestionAttemptInfoListRes questionList = new GetUserEntityQuestionAttemptInfoListRes();
        for (TestMetadata mdata : test.metadata) {
            List<String> mDataQIdsOrder = new ArrayList<String>();
            for (String qId : qIds) {
                if (mdata.qIds.contains(qId)) {
                    mDataQIdsOrder.add(qId);
                }
            }
            qIds.removeAll(mDataQIdsOrder);
            BoardWiseQuestionsAttemptInfos boardQuestion = new BoardWiseQuestionsAttemptInfos(
                    mdata.name, mdata.id);

            for (String qId : mDataQIdsOrder) {
                QuestionSearchIndexDetails detail = questionInfoMap.get(qId);
                Answer answer = answerMap.get(qId);
                UserQuestionAnalytics questionAnalytics = questionAnalyticsMap.get(qId);
                if (detail == null) {
                    continue;
                }

                IQuestionAnswer qAnswer = getUserQuestionAnswerGiven(detail, questionAnalytics,
                        answer);

                AttemptStatus attemptStatus = questionAnalytics != null ? AttemptStatus.ATTEMPTED
                        : AttemptStatus.LEFT;
                QuestionAttemptInfo qAttemptInfo = new QuestionAttemptInfo(detail, qAnswer,
                        questionAnalytics == null ? AnswerCorrectness.INCORRECT : questionAnalytics.isCorrect,
                        attemptStatus);
                boardQuestion.addQuestionAttemptInfo(qAttemptInfo);
            }
            questionList.addBoardWiseQuestions(boardQuestion);
        }
        return questionList;
    }

    public static IQuestionAnswer getUserQuestionAnswerGiven(QuestionSearchIndexDetails detail,
            UserQuestionAnalytics userQuestionAnalytics, Answer answer) {
		if (detail != null && detail.type == QuestionType.SUBJECTIVE
				&& userQuestionAnalytics != null) {
			userQuestionAnalytics.answerGiven = QuestionManager
					.constructAnswerText(userQuestionAnalytics.answerGiven);
		}
        IQuestionAnswer qAnswer = new QuestionListAnswer(userQuestionAnalytics == null ? null
                : userQuestionAnalytics.answerGiven, answer == null ? null : answer.answer,
                userQuestionAnalytics == null ? AnswerCorrectness.INCORRECT
                        : userQuestionAnalytics.isCorrect, userQuestionAnalytics == null ? 0
                        : userQuestionAnalytics.timeTaken);

//        IQuestionAnswer qAnswer = detail.type == QuestionType.MATRIX ? new QuestionMatrixAnswer(
//                userQuestionAnalytics == null ? null : userQuestionAnalytics.matrixAnswerGiven,
//                answer == null ? null : answer.matrixAnswer, userQuestionAnalytics == null ? AnswerCorrectness.INCORRECT
//                        : userQuestionAnalytics.isCorrect,userQuestionAnalytics == null ? 0 : userQuestionAnalytics.timeTaken) : new QuestionListAnswer(
//                userQuestionAnalytics == null ? null : userQuestionAnalytics.answerGiven,
//                answer == null ? null : answer.answer, userQuestionAnalytics == null ? AnswerCorrectness.INCORRECT
//                        : userQuestionAnalytics.isCorrect,userQuestionAnalytics == null ? 0 : userQuestionAnalytics.timeTaken);

        return qAnswer;
    }

    public static GetEntityQuestionAttemptInfoListRes getEntityQusAttemptInfoDetails(
            GetEntityQuestionsAttemptStatReq req) throws VedantuException {

        LOGGER.info("getEntityQusAttemptInfos request: " + req);
        AbstractTestCommonModel test = (AbstractTestCommonModel) getAttemptedEntity(req.entity);
        //Get qIds marks in test
        Map<String,Marks> qIdstoMarks = test.__getMarksMap();
        LOGGER.info("MAP qIdsToMarks"+qIdstoMarks.toString());
        LOGGER.info("getEntityQusAttemptInfos got response from getAttemptedEntity");
        TestQuestionSet qusSet = test.__getQuestionSet(req.setName);
        LOGGER.info("getEntityQusAttemptInfos got response from __getQuestionSet");
        // this will define the question no for the specific set
        List<String> qIds = qusSet == null ? test.__getAllQIds() : qusSet.qIds;
        LOGGER.info("getEntityQusAttemptInfos got response from __getAllQIds");
        // create a question order map with qId to qusIndex+1 no.. it's a
        // replacement for qIds.indexOf(Object) for fast iteration
        Map<String, Integer> qIdOrderNoMap = new HashMap<String, Integer>();
        LOGGER.info("getEntityQusAttemptInfos before qIdOrderNoMap");
        for (int i = 0; i < qIds.size(); i++) {
            qIdOrderNoMap.put(qIds.get(i), Integer.valueOf(i + 1));
        }
        LOGGER.info("getEntityQusAttemptInfos after qIdOrderNoMap");
        List<String> resultQueryQids = getQIdsSubList(req.brdId, test, qIds);
        LOGGER.info("getEntityQusAttemptInfos got response from  getQIdsSubList");
        final boolean defaultOrder = StringUtils.isEmpty(req.orderBy);
        long totalHits = 0;
        if (defaultOrder) {
            LOGGER.info("getEntityQusAttemptInfos inside defaultOrder");
            totalHits = resultQueryQids.size();
            req.start = Math.min(Math.max(0, req.start), resultQueryQids.size());
            req.size = Math.min(Math.max(0, req.size), (resultQueryQids.size() - req.start));
            if (req.size == 0) {
                req.size = resultQueryQids.size() - req.start;
            }
            LOGGER.info("getEntityQusAttemptInfos default order start: " + req.start + ", size:" + req.size);
            resultQueryQids = resultQueryQids.subList(req.start, req.start + req.size);
            LOGGER.info("getEntityQusAttemptInfos inside defaultOrder after subList");
            req.start = 0; // set it to zero so that MONGO will not skip the
                           // results as we have already skiped
        }
        LOGGER.debug("getEntityQusAttemptInfos qIds : " + resultQueryQids);
        DBObject query = new BasicDBObject("parentEntity.id", req.entity.id);
        query.put(ConstantsGlobal.QID,
                new BasicDBObject(MongoManager.IN_QUERY, resultQueryQids.toArray()));
        LOGGER.info("getEntityQusAttemptInfos before questionAnalytics");
        VedantuDBResult<QuestionAnalytics> questionAnalytics = QuestionAnalyticsDAO.INSTANCE
                .getInfos(query, null, req.start, req.size,
                        getQuestionSetOrderQuery(req.orderBy, req.sortOrder));
        LOGGER.info("getEntityQusAttemptInfos after questionAnalytics");
        LOGGER.debug("getEntityQusAttemptInfos QuesttionAnalytics hits : " + questionAnalytics + " query : " + query);
        // collect the resulted qIds to fetch their details data
        List<String> finalQids = new ArrayList<String>();
        for (QuestionAnalytics qA : questionAnalytics.results) {
            finalQids.add(qA.qId);
        }
        if (CollectionUtils.isEmpty(finalQids)) {
            finalQids = resultQueryQids;
        }
        GetEntityQuestionAttemptInfoListRes res = new GetEntityQuestionAttemptInfoListRes();
        res.totalHits = defaultOrder ? totalHits : questionAnalytics.totalHits;

        LOGGER.debug("getEntityQusAttemptInfos finalQIds : " + finalQids);
        LOGGER.info("getEntityQusAttemptInfos before questionInfoMap");
        Map<String, QuestionSearchIndexDetails> questionInfoMap = QuestionManager
                .getQuestionsMap(finalQids);
        LOGGER.info("getEntityQusAttemptInfos after questionInfoMap");
        LOGGER.info("getEntityQusAttemptInfos before getAnalyticsCount");
        long count = UserEntityAnalyticsDAO.INSTANCE.getAnalyticsCount(req.entity,
                AcademicDimensionType.OVERALL.name(),req.orgId);
        LOGGER.info("getEntityQusAttemptInfos after getAnalyticsCount");
        test.attempts = count;
        LOGGER.info("getEntityQusAttemptInfos before adding qStatsInfo");
        for (QuestionAnalytics qA : questionAnalytics.results) {
            UserQuestionAnalyticsDAO listOfStudents = new UserQuestionAnalyticsDAO();
            QuestionAttemptStatsInfoDetail qStatsInfo = new QuestionAttemptStatsInfoDetail(
                    qIdOrderNoMap.get(qA.qId), questionInfoMap.get(qA.qId), qA.measures);
            //Get Marks value.
            qStatsInfo.info.marks = qIdstoMarks.get(qA.qId);
//            Need to Fix Code.
            LOGGER.info("getEntityQusAttemptInfos before getting correct list for question "+qA.qId);
            qStatsInfo.measures.correct = getUsersListWithOrgFilter(listOfStudents.getAllUsersAnalytics(qA.qId, AnswerCorrectness.CORRECT, req.entity),req.orgId).size();
            LOGGER.info("getEntityQusAttemptInfos after getting correct list for question "+qA.qId);
            LOGGER.info("getEntityQusAttemptInfos before getting partial list for question "+qA.qId);
            qStatsInfo.measures.partial = getUsersListWithOrgFilter(listOfStudents.getAllUsersAnalytics(qA.qId, AnswerCorrectness.PARTIAL, req.entity),req.orgId).size();
            LOGGER.info("getEntityQusAttemptInfos after getting partial list for question "+qA.qId);
            LOGGER.info("getEntityQusAttemptInfos before getting incorrect list for question "+qA.qId);
            qStatsInfo.measures.incorrect = getUsersListWithOrgFilter(listOfStudents.getAllUsersAnalytics(qA.qId, AnswerCorrectness.INCORRECT, req.entity),req.orgId).size();
            LOGGER.info("getEntityQusAttemptInfos after getting incorrect list for question "+qA.qId);
            qStatsInfo.measures.left = (test.attempts - (qStatsInfo.measures.correct+qStatsInfo.measures.incorrect+qStatsInfo.measures.partial));
            res.list.add(qStatsInfo);
        }
        LOGGER.info("getEntityQusAttemptInfos after adding qStatsInfo");
        if (defaultOrder) {
            Collections.sort(res.list, new EntityQuestionAttemptInfoComparator());
            LOGGER.info("getEntityQusAttemptInfos after sort");
        }
        return res;
    }

    public static List<UserQuestionAnalytics> getUsersListWithOrgFilter(List<UserQuestionAnalytics> users, String orgId){
        List<UserQuestionAnalytics> newList = new ArrayList<UserQuestionAnalytics>();
        for(UserQuestionAnalytics user: users){
            if(OrgMemberDAO.INSTANCE.getMemberByUserId(orgId, user.userId) == null){

            }else{
                newList.add(user);
            }
        }
        return newList;
    }

    public static List<UserQuestionAnalytics> getStudentAnalyticsList(GetQuestionAnalyticsReq req){
        List<UserQuestionAnalytics> res = new ArrayList<UserQuestionAnalytics>();
        UserQuestionAnalyticsDAO data = new UserQuestionAnalyticsDAO();
        res = getUsersListWithOrgFilter(data.getAllUsersAnalytics(req.qId, req.isCorrect, req.parentEntity),req.orgId);
        return res;
    }

    public static GetUserEntityQuestionAttemptStatInfoListRes getUserEntityQuestionsAttemptInfoStat(
            GetEntityQuestionsAttemptStatReq req) throws VedantuException {
        LOGGER.debug("getUserEntityQuestionsAttemptInfoStat : Request Came ");
        String forUserId = req._getResultForUserId();
        LOGGER.debug("getUserEntityQuestionsAttemptInfoStat : Before getAttemptedEntity");
        getAttemptedEntity(req.entity);
        LOGGER.debug("getUserEntityQuestionsAttemptInfoStat : After getAttemptedEntity");
        UserEntityAttempt entityAttempt = UserEntityAttemptDAO.INSTANCE.getAttempt(forUserId,
                req.entity.type, req.entity.id);
        if (entityAttempt == null) {
            LOGGER.debug("getUserEntityQuestionsAttemptInfoStat : Test not attempted, Checking whether test is LIVE or NOT");
            if (req.target != null) {
                MutableLong totalHits = new MutableLong();
                LibraryContentLink cLink = LibraryContentLinksDAO.INSTANCE.getLibraryContentLink(new SrcEntity(
                        EntityType.TEST, req.entity.id), new SrcEntity(req.target.type,
                        req.target.id), UserActionType.ADDED, VedantuRecordState.ACTIVE, totalHits);
                if(cLink.getSchedule() != null){
                    if(cLink.getSchedule().startTime != null){
                        if (cLink.getSchedule().startTime.getTime() - System.currentTimeMillis() > 0){
                            LOGGER.debug("cLink time "+cLink.getSchedule().startTime.getTime()+" System Time "+System.currentTimeMillis());
                            throw new VedantuException(VedantuErrorCode.TEST_IS_NOT_LIVE, "This test starts on @ "+cLink.getSchedule().startTime.getTime());
                        }
                    }
                    if(cLink.getSchedule().closeTime != null){
                        if (System.currentTimeMillis() - cLink.getSchedule().closeTime.getTime() > 0){
                            LOGGER.debug("cLink time "+cLink.getSchedule().closeTime.getTime()+" System Time "+System.currentTimeMillis());
                            throw new VedantuException(VedantuErrorCode.TEST_IS_NOT_LIVE, "This test was ended on @ "+cLink.getSchedule().closeTime.getTime());
                        }
                    }
                }
            }else{
                throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, "SectionId is missing");
            }
        }
        if (entityAttempt == null) {
            String msg = "user[" + forUserId + "] has not attempted entity [" + req.entity + "]";
            LOGGER.error(msg);
            throw new VedantuException(VedantuErrorCode.NOT_ATTEMPTED, msg);
        }

        if (!entityAttempt.finished || entityAttempt.endTime <= 0) {
            String msg = "user[" + forUserId + "] attempted for entity [" + req.entity
                    + "] is in progress";
            LOGGER.error(msg);
            throw new VedantuException(VedantuErrorCode.ATTEMPT_IN_PROGRESS, msg);
        }

        GetUserEntityQuestionAttemptStatInfoListRes res = new GetUserEntityQuestionAttemptStatInfoListRes();
        res.totalHits = entityAttempt.qIds == null ? 0 : entityAttempt.qIds.size();
        res.startTime = entityAttempt.timeCreated;
        res.endTime = entityAttempt.endTime;
        if(entityAttempt.finished && entityAttempt.processed){
        	               res.analyticsGenerated = true;
        	        }

        UserEntityAnalytics userEntityAnalytics = UserEntityAnalyticsDAO.INSTANCE.getAnalytics(
                forUserId, entityAttempt._getStringId(), req.entity, AcademicDimensionType.OVERALL,
                AcademicDimensionType.OVERALL.name());
        res.timeTaken = userEntityAnalytics == null ? 0 : userEntityAnalytics.measures.timeTaken;

        List<String> resultQueryQids = entityAttempt.qIds;
        final boolean defaultOrder = StringUtils.isEmpty(req.orderBy);
        int lastQusNo = req.start;
        if (defaultOrder) {
            req.start = Math.min(Math.max(0, req.start), resultQueryQids.size());
            req.size = Math.min(Math.max(0, req.size), (resultQueryQids.size() - req.start));
            if (req.size == 0) {
                req.size = resultQueryQids.size() - req.start;
            }
            LOGGER.info("default order start: " + req.start + ", size:" + req.size);
            resultQueryQids = resultQueryQids.subList(req.start, req.start + req.size);
            req.start = 0; // set it to zero so that MONGO will not skip the
            // results as we have already skiped
        }

        // now fetch question attempts for entity==req.entity
        DBObject query = new BasicDBObject(ConstantsGlobal.USER_ID, req._getResultForUserId());
        query.put("parentEntity.id", req.entity.id);
        query.put("parentEntity.type", req.entity.type.name());
        query.put(ConstantsGlobal.QID,
                new BasicDBObject(MongoManager.IN_QUERY, resultQueryQids.toArray()));
        VedantuDBResult<UserQuestionAnalytics> questionAnalytics = UserQuestionAnalyticsDAO.INSTANCE
                .getInfos(query, null, req.start, req.size, null);
        Map<String, UserQuestionAnalytics> questionAnalyticsMap = new HashMap<String, UserQuestionAnalytics>();
        for (UserQuestionAnalytics qa : questionAnalytics.results) {
            questionAnalyticsMap.put(qa.qId, qa);
        }
        Map<String, ContentSearchDetails> questionContentInfoMap = null;
        Map<String, Answer> answerMap = null;
        if (req.downloadQuestions) {
            answerMap = AnswerDAO.INSTANCE.getQuestionAnswerMap(resultQueryQids);
            Map<String, QuestionSearchIndexDetails> questionInfoMap = QuestionManager
                    .getQuestionsMap(resultQueryQids);

            List<ContentSearchDetails> questionAnnotatedInfos = new ArrayList<ContentSearchDetails>();

            for (Entry<String, QuestionSearchIndexDetails> entry : questionInfoMap.entrySet()) {
                try {
                    questionAnnotatedInfos.add(entry.getValue().__getContentSearchDetails());
                } catch (JSONException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }

            questionInfoMap.clear();
            questionInfoMap = null;

            annotateExtraInfo(StringUtils.EMPTY, req.userId, req.orgId, req.entity.type, questionAnnotatedInfos, true);

            questionContentInfoMap = new HashMap<String, ContentSearchDetails>();

            for (ContentSearchDetails qDetails : questionAnnotatedInfos) {
                LOGGER.debug("question search details : " + qDetails);
                questionContentInfoMap.put(qDetails.id, qDetails);
            }
        }

        for (int i = 0; i < resultQueryQids.size(); i++) {
            UserQuestionAnalytics userQA = questionAnalyticsMap.get(resultQueryQids.get(i));
            QuestionAttemptStatsInfo qAanalyticInfo = null;

            if (userQA == null) {
                qAanalyticInfo = new QuestionAttemptStatsInfo(lastQusNo + i + 1, req.entity,
                        resultQueryQids.get(i), AttemptStatus.LEFT, null,
                        null, AnswerCorrectness.INCORRECT, 0, 0, 0);
            } else {
                qAanalyticInfo = new QuestionAttemptStatsInfo(i + 1, userQA.parentEntity,
                        userQA.qId, AttemptStatus.ATTEMPTED, userQA.answerGiven,
                        userQA.matrixAnswerGiven, userQA.isCorrect, userQA.score, userQA.timeTaken,
                        userQA.timeCreated);
            }

            if (questionContentInfoMap != null
                    && questionContentInfoMap.get(resultQueryQids.get(i)) != null) {
                ContentSearchDetails cSearchDetails = questionContentInfoMap.get(resultQueryQids
                        .get(i));
                if (answerMap != null) {
                    GetSolutionsReq solutionsReq = new GetSolutionsReq();
                    solutionsReq.qId = cSearchDetails.id;
                    GetSolutionsRes solutionRes = QuestionManager.getSolutions(solutionsReq);
                    ContentManager.annotateQuestionAnswerInfo(
                            answerMap.get(resultQueryQids.get(i)), solutionRes, cSearchDetails);
                }
                qAanalyticInfo.content = cSearchDetails;

            }
            res.list.add(qAanalyticInfo);
        }
        return res;
    }

    public static GetEntityTestStatusRes getEntityTestStatus(GetUserEntityAnalyticsReq req)
            throws VedantuException {
        GetEntityTestStatusRes res = new GetEntityTestStatusRes();
        if (req.target != null) {
            MutableLong totalHits = new MutableLong();
            LibraryContentLink cLink = LibraryContentLinksDAO.INSTANCE.getLibraryContentLink(
                    new SrcEntity(EntityType.TEST, req.entity.id), new SrcEntity(req.target.type,
                            req.target.id), UserActionType.ADDED, VedantuRecordState.ACTIVE,
                    totalHits);
            if (cLink.getSchedule() != null) {
                if (cLink.getSchedule().startTime != null) {
                    res.startTime = cLink.getSchedule().startTime.getTime();
                    res.startsIn = res.startTime - System.currentTimeMillis();
                }
                if (cLink.getSchedule().endTime != null) {
                    res.endTime = cLink.getSchedule().endTime.getTime();
                    res.endsIn = res.endTime - System.currentTimeMillis();
                }
                if (cLink.getSchedule().closeTime != null) {
                    res.closeTime = cLink.getSchedule().closeTime.getTime();
                    res.closesIn = res.closeTime - System.currentTimeMillis();
                }
            }
        } else {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, "SectionId is missing");
        }
        return res;
    }

    public static AbstractContentStatsModel getAttemptedEntity(SrcEntity entity)
            throws VedantuException {

        @SuppressWarnings("rawtypes")
        VedantuBasicDAO dao = EntityTypeDAOFactory.INSTANCE.get(entity.type);
        if (dao == null) {
            LOGGER.error("no DAO registred for entityType: " + entity.type);
            throw new VedantuException(VedantuErrorCode.ENTITY_NOT_FOUND);
        }
        VedantuBaseMongoModel model = dao.getById(entity.id, VedantuRecordState.ACTIVE);
        if (model == null) {
            LOGGER.error("no entity found entity:" + entity);
            throw new VedantuException(VedantuErrorCode.ENTITY_NOT_FOUND);
        }
        AbstractContentStatsModel test = (AbstractContentStatsModel) model;
        return test;
    }

    public static GetEntityMarkDistributionRes getEntityMarkDistribution(
            GetEntityMarkDistributionReq req) throws VedantuException {

        DBObject query = new BasicDBObject(ConstantsGlobal.ENTITY_DOT_ID, req.entity.id);
        query.put(ConstantsGlobal.USER_IDS, new BasicDBObject(MongoManager.NE_QUERY, null));
        query.put(ConstantsGlobal.ACAD_DIM_DOT_ID, StringUtils.isNotEmpty(req.brdId) ? req.brdId
                : AcademicDimensionType.OVERALL.name());

        DBObject fields = MongoManager.getFieldsDBObject(Arrays.asList(ConstantsGlobal.SCORE),
                MongoManager.INCLUDE_FIELD);
        EntityHighscore minEntityScore = EntityHighScoreDAO.INSTANCE.findOne(query, fields,
                MongoManager.getSortQuery(ConstantsGlobal.SCORE, SortOrder.ASC.name()));

        EntityHighscore maxEntityScore = EntityHighScoreDAO.INSTANCE.findOne(query, fields,
                MongoManager.getSortQuery(ConstantsGlobal.SCORE, SortOrder.DESC.name()));
        List<UserEntityAnalytics> usersList = UserEntityAnalyticsDAO.INSTANCE.getAnalyticsList(
                req.entity, StringUtils.isNotEmpty(req.brdId) ? req.brdId
                        : AcademicDimensionType.OVERALL.name(), req.orgId);
        double min = Integer.MAX_VALUE;
        double max = Integer.MIN_VALUE;
        for (UserEntityAnalytics user : usersList) {
            if (min > user.measures.score)
                min = user.measures.score;
            if (max < user.measures.score)
                max = user.measures.score;
        }
        double minScore = minEntityScore == null ? 0 : min;
        double maxScore = maxEntityScore == null ? 0 : max;
        double bucketRange = (maxScore - minScore) / (req.bucketCount < 1 ? 1 : req.bucketCount);
        if ((maxScore - minScore) % (req.bucketCount < 1 ? 1 : req.bucketCount) > 0) {
            bucketRange++;
        }
        bucketRange = Math.max(bucketRange, 1);

        query.put(ConstantsGlobal.SCORE, new BasicDBObject("$lte", maxScore));

        LOGGER.info("maxScore: " + maxScore + ", minScore: " + minScore + ",bucketRange: "
                + bucketRange + ", entity: " + req.entity);

        int totalScore = 0;// will be used to calculate avgScore
        GetEntityMarkDistributionRes res = new GetEntityMarkDistributionRes();
        VedantuDBResult<EntityHighscore> scores = EntityHighScoreDAO.INSTANCE.getInfos(query, null,
                MongoManager.NO_START, MongoManager.NO_LIMIT,
                MongoManager.getSortQuery(ConstantsGlobal.SCORE, SortOrder.ASC.name()));

        Set<String> usersSet = new HashSet<String>();
        for (UserEntityAnalytics userList : usersList) {
            usersSet.add(userList.userId);
        }
        Iterator<EntityHighscore> highscores = scores.results.iterator();
        while (highscores.hasNext()) {
            EntityHighscore highscore = highscores.next();
            if (highscore.userIds == null || highscore.userIds.isEmpty()) {
                continue;
            }
            Iterator<String> users = highscore.userIds.iterator();
            while (users.hasNext()) {
                String user = users.next();
                if (!usersSet.contains(user)) {
                    users.remove();
                }
            }
            if (highscore.userIds.isEmpty())
                highscores.remove();
        }

        double currentBucketStart = minScore;
        // 1st create buckets then add the data there, and in the last remove
        // the empty bucket from
        // the end

        for (int i = 0; i < req.bucketCount; i++) {
            double start = currentBucketStart;
            double end = currentBucketStart + bucketRange;
            res.list.add(new MarkDistribution(start, end, 0));
            currentBucketStart = end;
        }

        int activeBucket = 0;
        for (EntityHighscore highscore : scores.results) {
            int count = highscore.userIds == null ? 0 : highscore.userIds.size();
            totalScore += highscore.score * count;
            res.totalHits += count;
            MarkDistribution markDist = res.list.get(activeBucket);
            if (highscore.score >= markDist.to) {
                activeBucket = getNextBucket(res.list, highscore.score, activeBucket);
                markDist = res.list.get(activeBucket);
            }
            markDist.count += count;
        }
        // now remove empty buckets from the end
        for (int i = res.list.size() - 1; i >= 0; i--) {
            if (res.list.size() > i && res.list.get(i).count < 1) {
                res.list.remove(i);
            } else {
                break;
            }
        }

        Logger.debug("total Score : " + totalScore);
        res.avgScore = totalScore / (res.totalHits < 1 ? 1 : res.totalHits);
        return res;
    }

    private static int getNextBucket(List<MarkDistribution> list, double score, int currectBucket) {

        for (int i = currectBucket; i < list.size(); i++) {
            MarkDistribution markDist = list.get(i);
            if (score > markDist.from && score <= markDist.to) {
                return i;
            }
        }
        return currectBucket;
    }

    private static List<String> getQIdsSubList(String brdId, AbstractTestCommonModel test,
            List<String> qIds) {

        if (StringUtils.isNotEmpty(brdId)) {
            Set<String> brdQids = new HashSet<String>(test.__getAllQIds(brdId));
            List<String> updatedQids = new ArrayList<String>();
            for (String qId : qIds) {
                if (brdQids.contains(qId)) {
                    updatedQids.add(qId);
                }
            }
            qIds = updatedQids;
        }
        return qIds;
    }

    private static DBObject getQuestionSetOrderQuery(String orderBy, String sortOrder) {

        if (StringUtils.isEmpty(orderBy)) {
            return null;
        }
        SortOrder sOrder = SortOrder.valueOfKey(sortOrder);

        DBObject sortQuery = new BasicDBObject();

        if (StringUtils.equalsIgnoreCase(orderBy, "attempts")) {
            sortQuery.put("measures.attempts", sOrder.getValue());
            sortQuery.put("measures.correct", sOrder.getValue());
        } else if (StringUtils.equalsIgnoreCase(orderBy, "correct")) {
            sortQuery.put("measures.correct", sOrder.getValue());
            sortQuery.put("measures.attempts", sOrder.getValue());
        }

        return sortQuery;
    }

    private static Map<String, UserQuestionAnalytics> getQuestionAnalyticsMap(SrcEntity entity,
            String userId) {

        DBObject query = new BasicDBObject("parentEntity.id", entity.id);
        query.put(ConstantsGlobal.USER_ID, userId);
        VedantuDBResult<UserQuestionAnalytics> questionAnalytics = UserQuestionAnalyticsDAO.INSTANCE
                .getInfos(query, null, MongoManager.NO_START, MongoManager.NO_LIMIT, null);
        Map<String, UserQuestionAnalytics> questionAnalyticsMap = new HashMap<String, UserQuestionAnalytics>();
        for (UserQuestionAnalytics qA : questionAnalytics.results) {
            questionAnalyticsMap.put(qA.qId, qA);
        }
        return questionAnalyticsMap;
    }

    public static Map<String, Boolean> getEntityAttemptsMap(Set<String> entityIds, String userId) {

        Map<String, Boolean> entityAttemptsMap = new HashMap<String, Boolean>();
        if (StringUtils.isEmpty(userId) || CollectionUtils.isEmpty(entityIds)) {
            LOGGER.error("empty entityIds : " + entityIds);
            return entityAttemptsMap;
        }
        DBObject query = new BasicDBObject("entity.id", new BasicDBObject(MongoManager.IN_QUERY,
                entityIds.toArray()));
        query.put(ConstantsGlobal.USER_ID, userId);
        LOGGER.debug("getEntityAttemptsMap query : " + query);
        VedantuDBResult<UserEntityAttempt> entityAttempts = UserEntityAttemptDAO.INSTANCE.getInfos(
                query, null, MongoManager.NO_START, MongoManager.NO_LIMIT, null);
        for (UserEntityAttempt entityAttempt : entityAttempts.results) {
            entityAttemptsMap.put(entityAttempt.entity.id, Boolean.valueOf(true));
        }
        LOGGER.debug("returning attempts entity map : " + entityAttemptsMap);
        return entityAttemptsMap;
    }

    @SuppressWarnings("unchecked")
    public static GetEntityScheduleAnalyticsRes getEntityAnalyticsSchedule(
            GetEntityScheduleAnalyticsReq req) throws VedantuException {

        GetEntityScheduleAnalyticsRes res = new GetEntityScheduleAnalyticsRes();
        // get all the sections of this program
        Set<String> targetIds = StringUtils.isNotEmpty(req.sectionId) ? new HashSet<String>(
                Arrays.asList(req.sectionId)) : OrgProgramManager.getProgramSections(req.orgId,
                req.programId,
                StringUtils.isEmpty(req.centerId) ? null : Arrays.asList(req.centerId));

        // added by Shankar
        // aggregated view of all the test directly added to library or in a
        // module added to
        // the library

        // 1. aggregate all the module added to target sections and having any
        // test in it, and fetch
        // the test ids

        Query<LibraryContentLink> query = LibraryContentLinksDAO.INSTANCE.createQuery();
        query.filter("source.type", EntityType.MODULE).field("target.id").in(targetIds)
                .filter("target.type", EntityType.SECTION)
                .filter(ConstantsGlobal.LINK_TYPE, UserActionType.ADDED)
                .filter(ConstantsGlobal.RECORD_STATE, VedantuRecordState.ACTIVE);
        LOGGER.debug("module link fetcher query: " + query);
        Iterator<LibraryContentLink> links = query.iterator();

        Map<SrcEntity, Set<String>> moduleEntityToSectionIdsMap = new HashMap<SrcEntity, Set<String>>();

        while (links.hasNext()) {
            LibraryContentLink link = links.next();
            targetIds.add(link.source.id);
            if (moduleEntityToSectionIdsMap.get(link.source) == null) {
                moduleEntityToSectionIdsMap.put(link.source, new HashSet<String>());
            }
            moduleEntityToSectionIdsMap.get(link.source).add(link.target.id);
        }

        // 2. aggregate all the testId in library/targetIds, if query is there
        // than filter it
        // with query from es {returned ids.size()==totalHits}

        FilterBuilder filter = FilterBuilders.hasChildFilter(
                UserActionType.ADDED.getSearchIndexType(),
                FilterBuilders.inFilter("dst.id", targetIds.toArray()));
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if (StringUtils.isNotEmpty(req.query)) {
            boolQuery.must(QueryBuilders.queryString(req.query.toLowerCase()));
        }
        if (StringUtils.isNotEmpty(req.brdId)) {
            boolQuery.must(QueryBuilders.termQuery(ConstantsGlobal.BOARDS + "."
                    + ConstantsGlobal.ID, req.brdId));
        }
        if (StringUtils.isNotEmpty(req.courseId) && StringUtils.isEmpty(req.topicId)) {
            boolQuery.must(QueryBuilders.termQuery(ConstantsGlobal.BOARDS + "."
                    + ConstantsGlobal.ID, req.courseId));
        } else if (StringUtils.isNotEmpty(req.courseId) && StringUtils.isNotEmpty(req.topicId)) {
            boolQuery.must(QueryBuilders.termQuery(ConstantsGlobal.BOARDS + "."
                    + ConstantsGlobal.ID, req.topicId));
        }

        QueryBuilder esQuery = boolQuery.hasClauses() ? boolQuery : QueryBuilders.matchAllQuery();
        // TODO: use the partial fetching query of elastic-search
        QueryBuilder finalQuery = QueryBuilders.filteredQuery(esQuery, filter);
        SearchListResponse<EntityAnalyticsBasicInfo> tests = getEntityInfos(null, null, 0, 1000,
                req.entityType, EntityAnalyticsBasicInfo.class, finalQuery, null, null);
        res.totalHits = tests.totalHits;

        Map<String, EntityAnalyticsBasicInfo> entityMap = new HashMap<String, EntityAnalyticsBasicInfo>();

        for (EntityAnalyticsBasicInfo entity : tests.list) {
            entity.__fillBoardsTotalMarks();
            entityMap.put(entity.id, entity);
        }

        LOGGER.debug("entityMap: " + entityMap);
        if (entityMap.isEmpty()) {
            LOGGER.debug("no entity found for req : " + req);
            return res;
        }
        Map<String, EntityAnalytics> entityAnalyticsMap = getEntityAnalyticsMap(entityMap.keySet());
        Map<String, EntityTopper> entityTopperMap = getEntityToppers(req.orgId, entityMap.keySet(),
                AcademicDimensionType.OVERALL.name(), entityMap);

        AggregationOutput aggregationOutput = LibraryManager.getEntityScheduleAggregationOutput(
                entityMap.keySet(), targetIds, req.start, req.size, true);
        LOGGER.debug("aggregationOutput : " + aggregationOutput);
        if (aggregationOutput == null) {
            return res;
        }

        // in the aggregation result fetch mapping of module to set of
        // sectionIds

        for (DBObject d : aggregationOutput.results()) {
            LOGGER.debug("db object : " + d);
            String testId = d.get(ConstantsGlobal._ID).toString();
            ScheduleInfo scheduleInfo = ObjectMapperUtils.convertValue(
                    d.get(ConstantsGlobal.SCHEDULE), ScheduleInfo.class);
            List<DBObject> targets = (List<DBObject>) d.get(ConstantsGlobal.TARGETS);
            Set<String> sectnIds = new HashSet<String>();

            for (DBObject entity : targets) {
                SrcEntity srcEntity = new SrcEntity(EntityType.valueOfKey((String) entity
                        .get(ConstantsGlobal.TYPE)), (String) entity.get(ConstantsGlobal.ID));
                if (srcEntity.type == EntityType.SECTION) {
                    sectnIds.add(srcEntity.id);
                } else {
                    Set<String> sIds = moduleEntityToSectionIdsMap.get(srcEntity);
                    if (sIds != null) {
                        sectnIds.addAll(sIds);
                    }
                }
            }

            EntityAnalyticsBasicInfo entity = entityMap.get(testId);
            if (entity == null) {
                continue;
            }

            EntityAnalytics entityAnalytics = entityAnalyticsMap.get(__getEntityAnalyticsMapKey(
                    entity.id, AcademicDimensionType.OVERALL.name()));

            if (entityAnalytics != null) {
                entity.measures = entityAnalytics.measures;
                entity.totalAttempts = entity.attempts;
            }

            EntityAnalyticsScheduleInfo infos = new EntityAnalyticsScheduleInfo(scheduleInfo,
                    OrgProgramManager.getProgramBySectionIds(sectnIds, true),
                    entityTopperMap.get(entity.id), entity);

            // add the boards analytics measures here
            if (infos.entity.boards != null) {
                for (BoardAnalyticsInfo boardInfo : infos.entity.boards) {
                    EntityAnalytics boardAnalytics = entityAnalyticsMap
                            .get(__getEntityAnalyticsMapKey(entity.id, boardInfo.id));
                    if (boardAnalytics != null) {
                        boardInfo.measures = boardAnalytics.measures;
                    }
                }
            }

            // sort data by only course
            // if(StringUtils.isNotEmpty(req.courseId) &&
            // StringUtils.isEmpty(req.topicId)){
            // if(infos.entity.boards != null){
            // for(BoardAnalyticsInfo boardInfo : infos.entity.boards){
            // if(req.courseId.equals(boardInfo.id)){
            // LOGGER.debug("infos object sorted according to course : " +
            // infos);
            // res.list.add(infos);
            // break;
            // }
            // }
            // }
            // }
            // // sort data by course and topic wise
            // else if(StringUtils.isNotEmpty(req.courseId) &&
            // StringUtils.isNotEmpty(req.topicId)){
            // if(infos.entity.boards != null) {
            // for (BoardAnalyticsInfo boardInfo : infos.entity.boards) {
            // if (req.topicId .equals(boardInfo.id)) {
            // LOGGER.debug("infos object sorted according to topics : " +
            // infos);
            // res.list.add(infos);
            // break;
            // }
            // }
            // }
            // }else{
            LOGGER.debug("infos object : " + infos);
            res.list.add(infos);
            // }
        }
        entityMap.clear();
        return res;
    }

    /**
     *
     * @param req
     * @return data corresponding to entity{type:entityType, id:OVERALL}
     * @throws VedantuException
     */
    public static GetUserAnalyticsStatsRes getUserAnalyticsStats(GetUserAnalyticsStatsReq req)
            throws VedantuException {

        String userId = req._getResultForUserId();
        Map<String, UserEntityAnalytics> userEntityAnalyticsMap = getUserEntityAnalyticsMap(
                Arrays.asList(AcademicDimensionType.OVERALL.name()), Arrays.asList(userId),
                req.entityType, true);
        Set<String> brdIds = new HashSet<String>();
        GetUserAnalyticsStatsRes res = new GetUserAnalyticsStatsRes();
        for (Entry<String, UserEntityAnalytics> entry : userEntityAnalyticsMap.entrySet()) {
            String[] keys = StringUtils.split(entry.getKey(), "_");
            if (!StringUtils.equals(keys[2], AcademicDimensionType.OVERALL.name())) {
                brdIds.add(keys[2]);
            }
        }

        Map<String, BoardBasicInfo> boardInfo = BoardDAO.INSTANCE.getBasicInfosByIds(brdIds);
        for (Entry<String, UserEntityAnalytics> entry : userEntityAnalyticsMap.entrySet()) {
            String[] keys = StringUtils.split(entry.getKey(), "_");
            if (StringUtils.equals(keys[2], AcademicDimensionType.OVERALL.name())) {
                res.measures = entry.getValue().measures;
                res.percentage = entry.getValue().percentage;
                DBObject eAttemptCountQuery = new BasicDBObject(ConstantsGlobal.USER_ID, userId);
                eAttemptCountQuery.put(ConstantsGlobal.PARENT, null);
                eAttemptCountQuery.put(ConstantsGlobal.ENTITY_DOT_TYPE, req.entityType.name());
                res.totalAttempts = UserEntityAttemptDAO.INSTANCE.count(eAttemptCountQuery);
            } else {
                res.addBoardAnalytics(boardInfo.get(keys[2]), entry.getValue().measures,
                        entry.getValue().percentage);
            }
        }
        return res;
    }

    /**
     * only valid for test and assignment
     *
     * @param req
     * @throws VedantuException
     */
    public static GetUserEntityMeasuresRes getUserEntityMeasures(GetUserEntityMeasuresReq req)
            throws VedantuException {

        AbstractTestCommonModel test = (AbstractTestCommonModel) getAttemptedEntity(req.entity);

        UserEntityAnalytics userEntityAnalytics = UserEntityAnalyticsDAO.INSTANCE.getAnalytics(
                req.targetUserId, null, req.entity, AcademicDimensionType.OVERALL,
                AcademicDimensionType.OVERALL.name());
        GetUserEntityMeasuresRes res = populateUserEntityMeasures(test.qusCount,
                userEntityAnalytics);
        res.user = getUserInfo(req.orgId, req.targetUserId);

        return res;
    }

    public static GetUserEntityRankRes getUserEntityRank(GetUserEntityRankReq req)
            throws VedantuException {

        GetUserEntityRankRes res = new GetUserEntityRankRes();
        UserEntityAnalytics userEntityAnalytics = UserEntityAnalyticsDAO.INSTANCE.getAnalytics(
                req.targetUserId, null, req.entity, AcademicDimensionType.OVERALL,
                AcademicDimensionType.OVERALL.name());
        if (userEntityAnalytics == null) {
            throw new VedantuException(VedantuErrorCode.NOT_ATTEMPTED, "user " + req.targetUserId
                    + " has not attempted entity: " + req.entity);
        }
        res.user = getUserInfo(req.orgId, req.targetUserId);
        Test test = TestDAO.INSTANCE.getById(req.entity.id);
        if(test.showAIR){
            res.showAIR = true;
            res.AIR  = UserEntityAnalyticsDAO.INSTANCE.getRank(req.entity.id,
                    userEntityAnalytics.measures.score, userEntityAnalytics.measures.timeTaken,
                    AcademicDimensionType.OVERALL.name());
        }
        res.rank = UserEntityAnalyticsDAO.INSTANCE.getRank(req.orgId, req.entity.id,
                userEntityAnalytics.measures.score, userEntityAnalytics.measures.timeTaken,
                AcademicDimensionType.OVERALL.name());
        return res;
    }

    public static GetEntityMeasuresRes getEntityMeasures(GetEntityMeasuresReq req)
            throws VedantuException {

        AbstractTestCommonModel test = (AbstractTestCommonModel) getAttemptedEntity(req.entity);
        DBObject query = new BasicDBObject(ConstantsGlobal.ENTITY_DOT_ID, req.entity.id);
        query.put(ConstantsGlobal.ENTITY_DOT_TYPE, req.entity.type.name());
        query.put(ConstantsGlobal.ACAD_DIM_DOT_ID, AcademicDimensionType.OVERALL.name());
        GetEntityMeasuresRes res = new GetEntityMeasuresRes();

        VedantuDBResult<UserEntityAnalytics> analytics = UserEntityAnalyticsDAO.INSTANCE.getInfos(
                query, null, req.start, req.size,
                getQuestionSetOrderQuery(req.orderBy, req.sortOrder));
        res.totalHits = analytics.totalHits;
        Set<String> userIds = new HashSet<String>();

        for (UserEntityAnalytics userEntityAnalytics : analytics.results) {
            userIds.add(userEntityAnalytics.userId);
        }

        Map<String, ModelBasicInfo> userInfoMap = getUserInfoMap(req.orgId, userIds);

        for (UserEntityAnalytics userEntityAnalytics : analytics.results) {
            GetUserEntityMeasuresRes userMeasuresRes = populateUserEntityMeasures(test.qusCount,
                    userEntityAnalytics);
            userMeasuresRes.user = (UserInfo) userInfoMap.get(userEntityAnalytics.userId);
            res.list.add(userMeasuresRes);
        }
        return res;

    }

    private static GetUserEntityMeasuresRes populateUserEntityMeasures(int totalQus,
            UserEntityAnalytics userEntityAnalytics) {

        GetUserEntityMeasuresRes res = new GetUserEntityMeasuresRes();
        QuestionMeasures measures = new QuestionMeasures();
        measures.left = totalQus;
        res.measures = measures;
        if (userEntityAnalytics != null) {
            measures.correct = userEntityAnalytics.measures.correct;
            measures.partial = userEntityAnalytics.measures.partial;
            measures.incorrect = userEntityAnalytics.measures.incorrect;
            res._finalizeMeasures();
            res.lastAttempted = userEntityAnalytics.lastUpdated;
        }
        return res;
    }

    private static UserAnalyticsInfoRes __getUserAnalyticsInfo(String userId,
            EntityType entityType, TestMiniInfo testInfo,
            Map<String, UserEntityAnalytics> userEntityAnalyticsMap,
            Map<String, UserEntityAttempt> userEntityAttemptInfoMap,
            Map<String, EntityAnalytics> entityAnalyticsMap, boolean addTotalMarks,
            boolean addRankInAllDim, Map<String, Integer> entityLastRankMap) {

        String orgId = userId.equals("PUBLIC") ? "" : OrgMemberDAO.INSTANCE.getByUserId(userId).orgId;

        EntityInfo entity = new EntityInfo(entityType, testInfo.id, testInfo.name);
        final String acadDimId = AcademicDimensionType.OVERALL.name();
        EntityAnalytics entityAnalytics = entityAnalyticsMap == null ? null : entityAnalyticsMap
                .get(__getEntityAnalyticsMapKey(entity.id, acadDimId));
        entity.measures = entityAnalytics == null ? null : entityAnalytics.measures;
        entity.totalAttempts = StringUtils.isEmpty(orgId) ? UserEntityAnalyticsDAO.INSTANCE
                .getAnalyticsCount(entity, acadDimId) : UserEntityAnalyticsDAO.INSTANCE
                .getAnalyticsCount(orgId, entity, acadDimId);

        Integer lastRank = entityLastRankMap == null ? null : entityLastRankMap
                .get(__getEntityAnalyticsMapKey(entity.id, acadDimId));
        entity.lastRank = lastRank == null ? 0 : lastRank.intValue();
        UserEntityAnalytics userEntityAnalytics = userEntityAnalyticsMap
                .get(__getUserEntityAnalyticsMapKey(entity.id, userId, acadDimId));
        UserAnalyticsInfoRes userAnalyticsRes = new UserAnalyticsInfoRes(entity,
                userEntityAnalytics.measures);
        UserEntityAttempt attemptInfo = userEntityAttemptInfoMap == null ? null
                : userEntityAttemptInfoMap.get(__getUserEntityAttemptMapKey(entity.id, userId));
        if (attemptInfo != null) {
            userAnalyticsRes.startTime = attemptInfo.timeCreated;
            userAnalyticsRes.endTime = attemptInfo.endTime;
        }
        if (addTotalMarks) {
            userAnalyticsRes.qusCount = testInfo.qusCount;
            userAnalyticsRes.totalMarks = testInfo.totalMarks;
        }
        if (addRankInAllDim) {
            userAnalyticsRes.rank = StringUtils.isEmpty(orgId) ? UserEntityAnalyticsDAO.INSTANCE.getRank(entity.id, userAnalyticsRes.measures.score,
                            userAnalyticsRes.measures.timeTaken, acadDimId) : UserEntityAnalyticsDAO.INSTANCE
                    .getRank(orgId, entity.id, userAnalyticsRes.measures.score,
                            userAnalyticsRes.measures.timeTaken, acadDimId);
        }
        if (testInfo.children != null) {
            for (TestMiniInfo childInfo : testInfo.children) {
                UserAnalyticsInfoRes childAnalyticInfo = __getUserAnalyticsInfo(userId, entityType,
                        childInfo, userEntityAnalyticsMap, userEntityAttemptInfoMap,
                        entityAnalyticsMap, addTotalMarks, addRankInAllDim, entityLastRankMap);
                userAnalyticsRes.addChildAnalytics(childAnalyticInfo);
            }
        }

        for (IAnalyticsBoardMember boardMember : testInfo.metadata) {
            userAnalyticsRes.addBoardAnalytics(__getUserEntityBoardAnalyticsInfo(entity.id, userId,
                    boardMember, userEntityAnalyticsMap, entityAnalyticsMap, addTotalMarks,
                    addRankInAllDim, entityLastRankMap));
        }
        return userAnalyticsRes;
    }

    private static UserBoardAnalyticsInfoRes __getUserEntityBoardAnalyticsInfo(String entityId,
            String userId, IAnalyticsBoardMember iBoardMember,
            Map<String, UserEntityAnalytics> userEntityAnalyticsMap,
            Map<String, EntityAnalytics> entityAnalyticsMap, boolean addTotalMarks,
            boolean addRankInAllDim, Map<String, Integer> entityLastRankMap) {
        String orgId = userId.equals("PUBLIC") ? "" : OrgMemberDAO.INSTANCE.getByUserId(userId).orgId;
        UserEntityAnalytics userEntityAnalytics = userEntityAnalyticsMap
                .get(__getUserEntityAnalyticsMapKey(entityId, userId, iBoardMember._getEntity().id));
        if (userEntityAnalytics == null) {
            return null;
        }
        UserBoardAnalyticsInfoRes boardAnalytics = new UserBoardAnalyticsInfoRes(
                iBoardMember._getEntity(), userEntityAnalytics.measures);
        if (addTotalMarks) {
            boardAnalytics.totalMarks = iBoardMember._getTotalMarks();
            boardAnalytics.qusCount = iBoardMember._getQusCount();
            EntityAnalytics entityAnalytics = entityAnalyticsMap == null ? null
                    : entityAnalyticsMap.get(__getEntityAnalyticsMapKey(entityId,
                            iBoardMember._getEntity().id));
            boardAnalytics.entity.measures = entityAnalytics == null ? null
                    : entityAnalytics.measures;
            Integer lastRank = entityLastRankMap == null ? null : entityLastRankMap
                    .get(__getEntityAnalyticsMapKey(entityId, iBoardMember._getEntity().id));
            boardAnalytics.entity.lastRank = lastRank == null ? 0 : lastRank.intValue();
        }
        if (addRankInAllDim) {
            boardAnalytics.rank = StringUtils.isEmpty(orgId) ? UserEntityAnalyticsDAO.INSTANCE
                    .getRank(iBoardMember._getEntity().id, boardAnalytics.measures.score,
                            boardAnalytics.measures.timeTaken, iBoardMember._getEntity().id)
                    : UserEntityAnalyticsDAO.INSTANCE.getRank(orgId, iBoardMember._getEntity().id,
                            boardAnalytics.measures.score, boardAnalytics.measures.timeTaken,
                            iBoardMember._getEntity().id);
        }
        if (iBoardMember._getChildrenBoards() != null) {
            for (IAnalyticsBoardMember boardMember : iBoardMember._getChildrenBoards()) {
                boardAnalytics.addChildAnalytics(__getUserEntityBoardAnalyticsInfo(entityId,
                        userId, boardMember, userEntityAnalyticsMap, entityAnalyticsMap,
                        addTotalMarks, addRankInAllDim, entityLastRankMap));
            }
        }
        return boardAnalytics;

    }

    private static Map<String, UserEntityAnalytics> getUserEntityAnalyticsMap(
            Collection<String> entityIds, Collection<String> userIds, boolean addBoardWiseAnalytics) {

        return getUserEntityAnalyticsMap(entityIds, userIds, null, addBoardWiseAnalytics);
    }

    /**
     *
     * @param entityIds
     * @param userIds
     * @param addBoardWiseAnalytics
     * @return map key=__getUserEntityAnalyticsMapKey(uA.entity.id,uA.userId,
     *         uA.acadDim.id)
     */
    private static Map<String, UserEntityAnalytics> getUserEntityAnalyticsMap(
            Collection<String> entityIds, Collection<String> userIds, EntityType entityType,
            boolean addBoardWiseAnalytics) {

        DBObject analyticQuery = new BasicDBObject(ConstantsGlobal.ENTITY_DOT_ID,
                new BasicDBObject(MongoManager.IN_QUERY, entityIds.toArray()));
        if (entityType != null) {
            analyticQuery.put(ConstantsGlobal.ENTITY_DOT_TYPE, entityType.name());
        }
        analyticQuery.put(ConstantsGlobal.USER_ID,
                new BasicDBObject(MongoManager.IN_QUERY, userIds.toArray()));
        if (!addBoardWiseAnalytics) {
            analyticQuery
                    .put(ConstantsGlobal.ACAD_DIM_DOT_ID, AcademicDimensionType.OVERALL.name());
        }
        VedantuDBResult<UserEntityAnalytics> userEntityAnalytics = UserEntityAnalyticsDAO.INSTANCE
                .getInfos(analyticQuery, null, MongoManager.NO_START, MongoManager.NO_LIMIT, null);
        Map<String, UserEntityAnalytics> userEntityAnalyticsMap = new HashMap<String, UserEntityAnalytics>();
        for (UserEntityAnalytics uA : userEntityAnalytics.results) {
            String key = __getUserEntityAnalyticsMapKey(uA.entity.id, uA.userId, uA.acadDim.id);
            userEntityAnalyticsMap.put(key, uA);
        }
        return userEntityAnalyticsMap;

    }

    /**
     *
     * @param entityIds
     * @param userIds
     * @return map key=__getUserEntityAttemptMapKey(uA.entity.id, uA.userId)
     */
    private static Map<String, UserEntityAttempt> getUserEntityAttemptInfoMap(
            Collection<String> entityIds, Collection<String> userIds) {

        DBObject analyticQuery = new BasicDBObject(ConstantsGlobal.ENTITY + "."
                + ConstantsGlobal.ID, new BasicDBObject(MongoManager.IN_QUERY, entityIds.toArray()));
        analyticQuery.put(ConstantsGlobal.USER_ID,
                new BasicDBObject(MongoManager.IN_QUERY, userIds.toArray()));

        VedantuDBResult<UserEntityAttempt> userEntityAnalytics = UserEntityAttemptDAO.INSTANCE
                .getInfos(analyticQuery, null, MongoManager.NO_START, MongoManager.NO_LIMIT, null);
        Map<String, UserEntityAttempt> userEntityAnalyticsMap = new HashMap<String, UserEntityAttempt>();
        for (UserEntityAttempt uA : userEntityAnalytics.results) {
            String key = __getUserEntityAttemptMapKey(uA.entity.id, uA.userId);
            userEntityAnalyticsMap.put(key, uA);
        }
        return userEntityAnalyticsMap;

    }

    /**
     *
     * @param entityIds
     * @return map with key = __getEntityAnalyticsMapKey(eA.entity.id,
     *         eA.acadDim.id)
     */
    private static Map<String, EntityAnalytics> getEntityAnalyticsMap(Collection<String> entityIds) {

        DBObject query = new BasicDBObject(ConstantsGlobal.ENTITY + "." + ConstantsGlobal.ID,
                new BasicDBObject(MongoManager.IN_QUERY, entityIds.toArray()));

        VedantuDBResult<EntityAnalytics> userEntityAnalytics = EntityAnalyticsDAO.INSTANCE
                .getInfos(query, null, MongoManager.NO_START, MongoManager.NO_LIMIT, null);
        Map<String, EntityAnalytics> entityAnalyticsMap = new HashMap<String, EntityAnalytics>();
        for (EntityAnalytics eA : userEntityAnalytics.results) {
            String key = __getEntityAnalyticsMapKey(eA.entity.id, eA.acadDim.id);
            entityAnalyticsMap.put(key, eA);
        }
        LOGGER.debug("returning getEntityAnalyticsMap: " + entityAnalyticsMap);
        return entityAnalyticsMap;

    }

    /**
     *
     * @param entityIds
     * @return map with key = __getEntityAnalyticsMapKey(eA.entity.id,
     *         eA.acadDim.id)
     */
    private static Map<String, Integer> getEntityLastRankMap(Collection<String> entityIds) {

        DBObject match = new BasicDBObject(ConstantsGlobal.ENTITY + "." + ConstantsGlobal.ID,
                new BasicDBObject(MongoManager.IN_QUERY, entityIds.toArray()));
        match.put(ConstantsGlobal.USER_IDS, new BasicDBObject(MongoManager.NE_QUERY, null));

        DBObject group_id = new BasicDBObject(ConstantsGlobal.ENTITY_ID, "$entity.id");
        group_id.put("acadDimId", "$acadDim.id");

        DBObject group = new BasicDBObject(ConstantsGlobal._ID, group_id);
        group.put("lastRank", new BasicDBObject("$sum", 1));

        AggregationOutput aggregationOutput = EntityHighScoreDAO.INSTANCE.aggregate(
                new BasicDBObject("$match", match), new BasicDBObject("$group", group));

        Map<String, Integer> userEntityRankMap = new HashMap<String, Integer>();

        for (DBObject result : aggregationOutput.results()) {
            DBObject id = (DBObject) result.get(ConstantsGlobal._ID);
            if (id == null) {
                continue;
            }
            String key = __getEntityAnalyticsMapKey((String) id.get(ConstantsGlobal.ENTITY_ID),
                    (String) id.get(ConstantsGlobal.ACAD_DIM_ID));
            userEntityRankMap.put(key, (Integer) result.get("lastRank"));
        }
        return userEntityRankMap;

    }

    private static TestMiniInfo toMiniInfo(AbstractTestCommonModel test, Set<String> entityIds) {

        TestMiniInfo miniInfo = new TestMiniInfo(test.name, test.code, test._getStringId(),
                test.qusCount, test.duration, test.totalMarks);
        miniInfo.attempts = test.attempts;
        entityIds.add(miniInfo.id);
        miniInfo.metadata = test.metadata;
        miniInfo.resultVisibility = test.resultVisibility;

        if (test.childrenIds != null) {
            Map<String, Test> tests = TestDAO.INSTANCE.toInfosMap(TestDAO.INSTANCE.getInfos(
                    new BasicDBObject(ConstantsGlobal._ID, new BasicDBObject(MongoManager.IN_QUERY,
                            ObjectIdUtils.toObjectIds(test.childrenIds))), null,
                    MongoManager.NO_START, MongoManager.NO_LIMIT, null).results);
            for (String testId : test.childrenIds) {
                miniInfo.addChild(toMiniInfo(tests.get(testId), entityIds));
            }
        }
        removeExtracData(miniInfo);
        return miniInfo;
    }

    private static void removeExtracData(TestMiniInfo testInfo) {

        // this block remove, not required data from respose object
        for (TestMetadata mdata : testInfo.metadata) {
            mdata.marks = null;
            mdata.qIds = null;
            if (mdata.details != null) {
                for (TestDetails details : mdata.details) {
                    details.qIds = null;
                }
            }
            if (mdata.children != null) {
                for (BoardQus b : mdata.children) {
                    b.qIds = null;
                }
            }
        }
    }

    private static boolean isEntityAttemptAllowed(StartAttemptReq startAttemptReq) {

        switch (startAttemptReq.entityType) {
        case TEST:
            return true;
        case CHALLENGE:
            return true;
        case ASSIGNMENT:
            return true;
        case QUESTION:
            return true;
        default:
            return false;
        }
    }

    private static boolean isMultiAttemptAllowed(StartAttemptReq startAttemptReq) {

        return false;
    }

    private static boolean finalizeQuestionAttempt(UserQuestionAttempt userQuestionAttempt,
            Question question, String orgId) throws VedantuException {

        // NOTE: cannot be used for LEFT

        if (null == userQuestionAttempt) {
            LOGGER.error("finalizeQuestionAttempt cannot finalize question attempt for null userQuestionAttempt");
            return false;
        }
        if (null == question) {
            try {
                question = QuestionDAO.INSTANCE.getQuestion(userQuestionAttempt.qId);
            } catch (VedantuException e) {
                LOGGER.error(
                        "finalizeQuestionAttempt swallowing exception but this should not have occurred -- code: "
                                + e.errorCode + ", msg: " + e.getMessage(), e);
                return false;
            }
        }

        // store it in entity attempts and add the mapping to es
        LOGGER.debug("storing UserEntityAttempt userId: " + userQuestionAttempt.userId
                + ", entity : " + userQuestionAttempt.parentEntity);
        UserEntityAttemptDAO.INSTANCE.addAttempt(userQuestionAttempt.userId, orgId, EntityType.QUESTION,
                question._getStringId(), Arrays.asList(question._getStringId()),
                userQuestionAttempt.parentEntity, System.currentTimeMillis());
        EntityUserActionUtils.addEntityUserAction(userQuestionAttempt.userId,
                userQuestionAttempt.parentEntity, UserActionType.ATTEMPTED, false);

        // update UserQuestionAnalytics
        UserQuestionAnalytics userQuestionAnalytics = AnalyticsUtils
                .addUserQuestionAnalytics(userQuestionAttempt);
        LOGGER.debug("finalizeQuestionAttempt userQuestionAnalytics: " + userQuestionAnalytics);

        int attempts = 1;
        int correct = 0;
        int incorrect = 0;
        int left = 0;
        int partial = 0;
        if (CollectionUtils.isNotEmpty(userQuestionAttempt.answerGiven)) {
            switch (userQuestionAttempt.isCorrect) {
                case CORRECT:
                    correct = 1;
                    break;
                case INCORRECT:
                    incorrect = 1;
                    break;
                case PARTIAL:
                    partial = 1;
                    break;
            }
        }
        else {
            left = 1;
        }
        final double score = userQuestionAttempt.score;
        final long timeTaken = userQuestionAttempt.timeTaken;

        final EntityMeasures measures = new EntityMeasures(attempts, correct, partial, incorrect, left,
                timeTaken, score);

        return finalizeQuestionAttempt(userQuestionAttempt.userId, question,
                userQuestionAttempt.parentEntity, measures, userQuestionAttempt.answerGiven,
                userQuestionAttempt.matrixAnswerGiven, userQuestionAttempt.attemptId);

    }

    private static boolean finalizeQuestionAttempt(String userId, Question question,
            SrcEntity parentEntity, EntityMeasures measures, List<String> answerGiven,
            Map<String, List<String>> matrixAnswerGiven, String attemptId) {

        Set<AcademicDimension> acadDims = AnalyticsUtils.getAcadDimensions(question.boardIds);
        return finalizeQuestionAttempt(userId, question, parentEntity, measures, answerGiven,
                matrixAnswerGiven, acadDims);
    }

    private static boolean finalizeQuestionAttempt(String userId, Question question,
            SrcEntity parentEntity, EntityMeasures measures, List<String> answerGiven,
            Map<String, List<String>> matrixAnswerGiven, Set<AcademicDimension> acadDims) {

        // NOTE: can be used for LEFT

        // update QuestionAnalytics
        final String answerGivenKey = AnswerGivenCount.toAnswerKey(question, answerGiven,
                matrixAnswerGiven);

        boolean addedQuestionAnalytics = QuestionAnalyticsDAO.INSTANCE.addAnalytics(
                question._getStringId(), parentEntity, measures, answerGivenKey);
        LOGGER.debug("finalizeQuestionAttempt addedQuestionAnalytics: " + addedQuestionAnalytics);

        // update UserAnalytics
        for (AcademicDimension acadDim : acadDims) {
            boolean added = UserAnalyticsDAO.INSTANCE.addAnalytics(userId, acadDim.type,
                    acadDim.id, measures);
            LOGGER.debug("finalizeQuestionAttempt acadDim: " + acadDim + ", added: " + added);
        }

        return true;
    }

    private static Map<String, Marks> getEntityQuestionMarksDistribution(SrcEntity entity,
            Map<String, Integer> acadDimnToTotalQusCountMap,
            Map<String, Integer> acadDimToTotalMarksMap) {

        Map<String, Marks> markDistribution = new HashMap<String, Marks>();

        if (entity.type != EntityType.TEST) {
            return markDistribution;
        }
        Test test = TestDAO.INSTANCE.getById(entity.id);
        if (test == null) {
            return markDistribution;
        }
        acadDimnToTotalQusCountMap.put(AcademicDimensionType.OVERALL.name(), test.qusCount);
        acadDimToTotalMarksMap.put(AcademicDimensionType.OVERALL.name(), test.totalMarks);
        if (test.metadata != null) {
            for (TestMetadata mdata : test.metadata) {
                acadDimnToTotalQusCountMap.put(mdata.id, mdata.qusCount);
                acadDimToTotalMarksMap.put(mdata.id, mdata.totalMarks);
                if (mdata.children != null) {
                    for (BoardQus child : mdata.children) {
                        acadDimnToTotalQusCountMap.put(child.id, child.qusCount);
                        acadDimToTotalMarksMap.put(child.id, child.totalMarks);
                    }
                }
                if (mdata.marks != null) {
                    markDistribution.putAll(mdata.marks);
                }
            }
        }
        return markDistribution;
    }

    private static void finalizeAssignmentAttempt(UserQuestionAttempt userQuestionAttempt,
            Question question, String orgId) throws VedantuException {

        UserEntityAttempt entityAttempt = UserEntityAttemptDAO.INSTANCE.getAttempt(
                userQuestionAttempt.userId, userQuestionAttempt.parentEntity.type,
                userQuestionAttempt.parentEntity.id);
        if (entityAttempt == null) {
            Assignment assignment = AssignmentDAO.INSTANCE
                    .getAssignment(userQuestionAttempt.parentEntity.id);
            List<String> qIds = assignment.__getAllQIds();
            addEntityAttempt(userQuestionAttempt.userId, orgId, userQuestionAttempt.parentEntity.type,
                    userQuestionAttempt.parentEntity.id, qIds, null, true, 0, 0, true, assignment,
                    EventType.INDEX_ASSIGNMENT);
        }

        int attempts = 1;
        int correct = 0;
        int incorrect = 0;
        int left = 0;
        int partial = 0;
        if (CollectionUtils.isNotEmpty(userQuestionAttempt.answerGiven)) {
            switch (userQuestionAttempt.isCorrect) {
                case CORRECT:
                    correct = 1;
                    break;
                case INCORRECT:
                    incorrect = 1;
                    break;
                case PARTIAL:
                    partial = 1;
                    break;
            }
        }
        else {
            left = 1;
        }
        final double score = userQuestionAttempt.score;
        final long timeTaken = userQuestionAttempt.timeTaken;

        final EntityMeasures measures = new EntityMeasures(attempts, correct, partial, incorrect, left,
                timeTaken, score);

        // update UserQuestionAnalytics
        UserQuestionAnalytics userQuestionAnalytics = AnalyticsUtils
                .addUserQuestionAnalytics(userQuestionAttempt);
        LOGGER.info("userQuestionAnalytics: " + userQuestionAnalytics);
        Set<AcademicDimension> acadDims = AnalyticsUtils.getAcadDimensions(question.boardIds);
        if (entityAttempt != null) {
            entityAttempt.endTime = System.currentTimeMillis();
            UserEntityAttemptDAO.INSTANCE.save(entityAttempt);
        }
        finalizeQuestionAttempt(userQuestionAttempt.userId, question,
                userQuestionAttempt.parentEntity, measures, userQuestionAttempt.answerGiven,
                userQuestionAttempt.matrixAnswerGiven, acadDims);
        for (AcademicDimension acadDim : acadDims) {
            AnalyticsUtils.updateUserEntityAnalytics(userQuestionAttempt.userId,
                    userQuestionAttempt.parentEntity, acadDim.type, acadDim.id, measures, 0, orgId);
        }
    }

    public static Map<String, EntityMeasures> reComputeUserEntityAnalyticsData(
            StartAttemptReq recomputeAnalyticsReq) throws VedantuException {

        UserEntityAttempt userEntityAttempt = UserEntityAttemptDAO.INSTANCE.getAttempt(
                recomputeAnalyticsReq.userId, recomputeAnalyticsReq.entityType,
                recomputeAnalyticsReq.entityId);
        if (userEntityAttempt == null) {
            LOGGER.error("no attempts found for userId : " + recomputeAnalyticsReq.userId
                    + " entityType: " + recomputeAnalyticsReq.entityType + ", entityId: "
                    + recomputeAnalyticsReq.entityId);
            throw new VedantuException(VedantuErrorCode.ATTEMPT_NOT_FOUND);
        }
        LOGGER.info("recomputing entity analytics for userId : " + recomputeAnalyticsReq.userId
                + " entityType: " + recomputeAnalyticsReq.entityType + ", entityId: "
                + recomputeAnalyticsReq.entityId);
        Map<String, Question> qIdToQuestionMap = QuestionDAO.INSTANCE
                .toInfosMap(QuestionDAO.INSTANCE.getByIds(ObjectIdUtils
                        .toObjectIds(userEntityAttempt.qIds)));

        List<UserQuestionAttempt> attempts = UserQuestionAttemptDAO.INSTANCE.getAttempts(
                userEntityAttempt.userId, userEntityAttempt.entity, userEntityAttempt.qIds, true);

        // also has OVERALL
        Map<String, Integer> acadDimToTotalQusCountMap = new HashMap<String, Integer>();

        // acadDimentionWise totalMarks map
        Map<String, Integer> acadDimToTotalMarksMap = new HashMap<String, Integer>();
        Map<String, Marks> qIdsToMarksMap = getEntityQuestionMarksDistribution(
                userEntityAttempt.entity, acadDimToTotalQusCountMap, acadDimToTotalMarksMap);

        // this will also compute the score corresponding to an entity for every
        // question
        Map<String, UserQuestionAttempt> qIdToFinalAttempts = AnalyticsUtils.toFinalAttempts(
                qIdsToMarksMap, attempts);

        // also has OVERALL
        Map<String, AcademicDimension> boardwiseAcadDimMap = AnalyticsUtils
                .getBoardswiseAcademicDimensions(qIdToQuestionMap);

        // also has OVERALL
        Map<String, EntityMeasures> boardwiseEntityMeasuresMap = new HashMap<String, EntityMeasures>();
        Map<String, EntityMeasures> questionWiseMeasuresMap = new HashMap<String, EntityMeasures>();
        for (Entry<String, Question> entry : qIdToQuestionMap.entrySet()) {

            Question question = entry.getValue();

            UserQuestionAttempt userQuestionAttempt = qIdToFinalAttempts.get(entry.getKey());

            // update UserQuestionAnalytics
            UserQuestionAnalytics userQuestionAnalytics = null;
            if (userQuestionAttempt != null) {
                List<UserQuestionAnalytics> userQuestionAnalyticsList = UserQuestionAnalyticsDAO.INSTANCE
                        .createQuery().filter("userId", userQuestionAttempt.userId)
                        .filter("parentEntity.type", userQuestionAttempt.parentEntity.type)
                        .filter("parentEntity.id", userQuestionAttempt.parentEntity.id)
                        .filter("qId", userQuestionAttempt.qId).asList();
                if (userQuestionAnalyticsList.size() > 0) {
                    userQuestionAnalytics = userQuestionAnalyticsList.get(0);
                    for (int i = 1; i < userQuestionAnalyticsList.size(); i++) {
                        LOGGER.info("markDeleted userQuestionAnalytics: "
                                + userQuestionAnalyticsList.get(i));
                        UserQuestionAnalyticsDAO.INSTANCE.updateState(
                                userQuestionAnalyticsList.get(i), VedantuRecordState.DELETED);
                    }
                }
            }
            LOGGER.info("processEndAttempt userQuestionAnalytics: " + userQuestionAnalytics);

            int attemptCount = 0;
            int correct = 0;
            int incorrect = 0;
            int partial = 0;
            int left = 1;
            double score = 0;
            long timeTaken = 0;
            if (userQuestionAttempt != null) {
                switch (userQuestionAttempt.isCorrect) {

                    case CORRECT:
                        correct = 1;
                        break;
                    case INCORRECT:
                        incorrect = 1;
                        break;
                    case PARTIAL:
                        partial = 1;
                        break;
                }
                attemptCount = 1;
                left = 0;
                timeTaken = userQuestionAttempt.timeTaken;
                score = userQuestionAttempt.score;
            } else {
                // if the user has not attempted the question and the question
                // was declared as
                // bonus question then allow him the marks of the question
                Marks mark = qIdsToMarksMap.get(question._getStringId());
                if (mark != null && mark.status == QuestionResultStatus.BONUS) {
                    score = mark.positive;
                }
            }
            final EntityMeasures qusMeasures = new EntityMeasures(attemptCount, correct, partial, incorrect,
                    left, timeTaken, score);
            questionWiseMeasuresMap.put(question._getStringId(), qusMeasures);
            updateEntityMeasuresMap(boardwiseEntityMeasuresMap, question.boardIds, qusMeasures);
        }
        if (boardwiseEntityMeasuresMap.isEmpty()) {
            return boardwiseEntityMeasuresMap;
        }
        long duplicateCount = 0;
        for (Entry<String, EntityMeasures> entry : boardwiseEntityMeasuresMap.entrySet()) {

            AcademicDimension acadDim = boardwiseAcadDimMap.get(entry.getKey());
            if (acadDim == null) {
                continue;
            }
            EntityMeasures measures = entry.getValue();
            measures.left = (acadDimToTotalQusCountMap.get(entry.getKey()) == null ? 0
                    : acadDimToTotalQusCountMap.get(entry.getKey()) - measures.attempts);
            double percentage = acadDimToTotalMarksMap.get(acadDim.id) == null ? 0
                    : (measures.score * 100)
                            / (acadDimToTotalMarksMap.get(acadDim.id) != null
                                    && acadDimToTotalMarksMap.get(acadDim.id) != 0 ? acadDimToTotalMarksMap
                                    .get(acadDim.id) : 1);

            // this will calculate the deviation of analytics{attempts, left,
            // correct etc}
            UserEntityAnalytics userEntityBoardAnalyttics = UserEntityAnalyticsDAO.INSTANCE
                    .getAnalytics(userEntityAttempt.userId, null, userEntityAttempt.entity,
                            acadDim.type, acadDim.id);

            if (acadDim.id.equals(AcademicDimensionType.OVERALL.name())) {
                LOGGER.debug("userEntityBoardAnalyttics: " + userEntityBoardAnalyttics
                        + ", current measures:" + measures);
                duplicateCount = (userEntityBoardAnalyttics.measures.attempts + userEntityBoardAnalyttics.measures.left)
                        / (measures.attempts + measures.left);
                LOGGER.info("duplicateCount for user[" + userEntityAttempt.userId + "]: "
                        + duplicateCount);
            }
            measures.attempts -= userEntityBoardAnalyttics.measures.attempts;
            measures.correct -= userEntityBoardAnalyttics.measures.correct;
            measures.partial -= userEntityBoardAnalyttics.measures.partial;
            measures.incorrect -= userEntityBoardAnalyttics.measures.incorrect;
            measures.left -= userEntityBoardAnalyttics.measures.left;
            measures.score -= userEntityBoardAnalyttics.measures.score;
            measures.timeTaken -= userEntityBoardAnalyttics.measures.timeTaken;

            LOGGER.info("correction measures : " + measures + ", scorePercentage: " + percentage);
            AnalyticsUtils.updateUserEntityAnalytics(userEntityAttempt.userId,
                    userEntityAttempt.entity, acadDim.type, acadDim.id, measures, percentage,
                    recomputeAnalyticsReq.orgId);
            if (userEntityAttempt.parent != null
                    && StringUtils.isNotEmpty(userEntityAttempt.parent.id)) {
                // if test has parent then, update analytics of parent test
                AnalyticsUtils.updateUserEntityAnalytics(userEntityAttempt.userId,
                        userEntityAttempt.parent, acadDim.type, acadDim.id, measures, percentage,
                        recomputeAnalyticsReq.orgId);
            }
        }
        // now fix the over all question analytics and user over all analytics

        duplicateCount--;
        LOGGER.info("duplicateCount for user[" + userEntityAttempt.userId + "]: " + duplicateCount);
        for (Entry<String, Question> entry : qIdToQuestionMap.entrySet()) {
            UserQuestionAttempt userQuestionAttempt = qIdToFinalAttempts.get(entry.getKey());
            Set<AcademicDimension> acadDims = AnalyticsUtils.getAcadDimensionsSubset(
                    qIdToQuestionMap.get(entry.getKey()).boardIds, boardwiseAcadDimMap);
            EntityMeasures qusMeasures = questionWiseMeasuresMap.get(entry.getValue()
                    ._getStringId());
            // update UserAnalytics & QuestionAnalytics
            if (userQuestionAttempt != null && qusMeasures != null) {
                // if the question is attempted multiple time than this method
                // is need to be called
                // multiple times
                final EntityMeasures qusMeasuresCorrection = new EntityMeasures(
                        -(int) (qusMeasures.attempts * duplicateCount),
                        -(int) (qusMeasures.correct * duplicateCount),
                        -(int) (qusMeasures.partial * duplicateCount),
                        -(int) (qusMeasures.incorrect * duplicateCount),
                        -(int) (qusMeasures.left * duplicateCount),
                        -(int) (qusMeasures.timeTaken * duplicateCount),
                        -(int) (qusMeasures.score * duplicateCount));
                finalizeQuestionAttempt(userQuestionAttempt.userId,
                        qIdToQuestionMap.get(entry.getKey()), userQuestionAttempt.parentEntity,
                        qusMeasuresCorrection, userQuestionAttempt.answerGiven,
                        userQuestionAttempt.matrixAnswerGiven, acadDims);
            }
        }
        return boardwiseEntityMeasuresMap;
    }

    private static void processEndAttempt(UserEntityAttempt userEntityAttempt, String orgId) {

        LOGGER.info("processing endAttempt for userEntityAttempt: " + userEntityAttempt);
        Map<String, Question> qIdToQuestionMap = QuestionDAO.INSTANCE
                .toInfosMap(QuestionDAO.INSTANCE.getByIds(ObjectIdUtils
                        .toObjectIds(userEntityAttempt.qIds)));

        List<UserQuestionAttempt> attempts = UserQuestionAttemptDAO.INSTANCE.getAttempts(
                userEntityAttempt.userId, userEntityAttempt.entity, userEntityAttempt.qIds, true);

        // also has OVERALL
        Map<String, Integer> acadDimToTotalQusCountMap = new HashMap<String, Integer>();

        // acadDimentionWise totalMarks map
        Map<String, Integer> acadDimToTotalMarksMap = new HashMap<String, Integer>();
        Map<String, Marks> qIdsToMarksMap = getEntityQuestionMarksDistribution(
                userEntityAttempt.entity, acadDimToTotalQusCountMap, acadDimToTotalMarksMap);

        LOGGER.info("qusMarks Map : " + qIdsToMarksMap);
        // this will also compute the score corresponding to an entity for every
        // question
        Map<String, UserQuestionAttempt> qIdToFinalAttempts = AnalyticsUtils.toFinalAttempts(
                qIdsToMarksMap, attempts);

        // also has OVERALL
        Map<String, AcademicDimension> boardwiseAcadDimMap = AnalyticsUtils
                .getBoardswiseAcademicDimensions(qIdToQuestionMap);

        // also has OVERALL
        Map<String, EntityMeasures> boardwiseEntityMeasuresMap = new HashMap<String, EntityMeasures>();
        Test test=null;
        boolean bonusAllowed=true;
        if(userEntityAttempt.entity.type==EntityType.TEST){
        	test=TestDAO.INSTANCE.getById(userEntityAttempt.entity.id);
        	if(test.isNTAPattern){
        		if(userEntityAttempt.attemptedQIds.size()==test.qusCount){
        			bonusAllowed=false;
        		}
        	}
        }

        for (Entry<String, Question> entry : qIdToQuestionMap.entrySet()) {

            Question question = entry.getValue();

            UserQuestionAttempt userQuestionAttempt = qIdToFinalAttempts.get(entry.getKey());
            // update UserQuestionAnalytics
            UserQuestionAnalytics userQuestionAnalytics = AnalyticsUtils
                    .addUserQuestionAnalytics(userQuestionAttempt);
            LOGGER.info("processEndAttempt userQuestionAnalytics: " + userQuestionAnalytics);

            int attemptCount = 0;
            int correct = 0;
            int partial = 0;
            int incorrect = 0;
            int left = 1;
            double score = 0;
            long timeTaken = 0;
            if (userQuestionAttempt != null) {
                attemptCount = 1;
                switch (userQuestionAttempt.isCorrect) {
                    case CORRECT:
                        correct = 1;
                        break;
                    case INCORRECT:
                        incorrect = 1;
                        break;
                    case PARTIAL:
                        partial = 1;
                        break;
                }
                left = 0;
                timeTaken = userQuestionAttempt.timeTaken;
                score = userQuestionAttempt.score;
            } else {
                // if the user has not attempted the question and the question
                // was declared as
                // bonus question then allow him the marks of the question
            	if(bonusAllowed){
                    Marks mark = qIdsToMarksMap.get(question._getStringId());
                    if (mark != null && mark.status == QuestionResultStatus.BONUS) {
                    	if(test.isNTAPattern){
                        	String key="";
                        	for(TestMetadata metadata:test.metadata){
                               	if(metadata.qIds.contains(question.id)){
                               		key=metadata.id;
                               	}
                             }
                    		 if(userEntityAttempt.mapping.get(key)==null){
                        		 for(String keys:userEntityAttempt.mapping.keySet()){
                        			 String[] list=keys.split("_");
                        			 Set<String> mySet = new HashSet<String>(Arrays.asList(list));
                        			 if(mySet.contains(key)){
                        				 key= keys;
                        			 }
                        		 }

                    		 }
                    		 int count=userEntityAttempt.mapping.get(key).get(question.type);
                    		 LOGGER.info("userEntityAttempt.mapping.get(key).get(question.type) : "+count);
                    		 if(count>0){
                    			 // if count is greater than zero, which means a bonus mark can be assigned to this student
                                 score = mark.positive;
                                 // and after a bonus mark is assigned, count should be decremented by 1 
                                 //and here we are not adding the question to list of attemptedQids of userattemptentity.
                                 count=count-1;
                                 userEntityAttempt.mapping.get(key).put(question.type,count);
                                 UserEntityAttemptDAO.INSTANCE.save(userEntityAttempt);
                                 LOGGER.info("userEntityAttempt.mapping.get(key).get(question.type) : "+
                                 userEntityAttempt.mapping.get(key).get(question.type));
                                 
                                 
                    		 }
                    	}
                    	else{
                            score = mark.positive;
                    	}
                    }

            	}
            }

            final EntityMeasures qusMeasures = new EntityMeasures(attemptCount, correct,
                    partial, incorrect, left, timeTaken, score);
            updateEntityMeasuresMap(boardwiseEntityMeasuresMap, question.boardIds, qusMeasures);

            Set<AcademicDimension> acadDims = AnalyticsUtils.getAcadDimensionsSubset(
                    qIdToQuestionMap.get(entry.getKey()).boardIds, boardwiseAcadDimMap);

            // update UserAnalytics & QuestionAnalytics
            if (userQuestionAttempt != null) {
                finalizeQuestionAttempt(userQuestionAttempt.userId,
                        qIdToQuestionMap.get(entry.getKey()), userQuestionAttempt.parentEntity,
                        qusMeasures, userQuestionAttempt.answerGiven,
                        userQuestionAttempt.matrixAnswerGiven, acadDims);
            }
        }
        if (boardwiseEntityMeasuresMap.isEmpty()) {
            return;
        }
        for (Entry<String, EntityMeasures> entry : boardwiseEntityMeasuresMap.entrySet()) {

            AcademicDimension acadDim = boardwiseAcadDimMap.get(entry.getKey());
            if (acadDim == null) {
                continue;
            }
            EntityMeasures measures = entry.getValue();
            measures.left = acadDimToTotalQusCountMap.get(entry.getKey()) == null ? 0
                    : acadDimToTotalQusCountMap.get(entry.getKey()) - measures.attempts;
            double percentage = acadDimToTotalMarksMap.get(acadDim.id) == null ? 0
                    : (measures.score * 100)
                            / (acadDimToTotalMarksMap.get(acadDim.id) != null
                                    && acadDimToTotalMarksMap.get(acadDim.id) != 0 ? acadDimToTotalMarksMap
                                    .get(acadDim.id) : 1);
            AnalyticsUtils
                    .updateUserEntityAnalytics(userEntityAttempt.userId, userEntityAttempt.entity,
                            acadDim.type, acadDim.id, measures, percentage, orgId);
            if (userEntityAttempt.parent != null
                    && StringUtils.isNotEmpty(userEntityAttempt.parent.id)) {
                // if test has parent then, update analytics of parent test
                AnalyticsUtils.updateUserEntityAnalytics(userEntityAttempt.userId,
                        userEntityAttempt.parent, acadDim.type, acadDim.id, measures, percentage,
                        orgId);
            }
            AnalyticsUtils.updateHighScores(userEntityAttempt.userId, userEntityAttempt.entity,
                    userEntityAttempt.parent, measures.score, acadDim);
        }
    }

    private static void updateEntityMeasuresMap(
            Map<String, EntityMeasures> boardwiseEntityMeasuresMap, Collection<String> brdIds,
            EntityMeasures questionMeasures) {

        updateEntityMeasuresMap(boardwiseEntityMeasuresMap, AcademicDimensionType.OVERALL.name(),
                questionMeasures);
        for (String brdId : brdIds) {
            updateEntityMeasuresMap(boardwiseEntityMeasuresMap, brdId, questionMeasures);
        }
    }

    private static void updateEntityMeasuresMap(
            Map<String, EntityMeasures> boardwiseEntityMeasuresMap, String brdId,
            EntityMeasures questionMeasures) {

        EntityMeasures entityMeasures = boardwiseEntityMeasuresMap.get(brdId);
        if (entityMeasures == null) {
            entityMeasures = new EntityMeasures();
            boardwiseEntityMeasuresMap.put(brdId, entityMeasures);
        }
        entityMeasures.attempts += questionMeasures.attempts;
        entityMeasures.correct += questionMeasures.correct;
        entityMeasures.partial += questionMeasures.partial;
        entityMeasures.incorrect += questionMeasures.incorrect;
        entityMeasures.left += questionMeasures.left;
        entityMeasures.score += questionMeasures.score;
        entityMeasures.timeTaken += questionMeasures.timeTaken;
    }

    private static String __getUserEntityAnalyticsMapKey(String entityId, String userId,
            String acadDimId) {

        return entityId + "_" + userId + "_" + acadDimId;
    }

    private static String __getUserEntityAttemptMapKey(String entityId, String userId) {

        return entityId + "_" + userId;
    }

    private static String __getEntityAnalyticsMapKey(String entityId, String acadDimId) {

        return entityId + "_" + acadDimId;
    }

    /**
     *
     * @return Map(of entityId_acadDimId to overAll topper);
     */
    // TODO: take this result from entityHighScore table and save user timeTaken
    // in entityHighScore
    // table
    private static Map<String, EntityTopper> getEntityToppers(String orgId,
            Collection<String> entityIds, String acadDimId,
            Map<String, EntityAnalyticsBasicInfo> entityAnalyticContentInfo) {

        Map<String, EntityTopper> topperMap = new HashMap<String, EntityTopper>();

        DBObject match = new BasicDBObject("entity.id", new BasicDBObject(MongoManager.IN_QUERY,
                entityIds.toArray()));
        // match.put(ConstantsGlobal.USER_IDS, new
        // BasicDBObject(MongoManager.NE_QUERY, null));
        match.put(ConstantsGlobal.ACAD_DIM_DOT_ID, acadDimId);
        match.put(ConstantsGlobal.ORG_ID, orgId);

        DBObject sort = MongoManager.getSortQuery("_id.score", SortOrder.DESC.name());
        sort.putAll(MongoManager.getSortQuery("_id.timeTaken", SortOrder.ASC.name()));

        DBObject group1 = (DBObject) JSON
                .parse("{$group : {_id: {entity: \"$entity\", score: \"$measures.score\", timeTaken : \"$measures.timeTaken\"}, userId: {$first: \"$userId\"},score: {$first: \"$measures.score\"}}}");

        DBObject group2 = (DBObject) JSON
                .parse("{$group : {_id: {entity: \"$_id.entity\"}, score: {$first : \"$score\"},userId: {$first: \"$userId\"}}}");
        // group.put(ConstantsGlobal.SCORE, new BasicDBObject("$first",
        // "$score"));
        // group.put(ConstantsGlobal.USER_IDS, new BasicDBObject("$first",
        // "$userIds"));

        AggregationOutput aggregationOutput = UserEntityAnalyticsDAO.INSTANCE.aggregate(
                new BasicDBObject("$match", match), group1, new BasicDBObject("$sort", sort),
                group2);
        // OLD output will be of following format
        // {
        // "_id" : "5187b95c44ae2de1e1b93ba2",
        // "score" : 32,
        // "userIds" : [
        // "5177c99244ae06fcac01c522",
        // "5172516744ae884457fb6a3b"
        // ],
        // "acadDimId" : "OVERALL"
        // }

        // new output
        // {
        // "_id" : {
        // "entity" : {
        // "type" : "TEST",
        // "id" : "519e19f6ccf21eda84778a07"
        // }
        // },
        // "score" : -2,
        // "userId" : "5172516744ae884457fb6a3b"
        // }
        Set<String> userIds = new HashSet<String>();
        for (DBObject d : aggregationOutput.results()) {
            String topperId = (String) d.get(ConstantsGlobal.USER_ID);
            if (StringUtils.isNotEmpty(topperId)) {
                userIds.add(topperId);
            }
        }
        Map<String, ModelBasicInfo> userInfoMap = getUserInfoMap(orgId, userIds);
        for (DBObject d : aggregationOutput.results()) {
            DBObject entity = (DBObject) ((DBObject) d.get(ConstantsGlobal._ID))
                    .get(ConstantsGlobal.ENTITY);
            String entityId = (String) entity.get(ConstantsGlobal.ID);
//          The score obtained may be int or double,so casting it to string;
            String scoreObj =  (d.get(ConstantsGlobal.SCORE) == null ? "0" :  d
                    .get(ConstantsGlobal.SCORE).toString());
            int score = (int) Double.parseDouble(scoreObj);
            float percentage = entityAnalyticContentInfo.get(entityId) != null
                    && entityAnalyticContentInfo.get(entityId).totalMarks != 0 ? (score * 100)
                    / entityAnalyticContentInfo.get(entityId).totalMarks : 0;
            String topperId = (String) d.get(ConstantsGlobal.USER_ID);
            if (StringUtils.isNotEmpty(topperId)) {
                EntityTopper topper = new EntityTopper((UserInfo) userInfoMap.get(topperId),
                        percentage);
                topperMap.put(entityId, topper);
            }
            // we will take the 1st topper from this list, as we are only
            // showing one topper
        }
        return topperMap;
    }

    public static GetEntityResultAnalyticsRes resetStudentTest(
            GetEntityResultAnalyticsReq getAnalyticsResultReq) throws VedantuException {
        // TODO Auto-generated method stub
        if(getAnalyticsResultReq.entity.type.equals(EntityType.TEST)){
            if(getAnalyticsResultReq.studentUserId.isEmpty()){
                throw new VedantuException(VedantuErrorCode.INVALID_USER_ID);
            }else if(getAnalyticsResultReq.entity.id.isEmpty()){
                throw new VedantuException(VedantuErrorCode.INVALID_ENTITY_ID);
            }

            if(UserEntityAttemptDAO.INSTANCE.getAttempt(getAnalyticsResultReq.studentUserId, getAnalyticsResultReq.entity.type, getAnalyticsResultReq.entity.id).processed){
                // Remove entry from entityUserActionMapping
                EntityUserActionDAO.INSTANCE.removeEntityUserActionMapping(getAnalyticsResultReq.studentUserId, UserActionType.ATTEMPTED, getAnalyticsResultReq.entity);
                // Update test measures for over_all analytics
                UserEntityAnalyticsDAO.INSTANCE.updateParticularTestOverallAnalytics(getAnalyticsResultReq.studentUserId, getAnalyticsResultReq.entity.id);
                // Remove entry from userEntityAttempts
                UserEntityAttemptDAO.INSTANCE.removeUserEntityAttempt(getAnalyticsResultReq.studentUserId, getAnalyticsResultReq.entity);
                // Remove entry from userEntityAnalytics
                UserEntityAnalyticsDAO.INSTANCE.removeUserEntityAnalytics(getAnalyticsResultReq.studentUserId, getAnalyticsResultReq.entity);
                // Remove entry from userQuestionAnalytics
                UserQuestionAnalyticsDAO.INSTANCE.removeUserQuestionAnalytics(getAnalyticsResultReq.studentUserId, getAnalyticsResultReq.entity);
                // Remove entry from userQuestionAttempts
                UserQuestionAttemptDAO.INSTANCE.removeUserQuestionAttempt(getAnalyticsResultReq.studentUserId, getAnalyticsResultReq.entity);
            }else{
                throw new VedantuException(VedantuErrorCode.ANALYTICS_GENERATION_UNDER_PROCESS);
            }

        }else{
            LOGGER.debug("Invalid Entity type");
            throw new VedantuException(VedantuErrorCode.INVALID_ENTITY_TYPE);
        }

        return new GetEntityResultAnalyticsRes();
    }

	public static GetEntityResultAnalyticsRes regenerateStudentTestAnalytics(
			GetEntityResultAnalyticsReq getAnalyticsResultReq)
			throws VedantuException {
		if (getAnalyticsResultReq.entity.type.equals(EntityType.TEST)) {
			if (getAnalyticsResultReq.studentUserId.isEmpty()) {
				throw new VedantuException(VedantuErrorCode.INVALID_USER_ID);
			} else if (getAnalyticsResultReq.entity.id.isEmpty()) {
				throw new VedantuException(VedantuErrorCode.INVALID_ENTITY_ID);
			}
			// Getting the user attempt from UserEntityAttempt
			UserEntityAttempt userAttempt = UserEntityAttemptDAO.INSTANCE
					.getAttempt(getAnalyticsResultReq.studentUserId,
							getAnalyticsResultReq.entity.type,
							getAnalyticsResultReq.entity.id);
			// If attempt is not found
			if (userAttempt == null) {
				throw new VedantuException(VedantuErrorCode.ATTEMPT_NOT_FOUND);
			}

			if (userAttempt.processed) {
				// Update test measures for over_all analytics
				UserEntityAnalyticsDAO.INSTANCE
						.updateParticularTestOverallAnalytics(
								getAnalyticsResultReq.studentUserId,
								getAnalyticsResultReq.entity.id);
				// Remove entry from userEntityAnalytics
				UserEntityAnalyticsDAO.INSTANCE.removeUserEntityAnalytics(
						getAnalyticsResultReq.studentUserId,
						getAnalyticsResultReq.entity);
				// Remove entry from userQuestionAnalytics
				UserQuestionAnalyticsDAO.INSTANCE.removeUserQuestionAnalytics(
						getAnalyticsResultReq.studentUserId,
						getAnalyticsResultReq.entity);

				long endTime = 0;
				// Making ideal condition as of ending the test
				userAttempt.processed = false;
				userAttempt.testStatus = "ONGOING";
				userAttempt.finished = false;
				endTime = userAttempt.endTime;
				userAttempt.endTime = 0;
				if(getAnalyticsResultReq.entity.type.equals(EntityType.TEST)){
					Test test =TestDAO.INSTANCE.getTest(getAnalyticsResultReq.entity.id);
		        	LOGGER.info("test.isNTAPattern : "+test.isNTAPattern);
		        	if(test.isNTAPattern){
		            	addEntityAttemptMappingForNTAPattern(userAttempt,test);
		            	userAttempt.attemptedQIds.removeAll(userAttempt.attemptedQIds);
		            	LOGGER.info("userAttempt.attemptedQIds : "+userAttempt.attemptedQIds);
		        	}					
				}
				UserEntityAttemptDAO.INSTANCE.save(userAttempt);

				// Getting all the question attempts of particular user for
				// particular test.
				List<UserQuestionAttempt> userQuestionAttempts = UserQuestionAttemptDAO.INSTANCE
						.getAllAttempts(getAnalyticsResultReq.studentUserId,
								getAnalyticsResultReq.entity);
				LOGGER.debug("userQuestionAttempts for regenerateAnalytics"
						+ Arrays.toString(userQuestionAttempts.toArray()));
				UserQuestionAttemptDAO.INSTANCE.removeUserQuestionAttempt(
						getAnalyticsResultReq.studentUserId,
						getAnalyticsResultReq.entity);
				LOGGER.debug("HuserQuestionAttempts for regenerateAnalytics after deleting attempts"
						+ Arrays.toString(userQuestionAttempts.toArray()));
				if (!userQuestionAttempts.isEmpty()) {
					RecordAttemptReq recordAttemptReq = new RecordAttemptReq();
					recordAttemptReq.callingUserId = getAnalyticsResultReq.studentUserId;
					recordAttemptReq.userId = getAnalyticsResultReq.studentUserId;
					recordAttemptReq.entityId = getAnalyticsResultReq.entity.id;
					recordAttemptReq.entityType = getAnalyticsResultReq.entity.type;
					recordAttemptReq.setName = null;
					recordAttemptReq.attemptId = userAttempt._getStringId();

					for (UserQuestionAttempt userQuestionAttempt : userQuestionAttempts) {
						// changing is finalized to true for further operations
						// userQuestionAttempt.isFinalized = false;
						// UserQuestionAttemptDAO.INSTANCE
						// .save(userQuestionAttempt);
						recordAttemptReq.qId = userQuestionAttempt.qId;
						recordAttemptReq.answerGiven = userQuestionAttempt.answerGiven;
						recordAttemptReq.timeTaken = userQuestionAttempt.timeTaken;
						recordAttemptReq.matrixAnswer = userQuestionAttempt.matrixAnswerGiven;

						AnalyticsManager.recordAttempt(recordAttemptReq);
						if(userQuestionAttempt.type == QuestionType.SUBJECTIVE){
						    // Grade subjection question
						    GradeTestSubjectiveQuestionReq req = new GradeTestSubjectiveQuestionReq();
						    req.attemptId = userQuestionAttempt.attemptId;
						    req.qId = userQuestionAttempt.qId;
						    req.testId = userQuestionAttempt.parentEntity.id;
						    req.isCorrect = userQuestionAttempt.isCorrect;
						    req.score = userQuestionAttempt.score;
						    gradeTestSubjectiveQuestion(req);
						}
					}
				}

				// Generating new end attempt request
				EndAttemptReq endAttemptReq = new EndAttemptReq(
						getAnalyticsResultReq.studentUserId,
						getAnalyticsResultReq.studentUserId,
						getAnalyticsResultReq.entity.id,
						getAnalyticsResultReq.entity.type, null,
						userAttempt._getStringId(), userAttempt.orgId);
				endAttempt(endAttemptReq, endTime);

			} else {
				throw new VedantuException(
						VedantuErrorCode.ANALYTICS_GENERATION_UNDER_PROCESS);
			}

		} else {
			LOGGER.debug("Invalid Entity type");
			throw new VedantuException(VedantuErrorCode.INVALID_ENTITY_TYPE);
		}

		return new GetEntityResultAnalyticsRes();

	}

    public static RecordAttemptRes testStatus(StartAttemptReq recordAttemptReq) throws VedantuException {
        // TODO Auto-generated method stub
        RecordAttemptRes res = new RecordAttemptRes();
        res.isOnline = true;
        UserEntityAttempt testStatus = _entityStatus(recordAttemptReq.attemptId);
        if(testStatus.testStatus.equals("FINISHED")){
            LOGGER.error("Entity is Finished");
            throw new VedantuException(VedantuErrorCode.TEST_ENDED);
        }
        if(testStatus.testStatus.equals("PAUSED")){
            LOGGER.error("Entity is PAUSED");
            throw new VedantuException(VedantuErrorCode.TEST_PAUSED);
        }
        if(testStatus.testStatus.equals("RESUMED")){
            LOGGER.error("Entity is RESUMED");
            throw new VedantuException(VedantuErrorCode.TEST_PAUSED_RESUME_AGAIN);
        }
        testStatus.timeLeft = recordAttemptReq.timeLeft;
        UserEntityAttemptDAO.INSTANCE.save(testStatus);
        return res;
    }

    public static GetTestInfoRes _testStatus(GetTestInfoReq getTestReq) {
        // TODO Auto-generated method stub
        LOGGER.debug("Inside _testStatus");
        GetTestInfoRes testStatus = new GetTestInfoRes();
        LOGGER.debug("_testStatus log :: userId is "+ getTestReq.userId);
        LOGGER.debug("_testStatus log :: TestId is "+getTestReq.id);
        UserEntityAttempt userEntityAttempt = UserEntityAttemptDAO.INSTANCE.getAttempt(getTestReq.userId, EntityType.TEST, getTestReq.id);
        if(userEntityAttempt == null){
            LOGGER.debug("_testStatus log :: userEntityAttempt is null");
            testStatus.testStatus = "NOT_ATTEMPTED";
            return testStatus;
        }else{
            LOGGER.debug("_testStatus log :: userEntityAttempt is not null");
            if(TestDAO.INSTANCE.getById(getTestReq.id).autoResumeTest && userEntityAttempt.testStatus.equals("ONGOING")){
                userEntityAttempt.testStatus = "RESUMED";
                UserEntityAttemptDAO.INSTANCE.save(userEntityAttempt);
                testStatus.testStatus = userEntityAttempt.testStatus;
            }else{
                testStatus.testStatus = userEntityAttempt.testStatus;
            }
            testStatus.processed = userEntityAttempt.processed;
            return testStatus;
        }
    }

    public static EndAttemptRes pauseStudentAttempt(StartAttemptReq pauseAttemptReq) throws VedantuException {
        // TODO Auto-generated method stub
        if(pauseAttemptReq.entityType.equals(EntityType.TEST)){
            if(pauseAttemptReq.studentUserId.isEmpty()){
                throw new VedantuException(VedantuErrorCode.INVALID_USER_ID);
            }else if(pauseAttemptReq.entityId.isEmpty()){
                throw new VedantuException(VedantuErrorCode.INVALID_ENTITY_ID);
            }
            // Remove entry from userEntityAttempts
            UserEntityAttempt userEntityAttempt = UserEntityAttemptDAO.INSTANCE.getAttempt(pauseAttemptReq.studentUserId, pauseAttemptReq.entityType, pauseAttemptReq.entityId);
            if(userEntityAttempt.finished){
                throw new VedantuException(VedantuErrorCode.TEST_ENDED);
            }else{
                userEntityAttempt.testStatus = "PAUSED";
//                userEntityAttempt.timeLeft = userEntityAttempt.timeLeft - (System.currentTimeMillis() - userEntityAttempt.lastUpdated);
            }
            UserEntityAttemptDAO.INSTANCE.save(userEntityAttempt);
        }else{
            LOGGER.debug("Invalid Entity type");
            throw new VedantuException(VedantuErrorCode.INVALID_ENTITY_TYPE);
        }
        return null;
    }

    public static EndAttemptRes resumeStudentAttempt(StartAttemptReq resumeAttemptReq) throws VedantuException {
        // TODO Auto-generated method stub
        if(resumeAttemptReq.entityType.equals(EntityType.TEST)){
            if(resumeAttemptReq.userId.isEmpty()){
                throw new VedantuException(VedantuErrorCode.INVALID_USER_ID);
            }else if(resumeAttemptReq.entityId.isEmpty()){
                throw new VedantuException(VedantuErrorCode.INVALID_ENTITY_ID);
            }
            // Remove entry from userEntityAttempts
            UserEntityAttempt userEntityAttempt = UserEntityAttemptDAO.INSTANCE.getAttempt(resumeAttemptReq.studentUserId, resumeAttemptReq.entityType, resumeAttemptReq.entityId);
            if(userEntityAttempt.finished){
                throw new VedantuException(VedantuErrorCode.TEST_ENDED);
            }else{
                userEntityAttempt.testStatus = "RESUMED";
            }
            UserEntityAttemptDAO.INSTANCE.save(userEntityAttempt);
        }else{
            LOGGER.debug("Invalid Entity type");
            throw new VedantuException(VedantuErrorCode.INVALID_ENTITY_TYPE);
        }
        return null;
    }

    public static GradeTestSubjectiveQuestionRes gradeTestSubjectiveQuestion(GradeTestSubjectiveQuestionReq req) throws VedantuException {
        GradeTestSubjectiveQuestionRes res = new GradeTestSubjectiveQuestionRes();
        UserEntityAttempt userEntityAttempt = UserEntityAttemptDAO.INSTANCE.getById(req.attemptId);
        if (userEntityAttempt == null) {
			throw new VedantuException(VedantuErrorCode.ATTEMPT_NOT_FOUND);
		}
		if (!userEntityAttempt.finished) {
			throw new VedantuException(
					VedantuErrorCode.ATTEMPT_IN_PROGRESS,
					"Test is not finished...Either end the test or wait until the student finishes the test");
		}
        UserQuestionAttempt qAttempt = UserQuestionAttemptDAO.INSTANCE.getFinilazedQuestionAttempt(req.attemptId, req.qId, true);
        if (qAttempt != null) {
            qAttempt.isCorrect = req.isCorrect;
            qAttempt.score = req.score;
            UserQuestionAttemptDAO.INSTANCE.save(qAttempt);
            if (allSubjectiveQuestionAttemptsGraded(req) == true && !userEntityAttempt.processed) {
                //Generate END_TEST event
                Test test = TestDAO.INSTANCE.getById(req.testId);
                long duration = test.duration;
                EndTestDetails endTestDetails = new EndTestDetails(userEntityAttempt._getStringId(),
                        userEntityAttempt.userId, userEntityAttempt.entity.id,
                        userEntityAttempt.entity.type, null,
                        userEntityAttempt.timeCreated, duration, userEntityAttempt.orgId, "USER");
                long processTime = userEntityAttempt.timeCreated;
                generateEventAysc(userEntityAttempt.userId, endTestDetails, EventType.END_TEST, processTime);
            }
            res.success = true;
        }
        return res;
    }

    private static boolean allSubjectiveQuestionAttemptsGraded(GradeTestSubjectiveQuestionReq req) {
        // TODO Auto-generated method stub
        List<UserQuestionAttempt> allAttempts = UserQuestionAttemptDAO.INSTANCE.getRemainingSubjectiveAttempts(req.attemptId);
        if(CollectionUtils.size(allAttempts) > 0){
            return false;
        }
        return true;
    }
}
