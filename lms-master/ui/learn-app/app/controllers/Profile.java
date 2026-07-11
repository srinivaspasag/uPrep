package controllers;

import static controllers.Institute._setOrgParams;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import controllers.AbstractUIController.JSONResponseWrapper;
import play.Logger;
import play.libs.F.Promise;
import play.mvc.Scope;
import play.mvc.With;
import util.ClientUtil;
import util.ResponseUtil;

@With(Security.class)
public class Profile extends AbstractUIController {

    static final String className = Profile.class.getSimpleName();

    public static void getMyAccessCodes(){
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        request.params.put("forUserId", session.get("userId"));
        Promise<JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/accessCodes/getAccessCodes",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        render(renderTheme(orgId, getHTMLFilePath("Institute","accessCodesPopup")),resp);
    }

    public static void profilePage() {
        String orgId = session.get("loginOrgId");
        String profileInfoType = Scope.Params.current().get("profileInfo");
        JSONObject profileResponse = _reqProfileInfo(null);
        render(renderTheme(orgId, getHTMLFilePath(className)),profileResponse, profileInfoType);
    }

    public static void profileView() {
        String orgId = session.get("loginOrgId");
        Map<String, Object> allParams = getReqParams();
        JSONObject profileResponse = _reqProfileInfo(allParams);
        allParams.put("targetUserId", session.get("userId"));
        JSONObject myPointsInfo = _getMyTotalPoints(allParams);
        render(renderTheme(orgId, getHTMLFilePath(className)),profileResponse, myPointsInfo);
    }

    public static void saveEduInfo() {
        JSONObject profileResponse = addEducationalProfileResponse(null);
        renderJSON(profileResponse);
    }

    public static void deleteEduInfo() {
        JSONObject profileResponse = deleteEducationalProfileInfo(null);
        renderJSON(profileResponse);
    }

    public static void editEduInfo() {
        JSONObject profileResponse = editEducationalProfileResponse(null);
        renderJSON(profileResponse);
    }

    public static void savePersonalInfo() {
        JSONObject profileResponse = addPersonalProfileResponse(null);
        renderJSON(profileResponse);
    }

    public static void getUserOverallQuestionAnalytics() {
        JSONObject stats = reqUserQuestionAnalytics(null);
        renderJSON(stats.toString());
    }

    public static void getUserOverallTestAnalytics() {
        JSONObject stats = reqUserTestAnalytics(null);
        renderJSON(stats.toString());
    }

    private static JSONObject reqUserTestAnalytics(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.QUESTIONS_WEB_SERVICE_URL + "/attempts/getUserOverallTestAnalytics", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject profileResponse = getJSON(promise);
        return profileResponse;
    }

    private static JSONObject reqUserQuestionAnalytics(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.QUESTIONS_WEB_SERVICE_URL + "/attempts/getUserOverallQuestionAnalytics", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject profileResponse = getJSON(promise);
        return profileResponse;
    }

    private static JSONObject addEducationalProfileResponse(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.PROFILE_WEB_SERVICE_URL + "/profiles/addEducational", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject profileResponse = getJSON(promise);
        return profileResponse;
    }

    private static JSONObject deleteEducationalProfileInfo(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.PROFILE_WEB_SERVICE_URL + "/profiles/removeEducational", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject profileResponse = getJSON(promise);
        return profileResponse;
    }

    private static JSONObject editEducationalProfileResponse(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.PROFILE_WEB_SERVICE_URL + "/profiles/editEducational", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject profileResponse = getJSON(promise);
        return profileResponse;
    }

    private static JSONObject addPersonalProfileResponse(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.PROFILE_WEB_SERVICE_URL + "/profiles/editPersonal", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject profileResponse = getJSON(promise);
        return profileResponse;
    }

    public static void profileSettings() {
        String orgId = session.get("loginOrgId");
        String editType = Scope.Params.current().get("editType");
        JSONObject profileResponse = _reqProfileInfo(null);
        render(renderTheme(orgId, getHTMLFilePath(className)),profileResponse, editType);
    }

    public static void profileInfo() {
        String orgId = session.get("loginOrgId");
        JSONObject profileResponse = _reqProfileInfo(null);
        render(renderTheme(orgId, getHTMLFilePath(className)),profileResponse);
    }

    public static void profileOthers() {
        String orgId = session.get("loginOrgId");
        Map<String, Object> allParams = getReqParams();
        JSONObject profileResponse = _reqProfileInfo(allParams);
        JSONObject userPointsInfo = _getMyTotalPoints(allParams);
        render(renderTheme(orgId, getHTMLFilePath(className)),profileResponse, userPointsInfo);
    }

    protected static JSONObject _reqProfileInfo(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.PROFILE_WEB_SERVICE_URL + "/profiles/getprofile", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject profileResponse = getJSON(promise);
        return profileResponse;
    }

    public static void pointsInfo() {
        Scope.Params.current().put("targetUserId", session.get("userId"));
        JSONObject pointResponse = reqPointsInfo(null);
        if (pointResponse == null) {
            renderJSON("No response received");
        }
        renderJSON(pointResponse.toString());
    }

    public static void profileStats() {
        String orgId = session.get("loginOrgId");
        Logger.log4j.info("sending profile stats");
        render(renderTheme(orgId, getHTMLFilePath(className)));
    }

    public static void editProfile(String profURL) {
        String url = Scope.Params.current().get("url");
        Promise<JSONResponseWrapper> promise = client(ClientUtil.PROFILE_WEB_SERVICE_URL + "/profiles/" + url, null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject saveProf = getJSON(promise);
        renderJSON(saveProf.toString());
    }

    public static void resetPassword(String profURL) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.PROFILE_WEB_SERVICE_URL + "/password/resetPassword", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject pass = getJSON(promise);
        renderJSON(pass.toString());
    }

    public static void submitFeedback() {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.PROFILE_WEB_SERVICE_URL + "/CollectFeedback/submitFeedback", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        renderJSON(resp.toString());
    }

//    utilities
    public static JSONObject getUserClassAndUrl(JSONObject user) {
        //Logger.log4j.error("=========:");
        //Logger.log4j.error(user);
        JSONObject userClassAndUrl = new JSONObject();
        String orgId = Scope.Params.current().get("orgId");
        try {
            String userId = user.getString("id");
            if (user.has("userId") && user.has("orgId")) {
                userId = user.getString("userId");
            }
            userClassAndUrl.put("id", userId);
            if (orgId != null && !orgId.isEmpty()) {
                userClassAndUrl.put("url", "/organization/" + orgId + "/profile/" + userId);
                userClassAndUrl.put("className", "openInstProfile");
            } else if (session.get("userId").equals(userId)) {
                userClassAndUrl.put("url", "/myprofile");
                userClassAndUrl.put("className", "openMyProfile");
            } else if (user.has("agentOf") && !user.getJSONObject("agentOf").getString("type").equals("UNKNOWN")) {
                JSONObject agent = user.getJSONObject("agentOf");
                String id = agent.getString("id");
                String type = agent.getString("type").toLowerCase();
                userClassAndUrl.put("url", "/" + type + "/" + id);
                userClassAndUrl.put("className", "open" + type + "Profile");
            } else {
                userClassAndUrl.put("url", "/user/" + userId);
                userClassAndUrl.put("className", "openUserProfile");
            }
        } catch (Exception e) {
            Logger.log4j.error(e.getMessage(), e);
        }
        return userClassAndUrl;
    }
    // for right section
    public static JSONObject reqPointsInfo(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.POINTS_WEB_SERVICE_URL + "/points/getPoints", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject pointsInfo = getJSON(promise);
        return pointsInfo;
    }

    public static JSONObject getProfileMatch(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.PROFILE_WEB_SERVICE_URL + "/profiles/profileMatch", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject match = getJSON(promise);
        return match;
    }

    public static JSONObject _getMyTotalPoints(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.PROFILE_WEB_SERVICE_URL + "/points/getPoints", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject match = getJSON(promise);
        return match;
    }
}
