package controllers;

import static controllers.QrQuestions._getQuestionSetQuesns;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import play.Logger;
import play.libs.F.Promise;
import play.mvc.Scope;
import play.mvc.Scope.Params;
import play.mvc.With;
import pojos.UserOrg;
import uicom.response.JSONResponse;
import uicom.util.ClientUtil;
import uicom.util.ResponseUtil;

@With(Security.class)
public class QrAddContent extends AbstractQRUIController {

    public static void addContent() {

        recordActivity(ClientUtil.ActivityPages.ADD_CONTENT, ClientUtil.ActivityAction.OPEN);
        render();
    }

    public static void addVideo() {

        recordActivity(ClientUtil.ActivityPages.VIDEO, ClientUtil.ActivityAction.OPEN);
        render();
    }

    public static void addResourcePopup() {

        recordActivity(ClientUtil.ActivityPages.RESOURCE, ClientUtil.ActivityAction.OPEN);
        String TYPE = params.get("popupTypeMsg");
        render(TYPE);
    }

    public static void commitVideo(String orgId) {

        recordActivity(ClientUtil.ActivityPages.VIDEO, ClientUtil.ActivityAction.ADD);
        String folderId = request.params.get("folderId");
        if (StringUtils.isEmpty(folderId)) {
            folderId = QrResources._getRootFolderId(orgId);
            request.params.put("folderId", folderId);
        }
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsVideos/confirmVideo", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void commitFile(String orgId) {

        recordActivity(ClientUtil.ActivityPages.FILE, ClientUtil.ActivityAction.ADD);
        String folderId = request.params.get("folderId");
        if (StringUtils.isEmpty(folderId)) {
            folderId = QrResources._getRootFolderId(orgId);
            request.params.put("folderId", folderId);
        }
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsFiles/confirm", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void commitDoc(String orgId) {

        recordActivity(ClientUtil.ActivityPages.DOCUMENT, ClientUtil.ActivityAction.ADD);
        String folderId = request.params.get("folderId");
        if (StringUtils.isEmpty(folderId)) {
            folderId = QrResources._getRootFolderId(orgId);
            request.params.put("folderId", folderId);
        }
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsDocuments/confirm", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    /*
     * public static void cancelVideoUpload(){ Promise<AbstractQRUIController.JSONResponseWrapper>
     * promise = client(ClientUtil.CMDS_SERVICE_URL +"/cmdsResources/cancelVideo",null);
     * Logger.log4j.info("BEFORE AWAIT"); await(promise); Logger.log4j.info("AFTER AWAIT");
     * JSONObject resp = ResponseUtil.checkResponse(getJSON(promise)); renderJSON(resp.toString());
     * }
     */
    public static void addQuestionPage() {

        recordActivity(ClientUtil.ActivityPages.ADD_QUESTION, ClientUtil.ActivityAction.OPEN);
        render("UIComQuestions/addQuestion.html");
    }

    public static void addMultipleQuestionsPage() {

        recordActivity(ClientUtil.ActivityPages.ADD_QUESTION, ClientUtil.ActivityAction.OPEN);
        render("UIComQuestions/addMultipleQuestions.html");
    }

    protected static JSONObject _getUploadSigned() {

        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsResources/getSignedRequest", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }

    public static void getUploadSigned() {

        JSONObject resp = _getUploadSigned();
        renderJSON(resp.toString());
    }

    public static void uploadLocalFile() {

        JSONObject uploadResp = ResponseUtil.checkResponse(uploadUtil(ClientUtil.CMDS_SERVICE_URL
                + "/cmdsResources/upload", null, null));
        if (uploadResp == null) {
            uploadResp = new JSONObject(new JSONResponse("", "UPLOAD_ERROR", "UPLOAD_ERROR"));
        }
        renderJSON(uploadResp.toString());
        /*
         * Promise<AbstractQRUIController.JSONResponseWrapper> promise =
         * client(ClientUtil.CMDS_SERVICE_URL +"/cmdsResources/upload",null);
         * Logger.log4j.info("BEFORE AWAIT"); await(promise); Logger.log4j.info("AFTER AWAIT");
         * JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
         * renderJSON(resp.toString());
         */
    }

    public static void uploadCmdsVideo() {

        /*
         * JSONObject uploadResp=ResponseUtil.checkResponse(uploadUtil(ClientUtil.CMDS_SERVICE_URL
         * +"/cmdsVideos/uploadVideo",null,null)); renderJSON(uploadResp.toString());
         */
        Params params = Scope.Params.current();
        String url = params.get("postUrl");
        params.remove("postUrl");
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(url, null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void uploadQuestionSet() {

        JSONObject uploadResp = uploadUtil(ClientUtil.CMDS_SERVICE_URL
                + "/cmdsQuestionSets/uploadQuestionFile", null, null);
        JSONObject finalResp = new JSONObject(new JSONResponse("",
                "Some error occured. Please try again", "UPLOAD_ERROR"));
        String questionSetId = "";
        String filePrefix = "";
        try {
            if (uploadResp != null && uploadResp.has("errorCode")) {
                if (uploadResp.getString("errorCode").isEmpty()) {
                    JSONObject result = uploadResp.getJSONObject("result");
                    questionSetId = result.getString("questionSetId");
                    filePrefix = result.getString("filePrefix");
                    setQuestionSetParams(questionSetId);
                    Scope.Params.current().put("size", "1000");
                    JSONObject questions = _getQuestionSetQuesns(null);
                    finalResp = questions;
                } else if (!uploadResp.getString("errorCode").isEmpty()) {
                    finalResp = uploadResp;
                }
            }
        } catch (Exception e) {
            Logger.log4j.error("Problem in uploading question set" + e.getMessage());
        }
        // recordActivity(ClientUtil.ActivityPages.QUESTION_SET,ClientUtil.ActivityAction.UPLOAD);
        render("QrAddContent/preview.html", finalResp, filePrefix, questionSetId);
    }

    public static void getUploadedQSetQuesns() {

        recordActivity(ClientUtil.ActivityPages.QUESTION_SET, ClientUtil.ActivityAction.OPEN);
        setQuestionSetParams(request.params.get("questionSetId"));
        JSONObject questions = _getQuestionSetQuesns(null);
        render("QrQuestions/qrQuesns.html", questions);
    }

    private static void setQuestionSetParams(String id) {

        request.params.put("questionSet.id", id);
        request.params.put("questionSet.type", "CMDSQUESTIONSET");
        request.params.put("needCBox", "false");
        request.params.put("state", "TEMPORARY");
        request.params.put("size", "1000");
        request.params.put("target", "PREVIEW_QUESTIONS");
    }

    public static void submitUploadedQuesns() {

        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsQuestionSets/confirmQuestionSetUpload", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    // upload or add video
    public static void createAddVideo() {

        recordActivity(ClientUtil.ActivityPages.VIDEO, ClientUtil.ActivityAction.ADD);
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/CmdsDocuments/addVideo", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    // for direct access
    public static void addContentDirect(String orgId) {

        recordActivity(ClientUtil.ActivityPages.ADD_CONTENT, ClientUtil.ActivityAction.OPEN);
        String includeName = "QrAddContent/addContent.html";
        flash.put("ENTRY", "DIRECT");
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        render("Application/mapper.html", includeName, currentOrgInfo);
    }
}
