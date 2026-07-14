package controllers;

import java.io.File;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import controllers.AbstractUIController.JSONResponseWrapper;
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
import util.ClientUtil;
import util.ResponseUtil;
import util.Validation;
import util.WebUtil;

@With(Security.class)
public class Institute extends AbstractUIController {

    static final Integer        maxSimilarDoubts = 10;
    static final String         className        = Institute.class.getSimpleName();
    private static final String RESULT_LIST_STR  = "{'list':[{id:'',name:'No Sorting',order:'DESC'},{id:'attempts',name:'Most Attempted',order:'DESC'},{id:'attempts',name:'Least Attempted',order:'ASC'}]}";
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
                if (!"ACTIVE".equals(org.getString("userState"))) {
                    session.put("BLOCKED_ORG_ID", org.getString("id"));
                }
                _setUserSession(org);
            }
        } catch (JSONException ex) {
            Logger.log4j.error(ex.getMessage());
        }
        return orgArray;
    }

    public static void library(String programId) throws JSONException {
        String orgId = session.get("loginOrgId");
        if (orgId != null || !"".equals(orgId)) {
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
            String includeLibraryFile = renderTheme(orgId, getHTMLFilePath("Library", "subjects"));
            render(renderTheme(orgId, getHTMLFilePath(className, "library")), resp,
                    includeLibraryFile,programId);
        } else {
            render(renderTheme(orgId, getHTMLFilePath("errors", "404")));
        }
    }

    protected static boolean _clearInstCacheKey(String orgId, String userId) {
        String key = _getInstCacheKey(orgId);
        Cache.delete(key);
        return true;
    }

    protected static boolean _forceCleanOrgCache(String orgId) {
        String key = _getInstCacheKey(orgId);
        OrgInfo org = Cache.get(key, OrgInfo.class);
        if (org != null) {
            return Cache.safeDelete(key);
        }
        return true;
    }

    public static void libraryDirect(String programId) {
        String orgId = session.get("loginOrgId");
        if (orgId != null || !"".equals(orgId)) {
            JSONObject myOrgInfo = _setOrgParams(orgId);
            recordActivity(ClientUtil.ActivityPages.LIBRARY, ClientUtil.ActivityAction.OPEN);
            flash.put("ENTRY", "DIRECT");
            Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                    + "/application/getContentResponse", getReqParams());
            Logger.log4j.info("BEFORE AWAIT");
            await(promise);
            Logger.log4j.info("AFTER AWAIT");
            JSONObject resp = getJSON(promise);
            String includeLibraryFile = renderTheme(orgId, getHTMLFilePath("Library", "subjects"));
            String includeInstFile = renderTheme(orgId, getHTMLFilePath(className, "library"));
            render(renderTheme(orgId, getHTMLFilePath(null, "header")), includeInstFile, resp,
                    myOrgInfo, includeLibraryFile,programId);
        } else {
            render(renderTheme(orgId, getHTMLFilePath("errors", "404")));
        }
    }

    public static void addNewDoubt() {
        String orgId = session.get("loginOrgId");
        String targetUserId = session.get("userId");
        request.params.put("orgId", orgId);
        request.params.put("targetUserId", targetUserId);
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
        String flag = "singleDoubt";
        render("tags/institute/widgets/doubt.html", discuss, errorMessage,flag);
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

    public static void singleComment() {
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
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
        render("tags/widgets/commentPost.html", comData);
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

    public static void getReferralPage() throws JSONException{
        String orgId = session.get("loginOrgId");
        String targetUserId = session.get("userId");
        request.params.put("orgId", orgId);
        request.params.put("targetUserId", targetUserId);
        Map<String, Object> allParams = getReqParams();
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/members/getReferralData", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        String baseUrl = Play.configuration.getProperty("mydomain.url");
        JSONObject resp = getJSON(promise);
        resp.put("baseUrl", baseUrl);
        render(renderTheme(orgId, getHTMLFilePath(className, "referral")), resp);
    }

    public static void getReferralPageDirect() throws JSONException{
        String orgId = session.get("loginOrgId");
        String targetUserId = session.get("userId");
        request.params.put("orgId", orgId);
        request.params.put("targetUserId", targetUserId);
        request.params.put("campaignType", "REFERRAL");
        Map<String, Object> allParams = getReqParams();
        JSONObject myOrgInfo = _setOrgParams(orgId);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/members/getReferralData", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        String baseUrl = Play.configuration.getProperty("mydomain.url");
        JSONObject resp = getJSON(promise);
        resp.put("baseUrl", baseUrl);
        String includeInstFile = renderTheme(orgId, getHTMLFilePath(className, "referral"));
        render(renderTheme(orgId, getHTMLFilePath(null, "header")), includeInstFile, resp,myOrgInfo);
    }

    public static void profileDirect() {
        String orgId = session.get("loginOrgId");
        String targetUserId = session.get("userId");
        request.params.put("orgId", orgId);
        request.params.put("targetUserId", targetUserId);
        if (orgId != null || !"".equals(orgId)) {
            JSONObject myOrgInfo = _setOrgParams(orgId);
            try {
                recordActivity(ClientUtil.ActivityPages.PROFILE, ClientUtil.ActivityAction.OPEN,
                        ClientUtil.Entity.USER, targetUserId);
            } catch (Exception ex) {
            }
            Map<String, Object> allParams = getReqParams();
            JSONObject profileResp = null;// Profile._reqProfileInfo(allParams);
            JSONObject memberInfo = _getMemberInfo(allParams);
            JSONObject userInfo = _getUserInfo(allParams);
            JSONObject profileAnalytics = null;
            String userRole = Scope.Params.current().get("userRole");
            if (userRole.equals("STUDENT")) {
                profileAnalytics = _getProfileAnalytics(allParams);
            }
            String includeInstFile = renderTheme(orgId, getHTMLFilePath(className, "profile"));
            render(renderTheme(orgId, getHTMLFilePath(null, "header")), includeInstFile,
                    profileResp, memberInfo, userInfo, profileAnalytics, myOrgInfo);
        } else {
            render(renderTheme(orgId, getHTMLFilePath("errors", "404")));
        }
    }

    public static void OthersProfileDirect(@Required String orgId, @Required String targetUserId ) {
        request.params.put("orgId", orgId);
        request.params.put("targetUserId", targetUserId);
        if (orgId != null || !"".equals(orgId)) {
            JSONObject myOrgInfo = _setOrgParams(orgId);
            try {
                recordActivity(ClientUtil.ActivityPages.PROFILE, ClientUtil.ActivityAction.OPEN,
                        ClientUtil.Entity.USER, targetUserId);
            } catch (Exception ex) {
            }
            Map<String, Object> allParams = getReqParams();
            JSONObject profileResp = null;
            JSONObject memberInfo = _getMemberInfo(allParams);
            JSONObject userInfo = _getUserInfo(allParams);
            JSONObject profileAnalytics = null;
            String userRole = Scope.Params.current().get("userRole");
            if (userRole.equals("STUDENT")) {
                profileAnalytics = _getProfileAnalytics(allParams);
            }
            String includeInstFile = renderTheme(orgId, getHTMLFilePath(className, "profile"));
            render(renderTheme(orgId, getHTMLFilePath(null, "header")), includeInstFile,
                    profileResp, memberInfo, userInfo, profileAnalytics, myOrgInfo);
        } else {
            render(renderTheme(orgId, getHTMLFilePath("errors", "404")));
        }
    }

    public static void profile() {
        String orgId = session.get("loginOrgId");
        String targetUserId = session.get("userId");
        request.params.put("orgId", orgId);
        request.params.put("targetUserId", targetUserId);
        if (orgId != null || !"".equals(orgId)) {
            JSONObject myOrgInfo = _setOrgParams(orgId);
            try {
                recordActivity(ClientUtil.ActivityPages.PROFILE, ClientUtil.ActivityAction.OPEN,
                        ClientUtil.Entity.USER, targetUserId);
            } catch (Exception ex) {
            }
            Map<String, Object> allParams = getReqParams();
            JSONObject profileResp = null;// Profile._reqProfileInfo(allParams);
            JSONObject memberInfo = _getMemberInfo(allParams);
            JSONObject userInfo = _getUserInfo(allParams);
            JSONObject profileAnalytics = null;
            String userRole = Scope.Params.current().get("userRole");
            if (userRole.equals("STUDENT")) {
                profileAnalytics = _getProfileAnalytics(allParams);
            }
            render(renderTheme(orgId, getHTMLFilePath(className)), profileResp, memberInfo,
                    userInfo, myOrgInfo, profileAnalytics);
        } else {
            render(renderTheme(orgId, getHTMLFilePath("errors", "404")));
        }
    }

    public static void activityComments() throws JSONException {
        String orgId = session.get("loginOrgId");
        String targetUserId = session.get("userId");
        request.params.put("orgId", orgId);
        request.params.put("targetUserId", targetUserId);
        JSONObject params = new JSONObject();
        params.put("rootId", request.params.get("rootId"));
        params.put("rootType", request.params.get("rootId"));
        Map<String, Object> allParams = getReqParams();
        JSONObject comments = Widgets._getCommItems(allParams);
        render("tags/widgets/comments.html", comments,params);
    }

    public static void getNotifications() {
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        String targetUserId = session.get("userId");
        request.params.put("targetUserId", targetUserId);
        String feedType = Scope.Params.current().get("feedType");
        String url = "getNotifcations";
        if (feedType != null && feedType.equals("OLD")) {
            url = "getOlderNotifications";
        }
        JSONObject notis = _getNotifications(null, url);
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

    public static void getNotificationsSummary() {
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        JSONObject notiSummary = _getNotificationsSummary();
        JSONObject msgNoti = UserMessages._getNotifications();
        JSONObject news = new JSONObject();
        try {
            if (msgNoti != null && msgNoti.getString("errorCode").isEmpty()) {
                news.put("messages", msgNoti.getJSONObject("result"));
            }
        } catch (JSONException ex) {
            Logger.log4j.error(ex.getLocalizedMessage());
        }
        try {
            if (notiSummary != null && notiSummary.getString("errorCode").isEmpty()) {
                news.put("others", notiSummary.getJSONObject("result"));
            }
        } catch (JSONException ex) {
            Logger.log4j.error(ex.getLocalizedMessage());
        }
        renderJSON(news.toString());
    }

    protected static JSONObject _getNotificationsSummary() {
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.COMM_SERVICE_URL
                + "/newsFeeds/getNotifcationsSummary", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject notiSummary = getJSON(promise);
        return notiSummary;
    }

    protected static JSONObject _getNotifications(Map<String, Object> allParams, String url) {

        Promise<JSONResponseWrapper> promise = client(ClientUtil.COMM_SERVICE_URL + "/newsFeeds/"
                + url, allParams);

        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        resp = Validation.verifyResponse(resp);
        return resp;
    }

    public static void analytics() {
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        if(orgId != null || !"".equals(orgId)){
            Map<String, Object> allParams = getReqParams();
            recordActivity(ClientUtil.ActivityPages.ANALYTICS, ClientUtil.ActivityAction.OPEN);
            JSONObject profileAnalytics = null;
            JSONObject myOrgInfo = _setOrgParams(orgId);
            JSONObject memberInfo = _getViewableDomains(allParams);
            String userRole = Scope.Params.current().get("userRole");
            if (userRole.equals("STUDENT")) {
                profileAnalytics = _getProfileAnalytics(allParams);
            }
            String targetUserId = Scope.Params.current().get("targetUserId");
            String userId = session.get("userId");
            if (targetUserId == null || targetUserId.isEmpty()) {
                allParams.put("targetUserId", userId);
                Scope.Params.current().put("targetUserId", userId);
            }
            render(renderTheme(orgId, getHTMLFilePath(className)), memberInfo, profileAnalytics, userRole,myOrgInfo);
        }else{
            render(renderTheme(orgId, getHTMLFilePath("errors","404")));
        }
    }

    public static void aiims(){
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        if(orgId != null || !"".equals(orgId)){
            render(renderTheme(orgId, getHTMLFilePath(className)));
        }
        else{
            render(renderTheme(orgId, getHTMLFilePath("errors","404")));
        }
    }

    public static void aiimsDirect(){
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        if(orgId != null || !"".equals(orgId)){
            JSONObject myOrgInfo = _setOrgParams(orgId);
            String includeInstFile = renderTheme(orgId,getHTMLFilePath(className,"aiims"));
            boolean flag = true;
            render(renderTheme(orgId, getHTMLFilePath(null,"header")), includeInstFile,myOrgInfo,flag);
        }
        else{
            render(renderTheme(orgId, getHTMLFilePath("errors","404")));
        }

    }

    public static void jeeadvanced(){
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        if(orgId != null || !"".equals(orgId)){
            render(renderTheme(orgId, getHTMLFilePath(className)));
        }
        else{
            render(renderTheme(orgId, getHTMLFilePath("errors","404")));
        }
    }

    public static void jeeadvancedDirect(){
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        if(orgId != null || !"".equals(orgId)){
            JSONObject myOrgInfo = _setOrgParams(orgId);
            boolean flag = true;
            String includeInstFile = renderTheme(orgId,getHTMLFilePath(className,"jeeadvanced"));
            render(renderTheme(orgId, getHTMLFilePath(null,"header")), includeInstFile,myOrgInfo,flag);
        }
        else{
            render(renderTheme(orgId, getHTMLFilePath("errors","404")));
        }

    }

    public static void jeemain(){
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        if(orgId != null || !"".equals(orgId)){
            render(renderTheme(orgId, getHTMLFilePath(className)));
        }
        else{
            render(renderTheme(orgId, getHTMLFilePath("errors","404")));
        }
    }

    public static void jeemainDirect(){
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        if(orgId != null || !"".equals(orgId)){
            JSONObject myOrgInfo = _setOrgParams(orgId);
            boolean flag = true;
            String includeInstFile = renderTheme(orgId,getHTMLFilePath(className,"jeemain"));
            render(renderTheme(orgId, getHTMLFilePath(null,"header")), includeInstFile,myOrgInfo,flag);
        }
        else{
            render(renderTheme(orgId, getHTMLFilePath("errors","404")));
        }

    }

    public static void neet(){
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        if(orgId != null || !"".equals(orgId)){
            render(renderTheme(orgId, getHTMLFilePath(className)));
        }
        else{
            render(renderTheme(orgId, getHTMLFilePath("errors","404")));
        }
    }


    public static void neetDirect(){
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        if(orgId != null || !"".equals(orgId)){
            JSONObject myOrgInfo = _setOrgParams(orgId);
            boolean flag = true;
            String includeInstFile = renderTheme(orgId,getHTMLFilePath(className,"neet"));
            render(renderTheme(orgId, getHTMLFilePath(null,"header")), includeInstFile,myOrgInfo,flag);
        }
        else{
            render(renderTheme(orgId, getHTMLFilePath("errors","404")));
        }

    }

    public static void bitsat(){
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        if(orgId != null || !"".equals(orgId)){
            render(renderTheme(orgId, getHTMLFilePath(className)));
        }
        else{
            render(renderTheme(orgId, getHTMLFilePath("errors","404")));
        }
    }

    public static void bitsatDirect(){
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        if(orgId != null || !"".equals(orgId)){
            JSONObject myOrgInfo = _setOrgParams(orgId);
            boolean flag = true;
            String includeInstFile = renderTheme(orgId,getHTMLFilePath(className,"bitsat"));
            render(renderTheme(orgId, getHTMLFilePath(null,"header")), includeInstFile,myOrgInfo,flag);
        }
        else{
            render(renderTheme(orgId, getHTMLFilePath("errors","404")));
        }

    }

    public static void analyticsDirect() {
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        if(orgId != null || !"".equals(orgId)){
            JSONObject myOrgInfo = _setOrgParams(orgId);
            Map<String, Object> allParams = getReqParams();
            recordActivity(ClientUtil.ActivityPages.ANALYTICS, ClientUtil.ActivityAction.OPEN);
            JSONObject profileAnalytics = null;

            JSONObject memberInfo = _getViewableDomains(allParams);
            String userRole = Scope.Params.current().get("userRole");
            if (userRole.equals("STUDENT")) {
                profileAnalytics = _getProfileAnalytics(allParams);
            }
            String targetUserId = Scope.Params.current().get("targetUserId");
            String userId = session.get("userId");
            if (targetUserId == null || targetUserId.isEmpty()) {
                allParams.put("targetUserId", userId);
                Scope.Params.current().put("targetUserId", userId);
            }
            String includeInstFile = renderTheme(orgId,getHTMLFilePath(className,"analytics"));
            render(renderTheme(orgId, getHTMLFilePath(null,"header")), includeInstFile, myOrgInfo, memberInfo, profileAnalytics, userRole);
        }else{
            render(renderTheme(orgId, getHTMLFilePath("errors","404")));
        }
    }

    public static void emailVerifiedPage(String userId, String orgId) {
        String sessionUserId = session.get("userId");
        if (!StringUtils.isEmpty(sessionUserId) && sessionUserId.equals(userId)) {
            JSONArray orgArray = Institute._getUserOrgs(getReqParams(), true);
            orgId = Security._getOrgId(orgArray);
            flash.put("emailVerified", "done");
            redirect("/profile");
        } else {
            Security._logout();
            flash.put("emailVerified", "done");
            String instLoginPageUrl = Register._getOrgRefererUrl(orgId);
            if (StringUtils.isEmpty(instLoginPageUrl)) {
                redirect("/login");
            } else {
                redirect("/");
            }
        }
    }

    public static void openAllNotifications(String orgId) {
        orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        JSONObject myOrgInfo = _setOrgParams(orgId);
        render(renderTheme(orgId, getHTMLFilePath(className,"allNotifications")),myOrgInfo);
    }

    public static void allNotificationsDirect(String orgId) {
        orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        JSONObject myOrgInfo = _setOrgParams(orgId);
        String includeInstFile = renderTheme(orgId, getHTMLFilePath(className,"allNotifications"));
        render(renderTheme(orgId, getHTMLFilePath(null,"header")), includeInstFile, myOrgInfo);
    }

    public static void drawStudentTATestTable() {
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        Map<String, Object> allParams = getReqParams();
        JSONObject tests = _getTATestsStudent(allParams);
        String userRole = Scope.Params.current().get("userRole");
        render("tags/studentTATables.html", tests, userRole);
    }

    public static void drawTATestTable() {
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        Map<String, Object> allParams = getReqParams();
        JSONObject tests = _getTATests(allParams);
        String userRole = Scope.Params.current().get("userRole");
        render("tags/institute/TATables.html", tests, userRole);
    }

    public static void getTATestCenters() {
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
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

    public static void openDoubt() {
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        Map<String, Object> allParams = getReqParams();
        JSONObject discussion = _getDiscussion(allParams);
        JSONObject sortTypeList = _getArrayFromStrList(COMMENT_LIST_STR);
        render(renderTheme(orgId, getHTMLFilePath(className)), discussion, sortTypeList);
    }

    public static void getSimilarDoubts() {
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
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

    public static void directDoubt(@Required String dissId) {
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        if (orgId != null || !"".equals(orgId)) {
            JSONObject myOrgInfo = _setOrgParams(orgId);
            try {
                recordActivity(ClientUtil.ActivityPages.DISCUSSION, ClientUtil.ActivityAction.OPEN, ClientUtil.Entity.DISCUSSION, dissId);
            } catch (Exception ex) {
            }
            JSONObject discussion = _getDiscussion(null);
            JSONObject sortTypeList = _getArrayFromStrList(COMMENT_LIST_STR);
            String includeInstFile = renderTheme(orgId,getHTMLFilePath(className,"openDoubt"));
            render(renderTheme(orgId, getHTMLFilePath(null,"header")), includeInstFile, myOrgInfo, discussion, sortTypeList);
        } else {
            render(renderTheme(orgId, getHTMLFilePath("errors","404")));
        }
    }

    private static JSONObject _getArrayFromStrList(String str) {
        JSONObject resultTypeList = null;
        try {
            resultTypeList = new JSONObject(str);
        } catch (JSONException ex) {
        }
        return resultTypeList;
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
            _markEntityView(Scope.Params.current().get("id"), ClientUtil.Entity.DISCUSSION);
        } catch (Exception err) {
        }
        return discussion;
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

    public static void deleteDoubt() {
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
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

    public static void getProgrammeInfo() {
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        JSONObject data = _getProgramCourses(getReqParams());
        renderJSON(data.toString());
    }

    protected static JSONObject _getProgramCourses(Map<String, Object> allParams) {
        if (allParams != null) {
            allParams.put("recordState", "ACTIVE");
        } else {
            request.params.put("recordState", "ACTIVE");
        }
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/getProgramCourses", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        resp = Validation.verifyResponse(resp);
        return resp;
    }

    public static void getMoreActivityFeeds() {
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        Map<String, Object> allParams = getReqParams();
        Scope.Params.current().put("feedType", "OLD");
        JSONObject resp = _getActivityFeeds(allParams);
        String noFeedMsg = Messages.get("NO_MORE_FEEDS");
        render(renderTheme(orgId,getHTMLFilePath(className,"activityFeeds")), resp, noFeedMsg);
    }

    public static void moreActivityComments() throws JSONException {
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        JSONObject params = new JSONObject();
        params.put("rootId", request.params.get("rootId"));
        params.put("rootType", request.params.get("rootId"));
        Map<String, Object> allParams = getReqParams();
        JSONObject comments = Widgets._getCommItems(allParams);
        render("tags/widgets/commentList.html", comments,params);
    }

    public static void deleteActivityFeed() {
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
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

    private static JSONObject _getProfileAnalytics(Map<String, Object> allParams) {
        if (allParams != null) {
            allParams.put("entityType", "TEST");
        }
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/analytics/getUserAnalyticsStats", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        resp = Validation.verifyResponse(resp);
        return resp;
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

    public static void getWalletBalance() throws JSONException {
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        Map<String, Object> allParams = getReqParams();
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/members/getWalletBalance", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        renderJSON(resp.toString());
    }

    public static void getMySections() {
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/getMemberCategorySections", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        boolean showUserPaymentInfo = true;
        try {
            resp.put("isPartOf", true);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        render(renderTheme(orgId, getHTMLFilePath(className,"categorySections")), resp, showUserPaymentInfo);
    }

    public static void getCategorySections(){
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        if(session.contains("userId") && !StringUtils.isEmpty(session.get("userId"))){
            request.params.put("excludeSubscribed", "true");
        }
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/getCategorySections", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        try {
            resp.put("isPartOf", false);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        render(renderTheme(orgId, getHTMLFilePath(className,"categorySections")),resp);
    }

    public static void getSingleProgramPopup(){
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/getCategorySection", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        render(renderTheme(orgId, getHTMLFilePath(className,"singleProgramPopup")),resp);
    }

    public static void getSectionPayInfoPopup(String itemName) {
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/members/getPaymentInfo", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        render(renderTheme(orgId,getHTMLFilePath(className)),resp,itemName);
    }

    public static void payAndAddMemberToSection(String orgId,String item_sku,
            String transactionId,String transactionStatus, String payment_request_id, String payment_status){
        Logger.log4j.error("Divesh payment_request_id : "+ payment_request_id);
        Logger.log4j.error("Divesh payment_status : "+payment_status);
        orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        if(StringUtils.isEmpty(item_sku)){
            // From Instamojo
            Map<String, Object> allParams = getReqParams();
            allParams.put("orgId",orgId);
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
        String includeInstFile = renderTheme(orgId,getHTMLFilePath(className,"programs"));
        render(renderTheme(orgId, getHTMLFilePath(null,"header")), includeInstFile, myOrgInfo,item_sku,transactionId,transactionStatus);
    }

    public static void getDoubts() {
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        JSONObject discussions = getInstituteDoubts(getReqParams());
        render("tags/institute/doubts.html", discussions);
    }

    public static void programs() {
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        if(orgId != null || !"".equals(orgId)){
            recordActivity(ClientUtil.ActivityPages.PROGRAM, ClientUtil.ActivityAction.OPEN);
            render(renderTheme(orgId, getHTMLFilePath(className,"programs")));
        }else{
            render(renderTheme(orgId, getHTMLFilePath("errors","404")));
        }
    }

    public static void programsDirect() {
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        if(orgId != null || !"".equals(orgId)){
            JSONObject myOrgInfo = _setOrgParams(orgId);
            recordActivity(ClientUtil.ActivityPages.PROGRAM, ClientUtil.ActivityAction.OPEN);
            String includeInstFile = renderTheme(orgId,getHTMLFilePath(className,"programs"));
            render(renderTheme(orgId, getHTMLFilePath(null,"header")), includeInstFile, myOrgInfo);
        }else {
            render(renderTheme(orgId, getHTMLFilePath("errors","404")));
        }
    }

    public static void doubts() {
        String orgId = session.get("loginOrgId");
        if(orgId != null || !"".equals(orgId)){
            recordActivity(ClientUtil.ActivityPages.DOUBTS, ClientUtil.ActivityAction.OPEN);
            render(renderTheme(orgId, getHTMLFilePath(className,"doubts")));
        }else{
            render(renderTheme(orgId, getHTMLFilePath("errors","404")));
        }
    }

    public static void doubtsDirect() {
        String orgId = session.get("loginOrgId");
        if(orgId != null || !"".equals(orgId)){
            JSONObject myOrgInfo = _setOrgParams(orgId);
            recordActivity(ClientUtil.ActivityPages.DOUBTS, ClientUtil.ActivityAction.OPEN);
            flash.put("ENTRY", "DIRECT");
            String includeInstFile = renderTheme(orgId,getHTMLFilePath(className,"doubts"));
            render(renderTheme(orgId, getHTMLFilePath(null,"header")), includeInstFile, myOrgInfo);
        }else {
            render(renderTheme(orgId, getHTMLFilePath("errors","404")));
        }
    }

    public static void referralTerms() {
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        render(renderTheme(orgId, getHTMLFilePath(className,"referralTerms")));
    }

    public static void referralTermsDirect() {
        String orgId = session.get("loginOrgId");
        JSONObject myOrgInfo = _setOrgParams(orgId);
        String includeInstFile = renderTheme(orgId,getHTMLFilePath(className,"referralTerms"));
        flash.put("ENTRY", "DIRECT");
        render(renderTheme(orgId, getHTMLFilePath(null,"header")), includeInstFile, myOrgInfo);
    }

    public static void getActivityFeeds() {
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        Map<String, Object> allParams = getReqParams();
        JSONObject resp = _getActivityFeeds(allParams);
        String noFeedMsg = Messages.get("NO_RECENT_ACTIVITY");
        render(renderTheme(orgId,getHTMLFilePath(className,"activityFeeds")), resp, noFeedMsg);
    }

    protected static JSONObject getInstituteDoubts(Map<String, Object> allParams) {
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/discussions/getDiscussions", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject instituteResponse = getJSON(promise);
        instituteResponse = Validation.verifyResponse(instituteResponse);
        return instituteResponse;
    }

    protected static JSONObject _getActivityFeeds(Map<String, Object> allParams) {
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
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

    public static void activities() {
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        try {
            recordActivity(ClientUtil.ActivityPages.STATUS_FEED, ClientUtil.ActivityAction.OPEN);
        } catch (Exception ex) {
        }
        Scope.Params.current().put("entityType", "STATUSFEED");
        if(orgId != null || !"".equals(orgId)){
            JSONObject myOrgInfo = _setOrgParams(orgId);
            JSONObject memberInfo = _getViewableDomains(null);
            render(renderTheme(orgId, getHTMLFilePath(className,"activities")),memberInfo,myOrgInfo);
        }else{
            render(renderTheme(orgId, getHTMLFilePath("errors","404")));
        }
    }

    public static void activitiesDirect() {
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        if(orgId != null || !"".equals(orgId)){
            JSONObject myOrgInfo = _setOrgParams(orgId);
            recordActivity(ClientUtil.ActivityPages.STATUS_FEED, ClientUtil.ActivityAction.OPEN);
            flash.put("ENTRY", "DIRECT");
            JSONObject memberInfo = _getViewableDomains(null);
            String includeInstFile = renderTheme(orgId,getHTMLFilePath(className,"activities"));
            render(renderTheme(orgId, getHTMLFilePath(null,"header")), includeInstFile,memberInfo,myOrgInfo);
        }else {
            render(renderTheme(orgId, getHTMLFilePath("errors","404")));
        }
    }

    public static void getActivityJSON() {
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        Map<String, Object> allParams = getReqParams();
        JSONObject resp = _getActivityFeeds(allParams);
        renderJSON(resp.toString());
    }

    public static void getUserDoubtAsked() {
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        Map<String, Object> allParams = getReqParams();
        allParams.put("orderBy", "timeCreated");
        allParams.put("resultType", "CREATED");
        allParams.put("facet", false);
        JSONObject discussions = getInstituteDoubts(allParams);
        render("tags/institute/dbtTemplate.html", discussions);
    }

    public static void addRemark(){
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        try{
            Application.recordActivity(ClientUtil.ActivityPages.PROFILE,ClientUtil.ActivityAction.ADD,ClientUtil.Entity.REMARK,"");
        }catch(Exception ex){}
        Promise<JSONResponseWrapper> promise = client(ClientUtil.COMM_SERVICE_URL + "/remarks/addRemark", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject data = getJSON(promise);
        data = Validation.verifyResponse(data);
        renderJSON(data.toString());
    }
    public static void remarks(){
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.COMM_SERVICE_URL + "/remarks/getRemarksForUser", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject remarks = Validation.verifyResponse(getJSON(promise));
        render(remarks);
    }
    public static void getMemberMappings() {
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
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
        render(renderTheme(orgId, getHTMLFilePath(className)),mappings, targetProfile);
    }

    public static void getProfileExtraInfo(String orgId, @Required String targetUserId) {
        orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        Map<String, Object> allParams = getReqParams();
        JSONObject memberInfo = ResponseUtil.checkResponse(_getMemberInfo(allParams));
        render(renderTheme(orgId, getHTMLFilePath(className)),memberInfo);
    }

    public static void uploadActiForm(String orgId) {
        orgId = session.get("loginOrgId");
        String isResponse = "no";
        render("tags/widgets/imageUpload.html", isResponse);
    }

    public static void uploadFeedImg(File imageFile) {
        request.params.put("type", "STATUSFEED");
        request.params.put("uploadFileParamName", "imageFile");
        JSONObject data = uploadUtil(ClientUtil.COMM_SERVICE_URL
                + "/statusFeeds/uploadImage", null, imageFile);
        data = Validation.verifyResponse(data);
        String isResponse = "yes";
        render("tags/widgets/imageUpload.html", data, isResponse);
    }

    public static void fetchExtVideo() throws Exception {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL + "/videos/getVideoInfo", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject data = getJSON(promise);
        data = Validation.verifyResponse(data);
        render("tags/widgets/drawVideo.html", data);
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
        render("tags/widgets/drawLink.html", data);
    }

    public static void addActivityFeed() {
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
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
        render(renderTheme(orgId,getHTMLFilePath(className,"activityPost")), resp);
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

    public static JSONObject _getMemberInfo(Map<String, Object> allParams) {
        String targetUserId = Scope.Params.current().get("targetUserId");
        String userId = session.get("userId");
        if (allParams == null) {
            allParams = getReqParams();
        }
        if (StringUtils.isEmpty(targetUserId)) {
            allParams.put("targetUserId", userId);
        }
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/members/getMemberProfile", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject memberResponse = getJSON(promise);
        memberResponse = Validation.verifyResponse(memberResponse);
        return memberResponse;
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
                org.put("doubtsForumMode", orgCompleteInfo.getJSONObject("result").getString("doubtsForumMode"));
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

    protected static JSONObject _getOrgInfo(Map<String, Object> allParams) {

        F.Promise<AbstractUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/getOrganization", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
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

    private static String _getInstCacheKey(String orgId) {
        String userId = session.get("userId");
        String[] val = {"ORG_INFO/", orgId, "/", userId};
        String key = StringUtils.join(val, "");
        return key;
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

    public static void redirectToNoOrgError() {
        String redirectUrl = "/noOrgFound";
        redirect(redirectUrl);
    }

    public static void noOrgFound(){
        render(renderTheme(getHTMLFilePath(className)));
    }

    public static void noAccess(){
        JSONObject resp = new JSONObject();
        String baseUrl = Play.configuration.getProperty("domain.url");
        try {
            resp.put("baseUrl", baseUrl);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        render(renderTheme(getHTMLFilePath(className)),resp);
    }
}
