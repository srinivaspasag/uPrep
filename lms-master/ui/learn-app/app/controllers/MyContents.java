package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.data.validation.Required;
import play.libs.F.Promise;
import play.mvc.Scope;
import play.mvc.Scope.Params;
import play.mvc.With;
import util.ClientUtil;
import util.ResponseUtil;
import util.Validation;

@With(Security.class)
public class MyContents extends AbstractUIController {

    static final String className = MyContents.class.getSimpleName();

    public static void changeProgram() throws JSONException {
        String orgId = session.get("loginOrgId");
        if (orgId != null && !"".equals(orgId)) {
            Map<String, Object> allParams = getReqParams();
            allParams.put("orgId", orgId);
            allParams.put("userId", session.get("userId"));
            allParams.put("targetUserId", session.get("userId"));
            recordActivity(ClientUtil.ActivityPages.LIBRARY, ClientUtil.ActivityAction.OPEN);
            Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                    + "/application/getContentResponse", allParams);
            Logger.log4j.info("BEFORE AWAIT");
            await(promise);
            Logger.log4j.info("AFTER AWAIT");
            JSONObject resp = getJSON(promise);
            render(renderTheme(orgId, getHTMLFilePath("Library", "subjects")), resp);
        } else {
            render(renderTheme(orgId, getHTMLFilePath("errors", "404")));
        }
    }

    public static void subject(String programId, String parentId) throws JSONException {
        String orgId = session.get("loginOrgId");
        if (orgId != null && !"".equals(orgId)) {
            Map<String, Object> allParams = getReqParams();
            List<String> brdIds = new ArrayList<String>();
            brdIds.add(parentId);
            allParams.put("brdIds", brdIds);
            allParams.put("orgId", orgId);
            allParams.put("userId", session.get("userId"));
            allParams.put("targetUserId", session.get("userId"));
            recordActivity(ClientUtil.ActivityPages.LIBRARY, ClientUtil.ActivityAction.OPEN);
            Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                    + "/application/getContentResponse", allParams);
            Logger.log4j.info("BEFORE AWAIT");
            await(promise);
            Logger.log4j.info("AFTER AWAIT");
            JSONObject resp = getJSON(promise);
            String includeChaptersFile = renderTheme(orgId, getHTMLFilePath("Library", "chapters"));
            render(renderTheme(orgId, getHTMLFilePath("Institute", "libraryChapters")), resp,
                    includeChaptersFile, parentId, programId);
        } else {
            render(renderTheme(orgId, getHTMLFilePath("errors", "404")));
        }
    }

    public static void subjectDirect(String programId, String parentId) {
        String orgId = session.get("loginOrgId");
        if ((orgId != null && !"".equals(orgId)) || (parentId != null && !"".equals(parentId))) {
            JSONObject myOrgInfo = Institute._setOrgParams(orgId);
            recordActivity(ClientUtil.ActivityPages.LIBRARY, ClientUtil.ActivityAction.OPEN);
            flash.put("ENTRY", "DIRECT");
            Map<String, Object> allParams = getReqParams();
            List<String> brdIds = new ArrayList<String>();
            brdIds.add(parentId);
            allParams.put("brdIds", brdIds);
            allParams.put("orgId", orgId);
            allParams.put("userId", session.get("userId"));
            allParams.put("targetUserId", session.get("userId"));
            Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                    + "/application/getContentResponse", allParams);
            Logger.log4j.info("BEFORE AWAIT");
            await(promise);
            Logger.log4j.info("AFTER AWAIT");
            JSONObject resp = getJSON(promise);
            String includeChaptersFile = renderTheme(orgId, getHTMLFilePath("Library", "chapters"));
            String includeInstFile = renderTheme(orgId,
                    getHTMLFilePath("Institute", "libraryChapters"));
            render(renderTheme(orgId, getHTMLFilePath(null, "header")), includeInstFile, resp,
                    myOrgInfo, includeChaptersFile, parentId, programId);
        } else {
            render(renderTheme(orgId, getHTMLFilePath("errors", "404")));
        }
    }

    public static void changeSubject(String programId, String parentId) throws JSONException {
        String orgId = session.get("loginOrgId");
        if (orgId != null && !"".equals(orgId)) {
            Map<String, Object> allParams = getReqParams();
            List<String> brdIds = new ArrayList<String>();
            brdIds.add(parentId);
            allParams.put("brdIds", brdIds);
            allParams.put("orgId", orgId);
            allParams.put("userId", session.get("userId"));
            allParams.put("targetUserId", session.get("userId"));
            recordActivity(ClientUtil.ActivityPages.LIBRARY, ClientUtil.ActivityAction.OPEN);
            Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                    + "/application/getContentResponse", allParams);
            Logger.log4j.info("BEFORE AWAIT");
            await(promise);
            Logger.log4j.info("AFTER AWAIT");
            JSONObject resp = getJSON(promise);
            render(renderTheme(orgId, getHTMLFilePath("Library", "chapters")), resp, parentId,
                    programId);
        } else {
            render(renderTheme(orgId, getHTMLFilePath("errors", "404")));
        }
    }

    public static void chapter(String parentId, String chapterId) throws JSONException {
        String orgId = session.get("loginOrgId");
        if (orgId != null && !"".equals(orgId)) {
            Map<String, Object> allParams = getReqParams();
            allParams.put("orgId", orgId);
            allParams.put("userId", session.get("userId"));
            allParams.put("targetUserId", session.get("userId"));
            allParams.put("keepModuleResult", true);
            recordActivity(ClientUtil.ActivityPages.LIBRARY, ClientUtil.ActivityAction.OPEN);
            Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                    + "/application/getContentResponse", allParams);
            Logger.log4j.info("BEFORE AWAIT");
            await(promise);
            Logger.log4j.info("AFTER AWAIT");
            JSONObject resp = getJSON(promise);
            String includeContentFile = renderTheme(orgId,
                    getHTMLFilePath("Library", "contentList"));
            render(renderTheme(orgId, getHTMLFilePath("Institute", "subjectContent")), resp,
                    includeContentFile, chapterId, parentId);
        } else {
            render(renderTheme(orgId, getHTMLFilePath("errors", "404")));
        }
    }

    public static void chapterDirect(String parentId, String chapterId) {
        String orgId = session.get("loginOrgId");
        if (orgId != null && !"".equals(orgId)) {
            JSONObject myOrgInfo = Institute._setOrgParams(orgId);
            Map<String, Object> allParams = getReqParams();
            List<String> brdIds = new ArrayList<String>();
            brdIds.add(chapterId);
            allParams.put("brdIds", brdIds);
            allParams.put("keepModuleResult", true);
            recordActivity(ClientUtil.ActivityPages.LIBRARY, ClientUtil.ActivityAction.OPEN);
            flash.put("ENTRY", "DIRECT");
            Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                    + "/application/getContentResponse", allParams);
            Logger.log4j.info("BEFORE AWAIT");
            await(promise);
            Logger.log4j.info("AFTER AWAIT");
            JSONObject resp = getJSON(promise);
            String includeContentFile = renderTheme(orgId,
                    getHTMLFilePath("Library", "contentList"));
            String includeInstFile = renderTheme(orgId,
                    getHTMLFilePath("Institute", "subjectContent"));
            render(renderTheme(orgId, getHTMLFilePath(null, "header")), includeInstFile, resp,
                    myOrgInfo, includeContentFile, parentId, chapterId);
        } else {
            render(renderTheme(orgId, getHTMLFilePath("errors", "404")));
        }
    }

    public static void changeChapter(String parentId, String chapterId, String programId)
            throws JSONException {
        String orgId = session.get("loginOrgId");
        if (orgId != null && !"".equals(orgId)) {
            Map<String, Object> allParams = getReqParams();
            allParams.put("orgId", orgId);
            allParams.put("userId", session.get("userId"));
            allParams.put("targetUserId", session.get("userId"));
            allParams.put("keepModuleResult", true);
            recordActivity(ClientUtil.ActivityPages.LIBRARY, ClientUtil.ActivityAction.OPEN);
            Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                    + "/application/getContentResponse", allParams);
            Logger.log4j.info("BEFORE AWAIT");
            await(promise);
            Logger.log4j.info("AFTER AWAIT");
            JSONObject resp = getJSON(promise);
            render(renderTheme(orgId, getHTMLFilePath("Library", "contentList")), resp, parentId,
                    chapterId, programId);
        } else {
            render(renderTheme(orgId, getHTMLFilePath("errors", "404")));
        }
    }

    public static void getTests() {
        String orgId = session.get("loginOrgId");
        Map<String, Object> allParams = getReqParams();
        allParams.put("orgId", orgId);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/application/getContentResponse", allParams);
        Logger.log4j.info("BEFORE AWAIT PUBLISH Test");
        await(promise);
        Logger.log4j.info("AFTER AWAIT PUBLISH from Test");
        JSONObject resp = getJSON(promise);
        render(renderTheme(orgId, getHTMLFilePath("Library", "tests")), resp);
    }

    public static void getDocuments() {
        String orgId = session.get("loginOrgId");
        Map<String, Object> allParams = getReqParams();
        allParams.put("orgId", orgId);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/application/getContentResponse", allParams);
        Logger.log4j.info("BEFORE AWAIT PUBLISH Test");
        await(promise);
        Logger.log4j.info("AFTER AWAIT PUBLISH from Test");
        JSONObject resp = getJSON(promise);
        render(renderTheme(orgId, getHTMLFilePath("Library", "documents")), resp);
    }

    public static void getModules() {
        String orgId = session.get("loginOrgId");
        Map<String, Object> allParams = getReqParams();
        allParams.put("orgId", orgId);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/application/getContentResponse", allParams);
        Logger.log4j.info("BEFORE AWAIT PUBLISH Test");
        await(promise);
        Logger.log4j.info("AFTER AWAIT PUBLISH from Test");
        JSONObject resp = getJSON(promise);
        render(renderTheme(orgId, getHTMLFilePath("Library", "modules")), resp);
    }

    public static void getVideos() {
        String orgId = session.get("loginOrgId");
        Map<String, Object> allParams = getReqParams();
        allParams.put("orgId", orgId);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/application/getContentResponse", allParams);
        Logger.log4j.info("BEFORE AWAIT PUBLISH Test");
        await(promise);
        Logger.log4j.info("AFTER AWAIT PUBLISH from Test");
        JSONObject resp = getJSON(promise);
        render(renderTheme(orgId, getHTMLFilePath("Library", "videos")), resp);
    }

    public static void modulePageDirect(@Required String id) {

        Map<String, Object> allParams = getReqParams();
        String orgId = session.get("loginOrgId");
        if (orgId != null && !"".equals(orgId)) {
            JSONObject myOrgInfo = Institute._setOrgParams(orgId);
            recordActivity(ClientUtil.ActivityPages.MODULE, ClientUtil.ActivityAction.OPEN);
            flash.put("ENTRY", "DIRECT");
            allParams.put("id", id);
            allParams.put("orgId", orgId);
            allParams.put("userId", session.get("userId"));
            allParams.put("targetUserId", session.get("userId"));
            JSONObject moduleInfo = _getModuleInfo(allParams);
            String includeInstFile = renderTheme(orgId, getHTMLFilePath("Library", "modulePage"));
            render(renderTheme(orgId, getHTMLFilePath(null, "header")), includeInstFile,
                    moduleInfo, myOrgInfo);
            try {
                _markEntityView(id, ClientUtil.Entity.MODULE);
            } catch (Exception err) {
            }
        } else {
            render(renderTheme(orgId, getHTMLFilePath("errors", "404")));
        }
    }

    public static void modulePage(@Required String id) {

        Map<String, Object> allParams = getReqParams();
        String orgId = session.get("loginOrgId");
        if (orgId != null && !"".equals(orgId)) {
            JSONObject myOrgInfo = Institute._setOrgParams(orgId);
            recordActivity(ClientUtil.ActivityPages.MODULE, ClientUtil.ActivityAction.OPEN);
            flash.put("ENTRY", "DIRECT");
            allParams.put("id", id);
            allParams.put("orgId", orgId);
            allParams.put("userId", session.get("userId"));
            allParams.put("targetUserId", session.get("userId"));
            JSONObject moduleInfo = _getModuleInfo(allParams);
            render(renderTheme(orgId, getHTMLFilePath("Library", "modulePage")), moduleInfo,
                    myOrgInfo);
            try {
                _markEntityView(id, ClientUtil.Entity.MODULE);
            } catch (Exception err) {
            }
        } else {
            render(renderTheme(orgId, getHTMLFilePath("errors", "404")));
        }
    }

    public static void preTestPageDirect(@Required String id) {
        Map<String, Object> allParams = getReqParams();
        String orgId = session.get("loginOrgId");
        if (orgId != null && !"".equals(orgId)) {
            JSONObject myOrgInfo = Institute._setOrgParams(orgId);
            recordActivity(ClientUtil.ActivityPages.PRE_TEST, ClientUtil.ActivityAction.ATTEMPTED,
                    ClientUtil.Entity.TEST, id);
            request.params.put("targetUserId", session.get("userId"));
            boolean isAttempted = _isReAttemptTest(id);
            if (isAttempted) {
                testPageDirect(id);
            }
            flash.put("ENTRY", "DIRECT");
            allParams = _putTestEntityParams(id, allParams);
            allParams.put("orgId", orgId);
            JSONObject testInfo = _getTestDetails(allParams);
            allParams.put("id", id);
            allParams.put("orgId", orgId);
            allParams.put("userId", session.get("userId"));
            allParams.put("targetUserId", session.get("userId"));
            String targetUserRole = Scope.Params.current().get("userRole");
            if (targetUserRole.equals("STUDENT")) {
                String includeInstFile = renderTheme(orgId,
                        getHTMLFilePath("Library", "preTestPage"));
                render(renderTheme(orgId, getHTMLFilePath(null, "header")), includeInstFile,
                        testInfo, myOrgInfo);
            } else {
                testPageDirect(id);
            }
            try {
                _markEntityView(id, ClientUtil.Entity.TEST);
            } catch (Exception err) {
            }
        } else {
            render(renderTheme(orgId, getHTMLFilePath("errors", "404")));
        }
    }

    public static void preTestPage(@Required String id) {

        Map<String, Object> allParams = getReqParams();
        String orgId = session.get("loginOrgId");
        if (orgId != null && !"".equals(orgId)) {
            JSONObject myOrgInfo = Institute._setOrgParams(orgId);
            recordActivity(ClientUtil.ActivityPages.PRE_TEST, ClientUtil.ActivityAction.ATTEMPTED,
                    ClientUtil.Entity.TEST, id);
            request.params.put("targetUserId", session.get("userId"));
            boolean isAttempted = _isReAttemptTest(id);
            if (isAttempted) {
                testPage(id);
            }
            flash.put("ENTRY", "DIRECT");
            allParams = _putTestEntityParams(id, allParams);
            allParams.put("orgId", orgId);
            JSONObject testInfo = _getTestDetails(allParams);
            allParams.put("id", id);
            allParams.put("orgId", orgId);
            allParams.put("userId", session.get("userId"));
            allParams.put("targetUserId", session.get("userId"));
            String targetUserRole = Scope.Params.current().get("userRole");
            if (targetUserRole.equals("STUDENT")) {
                render(renderTheme(orgId, getHTMLFilePath("Library", "preTestPage")), testInfo,
                        myOrgInfo);
            } else {
                testPage(id);
            }
            try {
                _markEntityView(id, ClientUtil.Entity.TEST);
            } catch (Exception err) {
            }
        } else {
            render(renderTheme(orgId, getHTMLFilePath("errors", "404")));
        }
    }

    public static void testPage(@Required String id) {
        String orgId = session.get("loginOrgId");
        if (orgId != null && !"".equals(orgId)) {
            JSONObject myOrgInfo = Institute._setOrgParams(orgId);
            Application.recordActivity(ClientUtil.ActivityPages.TEST_ANALYTICS,
                    ClientUtil.ActivityAction.ATTEMPTED, ClientUtil.Entity.TEST, id);
            Map<String, Object> allParams = getReqParams();
            allParams = _putTestEntityParams(id, allParams);
            String targetUserRole = Scope.Params.current().get("userRole");
            if (targetUserRole == null || targetUserRole.isEmpty()) {
                targetUserRole = targetUserRole == null ? "STUDENT" : targetUserRole;
            }
            JSONObject testInfo = _getTestDetails(allParams);
            JSONObject testAnalytics = null;
            JSONObject marksDistribution = _getTestMarksDistribution(allParams);
            JSONObject toppersData = _getToppers(allParams);
            allParams.put("entity.id", id);
            allParams.put("entity.type", "TEST");
            JSONObject questions = Tests._getTestAnalyticsQuestions(allParams);
            JSONObject resultSheet = Tests.testResultSheet(allParams);
            try {
                _markEntityView(id, ClientUtil.Entity.TEST);
            } catch (Exception err) {
            }
            if (targetUserRole.equals("STUDENT")) {
                testAnalytics = _getUserTestAnalytics(allParams);
                render(renderTheme(orgId, getHTMLFilePath("Library", "postTestPage")), testInfo,
                        testAnalytics, marksDistribution, questions, resultSheet, myOrgInfo);
            } else {
                JSONObject questionsData = Tests._getTeacherTestAnalyticsQuestions(allParams);
                render(renderTheme(orgId, getHTMLFilePath("Library", "postTestTeacherPage")),
                        testInfo, marksDistribution, resultSheet, myOrgInfo,targetUserRole,toppersData,questionsData);
            }
        } else {
            render(renderTheme(orgId, getHTMLFilePath("errors", "404")));
        }
    }

    public static void testPageDirect(@Required String id) {
        String orgId = session.get("loginOrgId");
        if (orgId != null && !"".equals(orgId)) {
            request.params.put("targetUserId", session.get("userId"));
            JSONObject myOrgInfo = Institute._setOrgParams(orgId);
            Application.recordActivity(ClientUtil.ActivityPages.TEST_ANALYTICS,
                    ClientUtil.ActivityAction.ATTEMPTED, ClientUtil.Entity.TEST, id);
            Map<String, Object> allParams = getReqParams();
            allParams = _putTestEntityParams(id, allParams);
            boolean isAttempted = _isReAttemptTest(id);
            String targetUserRole = Scope.Params.current().get("userRole");
            if (!isAttempted && "STUDENT".equals(targetUserRole)) {
                preTestPageDirect(id);
            }
            if (targetUserRole == null || targetUserRole.isEmpty()) {
                targetUserRole = targetUserRole == null ? "STUDENT" : targetUserRole;
            }
            JSONObject testInfo = _getTestDetails(allParams);
            JSONObject testAnalytics = null;
            JSONObject toppersData = _getToppers(allParams);
            JSONObject marksDistribution = _getTestMarksDistribution(allParams);
            allParams.put("entity.id", id);
            allParams.put("entity.type", "TEST");
            JSONObject questions = Tests._getTestAnalyticsQuestions(allParams);
            JSONObject resultSheet = Tests.testResultSheet(allParams);
            JSONObject questionsData = Tests._getTeacherTestAnalyticsQuestions(allParams);
            try {
                _markEntityView(id, ClientUtil.Entity.TEST);
            } catch (Exception err) {
            }
            String includeInstFile = renderTheme(orgId,
                    getHTMLFilePath("Library", "postTestTeacherPage"));
            if (targetUserRole.equals("STUDENT")) {
                testAnalytics = _getUserTestAnalytics(allParams);
                includeInstFile = renderTheme(orgId, getHTMLFilePath("Library", "postTestPage"));
            }
            render(renderTheme(orgId, getHTMLFilePath(null, "header")), includeInstFile, testInfo,
                    testAnalytics, marksDistribution, myOrgInfo, questions, resultSheet,targetUserRole,toppersData,questionsData);
        } else {
            render(renderTheme(orgId, getHTMLFilePath("errors", "404")));
        }
    }

    public static void documentPageDirect(@Required String id) {

        Map<String, Object> allParams = getReqParams();
        String orgId = session.get("loginOrgId");
        if (orgId != null && !"".equals(orgId)) {
            JSONObject myOrgInfo = Institute._setOrgParams(orgId);
            recordActivity(ClientUtil.ActivityPages.DOCUMENT, ClientUtil.ActivityAction.OPEN);
            flash.put("ENTRY", "DIRECT");
            String moduleId = request.params.get("moduleId");
            if (moduleId != null) {
                allParams.put("context.id", moduleId);
                allParams.put("moduleId", moduleId);
                allParams.put("context.type", ClientUtil.Entity.MODULE);
            }
            allParams.put("entity.id", id);
            allParams.put("entity.type", ClientUtil.Entity.DOCUMENT);
            allParams.put("id", id);
            allParams.put("orgId", orgId);
            allParams.put("userId", session.get("userId"));
            allParams.put("targetUserId", session.get("userId"));
            try {
                _markEntityView(id, ClientUtil.Entity.DOCUMENT);
            } catch (Exception err) {
            }
            _markContentCompleted(allParams);
            JSONObject documentInfo = _getDocumentInfo(allParams);
            String includeInstFile = renderTheme(orgId, getHTMLFilePath("Library", "documentPage"));
            render(renderTheme(orgId, getHTMLFilePath(null, "header")), includeInstFile,
                    documentInfo, myOrgInfo);
        } else {
            render(renderTheme(orgId, getHTMLFilePath("errors", "404")));
        }
    }

    public static void documentPage(@Required String id) {

        Map<String, Object> allParams = getReqParams();
        String orgId = session.get("loginOrgId");
        if (orgId != null && !"".equals(orgId)) {
            JSONObject myOrgInfo = Institute._setOrgParams(orgId);
            recordActivity(ClientUtil.ActivityPages.DOCUMENT, ClientUtil.ActivityAction.OPEN);
            flash.put("ENTRY", "DIRECT");
            String moduleId = request.params.get("moduleId");
            allParams.put("entity.id", id);
            allParams.put("entity.type", ClientUtil.Entity.DOCUMENT);
            if (moduleId != null) {
                allParams.put("context.id", moduleId);
                allParams.put("moduleId", moduleId);
                allParams.put("context.type", ClientUtil.Entity.MODULE);
            }
            allParams.put("id", id);
            allParams.put("orgId", orgId);
            allParams.put("userId", session.get("userId"));
            allParams.put("targetUserId", session.get("userId"));
            try {
                _markEntityView(id, ClientUtil.Entity.DOCUMENT);
            } catch (Exception err) {
            }
            _markContentCompleted(allParams);
            JSONObject documentInfo = _getDocumentInfo(allParams);
            render(renderTheme(orgId, getHTMLFilePath("Library", "documentPage")), documentInfo,
                    myOrgInfo);
        } else {
            render(renderTheme(orgId, getHTMLFilePath("errors", "404")));
        }
    }

    public static void videoPageDirect(@Required String id) {

        Map<String, Object> allParams = getReqParams();
        String orgId = session.get("loginOrgId");
        if (orgId != null && !"".equals(orgId)) {
            String moduleId = request.params.get("moduleId");
            allParams.put("entity.id", id);
            allParams.put("entity.type", ClientUtil.Entity.VIDEO);
            if (moduleId != null) {
                allParams.put("context.id", moduleId);
                allParams.put("moduleId", moduleId);
                allParams.put("context.type", ClientUtil.Entity.MODULE);
            }
            allParams.put("orgId", orgId);
            allParams.put("id", id);
            allParams.put("userId", session.get("userId"));
            allParams.put("targetUserId", session.get("userId"));
            allParams.put("callingUserId", session.get("userId"));
            try {
                _markEntityView(id, ClientUtil.Entity.VIDEO);
            } catch (Exception err) {
            }
            _markContentCompleted(allParams);
            JSONObject myOrgInfo = Institute._setOrgParams(orgId);
            recordActivity(ClientUtil.ActivityPages.VIDEO, ClientUtil.ActivityAction.OPEN);
            flash.put("ENTRY", "DIRECT");
            JSONObject videoInfo = _getVideoInfo(allParams);
            putVideoCommentsParams(allParams, id);
            JSONObject comments =  Widgets._getCommItems(allParams);
            String includeInstFile = renderTheme(orgId, getHTMLFilePath("Library", "videoPage"));
            if (moduleId != null) {
                JSONObject nextVideosInfo = getPlaylistVideos(allParams);
                render(renderTheme(orgId, getHTMLFilePath(null, "header")), includeInstFile,
                        videoInfo, myOrgInfo, nextVideosInfo, moduleId,comments);
            }
            render(renderTheme(orgId, getHTMLFilePath(null, "header")), includeInstFile, videoInfo,
                    myOrgInfo, moduleId,comments);
        } else {
            render(renderTheme(orgId, getHTMLFilePath("errors", "404")));
        }
    }

    public static void videoPage(@Required String id, String moduleId) {

        Map<String, Object> allParams = getReqParams();
        String orgId = session.get("loginOrgId");
        if (orgId != null && !"".equals(orgId)) {
            allParams.put("entity.id", id);
            allParams.put("entity.type", ClientUtil.Entity.VIDEO);
            if (moduleId != null) {
                allParams.put("moduleId", moduleId);
                allParams.put("context.id", moduleId);
                allParams.put("context.type", ClientUtil.Entity.MODULE);
            }
            allParams.put("orgId", orgId);
            allParams.put("id", id);
            allParams.put("userId", session.get("userId"));
            allParams.put("targetUserId", session.get("userId"));
            allParams.put("callingUserId", session.get("userId"));
            try {
                _markEntityView(id, ClientUtil.Entity.VIDEO);
            } catch (Exception err) {
            }
            _markContentCompleted(allParams);
            JSONObject myOrgInfo = Institute._setOrgParams(orgId);
            recordActivity(ClientUtil.ActivityPages.VIDEO, ClientUtil.ActivityAction.OPEN);
            flash.put("ENTRY", "DIRECT");
            JSONObject videoInfo = _getVideoInfo(allParams);
            putVideoCommentsParams(allParams, id);
            JSONObject comments =  Widgets._getCommItems(allParams);
            if (moduleId != null) {
                JSONObject nextVideosInfo = getPlaylistVideos(allParams);
                render(renderTheme(orgId, getHTMLFilePath("Library", "videoPage")), videoInfo,
                        myOrgInfo, nextVideosInfo, moduleId,comments);
            }
            render(renderTheme(orgId, getHTMLFilePath("Library", "videoPage")), videoInfo,
                    myOrgInfo, moduleId,comments);
        } else {
            render(renderTheme(orgId, getHTMLFilePath("errors", "404")));
        }
    }

    public static Map<String,Object> putVideoCommentsParams(Map<String,Object> allParams, String id){
        allParams.put("root.id", id);
        allParams.put("root.type", ClientUtil.Entity.VIDEO);
        allParams.put("base.id", id);
        allParams.put("base.type", ClientUtil.Entity.VIDEO);
        allParams.put("parent.id", id);
        allParams.put("parent.type", ClientUtil.Entity.VIDEO);
        allParams.put("start", 0);
        allParams.put("orderBy", "timeCreated");
        return allParams;
    }

    protected static JSONObject _markContentCompleted(Map<String, Object> allParams) {
        Logger.log4j.info(allParams);
        Logger.log4j.info("coming to widgets");
        Promise<JSONResponseWrapper> promise = client(ClientUtil.SOCIALS_WEB_SERVICE_URL
                + "/socials/completed", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }

    private static JSONObject getPlaylistVideos(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/videos/getPlaylistVideos", allParams);
        Logger.log4j.info("BEFORE AWAIT GET PLAYLIST VIDEOS");
        await(promise);
        Logger.log4j.info("AFTER AWAIT GET PLAYLIST VIDEOS");
        JSONObject videosData = getJSON(promise);
        videosData = Validation.verifyResponse(videosData);
        return videosData;
    }

    private static JSONObject _getToppers(Map<String, Object> allParams) {
        JSONObject toppersData = _getToppersData(allParams);
        return toppersData;
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

    public static JSONObject _getVideoInfo(Map<String, Object> allParams) {

        Promise<JSONResponseWrapper> promise = clientWithAllHeaders(ClientUtil.CONTENT_SERVICE_URL
                + "/videos/getVideo", allParams, null);
        Logger.log4j.info("BEFORE AWAIT : GET MY CONTENTS - VIDEOS");
        await(promise);
        Logger.log4j.info("AFTER AWAIT : GET MY CONTENTS - VIDEOS");
        JSONObject data = getJSON(promise);
        data = Validation.verifyResponse(data);
        return data;
    }

    public static JSONObject _getDocumentInfo(Map<String, Object> allParams) {

        Promise<JSONResponseWrapper> promise = clientWithAllHeaders(ClientUtil.CONTENT_SERVICE_URL
                + "/documents/getDocument", allParams, null);
        Logger.log4j.info("BEFORE AWAIT : GET MY CONTENTS - DOCUMENT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT : GET MY CONTENTS - DOCUMENT");
        JSONObject data = getJSON(promise);
        data = Validation.verifyResponse(data);
        return data;
    }

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

    protected static JSONObject _getTestDetails(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/tests/getTestInfo", allParams);
        Logger.log4j.info("BEFORE AWAIT TEST-CREATION:GET TOPICS");
        await(promise);
        Logger.log4j.info("AFTER AWAIT TEST-CREATION:GET TOPICS");
        JSONObject data = ResponseUtil.checkResponse(getJSON(promise));
        data = Validation.verifyResponse(data);
        return data;
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

    public static JSONObject _getModuleInfo(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/modules/getModule", allParams);
        Logger.log4j.info("BEFORE AWAIT : GET MY CONTENTS - MODULE");
        await(promise);
        Logger.log4j.info("AFTER AWAIT : GET MY CONTENTS - MODULE");
        JSONObject data = ResponseUtil.checkResponse(getJSON(promise));
        return data;
    }

    protected static JSONObject _markEntityView(String entityId, ClientUtil.Entity entityType) {

        Map<String, Object> allParams = getReqParams();
        allParams.put("entity.type", entityType);
        allParams.put("entity.id", entityId);
        ClientUtil.ActivityPages page = null;
        try {
            page = ClientUtil.ActivityPages.valueOf(entityType.name().toUpperCase());
        } catch (Exception ex) {
            page = ClientUtil.ActivityPages.NEW_ENTITY;
        }
        recordActivity(page, ClientUtil.ActivityAction.VIEW, entityType, entityId);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.SOCIALS_WEB_SERVICE_URL
                + "/socials/view", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        return resp;
    }

    public static void videoSolution(@Required String id){
        Map<String, Object> allParams = getReqParams();
        String orgId = session.get("loginOrgId");
        allParams.put("orgId", orgId);
        allParams.put("id", id);
        allParams.put("userId", session.get("userId"));
        allParams.put("targetUserId", session.get("userId"));
        allParams.put("callingUserId", session.get("userId"));
        JSONObject myOrgInfo = Institute._setOrgParams(orgId);
        JSONObject videoInfo = _getVideoInfo(allParams);
        render(renderTheme(orgId, getHTMLFilePath("Library", "videoSolutionTag")), videoInfo,
                myOrgInfo);
    }

    public static String getSubjectName(String subjectFullName){
        String subjectLowerCase = subjectFullName.toLowerCase();
        if(subjectLowerCase.contains("physics")){
            return "Physics";
        }
        if(subjectLowerCase.contains("chemistry")){
            return "Chemistry";
        }
        if(subjectLowerCase.contains("biology")){
            return "Biology";
        }
        if(subjectLowerCase.contains("botany")){
            return "Botany";
        }
        if(subjectLowerCase.contains("mathematics") || subjectLowerCase.contains("maths")){
            return "Mathematics";
        }
        if(subjectLowerCase.contains("zoology")){
            return "Zoology";
        }
        return "Miscellaneous";
    }

}
