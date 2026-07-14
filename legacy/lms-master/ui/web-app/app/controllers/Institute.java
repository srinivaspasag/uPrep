package controllers;

import java.io.File;
import java.net.URLEncoder;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.Play;
import play.cache.Cache;
import play.data.validation.Required;
import play.i18n.Messages;
import play.libs.F;
import play.libs.F.Promise;
import play.mvc.Scope;
import play.mvc.Scope.Params;
import play.mvc.With;
import pojos.OrgInfo;
import pojos.WebInfo;
import uicom.util.ClientUtil;
import uicom.util.ResponseUtil;
import uicom.util.Validation;
import uicom.util.WebUtil;

@With(Security.class)
public class Institute extends AbstractUIController {

    static final Integer maxSimilarDoubts = 10;
    private static final String RESULT_LIST_STR = "{'list':[{id:'',name:'No Sorting',order:'DESC'},{id:'attempts',name:'Most Attempted',order:'DESC'},{id:'attempts',name:'Least Attempted',order:'ASC'}]}";
    private static final String COMMENT_LIST_STR = "{'list':[{id:'timeCreated',name:'Older',order:'ASC'},{id:'upVotes',name:'Most Voted',order:'DESC'}]}";

    protected static JSONArray _getUserOrgs(Map<String, Object> allParams, boolean setOrgInSession) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/getAssociatedOrgsOfUser", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject orgs = getJSON(promise);
        orgs = Validation.verifyResponse(orgs);
        JSONArray orgArray = null;
        try {
            orgArray = orgs.getJSONObject("result").getJSONArray("list");
            if (setOrgInSession) {
                JSONObject org = orgArray.getJSONObject(0);
                if (!"ACTIVE".equals(org.getString("userState")) || !"ACTIVE".equals(org.getString("orgStudentPageStatus"))) {
                    session.put("BLOCKED_ORG_ID", org.getString("id"));
                }
                _setUserSession(org);
            }
        } catch (JSONException ex) {
            Logger.log4j.error(ex.getMessage());
        }
        return orgArray;
    }

    private static void _setUserSession(JSONObject org) {
        try {
            session.put("profilePic", org.getString("thumbnail"));
            session.put("firstName", org.getString("firstName"));
            session.put("lastName", org.getString("lastName"));
            session.put("fullname", org.getString("firstName") + " " + org.getString("lastName"));
            if(org.has("authType")){
                session.put("userAuthType", org.get("authType"));
            }
        } catch (Exception e) {
            Logger.log4j.error("Error in setting user session params from org Info fetch.......");
        }
    }

    private static OrgInfo _getOrgParamsFromService(String orgId) {
        String userRole = "";
        String memberId = "";
        String instFullName = "";
        String instLogo = "";
        String type = "";
        String firstName = "";
        String lastName = "";
        String fullName = "";
        String profilePic = "";
        String orgMemberId = "";
        JSONArray orgArray = _getUserOrgs(null, false);
        for (int i = 0; i < orgArray.length(); i++) {
            try {
                JSONObject org = orgArray.getJSONObject(i);
                String _id = org.getString("id");
                if (orgId.equals(_id)) {
                    instFullName = org.getString("fullName");
                    instLogo = org.getString("orgThumbnail");
                    type = org.getString("type");
                    memberId = org.getString("memberId");
                    userRole = org.getString("profile");
                    firstName = org.getString("firstName");
                    lastName = org.getString("lastName");
                    fullName = org.getString("firstName") + " " + org.getString("lastName");
                    profilePic = org.getString("thumbnail");
                    orgMemberId = org.getString("orgMemberId");
                    _setUserSession(org);
                    OrgInfo info = new OrgInfo(orgId, userRole, memberId, instFullName, instLogo, type, firstName, lastName, fullName, profilePic, orgMemberId);
                    return info;
                }
            } catch (Exception ex) {
                Logger.log4j.error("============================== ERROR ::::: " + ex.getMessage());
                redirectToNoOrgError();
            }
        }
        return null;
    }

    private static String _getInstCacheKey(String orgId) {
        String userId = session.get("userId");
        String[] val = {"ORG_INFO/", orgId, "/", userId};
        String key = StringUtils.join(val, "");
        return key;
    }

    protected static boolean _clearInstCacheKey(String orgId, String userId) {
        String key = _getInstCacheKey(orgId);
        Cache.delete(key);
        return true;
    }

    protected static OrgInfo _getOrgParams(String orgId) {
        String key = _getInstCacheKey(orgId);
        OrgInfo org = Cache.get(key, OrgInfo.class);
        if (org == null) {
            org = _getOrgParamsFromService(orgId);
            Cache.safeSet(key, org, "5mn");
        }
        return org;
    }

    protected static boolean _forceCleanOrgCache(String orgId) {
        String key = _getInstCacheKey(orgId);
        OrgInfo org = Cache.get(key, OrgInfo.class);
        if (org != null) {
            return Cache.safeDelete(key);
        }
        return true;
    }

    protected static JSONObject _setOrgParams(String orgId) {
        if (StringUtils.isEmpty(orgId)) {
            orgId = Scope.Params.current().get("orgId");
        }
        OrgInfo orgInfo = _getOrgParams(orgId);
        JSONObject org = null;
        if (orgInfo != null) {
            org = new JSONObject(orgInfo);
            session.put("myOrgInfo", org);
            Params p = Scope.Params.current();
            p.put("orgId", orgId);
            try {
                p.put("userRole", org.getString("userRole"));
                p.put("memberId", org.getString("memberId"));
            } catch (JSONException ex) {
                Logger.log4j.error("==============================ERROR ::::: " + ex.getMessage());
            }
            p.put("parent.type", "ORGANIZATION");
            p.put("parent.id", orgId);
        } else {
            redirectToNoOrgError();
        }
        JSONObject orgCompleteInfo = _getOrgInfo(null);
        Logger.log4j.info("orgComplete info :"+ orgCompleteInfo);
        if(orgCompleteInfo != null){
            try {
                JSONObject result = orgCompleteInfo.getJSONObject("result");
                org.put("doubtsForumMode", result.getString("doubtsForumMode"));
                org.put("showClassroomConnect", result.getString("showClassroomConnect"));
                org.put("enableOTP", result.getString("enableOTP"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            try {
                org.put("doubtsForumMode", "HIDDEN");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return org;
    }

    public static void redirectToNoOrgError() {
        String redirectUrl = "/Institute/notPartOfOrg";
        render("UIComRegister/redirectPage.html", redirectUrl);
    }

    public static void notPartOfOrg() {
        render();
    }

    public static void home() {
        String includeInstFile = "Institute/home.html";
        JSONObject myOrgInfo = _setOrgParams(null);
        render("Institute/header.html", includeInstFile, myOrgInfo);
    }

    protected static JSONObject _getOrgInfo(Map<String, Object> allParams) {

        F.Promise<AbstractUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/getOrganization", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }

    public static void homeFromInside() {
        JSONObject myOrgInfo = _setOrgParams(null);
        recordActivity(ClientUtil.ActivityPages.STATUS_FEED, ClientUtil.ActivityAction.OPEN);
        render("Institute/home.html", myOrgInfo);
    }

    public static void getMyBatchMembers() {
        Map<String, Object> allParams = getReqParams();
        allParams.put("role", "MEMBER");
        JSONObject memberResponse = _reqMembersInfo(allParams);
        renderJSON(memberResponse.toString());
    }

    public static void addNewDoubt() {
        JSONObject discussion = askDoubt(getReqParams());
        JSONObject discuss = null;
        String errorMessage = "";
        try {
            discuss = discussion.getJSONObject("result");
            errorMessage = discussion.getString("errorMessage");
        } catch (JSONException ex) {
            Logger.log4j.error(ex);
        }
        if (!discuss.has("id")) {
            discuss = null;
        } else {
            try {
                recordActivity(ClientUtil.ActivityPages.DOUBTS, ClientUtil.ActivityAction.ADD, ClientUtil.Entity.DISCUSSION, discuss.getString("id"));
            } catch (JSONException ex) {
            }
        }
        render("tags/institute/widgets/doubt.html", discuss, errorMessage);
    }

    public static void getDoubts() {
        JSONObject discussions = getInstituteDoubts(getReqParams());
        render("tags/institute/doubts.html", discussions);
    }

    public static void openDoubt() {
        Map<String, Object> allParams = getReqParams();
        JSONObject discussion = _getDiscussion(allParams);
        JSONObject sortTypeList = _getArrayFromStrList(COMMENT_LIST_STR);
        render(discussion, sortTypeList);
    }

    public static void getSimilarDoubts() {
        Map<String, Object> allParams = getReqParams();
        allParams.put("size", maxSimilarDoubts);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/discussions/getSimilarDiscussions", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject questions = getJSON(promise);
        questions = Validation.verifyResponse(questions);
        render("tags/institute/similarDoubts.html", questions);
    }

    protected static JSONObject askDoubt(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/discussions/addDiscussion", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject discussion = getJSON(promise);
        discussion = Validation.verifyResponse(discussion);
        return discussion;

    }

    protected static JSONObject getInstituteDoubts(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/discussions/getDiscussions", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject instituteResponse = getJSON(promise);
        instituteResponse = Validation.verifyResponse(instituteResponse);
        return instituteResponse;
    }

    protected static JSONObject _getDiscussion(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/discussions/getDiscussionInfo", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject discussion = getJSON(promise);
        discussion = Validation.verifyResponse(discussion);
        try {
            Application._markEntityView(Scope.Params.current().get("id"), ClientUtil.Entity.DISCUSSION);
        } catch (Exception err) {
        }
        return discussion;
    }

    public static void homeDoubts(@Required String orgId) {
        if (StringUtils.isNotEmpty(orgId)) {
            JSONObject myOrgInfo = _setOrgParams(orgId);
            request.params.put("instHomePg", "Institute/doubtUi.html");
            recordActivity(ClientUtil.ActivityPages.DOUBTS, ClientUtil.ActivityAction.OPEN);
            flash.put("ENTRY", "DIRECT");
            String includeInstFile = "Institute/home.html";
            render("Institute/header.html", includeInstFile, myOrgInfo);
        } else {
            render("errors/404.html");
        }
    }

    public static void homeSingleDoubt(@Required String orgId, String id) {
        if (StringUtils.isNotEmpty(orgId)) {
            JSONObject myOrgInfo = _setOrgParams(orgId);
            JSONObject discussion = _getDiscussion(null);
            request.params.put("instHomePg", "Institute/openDoubt.html");
            JSONObject sortTypeList = _getArrayFromStrList(COMMENT_LIST_STR);
            flash.put("ENTRY", "DIRECT");
            String includeInstFile = "Institute/home.html";
            render("Institute/header.html", includeInstFile, discussion, sortTypeList, myOrgInfo);
        } else {
            render("errors/404.html");
        }
    }

    // institute metadata
    public static void membersUi() {
        Map<String, Object> allParams = getReqParams();
        recordActivity(ClientUtil.ActivityPages.MEMBERS, ClientUtil.ActivityAction.OPEN);
        Scope.Params.current().put("role", "MEMBER");
        allParams.put("size", "-1");
        JSONObject courses = _getCourses(allParams);
        JSONObject memberInfo = _getViewableDomains(allParams);
        /*
         * JSONObject centers = _getProgrammes(allParams);
         * allParams.put("size","8"); JSONObject instMembers =
         * _reqMembersInfo(allParams);
         */
        render(memberInfo, courses);
    }

    public static void homeMembersUi(@Required String orgId, String userId) {
        if (StringUtils.isNotEmpty(orgId)) {
            JSONObject myOrgInfo = _setOrgParams(orgId);
            recordActivity(ClientUtil.ActivityPages.MEMBERS, ClientUtil.ActivityAction.OPEN);
            Scope.Params.current().put("role", "MEMBER");
            JSONObject courses = _getCourses(null);
            JSONObject memberInfo = _getViewableDomains(null);;
            request.params.put("instHomePg", "Institute/membersUi.html");
            flash.put("ENTRY", "DIRECT");
            String includeInstFile = "Institute/home.html";
            render("Institute/header.html", includeInstFile, courses, memberInfo, myOrgInfo);
        } else {
            render("errors/404.html");
        }
    }

    public static void getCentersByProgramme() {
        Map<String, Object> allParams = getReqParams();
        JSONObject centers = _getCenters(allParams);
        render("tags/institute/centerList.html", centers);
    }

    public static void searchMembers() {
        Map<String, Object> allParams = getReqParams();
        JSONObject users = _reqMembersInfo(allParams);
        boolean canImpersonate = false;
        try {
            request.params.put("targetUserId", session.get("callingUserId"));
            JSONObject resp = _getMemberInfo(null);
            canImpersonate = resp.getJSONObject("result").getJSONObject("info").getBoolean("canImpersonate");
        } catch (Exception ex) {
        }
        render("tags/institute/members.html", users, canImpersonate);
    }

    public static void suggMembers() {
        JSONObject users = _reqMembersInfo(getReqParams());
        render("tags/institute/previews/userSugg.html", users);
    }

    public static void searchMembersTA() {
        JSONObject users = _reqMembersInfo(getReqParams());
        render("tags/institute/membersTA.html", users);
    }

    protected static JSONObject _reqInstituteInfo(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/getOrganization", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject instituteResponse = getJSON(promise);
        instituteResponse = Validation.verifyResponse(instituteResponse);
        return instituteResponse;
    }

    protected static JSONObject _reqMembersInfo(Map<String, Object> allParams) {
        if (allParams != null) {
            allParams.put("excludeProfiles", "OFFLINE_USER");
        } else {
            request.params.put("excludeProfiles", "OFFLINE_USER");
        }
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/members/getMembers", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject memberResponse = getJSON(promise);
        memberResponse = Validation.verifyResponse(memberResponse);
        return memberResponse;
    }

    protected static JSONObject _getCourses(Map<String, Object> allParams) {
        allParams = getReqParams();
        allParams.put("recordState", "ACTIVE");
        allParams.put("context", "ORG");
        allParams.put("type", "COURSE");
        allParams.put("size", 30);
        allParams.put("ownerId", Scope.Params.current().get("orgId"));
        Promise<JSONResponseWrapper> promise
                = client(ClientUtil.BOARDS_SERVICE_URL + "/boards/getChildren", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        resp = Validation.verifyResponse(resp);
        return resp;
    }

    protected static JSONObject _getProgrammes(Map<String, Object> allParams) {
        allParams.put("addStreamInfo", true);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/getProgrammes", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject data = getJSON(promise);
        data = Validation.verifyResponse(data);
        return data;
    }

    protected static JSONObject _getCenters(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/getProgrammeCenters", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject data = getJSON(promise);
        data = Validation.verifyResponse(data);
        return data;
    }

    public static JSONObject _getMemberInfo(Map<String, Object> allParams) {
        if (allParams == null) {
            allParams = getReqParams();
        }
        String targetUserId = Scope.Params.current().get("targetUserId");
        String userId = session.get("userId");
        if (StringUtils.isEmpty(targetUserId)) {
            allParams.put("targetUserId", userId);
            targetUserId = userId;
        }
//        Logger.log4j.info("Key :: "+targetUserId+"_getMemberInfo");
//        String resp = Cache.get(targetUserId+"_getMemberInfo",String.class);
//        if(!StringUtils.isEmpty(resp)){
//            Logger.log4j.info("Served _getMemberInfo from Cache");
//            JSONObject memberResponse = getCacheDataInJsonObject(resp);
//            return memberResponse;
//        }else{
            Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                    + "/members/getMemberProfile", allParams);
            Logger.log4j.info("BEFORE AWAIT");
            await(promise);
            Logger.log4j.info("AFTER AWAIT");
            JSONObject memberResponse = getJSON(promise);
            memberResponse = Validation.verifyResponse(memberResponse);
//            if(memberResponse != null)
//                Cache.set(targetUserId+"_getMemberInfo", memberResponse.toString(), "30mn");
            return memberResponse;
//        }
    }

    public static JSONObject _getUserInfo(Map<String, Object> allParams) {
        String targetUserId = Scope.Params.current().get("targetUserId");
        String userId = session.get("userId");
        if (allParams == null) {
            allParams = getReqParams();
        }
        if (StringUtils.isEmpty(targetUserId)) {
            allParams.put("targetUserId", userId);
        }
        Promise<JSONResponseWrapper> promise = client(ClientUtil.USER_SERVICE_URL
                + "/users/getUserSelfFullProfile", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject userResponse = getJSON(promise);
        userResponse = Validation.verifyResponse(userResponse);
        return userResponse;
    }

    // for more professor and students
    public static void moreMembers() {
        JSONObject profResponse = _reqMembersInfo(getReqParams());
        JSONArray users = new JSONArray();
        try {
            users = profResponse.getJSONObject("result").getJSONArray("users");
        } catch (JSONException ex) {
            Logger.log4j.error(ex.getMessage());
        }
        render("Widgets/people.html", users);
    }

    // my schedule
    public static void mySchedule() {
        recordActivity(ClientUtil.ActivityPages.SCHEDULE, ClientUtil.ActivityAction.OPEN);
        JSONObject myProgrammes = _getMemberInfo(null);
        render(myProgrammes);
    }

    public static void myClassroomConnect() {
        Map<String, Object> allParams = getReqParams();
        recordActivity(ClientUtil.ActivityPages.SCHEDULE, ClientUtil.ActivityAction.OPEN);
        JSONObject memberInfo = _getViewableDomains(allParams);
        JSONObject myProgrammes = _getMemberInfo(null);
        render(myProgrammes,memberInfo);
    }

    public static void mySchedulePage(@Required String orgId) {
        if (StringUtils.isNotEmpty(orgId)) {
            JSONObject myOrgInfo = _setOrgParams(orgId);
            recordActivity(ClientUtil.ActivityPages.SCHEDULE, ClientUtil.ActivityAction.OPEN);
            JSONObject myProgrammes = _getMemberInfo(null);
            request.params.put("instHomePg", "Institute/mySchedule.html");
            flash.put("ENTRY", "DIRECT");
            String includeInstFile = "Institute/home.html";
            render("Institute/header.html", includeInstFile, myProgrammes, myOrgInfo);
        } else {
            render("errors/404.html");
        }
    }

    public static void myClassroomConnectPage(@Required String orgId) {
        if (StringUtils.isNotEmpty(orgId)) {
            Map<String, Object> allParams = getReqParams();
            JSONObject myOrgInfo = _setOrgParams(orgId);
            JSONObject memberInfo = _getViewableDomains(allParams);
            recordActivity(ClientUtil.ActivityPages.SCHEDULE, ClientUtil.ActivityAction.OPEN);
            JSONObject myProgrammes = _getMemberInfo(null);
            request.params.put("instHomePg", "Institute/myClassroomConnect.html");
            flash.put("ENTRY", "DIRECT");
            String includeInstFile = "Institute/home.html";
            render("Institute/header.html", includeInstFile, myProgrammes, myOrgInfo, memberInfo);
        } else {
            render("errors/404.html");
        }
    }

    public static void getMemberInfo() {
        JSONObject info = _getMemberInfo(null);
        renderJSON(info.toString());
    }

//    public static void scheduleWidget() {
//        JSONObject resp = _getSchedule();
//        render("tags/institute/myScheduleWidget.html", resp);
//    }
//
//    public static JSONObject _getSchedule() {
//        Promise<JSONResponseWrapper> promise
//                = client(ClientUtil.ORGANIZATION_SERVICE_URL
//                        + "/MasterPlans/getMasterPlanOfProgramme", null);
//        Logger.log4j.info("BEFORE SCHEDULE AWAIT");
//        await(promise);
//        Logger.log4j.info("AFTER SCHEDULE AWAIT");
//        JSONObject scheduleResponse = getJSON(promise);
//        /*String s="{'result':{'masterPlanId':'509b784f5673ae84db13f4e3','total':3,'centerCalendarEntries':[{'center':{'1362076200000':[{'course':{'name':'Chemistry','code':'Chemistry','courseId':'50694e57d02aae84534d102a'},'uuid':'4c96fffb-dbe2-4dbd-be0a-bc346af377cf','startTime':1362076500000,'endTime':1362076700000,'classname':'com.vedantu.masterplan.LectureEntry','assignee':['5066d8c9553eae84c1b88697'],'status':[{'startTime':1361817200000,'endTime':1361817500000,'by':'NA','completedTopics':[],'status':'SCHEDULED','markedBy':'NA','markedTime':1361385000000}],'state':'SCHEDULED','note':[],'isExtra':false},{'course':{'name':'Chemistry','code':'Chemistry','courseId':'50694e57d02aae84534d102a'},'uuid':'780132ab-9eee-4c67-8160-c4b94d19a22f','startTime':1361817300000,'endTime':1361817400000,'classname':'com.vedantu.masterplan.LectureEntry','assignee':['5066d8c9553eae84c1b88697'],'status':[{'startTime':1361389000000,'endTime':1361389500000,'by':'NA','completedTopics':[],'status':'SCHEDULED','markedBy':'NA','markedTime':1361389500000}],'state':'SCHEDULED','note':[],'isExtra':false},{'course':{'name':'Physics','code':'Physics','courseId':'50694e65d02aae84554d102a'},'uuid':'e836ca01-bd47-4aa6-8c40-5c2f0a6cc7d0','startTime':1361817700000,'endTime':1361817800000,'classname':'com.vedantu.masterplan.LectureEntry','assignee':['5066d8c9553eae84c1b88697'],'status':[{'startTime':1352121300000,'endTime':1352121300000,'by':'NA','completedTopics':[],'status':'SCHEDULED','markedBy':'NA','markedTime':1352366160877}],'state':'SCHEDULED','note':[],'isExtra':false}],'1362162600000':[{'course':{'name':'Mathematics','code':'Mathematics','courseId':'50694e4ad02aae84514d102a'},'uuid':'2a68699e-ec40-48c9-bd0e-4a56f7ca5370','startTime':1362162600000,'endTime':1362162600000,'classname':'com.vedantu.masterplan.LectureEntry','assignee':['5066d8c9553eae84c1b88697'],'status':[{'startTime':1361903500000,'endTime':1361903600000,'by':'NA','completedTopics':[],'status':'SCHEDULED','markedBy':'NA','markedTime':1352366160540}],'state':'SCHEDULED','note':[],'isExtra':false},{'course':{'name':'Mathematics','code':'Mathematics','courseId':'50694e4ad02aae84514d102a'},'uuid':'d5125022-3e85-48ef-8dec-fab32403a180','startTime':1362162600000,'endTime':1362162600000,'classname':'com.vedantu.masterplan.LectureEntry','assignee':['5066d8c9553eae84c1b88697'],'status':[{'startTime':1361903700000,'endTime':1361903800000,'by':'NA','completedTopics':[],'status':'SCHEDULED','markedBy':'NA','markedTime':1352366160738}],'state':'SCHEDULED','note':[],'isExtra':false},{'course':{'name':'Physics','code':'Physics','courseId':'50694e65d02aae84554d102a'},'uuid':'9f3350b9-c202-4d67-95b2-5a570b36b206','startTime':1362162600000,'endTime':1362162600000,'classname':'com.vedantu.masterplan.LectureEntry','assignee':['5066d8c9553eae84c1b88697'],'status':[{'startTime':1352294100000,'endTime':1352294100000,'by':'NA','completedTopics':[],'status':'SCHEDULED','markedBy':'NA','markedTime':1352366160946}],'state':'SCHEDULED','note':[],'isExtra':false}],'1362249000000':[{'course':{'name':'Physics','code':'Physics','courseId':'50694e65d02aae84554d102a'},'uuid':'6d0abb8e-2107-4918-ad6f-59ce77faca64','startTime':1362249000000,'endTime':1362249000000,'classname':'com.vedantu.masterplan.LectureEntry','assignee':['5066d8c9553eae84c1b88697'],'status':[{'startTime':1361989820000,'endTime':1361989830000,'by':'NA','completedTopics':[],'status':'SCHEDULED','markedBy':'NA','markedTime':1352366160582}],'state':'SCHEDULED','note':[],'isExtra':false},{'course':{'name':'Physics','code':'Physics','courseId':'50694e65d02aae84554d102a'},'uuid':'d9877b77-0ff8-48be-b418-7734a2cb1bf4','startTime':1362249000000,'endTime':1362249000000,'classname':'com.vedantu.masterplan.LectureEntry','assignee':['5066d8c9553eae84c1b88697'],'status':[{'startTime':1361989840000,'endTime':1361989850000,'by':'NA','completedTopics':[],'status':'SCHEDULED','markedBy':'NA','markedTime':1352366160773}],'state':'SCHEDULED','note':[],'isExtra':false},{'course':{'name':'Physics','code':'Physics','courseId':'50694e65d02aae84554d102a'},'uuid':'ffbaaa91-6e86-4887-8d4a-5dee12bfacf4','startTime':1362249000000,'endTime':1362249000000,'classname':'com.vedantu.masterplan.LectureEntry','assignee':['5066d8c9553eae84c1b88697'],'status':[{'startTime':1361989850000,'endTime':1361989860000,'by':'NA','completedTopics':[],'status':'SCHEDULED','markedBy':'NA','markedTime':1352366161007}],'state':'SCHEDULED','note':[],'isExtra':false}]},'name':'Chandigarh','sections':['D-1']}]},'errorMessage':'','errorCode':''}";
//         JSONObject scheduleResponse = null;
//         try {
//         scheduleResponse = new JSONObject(s);
//         } catch (JSONException ex) {
//         Logger.log4j.error(ex.getMessage());
//         }*/
//        scheduleResponse = Validation.verifyResponse(scheduleResponse);
//        return scheduleResponse;
//    }

//    public static void getSchedule() {
//        JSONObject scheduleResponse = _getSchedule();
//        renderJSON(scheduleResponse.toString());
//    }

    public static void getProfInfo() {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/MasterPlans/getProfInfo", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject data = getJSON(promise);
        data = Validation.verifyResponse(data);
        renderJSON(data.toString());
    }

    //test analytics
    public static void getProgrammeInfo() {
        JSONObject data = _getProgramCourses(getReqParams());
        renderJSON(data.toString());
    }

    public static void getTATestCeters() {
        Map<String, Object> allParams = getReqParams();
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/analytics/getEntityScheduleInfo", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject centers = getJSON(promise);
        centers = Validation.verifyResponse(centers);
        render("tags/institute/analytics/centerPopup.html", centers);
    }

    public static void drawTATestTable() {
        Map<String, Object> allParams = getReqParams();
        JSONObject tests = _getTATests(allParams);
        String userRole = Scope.Params.current().get("userRole");
        render("tags/institute/TATables.html", tests, userRole);
    }

    public static void drawStudentTATestTable() {
        Map<String, Object> allParams = getReqParams();
        JSONObject tests = _getTATestsStudent(allParams);
        String userRole = Scope.Params.current().get("userRole");
        render("tags/institute/studentTATables.html", tests, userRole);
    }

    public static void getTATestGraph() {
        Map<String, Object> allParams = getReqParams();
        JSONObject tests = _getTATests(allParams);
        renderJSON(tests.toString());
    }

    public static void getTATestGraphStudent() {
        Map<String, Object> allParams = getReqParams();
        JSONObject tests = _getTATestsStudent(allParams);
        renderJSON(tests.toString());
    }

    /*
     * DEPRECATED - not in use
     * public static void getTargetExams() {
     Map<String, Object> allParams = getReqParams();
     allParams.put("type", "EXAM");
     allParams.put("size", "-1");
     Promise<JSONResponseWrapper> promise = client(ClientUtil.COLLEGES_WEB_SERVICE_URL
     + "/Organizations/getProgrammeTargets", allParams);
     Logger.log4j.info("BEFORE AWAIT GET TARGET EXAMS");
     await(promise);
     Logger.log4j.info("AFTER AWAIT  GET TARGET EXAMS");
     JSONObject targetExam = getJSON(promise);
     renderJSON(targetExam.toString());
     }*/
    protected static JSONObject _getBatchesWithTestsStats(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/getTests", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject data = getJSON(promise);
        data = Validation.verifyResponse(data);
        return data;
    }

    protected static JSONObject _getTATests(Map<String, Object> allParams) {
        allParams.put("orderBy", "timeCreated");
        allParams.put("sortOrder", "DESC");
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/analytics/getEntityScheduleAnalytics", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject data = getJSON(promise);
        data = Validation.verifyResponse(data);
        return data;
    }

    protected static JSONObject _getTATestsStudent(Map<String, Object> allParams) {
        allParams.put("orderBy", "timeCreated");
        allParams.put("sortOrder", "DESC");
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/analytics/getUserEntityResultAnalytics", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject data = getJSON(promise);
        data = Validation.verifyResponse(data);
        return data;
    }

    //commom utilities
    protected static JSONObject _getProgramCourses(Map<String, Object> allParams) {
        if (allParams != null) {
            allParams.put("recordState", "ACTIVE");
        } else {
            request.params.put("recordState", "ACTIVE");
        }
//        Logger.log4j.info("Key :: "+allParams.get("orgId")+"_"+allParams.get("programId")+"_getProgramCourses");
//        String res = Cache.get(allParams.get("orgId")+"_"+allParams.get("programId")+"_getProgramCourses", String.class);
//        if(!StringUtils.isEmpty(res)){
//            Logger.log4j.info("Served _getProgramCourses from Cache");
//            JSONObject resp = getCacheDataInJsonObject(res);
//            return resp;
//        }else{
            Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                    + "/organizations/getProgramCourses", allParams);
            Logger.log4j.info("BEFORE AWAIT");
            await(promise);
            Logger.log4j.info("AFTER AWAIT");
            JSONObject resp = getJSON(promise);
            resp = Validation.verifyResponse(resp);
//            if(resp != null)
//                Cache.set(allParams.get("orgId")+"_"+allParams.get("programId")+"_getProgramCourses", resp.toString(), "1h");
            return resp;
//        }
    }

    private static JSONObject _getProgramBoards(Map<String, Object> allParams) {
        allParams.put("context", "ORG");
        allParams.put("ownerId", Scope.Params.current().get("orgId"));
        allParams.put("showSharedSubjects", "show");
        JSONObject resp = Boards._getBoards(allParams);
        return resp;
    }

    protected static JSONObject _getProgrammeCourseTopics(Map<String, Object> allParams) {
        allParams.put("type", "TOPIC");
        JSONObject resp = _getProgramBoards(allParams);
        return resp;
    }

    protected static JSONObject _getProgrammeCourseSubTopics(Map<String, Object> allParams) {
        allParams.put("type", "SUBTOPIC");
        JSONObject resp = _getProgramBoards(allParams);
        return resp;
    }

    protected static JSONObject _getCourseInfo(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/getCourseInfo", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        resp = Validation.verifyResponse(resp);
        return resp;
    }

    protected static JSONObject _getCourseProfessors(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/getCourseProfessors", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        resp = Validation.verifyResponse(resp);
        return resp;
    }

    protected static JSONObject _getSharedEntity(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/getSharedEntity", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        resp = Validation.verifyResponse(resp);
        return resp;
    }

    private static JSONObject _getViewableDomains(Map<String, Object> allParams) {
        JSONObject data = _getMemberInfo(allParams);
        if (data == null) {
            return null;
        }
        JSONObject mappings = null;
        try {
            if (data.getString("errorCode").isEmpty()) {
                JSONObject result = data.getJSONObject("result");
                JSONObject info = result.getJSONObject("info");
                mappings = info.getJSONObject("mappings");
            }
        } catch (JSONException ex) {
            Logger.log4j.error(ex.getLocalizedMessage());
        }
        return mappings;
    }

    private static JSONObject _getParamsResultTypeList() {
        return _getArrayFromStrList(RESULT_LIST_STR);
    }

    private static JSONObject _getArrayFromStrList(String str) {
        JSONObject resultTypeList = null;
        try {
            resultTypeList = new JSONObject(str);
        } catch (JSONException ex) {
        }
        return resultTypeList;
    }
    //Library

    public static void library(String tabId) {
        Map<String, Object> allParams = getReqParams();
        recordActivity(ClientUtil.ActivityPages.LIBRARY, ClientUtil.ActivityAction.OPEN);
        JSONObject memberInfo = _getViewableDomains(allParams);
        JSONObject resultTypeList = _getParamsResultTypeList();
        JSONObject sortTypeList = webUtils.TemplateHelper._getParamsSortTypeList();
        String orgId = request.params.get("orgId");
        JSONArray fields = _getDigitalLibraryHiddenFields(orgId);
        render(memberInfo, resultTypeList, sortTypeList, tabId,fields);
    }

    public static void libraryHome(String tabId) {
        Map<String, Object> allParams = getReqParams();
        JSONObject memberInfo = _getViewableDomains(allParams);
        JSONObject resultTypeList = _getParamsResultTypeList();
        JSONObject sortTypeList = webUtils.TemplateHelper._getParamsSortTypeList();
        String includeInstFile = "Institute/libraryDirect.html";
        JSONObject myOrgInfo = _setOrgParams(null);
        String orgId = request.params.get("orgId");
        JSONArray fields = _getDigitalLibraryHiddenFields(orgId);
        render("Institute/header.html", includeInstFile, memberInfo, sortTypeList, resultTypeList, tabId, myOrgInfo,fields);
    }

    public static void getLibProgrammeInfo() {
        JSONObject data = _getProgramCourses(getReqParams());
        render("tags/institute/library/subjectBar.html", data);
    }

    public static void getLibTopics() {
        Map<String, Object> allParams = getReqParams();
        JSONObject data = _getProgrammeCourseTopics(allParams);
        render("tags/institute/library/topicsBar.html", data);
    }

    public static void getLibSubTopics() {
        Map<String, Object> allParams = getReqParams();
        JSONObject data = _getProgrammeCourseSubTopics(allParams);
        render("tags/institute/library/subTopicsBar.html", data);
    }

    protected static String getViewPath(String fileName) {
        String viewType = "list";
        String path = "iconView";
        String pre = "tags/myContents/";
        if ("list".equals(viewType)) {
            path = "listView";
        }
        String file = pre.concat(path).concat("/" + fileName);
        return file;
    }

    private static JSONObject _getQuestions(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/questions/getQuestions", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = Validation.verifyResponse(getJSON(promise));
        return resp;
    }

    public static void getLibQuestions() {
        Map<String, Object> allParams = getReqParams();
        //recordActivity("LIBRARY PAGE - QUESTIONS",ClientUtil.ActivityAction.OPEN);
        allParams.put("entityType", "QUESTION");
        //JSONObject quesns= _getSharedEntity(allParams);
        JSONObject quesns = _getQuestions(allParams);
        //render("Questions/quesItems.html",quesns);
        render("tags/institute/library/quesItems.html", quesns);
    }

    public static void getLibPlaylists() {
        Map<String, Object> allParams = getReqParams();
        //recordActivity("LIBRARY PAGE - PLAYLISTS",ClientUtil.ActivityAction.OPEN);
        allParams.put("entityType", "PLAYLIST");
        JSONObject playlist = _getSharedEntity(allParams);
        String file = getViewPath("getPlaylists.html");
        render("tags/myContents/items.html", file, playlist);
    }

    public static void getLibDocuments() {
        Map<String, Object> allParams = getReqParams();
        //recordActivity("LIBRARY PAGE - DOCUMENTS",ClientUtil.ActivityAction.OPEN);
        allParams.put("entityType", "DOCUMENT");
        JSONObject documents = MyContents._getDocuments(allParams);
        String file = getViewPath("getDocuments.html");
        render("tags/myContents/items.html", file, documents);
    }

    public static void getLibFiles() {
        Map<String, Object> allParams = getReqParams();
        //recordActivity("LIBRARY PAGE - FILES",ClientUtil.ActivityAction.OPEN);
        allParams.put("entityType", "FILE");
        JSONObject files = MyContents._getFiles(allParams);
        String file = getViewPath("getFiles.html");
        render("tags/myContents/items.html", file, files);
    }

    public static void getLibTests() {
        Map<String, Object> allParams = getReqParams();
        //recordActivity("LIBRARY PAGE - TESTS",ClientUtil.ActivityAction.OPEN);
        allParams.put("entityType", "TEST");
        JSONObject tests = Tests._getTests(allParams);
        String file = getViewPath("getTests.html");
        render("tags/myContents/items.html", file, tests);
    }

    public static void getLibAssignments() {
        Map<String, Object> allParams = getReqParams();
        //recordActivity("LIBRARY PAGE - ASSIGNMENTS",ClientUtil.ActivityAction.OPEN);
        allParams.put("entityType", "ASSIGNMENTS");
        JSONObject tests = Assignments._getAssignments(allParams);
        String file = getViewPath("getTests.html");
        render("tags/myContents/items.html", file, tests);
    }

    public static void getLibVideos() {
        Map<String, Object> allParams = getReqParams();
        //recordActivity("LIBRARY PAGE - VIDEOS",ClientUtil.ActivityAction.OPEN);
        allParams.put("entityType", "VIDEO");
        JSONObject videos = MyContents._getVideos(allParams);
        String file = getViewPath("getVideos.html");
        render("tags/myContents/items.html", file, videos);
    }

    public static void getLibModules() {
        Map<String, Object> allParams = getReqParams();
        allParams.put("entityType", "MODULE");
        JSONObject modules = MyContents._getModules(allParams);
        String file = getViewPath("getModules.html");
        render("tags/myContents/items.html", file, modules);
    }
    //Library url mapping

    public static void libraryDirect(@Required String orgId, String tabId) {
        JSONObject myOrgInfo = _setOrgParams(orgId);
        Map<String, Object> allParams = getReqParams();
        recordActivity(ClientUtil.ActivityPages.LIBRARY, ClientUtil.ActivityAction.OPEN);
        JSONObject memberInfo = _getViewableDomains(allParams);
        JSONObject resultTypeList = _getParamsResultTypeList();
        JSONObject sortTypeList = webUtils.TemplateHelper._getParamsSortTypeList();
        JSONArray fields = _getDigitalLibraryHiddenFields(orgId);
        flash.put("ENTRY", "DIRECT");
        String includeName = "Institute/header.html";
        String includeInstFile = "Institute/libraryDirect.html";
        render("Application/myPages.html", includeName, includeInstFile, memberInfo, resultTypeList, sortTypeList, tabId, myOrgInfo, fields);
    }

    public static void drawLibraryDropList() {
        String listStr = Scope.Params.current().get("list");
        String listType = Scope.Params.current().get("listType");
        JSONObject listData = null;
        JSONArray list = null;
        try {
            listData = new JSONObject(listStr.toString());
            list = listData.getJSONArray("data");
        } catch (JSONException ex) {
            Logger.log4j.error(ex.getLocalizedMessage());
            list = null;
        }
        render("tags/institute/library/drawList.html", list, listType);
    }
    // Digital Library Hidden Fields

    public static JSONArray _getDigitalLibraryHiddenFields(String orgId){
        JSONArray array = null;
        Logger.log4j.info("Key :: "+orgId+"_getDigitalLibraryHiddenFields");
        String resps = Cache.get(orgId+"_getDigitalLibraryHiddenFields", String.class);
        if(!StringUtils.isEmpty(resps)){
            Logger.log4j.info("Served _getDigitalLibraryHiddenFields from cache");
            array = getCacheDataInJsonArray(resps);
            return array;
        }else{
            request.params.put("orgId", orgId);
            Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                    + "/organizations/getDigitalLibraryFields", null);
            Logger.log4j.info("BEFORE AWAIT");
            await(promise);
            Logger.log4j.info("AFTER AWAIT");
            JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
            try {
                if(StringUtils.isEmpty(resp.getString("errorCode"))){
                    array = resp.getJSONObject("result").getJSONArray("fields");
                }
            } catch (JSONException ex) {
                Logger.log4j.error(ex.getMessage());
                array = null;
            }
            if(array != null)
                Cache.set(orgId+"_getDigitalLibraryHiddenFields", array.toString(), "10min");
            return array;
        }
    }

    // Inst More Test Toppers

    public static void getMoreTestLeaders() {
        Map<String, Object> allParams = getReqParams();
        JSONObject toppersData = Tests._getToppersData(allParams);
        String userRole = Scope.Params.current().get("userRole");
        render("tags/institute/testToppersTR.html", toppersData, userRole);
    }
    // Inst Test Toppers

    public static void getTestLeaders() {
        Scope.Params.current().put("start", ClientUtil.DEFAULT_FETCH_START);
        Scope.Params.current().put("size", ClientUtil.DEFAULT_FETCH_SIZE_50);
        Map<String, Object> allParams = getReqParams();
        JSONObject toppersData = Tests._getToppersData(allParams);
        JSONObject data = Tests.getOrgTestDetails(allParams);
        String userRole = Scope.Params.current().get("userRole");
        render("Institute/testToppers.html", data, toppersData, userRole);
    }
    // Inst Test Toppers page

    public static void getTestLeadersPage() {
        recordActivity(ClientUtil.ActivityPages.TEST_LEADERS, ClientUtil.ActivityAction.OPEN);
        Scope.Params.current().put("start", ClientUtil.DEFAULT_FETCH_START);
        Scope.Params.current().put("size", ClientUtil.DEFAULT_FETCH_SIZE_50);
        Map<String, Object> allParams = getReqParams();
        JSONObject toppersData = Tests._getToppersData(allParams);
        JSONObject data = Tests.getOrgTestDetails(allParams);
        String userRole = Scope.Params.current().get("userRole");
        flash.put("ENTRY", "DIRECT");
        String includeInstFile = "Institute/testToppersDirect.html";
        JSONObject myOrgInfo = _setOrgParams(null);
        render("Institute/header.html", includeInstFile, data, toppersData, userRole, myOrgInfo);
    }
    // Inst Test Toppers Url Map

    public static void getTestLeadersDirect(@Required String orgId) {
        JSONObject myOrgInfo = _setOrgParams(orgId);
        recordActivity(ClientUtil.ActivityPages.TEST_LEADERS, ClientUtil.ActivityAction.OPEN);
        Scope.Params.current().put("start", ClientUtil.DEFAULT_FETCH_START);
        Scope.Params.current().put("size", ClientUtil.DEFAULT_FETCH_SIZE_50);
        Map<String, Object> allParams = getReqParams();
        JSONObject toppersData = Tests._getToppersData(allParams);
        JSONObject data = Tests.getOrgTestDetails(allParams);
        String userRole = Scope.Params.current().get("userRole");
        flash.put("ENTRY", "DIRECT");
        String includeInstFile = "Institute/testToppersDirect.html";
        String includeName = "Institute/header.html";
        render("Application/myPages.html", includeName, includeInstFile, data, toppersData, userRole, myOrgInfo);
    }

    // result analytics
    public static void testAnalytics() {
        Map<String, Object> allParams = getReqParams();
        JSONObject myOrgInfo = _setOrgParams(null);
        recordActivity(ClientUtil.ActivityPages.ANALYTICS, ClientUtil.ActivityAction.OPEN);

        JSONObject memberInfo = _getViewableDomains(allParams);
        JSONObject profileAnalytics = null;
        String userRole = Scope.Params.current().get("userRole");
        if (userRole.equals("STUDENT")) {
            Logger.log4j.info("Inside testAnalytics");
            profileAnalytics = _getProfileAnalytics(allParams);
        }
        String targetUserId = Scope.Params.current().get("targetUserId");
        String userId = session.get("userId");
        if (targetUserId == null || targetUserId.isEmpty()) {
            allParams.put("targetUserId", userId);
            Scope.Params.current().put("targetUserId", userId);
        }
        render(memberInfo, userRole, profileAnalytics, myOrgInfo);
    }
    // result analytics

    public static void resultAnalytics(String orgId) {
        JSONObject myOrgInfo = _setOrgParams(orgId);
        recordActivity(ClientUtil.ActivityPages.ANALYTICS, ClientUtil.ActivityAction.OPEN);

        Map<String, Object> allParams = getReqParams();
        JSONObject profileAnalytics = null;

        JSONObject memberInfo = _getViewableDomains(allParams);
        flash.put("ENTRY", "DIRECT");
        String userRole = Scope.Params.current().get("userRole");
        if (userRole.equals("STUDENT")) {
            Logger.log4j.info("Inside resultAnalytics");
            profileAnalytics = _getProfileAnalytics(allParams);
        }
        String targetUserId = Scope.Params.current().get("targetUserId");
        String userId = session.get("userId");
        if (targetUserId == null || targetUserId.isEmpty()) {
            allParams.put("targetUserId", userId);
            Scope.Params.current().put("targetUserId", userId);
        }
        String includeInstFile = "Institute/TADirect.html";
        render("Institute/header.html", includeInstFile, memberInfo, profileAnalytics, userRole, myOrgInfo);
    }

    // Url mapping result analytics
    public static void resultAnalyticsDirect(@Required String orgId) {
        JSONObject myOrgInfo = _setOrgParams(orgId);
        Map<String, Object> allParams = getReqParams();
        recordActivity(ClientUtil.ActivityPages.ANALYTICS, ClientUtil.ActivityAction.OPEN);
        JSONObject profileAnalytics = null;

        JSONObject memberInfo = _getViewableDomains(allParams);
        String userRole = Scope.Params.current().get("userRole");
        if (userRole.equals("STUDENT")) {
            Logger.log4j.info("Inside resultAnalyticsDirect");
            profileAnalytics = _getProfileAnalytics(allParams);
        }
        String targetUserId = Scope.Params.current().get("targetUserId");
        String userId = session.get("userId");
        if (targetUserId == null || targetUserId.isEmpty()) {
            allParams.put("targetUserId", userId);
            Scope.Params.current().put("targetUserId", userId);
        }
        flash.put("ENTRY", "DIRECT");
        String includeInstFile = "Institute/TADirect.html";
        String includeName = "Institute/header.html";
        render("Application/myPages.html", includeName, includeInstFile, memberInfo, profileAnalytics, userRole, myOrgInfo);
    }

    // url mapping
    public static void directHome(@Required String orgId, String instHomePg) {
        if (orgId != null || !"".equals(orgId)) {
            JSONObject myOrgInfo = _setOrgParams(orgId);
            recordActivity(ClientUtil.ActivityPages.INSTITUTE_HOME, ClientUtil.ActivityAction.OPEN);
            if (instHomePg != null) {
                request.params.put("instHomePg", instHomePg);
            }
            flash.put("ENTRY", "DIRECT");
            String includeInstFile = "Institute/home.html";
            String includeName = "Institute/header.html";
            render("Application/myPages.html", includeName, includeInstFile, myOrgInfo);
        } else {
            render("errors/404.html");
        }
    }
    //ACTIVITY

    public static void activityComments() {
        Map<String, Object> allParams = getReqParams();
        JSONObject comments = Widgets._getCommItems(allParams);
        render("tags/institute/widgets/comments.html", comments);
    }

    public static void moreActivityComments() {
        Map<String, Object> allParams = getReqParams();
        JSONObject comments = Widgets._getCommItems(allParams);
        render("tags/institute/widgets/commentList.html", comments);
    }

    public static void singleComment() {
        JSONObject comData = new JSONObject();
        try {
            Params p = Scope.Params.current();
            JSONObject user = new JSONObject();
            user.put("id", session.get("userId"));
            user.put("thumbnail", session.get("profilePic"));
            user.put("lastName", session.get("lastName"));
            user.put("firstName", session.get("firstName"));
            comData.put("user", user);
            comData.put("content", p.get("content"));
            comData.put("id", p.get("id"));
            comData.put("timeCreated", Long.parseLong(p.get("timeCreated")));
        } catch (JSONException ex) {
            Logger.log4j.error(ex.getMessage());
        }
        Logger.log4j.info("single comment post response === " + comData);
        render("tags/institute/widgets/commentPost.html", comData);
    }

    public static void addActivityFeed() {
        Map<String, Object> allParams = getReqParams();
        Promise<JSONResponseWrapper> promise = client(ClientUtil.COMM_SERVICE_URL
                + "/statusFeeds/addStatusFeed", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        resp = Validation.verifyResponse(resp);
        try {
            recordActivity(ClientUtil.ActivityPages.INSTITUTE_HOME, ClientUtil.ActivityAction.ADD, ClientUtil.Entity.STATUSFEED, resp.getJSONObject("result").getJSONArray("list").getJSONObject(0).getString("newsFeedId"));
        } catch (JSONException ex) {
            Logger.log4j.error(ex.getLocalizedMessage());
        }
        render("Institute/activityPost.html", resp);
    }

    public static void deleteActivityFeed() {
        Map<String, Object> allParams = getReqParams();
        Promise<JSONResponseWrapper> promise = client(ClientUtil.COMM_SERVICE_URL
                + "/statusFeeds/delete", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        resp = Validation.verifyResponse(resp);
        try {
            recordActivity(ClientUtil.ActivityPages.INSTITUTE_HOME, ClientUtil.ActivityAction.DELETE, ClientUtil.Entity.STATUSFEED, (String) allParams.get("id"));
        } catch (Exception ex) {
        }
        renderJSON(resp.toString());
    }

    public static void getClassroomsDirect(@Required String orgId){
        Logger.log4j.info("VIPUL orgId"+orgId);
        JSONObject myOrgInfo = _setOrgParams(orgId);
        String includeInstFile = "Institute/home.html";
        String includeName = "Institute/header.html";
        flash.put("ENTRY", "DIRECT");
        request.params.put("instHomePg", "VirtualClassroom/getClassrooms.html");
        render("Application/myPages.html", includeName, includeInstFile, myOrgInfo);
    }

    public static void deleteDoubt() {
        Map<String, Object> allParams = getReqParams();
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/discussions/removeDiscussion", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        resp = Validation.verifyResponse(resp);
        try {
            recordActivity(ClientUtil.ActivityPages.DOUBTS, ClientUtil.ActivityAction.DELETE, ClientUtil.Entity.DISCUSSION, (String) allParams.get("id"));
        } catch (Exception ex) {
        }
        renderJSON(resp.toString());
    }

    public static void uploadActiForm(String orgId) {
        String isResponse = "no";
        render("tags/institute/widgets/imageUpload.html", isResponse);
    }

    public static void uploadFeedImg(File imageFile) {
        request.params.put("type", "STATUSFEED");
        request.params.put("uploadFileParamName", "imageFile");
        JSONObject data = uploadUtil(ClientUtil.COMM_SERVICE_URL
                + "/statusFeeds/uploadImage", null, imageFile);
        data = Validation.verifyResponse(data);
        String isResponse = "yes";
        render("tags/institute/widgets/imageUpload.html", data, isResponse);
    }

    protected static JSONObject _getActivityFeeds(Map<String, Object> allParams) {
        String feedType = Scope.Params.current().get("feedType");
        String url = "getActivityFeeds";
        if (feedType != null && feedType.equals("OLD")) {
            url = "getOlderActivityFeeds";
        }
        Scope.Params.current().put("eType", "STATUSFEED");
        Promise<JSONResponseWrapper> promise = client(ClientUtil.COMM_SERVICE_URL
                + "/newsFeeds/" + url, allParams);

        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        resp = Validation.verifyResponse(resp);
        return resp;
    }

    protected static JSONObject _getSingleActivityFeed(Map<String, Object> allParams) {
        Scope.Params.current().put("eType", "STATUSFEED");
        Promise<JSONResponseWrapper> promise = client(ClientUtil.COMM_SERVICE_URL
                + "/statusFeeds/getStatusFeed", allParams);

        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        resp = Validation.verifyResponse(resp);
        return resp;
    }

    public static void getActivityFeeds() {
        Map<String, Object> allParams = getReqParams();
        JSONObject resp = _getActivityFeeds(allParams);
        String noFeedMsg = Messages.get("NO_RECENT_ACTIVITY");
        render("Institute/activityFeeds.html", resp, noFeedMsg);
    }

    public static void getMoreActivityFeeds() {
        Map<String, Object> allParams = getReqParams();
        Scope.Params.current().put("feedType", "OLD");
        JSONObject resp = _getActivityFeeds(allParams);
        String noFeedMsg = Messages.get("NO_MORE_FEEDS");
        render("Institute/activityFeeds.html", resp, noFeedMsg);
    }

    public static void doubtUi() {
        try {
            recordActivity(ClientUtil.ActivityPages.DISCUSSION, ClientUtil.ActivityAction.OPEN);
        } catch (Exception ex) {
        }
        render();
    }

    public static void activityUi() {
        try {
            recordActivity(ClientUtil.ActivityPages.STATUS_FEED, ClientUtil.ActivityAction.OPEN);
        } catch (Exception ex) {
        }
        Scope.Params.current().put("entityType", "STATUSFEED");
        JSONObject memberInfo = _getViewableDomains(null);
        render(memberInfo);
    }

    public static void activities(@Required String orgId) {
        try {
            recordActivity(ClientUtil.ActivityPages.STATUS_FEED, ClientUtil.ActivityAction.OPEN);
        } catch (Exception ex) {
        }
        Scope.Params.current().put("entityType", "STATUSFEED");
        JSONObject myOrgInfo = _setOrgParams(orgId);
        JSONObject memberInfo = _getViewableDomains(null);
        if (StringUtils.isNotEmpty(orgId)) {
            request.params.put("instHomePg", "Institute/activityUi.html");
            flash.put("ENTRY", "DIRECT");
            String includeInstFile = "Institute/home.html";
            // String includeName = "Institute/header.html";
            render("Institute/header.html",includeInstFile, myOrgInfo,memberInfo);
        } else {
            render("errors/404.html");
        }
    }

    public static void directActivities(@Required String orgId) {
        try {
            recordActivity(ClientUtil.ActivityPages.STATUS_FEED, ClientUtil.ActivityAction.OPEN);
        } catch (Exception ex) {
        }
        Scope.Params.current().put("entityType", "STATUSFEED");
        JSONObject myOrgInfo = _setOrgParams(orgId);
        JSONObject memberInfo = _getViewableDomains(null);
        if (StringUtils.isNotEmpty(orgId)) {
            request.params.put("instHomePg", "Institute/activityUi.html");
            flash.put("ENTRY", "DIRECT");
            String includeInstFile = "Institute/home.html";
            String includeName = "Institute/header.html";
            render("Application/myPages.html", includeName, includeInstFile, myOrgInfo,memberInfo);
        } else {
            render("errors/404.html");
        }
    }

    public static void feedUi(String orgId, @Required String feedId) {
        if (orgId != null || !"".equals(orgId)) {
            Map<String, Object> allParams = getReqParams();
            JSONObject resp = _getSingleActivityFeed(allParams);
            JSONObject myOrgInfo = _setOrgParams(orgId);
            request.params.put("instHomePg", "Institute/feedUi.html");
            try {
                recordActivity(ClientUtil.ActivityPages.STATUS_FEED, ClientUtil.ActivityAction.OPEN, ClientUtil.Entity.STATUSFEED, feedId);
            } catch (Exception ex) {
            }
            flash.put("ENTRY", "DIRECT");
            String includeInstFile = "Institute/home.html";
            render("Institute/header.html", includeInstFile, resp, myOrgInfo);
        } else {
            render("errors/404.html");
        }
    }

    public static void feedUiDirect(@Required String orgId, @Required String feedId) {
        JSONObject myOrgInfo = _setOrgParams(orgId);
        Map<String, Object> allParams = getReqParams();
        try {
            recordActivity(ClientUtil.ActivityPages.STATUS_FEED, ClientUtil.ActivityAction.OPEN, ClientUtil.Entity.STATUSFEED, feedId);
        } catch (Exception ex) {
        }
        JSONObject resp = _getSingleActivityFeed(allParams);

        request.params.put("instHomePg", "Institute/feedUi.html");

        flash.put("ENTRY", "DIRECT");
        String includeInstFile = "Institute/home.html";
        String includeName = "Institute/header.html";
        render("Application/myPages.html", resp, includeName, includeInstFile, myOrgInfo);
    }
    // doubts

    public static void directDoubts(@Required String orgId) {
        JSONObject myOrgInfo = _setOrgParams(orgId);
        try {
            recordActivity(ClientUtil.ActivityPages.DOUBTS, ClientUtil.ActivityAction.OPEN);
        } catch (Exception ex) {
        }
        if (StringUtils.isNotEmpty(orgId)) {
            request.params.put("instHomePg", "Institute/doubtUi.html");

            flash.put("ENTRY", "DIRECT");
            String includeInstFile = "Institute/home.html";
            String includeName = "Institute/header.html";
            render("Application/myPages.html", includeName, includeInstFile, myOrgInfo);
        } else {
            render("errors/404.html");
        }
    }

    public static void directDoubt(@Required String orgId, @Required String dissId) {
        if (orgId != null || !"".equals(orgId)) {
            JSONObject myOrgInfo = _setOrgParams(orgId);
            try {
                recordActivity(ClientUtil.ActivityPages.DISCUSSION, ClientUtil.ActivityAction.OPEN, ClientUtil.Entity.DISCUSSION, dissId);
            } catch (Exception ex) {
            }
            JSONObject discussion = _getDiscussion(null);
            request.params.put("instHomePg", "Institute/openDoubt.html");
            JSONObject sortTypeList = _getArrayFromStrList(COMMENT_LIST_STR);
            flash.put("ENTRY", "DIRECT");
            String includeInstFile = "Institute/home.html";
            String includeName = "Institute/header.html";
            render("Application/myPages.html", includeName, includeInstFile, discussion, sortTypeList, myOrgInfo);
        } else {
            render("errors/404.html");
        }
    }

    public static void myScheduleDirect(@Required String orgId) {
        JSONObject myOrgInfo = _setOrgParams(orgId);
        JSONObject myProgrammes = _getMemberInfo(null);
//      String s="{'errorMessage':'','result':{'center':{'sections':[],'name':'Patiala'},'_id':'50b5d42eb2ff451cee9dc0c3','programmes':[{'_id':'50b5e2f47a9350ed875d4d08','name':'Aakar-10th','centers':[{'sections':[{'courses':[{'_id':'50448592662450edaaaa29bd','name':'Physics','code':'PHY','brdId':'506949e3ff3150eda90fcd7f'}],'name':'A10-1'}],'name':'Patiala'}],'code':'A10'},{'_id':'502f9026cecf50ed635f0916','name':'Drishti','centers':[{'sections':[{'courses':[{'_id':'50448592662450edaaaa29bd','name':'Physics','code':'PHY','brdId':'506949e3ff3150eda90fcd7f'}],'name':'D-O'},{'courses':[{'_id':'50448592662450edaaaa29bd','name':'Physics','code':'PHY','brdId':'506949e3ff3150eda90fcd7f'}],'name':'D-1'}],'name':'Patiala'}],'code':'DRNM'},{'_id':'50b5e2b57a9350ed675d4d08','name':'Sankalp','centers':[{'sections':[{'courses':[{'_id':'50448592662450edaaaa29bd','name':'Physics','code':'PHY','brdId':'506949e3ff3150eda90fcd7f'}],'name':'S-1'}],'name':'Patiala'}],'code':'SNNM'}],'department':{'stream':'Non-Medical','acronym':'SC','_id':'502f9026cecf50ed625f0916','name':'Science'},'userId':'4f43a999dbc450ed18b9da3d','lastUpdated':1348841173002,'timeCreated':1348841173002,'year':2013,'memberId':'PAT1','userRole':'PROFESSOR','orgId':'4ffd9fcb6f6650edc551f744'},'errorCode':''}";
//      JSONObject myProgrammes=new JSONObject(s);
        String includeInstFile = "Institute/home.html";
        String includeName = "Institute/header.html";
        flash.put("ENTRY", "DIRECT");
        request.params.put("instHomePg", "Institute/mySchedule.html");
        render("Application/myPages.html", includeName, includeInstFile, myProgrammes, myOrgInfo);
    }

    public static void myClassroomConnectDirect(@Required String orgId) {
        JSONObject myOrgInfo = _setOrgParams(orgId);
        Map<String, Object> allParams = getReqParams();
        JSONObject memberInfo = _getViewableDomains(allParams);
        String includeInstFile = "Institute/home.html";
        String includeName = "Institute/header.html";
        flash.put("ENTRY", "DIRECT");
        request.params.put("instHomePg", "Institute/myClassroomConnect.html");
        render("Application/myPages.html", includeName, includeInstFile, memberInfo , myOrgInfo);
    }

    public static void membersDirect(@Required String orgId) {
        JSONObject myOrgInfo = _setOrgParams(orgId);
        try {
            recordActivity(ClientUtil.ActivityPages.MEMBERS, ClientUtil.ActivityAction.OPEN);
        } catch (Exception ex) {
        }
        Scope.Params.current().put("role", "MEMBER");
        JSONObject courses = _getCourses(null);
        JSONObject memberInfo = _getViewableDomains(null);
        String includeInstFile = "Institute/home.html";
        String includeName = "Institute/header.html";
        flash.put("ENTRY", "DIRECT");
        request.params.put("instHomePg", "Institute/membersUi.html");
        render("Application/myPages.html", includeName, includeInstFile, courses, memberInfo, myOrgInfo);
    }

    public static void fetchExturl(@Required String url, String domain) {
        WebInfo webInfo = null;
        JSONObject data = null;
        try {
            webInfo = WebUtil.fetchDataFromLink(url, domain);
            if (webInfo != null) {
                data = webInfo.toJson();
                data.put("type", "link");
            }
        } catch (Exception ex) {
            data = null;
        }
        Logger.log4j.info("fetch external url , json data ======= " + data);
        render("tags/institute/widgets/drawLink.html", data);
    }

    public static void fetchExtVideo() throws Exception {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL + "/videos/getVideoInfo", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject data = getJSON(promise);
        data = Validation.verifyResponse(data);
        render("tags/institute/widgets/drawVideo.html", data);
    }

    public static void challengeLeaderBoard() {
        JSONObject statsInfo = Challenges._getChallengeStats(null);
        JSONObject leaders = Challenges._getChallengeGlobalLeaderBoard(null);
        render("tags/institute/widgets/challenges.html", leaders, statsInfo);
    }

    public static void challengeLeaderBoardItems() {
        JSONObject leaders = Challenges._getChallengeGlobalLeaderBoard(null);
        render("tags/institute/widgets/challengesItems.html", leaders);
    }

    public static void challengesDirect(@Required String orgId) {
        JSONObject myOrgInfo = _setOrgParams(orgId);
        try {
            recordActivity(ClientUtil.ActivityPages.CHALLENGES, ClientUtil.ActivityAction.OPEN);
        } catch (Exception ex) {
        }
        request.params.put("resultType", "ALL");
        JSONObject channels = Challenges._getChannels(null);
        JSONObject challenges = Challenges._getChallengesPageResp(null);
        flash.put("ENTRY", "DIRECT");
        String includeName = "Institute/header.html";
        String includeInstFile = "Challenges/challenges.html";
        render("Application/myPages.html", includeName, includeInstFile, challenges, channels, myOrgInfo);
    }

    public static void challengesHome(@Required String orgId) {
        JSONObject myOrgInfo = _setOrgParams(orgId);
        request.params.put("resultType", "ALL");
        JSONObject channels = Challenges._getChannels(null);
        JSONObject challenges = Challenges._getChallengesPageResp(null);
        flash.put("ENTRY", "DIRECT");
        String includeInstFile = "Challenges/challenges.html";
        render("Institute/header.html", includeInstFile, challenges, channels, myOrgInfo);
    }

    public static void getNotifications() {
        String feedType = Scope.Params.current().get("feedType");
        String url = "getNotifcations";
        if (feedType != null && feedType.equals("OLD")) {
            url = "getOlderNotifications";
        }
        JSONObject notis = Application._getNotifications(null, url);
        /*try {
         notis = new JSONObject("{'errorMessage':'','result':{'newsFeedCount':1,'newsFeeds':[{'time':1364282842677,'newsActivityId':'5049f3dc7d4af20ce529d952_u_9223370672571933130_1339','eType':'FOLLOW_ENTITY','newsFeedId':'5049f3627d4af20ce029d952_u_9223370672571933130_49511','actor':{'id':'5049f3dc7d4af20ce529d952','lastName':'Dutta','_id':'5049f3dc7d4af20ce529d952','profilePic':'http://img.lakshya.vedantu.com/viewer/view/user/img/68b7d9b967b0423b9f18d5481005e1f0.usr.img.conv.small.jpg','type':'USER','firstName':'Anirban'},'src':{'id':'5049f3627d4af20ce029d952','lastName':'Chakrovarty','_id':'5049f3627d4af20ce029d952','profilePic':'http://img.lakshya.vedantu.com/viewer/view/user/img/035203ae19b84b7493fb043cc7a412f4.usr.img.conv.small.jpg','type':'USER','firstName':'Shankhoneer'},'sendNewsFeed':true,'srcOwner':{'id':'5049f3627d4af20ce029d952','lastName':'Chakrovarty','_id':'5049f3627d4af20ce029d952','profilePic':'http://img.lakshya.vedantu.com/viewer/view/user/img/035203ae19b84b7493fb043cc7a412f4.usr.img.conv.small.jpg','type':'USER','firstName':'Shankhoneer'},'why':'OWNER','info':{'className':'com.vedantu.news.info.FollowInfo','actionType':'FOLLOWED'}}]},'errorCode':''}");
         } catch (JSONException ex) {
         java.util.logging.Logger.getLogger(Institute.class.getName()).log(Level.SEVERE, null, ex);
         }
         * renderJSON(notis.toString());
         */
        //render("tags/header/notifications.html",notis);
        renderJSON(notis.toString());

    }

    public static void getReferralData() throws JSONException {
        Map<String, Object> allParams = getReqParams();
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/members/getReferralData", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        String baseUrl = Play.configuration.getProperty("mydomain.url");
        JSONObject resp = getJSON(promise);
        resp.put("baseUrl", baseUrl);
        renderJSON(resp.toString());
    }

    public static void getWalletBalance() throws JSONException {
        Map<String, Object> allParams = getReqParams();
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/members/getWalletBalance", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        renderJSON(resp.toString());
    }

    public static void getNotificationsSummary() {
        JSONObject notiSummary = Application._getNotificationsSummary();
//        JSONObject msgNoti = null;//UserMessages._getNotifications();
        JSONObject news = new JSONObject();
//        try {
//            if (msgNoti != null && msgNoti.getString("errorCode").isEmpty()) {
//                news.put("messages", msgNoti.getJSONObject("result"));
//            }
//        } catch (JSONException ex) {
//            Logger.log4j.error(ex.getLocalizedMessage());
//        }
        try {
            if (notiSummary != null && notiSummary.getString("errorCode").isEmpty()) {
                news.put("others", notiSummary.getJSONObject("result"));
            }
        } catch (JSONException ex) {
            Logger.log4j.error(ex.getLocalizedMessage());
        }
        renderJSON(news.toString());
    }

    private static JSONObject _getProfileAnalytics(Map<String, Object> allParams) {
        if (allParams != null) {
            allParams.put("entityType", "TEST");
        }
        String targetUserId = allParams.get("targetUserId").toString();
        String userId = targetUserId.contains(",") ? targetUserId.split(",")[0].substring(1) : targetUserId;
        userId = userId.contains("[") ? userId.substring(1,userId.length() - 1) : userId;
        if(StringUtils.isEmpty(userId)){
            userId = allParams.get("userId").toString();
        }
        JSONObject resp = null;
        Logger.log4j.info("Key :: "+userId+"_getProfileAnalytics");
        String value = Cache.get(userId+"_getProfileAnalytics",String.class);
        if(StringUtils.isEmpty(value)){
            Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                    + "/analytics/getUserAnalyticsStats", allParams);
            Logger.log4j.info("BEFORE AWAIT");
            await(promise);
            Logger.log4j.info("AFTER AWAIT");
            resp = getJSON(promise);
            resp = Validation.verifyResponse(resp);
            if(resp != null)
                Cache.set(userId+"_getProfileAnalytics", resp.toString(), "30mn");
            Logger.log4j.info("CACHE set");
        }
        else{
                resp = getCacheDataInJsonObject(value);
        }
        return resp;
    }

    public static void getStudentProfileAnalytics() {
        Logger.log4j.info("Inside getStudentProfileAnalytics");
        JSONObject resp = _getProfileAnalytics(getReqParams());
        renderJSON(resp);
    }

    public static void profile(String orgId, @Required String targetUserId) {
        JSONObject myOrgInfo = _setOrgParams(orgId);
        try {
            recordActivity(ClientUtil.ActivityPages.PROFILE, ClientUtil.ActivityAction.OPEN, ClientUtil.Entity.USER, targetUserId);
        } catch (Exception ex) {
        }
        Map<String, Object> allParams = getReqParams();
        JSONObject profileResp = null;//Profile._reqProfileInfo(allParams);
        JSONObject memberInfo = _getMemberInfo(allParams);
        JSONObject userInfo = _getUserInfo(allParams);
        JSONObject profileAnalytics = null;
        String userRole = Scope.Params.current().get("userRole");
        if (userRole.equals("STUDENT")) {
            Logger.log4j.info("Inside Profile");
            profileAnalytics = _getProfileAnalytics(allParams);
        }
        render(profileResp,memberInfo,userInfo, myOrgInfo, profileAnalytics);
    }

    public static void openProfile(String orgId, @Required String targetUserId) {
        JSONObject myOrgInfo = _setOrgParams(orgId);
        try {
            recordActivity(ClientUtil.ActivityPages.PROFILE, ClientUtil.ActivityAction.OPEN, ClientUtil.Entity.USER, targetUserId);
        } catch (Exception ex) {
        }
        Map<String, Object> allParams = getReqParams();
        JSONObject profileResp = null;//Profile._reqProfileInfo(allParams);
        JSONObject memberInfo = _getMemberInfo(allParams);
        JSONObject userInfo = _getUserInfo(allParams);
        JSONObject profileAnalytics = null;
        String userRole = Scope.Params.current().get("userRole");
        if (userRole.equals("STUDENT")) {
            Logger.log4j.info("Inside openProfile");
            profileAnalytics = _getProfileAnalytics(allParams);
        }
        String includeInstFile = "Institute/profile.html";
        render("Institute/header.html",includeInstFile,profileResp,memberInfo,userInfo, myOrgInfo, profileAnalytics);
    }

    public static void profileDirect(@Required String orgId, @Required String targetUserId) {
        JSONObject myOrgInfo = _setOrgParams(orgId);
        try {
            recordActivity(ClientUtil.ActivityPages.PROFILE, ClientUtil.ActivityAction.OPEN, ClientUtil.Entity.USER, targetUserId);
        } catch (Exception ex) {
        }
        Map<String, Object> allParams = getReqParams();
        JSONObject profileResp = null;//Profile._reqProfileInfo(allParams);
        JSONObject memberInfo = _getMemberInfo(allParams);
        JSONObject userInfo = _getUserInfo(allParams);
        JSONObject profileAnalytics = null;
        String userRole = Scope.Params.current().get("userRole");
        if (userRole.equals("STUDENT")) {
            Logger.log4j.info("Inside profileDirect");
            profileAnalytics = _getProfileAnalytics(allParams);
        }
        String includeInstFile = "Institute/profile.html";
        flash.put("ENTRY", "DIRECT");
        String includeName = "Institute/header.html";
        render("Application/myPages.html", includeName, includeInstFile, profileResp, memberInfo, userInfo, profileAnalytics, myOrgInfo);
    }

    public static void getActivityJSON() {
        Map<String, Object> allParams = getReqParams();
        JSONObject resp = _getActivityFeeds(allParams);
        renderJSON(resp.toString());
    }

    public static void getUserDoubtAsked() {
        Map<String, Object> allParams = getReqParams();
        allParams.put("orderBy", "timeCreated");
        allParams.put("resultType", "CREATED");
        allParams.put("facet", false);
        JSONObject discussions = getInstituteDoubts(allParams);
        render("tags/institute/profile/dbtTemplate.html", discussions);
    }

    public static void addQuestion(String orgId) {
        String includeInstFile = "UIComQuestions/addQuestion.html";
        JSONObject myOrgInfo = _setOrgParams(null);
        render("Institute/header.html", includeInstFile, orgId, myOrgInfo);
    }

    public static void addQuestionDirect(@Required String orgId) {
        JSONObject myOrgInfo = _setOrgParams(orgId);
        flash.put("ENTRY", "DIRECT");
        String includeName = "Institute/header.html";
        String includeInstFile = "UIComQuestions/addQuestion.html";
        render("Application/myPages.html", includeName, includeInstFile, orgId, myOrgInfo);
    }
    //QUESTION

    public static void questionPage(@Required String id) {
        JSONObject org = _setOrgParams(null);
        try {
            recordActivity(ClientUtil.ActivityPages.QUESTION, ClientUtil.ActivityAction.OPEN, ClientUtil.Entity.QUESTION, id);
        } catch (Exception ex) {
        }
        JSONObject ques = Questions._getQuestionInfo(null);
        String includeInstFile = "Questions/questionPage.html";
        JSONObject myOrgInfo = _setOrgParams(null);
        render("Institute/header.html", includeInstFile, ques, myOrgInfo);
    }

    public static void questionDirect(String orgId, @Required String id) {
        JSONObject ques = Questions._getQuestionInfo(null);
        JSONObject myOrgInfo = _setOrgParams(orgId);
        try {
            recordActivity(ClientUtil.ActivityPages.QUESTION, ClientUtil.ActivityAction.OPEN, ClientUtil.Entity.QUESTION, id);
        } catch (Exception ex) {
        }
        flash.put("ENTRY", "DIRECT");
        String includeName = "Institute/header.html";
        String includeInstFile = "Questions/questionPage.html";
        render("Application/myPages.html", includeName, includeInstFile, ques, myOrgInfo);
    }

    public static void emailVerifiedPage(String userId, String orgId) {
        String sessionUserId = session.get("userId");
        if (!StringUtils.isEmpty(sessionUserId) && sessionUserId.equals(userId)) {
            JSONArray orgArray = Institute._getUserOrgs(getReqParams(), true);
            orgId = Security._getOrgId(orgArray);
            JSONObject myOrgInfo = _setOrgParams(orgId);
            flash.put("ENTRY", "DIRECT");
            String emailVerifiedEntry = "done";
            String includeName = "Institute/header.html";
            String includeInstFile = "Institute/home.html";
            render("Application/myPages.html", includeName, includeInstFile, emailVerifiedEntry, myOrgInfo);
        } else {
            Security._logout();
            flash.put("emailVerified", "done");
            String instLoginPageUrl = UIComRegister._getOrgRefererUrl(orgId);
            if (StringUtils.isEmpty(instLoginPageUrl)) {
                redirect("/login");
            } else {
                String redirectUrl = instLoginPageUrl;
                render("UIComRegister/redirectPage.html", redirectUrl);
            }
        }
        render();
    }

    public static void getMemberMappings() {
        JSONObject data = _getMemberInfo(getReqParams());
        JSONObject mappings = null;
        String targetProfile = "";
        try {
            if (data != null && data.getString("errorCode").isEmpty()) {
                JSONObject result = data.getJSONObject("result");
                JSONObject info = result.getJSONObject("info");
                mappings = info.getJSONObject("mappings");
                targetProfile = info.getString("profile");
            }
        } catch (JSONException ex) {
            Logger.log4j.error(ex.getLocalizedMessage());
        }
        render(mappings, targetProfile);
    }

    public static void getProfileExtraInfo(String orgId, @Required String targetUserId) {
        Map<String, Object> allParams = getReqParams();
        JSONObject memberInfo = ResponseUtil.checkResponse(_getMemberInfo(allParams));
        render(memberInfo);
    }

    public static void getMemberToolTip() {
        JSONObject data = _getMemberInfo(getReqParams());
        render(data);
    }

    public static void allNotifications(String orgId) {
        String includeInstFile = "Institute/allNotifications.html";
        JSONObject myOrgInfo = _setOrgParams(orgId);
        render("Institute/header.html", includeInstFile, myOrgInfo);
    }

    public static void openAllNotifications(String orgId) {
        JSONObject myOrgInfo = _setOrgParams(orgId);
        render("Institute/allNotifications.html",myOrgInfo);
    }

    public static void allNotificationsDirect(String orgId) {
        JSONObject myOrgInfo = _setOrgParams(orgId);
        String includeInstFile = "Institute/allNotifications.html";
        flash.put("ENTRY", "DIRECT");
        String includeName = "Institute/header.html";
        render("Application/myPages.html", includeName, includeInstFile, myOrgInfo);
    }

    public static void referralTerms(String orgId) {
        String includeInstFile = "Institute/referralTerms.html";
        JSONObject myOrgInfo = _setOrgParams(orgId);
        render("Institute/header.html", includeInstFile, myOrgInfo);
    }

    public static void referralTermsDirect(String orgId) {
        JSONObject myOrgInfo = _setOrgParams(orgId);
        String includeInstFile = "Institute/referralTerms.html";
        flash.put("ENTRY", "DIRECT");
        String includeName = "Institute/header.html";
        render("Application/myPages.html", includeName, includeInstFile, myOrgInfo);
    }

    public static void openInstAvailPrograms(String orgId) {
        JSONObject myOrgInfo = _setOrgParams(orgId);
        render("Institute/myProgramsPage.html",myOrgInfo);
    }

    public static void myProgramsPage(String orgId) {
        String includeInstFile = "Institute/myProgramsPage.html";
        JSONObject myOrgInfo = _setOrgParams(orgId);
        render("Institute/header.html", includeInstFile, myOrgInfo);
    }

    public static void myProgramsPageDirect(String orgId) {
        JSONObject myOrgInfo = _setOrgParams(orgId);
        String includeInstFile = "Institute/myProgramsPage.html";
        flash.put("ENTRY", "DIRECT");
        String includeName = "Institute/header.html";
        render("Application/myPages.html", includeName, includeInstFile, myOrgInfo);
    }

    public static void getMySections() {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/getMemberCategorySections", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        boolean showUserPaymentInfo = true;
        render("UIComMicrosite/categorySections.html", resp, showUserPaymentInfo);
    }

    public static void getSchedule(){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/classroomconnect/getSchedule", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }
    public static void removeDaySchedule(){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/classroomconnect/removeDaySchedule", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }
    public static void removeSchedule(){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/classroomconnect/removeSchedule", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }
    public static void getScheduleDayInfo(){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/classroomconnect/getDaySchedule", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject scheduleDayInfo = getJSON(promise);
        render("Institute/getScheduleDayInfo.html",scheduleDayInfo);
    }
    public static void getSectionPayInfoPopup(String itemName) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/members/getPaymentInfo", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        render(resp,itemName);
    }
    public static void payAndAddMemberToSection(String orgId,String item_sku,
            String payment_request_id, String payment_status, String transactionId, String transactionStatus){

        Logger.log4j.error("Divesh payment_request_id : "+ payment_request_id);
        Logger.log4j.error("Divesh payment_status : "+payment_status);
        // If item_sku is EMPTY, Then payment channel is INSTAMOJO, Else payment channel is EBS
        if(StringUtils.isEmpty(item_sku)){
            // From Instamojo
            Map<String, Object> allParams = getReqParams();
            Promise<JSONResponseWrapper> promise = client(ClientUtil.BILLING_WEB_SERVICE_URL
                    + "/payments/confirmPayment", allParams);
            Logger.log4j.info("Divesh BEFORE AWAIT");
            await(promise);
            Logger.log4j.info("Divesh AFTER AWAIT");
            JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
            Logger.log4j.info("Divesh AFTER resp"+resp.toString());
            try {
                Logger.log4j.info("Divesh in try");
                item_sku = resp.getJSONObject("result").getString("item_sku");
                Logger.log4j.error("Divesh item_sku : "+ item_sku);
                transactionStatus = resp.getJSONObject("result").getString("status");
                transactionId = resp.getJSONObject("result").getString("transactionId");
            } catch (JSONException e) {
                Logger.log4j.error("Divesh Something went wrong "+ e.getMessage());
            }
        }
        JSONObject myOrgInfo = _setOrgParams(orgId);
        String includeInstFile = "Institute/myProgramsPage.html";
        flash.put("ENTRY", "DIRECT");
        String includeName = "Institute/header.html";
        render("Application/myPages.html", includeName, includeInstFile, myOrgInfo,
                item_sku,transactionId,transactionStatus);
    }
    private static JSONObject _getSectionPaySKU(String sectionId,String orgId,
            String targetProfile,String transactionId) throws Exception{

        Map<String, Object> allParams = getReqParams();
        allParams.put("transactionId", transactionId);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.BILLING_WEB_SERVICE_URL
                + "/payments/getOrderItemInfo", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        String errorCode = resp.getString("errorCode");
        JSONObject jsonObj=null;
        if(StringUtils.isEmpty(errorCode)){
            JSONObject item = resp.getJSONObject("result").getJSONObject("item");
            sectionId = item.getString("sectionId");
            String programId = item.getString("programId");
            String centerId = item.getString("centerId");
            String itemName = item.getString("name");
            jsonObj = new JSONObject();
            String targetOrgMemberId = _getOrgParams(orgId).getOrgMemberId();
            String itemSku = "orgId_"+orgId+"#sectionIds[0]_"+sectionId+"#centerId_"+centerId+"#programId_"+programId+"#targetOrgMemberId_"+targetOrgMemberId+"#targetProfile_"+targetProfile+"#itemName_"+itemName;
            jsonObj.put("itemSKU", itemSku);
            String callbackUrl = "/organization/"+orgId+"/postPaymentAddSection";
            jsonObj.put("callbackUrl", callbackUrl);
        }
        return jsonObj;
    }
    public static void retryOrderProcess(@Required String orderId,String orgId,String targetProfile) throws Exception {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.BILLING_WEB_SERVICE_URL
                + "/payments/getTransactionStatus", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        String errorCode = resp.getString("errorCode");
        if(StringUtils.isEmpty(errorCode)){
            JSONObject result = resp.getJSONObject("result");
            String deviceType = result.getString("deviceType");
            String callbackUrl = result.getString("callbackUrl");
            String itemSKU = result.getString("item_sku");
            String transactionId = result.getString("transactionId");
            String transactionStatus = result.getString("transactionStatus");
            if(!"WEB".equals(deviceType)){
                JSONObject item = result.getJSONObject("item");
                String itemType= item.getString("type");
                String itemId = item.getString("id");
                JSONObject retObj;
                if("SECTION".equals(itemType)){
                    retObj = _getSectionPaySKU(itemId,orgId,targetProfile,transactionId);
                }else{
                    String msg = play.i18n.Messages.get("payment.NOT_A_WEB_TRANSACTION",orderId);
                    render("UIComRegister/msgPage.html",msg);
                    return;
                }
                if(retObj!=null){
                    callbackUrl = retObj.getString("callbackUrl");
                    itemSKU = retObj.getString("itemSKU");
                }else{
                    String msg = play.i18n.Messages.get("payment.NOT_A_WEB_TRANSACTION",orderId);
                    render("UIComRegister/msgPage.html",msg);
                    return;
                }
            }
            if("SUCCESS".equals(transactionStatus) && callbackUrl!=null){
                itemSKU = URLEncoder.encode(itemSKU, "UTF-8");
                String url = callbackUrl+"?transactionId="+transactionId
                        +"&transactionStatus="+transactionStatus+"&item_sku="+itemSKU;
                Logger.log4j.info("URL ========== "+url);
                redirect(url);
                //render("UIComRegister/redirectPage.html", url);
                return;
            }
        }
        String msg = play.i18n.Messages.get("payment.PAYMENT_RETRY_FAILED",orderId);
        render("UIComRegister/msgPage.html",msg);
    }

    public static JSONObject getCertificate(){
    	Map<String, Object> allParams = getReqParams();
    	Logger.log4j.info("allParams : "+allParams);
    	Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL + "/contents/checkWhetherProgramIsCompleted", allParams);
        Logger.log4j.info("BEFORE AWAIT : GET MY CERTIFICATE");
        await(promise);
        Logger.log4j.info("AFTER AWAIT : GET MY CERTIFICATE");
        JSONObject data = ResponseUtil.checkResponse(getJSON(promise));
        render("Institute/getCertificate.html",data);
    	return data;
    }
}
