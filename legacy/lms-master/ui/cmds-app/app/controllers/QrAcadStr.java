package controllers;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import play.Logger;
import play.Play;
import play.libs.F;
import play.libs.F.Promise;
import play.mvc.With;
import pojos.UserOrg;
import uicom.util.ClientUtil;
import uicom.util.ResponseUtil;
import util.CacheUtil;

@With(Security.class)
public class QrAcadStr extends AbstractQRUIController {

    public static void main() {

        recordActivity(ClientUtil.ActivityPages.ACADEMIC_STRUCTURE, ClientUtil.ActivityAction.OPEN);
        request.params.put("targetTable", "EDIT_ACAD_STR");
        Map<String, Object> allParams = getReqParams();
        JSONObject depts = _getDepartments(allParams);
        JSONObject centers = _getCenters(allParams);
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(request.params.get("orgId"));
        render(depts, centers, currentOrgInfo);
    }

    public static void instituteInfo() {

        recordActivity(ClientUtil.ActivityPages.ORGANIZATION_INFO, ClientUtil.ActivityAction.OPEN);
        JSONObject orgInfo = _getOrgInfo(null);
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(request.params.get("orgId"));
        render(orgInfo, currentOrgInfo);
    }

    public static void editInstituteInfo() {

        recordActivity(ClientUtil.ActivityPages.ORGANIZATION_INFO, ClientUtil.ActivityAction.EDIT);
        JSONObject orgInfo = _getOrgInfo(null);
        render(orgInfo);
    }

    public static void submitEditInstituteInfo() {

        String isEnc = params.get("isEnc");
        if (isEnc.equals("true")) {
            String encLevel = Play.configuration.getProperty("ORG_ENCRYPTION_LEVEL");
            params.put("encLevel", encLevel);
        } else {
            params.put("encLevel", "NA");
        }
        String authType = params.get("authType");
        if (!StringUtils.isEmpty(authType) && !authType.equals(session.get("orgAuthType"))) {
            session.put("orgAuthType", authType);
            CacheUtil.clearCacheUserOrgs(session.get("userId"));
        }
        F.Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/updateOrganization", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void uploadOrgPic() {

        JSONObject resp = ResponseUtil.checkResponse(uploadUtil(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/uploadOrgPic", null, null));
        try {
            if (StringUtils.isEmpty(resp.getString("errorCode"))) {
                String thumbnail = resp.getJSONObject("result").getString("thumbnail");
                session.put("orgThumbnail", thumbnail);
            }
        } catch (Exception e) {
            Logger.log4j.error("Error in setting org pic in session" + e.getMessage());
        }
        renderJSON(resp.toString());
    }

    public static void getDepts() {

        JSONObject entities = _getDepartments(null);
        JSONObject renderInfo = getRenderInfo("DEPARTMENT", "getProgramsOfDept", "addDept",
                "DEPARTMENT_NOT_FOUND");
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(request.params.get("orgId"));
        render("QrAcadStr/acadEntities.html", entities, renderInfo, currentOrgInfo);
    }

    public static void programsOfDeptOrCourse() {

        String courseId = request.params.get("courseId");
        JSONObject entities;
        if (StringUtils.isEmpty(courseId)) {
            entities = _getProgramsOfDept(null);
        } else {
            entities = _getCoursePrograms(null);
        }
        String astClass = "";
        if (!request.params.get("acadStrContext").equals("ASSIGN_COURSES")) {
            astClass = "getCentersOfProgram";
        } else {
            astClass = "getCoursesOfProgram";
        }
        JSONObject renderInfo = getRenderInfo("PROGRAM", astClass, "addProgram",
                "PROGRAM_NOT_FOUND");
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(request.params.get("orgId"));
        render("QrAcadStr/acadEntities.html", entities, renderInfo, currentOrgInfo);
    }

    public static void centersOfProgram() {

        JSONObject entities = _getProgramCenters(null);
        String addType = "MAP";

        JSONObject renderInfo = getRenderInfo("CENTER", "getSectionsOfCenter",
                "addProgramToCenter", "PROGRAM_CENTER_NOT_FOUND");
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(request.params.get("orgId"));
        render("QrAcadStr/acadEntities.html", entities, renderInfo, currentOrgInfo, addType);
    }

    public static void coursesOfProgram() {

        JSONObject entities = _getProgramCourses(null);
        String addType = "MAP";
        JSONObject renderInfo = getRenderInfo("COURSE", "", "addCoursesToProgram",
                "PROGRAM_COURSE_NOT_FOUND");
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(request.params.get("orgId"));
        render("QrAcadStr/acadEntities.html", entities, renderInfo, currentOrgInfo, addType);

    }

    public static void sectionsOfProgCenter() {

        JSONObject entities = _getSections(null);
        String astClass = "";
        if (request.params.get("targetTable").equals("CHANGE_PROGRAM")) {
            astClass = "selectSectionForProgChange";
        }
        String addType = "";

        JSONObject renderInfo = getRenderInfo("SECTION", astClass, "addSection",
                "SECTION_NOT_FOUND");
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(request.params.get("orgId"));
        render("QrAcadStr/acadEntities.html", entities, renderInfo, currentOrgInfo, addType);
    }

    public static void addProgramToCenter() {

        JSONObject centers = _getCenters(null);
        render(centers);
    }

    public static void addCoursesToProgram() {

        JSONObject courses = UIComBoards._getOrgBoards(null);
        render(courses);
    }

    public static void addProgramCourses() {

        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/addProgramCourses", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void removeProgramCourses() {

        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/removeProgramCourses", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void addDepartment() {

        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/addDepartment", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void editDepartment() {

        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/updateDepartment", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void removeDepartment() {

        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/removeDepartment", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void reactivateDepartment() {

        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/activateDepartment", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void addProgram() {

        recordActivity(ClientUtil.ActivityPages.PROGRAM, ClientUtil.ActivityAction.ADD);
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/addProgram", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void editProgram() {

        recordActivity(ClientUtil.ActivityPages.PROGRAM, ClientUtil.ActivityAction.EDIT);
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/updateProgram", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void reactivateProgram() {

        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/activateProgram", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void removeProgram() {

        recordActivity(ClientUtil.ActivityPages.PROGRAM, ClientUtil.ActivityAction.DELETE);
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/removeProgram", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void addProgramCenters() {

        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/addProgramCenters", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void removeProgramCenters() {

        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/removeProgramCenters", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void addCenter() {

        recordActivity(ClientUtil.ActivityPages.CENTER, ClientUtil.ActivityAction.ADD);
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/addCenter", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void editCenter() {

        recordActivity(ClientUtil.ActivityPages.CENTER, ClientUtil.ActivityAction.EDIT);
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/updateCenter", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void removeCenter() {

        recordActivity(ClientUtil.ActivityPages.CENTER, ClientUtil.ActivityAction.DELETE);
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/removeCenter", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void reactivateCenter() {

        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/activateCenter", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void addSection() {

        recordActivity(ClientUtil.ActivityPages.SECTION, ClientUtil.ActivityAction.ADD);
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/addSection", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void editSection() {

        recordActivity(ClientUtil.ActivityPages.SECTION, ClientUtil.ActivityAction.EDIT);
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/updateSection", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void removeSection() {

        recordActivity(ClientUtil.ActivityPages.SECTION, ClientUtil.ActivityAction.EDIT);
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/removeSection", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void reactivateSection() {

        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/activateSection", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void getProgramCourses() {

        JSONObject resp = _getProgramCourses(null);
        renderJSON(resp.toString());
    }

    public static void pointsOfSale() {
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/getOrgPointsOfSale", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    // return
    protected static JSONObject _getUserOrgs(Map<String, Object> allParams) {

        F.Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/getAssociatedOrgsOfUser",
                allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }

    protected static JSONObject _getDepartments(Map<String, Object> allParams) {

        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/getDepartments", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }

    protected static JSONObject _getProgramsOfDept(Map<String, Object> allParams) {

        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/getPrograms", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }

    protected static JSONObject _getCoursePrograms(Map<String, Object> allParams) {

        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/getCoursePrograms", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }

    protected static JSONObject _getCenters(Map<String, Object> allParams) {

        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/getCenters", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }

    protected static JSONObject _getProgramCenters(Map<String, Object> allParams) {

        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/getProgramCenters", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }

    protected static JSONObject _getSections(Map<String, Object> allParams) {

        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/getSections", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }

    protected static JSONObject _getProgramCourses(Map<String, Object> allParams) {

        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/getProgramCourses", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
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

    // utils
    private static JSONObject getRenderInfo(String acadEntityType, String astClass,
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

    // direct
    public static void acadStrDirect(String orgId) {

        recordActivity(ClientUtil.ActivityPages.ACADEMIC_STRUCTURE, ClientUtil.ActivityAction.OPEN);
        request.params.put("start", "0");
        request.params.put("size", "200");
        request.params.put("targetTable", "EDIT_ACAD_STR");
        Map<String, Object> allParams = getReqParams();
        JSONObject depts = _getDepartments(allParams);
        JSONObject centers = _getCenters(allParams);
        String includeName = "QrAcadStr/main.html";
        flash.put("ENTRY", "DIRECT");
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(request.params.get("orgId"));
        render("Application/mapper.html", includeName, depts, centers, currentOrgInfo);
    }

    public static void instituteInfoDirect(String orgId) {

        recordActivity(ClientUtil.ActivityPages.ORGANIZATION_INFO, ClientUtil.ActivityAction.OPEN);
        JSONObject orgInfo = _getOrgInfo(null);
        String includeName = "QrAcadStr/instituteInfo.html";

        flash.put("ENTRY", "DIRECT");
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(request.params.get("orgId"));
        render("Application/mapper.html", includeName, orgInfo, currentOrgInfo);
    }
}
