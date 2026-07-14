package com.vedantu.eventbus.processors.cmds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.vedantu.board.pojos.BoardBasicInfo;
import com.vedantu.cmds.daos.TempParsedDataDAO;
import com.vedantu.cmds.models.TempParsedDATA;
import com.vedantu.cmds.models.event.details.OfflineTestResultUploadDetails;
import com.vedantu.cmds.pojos.content.tests.OfflineQInfo;
import com.vedantu.cmds.pojos.content.tests.OfflineTestData;
import com.vedantu.cmds.utils.OfflineTestUtils;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.daos.EntityOperationStatusDAO;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.models.mongo.EntityOperationStatus;
import com.vedantu.commons.pojos.ScheduleInfo;
import com.vedantu.commons.utils.ObjectMapperUtils;
import com.vedantu.content.daos.TestDAO;
import com.vedantu.content.enums.QuestionType;
import com.vedantu.content.managers.AnalyticsManager;
import com.vedantu.content.models.tests.Test;
import com.vedantu.content.pojos.requests.analytics.RecordAttemptReq;
import com.vedantu.content.pojos.requests.analytics.SyncTabletAnalyticsReq;
import com.vedantu.content.pojos.tests.SimplifiedBoardNames;
import com.vedantu.content.pojos.tests.TestDetails;
import com.vedantu.content.pojos.tests.TestMetadata;
import com.vedantu.events.models.Event;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.apis.IProcessor;
import com.vedantu.events.task.enums.Status;
import com.vedantu.mongo.MongoManager.SortOrder;
import com.vedantu.organization.daos.OrgMemberDAO;
import com.vedantu.organization.daos.OrgProgramDAO;
import com.vedantu.organization.models.OrgMember;
import com.vedantu.organization.models.OrgProgram;

public class OfflineTestResultProcessor implements IProcessor {
	private static final ALogger LOGGER = Logger
			.of(OfflineTestResultProcessor.class);

	public OfflineTestResultProcessor() {
		super();
	}

	@Override
	public Status process(IConsumable consumable) {
		if (consumable == null) {
			return Status.FAILURE;
		}

		Event event = (Event) consumable;
		OfflineTestResultUploadDetails details = (OfflineTestResultUploadDetails) event
				.fetchEventDetails();
		LOGGER.info("fetched eventDetails " + details);

		EntityOperationStatus status = EntityOperationStatusDAO.INSTANCE
				.getById(details.jobId);
		String errorCode = null;
		try {
		    if((details.programId == null || details.programId.equalsIgnoreCase("null")) && (details.testId != null || !details.testId.equalsIgnoreCase("null"))){
		        DBObject query = new BasicDBObject("uuid", details.uuid);
		        query.put("sheetId", 1);
		        DBCursor cursor = TempParsedDataDAO.INSTANCE.getCollection().find(query);
		        cursor.sort(new BasicDBObject("rowNo", SortOrder.ASC.getValue()));
		        int rowNum = 1;
		        while(cursor.hasNext()){
		            rowNum++;
		            TempParsedDATA row = ObjectMapperUtils.convertToVedantuBaseModel(cursor.next(),TempParsedDATA.class);
		            LOGGER.debug("Reading row number: " + rowNum + " for sheet : " + row.sheetName);
		            if (row.data == null) {
		                LOGGER.error("no data found at rowNo:" + rowNum + ", row: " + row);
		                continue;
		            }
		            SyncTabletAnalyticsReq req  = constructReq(row,details);
		            AnalyticsManager.syncTabletAnalytics(req);
		            status.numOfStepsCompleted++;
	                EntityOperationStatusDAO.INSTANCE.save(status);
	                if (status.message == null) {
	                    status.message = StringUtils.EMPTY;
	                }
//                  To check progres bar functionality in frontend.
//	                Thread.sleep(5000);
		        }
		    }else{
		        OrgProgram orgProgram = OrgProgramDAO.INSTANCE.getProgramById(
	                    details.orgId, details.programId);
	            Map<String, OfflineTestData> offlineTestDataMap = OfflineTestUtils
	                    .collectTestMetadataData(details.sheetNames, details.uuid);
	            LOGGER.info("offlineTestDataMap : " + offlineTestDataMap);
	            // when there are paper-1 and paper-2 type format is there than
	            // a testGroup {TEST} will be created with testGroupName. when
	            // no test group than only one test will be created with test
	            // name=testGroupName=testCode
	            String testGroupName = StringUtils.substringAfter(
	                    details.sheetNames.get(0), "_");
	            Map<String, Set<String>> brdIdToQidsMap = new HashMap<String, Set<String>>();

	            Map<String, BoardBasicInfo> brdNameToBoardInfoMap = OfflineTestUtils
	                    .validateAndUpdateTestMetadata(offlineTestDataMap,
	                            details.orgId, details.programId, brdIdToQidsMap);
	            ScheduleInfo scheduleInfo = null;
	            Map<String, OfflineQInfo> qCodeToQusMap = OfflineTestUtils
	                    .createTestAndQuestions(details.userId, details.orgId,
	                            orgProgram, offlineTestDataMap,
	                            brdNameToBoardInfoMap, testGroupName, scheduleInfo);
	            status.numOfStepsCompleted += qCodeToQusMap.size();
	            EntityOperationStatusDAO.INSTANCE.save(status);
	            if (status.message == null) {
	                status.message = StringUtils.EMPTY;
	            }
	            LOGGER.debug("offlineTestDataMap : " + offlineTestDataMap);
	            OfflineTestUtils.collectAndProcessUserTestData(details.orgId,
	                    details.programId, details.sheetNames, offlineTestDataMap,
	                    qCodeToQusMap, details.uuid, status);
		    }
		} catch (Exception e) {
			errorCode = e.getMessage();
			LOGGER.error(errorCode, e);
		} catch (VedantuException e) {
			errorCode = e.getMessage();
			LOGGER.error(errorCode, e);
		}

		status.errorCode = errorCode == null ? StringUtils.EMPTY : errorCode;
		status.numOfStepsCompleted = status.numOfSteps;
		EntityOperationStatusDAO.INSTANCE.save(status);

		deleteTempData(details.uuid);
		return Status.SUCCESS;
	}

	private SyncTabletAnalyticsReq constructReq(TempParsedDATA row, OfflineTestResultUploadDetails details) {
        // TODO Auto-generated method stub
	    OrgMember orgMember = OrgMemberDAO.INSTANCE.getMemberByMemberId(details.orgId, row.data.get("memberId"));
	    Test test = TestDAO.INSTANCE.getById(row.data.get("testId"));

	    SyncTabletAnalyticsReq req = new SyncTabletAnalyticsReq();

	    req.userId=orgMember.userId;
	    req.callingUserId=orgMember.userId;
	    req.orgId = details.orgId;
        req.endTime=System.currentTimeMillis();
        req.startTime=System.currentTimeMillis()-test.duration;
        req.callingApp="WebApp";
        req.callingAppId="WebApp";
        req.entityType=EntityType.TEST;
        req.entityId=test._getStringId();
//        req.qIds = test.__getAllQIds();
        if(test.simplifiedBoardNames != null && !test.simplifiedBoardNames.isEmpty()){
            req.qIds = getSimplifiedTestBoardWiseQuestions(test);
        }else{
            req.qIds = getTestBoardWiseQuestions(test);
        }
        List<RecordAttemptReq> qusAttemptReq = new ArrayList<RecordAttemptReq>();
        long timeTaken = test.duration/test.qusCount;
        for(int i = 1; i <= test.qusCount; i++){
            if(row.data.containsKey("q"+i)){
                String[] answerGiven = row.data.get("q"+i).split(",");
                RecordAttemptReq qusAttempt = new RecordAttemptReq();
                qusAttempt.qId = req.qIds.get(i-1);
                qusAttempt.answerGiven = Arrays.asList(answerGiven);
                qusAttempt.timeTaken = timeTaken;
                qusAttemptReq.add(qusAttempt);
            }
        }
        req.qusAttemptReqs = qusAttemptReq;
        if(row.data.get("targetType").equalsIgnoreCase("MODULE")){
            req.target.type = EntityType.MODULE;
        }else{
            req.target.type = EntityType.SECTION;
        }
        req.target.id = row.data.get("targetId");
        return req;
    }

    private List<String> getSimplifiedTestBoardWiseQuestions(Test test) {
        List<String> qIds = new ArrayList<String>();
        // We are using linkedhashmap for maintaining insertion and retrieval order
        Map<String,List<TestDetails>> subjectQuesMap = new LinkedHashMap<String, List<TestDetails>>();
        for(TestMetadata mData: test.metadata){
            subjectQuesMap.put(mData.id, mData.details);
        }
        for(SimplifiedBoardNames simplifiedBoard : test.simplifiedBoardNames){
            List<String> SCQ = new ArrayList<String>();
            List<String> MCQ = new ArrayList<String>();
            List<String> NUMERIC = new ArrayList<String>();
            List<String> PARA = new ArrayList<String>();
            List<String> MATRIX = new ArrayList<String>();
            for(String brdId : simplifiedBoard.brdIds){
                for(TestDetails detail : subjectQuesMap.get(brdId)){
                    if(detail.type == QuestionType.SCQ && detail.qIds != null){
                        SCQ.addAll(detail.qIds);
                    }else if(detail.type == QuestionType.MCQ && detail.qIds != null){
                        MCQ.addAll(detail.qIds);
                    }else if(detail.type == QuestionType.NUMERIC && detail.qIds != null){
                        NUMERIC.addAll(detail.qIds);
                    }else if(detail.type == QuestionType.PARA && detail.qIds != null){
                        PARA.addAll(detail.qIds);
                    }else if(detail.type == QuestionType.MATRIX && detail.qIds != null){
                        MATRIX.addAll(detail.qIds);
                    }
                }
                // Now remove that brdId key from map
                subjectQuesMap.remove(brdId);
            }
            qIds.addAll(SCQ);
            qIds.addAll(MCQ);
            qIds.addAll(NUMERIC);
            qIds.addAll(PARA);
            qIds.addAll(MATRIX);
        }
        if(subjectQuesMap.keySet().size() > 0){
            for(String brdId : subjectQuesMap.keySet()){
                List<String> SCQ = new ArrayList<String>();
                List<String> MCQ = new ArrayList<String>();
                List<String> NUMERIC = new ArrayList<String>();
                List<String> PARA = new ArrayList<String>();
                List<String> MATRIX = new ArrayList<String>();
                for(TestDetails details : subjectQuesMap.get(brdId)){
                    if(details.type == QuestionType.SCQ && details.qIds != null){
                        SCQ.addAll(details.qIds);
                    }else if(details.type == QuestionType.MCQ && details.qIds != null){
                        MCQ.addAll(details.qIds);
                    }else if(details.type == QuestionType.NUMERIC && details.qIds != null){
                        NUMERIC.addAll(details.qIds);
                    }else if(details.type == QuestionType.PARA && details.qIds != null){
                        PARA.addAll(details.qIds);
                    }else if(details.type == QuestionType.MATRIX && details.qIds != null){
                        MATRIX.addAll(details.qIds);
                    }
                }
                qIds.addAll(SCQ);
                qIds.addAll(MCQ);
                qIds.addAll(NUMERIC);
                qIds.addAll(PARA);
                qIds.addAll(MATRIX);
            }
        }
        return qIds;
    }

    private List<String> getTestBoardWiseQuestions(Test test) {
        List<String> qIds = new ArrayList<String>();
        for(TestMetadata mData: test.metadata){
            //Inside each board/subject
            List<String> SCQ = new ArrayList<String>();
            List<String> MCQ = new ArrayList<String>();
            List<String> NUMERIC = new ArrayList<String>();
            List<String> PARA = new ArrayList<String>();
            List<String> MATRIX = new ArrayList<String>();
            for(TestDetails details : mData.details){
                //Inside each type of question for each subject
                if(details.type == QuestionType.SCQ && details.qIds != null){
                    SCQ.addAll(details.qIds);
                }else if(details.type == QuestionType.MCQ && details.qIds != null){
                    MCQ.addAll(details.qIds);
                }else if(details.type == QuestionType.NUMERIC && details.qIds != null){
                    NUMERIC.addAll(details.qIds);
                }else if(details.type == QuestionType.PARA && details.qIds != null){
                    PARA.addAll(details.qIds);
                }else if(details.type == QuestionType.MATRIX && details.qIds != null){
                    MATRIX.addAll(details.qIds);
                }
            }
            qIds.addAll(SCQ);
            qIds.addAll(MCQ);
            qIds.addAll(NUMERIC);
            qIds.addAll(PARA);
            qIds.addAll(MATRIX);
        }
        return qIds;
    }

    private void deleteTempData(String uuid) {
		TempParsedDataDAO.INSTANCE.deleteByQuery(TempParsedDataDAO.INSTANCE
				.createQuery().filter(ConstantsGlobal.UUID, uuid));
	}
}
