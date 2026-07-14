package controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.json.JSONException;
import org.json.JSONObject;

import controllers.AbstractUIController.JSONResponseWrapper;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.data.validation.Required;
import play.libs.F;
import play.libs.F.Promise;
import play.mvc.Scope;
import uicom.response.ErrorInfo;
import uicom.response.JSONResponse;
import uicom.util.ClientUtil;
import uicom.util.ResponseUtil;
import uicom.util.Utilities;
import util.TabAppUrlFactory;



public class TabAppRouter extends AbstractUIController {

    public static ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<String, String>();
    public static String date = Utilities.getDateMonthYear(System.currentTimeMillis());

    //added the funtion to check User entry in DB from mobile App : Amrita
    public static void checkUserInDB(){
		JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE.getServiceUrl("checkUserInDB"));
		renderJSON(jsonData.toString());
    }
    //Check App version to force update android app.
    public static void checkAppVersion(){
		JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE
				.getServiceUrl("checkAppVersion"));
		if (jsonData != null) {
			try {
				JSONObject obj = jsonData.getJSONObject("result");
				boolean appVersion = obj.getBoolean("appVersion");
				jsonData.put("appVersion", appVersion);
			} catch (JSONException e) {
				Logger.log4j.debug("Error in checkAppVersion   "
						+ e.getMessage());
			}
		}
		renderJSON(jsonData.toString());
    }

    public static void getOrgInfo(String orgCmdsURL) {

        String cmdsBaseUrl = Play.configuration.getProperty("cmds.url");
        URL url = null;
        URL cmdsUrl = null;
        try {
            url = new URL(orgCmdsURL);
            cmdsUrl = new URL(cmdsBaseUrl);
        } catch (MalformedURLException e) {
            Logger.log4j.error(e.getMessage(), e);
            error(e.getMessage());
        }

        if (!StringUtils.equalsIgnoreCase(cmdsUrl.getHost(), url.getHost())) {
            Logger.log4j.error("mismatch on host url[" + orgCmdsURL
                    + "], and system cmds host url:" + cmdsBaseUrl);
            renderJSON(new JSONResponse(new ErrorInfo("INVALID_URL", orgCmdsURL
                    + " is not a valid cmds url")));
        }

        // below code will figure out slug and memberId

        String slug = StringUtils.substringBetween(url.getPath().replace("org/", ""), "/");
        String memberId = null;

        if (slug == null) {
            slug = StringUtils.substringAfterLast(url.getPath(), "/");
        } else {
            memberId = StringUtils.substringAfterLast(url.getPath(), "/");
        }

        Scope.Params.current().put("slug", slug);
        Scope.Params.current().put("getKey", String.valueOf(true));
        JSONObject json = __getJSONData(TabAppUrlFactory.INSTANCE.getServiceUrl("getOrgInfo"));
        try {
            JSONObject result = json.getJSONObject(RESULT);
            result.put("cmdsUrl", cmdsBaseUrl);
            if (StringUtils.isNotEmpty(memberId)) {
                Scope.Params.current().put("ensureCourseInfo", String.valueOf(true));
                Scope.Params.current().put("orgId", result.getString("id"));
                Scope.Params.current().put("memberId", memberId.toUpperCase());
                JSONObject orgInfo = getOrgMemberProfileInfo();
                if (StringUtils.isNotEmpty(orgInfo.getString(ERROR_CODE))) {
                    renderJSON(orgInfo.toString());
                }
                JSONObject orgProgfile = orgInfo.getJSONObject(RESULT);
                if (orgProgfile == null
                        || orgProgfile.get("info") == null
                        || !"OFFLINE_USER".equalsIgnoreCase(orgProgfile.getJSONObject("info")
                                .getString("profile"))) {
                    orgInfo.put(RESULT, "");
                    orgInfo.put(ERROR_CODE, "NOT_ALLOWED");
                    orgInfo.put(ERROR_MESSAGE,
                            "tablet offline configuration not allowed for memberId: " + memberId);
                    renderJSON(orgInfo.toString());
                }
                result.put("orgProfile", orgProgfile);
            }
        } catch (JSONException e) {
            Logger.log4j.error(e.getMessage(), e);
        }
        renderJSON(json.toString());
    }

    public static void authenticate(boolean useGlobalUsername) {

        Logger.log4j.info("authenticate user with orgCredential : " + useGlobalUsername);
        Map<String, Object> reqParams = getReqParams();
        JSONObject jsonData = useGlobalUsername ? __getJSONData(
                TabAppUrlFactory.INSTANCE.getServiceUrl("authenticateUser"), reqParams)
                : __getJSONData(TabAppUrlFactory.INSTANCE.getServiceUrl("authenticateMember"),
                        reqParams);
        addUserOrgProfileToJSON(jsonData, reqParams);
        renderJSON(jsonData.toString());
    }

    public static void sendOTP() {
        Map<String, Object> reqParams = getReqParams();
        JSONObject jsonData = __getJSONData(
                TabAppUrlFactory.INSTANCE.getServiceUrl("sendOTP"), reqParams);
        renderJSON(jsonData.toString());
    }

    public static void getAllUserData() {
        Map<String, Object> reqParams = getReqParams();
        JSONObject jsonData = __getJSONData(
                TabAppUrlFactory.INSTANCE.getServiceUrl("getAllUserData"), reqParams);
        renderJSON(jsonData.toString());
    }

    public static void validateOTP() {
        Map<String, Object> reqParams = getReqParams();
        JSONObject jsonData = __getJSONData(
                TabAppUrlFactory.INSTANCE.getServiceUrl("validateOTP"), reqParams);
        renderJSON(jsonData.toString());
    }

    public static void changeUserPassword(){
        Map<String, Object> reqParams = getReqParams();
        JSONObject jsonData = __getJSONData(
                TabAppUrlFactory.INSTANCE.getServiceUrl("changeUserPassword"), reqParams);
        renderJSON(jsonData.toString());
    }

    public static void getStudentsCount(){
        Map<String, Object> reqParams = getReqParams();
        JSONObject jsonData = __getJSONData(
                TabAppUrlFactory.INSTANCE.getServiceUrl("getStudentsCount"), reqParams);
        renderJSON(jsonData.toString());
    }

    public static void forgotPasswordUser(){
        Map<String, Object> reqParams = getReqParams();
        JSONObject jsonData = __getJSONData(
                TabAppUrlFactory.INSTANCE.getServiceUrl("forgotPasswordUser"), reqParams);
        renderJSON(jsonData.toString());
    }

    public static void forgotPasswordMember(){
        Map<String, Object> reqParams = getReqParams();
        JSONObject jsonData = __getJSONData(
                TabAppUrlFactory.INSTANCE.getServiceUrl("forgotPasswordMember"), reqParams);
        renderJSON(jsonData.toString());
    }

    public static void setPassword(){
        Map<String, Object> reqParams = getReqParams();
        JSONObject jsonData = __getJSONData(
                TabAppUrlFactory.INSTANCE.getServiceUrl("setPassword"), reqParams);
        renderJSON(jsonData.toString());
    }

    public static void changePassword(){
        Map<String, Object> reqParams = getReqParams();
        JSONObject jsonData = __getJSONData(
                TabAppUrlFactory.INSTANCE.getServiceUrl("changePassword"), reqParams);
        renderJSON(jsonData.toString());
    }

    public static void getSectionByAccessCode() {

        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE
                .getServiceUrl("getSectionByAccessCode"));
        try {
            JSONObject result = jsonData.getJSONObject(RESULT);
            JSONObject orgResult = result.getJSONObject("org");
            orgResult.put("cmdsUrl", Play.configuration.getProperty("cmds.url"));
        } catch (Exception e) {
            Logger.log4j.error(e.getMessage(), e);
        }
        renderJSON(jsonData.toString());
    }

    public static void getOrgCategories() {

        Map<String, Object> reqParams = getReqParams();
        JSONObject jsonData = __getJSONData(
                TabAppUrlFactory.INSTANCE.getServiceUrl("getOrgCategories"), reqParams);
        renderJSON(jsonData.toString());
    }

    public static void getReferralData() {

        Map<String, Object> reqParams = getReqParams();
        JSONObject jsonData = __getJSONData(
                TabAppUrlFactory.INSTANCE.getServiceUrl("getReferralData"), reqParams);
        renderJSON(jsonData.toString());
    }

    public static void getCategorySections() {

        Map<String, Object> reqParams = getReqParams();
        JSONObject jsonData = __getJSONData(
                TabAppUrlFactory.INSTANCE.getServiceUrl("getCategorySections"), reqParams);
        renderJSON(jsonData.toString());
    }

    public static void getOrganizations() {

        Map<String, Object> reqParams = getReqParams();
        JSONObject jsonData = __getJSONData(
                TabAppUrlFactory.INSTANCE.getServiceUrl("getOrganizations"), reqParams);
        renderJSON(jsonData.toString());
    }

    public static void getOrgMemberExtraInputFields() {

        Map<String, Object> reqParams = getReqParams();
        reqParams.put("targetOrgMemberProfile", "STUDENT");
        reqParams.put("checkIfSignupAllowed", "true");
        JSONObject jsonData = __getJSONData(
                TabAppUrlFactory.INSTANCE.getServiceUrl("getOrgMemberExtraInputFields"), reqParams);
        renderJSON(jsonData.toString());
    }

    public static void addUserToken() {

        Map<String, Object> reqParams = getReqParams();
        JSONObject jsonData = __getJSONData(
                TabAppUrlFactory.INSTANCE.getServiceUrl("addUserToken"), reqParams);
        renderJSON(jsonData.toString());
    }

    public static void recordTeacherResponse(){
        Map<String, Object> reqParams = getReqParams();
        JSONObject jsonData = __getJSONData(
                TabAppUrlFactory.INSTANCE.getServiceUrl("recordTeacherResponse"), reqParams);
        renderJSON(jsonData.toString());
    }

    public static void addOrgMember() {

        Map<String, Object> reqParams = getReqParams();
        JSONObject jsonData = __getJSONData(
                TabAppUrlFactory.INSTANCE.getServiceUrl("addOrgMember"), reqParams);
        addUserOrgProfileToJSON(jsonData, reqParams);
        renderJSON(jsonData.toString());
    }

    public static void addMemberWithAccessCode() {

        Map<String, Object> reqParams = getReqParams();
        JSONObject jsonData = __getJSONData(
                TabAppUrlFactory.INSTANCE.getServiceUrl("addMemberWithAccessCode"), reqParams);
        addUserOrgProfileToJSON(jsonData, reqParams);
        renderJSON(jsonData.toString());
    }

    public static void addOrgMemberMapping() {

        Map<String, Object> reqParams = getReqParams();
        JSONObject jsonData = __getJSONData(
                TabAppUrlFactory.INSTANCE.getServiceUrl("addOrgMemberMapping"), reqParams);
        renderJSON(jsonData.toString());
    }

    public static JSONObject getOrgMemberProfile() {

        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE
                .getServiceUrl("getOrgMemberProfile"));
        return jsonData;
    }

    public static void getProgramCourses() {

        Map<String, Object> reqParams = getReqParams();
        JSONObject jsonData = __getJSONData(
                TabAppUrlFactory.INSTANCE.getServiceUrl("getProgramCourses"), reqParams);
        renderJSON(jsonData.toString());
    }

    public static void startTransaction() {

        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE
                .getServiceUrl("startTransaction"));
        renderJSON(jsonData.toString());
    }

    public static void updateTransaction() {

        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE
                .getServiceUrl("updateTransaction"));
        renderJSON(jsonData.toString());
    }

    public static void getDemoContentReq() {

        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE
                .getServiceUrl("getDemoContentReq"));
        renderJSON(jsonData.toString());
    }

    public static void applyCoupon(){
        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE
                .getServiceUrl("applyCoupon"));
        renderJSON(jsonData.toString());
    }

    public static void verifyAccessCode() {

        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE
                .getServiceUrl("verifyAccessCode"));
        renderJSON(jsonData.toString());
    }

    public static void getBuyOrders() {

        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE.getServiceUrl("getBuyOrders"));
        renderJSON(jsonData.toString());
    }

    public static void recordLogin() {

        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE.getServiceUrl("recordLogin"));

        renderJSON(jsonData.toString());
    }

    public static void recordLogout() {

        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE.getServiceUrl("recordLogout"));
        renderJSON(jsonData.toString());
    }

    public static void recordActivity() {

        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE
                .getServiceUrl("recordActivity"));
        renderJSON(jsonData.toString());
    }

    public static void getContentLinks() {
//        JSONObject jsonData = null;
//        if(Scope.Params.current().get("orgId").equalsIgnoreCase("55f6b821e4b06863a03b2fcc")){
//            String key = Scope.Params.current().get("addedAfter")+"_"+Scope.Params.current().get("target.id")+"_"+Scope.Params.current().get("target.type")+"_getContentLinks";
//            Logger.log4j.info("Key :: "+key);
//            String resp = Cache.get(key,String.class);
//            if(!StringUtils.isEmpty(resp)){
//                Logger.log4j.info("Served getContentLinks from cache");
//                jsonData = getCacheDataInJsonObject(resp);
//            }else{
//                jsonData = __getJSONData(TabAppUrlFactory.INSTANCE
//                        .getServiceUrl("getContentLinks"));
//                if(jsonData != null)
//                    Cache.set(key, jsonData.toString(), "1h");
//            }
//        }else{
        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE
                    .getServiceUrl("getContentLinks"));
//        }
        renderJSON(jsonData.toString());
    }

    public static void getTests() {

        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE
                .getServiceUrl("getTests"));

        renderJSON(jsonData.toString());
    }

    public static void getOrgPointsOfSale(){
        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE
                .getServiceUrl("getOrgPointsOfSale"));

        renderJSON(jsonData.toString());
    }

    public static void getPrograms(){
        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE
                .getServiceUrl("getPrograms"));

        renderJSON(jsonData.toString());
    }

    public static void getMembers(){
        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE
                .getServiceUrl("getMembers"));

        renderJSON(jsonData.toString());
    }

    public static void getProgramCenters(){
        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE
                .getServiceUrl("getProgramCenters"));

        renderJSON(jsonData.toString());
    }

    public static void getSections(){
        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE
                .getServiceUrl("getSections"));

        renderJSON(jsonData.toString());
    }

    public static void getSectionPackageInfo(){
        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE
                .getServiceUrl("getSectionPackageInfo"));

        renderJSON(jsonData.toString());
    }

    public static void getOrgMemberWithEmail(){
        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE
                .getServiceUrl("getOrgMemberWithEmail"));

        renderJSON(jsonData.toString());
    }

    public static void getSaleDetails(){
        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE
                .getServiceUrl("getSaleDetails"));

        renderJSON(jsonData.toString());
    }

    public static void updateSaleDetails(){
        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE
                .getServiceUrl("updateSaleDetails"));

        renderJSON(jsonData.toString());
    }

    public static void getRemovedContentLinks() {

        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE
                .getServiceUrl("getRemovedContentLinks"));

        renderJSON(jsonData.toString());
    }

    public static void getContentDownloadLink() {

        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE
                .getServiceUrl("getContentDownloadLink"));

        renderJSON(jsonData.toString());
    }

    public static void getPdfUrl() {

        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE
                .getServiceUrl("getPdfUrl"));

        renderJSON(jsonData.toString());
    }

    public static void getContentsCache() {

        JSONObject jsonData = null;
        String type = request.params.get("entity.type");
        Logger.log4j.info("Attempt Log :: Params "+type);
        if(!StringUtils.isEmpty(type)){
            if(type.equals("TEST")){
                Logger.log4j.info("Entered getContentsCache Of Type TEST");
                String testId = request.params.get("entity.id");
                Logger.log4j.info("Key :: "+testId+"getContentsCache");
                String resp = Cache.get(testId+"getContentsCache", String.class);
                if(!StringUtils.isEmpty(resp)){
                    Logger.log4j.info("Fetching getContentsCache from cache");
                    renderJSON(resp);
                }else{
                    Logger.log4j.info("Fetching getContentsCache from server");
                    jsonData = __getJSONData(TabAppUrlFactory.INSTANCE.getServiceUrl("getContents"));
                    if(jsonData != null)
                        Cache.set(testId+"getContentsCache",jsonData.toString(),"1h");
                }
            }else{
                Logger.log4j.info("Entered getContentsCache Of Type not TEST");
                jsonData = __getJSONData(TabAppUrlFactory.INSTANCE.getServiceUrl("getContents"));
            }
        }else{
            Logger.log4j.info("Entered getContentsCache Of Type Empty");
            jsonData = __getJSONData(TabAppUrlFactory.INSTANCE.getServiceUrl("getContents"));
        }
        renderJSON(jsonData.toString());
    }

    public static void getContentsMap() {

        JSONObject jsonData = new JSONObject();
        String type = request.params.get("entity.type");
        Logger.log4j.info("Attempt Log :: Params "+type);
        if(!StringUtils.isEmpty(type)){
            if(type.equals("TEST")){
                Logger.log4j.info("Entered getContentsMap Of Type TEST");
                String testId = request.params.get("entity.id");
                Logger.log4j.info("Key :: "+testId+"getContentsMap");
                if(cache.containsKey(testId+"getContentsMap") && isSameDay()){
                    Logger.log4j.info("Fetching getContentsMap from cache");
                    renderJSON(cache.get(testId+"getContentsMap"));
                }else{
                    Logger.log4j.info("Fetching getContentsMap from server");
                    jsonData = __getJSONData(TabAppUrlFactory.INSTANCE.getServiceUrl("getContents"));
                    if(jsonData != null)
                        cache.put(testId+"getContentsMap",jsonData.toString());
                }
            }else{
                Logger.log4j.info("Entered getContentsMap Of Type not TEST");
                jsonData = __getJSONData(TabAppUrlFactory.INSTANCE.getServiceUrl("getContents"));
            }
        }else{
            Logger.log4j.info("Entered getContentsMap Of Type Empty");
            jsonData = __getJSONData(TabAppUrlFactory.INSTANCE.getServiceUrl("getContents"));
        }

        renderJSON(jsonData.toString());
    }

    public static void getContents() {

        JSONObject jsonData = null;
        String type = request.params.get("entity.type");
        Logger.log4j.info("Attempt Log :: Params "+type);
        if(!StringUtils.isEmpty(type)){
            if(type.equals("TEST")){
                Logger.log4j.info("Entered getContents Of Type TEST");
                String testId = request.params.get("entity.id");
                Logger.log4j.info("Key :: "+testId+"_getContents");
                String resp = Cache.get(testId+"_getContents", String.class);
                if(!StringUtils.isEmpty(resp)){
                    Logger.log4j.info("Fetching getContents from cache");
//                    jsonData = getCacheDataInJsonObject(resp);
                    renderJSON(resp);
                }else{
                    Logger.log4j.info("Fetching getContents from server");
                    jsonData = __getJSONData(TabAppUrlFactory.INSTANCE.getServiceUrl("getContents"));
                    if(jsonData != null)
                        Cache.set(testId+"_getContents",jsonData.toString(),"1h");
                }
            }else{
                Logger.log4j.info("Entered getContents Of Type not TEST");
                jsonData = __getJSONData(TabAppUrlFactory.INSTANCE.getServiceUrl("getContents"));
            }
        }else{
            Logger.log4j.info("Entered getContents Of Type Empty");
            jsonData = __getJSONData(TabAppUrlFactory.INSTANCE.getServiceUrl("getContents"));
        }

        renderJSON(jsonData.toString());
    }

    private static boolean isSameDay() {
        String currentDate = Utilities.getDateMonthYear(System.currentTimeMillis());
        if (currentDate.equals(date)) {
            return true;
        } else {
            date = currentDate;
            cache.clear();
            return false;
        }
    }

    public static void getSchedule() {
        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE.getServiceUrl("getSchedule"));
        renderJSON(jsonData.toString());
    }

    public static void getDaySchedule(){
        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE.getServiceUrl("getDaySchedule"));
        renderJSON(jsonData.toString());
    }

    public static void getEntityMarkDistribution() {

        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE
                .getServiceUrl("getEntityMarkDistribution"));

        renderJSON(jsonData.toString());
    }

    public static void getTestInfo() {

        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE.getServiceUrl("getTestInfo"));

        renderJSON(jsonData.toString());
    }

    public static void getTestInfoWithQuestions(){
        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE.getServiceUrl("getTestQuestions"));
        renderJSON(jsonData.toString());
    }

    public static void getEntityQuestionAttempts() {

        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE
                .getServiceUrl("getEntityQuestionAttempts"));

        renderJSON(jsonData.toString());
    }

    public static void getEntityScheduleAnalytics() {

        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE
                .getServiceUrl("getEntityScheduleAnalytics"));
        renderJSON(jsonData.toString());
    }

    public static void uploadImage() throws IOException {

        String bytes = request.params.get("bytesString");
        File imageFile = new File(getUploadPath() + File.separator + RandomUtils.nextLong()
                + request.params.get("imageName"));
        FileOutputStream fileOutputStream = new FileOutputStream(imageFile);

 //      Logger.log4j.info("inside uploadImage : "+imageFile);
   //    Logger.log4j.info("inside uploadImage callingUserId : "+request.params.get("callingUserId"));
     //  Logger.log4j.info("inside uploadImage userId : "+request.params.get("userId"));
        byte[] imageData = Base64.decodeBase64(bytes);
        fileOutputStream.write(imageData);
        fileOutputStream.flush();
        fileOutputStream.close();
        request.params.put("uploadFileParamName", "imageFile");
        Scope.Params.current().all().remove("bytesString");

       // Logger.log4j.info("inside uploadImage imagFile : "+imageFile);
 //       Logger.log4j
   //             .info("File UpuploadFileParamNameload REQ Came =============================== ");
        JSONObject data = uploadUtil(ClientUtil.CONTENT_SERVICE_URL + "/uploads/uploadImage", null,
                imageFile);
        Logger.info("uploaded file response from  server " + data);
        renderJSON(data.toString());
    }

    public static void getBoards() {

        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE.getServiceUrl("getBoards"));

        renderJSON(jsonData.toString());
    }

    public static void getEntityQuestionsAttemptStat() {

        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE
                .getServiceUrl("getEntityQuestionsAttemptStat"));

        renderJSON(jsonData.toString());
    }

    public static void getEntityTestStatus() {
        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE
                .getServiceUrl("getEntityTestStatus"));
        renderJSON(jsonData.toString());
    }

    public static void getModuleSchedules() {
        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE
                .getServiceUrl("getModuleSchedules"));
        renderJSON(jsonData.toString());
    }

    public static void getCurrentTime(){
        JSONObject jsonData = new JSONObject();
        JSONObject result =  new JSONObject();
        try {
            result.put("currentTime", System.currentTimeMillis());
            jsonData.put(ERROR_MESSAGE,"");
            jsonData.put(ERROR_CODE,"");
            jsonData.put(RESULT,result);
        } catch (JSONException e) {
            Logger.log4j.debug("Error in getting current time" + e.getMessage());
        }
        renderJSON(jsonData.toString());
    }

    public static void syncTabletAnalytics() {
        //recordActivity(ClientUtil.ActivityPages.SYNC_TABLET_ANALYTICS, ClientUtil.ActivityAction.SYNC_USER_TEST_DATA);
        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE
                .getServiceUrl("syncTabletAnalytics"));

        renderJSON(jsonData.toString());
    }

    public static void getAttemptedEntities() {

        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE
                .getServiceUrl("getAttemptedEntities"));

        renderJSON(jsonData.toString());
    }

    public static void getEntityLeaderBoard() {

        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE
                .getServiceUrl("getEntityLeaderBoard"));

        renderJSON(jsonData.toString());
    }

    public static void getUserEntityRank() {

        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE
                .getServiceUrl("getUserEntityRank"));

        renderJSON(jsonData.toString());
    }

    public static void getQuestionsSolutions() {

        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE
                .getServiceUrl("getQuestionsSolutions"));

        renderJSON(jsonData.toString());
    }

    public static void addDiscussion() {

        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE
                .getServiceUrl("addDiscussion"));

        renderJSON(jsonData.toString());
    }

    public static void getDiscussions() {

        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE
                .getServiceUrl("getDiscussions"));

        renderJSON(jsonData.toString());
    }

    public static void getDiscussionInfo() {

        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE
                .getServiceUrl("getDiscussionInfo"));

        renderJSON(jsonData.toString());
    }

    public static void getSimilarDiscussions() {

        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE
                .getServiceUrl("getSimilarDiscussions"));

        renderJSON(jsonData.toString());
    }

    public static void addComment() {

        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE.getServiceUrl("addComment"));

        renderJSON(jsonData.toString());
    }

    public static void getComments() {

        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE.getServiceUrl("getComments"));

        renderJSON(jsonData.toString());
    }

    public static void follow() {

        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE.getServiceUrl("follow"));

        renderJSON(jsonData.toString());
    }

    public static void unFollow() {

        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE.getServiceUrl("unFollow"));

        renderJSON(jsonData.toString());
    }

    public static void upVote() {

        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE.getServiceUrl("upVote"));

        renderJSON(jsonData.toString());
    }

    public static void view() {

        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE.getServiceUrl("view"));

        renderJSON(jsonData.toString());
    }

    public static void getFollowers() {

        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE.getServiceUrl("getFollowers"));

        renderJSON(jsonData.toString());
    }

    private static JSONObject getOrgMemberProfileInfo() {

        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE
                .getServiceUrl("getOrgMemberProfile"));
        return jsonData;
    }

    private static JSONObject getMemberProfile(Map<String, Object> reqParams) {

        JSONObject jsonData = __getJSONData(
                TabAppUrlFactory.INSTANCE.getServiceUrl("getMemberProfile"), reqParams);
        return jsonData;
    }

    public static JSONObject moduleEntryStatusSyncer(Map<String, Object> reqParams) {

        JSONObject jsonData = __getJSONData(
                TabAppUrlFactory.INSTANCE.getServiceUrl("moduleEntryStatusSyncer"), reqParams);
        return jsonData;
    }

    public static void saveTestUserData(Map<String, Object> reqParams) {
        Logger.error("Inside saveTestUserData");
        JSONObject jsonData = __getJSONData(
                TabAppUrlFactory.INSTANCE.getServiceUrl("saveTestUserData"), reqParams);
        renderJSON(jsonData.toString());
    }

    //register GCM registration id
    public static void registerById(){
        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE.getServiceUrl("registerById"));
        renderJSON(jsonData.toString());
    }

    // FOR INSTAMOJO ACCESS TOKEN
    public static void getInstaMojoAccessToken() {
        Logger.log4j.debug("INSIDE ACCESS TOKEN  ");
        Map<String, Object> reqParams = getReqParams();
        reqParams.put("callingUserId", "PUBLIC");
        reqParams.put("userId", "PUBLIC");
        reqParams.put("callingApp", "CMDS-APP");
        reqParams.put("callingAppId", "CMDS-APP");
        Logger.error("Inside getInstaMojoAccessToken   " + reqParams);
        JSONObject jsonData = __getJSONData(
                TabAppUrlFactory.INSTANCE.getServiceUrl("getInstaMojoAccessToken"), reqParams);
        Logger.log4j.debug("INSIDE ACCESS TOKEN  " + jsonData);
        try {
            renderText(jsonData.getJSONObject("result").getString("access_token"));
        } catch (JSONException e) {
            Logger.log4j.debug("Error in access token" + e.getMessage());
        }
        // renderJSON(jsonData.toString());
    }

    public static void getEntityInfo(){
        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE
                .getServiceUrl("getEntityInfoForApp"));
        renderJSON(jsonData.toString());
    }

    public static void addEntityRatingAndFeedback(){
        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE
                .getServiceUrl("addRatingAndFeedback"));
        renderJSON(jsonData.toString());
    }

	public static void ping(){
		JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE.getServiceUrl("ping"));
		JSONObject result =  new JSONObject();
		try {
			if (!jsonData.getString(ERROR_CODE).equals("")) {
				jsonData.put(ERROR_MESSAGE,"Backend service is down");
				result.put("success", false);
				jsonData.put(RESULT,result);
			}
			else{
				result.put("success", true);
				jsonData.put(RESULT,result);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		renderJSON(jsonData.toString());
	}

    private static JSONObject __getJSONData(String url) {

        return __getJSONData(url, getReqParams());
    }

    private static JSONObject __getJSONData(String url, Map<String, Object> allParams) {

//      Logger.log4j.info("fetching json data from backend server url:" + url);
        JSONObject jsonResponse = null;
        try {
            F.Promise<AbstractUIController.JSONResponseWrapper> promise = client(url, allParams);
//          Logger.log4j.info("BEFORE AWAIT");
            await(promise);
//          Logger.log4j.info("AFTER AWAIT");
            jsonResponse = getJSON(promise);
            if (jsonResponse == null) {
                Logger.error("no response from backend server : " + jsonResponse);
                jsonResponse = new JSONObject();
                jsonResponse.put(ERROR_CODE, "SERVICE_ERROR");
            }
        } catch (IllegalArgumentException e) {
            Logger.log4j.error(e.getMessage(), e);
        } catch (JSONException e) {
            Logger.log4j.error(e.getMessage(), e);
        }
        return jsonResponse;
    }

    private static void addUserOrgProfileToJSON(JSONObject jsonData, Map<String, Object> reqParams) {

        try {
            if (StringUtils.isEmpty(jsonData.getString(ERROR_CODE))
                    && jsonData.get(RESULT) instanceof JSONObject) {
                JSONObject resultJSON = jsonData.getJSONObject(RESULT);
                String userId = null;
                try {
                    userId = resultJSON.getString("userId");
                } catch (Exception e) {}

                try {
                    if (userId == null) {
                        userId = resultJSON.getString("id");
                    }

                } catch (Exception e) {}

                try {
                    if (reqParams.get("orgId") == null) {
                        reqParams.put("orgId", resultJSON.getString("orgId"));
                    }
                } catch (Exception e) {}
                reqParams.put(USER_ID, userId);
                reqParams.put("getKey", "true");
                reqParams.put(CALLING_USER_ID, userId);
                reqParams.put(TARGET_USER_ID, userId);
                reqParams.put("ensureCourseInfo", String.valueOf(true));
                reqParams.put("callingApp", "TabletApp");
                reqParams.put("callingAppId", "TabletApp");
                JSONObject orgInfo = getMemberProfile(reqParams);
                jsonData.getJSONObject(RESULT).put("orgProfile", orgInfo.get(RESULT));

            }
        } catch (JSONException e) {
            Logger.log4j.error(e.getMessage(), e);
        }
    }

    private static final String ERROR_CODE      = "errorCode";
    private static final String ERROR_MESSAGE   = "errorMessage";

    private static final String RESULT          = "result";
    private static final String USER_ID         = "userId";
    private static final String CALLING_USER_ID = "callingUserId";
    private static final String TARGET_USER_ID  = "targetUserId";

}
