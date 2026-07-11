package controllers;

import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import controllers.AbstractUIController.JSONResponseWrapper;
import play.Logger;
import play.cache.Cache;
import play.data.validation.Required;
import play.i18n.Messages;
import play.libs.F.Promise;
import play.mvc.Scope;
import play.mvc.Scope.Params;
import play.mvc.With;
import pojos.TestCacheData;
import util.ClientUtil;
import util.ResponseUtil;
import util.Validation;

@With(Security.class)
public class Tests extends AbstractUIController {
    // --------TAKE TEST------
    protected static JSONObject _getUserTestAnalytics(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/analytics/getUserEntityAnalytics", allParams);
        Logger.log4j.info("BEFORE AWAIT : ATTEMPT END TEST");
        await(promise);
        Logger.log4j.info("AFTER AWAIT :  ATTEMPT END TEST");
        JSONObject userAnalyticsData = getJSON(promise);
        userAnalyticsData = Validation.verifyResponse(userAnalyticsData);
        return userAnalyticsData;
    }

    public static void isReAttemptTest() {
        String testId = Scope.Params.current().get("testId");
        boolean isReattempt = _isReAttemptTest(testId);
        renderJSON(isReattempt);
    }

    private static JSONObject _getToppers(Map<String, Object> allParams) {
        allParams.put("start", ClientUtil.DEFAULT_FETCH_START);
        allParams.put("size", ClientUtil.DEFAULT_FETCH_SIZE_10);
        JSONObject toppersData = _getToppersData(allParams);
        return toppersData;
    }

    private static JSONObject _getTestMarksDistribution(Map<String, Object> allParams) {
        allParams.put("bucketCount", 5);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/analytics/getEntityMarkDistribution", allParams);
        Logger.log4j.info("BEFORE AWAIT : ATTEMPT END TEST");
        await(promise);
        Logger.log4j.info("AFTER AWAIT :  ATTEMPT END TEST");
        JSONObject marksDistributionData = getJSON(promise);
        marksDistributionData = Validation.verifyResponse(marksDistributionData);
        return marksDistributionData;
    }

    public static JSONObject _getTestAnalyticsQuestions(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/analytics/getUserEntityQuestionAttempts", allParams);
        Logger.log4j.info("BEFORE AWAIT : ATTEMPT END TEST");
        await(promise);
        Logger.log4j.info("AFTER AWAIT :  ATTEMPT END TEST");
        JSONObject userAnalyticsData = getJSON(promise);
        userAnalyticsData = Validation.verifyResponse(userAnalyticsData);
        return userAnalyticsData;
    }

    public static JSONObject _getTeacherTestAnalyticsQuestions(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/analytics/getEntityQuestionAttempts", allParams);
        Logger.log4j.info("BEFORE AWAIT : ATTEMPT END TEST");
        await(promise);
        Logger.log4j.info("AFTER AWAIT :  ATTEMPT END TEST");
        JSONObject userAnalyticsData = getJSON(promise);
        userAnalyticsData = Validation.verifyResponse(userAnalyticsData);
        return userAnalyticsData;
    }

    protected static JSONObject _getToppersData(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/analytics/getEntityLeaderBoard", allParams);
        Logger.log4j.info("BEFORE AWAIT TEST-CREATION:GET LEADER BOARD");
        await(promise);
        Logger.log4j.info("AFTER AWAIT TEST-CREATION:GET LEADER BOARD");
        JSONObject toppersData = getJSON(promise);
        toppersData = Validation.verifyResponse(toppersData);
        return toppersData;
    }

    public static void checkPasswordForTestPopup() {
        render();
    }

    public static void attempt() {
        Map<String, Object> allParams = getReqParams();
        String orgId = session.get("loginOrgId");
        allParams.put("orgId", orgId);
        String testIdStr = Scope.Params.current().get("testId");
        String pdfIdStr = Scope.Params.current().get("pdfId");
        String testNameStr = Scope.Params.current().get("testName");

        try {
            Application.recordActivity(ClientUtil.ActivityPages.TEST_ATTEMPT,
                    ClientUtil.ActivityAction.ATTEMPTED, ClientUtil.Entity.TEST, testIdStr);
            if (StringUtils.equals("STUDENT", session.get("userRole"))) {
                _markContentCompleted(allParams);
            }
        } catch (Exception ex) {
        }
        JSONObject cacheResp = null;
        cacheResp = _verifyTestCache(testIdStr);
        if (cacheResp != null) {
            render(renderTheme(orgId, getHTMLFilePath("Library", "testPage")), cacheResp,
                    testIdStr, testNameStr);
            return;
        }
        // IF VERIFIED
        allParams.put("entityId", testIdStr);
        allParams.put("entityType", "TEST");
        _createTestAnalytics(allParams);
        allParams.put("qTypeDistribution", true);
        allParams.put("id", testIdStr);
        JSONObject data = _getQuestionsData(allParams);
        cacheResp = _setTestCache(data, testIdStr, testNameStr);
        render(renderTheme(orgId, getHTMLFilePath("Library", "testPage")), data, cacheResp,
                testIdStr, testNameStr, pdfIdStr, orgId);
    }

    protected static JSONObject _markContentCompleted(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.SOCIALS_WEB_SERVICE_URL
                + "/socials/completed", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }

    public static void endTest(String testId) {
        Map<String, Object> allParams = getReqParams();
        allParams.put("start", ClientUtil.DEFAULT_FETCH_START);
        allParams.put("size", ClientUtil.DEFAULT_FETCH_SIZE_10);
        allParams = _putTestSessions(allParams);
        allParams.put("entityId", testId);
        allParams.put("entityType", "TEST");
        allParams.put("orgId", session.get("loginOrgId"));
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/analytics/endAttempt", allParams);
        Logger.log4j.info("BEFORE AWAIT : ATTEMPT END TEST");
        await(promise);
        Logger.log4j.info("AFTER AWAIT :  ATTEMPT END TEST");
        JSONObject resp = getJSON(promise);
        resp = Validation.verifyResponse(resp);

        util.TestsCacheHandler._clearCache(session.get("userId").toString());
        _setTestSessions();

        renderJSON(resp.toString());

    }

    protected static JSONObject getTargetExams(Map<String, Object> allParams) {
        allParams.put("type", "EXAM");
        allParams.put("size", "-1");
        Promise<JSONResponseWrapper> promise = client(ClientUtil.BOARDS_WEB_SERVICE_URL
                + "/Boards/getBoards", allParams);
        Logger.log4j.info("BEFORE AWAIT GET TARGET EXAMS");
        await(promise);
        Logger.log4j.info("AFTER AWAIT  GET TARGET EXAMS");
        JSONObject targetExam = getJSON(promise);
        return targetExam;
    }

    public static void detailsGetTopics() {
        Map<String, Object> allParams = getReqParams();
        allParams.put("type", "TOPIC");
        allParams.put("size", ClientUtil.DEFAULT_FETCH_SIZE_10);// 100->10
        Promise<JSONResponseWrapper> promise = client(ClientUtil.BOARDS_WEB_SERVICE_URL
                + "/Boards/getBoards", allParams);
        Logger.log4j.info("BEFORE AWAIT TEST-CREATION:GET TOPICS");
        await(promise);
        Logger.log4j.info("AFTER AWAIT TEST-CREATION:GET TOPICS");
        JSONObject topicsData = getJSON(promise);
        // Logger.log4j.info("exam data = "+topicsData);
        topicsData = Validation.verifyResponse(topicsData);
        render(topicsData);
    }

    public static void examResult() {
        Map<String, Object> allParams = getReqParams();
        JSONObject data = _getQuestionsData(allParams);
        render(data);
    }

    public static void testWidgets() {
        render();
    }

    private static Map<String, Object> _putTestEntityParams(String testId,
            Map<String, Object> allParams) {
        Params pr = Scope.Params.current();
        String targetUserId = pr.get("targetUserId");
        if (targetUserId == null || targetUserId.isEmpty()) {
            targetUserId = session.get("userId");
        }
        if (allParams != null) {
            allParams.put("entity.id", testId);
            allParams.put("entity.type", "TEST");
            allParams.put("targetUserId", targetUserId);
            return allParams;
        } else {
            pr.put("entity.id", testId);
            pr.put("entity.type", "TEST");
            pr.put("targetUserId", targetUserId);
        }
        return null;
    }

    // Org Test Fetch
    public static void drawTestQuestions() {
        Map<String, Object> allParams = getReqParams();
        JSONObject questions = _getTestAnalyticsQuestions(allParams);
        String testId = request.params.get("entity.id");
        allParams.put("id", testId);
        JSONObject data = _getTestDetails(allParams);
        render("tags/test/postTestQues.html", questions, data);
    }

    public static void drawTeacherTestQuestions() {
        Map<String, Object> allParams = getReqParams();
        allParams.put("targetUserId", session.get("userId"));
        JSONObject questionsData = _getTeacherTestAnalyticsQuestions(allParams);
        render("Tests/postTestTeachersQuestions.html", questionsData);
    }

    public static JSONObject testResultSheet(Map<String,Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/analytics/getEntityResultAnalytics", allParams);
        Logger.log4j.info("BEFORE AWAIT TEST-CREATION:GET TOPICS");
        await(promise);
        Logger.log4j.info("AFTER AWAIT TEST-CREATION:GET TOPICS");
        JSONObject testFullDetails = Validation.verifyResponse(getJSON(promise));
        return testFullDetails;
    }

    public static void printableAnalytics() {
        Map<String, Object> allParams = getReqParams();
        allParams.put("test.type", "TEST");
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/analytics/getUserEntityAnalyticsBySubject", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject response = getJSON(promise);
        Logger.log4j.info("Siddhardha :" + session.get("myOrgInfo"));
        render(response);
    }

    public static void testResultSheetStudents() {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/analytics/getEntityResultAnalytics", null);
        Logger.log4j.info("BEFORE AWAIT TEST-CREATION:GET TOPICS");
        await(promise);
        Logger.log4j.info("AFTER AWAIT TEST-CREATION:GET TOPICS");
        JSONObject testFullDetails = Validation.verifyResponse(getJSON(promise));
        render(testFullDetails);
    }

    protected static String getStudentsMarksHTML(JSONArray testItems, JSONArray studentTestItems,
            String tdClassStrip, int paperIndex) throws JSONException {
        String htmlStr = "";
        int spStart = 0;
        for (int tp = 0; tp < testItems.length(); tp++) {
            String marks = "-";
            JSONObject testItem = testItems.getJSONObject(tp);
            for (int sp = spStart; sp < studentTestItems.length(); sp++) {
                JSONObject studentTestItem = studentTestItems.getJSONObject(sp);

                if (studentTestItem.getJSONObject("entity").getString("id")
                        .equals(testItem.getString("id"))) {
                    if (testItem.has("metadata") && studentTestItem.has("boards")) {
                        String subjectMarks = getStudentsMarksHTML(
                                testItem.getJSONArray("metadata"),
                                studentTestItem.getJSONArray("boards"), "subject", tp);
                        htmlStr += subjectMarks;
                    }
                    // For correct wrong and left
                    JSONObject measures = studentTestItem.getJSONObject("measures");
                    int total = measures.getInt("correct") + measures.getInt("incorrect")
                            + measures.getInt("left");
                    if (tdClassStrip.equals("subject")) {
                        marks = "<table class=\"testSubjectStats table text-center\">"
                                + "<tr><td class='c-green'>Correct</td><td class='c-red'>Wrong</td><td>Left</td><td class='c-blue'>Total</td></tr>"
                                + "<tr><td class='c-green'>" + measures.getString("correct") + "</td><td class='c-red'>"
                                + measures.getString("incorrect") + "</td><td>"
                                + measures.getString("left") + "</td><td class='c-blue'>"
                                + total + "</td></tr>" + "</table>";
                    } else {
                        marks += "<div class='testSubjectStats'>" + total + "</div>";
                    }
                    spStart = ++sp;
                }
                break;
            }
            if (marks.equals("-")) {
                if (testItem.has("metadata")) {
                    String subjectMarks = getStudentsMarksHTML(testItem.getJSONArray("metadata"),
                            new JSONArray(), "subject", tp);
                    htmlStr += subjectMarks;
                }
            }

            int classIndex = paperIndex;
            if (paperIndex == -1) {
                classIndex = tp;
            }

            String paperStr = "paper paper" + (classIndex + 1);

            // checking to hide elements
            String nonnerClass = "";

            if (tdClassStrip.equals("subject")) {
                if (tp > 2) {
                    nonnerClass = " nonnerForSubs";
                }
                if (classIndex > 1) {
                    nonnerClass += " nonnerForPapers";
                }
            } else {
                if (tp > 1) {
                    nonnerClass = " nonnerForPapers";
                }
            }

            String className = tdClassStrip + " " + tdClassStrip + (tp + 1) + " " + paperStr
                    + nonnerClass;

            htmlStr += "<td class='" + className + "' data-subject='" + (tp + 1) + "' data-paper='"
                    + (classIndex + 1) + "'>" + marks + "</td>";
        }
        return htmlStr;
    }

    // offline tests
    public static JSONObject getOrgTestDetails(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.PROFILE_WEB_SERVICE_URL
                + "/organizations/getTestInfo", allParams);
        Logger.log4j.info("BEFORE AWAIT TEST-CREATION:GET TOPICS");
        await(promise);
        Logger.log4j.info("AFTER AWAIT TEST-CREATION:GET TOPICS");
        JSONObject data = getJSON(promise);
        data = Validation.verifyResponse(data);
        try {
            data = data.getJSONObject("result");
        } catch (JSONException ex) {
            java.util.logging.Logger.getLogger(Tests.class.getName()).log(Level.SEVERE, null, ex);
            data = null;
        }
        return data;
    }

    // Independent Leader Board
    public static void leaderBoard() {
        Map<String, Object> allParams = getReqParams();
        JSONObject toppersData = _getToppers(allParams);
        render(toppersData);
    }

    private static String _getUserAttemptKey() {
        String userId = session.get("userId");
        String key = "testAttempt/" + userId;
        return key;
    }

    protected static void _setTestSessions(String testAttemptId, String scheduleId,
            String testIdValidated) {
        String key = _getUserAttemptKey();
        JSONObject data = new JSONObject();
        try {
            if (testAttemptId != null) {
                data.put("testAttemptId", testAttemptId);
            }
            if (scheduleId != null) {
                data.put("scheduleId", scheduleId);
            }
            if (testIdValidated != null) {
                data.put("testIdValidated", testIdValidated);
            }
            Logger.log4j.info("user attempt test data ========================= " + data);
            Cache.safeAdd(key, data.toString(), null);
        } catch (JSONException ex) {
            Logger.log4j.error(ex.getMessage());
        }
    }

    protected static void _setTestSessions() {
        String key = _getUserAttemptKey();
        Cache.delete(key);
    }

    private static String _getAttemptId() {
        String key = _getUserAttemptKey();
        Logger.log4j.info("siddhardha :"+key);
        String dataStr = Cache.get(key, String.class);
        Logger.log4j.info("siddhardha :"+dataStr);
        String testAttemptId = "";
        try {
            JSONObject data = new JSONObject(dataStr);
            testAttemptId = data.getString("testAttemptId");
        } catch (JSONException ex) {
            Logger.log4j.error(ex.getMessage());
        }
        return testAttemptId;
    }

    protected static Map<String, Object> _putTestSessions(Map<String, Object> allParams) {
        String key = _getUserAttemptKey();
        String dataStr = Cache.get(key, String.class);
        Logger.log4j.info("siddhardha data :"+dataStr);
        try {
            if (dataStr == null || dataStr.isEmpty()) {
                return allParams;
            }
            JSONObject data = new JSONObject(dataStr);
            if (data.has("testAttemptId")) {
                String testAttemptId = data.getString("testAttemptId");
                if (testAttemptId != null) {
                    allParams.put("attemptId", testAttemptId);
                }
            }
            if (data.has("scheduleId")) {
                String scheduleId = data.getString("scheduleId");
                allParams.put("scheduleId", scheduleId);
            }
        } catch (JSONException ex) {
            Logger.log4j.error(ex.getMessage());
        }
        return allParams;
    }

    protected static boolean _isReAttemptTest(@Required String id) {
        Map<String, Object> allParams = getReqParams();
        allParams.put("entity.id", id);
        allParams.put("entity.type", "TEST");
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/analytics/getUserEntityAttemptStatusInfo", allParams);
        Logger.log4j.info("BEFORE AWAIT : ATTEMPT END TEST");
        await(promise);
        Logger.log4j.info("AFTER AWAIT :  ATTEMPT END TEST");
        JSONObject resp = getJSON(promise);
        boolean isAttempted = false;
        try {
            if (resp != null && resp.has("result") && resp.getString("errorCode").isEmpty()) {
                JSONObject result = resp.getJSONObject("result");
                String type = result.getString("type");
                isAttempted = result.getBoolean("attempted");
                if ("OFFLINE".equals(type)) {
                    isAttempted = true;
                }
            }
        } catch (JSONException ex) {
            Logger.log4j.error(ex.getMessage());
            isAttempted = false;
        }
        return isAttempted;
    }

    protected static JSONObject _getTestResponseJSON(@Required String errorCode, String errorMsg,
            JSONObject result) {
        errorMsg = errorMsg.isEmpty() ? Messages.get(errorCode) : errorMsg;
        String errStr = "{'errorCode':'" + errorCode + "','errorMessage':'" + errorMsg
                + "','result':{noData:''}}";
        JSONObject err = null;
        try {
            err = new JSONObject(errStr);
            if (result != null) {
                err.put("result", result);
            }
        } catch (JSONException ex) {
            Logger.log4j.error(ex.getMessage());
        }
        return err;
    }

    private static JSONObject _getTestCache(@Required String userId) {
        TestCacheData cacheData = TestsCacheHandler._getCurrentCache(userId);
        JSONObject result = null;
        try {
            result = new JSONObject(cacheData);
            long timeLeft = TestsCacheHandler._getTimeLeft(cacheData);
            result.put("timeLeft", timeLeft);
        } catch (JSONException ex) {
            Logger.log4j.error(ex.getMessage());
        }
        return result;
    }

    protected static JSONObject _verifyTestCache(String testId) {
        Logger.log4j
                .info("VERIFYING Test Cache data >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> for test = "
                        + testId);
        String userId = session.get("userId");
        String strResponse = TestsCacheHandler._verifyBeforeTest(userId, testId, session.getId());
        if (strResponse != null && !strResponse.isEmpty()) {
            JSONObject result = _getTestCache(userId);
            return _getTestResponseJSON(strResponse, "", result);
        } else {
            return null;
        }
    }

    protected static JSONObject _setTestCache(JSONObject testData, String testId, String testName) {
        Logger.log4j.info("Setting Test Cache data >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> for test = "
                + testId);
        String startTimeStr = request.params.get("startTime");
        long startTime = Long.parseLong(startTimeStr);
        long duration = 0;
        if (testData != null && testData.has("duration")) {
            duration = testData.optLong("duration");
            if (duration > 0) {
                duration += ClientUtil.TEST_CACHE_GRACE_TIME_MILLISEC;
            }
        }
        String testAttemptId = _getAttemptId();
        long extraTime = 0;
        if (session.contains("extraTime")) {
            String extraTimeStr = session.get("extraTime");
            extraTime = Long.parseLong(extraTimeStr);
        }
        String userId = session.get("userId");
        String strResponse = TestsCacheHandler._setTestCache(userId, startTime, duration,
                testAttemptId, testId, testName, extraTime, session.getId());
        if (strResponse != null && !strResponse.isEmpty()) {
            JSONObject result = _getTestCache(userId);
            return _getTestResponseJSON(strResponse, "", result);
        } else {
            return null;
        }
    }

    public static void submitTestAnswer() {
        Map<String, Object> allParams = getReqParams();
        String testId = Scope.Params.current().get("testId");
        String testAttemptId = _getAttemptId();
        String canResp = TestsCacheHandler._verifyTestTime(session.get("userId"), testId,
                testAttemptId, session.getId());
        Logger.log4j.info("TEST TIME VERIFICATION DONE RETURN ============= " + canResp);
        if (!canResp.isEmpty()) {
            JSONObject err = _getTestResponseJSON(canResp, "", null);
            renderJSON(err.toString());
            return;
        }
        allParams = _putTestSessions(allParams);
        allParams.put("entityId", testId);
        allParams.put("entityType", "TEST");
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/analytics/recordAttempt", allParams);
        Logger.log4j.info("BEFORE AWAIT : ATTEMPT SUMIT ANSWER");
        await(promise);
        Logger.log4j.info("AFTER AWAIT : ATTEMPT SUMIT ANSWER");
        if (promise != null) {
            JSONObject responseData = ResponseUtil.checkResponse(getJSON(promise));
            renderJSON(responseData.toString());
        }
    }

    public static void resetTestAnswer() {
        Map<String, Object> allParams = getReqParams();
        String testId = Scope.Params.current().get("testId");
        String testAttemptId = _getAttemptId();
        String canResp = TestsCacheHandler._verifyTestTime(session.get("userId"), testId,
                testAttemptId, session.getId());
        Logger.log4j.info("TEST TIME VERIFICATION DONE RETURN ============= " + canResp);
        if (!canResp.isEmpty()) {
            JSONObject err = _getTestResponseJSON(canResp, "", null);
            renderJSON(err.toString());
            return;
        }
        allParams = _putTestSessions(allParams);
        allParams.put("attemptId", testAttemptId);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/analytics/resetQuestionAttempt", allParams);
        Logger.log4j.info("BEFORE AWAIT : ATTEMPT SUMIT ANSWER");
        await(promise);
        Logger.log4j.info("AFTER AWAIT : ATTEMPT SUMIT ANSWER");
        if (promise != null) {
            JSONObject responseData = ResponseUtil.checkResponse(getJSON(promise));
            renderJSON(responseData.toString());
        }
    }

    public static void getQuestionsJson() {
        Map<String, Object> allParams = getReqParams();
        allParams.put("orgId", session.get("loginOrgId"));
        allParams.put("userId", session.get("userId"));
        allParams.put("callingUserId", session.get("userId"));
        allParams.put("callingApp", "learn-app");
        allParams.put("callingAppId", "learn-app");
        JSONObject data = _getQuestionsData(allParams);
        renderJSON(data.toString());
    }

    protected static JSONObject _getQuestionsData(Map<String, Object> allParams) {
        allParams = _putTestSessions(allParams);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/tests/getTestQuestions", allParams);
        Logger.log4j.info("BEFORE AWAIT TEST-CREATION:GET Test Question");
        await(promise);
        Logger.log4j.info("AFTER AWAIT TEST-CREATION:GET Test Question");
        JSONObject quesData = ResponseUtil.checkResponse(getJSON(promise));
        try {
            quesData = quesData.getJSONObject("result");
        } catch (JSONException ex) {
            quesData = null;
            Logger.log4j.error(ex.getMessage());
        }
        return quesData;
    }

    protected static JSONObject _createTestAnalytics(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/analytics/startAttempt", allParams);
        Logger.log4j.info("BEFORE AWAIT : CREATE USER TEST ANALYTICS");
        await(promise);
        Logger.log4j.info("AFTER AWAIT : CREATE USER TEST ANALYTICS");
        JSONObject userAnalyticsData = ResponseUtil.checkResponse(getJSON(promise));
        String testAttemptId = null;
        try {
            testAttemptId = userAnalyticsData.getJSONObject("result").getJSONObject("info")
                    .getString("id");
            _setTestSessions(testAttemptId, null, null);
        } catch (JSONException ex) {
            Logger.log4j.error(ex.getMessage());
        }
        return userAnalyticsData;
    }

    protected static JSONObject _getTestDetails(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/tests/getTestInfo", allParams);
        Logger.log4j.info("BEFORE AWAIT TEST-CREATION:GET TOPICS");
        await(promise);
        Logger.log4j.info("AFTER AWAIT TEST-CREATION:GET TOPICS");
        JSONObject data = ResponseUtil.checkResponse(getJSON(promise));
        data = Validation.verifyResponse(data);
        String error = null;
        String errorMsg = null;
        try {
            error = data.getString("errorCode");
            errorMsg = data.getString("errorMessage");
            if (error.length() > 0) {
                // data=null;
            }
        } catch (JSONException ex) {
            Logger.log4j.error(ex.getMessage());
            return null;
        }
        try {
            data = data.getJSONObject("result");
            data.put("errorCode", error);
            data.put("errorMessage", errorMsg);
        } catch (JSONException ex) {
            Logger.log4j.error(ex.getMessage());
            // data = null;
        }
        return data;
    }

    public static void terminateTest(@Required String testId) {
        Map<String, Object> allParams = getReqParams();
        allParams.put("entityId", testId);
        allParams.put("entityType", "TEST");
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/analytics/endAttempt", allParams);
        Logger.log4j.info("BEFORE AWAIT : ATTEMPT END TEST");
        await(promise);
        Logger.log4j.info("AFTER AWAIT :  ATTEMPT END TEST");
        JSONObject postTestResp = ResponseUtil.checkResponse(getJSON(promise));
        util.TestsCacheHandler._clearCache(session.get("userId").toString());
        _setTestSessions();
        renderJSON(postTestResp.toString());
    }

    public static void ping() {
        renderJSON("true");
    }

    public static void quesSolutions(){
        String orgId = session.get("loginOrgId");
        Map<String, Object> allParams = getReqParams();
        allParams.put("orgId",orgId);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                +"/questions/getSolutions",allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject solutions = getJSON(promise);
        render(renderTheme(orgId, getHTMLFilePath("Library", "quesSolutions")),solutions);
    }

    public static void resetStudentTest(){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/analytics/resetStudentTest", null);
        Logger.log4j.info("BEFORE AWAIT TEST-CREATION:GET TOPICS");
        await(promise);
        Logger.log4j.info("AFTER AWAIT TEST-CREATION:GET TOPICS");
        JSONObject testDetails = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(testDetails.toString());
    }
}
