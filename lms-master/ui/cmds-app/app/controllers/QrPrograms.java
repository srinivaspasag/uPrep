/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package controllers;

import static controllers.QrAcadStr._getProgramsOfDept;
import static controllers.QrPeople._getMembers;
import static controllers.QrPeople._getOrgsOfProg;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.libs.F;
import play.mvc.Scope;
import play.mvc.With;
import pojos.UserOrg;
import uicom.util.ClientUtil;
import uicom.util.ResponseUtil;

/**
 *
 * @author ajith
 */
@With(Security.class)
public class QrPrograms extends AbstractQRUIController {

    public static void program() {

        recordActivity(ClientUtil.ActivityPages.PROGAM_LIBRARY, ClientUtil.ActivityAction.OPEN);
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(request.params.get("orgId"));
        render(currentOrgInfo);
    }

    public static void programContent() {

        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(request.params.get("orgId"));
        programReqParams();
        if (!StringUtils.isEmpty(request.params.get("courseId"))
                && StringUtils.isEmpty(request.params.get("brdIds[0]"))) {
            request.params.put("brdIds[0]", request.params.get("courseId"));
        }
        // if (StringUtils.isEmpty(request.params.get("courseId"))) {
        // Widgets._putAMemberCourseInRequest();
        // }
        JSONObject resources = _checkTypeAndGetResources();
        render(resources, currentOrgInfo);
    }


    public static JSONObject reprocessProgram() {

        programReqParams();
        if (!StringUtils.isEmpty(request.params.get("courseId"))
                && StringUtils.isEmpty(request.params.get("brdIds[0]"))) {
            request.params.put("brdIds[0]", request.params.get("courseId"));
        }
        JSONObject resources = _checkTypeAndGetResourcesForReprocessing();
        JSONObject moduleInfo = new JSONObject();
        try{
            for (int i = 0; i < resources.length(); i++) {
                String id = resources.getString("module "+i);

                Map<String, Object> allParams = getReqParams();
                allParams.put("id",id);

                F.Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(ClientUtil.CMDS_SERVICE_URL + "/cmdsModules/getCMDSModuleInfo", allParams);
                Logger.log4j.info("BEFORE AWAIT");
                await(promise);
                Logger.log4j.info("AFTER AWAIT");

                JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
                JSONObject result = resp.getJSONObject("result");
                JSONArray ListArray = result.getJSONArray("children");
                for (int j = 0; j < ListArray.length(); j++) {
                    JSONObject entity = ListArray.getJSONObject(j).optJSONObject("entity");
                    if (entity != null) {
                        String type = entity.getString("type");
                        if (type.equals("CMDSVIDEO")){
                            String videoid = entity.getString("id");
                            moduleInfo.put("video "+j,videoid);
                            Map<String, Object> allParams1 = new HashMap<String, Object>();
                            F.Promise<AbstractQRUIController.JSONResponseWrapper> promise1 = client(ClientUtil.CMDS_SERVICE_URL + "/cmdsVideos/reprocess/"+videoid, allParams1);
                            Logger.log4j.info("BEFORE AWAIT");
                            await(promise1);
                            Logger.log4j.info("AFTER AWAIT");
                        }
                    }
               }
            }
        }catch (JSONException ex) {
            Logger.log4j.error(ex.getLocalizedMessage());
        }

        return moduleInfo;
    }
    public static void libraryTable() {

        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(request.params.get("orgId"));
        makeOrgEntity();
        if (!StringUtils.equals(request.params.get("includes"), "CMDSQUESTION")) {
            request.params.put("excludes", "CMDSQUESTION");
            Map<String, Object> allParams = getReqParams();
            JSONObject resources = _getLibraryResources(allParams);
            putQuestionsHits(resources, allParams);
            String sectionId = request.params.get("sectionId");
            if (StringUtils.isNotEmpty(sectionId)) {
                render("QrPrograms/sectionLibraryTable.html", resources, currentOrgInfo);
            } else {
                render(resources,currentOrgInfo);
            }
        } else {
            if (StringUtils.isNotEmpty(request.params.get("sectionId"))) {
                request.params.put("needCBox", "true");
            }
            JSONObject questions = _getLibraryResources(null);
            render("QrQuestions/qrQuesns.html", questions);
        }
    }

    public static void programMembers() {

        if (StringUtils.isEmpty(request.params.get("targetProfile"))) {
            request.params.put("targetProfile", "TEACHER");
        }
        programReqParams();
        JSONObject people = _getMembers(null);
        boolean canImpersonate = QrPeople._canImpersonate();
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(request.params.get("orgId"));
        render("QrPeople/people.html", people, canImpersonate, currentOrgInfo);
    }

    // Getting all the organizations that share this programId and orgId
    public static void programOrganizations(String programId, String orgId) {

        if (StringUtils.isEmpty(request.params.get("targetProfile"))) {
            request.params.put("targetProfile", "ORGANIZATION");
        }

        request.params.put("providerOrgId", orgId);

        programReqParams();
        // Get all organizations that are given access to this programId by this orgId
        JSONObject people = _getOrgsOfProg(null);
        boolean canImpersonate = QrPeople._canImpersonate();
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(request.params.get("orgId"));
        render("QrPeople/people.html", people, canImpersonate, currentOrgInfo);
    }

    public static void removeProgramSharing() {
        Map<String, Object> allParams = getReqParams();
        F.Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/removeSharedProgramFromOrg", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void programStudents(String programId, String orgId) {

        request.params.put("targetProfile", "STUDENT");
        programReqParams();
        JSONObject people = _getMembers(null);
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(request.params.get("orgId"));
        render("QrPeople/people.html", people, currentOrgInfo);
    }

    public static void changeProgPageProgram() {

        JSONObject programs = _getProgramsOfDept(null);
        JSONObject renderInfo = getRenderInfo("PROGRAM", "getCentersOfProgram", "",
                "PROGRAM_NOT_FOUND");
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(request.params.get("orgId"));
        render(programs, renderInfo, currentOrgInfo);
    }

    public static JSONObject getRenderInfo(String acadEntityType, String astClass,
            String addClass, String notFoundMessage) {

        JSONObject renderInfo = new JSONObject();
        try {
            renderInfo.put("entityType", acadEntityType);
            renderInfo.put("astClass", astClass);
            renderInfo.put("addClass", addClass);
            renderInfo.put("notFoundMessage", notFoundMessage);
        } catch (Exception e) {
            Logger.log4j.error("Error in setting render info.");
            return null;
        }
        return renderInfo;
    }

    // make visible different entities
    public static void makeVisible() {

        recordActivity(ClientUtil.ActivityPages.PROGAM_LIBRARY,
                ClientUtil.ActivityAction.MAKE_VISIBLE);
        JSONObject resp = _makeVisible(null);
        renderJSON(resp.toString());
    }

    public static void publish() {

        recordActivity(ClientUtil.ActivityPages.PROGAM_LIBRARY, ClientUtil.ActivityAction.PUBLISH);
        F.Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsResources/publish", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        try {
            JSONObject result = resp.getJSONObject("result");
            JSONObject info = result.getJSONObject("info");
            Iterator<String> itr = info.keys();
            while (itr.hasNext()) {
                String key = itr.next();
                JSONObject item = info.getJSONObject(key);
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
        renderJSON(resp.toString());
    }

    public static void getPublishStatus() {

        JSONObject resp = Widgets._getStatus();
        renderJSON(resp.toString());
    }

    // upload offline tests
    public static void uploadMarkSheets() {

        JSONObject resp = uploadUtil(ClientUtil.CMDS_SERVICE_URL + "/cmdsTests/uploadTestResults",
                null, null);
        recordActivity(ClientUtil.ActivityPages.MARKS_SHEET, ClientUtil.ActivityAction.UPLOAD);
        renderJSON(resp.toString());
    }

    public static void uploadMarkSheetsStatus() {

        JSONObject resp = Widgets._getStatus();
        renderJSON(resp.toString());
    }

    public static void offlineTests() {

        recordActivity(ClientUtil.ActivityPages.TEST, ClientUtil.ActivityAction.OPEN);
        JSONObject tests = _getOfflineTests(null);
        render(tests);
    }

    public static void offlineTestsTable() {

        JSONObject tests = _getOfflineTests(null);
        render(tests);
    }

    public static void studentPaymentInfo() {
        Map<String, Object> allParams = getReqParams();
        JSONObject memberInfo = QrPeople._getMemberInfo(allParams);
        JSONObject orderInfo = null;
        String orderId = null;
        try {
            if (StringUtils.isEmpty(memberInfo.getString("errorCode"))) {
                JSONArray programs = memberInfo.getJSONObject("result")
                        .getJSONObject("info").getJSONObject("mappings")
                        .getJSONArray("programs");
                boolean foundOrderId = false;
                for (int p = 0; p < programs.length(); p++) {
                    JSONArray centers = programs.getJSONObject(p).getJSONArray("centers");
                    for (int c = 0; c < centers.length(); c++) {
                        JSONArray sections = centers.getJSONObject(c).getJSONArray("sections");
                        for (int s = 0; s < sections.length(); s++) {
                            JSONObject section = sections.getJSONObject(s);
                            if (StringUtils.equals(section.getString("id"), request.params.get("sectionId"))
                                    && StringUtils.isNotEmpty(section.getString("orderId"))) {
//                                 orderId =  section.getString("orderId");
                                orderId = (section.get("orderId").equals(null) ? null : (String) section.get("orderId"));
                                foundOrderId = true;
                                break;
                            }
                        }
                        if (foundOrderId) {
                            break;
                        }
                    }
                    if (foundOrderId) {
                        break;
                    }
                }
                if (StringUtils.isNotEmpty(orderId)) {
                    allParams.put("orderId", orderId);
                    orderInfo = UIComInvoices._getOrder(allParams);
                }
            }
        } catch (JSONException e) {
            Logger.log4j.error(e.getMessage());
        }
        render(orderInfo);
    }

    private static JSONObject _getOfflineTests(Map<String, Object> allParams) {

        F.Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CONTENT_SERVICE_URL + "/tests/getTests", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject tests = ResponseUtil.checkResponse(getJSON(promise));
        return tests;
    }

    // return
    protected static JSONObject _getLibraryResources(Map<String, Object> allParams) {

        if (allParams == null) {
            allParams = getReqParams();
        }
        String orderBy = allParams.get("orderBy").toString();
        if (StringUtils.equals(orderBy, "[name.untouched]") || StringUtils.equals(orderBy, "[customOrder]")) {
            allParams.put("sortOrder", "ASC");
        }

        F.Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsLibrary/getLibraryResources", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }

    protected static JSONObject _getLibraryResourcesForReprocessing(Map<String, Object> allParams) {

        if (allParams == null) {
            allParams = getReqParams();
        }
        String orderBy = allParams.get("orderBy").toString();
        if (StringUtils.equals(orderBy, "[name.untouched]") || StringUtils.equals(orderBy, "[customOrder]")) {
            allParams.put("sortOrder", "ASC");
        }

        F.Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsLibrary/getLibraryResources", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        JSONObject listofids = new JSONObject();
        try {
            JSONObject result = resp.getJSONObject("result");
            JSONArray ListArray = result.getJSONArray("list");
            for (int i = 0; i < ListArray.length(); i++) {
                JSONObject source = ListArray.getJSONObject(i).getJSONObject("source");
                String type = source.getString("type");
                if (type.equals("CMDSMODULE")){
                    String id = source.getString("id");
                    listofids.put("module "+i,id);
                }
            }
        } catch (JSONException e) {
            Logger.log4j.error(e.getLocalizedMessage());
        }
        return listofids;
    }

    protected static JSONObject _makeVisible(Map<String, Object> allParams) {

        F.Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsLibrary/makeVisible", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        resp = ResponseUtil.checkResponse(resp);
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

    // direct
    public static void programDirect(String programId, String orgId) {

        recordActivity(ClientUtil.ActivityPages.PROGAM_LIBRARY, ClientUtil.ActivityAction.OPEN);
        String includeName = "QrPrograms/program.html";
        flash.put("ENTRY", "DIRECT");
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        render("Application/mapper.html", includeName, currentOrgInfo);
    }

    private static void programReqParams() {

        Scope.Params reqParams = request.params;
        if (StringUtils.isEmpty(reqParams.get("start"))) {
            request.params.put("start", "0");
        }
        if (StringUtils.isEmpty(reqParams.get("orderBy"))) {
            request.params.put("orderBy", "timeCreated");
        }

        if (StringUtils.isEmpty(reqParams.get("size"))) {
            request.params.put("size", "50");
        }
        if (!StringUtils.equals(reqParams.get("includes"), "CMDSQUESTION")) {
            request.params.put("excludes", "CMDSQUESTION");
        } else {
            if (StringUtils.isNotEmpty(request.params.get("sectionId"))) {
                request.params.put("needCBox", "true");
            }
        }
        makeOrgEntity();
    }

    private static void makeOrgEntity() {

        String programId = request.params.get("programId");
        String centerId = request.params.get("centerId");
        String sectionId = request.params.get("sectionId");
        if (StringUtils.isNotEmpty(sectionId)) {
            request.params.put("orgEntity.id", sectionId);
            request.params.put("orgEntity.type", "SECTION");
            return;
        }
        if (StringUtils.isNotEmpty(programId)) {
            request.params.put("orgEntity.id", programId);
            request.params.put("orgEntity.type", "PROGRAM");
            if (StringUtils.isNotEmpty(centerId)) {
                request.params.put("orgEntity.centers[0].id", centerId);
                request.params.put("orgEntity.centers[0].type", "CENTER");
            }
        }
    }

    private static JSONObject _checkTypeAndGetResources() {

        Map<String, Object> allParams = getReqParams();
        JSONObject resources = _getLibraryResources(allParams);
        if (!StringUtils.equals(request.params.get("includes"), "CMDSQUESTION")) {
            putQuestionsHits(resources, allParams);
        }
        return resources;
    }

    private static JSONObject _checkTypeAndGetResourcesForReprocessing() {

        Map<String, Object> allParams = getReqParams();
        JSONObject resources = _getLibraryResourcesForReprocessing(allParams);
        if (!StringUtils.equals(request.params.get("includes"), "CMDSQUESTION")) {
            putQuestionsHits(resources, allParams);
        }
        return resources;
    }

    private static JSONObject putQuestionsHits(JSONObject resp, Map<String, Object> allParams) {

        try {
            if (StringUtils.isEmpty(resp.getString("errorCode"))) {
                allParams.remove("excludes");
                allParams.put("includes", "CMDSQUESTION");
                allParams.put("size", "1");
                Logger.log4j
                        .error(":::::::::::::::::::::::Library Resources not found,fetching question count.");
                JSONObject resources = _getLibraryResources(allParams);
                int totalQuestions = resources.getJSONObject("result").getInt("totalHits");
                JSONObject respResult = resp.getJSONObject("result");
                respResult.put("totalQuestions", totalQuestions);
            }
        } catch (Exception e) {
            Logger.log4j.error("Error in fetching questions hits" + e.getMessage());
        }
        return resp;
    }

    public static void removeFromLibrary() {

        recordActivity(ClientUtil.ActivityPages.PROGAM_LIBRARY, ClientUtil.ActivityAction.DELETE);
        F.Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsLibrary/removeFromLibrary", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }
    public static void reorderContent(){
        recordActivity(ClientUtil.ActivityPages.PROGAM_LIBRARY, ClientUtil.ActivityAction.REORDER);
        F.Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsLibrary/move", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }
}
