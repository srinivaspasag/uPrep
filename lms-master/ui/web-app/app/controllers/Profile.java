package controllers;

import static controllers.Institute._setOrgParams;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.libs.F.Promise;
import play.mvc.Scope;
import play.mvc.With;
import uicom.util.ClientUtil;
import uicom.util.ResponseUtil;

@With(Security.class)
public class Profile extends AbstractUIController {

    public static void profilePage() {
        String profileInfoType = Scope.Params.current().get("profileInfo");
        JSONObject profileResponse = _reqProfileInfo(null);
        render(profileResponse, profileInfoType);
    }

    public static void profileView() {
        Map<String, Object> allParams = getReqParams();
        JSONObject profileResponse = _reqProfileInfo(allParams);
        allParams.put("targetUserId", session.get("userId"));
        JSONObject myPointsInfo = _getMyTotalPoints(allParams);
        render(profileResponse, myPointsInfo);
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
        String editType = Scope.Params.current().get("editType");
        JSONObject profileResponse = _reqProfileInfo(null);
        render(profileResponse, editType);
    }

    public static void profilePercent() {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.PROFILE_WEB_SERVICE_URL + "/profiles/profileCompleted", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject percent = getJSON(promise);
        Logger.log4j.info("response for %" + percent);
        try {
            if (percent.isNull("errorMessage") || !percent.getString("errorMessage").isEmpty()) {
                render("/Application/error.html");
            } else {
                renderJSON(percent.toString());
            }
        } catch (JSONException ex) {
            Logger.log4j.error(ex.getLocalizedMessage());
        }
    }

    public static void profileInfo() {
        JSONObject profileResponse = _reqProfileInfo(null);
        render(profileResponse);
    }

    public static void profileOthers() {
        Map<String, Object> allParams = getReqParams();
        JSONObject profileResponse = _reqProfileInfo(allParams);
        JSONObject userPointsInfo = _getMyTotalPoints(allParams);
        render(profileResponse, userPointsInfo);
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
        Logger.log4j.info("sending profile stats");
        render();
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

    public static void myProfileDirect() {
        Map<String, Object> allParams = getReqParams();
        JSONObject profileResponse = _reqProfileInfo(allParams);
        String includeName = "Profile/profileView.html";
        allParams.put("targetUserId", session.get("userId"));
        JSONObject myPointsInfo = _getMyTotalPoints(allParams);
        flash.put("ENTRY", "DIRECT");
        render("Application/myPages.html", includeName, profileResponse, myPointsInfo);
    }

    public static void profileOthersDirect(String targetUserId) {
        if (targetUserId.equals(session.get("userId"))) {
            Profile.myProfileDirect();
        } else {
            Scope.Params.current().put("targetUserId", targetUserId);
            Map<String, Object> allParams = getReqParams();
            JSONObject profileResponse = _reqProfileInfo(allParams);
            String includeName = "Profile/profileOthers.html";
            JSONObject userPointsInfo = _getMyTotalPoints(allParams);
            flash.put("ENTRY", "DIRECT");
            render("Application/myPages.html", includeName, profileResponse, userPointsInfo);
        }
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
    /*public static void upload(String qqfile){
     String actionUrl=ClientUtil.PROFILE_WEB_SERVICE_URL +"/profiles/uploadProfilePic";
     JSONObject resp =  uploadUtil(actionUrl,null,null);
     renderJSON(resp.toString());
     }*/

    public static void uploadProfilePic() {
        JSONObject resp = ResponseUtil.checkResponse(uploadUtil(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/members/uploadProfilePic", null, null));
        try {
            if (StringUtils.isEmpty(resp.getString("errorCode"))
                    && StringUtils.equals(session.get("userId"), request.params.get("targetUserId"))) {
                String thumbnail = resp.getJSONObject("result").getString("thumbnail");
                session.put("profilePic", thumbnail);
                String orgId = params.get("orgId");
                if (!StringUtils.isEmpty(orgId)) {
                    boolean ret = Institute._forceCleanOrgCache(orgId);
                    if (!ret) {
                        Institute._clearInstCacheKey(orgId, session.get("userId"));
                    }
                }
            }
        } catch (Exception e) {
            Logger.log4j.error("Error in setting org pic in session" + e.getMessage());
        }
        renderJSON(resp.toString());
    }

    //orders
    private static final String ordersType="STUDENT_BUY_ORDERS";
    public static void myOrders() {
        String orgId = request.params.get("orgId");
        JSONObject myOrgInfo = _setOrgParams(orgId);
        request.params.put("customer.id", session.get("userId"));
        request.params.put("ordersType", ordersType);
        JSONObject ordersResp = UIComInvoices._getBuyOrders(null);
        if (!StringUtils.isEmpty(orgId)) {
            String includeInstFile = "Profile/myOrders.html";
            render("Institute/header.html", includeInstFile, ordersResp, myOrgInfo);
        } else {
            render(ordersResp);
        }
    }

    public static void ordersTable() {
        JSONObject ordersResp = ResponseUtil.checkResponse(null);
        request.params.put("ordersType", ordersType);
        if (StringUtils.equalsIgnoreCase(session.get("userId"),
                request.params.get("customer.id"))) {
            ordersResp = UIComInvoices._getBuyOrders(null);
        }
        render("UIComInvoices/ordersTable.html", ordersResp);
    }

    public static void myOrdersDirect(String orgId) {
        JSONObject myOrgInfo = _setOrgParams(orgId);
        request.params.put("customer.id", session.get("userId"));
        request.params.put("customer.type", "USER");
        request.params.put("start", "0");
        request.params.put("size", "50");
        request.params.put("ordersType", ordersType);
        JSONObject ordersResp = UIComInvoices._getBuyOrders(null);

        String includeName = "Profile/myOrders.html";
        String includeInstFile = includeName;
        if (StringUtils.isNotEmpty(orgId)) {
            myOrgInfo = Institute._setOrgParams(orgId);
            includeName = "Institute/header.html";
        }
        flash.put("ENTRY", "DIRECT");
        render("Application/myPages.html", includeName, ordersResp, myOrgInfo, includeInstFile);
    }
    public static void getMyAccessCodes(){
        request.params.put("forUserId", session.get("userId"));
        Promise<JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/accessCodes/getAccessCodes",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        render("Institute/accessCodesPopup.html",resp);
    }
}
