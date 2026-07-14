package com.vedantu.cmds.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;

import play.Logger;
import play.Logger.ALogger;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.vedantu.board.pojos.BoardBasicInfo;
import com.vedantu.cmds.daos.TempParsedDataDAO;
import com.vedantu.cmds.managers.CMDSTestManager;
import com.vedantu.cmds.models.TempParsedDATA;
import com.vedantu.cmds.models.event.details.OfflineTestResultUploadDetails;
import com.vedantu.cmds.pojos.content.tests.OfflineQInfo;
import com.vedantu.cmds.pojos.content.tests.OfflineTestData;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.daos.EntityOperationStatusDAO;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.OperationType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.models.mongo.EntityOperationStatus;
import com.vedantu.commons.pojos.ScheduleInfo;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.ObjectMapperUtils;
import com.vedantu.content.daos.AnswerDAO;
import com.vedantu.content.daos.QuestionDAO;
import com.vedantu.content.daos.TestDAO;
import com.vedantu.content.enums.Difficulty;
import com.vedantu.content.enums.EnumBasket.TestType;
import com.vedantu.content.enums.QuestionType;
import com.vedantu.content.enums.TestMode;
import com.vedantu.content.enums.TestResultVisibility;
import com.vedantu.content.managers.AnalyticsManager;
import com.vedantu.content.managers.LibraryManager;
import com.vedantu.content.models.Answer;
import com.vedantu.content.models.Question;
import com.vedantu.content.models.tests.Test;
import com.vedantu.content.pojos.requests.analytics.EndAttemptReq;
import com.vedantu.content.pojos.requests.analytics.RecordAttemptReq;
import com.vedantu.content.pojos.requests.analytics.StartAttemptReq;
import com.vedantu.content.pojos.responses.analytics.EndAttemptRes;
import com.vedantu.content.pojos.responses.analytics.StartAttemptRes;
import com.vedantu.content.pojos.tests.Marks;
import com.vedantu.content.pojos.tests.QuestionResultStatus;
import com.vedantu.content.pojos.tests.TestDetails;
import com.vedantu.content.pojos.tests.TestMetadata;
import com.vedantu.content.pojos.tests.TestQuestionSet;
import com.vedantu.content.search.details.TestSearchIndexDetails;
import com.vedantu.mongo.MongoManager.SortOrder;
import com.vedantu.organization.daos.OrgMemberDAO;
import com.vedantu.organization.daos.OrgProgramDAO;
import com.vedantu.organization.models.OrgMember;
import com.vedantu.organization.models.OrgProgram;
import com.vedantu.organization.pojos.OrgProgramCenterSections;
import com.vedantu.xml.parsers.TestResultSheetHandaler;
import com.vedantu.xml.parsers.XLFileParser;

public class OfflineTestUtils {

    private static final ALogger LOGGER = Logger.of(OfflineTestUtils.class);

    public static String loadOfflineResult(String userId, String orgId, String testId,
            File testResultFile, SrcEntity target) throws VedantuException {

        String jobId = null;
        try {
            LOGGER.debug("testId: " + testId);
            XLFileParser parser = new XLFileParser(testResultFile);
            TestResultSheetHandaler handaler = new TestResultSheetHandaler();
            parser.parse2(handaler, testId, orgId, target);

            LOGGER.info("Reading data from file: " + testResultFile.getAbsolutePath());
            LOGGER.info("total no of sheets : " + parser.getSheetNames().size());
            final EntityOperationStatus jobStatus = new EntityOperationStatus();
            jobStatus.type = EntityType.TEST;
            jobStatus.id = handaler.getUuid();
            jobStatus.oType = OperationType.TEST_RESULT_UPLOAD;
            jobStatus.numOfSteps = (int) TempParsedDataDAO.INSTANCE.count(new BasicDBObject(
                    ConstantsGlobal.UUID, handaler.getUuid()));
            EntityOperationStatusDAO.INSTANCE.save(jobStatus);
            jobId = jobStatus._getStringId();
            LOGGER.debug("Entity will be  published in jobId " + jobId);

            OfflineTestResultUploadDetails details = new OfflineTestResultUploadDetails(
                    handaler.getUuid(), parser.getSheetNames(), orgId, null, testId, userId, jobId);
            CMDSTestManager.generateEventAysc(userId, details, EventType.UPLOAD_TEST_RESULT);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new VedantuException(VedantuErrorCode.INVALID_INPUT_DATA, e.getMessage(), e);
        }
        return jobId;
    }

    public static String loadOfflineResult(String userId, String orgId, String progId,
            File testResultFile, boolean merge) throws VedantuException {

        String jobId = null;
        try {
            LOGGER.debug("orgId: " + orgId + ", programId:" + progId);

            OrgProgram orgProgram = OrgProgramDAO.INSTANCE.getProgramById(orgId, progId);

            XLFileParser parser = new XLFileParser(testResultFile);
            TestResultSheetHandaler handaler = new TestResultSheetHandaler();
            parser.parse(handaler);

            LOGGER.info("Reading data from file: " + testResultFile.getAbsolutePath()
                    + " programId:" + orgProgram._getStringId());
            LOGGER.info("total no of sheets : " + parser.getSheetNames().size());
            final EntityOperationStatus jobStatus = new EntityOperationStatus();
            jobStatus.type = EntityType.TEST;
            jobStatus.id = handaler.getUuid();
            jobStatus.oType = OperationType.TEST_RESULT_UPLOAD;
            jobStatus.numOfSteps = (int) TempParsedDataDAO.INSTANCE.count(new BasicDBObject(
                    ConstantsGlobal.UUID, handaler.getUuid()));
            EntityOperationStatusDAO.INSTANCE.save(jobStatus);
            jobId = jobStatus._getStringId();
            LOGGER.debug("Entity will be  published in jobId " + jobId);

            OfflineTestResultUploadDetails details = new OfflineTestResultUploadDetails(
                    handaler.getUuid(), parser.getSheetNames(), orgId, progId, null, userId, jobId);
            CMDSTestManager.generateEventAysc(userId, details, EventType.UPLOAD_TEST_RESULT);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new VedantuException(VedantuErrorCode.INVALID_INPUT_DATA, e.getMessage(), e);
        }
        return jobId;
    }

    private static StartAttemptRes startTestAttempt(OfflineTestData testData, String setName,
            String memberId, String userId, ScheduleInfo scheduleInfo, boolean merge,
            EntityOperationStatus status) throws VedantuException {

        TestQuestionSet qSet = testData.__getQuestionSet(setName);
        if (StringUtils.isNotEmpty(setName) && qSet == null) {
            String errorMsg = "no set present with setName:[" + setName + "] for test:"
                    + testData.code;
            LOGGER.error(errorMsg);
            throw new VedantuException(VedantuErrorCode.INVALID_METADATA, errorMsg);
        }

        // start test attempt for the user
        StartAttemptReq startAttemptReq = new StartAttemptReq();
        startAttemptReq.entityId = testData.id;
        startAttemptReq.entityType = EntityType.TEST;
        startAttemptReq.userId = userId;
        startAttemptReq.callingUserId = userId;
        startAttemptReq.setName = setName;
        LOGGER.debug("start attempt request : " + startAttemptReq);
        if (StringUtils.isEmpty(startAttemptReq.entityId)) {
            LOGGER.error("entityId[" + startAttemptReq.entityId + "] can not be null");
            throw new VedantuException(VedantuErrorCode.ATTEMPT_NOT_FOUND, "entity can not be null");
        }
        StartAttemptRes startAttemptRes = null;
        try {
            startAttemptRes = AnalyticsManager.startAttempt(startAttemptReq, true,
                    scheduleInfo == null ? 0 : scheduleInfo.startTime.getTime(),
                    scheduleInfo == null ? 0 : scheduleInfo.endTime.getTime());
        } catch (VedantuException e) {
            String errorMsg = "data has been already added for member [" + memberId + "] for "
                    + startAttemptReq.entityType + "[" + startAttemptReq.entityId + "]";
            LOGGER.error(errorMsg + "," + e.getMessage(), e);
            if (!merge) {
                throw new VedantuException(e.errorCode, "data has been already added for member : "
                        + memberId + ", " + e.getMessage());
            }
            status.message += errorMsg + "<br> ";
        }
        LOGGER.debug("start attempt response : " + startAttemptRes);
        return startAttemptRes;
    }

    public static Map<String, OfflineQInfo> createTestAndQuestions(String userId, String orgId,
            OrgProgram orgProgram, Map<String, OfflineTestData> testDataMap,
            Map<String, BoardBasicInfo> brdNameToBoardInfoMap, String groupName,
            ScheduleInfo scheduleInfo) throws VedantuException, Exception {

        Map<String, OfflineQInfo> qCodeToQusMap = new HashMap<String, OfflineQInfo>();

        for (Entry<String, OfflineTestData> entry : testDataMap.entrySet()) {
            OfflineTestData testData = entry.getValue();
            for (OfflineQInfo qInfo : testData.qusMarks) {
                if (brdNameToBoardInfoMap.get(qInfo.courseBrdName) != null) {
                    qInfo.courseBrdId = brdNameToBoardInfoMap.get(qInfo.courseBrdName).id;
                    DBObject query = new BasicDBObject(ConstantsGlobal.CODE, qInfo.qCode);
                    SrcEntity contentSrc = new SrcEntity(EntityType.ORGANIZATION, orgId);
                    query.put(ConstantsGlobal.CONTENT_SRC, contentSrc.toDBObject());
                    LOGGER.debug("find Question query : " + query);
                    Question qus = QuestionDAO.INSTANCE.findOne(query, null);
                    if (qus == null) {
                        qus = QuestionDAO.INSTANCE.addQuestion(userId, null, null,
                                Arrays.asList(qInfo.courseBrdId), qInfo.type, null, null, null,
                                null, Scope.ORG, Difficulty.UNKNOWN, contentSrc);
                        LOGGER.debug("created question  : " + qus);
                        Answer ans = AnswerDAO.INSTANCE.addAnswer(userId, qus._getStringId(),
                                qus.type, qInfo.correctAnswer, qInfo.matrixAnswer);
                        LOGGER.debug("created answer  : " + ans);
                        if (qInfo.optionalCorrectAnswers != null) {
                            ans.optionalCorrectAnswers = qInfo.optionalCorrectAnswers;
                            AnswerDAO.INSTANCE.save(ans);
                        }
                        if (ans != null) {
                            qus.code = qInfo.qCode;
                            qus.hasAns = true;
                            // TODO: if needed then index this question
                            QuestionDAO.INSTANCE.save(qus);
                        }
                    }
                    qInfo.id = qus._getStringId();
                    qCodeToQusMap.put(qInfo.qCode, qInfo);
                }
            }
        }

        boolean createTestGroup = testDataMap.keySet().size() > 1;

        createTests(userId, orgId, orgProgram, testDataMap, qCodeToQusMap, groupName,
                createTestGroup, scheduleInfo);
        return qCodeToQusMap;
    }

    public static void collectAndProcessUserTestData(String orgId, String progId,
            List<String> sheetNames, Map<String, OfflineTestData> testDataMap,
            Map<String, OfflineQInfo> qCodeToQusMap, String sessionUuid,
            EntityOperationStatus status) throws Exception {

        String errorMsg = null;
        for (int sIndex = 1; sIndex < sheetNames.size(); sIndex++) {
            String sheetName = sheetNames.get(sIndex);
            DBObject query = new BasicDBObject("uuid", sessionUuid);
            query.put("sheetId", (sIndex + 1));

            DBCursor cursor = TempParsedDataDAO.INSTANCE.getCollection().find(query);
            cursor.sort(new BasicDBObject("data.enrollmentid", SortOrder.ASC.getValue()));
            String userEnrollmentId = null;
            StartAttemptRes userAttempt = null;
            OfflineTestData testData = null;
            OrgMember orgMember = null;
            ScheduleInfo scheduleInfo = null;
            int rowNum = 0;
            int processCount = 0;
            while (cursor.hasNext()) {
                rowNum++;
                processCount++;
                TempParsedDATA row = ObjectMapperUtils.convertToVedantuBaseModel(cursor.next(),
                        TempParsedDATA.class);
                LOGGER.info("Reading row number: " + rowNum + " for sheet : " + sheetName
                        + ", row:" + row);
                String testCode = getCellValue(row.data, "testCode");
                if (!StringUtils.equals(testCode, sheetName)) {
                    errorMsg = "sheet name [" + sheetName + "] does not match with testCode ["
                            + testCode + "]";
                    LOGGER.error(errorMsg);
                    throw new Exception(errorMsg);
                }

                String enrollmentId = getCellValue(row.data, "enrollmentId");

                int questionNumber = getCellIntValue(row.data, "questionNumber");
                String answerGiven = getCellValue(row.data, "answerGiven");

                String setName = getCellValue(row.data, "setName");

                testData = testDataMap.get(testCode);

                TestQuestionSet qSet = testData.__getQuestionSet(setName);

                long testDate = testData.testDate == 0 ? System.currentTimeMillis()
                        : testData.testDate;
                scheduleInfo = new ScheduleInfo(new Date(testDate), new Date(testDate));

                if (!StringUtils.equalsIgnoreCase(userEnrollmentId, enrollmentId)) {

                    if (StringUtils.isNotEmpty(setName) && qSet == null) {
                        errorMsg = "no set present with setName:[" + setName + "] for test:"
                                + testCode;
                        LOGGER.error(errorMsg);
                        throw new Exception(errorMsg);
                    }

                    if (userAttempt != null && orgMember != null) {
                        status.numOfStepsCompleted += processCount;
                        EntityOperationStatusDAO.INSTANCE.save(status);
                        processCount = 0;
                        endAttempt(userAttempt, orgMember, testData, scheduleInfo.endTime.getTime());
                    }

                    orgMember = OrgMemberDAO.INSTANCE.getMemberByMemberId(orgId, enrollmentId);
                    userEnrollmentId = enrollmentId;
                    if (orgMember == null || orgMember._getProgramMapping(progId) == null) {
                        errorMsg = "member not found with memberId: " + enrollmentId
                                + ", for program[" + progId + "], organization[" + orgId + "]";
                        LOGGER.error(errorMsg);
                        status.message += errorMsg + " <br> ";
                        EntityOperationStatusDAO.INSTANCE.save(status);
                        continue;
                    }

                    try {
                        // start new attempt for this user
                        userAttempt = startTestAttempt(testData, setName, enrollmentId,
                                orgMember.userId, scheduleInfo, true, status);
                    } catch (VedantuException e) {
                        status.message += e.getMessage() + "\n";
                        EntityOperationStatusDAO.INSTANCE.save(status);
                        LOGGER.error(e.getMessage(), e);
                        continue;
                    }
                }

                OfflineQInfo qInfo = null;
                if (qSet == null) {
                    qInfo = testData.qusMarks.size() < questionNumber ? null : testData.qusMarks
                            .get(questionNumber - 1);
                } else {
                    LOGGER.debug("questionSet: " + qSet);
                    String qId = qSet.qIds.size() < questionNumber ? null : qSet.qIds
                            .get(questionNumber - 1);
                    if (qId != null) {
                        for (Entry<String, OfflineQInfo> qusInfo : qCodeToQusMap.entrySet()) {
                            if (StringUtils.equals(qId, qusInfo.getValue().id)) {
                                qInfo = qusInfo.getValue();
                                break;
                            }
                        }
                    }
                }

                if (qInfo == null) {
                    LOGGER.error("more no of questions found for member[" + enrollmentId
                            + "] qusNo[" + questionNumber + "], for set[" + setName + "], test["
                            + testCode + "], maxQuestions:" + testData.qusMarks.size());
                    continue;
                }
                if (StringUtils.isEmpty(answerGiven)) {
                    LOGGER.error("user has not attempted qusNo[" + questionNumber + "], for set["
                            + setName + "], test[" + testCode + "]");
                    continue;
                }

                if (userAttempt == null || orgMember == null) {
                    continue;
                }
                RecordAttemptReq recordAttemptReq = new RecordAttemptReq();
                recordAttemptReq.attemptId = userAttempt.info.id;
                recordAttemptReq.qId = qInfo.id;
                recordAttemptReq.entityId = testData.id;
                recordAttemptReq.entityType = EntityType.TEST;
                recordAttemptReq.userId = orgMember.userId;
                recordAttemptReq.callingUserId = orgMember.userId;

                //if (qInfo.type != QuestionType.MATRIX) {
                    recordAttemptReq.setAnswerGiven(convertOptionToNumericalList(qInfo.type,
                            answerGiven));
//                } else {
//                    recordAttemptReq.setMatrixAnswer(convertOptionToNumericalMatrix(answerGiven));
//                }
                // record attempt for every question for the user
                try {
                    AnalyticsManager.recordAttempt(recordAttemptReq);
                } catch (VedantuException e) {
                    status.errorCode += e.getMessage() + "\n";
                    EntityOperationStatusDAO.INSTANCE.save(status);
                    LOGGER.error(e.getMessage(), e);
                }
            }
            if (userAttempt != null && orgMember != null && testData != null) {
                status.numOfStepsCompleted += processCount;
                endAttempt(
                        userAttempt,
                        orgMember,
                        testData,
                        scheduleInfo == null ? userAttempt.startTime : scheduleInfo.endTime
                                .getTime());
            }
        }
    }

    private static void endAttempt(StartAttemptRes userAttempt, OrgMember orgMember,
            OfflineTestData testData, long endTime) {

        // end the previous attempt
        EndAttemptReq endAttemptReq = new EndAttemptReq();
        endAttemptReq.attemptId = userAttempt.info.id;
        endAttemptReq.userId = orgMember.userId;
        endAttemptReq.callingUserId = orgMember.userId;
        endAttemptReq.entityId = testData.id;
        endAttemptReq.entityType = EntityType.TEST;
        EndAttemptRes endAttemptRes;
        try {
            endAttemptRes = AnalyticsManager.endAttempt(endAttemptReq, endTime, true);
            LOGGER.debug("end attempt response for endAttemptReq : " + endAttemptReq
                    + ", endAttemptRes : " + endAttemptRes);
        } catch (VedantuException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public static Map<String, OfflineTestData> collectTestMetadataData(List<String> sheetNames,
            String uuid) throws Exception {

        Map<String, OfflineTestData> testDataMap = new HashMap<String, OfflineTestData>();
        String sheetName = sheetNames.get(0);

        DBObject query = new BasicDBObject("uuid", uuid);
        query.put("sheetId", 1);

        DBCursor cursor = TempParsedDataDAO.INSTANCE.getCollection().find(query);
        cursor.sort(new BasicDBObject("rowNo", SortOrder.ASC.getValue()));

        int rowNum = 0;
        List<String> setNames = new ArrayList<String>();
        Map<String, Map<Integer, String>> setNameToQusMapping = new HashMap<String, Map<Integer, String>>();
        String errorMsg = "";

        while (cursor.hasNext()) {
            rowNum++;
            LOGGER.debug("Reading row number: " + rowNum + " for sheet : " + sheetName);
            TempParsedDATA row = ObjectMapperUtils.convertToVedantuBaseModel(cursor.next(),
                    TempParsedDATA.class);
            if (row.data == null) {
                LOGGER.error("no data found at rowNo:" + rowNum + ", row:" + row);
                continue;
            }

            if (rowNum == 1) {
                Collection<String> stNames = verifyTestMetadatSheet(row);
                for (String sName : stNames) {
                    if (StringUtils.isNotEmpty(sName)) {
                        setNames.add(sName);
                    }
                }
                LOGGER.debug("setNames: " + setNames);
            }

            String testCode = getCellValue(row.data, "testCode");
            String targetExam = getCellValue(row.data, "targetExam");
            String durationString = getCellValue(row.data, "duration");
            long duration = (durationString == null ? 0 : Long.parseLong(durationString)
                    * DateUtils.MILLIS_PER_MINUTE);
            Date date = HSSFDateUtil.getJavaDate(getCellIntValue(row.data, "date"));

            int questionNumber = getCellIntValue(row.data, "questionNumber");
            String qType = getCellValue(row.data, "type");
            QuestionType questionType = QuestionType.valueOfKey(qType);

            if (questionType == QuestionType.UNKNOWN) {
                errorMsg = qType + " is not a valid question type";
                LOGGER.error(errorMsg);
                throw new Exception(errorMsg);
            }
            String subject = getCellValue(row.data, "subject");
            String correctAnswer = getCellValue(row.data, "correctAnswer");
            int positiveScore = getCellIntValue(row.data, "positiveScore");
            int negativeScore = getCellIntValue(row.data, "negativeScore");

            if (StringUtils.isEmpty(testCode) || StringUtils.isEmpty(subject)
                    || StringUtils.isEmpty(correctAnswer) || date == null) {
                errorMsg = "some fields are missing at rowNo : " + rowNum + " for sheet: "
                        + sheetName + " [testCode: " + testCode + ", subject: " + subject
                        + ", correctAnswer: " + correctAnswer + ", date: " + date + " ]";
                LOGGER.error(errorMsg);
                throw new Exception(errorMsg);
            }

            OfflineTestData testData = testDataMap.get(testCode);
            if (testData == null) {
                testData = new OfflineTestData(testCode, testCode, testCode,
                        new ArrayList<TestMetadata>(), Arrays.asList(targetExam), TestType.TEST,
                        duration, date.getTime());
                testDataMap.put(testCode, testData);
            }
            testData.targetIds = Arrays.asList(targetExam);
            String qusId = getUniqueQid(testCode, questionNumber);

            QuestionResultStatus status = QuestionResultStatus.valueOfKey(correctAnswer);

            // if values are given as A,B,AB, mean A is correct, B is also
            // correct
            // and AB is also corrects
            Map<Integer, List<String>> optionalCorrectAnswers = null;
            List<String> correctAnswers = null;
            //if (questionType != QuestionType.MATRIX) {
                if (correctAnswer != null && correctAnswer.contains(",")) {

                    // presence of (,) mean partial marking of the question-->
                    // multiple possible answers
                    optionalCorrectAnswers = new HashMap<Integer, List<String>>();
                    int i = 0;
                    for (String ans : correctAnswer.split(",")) {
                        i++;
                        if (i == 1) {
                            correctAnswers = status == QuestionResultStatus.ACTIVE ? convertOptionToNumericalList(
                                    questionType, ans) : null;
                        } else {
                            List<String> optnAns = status == QuestionResultStatus.ACTIVE ? convertOptionToNumericalList(
                                    questionType, ans) : null;
                            optionalCorrectAnswers.put(Integer.valueOf(i), optnAns);
                        }
                    }

                } else {
                    correctAnswers = status == QuestionResultStatus.ACTIVE ? convertOptionToNumericalList(
                            questionType, correctAnswer) : null;
                }
            //}
            OfflineQInfo qInfo = new OfflineQInfo(qusId, qusId, correctAnswers, questionType,
                    subject, subject, negativeScore, positiveScore);
            qInfo.optionalCorrectAnswers = optionalCorrectAnswers;
            qInfo.status = status;
//            if (status == QuestionResultStatus.ACTIVE && questionType == QuestionType.MATRIX) {
//                qInfo.matrixAnswer = convertOptionToNumericalMatrix(correctAnswer);
//            }
            testData.addQusInfo(qInfo);

            boolean found = false;
            for (TestMetadata testMetadata : testData.testMetadatas) {
                if (StringUtils.equalsIgnoreCase(testMetadata.id, subject)) {
                    if (!testMetadata.qIds.contains(qusId)) {
                        Marks marks = new Marks(positiveScore, negativeScore);
                        marks.status = qInfo.status;
                        testMetadata.addQuestion(qusId, qInfo.type, marks);
                        LOGGER.info("max marks: " + positiveScore + " test metadata marks: "
                                + testMetadata.totalMarks);
                    }
                    found = true;
                    break;
                }
            }

            if (!found) {
                TestMetadata testMetadata = createTestMetadata(subject, subject, qusId,
                        questionType, positiveScore, negativeScore);
                testData.testMetadatas.add(testMetadata);
            }

            if (CollectionUtils.isNotEmpty(setNames)) {
                for (int i = 0; i < setNames.size(); i++) {
                    String mapKey = getTestQuestionSetMapKey(testCode, setNames.get(i));
                    Map<Integer, String> qusNoToQidMap = setNameToQusMapping.get(mapKey);
                    if (qusNoToQidMap == null) {
                        qusNoToQidMap = new HashMap<Integer, String>();
                        setNameToQusMapping.put(mapKey, qusNoToQidMap);
                    }
                    int setQusNo = getCellIntValue(row.data, setNames.get(i));
                    Integer seQusNumber = new Integer(setQusNo);
                    qusNoToQidMap.put(seQusNumber, qusId);
                }
            }
        }

        if (!setNameToQusMapping.isEmpty()) {
            for (Entry<String, Map<Integer, String>> entry : setNameToQusMapping.entrySet()) {
                String testCode = StringUtils.substringBefore(entry.getKey(), "#");
                String setName = StringUtils.substringAfter(entry.getKey(), "#");
                LOGGER.debug("testCode: " + testCode + ", setName: " + setName);
                OfflineTestData testData = testDataMap.get(testCode);
                if (testData != null) {
                    int i = 0;
                    TestQuestionSet qSet = new TestQuestionSet(setName);

                    while (true) {
                        i++;
                        String qid = entry.getValue().get(Integer.valueOf(i));
                        if (StringUtils.isEmpty(qid)) {
                            break;
                        }
                        qSet.addQid(qid);
                    }
                    testData.addQuestionSet(qSet);
                }
            }
        }
        return testDataMap;
    }

    private static String getTestQuestionSetMapKey(String testCode, String setName) {

        return testCode + "#" + setName;
    }

    private static void createTests(String userId, String orgId, OrgProgram orgProgram,
            Map<String, OfflineTestData> testDataMap, Map<String, OfflineQInfo> qcodeToQusMap,
            String testGroupName, boolean createTestGroup, ScheduleInfo scheduleInfo)
            throws VedantuException {

        LOGGER.info("creating test if previously not created");
        Map<String, Test> testIdToTestMap = new HashMap<String, Test>();
        long testGroupDuration = 0;
        int testGroupTotalMarks = 0;
        int testGroupQusCount = 0;
        Set<String> testGroupBrdIds = new HashSet<String>();
        List<TestMetadata> groupMedatata = new ArrayList<TestMetadata>();
        Set<Test> testForLibrary = new HashSet<Test>();
        for (Entry<String, OfflineTestData> entry : testDataMap.entrySet()) {
            Test test = null;
            try {
                if (scheduleInfo == null) {
                    scheduleInfo = new ScheduleInfo(new Date(entry.getValue().testDate), new Date(
                            entry.getValue().testDate));
                }

                test = createTestFromOfflineData(userId, orgId, entry.getValue(), qcodeToQusMap);
                testForLibrary.add(test);
            } catch (Exception e) {
                LOGGER.error("test is already added " + e.getMessage(), e);
                return;
            }
            if (createTestGroup && StringUtils.isNotEmpty(testGroupName)) {
                testIdToTestMap.put(test._getStringId(), test);
                testGroupDuration += test.duration;
                testGroupTotalMarks += test.totalMarks;
                testGroupQusCount += test.qusCount;
                testGroupBrdIds.addAll(test.boardIds);

                for (TestMetadata md : test.metadata) {
                    boolean found = false;
                    for (TestMetadata globalTestMd : groupMedatata) {
                        if (StringUtils.equalsIgnoreCase(globalTestMd.id, md.id)) {
                            found = true;
                            globalTestMd.qusCount += md.qusCount;
                            globalTestMd.totalMarks += md.totalMarks;
                            if (globalTestMd.qIds == null) {
                                globalTestMd.qIds = new ArrayList<String>();
                            }
                            globalTestMd.qIds.addAll(md.qIds);
                            if (globalTestMd.marks == null) {
                                globalTestMd.marks = new HashMap<String, Marks>();
                            }
                            if (md.marks != null) {
                                globalTestMd.marks.putAll(md.marks);
                            }
                            if (md.details == null) {
                                md.details = new ArrayList<TestDetails>();
                            }
                            for (TestDetails details : md.details) {
                                TestDetails globalDetails = globalTestMd.__getDetails(details.type);
                                if (globalDetails == null) {
                                    globalDetails = new TestDetails(details.type, details.qusCount,
                                            details.marks.positive, details.marks.negative);
                                    if (globalTestMd.details == null) {
                                        globalTestMd.details = new ArrayList<TestDetails>();
                                    }
                                    globalTestMd.details.add(globalDetails);
                                } else {
                                    if (details.qIds != null && globalDetails.qIds != null) {
                                        globalDetails.qIds.addAll(details.qIds);
                                        globalDetails.qusCount += details.qusCount;
                                    }
                                }
                            }
                        }
                    }
                    if (!found) {
                        TestMetadata globalTestMd = new TestMetadata(md.id, md.name, md.qusCount);
                        globalTestMd.children = md.children;
                        globalTestMd.details = md.details;
                        globalTestMd.marks = md.marks;
                        globalTestMd.qIds = md.qIds;
                        globalTestMd.qusCount = md.qusCount;
                        globalTestMd.totalMarks = md.totalMarks;
                        groupMedatata.add(globalTestMd);
                    }
                }
            }
        }

        // if testGroup is there than createtestGroup and addParent and
        // childmappings
        if (createTestGroup && StringUtils.isNotEmpty(testGroupName)) {
            LOGGER.info("creating test group : " + testGroupName);
            SrcEntity org = new SrcEntity(EntityType.ORGANIZATION, orgId);

            DBObject query = new BasicDBObject(ConstantsGlobal.CODE, testGroupName);
            query.put(ConstantsGlobal.CONTENT_SRC, org.toDBObject());

            Test testGroup = TestDAO.INSTANCE.findOne(query, null);
            boolean createNewGroup = true;
            if (testGroup != null) {
                String msg = "a testGroup with code: " + testGroupName + " already present for "
                        + org;
                createNewGroup = false;
                LOGGER.error(msg);
            }
            if (createNewGroup) {
                testGroup = TestDAO.INSTANCE.addTest(userId, testGroupName, testGroupName,
                        testGroupName, testGroupQusCount, testGroupDuration, testGroupTotalMarks,
                        groupMedatata, TestType.TEST, TestMode.OFFLINE, org, Scope.ORG,
                        TestResultVisibility.VISIBLE);
                testGroup._fillBrdIds();
                TestDAO.INSTANCE.save(testGroup);
            }
            testForLibrary.clear();
            testForLibrary.add(testGroup);
            for (Entry<String, Test> entry : testIdToTestMap.entrySet()) {
                Test t = entry.getValue();
                t.parentId = testGroup._getStringId();
                testGroup.addChildren(t._getStringId());
                LOGGER.debug("updating test : " + t);
                TestDAO.INSTANCE.save(t);
            }
            testIdToTestMap.put(testGroup._getStringId(), testGroup);
            LOGGER.debug("updating test Group : " + testGroup);
            TestDAO.INSTANCE.save(testGroup);
        }

        Set<String> sectionIds = new HashSet<String>();
        for (OrgProgramCenterSections center : orgProgram.centersSections) {
            if (center.sectionIds != null) {
                sectionIds.addAll(center.sectionIds);
            }
        }

        for (Test test : testForLibrary) {
            TestSearchIndexDetails details = new TestSearchIndexDetails();
            details.fromMongoModel(test);
            // index this test live here and than add parent child mapping to es
            String esId = AnalyticsManager.addLiveEntityToSearchIndex(details, EntityType.TEST,
                    true);

            if (scheduleInfo == null) {
                LOGGER.error("schedule info for test can not be null");
                return;
            }
            for (String sectionId : sectionIds) {
                try {
                    LibraryManager.addToLibrary(new SrcEntity(EntityType.TEST, test._getStringId()),
                            new SrcEntity(EntityType.SECTION, sectionId), UserActionType.ADDED,
                            userId, Scope.ORG, scheduleInfo, esId);
                } catch (VedantuException e) {
                    LOGGER.error(e.getMessage() + ",errorCode:" + e.errorCode, e);
                }
            }
        }

    }

    private static Test createTestFromOfflineData(String userId, String orgId,
            OfflineTestData offlineTestData, Map<String, OfflineQInfo> qcodeToQusMap)
            throws Exception {

        SrcEntity org = new SrcEntity(EntityType.ORGANIZATION, orgId);
        DBObject query = new BasicDBObject(ConstantsGlobal.CODE, offlineTestData.code);
        query.put(ConstantsGlobal.CONTENT_SRC, org.toDBObject());
        Test test = TestDAO.INSTANCE.findOne(query, null);
        boolean createNewTest = true;
        LOGGER.debug("test findOne query : " + query + ", response : " + test);
        if (test != null) {
            offlineTestData.id = test._getStringId();
            String msg = "a test with code: " + offlineTestData.code + " already present for "
                    + org;
            createNewTest = false;
            LOGGER.error(msg);
        }

        int qusCount = 0;
        int totalMarks = 0;

        for (TestMetadata mdata : offlineTestData.testMetadatas) {
            qusCount += mdata.qusCount;
            totalMarks += mdata.totalMarks;
            if (mdata.marks == null) {
                mdata.marks = new HashMap<String, Marks>();
            }
            Map<String, Marks> marks = new HashMap<String, Marks>();
            List<String> qIds = new ArrayList<String>();
            if (mdata.qIds == null) {
                mdata.qIds = new ArrayList<String>();
            }
            for (String qId : mdata.qIds) {
                String updateQId = qcodeToQusMap.get(qId).id;
                if (!qIds.contains(updateQId)) {
                    qIds.add(updateQId);
                }
                marks.put(updateQId, mdata.marks.get(qId));
            }
            mdata.qIds = qIds;
            mdata.marks = marks;

            // update the details qids
            if (mdata.details == null) {
                mdata.details = new ArrayList<TestDetails>();
            }
            for (TestDetails details : mdata.details) {
                List<String> dQIds = new ArrayList<String>();
                for (String qId : details.qIds) {
                    String updateQId = qcodeToQusMap.get(qId).id;
                    if (!dQIds.contains(updateQId)) {
                        dQIds.add(updateQId);
                    }

                }
                details.qIds = dQIds;
            }
        }

        if (createNewTest) {
            LOGGER.debug("creating test with name: " + offlineTestData.name);
            test = TestDAO.INSTANCE.addTest(userId, offlineTestData.name, offlineTestData.code,
                    offlineTestData.desc, qusCount, offlineTestData.duration, totalMarks,
                    offlineTestData.testMetadatas, offlineTestData.type, TestMode.OFFLINE, org,
                    Scope.ORG, TestResultVisibility.VISIBLE);
            test.addTargets(offlineTestData.targetIds);
            test._fillBrdIds();
        }
        if (offlineTestData.sets != null) {
            for (TestQuestionSet set : offlineTestData.sets) {
                List<String> qids = new ArrayList<String>();
                if (set.qIds != null) {
                    for (String qid : set.qIds) {
                        String updateQId = qcodeToQusMap.get(qid).id;
                        if (!qids.contains(updateQId)) {
                            qids.add(updateQId);
                        }
                    }
                }
                set.qIds = qids;
            }
            if (createNewTest) {
                test.sets = offlineTestData.sets;
            }
        }

        if (createNewTest) {
            TestDAO.INSTANCE.save(test);
        }

        offlineTestData.id = test._getStringId();
        return test;
    }

    public static Map<String, BoardBasicInfo> validateAndUpdateTestMetadata(
            Map<String, OfflineTestData> testData, String orgId, String progId,
            Map<String, Set<String>> brdIdToQidsMap) throws Exception, VedantuException {

        Set<BoardBasicInfo> basicInfos = OrgProgramDAO.INSTANCE.getProgramCourses(orgId, progId);
        Map<String, BoardBasicInfo> availbleBoardNameToInfoMap = new HashMap<String, BoardBasicInfo>();
        for (BoardBasicInfo bInfo : basicInfos) {
            availbleBoardNameToInfoMap.put(bInfo.name, bInfo);
        }

        for (Entry<String, OfflineTestData> entry : testData.entrySet()) {
            for (TestMetadata mdata : entry.getValue().testMetadatas) {
                BoardBasicInfo bInfo = availbleBoardNameToInfoMap.get(mdata.name);
                if (bInfo == null) {
                    String erroMsg = "no programme course mapping found for subject : "
                            + mdata.name;
                    LOGGER.error(erroMsg);
                    throw new Exception(erroMsg);
                }
                mdata.id = bInfo.id;
                brdIdToQidsMap.put(bInfo.id, new HashSet<String>(mdata.qIds));
            }
        }
        return availbleBoardNameToInfoMap;
    }

    /**
     *
     * @param forUserId
     * @param userTestData
     * @param orgId
     * @param progId
     * @param availbleBoardNameToInfoMap
     * @return
     * @throws Exception
     * @throws VedantuException
     *             return Map<String, String> of qcode to generated qid map
     *
     */

    private static TestMetadata createTestMetadata(String brdId, String subject, String qId,
            QuestionType qType, int positivemarks, int negativeMarks) {

        TestMetadata testMetadata = new TestMetadata(brdId, subject, 0);
         testMetadata.addQuestion(qId, qType, new Marks(positivemarks, negativeMarks));
        LOGGER.debug("test metadata: " + testMetadata.toString());
        return testMetadata;
    }

    private static String getUniqueQid(String testCode, int qusNo) {

        return testCode + "_" + qusNo;
    }

    private static List<String> convertOptionToNumericalList(QuestionType qType, String ansGiven) {

        if (qType == QuestionType.NUMERIC) {
            return Arrays.asList(ansGiven.trim());
        }

//        if (qType == QuestionType.MATRIX) {
//            return null;
//        }
        List<String> answersGiven = new ArrayList<String>();
        if (StringUtils.isEmpty(ansGiven)) {
            return answersGiven;
        }
        for (char ch : ansGiven.toCharArray()) {
            String val = getCharAnsNumericValue(ch);
            if (StringUtils.isNotEmpty(val)) {
                answersGiven.add(val);
            }
        }
        return QuestionType.getSortedIndex(answersGiven);
    }

    private static Map<String, List<String>> convertOptionToNumericalMatrix(String ansGiven) {

        Map<String, List<String>> ansMatrixMap = new TreeMap<String, List<String>>();
        if (StringUtils.isEmpty(ansGiven)) {
            return ansMatrixMap;
        }
        List<String> answersGiven = Arrays.asList(StringUtils.split(ansGiven, "-"));
        for (int i = 0; i < answersGiven.size(); i++) {
            List<String> ans = ansMatrixMap.get(String.valueOf(i + 1));
            if (ans == null) {
                ans = new ArrayList<String>();
                ansMatrixMap.put(String.valueOf(i + 1), ans);
            }
            for (char ch : answersGiven.get(i).toCharArray()) {
                String val = getCharAnsNumericValue(ch);
                if (StringUtils.isNotEmpty(val)) {
                    ans.add(val);
                }
            }
            Collections.sort(ans);
        }
        return ansMatrixMap;
    }

    private static String getCharAnsNumericValue(char ch) {

        if (ch >= 'A' && ch < 'P') {
            return String.valueOf(new Integer((ch - 'A') + 1));
        } else if (ch >= 'P' && ch <= 'Z') {
            return String.valueOf(new Integer((ch - 'P') + 1));
        }
        LOGGER.error("the options are not alphabets!");
        return null;
    }

    // private static boolean verifyUserSheet(TempParsedDATA row) throws
    // Exception {
    // String[] cellNames = { "testcode", "enrollmentid", "questionnumber" }; //
    // last
    // // column
    // // name
    // // should
    // // be
    // // "setname"
    // for (int i = 0; i < cellNames.length; i++) {
    // if (row.data == null || row.data.get(cellNames[i]) == null) {
    // String errorMsg = "cell name at col:"
    // + i
    // + " should be : "
    // + cellNames[i]
    // + " right now it is["
    // + (row.data == null ? null : row.data.get(cellNames[i]))
    // + "], valid col name order : "
    // + Arrays.asList(cellNames) + ", rowData: " + row.data;
    // LOGGER.error(errorMsg);
    // throw new Exception(errorMsg);
    // }
    // }
    // return true;
    // }

    /**
     *
     * @param row
     * @return setNames of the test
     * @throws Exception
     */
    private static Collection<String> verifyTestMetadatSheet(TempParsedDATA row) throws Exception {

        String[] cellNames = { "testcode", "targetexam", "duration", "date", "questionnumber",
                "type", "subject", "correctanswer", "positivescore", "negativescore" };

        for (int i = 0; i < cellNames.length; i++) {
            if (row.data == null || row.data.get(cellNames[i]) == null) {
                String errorMsg = "cell name at col:" + i + " should be : " + cellNames[i]
                        + ", valid col name order : " + Arrays.asList(cellNames);
                LOGGER.error(errorMsg);
                throw new Exception(errorMsg);
            }
        }
        @SuppressWarnings("unchecked")
        Collection<String> setNames = CollectionUtils.subtract(row.data.keySet(),
                Arrays.asList(cellNames));
        return setNames;
    }

    private static int getCellIntValue(Map<String, String> data, String key) {

        String value = getCellValue(data, key);
        return value == null ? 0 : Integer.parseInt(value);
    }

    private static String getCellValue(Map<String, String> data, String key) {

        return data == null ? null : data.get(key.trim().toLowerCase().replace(" ", ""));
    }

    // private static String getCellValue(Cell cell) {
    // String cellValue = "";
    // if (cell != null) {
    // if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
    // cellValue = cell.getStringCellValue();
    // } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
    // if (HSSFDateUtil.isCellDateFormatted(cell)) {
    // // String dateFmt =
    // // cell.getCellStyle().getDataFormatString();
    // Date date = HSSFDateUtil.getJavaDate(cell
    // .getNumericCellValue());
    // // cellValue = new CellDateFormatter(dateFmt).format(date);
    // cellValue = String.valueOf(date.getTime());
    // } else {
    // cell.setCellType(Cell.CELL_TYPE_STRING);
    // cellValue = String.valueOf(cell.getStringCellValue());
    // }
    // } else {
    // LOGGER.error("cell value not well formatted! "
    // + cell.getCellType());
    // return null;
    // }
    // } else {
    // LOGGER.error("cell value cannot be null!");
    // return null;
    // }
    // LOGGER.debug("cell Value: " + cellValue);
    // return cellValue.trim();
    // }

    public static void main(String[] args) {

        String ans = "PQRS-PR-RP-ST";
        String correct = "PSRT-RP-RP-ST";
        Map<String, List<String>> givnMap = convertOptionToNumericalMatrix(ans);
        Map<String, List<String>> correctMap = convertOptionToNumericalMatrix(correct);
        System.out.println("givenMap : " + givnMap);
        System.out.println("correctMap : " + correctMap);
        // System.out.println("equsl "
        // + QuestionType.isEqualMatrix(givnMap, correctMap));
        try {
            File file = new File("/home/shankar/projects/vedantu/indexes_1.xlsx");
            System.out.println("input file: " + file.getAbsolutePath() + ". file exists: "
                    + file.exists());
            XLFileParser parser = new XLFileParser(file);
            TestResultSheetHandaler handaler = new TestResultSheetHandaler();
            parser.parse(handaler);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        }
    }
}
