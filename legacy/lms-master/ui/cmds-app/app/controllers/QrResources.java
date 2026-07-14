/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package controllers;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.cache.Cache;
import play.libs.F;
import play.mvc.Scope.Params;
import play.mvc.With;
import pojos.UserOrg;
import uicom.response.JSONResponse;
import uicom.util.ClientUtil;
import uicom.util.ClientUtil.ActivityPages;
import uicom.util.ResponseUtil;

/**
 *
 * @author ajith
 */
@With(Security.class)
public class QrResources extends AbstractQRUIController {

    public static void resources() {
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(request.params.get("orgId"));
        recordActivity(ClientUtil.ActivityPages.RESOURCE, ClientUtil.ActivityAction.OPEN);
        checkAndSetFolderId();
        putReqParamsForResources();
        JSONObject resources = _checkTypeAndGetResources();
        render("QrResources/folderPage.html", resources, currentOrgInfo);
    }

    public static void folderPage() {

        recordActivity(ClientUtil.ActivityPages.FOLDER, ClientUtil.ActivityAction.OPEN);
        putReqParamsForResources();
        JSONObject resources = _checkTypeAndGetResources();
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(request.params.get("orgId"));
        render(resources, currentOrgInfo);
    }

    public static void resourcesTable() {
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(request.params.get("orgId"));
        if (!StringUtils.equals(request.params.get("includes"), "CMDSQUESTION")) {
            request.params.put("excludes", "CMDSQUESTION");
            Map<String, Object> allParams = getReqParams();
            JSONObject resources = _getResources(allParams);
            putQuestionsHits(resources, allParams);
            render(resources,currentOrgInfo);
        } else {
            request.params.put("needCBox", "true");
            JSONObject questions = _getQuestions(null);
            render("QrQuestions/qrQuesns.html", questions);
        }
    }

    public static void popupResources() {
        checkAndSetFolderId();
        putReqParamsForResources();
        Map<String, Object> allParams = getReqParams();
        if (!allParams.containsKey("includes")) {
            allParams.put("includes[0]", "CMDSDOCUMENT");
            allParams.put("includes[1]", "CMDSTEST");
            allParams.put("includes[2]", "CMDSASSIGNMENT");
            allParams.put("includes[3]", "CMDSVIDEO");
            allParams.put("includes[4]", "CMDSFILE");
            allParams.put("includes[5]", "FOLDER");
            allParams.remove("excludes");
        }
        JSONObject resources = _getResources(allParams);
        render(resources);
    }

    public static void popupResourcesTable() {
        checkAndSetFolderId();
        Map<String, Object> allParams = getReqParams();
        if (!allParams.containsKey("includes")) {
            allParams.put("includes[0]", "CMDSDOCUMENT");
            allParams.put("includes[1]", "CMDSTEST");
            allParams.put("includes[2]", "CMDSASSIGNMENT");
            allParams.put("includes[3]", "CMDSVIDEO");
            allParams.put("includes[4]", "CMDSFILE");
            allParams.put("includes[5]", "FOLDER");
            allParams.remove("excludes");
        }
        JSONObject resources = _getResources(allParams);
        render(resources);
    }

    public static void getFolders() {

        JSONObject resp = _getFolders(null);
        renderJSON(resp.toString());
    }

    public static void fetchRootFolderId() {

        String folderId = _getRootFolderId(request.params.get("orgId"));
        String errorCode = "";
        if (StringUtils.isEmpty(folderId)) {
            errorCode = "There is some problem in fetching organization details."
                    + "<br>Refresh the page and try again.";
        }
        JSONObject resp = new JSONObject(new JSONResponse(folderId, errorCode, errorCode));
        renderJSON(resp.toString());
    }

    public static void moveToFolders() {

        recordActivity(ClientUtil.ActivityPages.RESOURCE, ClientUtil.ActivityAction.MOVE);
        JSONObject folders = _getFolders(null);
        render(folders);
    }

    public static void moveToFoldersSubmit() {

        F.Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsResources/move", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void deleteResources() {

        recordActivity(ClientUtil.ActivityPages.RESOURCE, ClientUtil.ActivityAction.DELETE);
        F.Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsResources/removeResources", null);
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
                if (item.get("errorCode") != null && !"null".equals(errorCode)
                        && !StringUtils.isEmpty(errorCode)) {
                    errorMessage = ResponseUtil._getErrorMessage(errorCode);
                }
                item.put("errorMessage", errorMessage);
            }
        } catch (JSONException ex) {
            Logger.log4j.error(ex.getLocalizedMessage());
        }
        renderJSON(resp.toString());
    }

    public static void addToLibrary() {

        recordActivity(ClientUtil.ActivityPages.RESOURCE, ClientUtil.ActivityAction.ADD);
        F.Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsLibrary/addToLibrary", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void createFolder() {

        recordActivity(ClientUtil.ActivityPages.FOLDER, ClientUtil.ActivityAction.ADD);
        F.Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsResources/createFolder", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void updateTitleAndDesc() {

        // very bad patch, to be redone
        String entityType = request.params.get("entity.type");
        if (entityType.equals("CMDSQUESTIONSET")) {
            entityType = "QUESTION_SET";
        } else if (!entityType.equals("FOLDER")) {
            entityType = entityType.substring(4);
        }
        ActivityPages type = ActivityPages.valueOf(entityType);
        recordActivity(type, ClientUtil.ActivityAction.EDIT);
        String urlClass = request.params.get("urlClass");
        F.Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/" + urlClass + "/update", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    protected static JSONObject _getFolders(Map<String, Object> allParams) {

        F.Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsResources/getFolders", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }

    protected static JSONObject _getResources(Map<String, Object> allParams) {

        if (allParams == null) {
            allParams = getReqParams();
        }
        String orderBy = allParams.get("orderBy").toString();
        if (StringUtils.equals(orderBy, "[name.untouched]") || StringUtils.equals(orderBy, "[customOrder]")) {
            allParams.put("sortOrder", "ASC");
        }
        F.Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsResources/getResources", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }

    protected static JSONObject _getQuestionsCount(Map<String, Object> allParams){
        if (allParams == null) {
            allParams = getReqParams();
        }
        String orderBy = allParams.get("orderBy").toString();
        if (StringUtils.equals(orderBy, "[name.untouched]") || StringUtils.equals(orderBy, "[customOrder]")) {
            allParams.put("sortOrder", "ASC");
        }
        F.Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsResources/getQuestionsCount", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }

    protected static JSONObject _getQuestions(Map<String, Object> allParams){
        if (allParams == null) {
            allParams = getReqParams();
        }
        String orderBy = allParams.get("orderBy").toString();
        if (StringUtils.equals(orderBy, "[name.untouched]") || StringUtils.equals(orderBy, "[customOrder]")) {
            allParams.put("sortOrder", "ASC");
        }
        F.Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsResources/getQuestions", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }

    public static void resourcesDirect(String orgId) {

        recordActivity(ClientUtil.ActivityPages.RESOURCE, ClientUtil.ActivityAction.OPEN);
        Widgets.setOrgRequestParams(orgId);
        checkAndSetFolderId();
        putReqParamsForResources();
        JSONObject resources = _checkTypeAndGetResources();
        String includeName = "QrResources/main.html";
        flash.put("ENTRY", "DIRECT");
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        render("Application/mapper.html", includeName, resources, currentOrgInfo);
    }

    public static void folderDirect(String folderId, String orgId) {

        recordActivity(ClientUtil.ActivityPages.FOLDER, ClientUtil.ActivityAction.OPEN);
        Widgets.setOrgRequestParams(orgId);
        putReqParamsForResources();
        request.params.put("folderId", folderId);
        JSONObject resources = _checkTypeAndGetResources();
        String includeName = "QrResources/folderPage.html";
        flash.put("ENTRY", "DIRECT");
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        render("Application/mapper.html", includeName, resources, currentOrgInfo);
    }

    private static JSONObject _checkTypeAndGetResources() {

        Map<String, Object> allParams = getReqParams();
        JSONObject resources = new JSONObject();
        if (!StringUtils.equals(request.params.get("includes"), "CMDSQUESTION")) {
            resources = _getResources(allParams);
            putQuestionsHits(resources, allParams);
        }else{
            if(!allParams.containsKey("quesType"))
                allParams.put("quesType", "NOT_PARA");
            resources = _getQuestions(allParams);
        }
        return resources;
    }

    private static void putReqParamsForResources() {

        Params reqParams = request.params;
        if (StringUtils.isEmpty(reqParams.get("start"))) {
            request.params.put("start", "0");
        }
        if (StringUtils.isEmpty(reqParams.get("size"))) {
            request.params.put("size", "50");
        }
        if (StringUtils.isEmpty(reqParams.get("orderBy"))) {
            request.params.put("orderBy", "timeCreated");
        }
        if (!StringUtils.equals(reqParams.get("includes"), "CMDSQUESTION")) {
            request.params.put("excludes", "CMDSQUESTION");
        } else {
            request.params.put("needCBox", "true");
        }
        // if (StringUtils.isEmpty(reqParams.get("courseId"))) {
        // Widgets._putAMemberCourseInRequest();
        // }else if(!StringUtils.isEmpty(reqParams.get("courseId"))&&
        // StringUtils.isEmpty(reqParams.get("brdIds[0]"))){
        // request.params.put("brdIds[0]", reqParams.get("courseId"));
        // }
        if (!StringUtils.isEmpty(reqParams.get("courseId"))
                && StringUtils.isEmpty(reqParams.get("brdIds[0]"))) {
            request.params.put("brdIds[0]", reqParams.get("courseId"));
        }
    }

    private static JSONObject putQuestionsHits(JSONObject resp, Map<String, Object> allParams) {

        try {
            if (StringUtils.isEmpty(resp.getString("errorCode"))) {
                allParams.remove("excludes");
                allParams.put("includes", "CMDSQUESTION");
                allParams.put("size", "1");
                Logger.log4j.error("::::::::::::::::::::::: Fetching question count.");
                JSONObject resources = _getQuestionsCount(allParams);
                int totalQuestions = resources.getJSONObject("result").getInt("totalHits");
                int totalParagraphs = resources.getJSONObject("result").getInt("paraHits");
                JSONObject respResult = resp.getJSONObject("result");
                respResult.put("totalQuestions", totalQuestions);
                respResult.put("totalParagraphs", totalParagraphs);
            }
        } catch (Exception e) {
            Logger.log4j.error("Error in fetching questions hits" + e.getMessage());
        }
        return resp;
    }

    protected static void checkAndSetFolderId() {

        String folderId = request.params.get("folderId");
        if (StringUtils.isEmpty(folderId)) {
            folderId = _getRootFolderId(request.params.get("orgId"));
            if (StringUtils.isEmpty(folderId)) {
                UIComSecurity._logout();
                Application.errorInRootFolder();
            } else {
                request.params.put("folderId", folderId);
            }
        }
    }

    protected static String _getRootFolderId(String orgId) {

        if (StringUtils.isEmpty(orgId)) {
            return null;
        }
        String key = "ORG_ROOT_FOLDER_" + orgId;
        String rootFolderId = Cache.get(key, String.class);
        try {
            if (rootFolderId == null) {
                Logger.log4j.info("no root folderId found...fetching them.");
                JSONObject folderList = syncCaller(ClientUtil.CMDS_SERVICE_URL
                        + "/cmdsResources/getFolders", null);
                rootFolderId = folderList.getJSONObject("result").getJSONArray("list")
                        .getJSONObject(0).getString("id");
            }
            Cache.set(key, rootFolderId);
        } catch (Exception e) {
            Logger.log4j.error(e.getMessage());
        }
        return rootFolderId;
    }
}
