package controllers;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.libs.F;
import play.libs.F.Promise;
import play.mvc.Scope.Params;
import play.mvc.With;
import pojos.UserOrg;
import uicom.response.JSONResponse;
import uicom.util.ClientUtil;
import uicom.util.ResponseUtil;
import util.CacheUtil;

@With(Security.class)
public class Widgets extends AbstractQRUIController {

    public static void topicTree() throws JSONException {

        JSONArray topics = new JSONArray();
        try {
            topics = UIComBoards._getOrgBoards(null).getJSONObject("result").getJSONArray("list");
        } catch (Exception e) {
            Logger.log4j.info("problem in fetching topics " + e.getMessage());
        }
        render(topics);
    }

    public static void addMember() {

        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/UserManagements/addMember", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    // public static void getExams(){
    // JSONObject resp=UIComWidgets._getCmdsExams(null);
    // renderJSON(resp.toString());
    // }
    public static void uiSamples() {

        render();
    }

    // for changing between cmds of one org to another

    public static void main() {

        String orgId = getOrgRequestParams(null);
        redirect("/organization/" + orgId + "/resources");
    }

    protected static void setOrgRequestParams(String orgId) {

        if (StringUtils.isEmpty(orgId)) {
            error(500, "You do not have permission to view this page.");
        }
        getOrgRequestParams(orgId);
    }

    public static void setUserOrgs() {

        Map<String, UserOrg> orgs = CacheUtil.getAndCacheUserOrgs(session.get("userId"));
        render(orgs);
    }

    protected static String getOrgRequestParams(String orgId) {

        UserOrg org = getOrgInfo(orgId);
        if (org == null) {
            Logger.log4j.info("No organizations found for " + session.get("username"));
            // session.clear();
            UIComSecurity._logout();
            Application.noOrgsFound();
        }
        request.params.put("orgId", org.getOrgId());
        request.params.put("memberId", org.getMemberId());
        request.params.put("orgMemberId", org.getOrgMemberId());
        request.params.put("profile", org.getOrgUserProfile());

        return org.getOrgId();
    }

    protected static UserOrg getCurrentOrgInfo(String orgId) {

        UserOrg org = getOrgInfo(orgId);
        // if(org!=null){
        // Logger.log4j.info("setting orgId and orgInfo in flash");
        // flash.put("orgId",org.getId());
        // flash.put("orgName",org.getName());
        // flash.put("orgThumbnail",org.getOrgThumbnail());
        // flash.put("memberId",org.getMemberId());
        // flash.put("orgMemberId",org.getOrgMemberId());
        // flash.put("currentOrg", org);
        // flash.put("orgUserProfile", org.getOrgUserProfile());
        // flash.put("orgUserFirstName", org.getOrgUserFirstName());
        // flash.put("orgUserLastName", org.getOrgUserLastName());
        // flash.put("orgUserFullName",org.getOrgUserFullName());
        // flash.put("orgUserProfilePic",org.getOrgUserProfilePic());
        // }
        return org;
    }

    private static UserOrg getOrgInfo(String orgId) {

        Map<String, UserOrg> userOrgsMap = CacheUtil.getAndCacheUserOrgs(session.get("userId"));
        UserOrg org = null;
        if (userOrgsMap.isEmpty()) {
            return null;
        }
        if (StringUtils.isNotEmpty(orgId)) {
            if (userOrgsMap.containsKey(orgId)) {
                org = userOrgsMap.get(orgId);
            } else {
                return null;
            }
        } else {
            for (String key : userOrgsMap.keySet()) {
                org = userOrgsMap.get(key);
                break;
            }
        }
        if(!"ACTIVE".equals(org.getUserState())){
                    session.put("BLOCKED_ORG_ID", org.getOrgId());
        }
        session.put("orgAuthType", org.getAuthType());
        //Logger.log4j.info("authType set in session by orgs of users ================ >>>> "+session.get("orgAuthType"));
        return org;
    }
    protected static boolean _amISuperAdmin(UserOrg currentOrgInfo){
        if(currentOrgInfo!=null && "MANAGER".equals(currentOrgInfo.getOrgUserProfile())){
            JSONObject memberExtraInfo = currentOrgInfo.getExtraInfo();
            try{
                if(memberExtraInfo.optBoolean("isSuperAdmin")){
                    return true;
                }
            }catch(Exception ex){}
        }
        return false;
    }
    // miscellaneous
    public static void programListOfMember() {

        JSONObject data = _getMemberPrograms();
        render("UIComShare/shareableBatches.html", data);
    }
    public static void programListOfMemberJSON() {

        JSONObject data = _getMemberPrograms();
        renderJSON(data.toString());
    }

    public static void centersOfProgramOfMember(String programId) {

        JSONObject data = new JSONObject();
        try {
            JSONArray programs = _getMemberPrograms().getJSONArray("programs");
            for (int p = 0; p < programs.length(); p++) {
                JSONObject program = programs.getJSONObject(p);
                if (programId.equals(program.getString("id"))) {
                    data.put("centers", program.getJSONArray("centers"));
                    break;
                }
            }
        } catch (Exception e) {
            Logger.log4j.info("Problem in fetching centers of " + programId);
            Logger.log4j.info(e.getMessage());
            data = null;
        }
        render("UIComShare/shareableCenters.html", data);
    }

    private static JSONObject _getMemberPrograms() {

        request.params.put("targetUserId", session.get("userId"));
        JSONObject data = QrPeople._getMemberInfo(null);
        try {
            JSONArray programs = data.getJSONObject("result").getJSONObject("info")
                    .getJSONObject("mappings").getJSONArray("programs");
            data.put("programs", programs);
        } catch (Exception e) {
            data = new JSONObject(new JSONResponse("", "ERROR", "ERROR"));
            Logger.log4j.error(e.getMessage());
        }
        return data;
    }

    protected static void _putAMemberCourseInRequest() {

        request.params.put("targetUserId", session.get("userId"));
        JSONObject data = syncCaller(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/members/getMemberProfile", null);
        try {
            String courseId = null;
            JSONArray programs = data.getJSONObject("result").getJSONObject("info")
                    .getJSONObject("mappings").getJSONArray("programs");
            for (int k = 0; k < programs.length(); k++) {
                JSONArray centers = programs.getJSONObject(k).getJSONArray("centers");
                for (int l = 0; l < centers.length(); l++) {
                    JSONArray sections = centers.getJSONObject(l).getJSONArray("sections");
                    for (int m = 0; m < sections.length(); m++) {
                        JSONArray courses = sections.getJSONObject(m).getJSONArray("courses");
                        if (courses.length() > 0) {
                            courseId = courses.getJSONObject(0).getString("id");
                        }
                    }
                    if (StringUtils.isNotEmpty(courseId)) {
                        break;
                    }
                }
                if (StringUtils.isNotEmpty(courseId)) {
                    break;
                }
            }
            if (StringUtils.isNotEmpty(courseId)) {
                request.params.put("brdIds[0]", courseId);
                request.params.put("courseId", courseId);
            }
        } catch (Exception e) {
            Logger.log4j.error(e.getMessage());
        }
    }

    // commons for resources and library
    public static void visibilityStatus() {
        
        F.Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsLibrary/getVisibilityStatus", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject info = ResponseUtil.checkResponse(getJSON(promise));
        render(info);
    }

    public static JSONObject _getStatus() {

        F.Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsResources/getStatus", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        try {
            JSONObject result = resp.getJSONObject("result");
            JSONArray list = result.getJSONArray("list");
            for (int i = 0; i < list.length(); i++) {
                JSONObject item = list.getJSONObject(i);
                String errorCode = item.getString("errorCode");
                String errorMessage = "";
                if (!StringUtils.isEmpty(errorCode)) {
                    errorMessage = ResponseUtil._getErrorMessage(errorCode);
                }
                item.put("errorMessage", errorMessage);
            }
        } catch (JSONException ex) {
            Logger.log4j.error(ex.getLocalizedMessage());
        }
        return resp;
    }
    public static void getEntityLink(){
        Params params = request.params;
        String entityName = params.get("entity.name");
        String entityId = params.get("entity.id");
        String entityType = params.get("entity.type");
        String type = params.get("entity.entityType");
        render(entityName,entityId,type,entityType);
    }
}