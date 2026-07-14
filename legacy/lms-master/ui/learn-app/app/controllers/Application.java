package controllers;

import play.*;
import play.libs.F;
import play.libs.F.Promise;
import util.ResponseUtil;
import util.ClientUtil;

import java.util.*;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class Application extends AbstractUIController {

    static final String className = Application.class.getSimpleName();

    public static void newIndex() {
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate, no-store");
        response.setHeader("Expires", "0");
        if (StringUtils.isNotEmpty(session.get("userId"))) {
            Logger.log4j.info("The user is already logged in.");
            redirect("/home");
            return;
        }
        JSONObject categoriesResponse = getDynamicCategories();
        String activeClass = "students";
//        String activeClass = "home";
        render(renderTheme(getHTMLFilePath(className)),activeClass,categoriesResponse);
    }

    public static void contactus() {
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate, no-store");
        response.setHeader("Expires", "0");
        if (StringUtils.isNotEmpty(session.get("userId"))) {
            Logger.log4j.info("The user is already logged in.");
            redirect("/home");
            return;
        }
        JSONObject categoriesResponse = getDynamicCategories();
        render(renderTheme(getHTMLFilePath(className)),categoriesResponse);
    }

    public static void institutes() {
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate, no-store");
        response.setHeader("Expires", "0");
        if (StringUtils.isNotEmpty(session.get("userId"))) {
            Logger.log4j.info("The user is already logged in.");
            redirect("/home");
            return;
        }
        JSONObject categoriesResponse = getDynamicCategories();
        String activeClass = "home";
//        String activeClass = "institutes";
        render(renderTheme(getHTMLFilePath(className)),categoriesResponse, activeClass);
    }

    public static void pricing() {
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate, no-store");
        response.setHeader("Expires", "0");
        if (StringUtils.isNotEmpty(session.get("userId"))) {
            Logger.log4j.info("The user is already logged in.");
            redirect("/home");
            return;
        }
        JSONObject categoriesResponse = getDynamicCategories();
        String activeClass = "pricing";
        render(renderTheme(getHTMLFilePath(className)),categoriesResponse, activeClass);
    }

    public static void faqs() {
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate, no-store");
        response.setHeader("Expires", "0");
        if (StringUtils.isNotEmpty(session.get("userId"))) {
            Logger.log4j.info("The user is already logged in.");
            redirect("/home");
            return;
        }
        JSONObject categoriesResponse = getDynamicCategories();
        render(renderTheme(getHTMLFilePath(className)),categoriesResponse);
    }

    public static void aboutus() {
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate, no-store");
        response.setHeader("Expires", "0");
        if (StringUtils.isNotEmpty(session.get("userId"))) {
            Logger.log4j.info("The user is already logged in.");
            redirect("/home");
            return;
        }
        JSONObject categoriesResponse = getDynamicCategories();
        render(renderTheme(getHTMLFilePath(className)),categoriesResponse);
    }

    public static void blog(){
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate, no-store");
        response.setHeader("Expires", "0");
        render(renderTheme(getHTMLFilePath(className)));
    }

    public static void stopWelcomeMessage(){
        session.put("showWelcomeMessage", false);
        renderJSON(true);
    }

    public static void ping(){
        renderJSON(true);
    }

    public static void franchisee() {
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate, no-store");
        response.setHeader("Expires", "0");
        if (StringUtils.isNotEmpty(session.get("userId"))) {
            Logger.log4j.info("The user is already logged in.");
            redirect("/home");
            return;
        }
        JSONObject categoriesResponse = getDynamicCategories();
        render(renderTheme(getHTMLFilePath(className)),categoriesResponse);
    }

    public static void privacyPolicy() {
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate, no-store");
        response.setHeader("Expires", "0");
        if (StringUtils.isNotEmpty(session.get("userId"))) {
            Logger.log4j.info("The user is already logged in.");
            redirect("/home");
            return;
        }
        JSONObject categoriesResponse = getDynamicCategories();
        render(renderTheme(getHTMLFilePath(className)),categoriesResponse);
    }

    public static JSONObject _getAdditionalSignupFields(){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/getOrgMemberExtraInputFields", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }

    public static void signup() {
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate, no-store");
        response.setHeader("Expires", "0");
        if (StringUtils.isNotEmpty(session.get("userId"))) {
            Logger.log4j.info("The user is already logged in.");
            redirect("/home");
            return;
        }
        JSONObject categoriesResponse = getDynamicCategories();
        String activeClass= "signup";
        request.params.put("targetOrgMemberProfile", "STUDENT");
        request.params.put("checkIfSignupAllowed", "true");
        request.params.put("orgId",play.Play.configuration.getProperty("learnpedia.id"));
        JSONObject resp = _getAdditionalSignupFields();
        render(renderTheme(getHTMLFilePath(className)),activeClass,categoriesResponse,resp);
    }

    public static void uploadImg() {
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        Logger.log4j.info("File Upload REQ Came =============================== ");
        String UPLOAD_PATH = getUploadPath();
        File inputDoc = new File(UPLOAD_PATH + File.separator + request.params.get("qqfile"));
        JSONObject data = uploadUtil(ClientUtil.CONTENT_SERVICE_URL
                + "/uploads/uploadImage", null, inputDoc);
        renderJSON(data.toString());
    }

    public static void uploadProfilePic() {
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        Logger.log4j.info("File Upload REQ Came to upload profile pic =============================== ");
        String UPLOAD_PATH = getUploadPath();
        File inputDoc = new File(UPLOAD_PATH + File.separator + request.params.get("qqfile"));
        JSONObject resp = ResponseUtil.checkResponse(uploadUtil(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/members/uploadProfilePic", null, inputDoc));
        try {
            if (StringUtils.isEmpty(resp.getString("errorCode"))
                    && StringUtils.equals(session.get("userId"), request.params.get("targetUserId"))) {
                String thumbnail = resp.getJSONObject("result").getString("thumbnail");
                session.put("profilePic", thumbnail);
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

    public static void makeFile() throws JSONException {
        String qqfile = request.params.get("qqfile");
        String UPLOAD_PATH = getUploadPath();
        String filename = request.headers.get("x-file-name").value();
        JSONObject resp = new JSONObject();
        if (request.isNew) {

            FileOutputStream moveTo = null;

            Logger.info("Name of the file %s", qqfile);

            Logger.info("Absolute on where to send %s", Play.getFile("").getAbsolutePath()
                    + File.separator + "uploads" + File.separator);
            InputStream data = null;
            try {

                data = request.body;
                File inputDoc = new File(UPLOAD_PATH + File.separator + filename);
                moveTo = new FileOutputStream(inputDoc);
                IOUtils.copy(data, moveTo);
            } catch (Exception ex) {
                Logger.log4j.error(ex.getMessage(), ex);
                resp.put("success", false);
                renderJSON(resp.toString());
            } finally {
                if (data != null) {
                    Logger.log4j.info("closing data input stream");
                    try {
                        data.close();
                    } catch (Exception ex) {
                        Logger.log4j.error(ex.getMessage(), ex);
                    }
                }
            }

        }
        resp.put("success", true);
        resp.put("fileName", filename);
        renderJSON(resp.toString());
    }

    public static void login() {
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate, no-store");
        response.setHeader("Expires", "0");
        if (StringUtils.isNotEmpty(session.get("userId"))) {
            Logger.log4j.info("The user is already logged in.");
            redirect("/home");
            return;
        }
        JSONObject categoriesResponse = getDynamicCategories();
        String activeClass = "login";
        render(renderTheme(getHTMLFilePath(className)),activeClass,categoriesResponse);
    }

    public static void terms() {
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate, no-store");
        response.setHeader("Expires", "0");
        JSONObject categoriesResponse = getDynamicCategories();
        render(renderTheme(getHTMLFilePath(className)),categoriesResponse);
    }

    public static void packages() {
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate, no-store");
        response.setHeader("Expires", "0");
        if (StringUtils.isNotEmpty(session.get("userId"))) {
            Logger.log4j.info("The user is already logged in.");
            redirect("/home");
            return;
        }
        JSONObject categoriesResponse = getDynamicCategories();
        String activeClass = "programs";
        render(renderTheme(getHTMLFilePath(className)),categoriesResponse, activeClass);
    }

    public static JSONObject getDynamicCategories() {
        Map<String, Object> allParams = getReqParams();
        allParams.put("userId", "PUBLIC");
        allParams.put("callingUserId", "PUBLIC");
        String orgId = play.Play.configuration.getProperty("learnpedia.id");
        allParams.put("orgId", orgId);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/getCategories", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }
    public static void categoryPrograms(String name) {
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate, no-store");
        response.setHeader("Expires", "0");
        if (StringUtils.isNotEmpty(session.get("userId"))) {
            Logger.log4j.info("The user is already logged in.");
            redirect("/home");
            return;
        }
        Map<String, Object> allParams = getReqParams();
        allParams.put("name", name);
        allParams.put("scope", "OPEN");
        allParams.put("start", 0);
        allParams.put("size", 50);
        allParams.put("orderBy", "timeCreated");
        JSONObject categorySectionsRes = getCategorySectionsForOuter(allParams);
        JSONObject categoriesResponse = getDynamicCategories();
        JSONObject categoryDetails = getCategoryDetails(allParams);
        Logger.log4j.info("categorySectionsRes : " + categorySectionsRes);
        String activeClass = "programs";
        render(renderTheme(getHTMLFilePath(className)), categorySectionsRes, categoriesResponse, categoryDetails, activeClass);
    }

    public static JSONObject getCategorySectionsForOuter(Map<String, Object> allParams) {
        String orgId = play.Play.configuration.getProperty("learnpedia.id");
        allParams.put("orgId", orgId);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/getCategorySections", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }

    public static JSONObject getCategoryDetails(Map<String, Object> allParams) {
        String orgId = play.Play.configuration.getProperty("learnpedia.id");
        allParams.put("orgId", orgId);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/getCategory", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }

    public static void exams(){
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate, no-store");
        response.setHeader("Expires", "0");
        if (StringUtils.isNotEmpty(session.get("userId"))) {
            Logger.log4j.info("The user is already logged in.");
            redirect("/home");
            return;
        }
        JSONObject categoriesResponse = getDynamicCategories();
        String activeClass = "exam";
        render(renderTheme(getHTMLFilePath(className)),activeClass,categoriesResponse);
    }

    public static void aiims(){
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate, no-store");
        response.setHeader("Expires", "0");
        if (StringUtils.isNotEmpty(session.get("userId"))) {
            Logger.log4j.info("The user is already logged in.");
            redirect("/home");
            return;
        }
        String popup = "exam";
        String activeClass = "aiims";
        JSONObject categoriesResponse = getDynamicCategories();
        render(renderTheme(getHTMLFilePath(className)),popup,activeClass,categoriesResponse);
    }

    public static void jeeadvanced(){
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate, no-store");
        response.setHeader("Expires", "0");
        if (StringUtils.isNotEmpty(session.get("userId"))) {
            Logger.log4j.info("The user is already logged in.");
            redirect("/home");
            return;
        }
        String popup = "exam";
        String activeClass = "jeeadvanced";
        JSONObject categoriesResponse = getDynamicCategories();
        render(renderTheme(getHTMLFilePath(className)),popup,activeClass,categoriesResponse);
    }


    public static void jeemain(){
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate, no-store");
        response.setHeader("Expires", "0");
        if (StringUtils.isNotEmpty(session.get("userId"))) {
            Logger.log4j.info("The user is already logged in.");
            redirect("/home");
            return;
        }
        String popup = "exam";
        String activeClass = "jeemain";
        JSONObject categoriesResponse = getDynamicCategories();
        render(renderTheme(getHTMLFilePath(className)),popup,activeClass,categoriesResponse);
    }

    public static void neet(){
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate, no-store");
        response.setHeader("Expires", "0");
        if (StringUtils.isNotEmpty(session.get("userId"))) {
            Logger.log4j.info("The user is already logged in.");
            redirect("/home");
            return;
        }
        String popup = "exam";
        String activeClass = "neet";
        JSONObject categoriesResponse = getDynamicCategories();
        render(renderTheme(getHTMLFilePath(className)),popup,activeClass,categoriesResponse);
    }

    public static void bitsat(){
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate, no-store");
        response.setHeader("Expires", "0");
        if (StringUtils.isNotEmpty(session.get("userId"))) {
            Logger.log4j.info("The user is already logged in.");
            redirect("/home");
            return;
        }
        String popup = "exam";
        String activeClass = "bitsat";
        JSONObject categoriesResponse = getDynamicCategories();
        render(renderTheme(getHTMLFilePath(className)),popup,activeClass,categoriesResponse);
    }

    public static JSONObject sendEmail() {
        Map<String, Object> allParams = getReqParams();
        if (play.Play.configuration.getProperty("environment").equalsIgnoreCase("prod")) {
            try {
                PostToLeadSquared();
            } catch (Exception e) {
                Logger.log4j
                        .error("*****     Something error happened while posting in LeadSquared     ******");
                Logger.log4j.debug(e.getMessage());
            }
        }
        F.Promise<AbstractUIController.JSONResponseWrapper> promise = client(
                ClientUtil.SOCIALS_WEB_SERVICE_URL + "/socials/sendEmail", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }

    public static void PostToLeadSquared() throws Exception {
            Logger.log4j.info("Inside PostToLead");
            LeadSquaredJSONHelper leadJSON = new LeadSquaredJSONHelper();
            Logger.log4j.info("fromForm is : "+request.params.get("fromForm"));
            if(request.params.get("fromForm").equals("ENQUIRY")){
                leadJSON.addLeadAttribute("mx_Investment_Size", request.params.get("investment").toString());
                leadJSON.addLeadAttribute("mx_City", request.params.get("city").toString());
                leadJSON.addLeadAttribute("Source", "Franchisee Page");
                leadJSON.addLeadAttribute("mx_Query", request.params.get("que_message").toString());
            }else if(request.params.get("fromForm").equals("EMAIL")){
                leadJSON.addLeadAttribute("Source", "Get In Touch");
                leadJSON.addLeadAttribute("mx_Query", request.params.get("message").toString());
            }
            leadJSON.addLeadAttribute("Phone", request.params.get("number").toString());
            leadJSON.addLeadAttribute("EmailAddress", request.params.get("email").toString());
            leadJSON.addLeadAttribute("FirstName", request.params.get("name").toString());

            String jsonData = leadJSON.getJSONString();

            Logger.log4j.info("json data "+ jsonData.toString());

            // adding a lead
            String accessKey = "";
            String secretKey = "";
            if(request.params.get("fromForm").equals("ENQUIRY")){
                accessKey = "u$r397e8b043a3b24f764ec7d12b1366a36";
                secretKey = "8402188cf18d7831d5f27c4926ba049dee64be21";
            }
            else{
                accessKey = "u$rd12484176de6dd1318a8621f45305114";
                secretKey = "1bbd3f66fb3d78f644969e382553a84dc9f004c3";
            }
            LeadSquaredAPIManager lead = new LeadSquaredAPIManager(accessKey, secretKey);

            lead.addLead(jsonData);
    }

    public static void singleProgramPopup(){
        Map<String,Object> allParams = getReqParams();
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/getCategorySection", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        render(renderTheme(getHTMLFilePath(className)),resp);
    }

}
