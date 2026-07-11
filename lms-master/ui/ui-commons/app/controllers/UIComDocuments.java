/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.Play;
import play.data.validation.Required;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Scope;
import play.mvc.With;
import uicom.util.ClientUtil;
import uicom.util.ResponseUtil;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import com.ning.http.multipart.FilePart;


/**
 * 
 * @author ajith
 */
@With(Security.class)
public class UIComDocuments extends AbstractUIController {
    private static String       DOMAIN_NAME    = null;
    private static final String WEB_APP        = "web-app";
    private static final String CMDS_APP       = "question-repository";
    private static String       DOCUMENTS_URL  = null;
    private static String       TAGS_URL       = null;
    private static String       DOC_UPLOAD_URL = null;
    private static String       VIDEO_UPLOAD_URL = null;
    static {
        if (Play.configuration.getProperty("application.name").equals(WEB_APP)) {
            DOCUMENTS_URL = ClientUtil.LIB_WEB_SERVICE_URL + "/documents";
            TAGS_URL = ClientUtil.LIB_WEB_SERVICE_URL + "/Tags";
            DOC_UPLOAD_URL = ClientUtil.LIB_WEB_SERVICE_URL + "/uploads";
            DOMAIN_NAME = WEB_APP;
        } else {
            String cmdsDocUrl = ClientUtil.CMDS_SERVICE_URL
                    + "/cmdsdocuments";
            DOCUMENTS_URL = cmdsDocUrl;
            TAGS_URL = cmdsDocUrl;
            DOC_UPLOAD_URL = cmdsDocUrl;
            DOMAIN_NAME = CMDS_APP;
            VIDEO_UPLOAD_URL=cmdsDocUrl;
        }
    }
    public static void up() {
        render();
    }  
    public static void pdfViewer(String openUrl){
        String url = openUrl;
        render(url);
    }
    public static void uploadStart() {
        JSONObject exams = null;
        JSONObject subjects = null;
        if (DOMAIN_NAME.equals(WEB_APP)) {
            if (Boolean.parseBoolean(Play.configuration
                    .getProperty("UPLOAD_DOC_FEATURE"))) {
                Map<String,Object> allParams=getReqParams();
                allParams.put("type", "EXAM");
                exams = UIComBoards._getOrgBoards(allParams);
                allParams.put("type", "COURSE");
                subjects = UIComBoards._getOrgBoards(allParams);
            } else {
                error(404, "You do not have permission to view this page.");
            }
        }
        render(exams, subjects);
    }

    public static void createDoc() {
        JSONObject newDoc = _createDoc(null);
        renderJSON(newDoc.toString());
    }

    public static void editDocTitle() {
        Map<String, Object> allParams = getReqParams();
        JSONObject jsonResponse;
        if (DOMAIN_NAME.equals(WEB_APP)) {
            jsonResponse = docTitle(allParams);
        } else {
            jsonResponse = _editcmdsDocMetadata(allParams);
        }
        renderJSON(jsonResponse.toString());
    }

    public static void getSuggestedTags() {
        JSONObject jsonResponse = tagSuggestions(null);
        renderJSON(jsonResponse.toString());
    }

    public static void applyTemplate() {
        Map<String, Object> allParams = getReqParams();
        JSONObject docDetails;
        JSONObject docTitle;
        if (DOMAIN_NAME.equals(WEB_APP)) {
            docDetails = _applyDocDetails(allParams);
            docTitle = docTitle(allParams);
        } else {
            docDetails = _editcmdsDocMetadata(allParams);
        }
        renderJSON(docDetails.toString());
    }

    // toc addition
    public static void toc() {
        Map<String, Object> allParams = getReqParams();
        JSONObject docInfo = _getDoc(allParams);
        JSONObject docTocs = _getTocs(allParams);
        flash.put("tabType", Http.Request.current().params.get("tabType"));
        render(docTocs, docInfo);
    }

    public static void getPage() {
        F.Promise<JSONResponseWrapper> promise = client(DOCUMENTS_URL
                + "/getPage", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject pageResponse = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(pageResponse.toString());
    }

    public static void addTOC() {
        F.Promise<JSONResponseWrapper> promise = client(DOCUMENTS_URL
                + "/addTOC", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject addedToc = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(addedToc.toString());
    }

    public static void removeToc() {
        F.Promise<JSONResponseWrapper> promise = client(DOCUMENTS_URL
                + "/deleteTOC", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject removedToc = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(removedToc.toString());
    }

    public static void editToc() {
        F.Promise<JSONResponseWrapper> promise = client(DOCUMENTS_URL
                + "/editTOC", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    // return methods
    protected static JSONObject _createDoc(Map<String, Object> allParams) {
        F.Promise<JSONResponseWrapper> promise = client(DOCUMENTS_URL
                + "/createDoc", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject newDoc = ResponseUtil.checkResponse(getJSON(promise));
        return newDoc;
    }
    protected static JSONObject _getTocs(Map<String, Object> allParams) {
        F.Promise<JSONResponseWrapper> promiseTocs = client(DOCUMENTS_URL
                + "/getTocs", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promiseTocs);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promiseTocs);
        return resp;
    }    

    protected static JSONObject tagSuggestions(Map<String, Object> allParams) {
        F.Promise<JSONResponseWrapper> promise = client(TAGS_URL
                + "/getDocTagSuggestion", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject jsonResponse = ResponseUtil.checkResponse(getJSON(promise));
        return jsonResponse;
    }

    protected static JSONObject docTitle(Map<String, Object> allParams) {
        F.Promise<JSONResponseWrapper> promise = client(DOCUMENTS_URL
                + "/editDocTitle", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject jsonResponse = ResponseUtil.checkResponse(getJSON(promise));
        return jsonResponse;
    }

    public static JSONObject _applyDocDetails(Map<String, Object> allParams) {
        F.Promise<JSONResponseWrapper> promiseDocDetails = client(DOCUMENTS_URL
                + "/editMetadata", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promiseDocDetails);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promiseDocDetails);
        return resp;
    }

    public static JSONObject _editcmdsDocMetadata(Map<String, Object> allParams) {
        F.Promise<JSONResponseWrapper> promiseDocDetails = client(DOCUMENTS_URL
                + "/editDoc", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promiseDocDetails);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promiseDocDetails);
        return resp;
    }

    
    public static void addVideo(){      
        render();
    }          
    
    public static void upload(String qqfile) {
        String docId = Scope.Params.current().get("docId");
        String UPLOAD_PATH = Play.getFile("").getAbsolutePath()
                + File.separator + "uploads";
        Logger.log4j.info("starting the upload progress");
        if (request.isNew) {
            FileOutputStream moveTo = null;

            Logger.info("Name of the file %s", qqfile);
            // Another way I used to grab the name of the file
            String filename = request.headers.get("x-file-name").value();

            Logger.info("Absolute on where to send %s", UPLOAD_PATH
                    + File.separator);
            InputStream data = null;
            try {

                Logger.log4j
                        .info("reading input stream from request body header");
                data = request.body;
                Logger.log4j
                        .info("successfully read file data from request body header");
                File up = new File(UPLOAD_PATH);
                if (!up.exists()) {
                    Logger.log4j.info("making directory "
                            + up.getAbsolutePath() + ", result is : "
                            + up.mkdir());
                }
                File inputDoc = new File(UPLOAD_PATH + File.separator
                        + filename);
                moveTo = new FileOutputStream(inputDoc);
                Logger.log4j.info("cpoying file to web-app's local system");
                IOUtils.copy(data, moveTo);
                Logger.log4j.info("file copied to local system : "
                        + inputDoc.getAbsolutePath());
                moveTo.close();
                // data.close();
                AsyncHttpClient asynClient = new AsyncHttpClient();
                Logger.log4j.info("uploading file to library service");
                Future<Response> response = asynClient
                        .preparePost(
                                DOC_UPLOAD_URL
                                        + "/uploadDoc?userId="
                                        + session.get("userId")
                                        + "&appId="
                                        + Play.configuration
                                                .getProperty("auth.appId")
                                        + "&docId=" + docId
                                        + "&organizationId="
                                        + session.get("organizationId"))
                        .addHeader("Content-Type",
                                "multipart/form-data;boundary=randomBoundaryNotInAnyOfParts")
                        .addBodyPart(new FilePart("inputDoc", inputDoc))
                        .execute();
                Response r = null;
                JSONObject jsonResponse = new JSONObject();

                try {
                    r = response.get();
                    String rspString = r.getResponseBody();
                    jsonResponse = new JSONObject(rspString);
                    Logger.log4j
                            .info("uploaded doc got deleted from local dir : "
                                    + inputDoc.delete());
                    Logger.log4j.info("status" + jsonResponse);
                } catch (InterruptedException e) {
                    Logger.log4j.error(e.getMessage(), e);
                } catch (ExecutionException e) {
                    Logger.log4j.error(e.getMessage(), e);
                } catch (JSONException e) {
                    Logger.log4j.error(e.getMessage(), e);
                } finally {
                    asynClient.close();
                    if (data != null) {
                        Logger.log4j.info("closing data input stream");
                        data.close();
                    }
                }

            } catch (Exception ex) {
                Logger.log4j.error(ex.getMessage(), ex);
                renderJSON("{success: false}");
            }

        }
        Logger.log4j.info("response for id of doc" + docId);
        renderJSON("{success: true,docId:'" + docId + "'}");
    }
    
public static void uploadVideo(String qqfile) {
        String docId = Scope.Params.current().get("docId");
        String UPLOAD_PATH = Play.getFile("").getAbsolutePath()
                + File.separator + "uploads";
        Logger.log4j.info("starting the upload progress");
        if (request.isNew) {
            FileOutputStream moveTo = null;

            Logger.info("Name of the file %s", qqfile);
            // Another way I used to grab the name of the file
            String filename = request.headers.get("x-file-name").value();

            Logger.info("Absolute on where to send %s", UPLOAD_PATH
                    + File.separator);
            InputStream data = null;
            try {

                Logger.log4j
                        .info("reading input stream from request body header");
                data = request.body;
                Logger.log4j
                        .info("successfully read file data from request body header");
                File up = new File(UPLOAD_PATH);
                if (!up.exists()) {
                    Logger.log4j.info("making directory "
                            + up.getAbsolutePath() + ", result is : "
                            + up.mkdir());
                }
                File inputVideo = new File(UPLOAD_PATH + File.separator
                        + filename);
                moveTo = new FileOutputStream(inputVideo);
                Logger.log4j.info("cpoying file to web-app's local system");
                IOUtils.copy(data, moveTo);
                Logger.log4j.info("file copied to local system : "
                        + inputVideo.getAbsolutePath());
                moveTo.close();
                // data.close();
                AsyncHttpClient asynClient = new AsyncHttpClient();
                Logger.log4j.info("uploading file to library service");
                Future<Response> response = asynClient
                        .preparePost(
                                VIDEO_UPLOAD_URL
                                        + "/uploadVideo?userId="
                                        + session.get("userId")
                                        + "&appId="
                                        + Play.configuration
                                                .getProperty("auth.appId")
                                        + "&docId=" + docId
                                        + "&organizationId="
                                        + session.get("organizationId"))
                        .addHeader("Content-Type",
                                "multipart/form-data;boundary=randomBoundaryNotInAnyOfParts")
                        .addBodyPart(new FilePart("inputVideo", inputVideo))
                        .execute();
                Response r = null;
                JSONObject jsonResponse = new JSONObject();

                try {
                    r = response.get();
                    String rspString = r.getResponseBody();
                    jsonResponse = new JSONObject(rspString);
                    Logger.log4j
                            .info("uploaded doc got deleted from local dir : "
                                    + inputVideo.delete());
                    Logger.log4j.info("status" + jsonResponse);
                } catch (InterruptedException e) {
                    Logger.log4j.error(e.getMessage(), e);
                } catch (ExecutionException e) {
                    Logger.log4j.error(e.getMessage(), e);
                } catch (JSONException e) {
                    Logger.log4j.error(e.getMessage(), e);
                } finally {
                    asynClient.close();
                    if (data != null) {
                        Logger.log4j.info("closing data input stream");
                        data.close();
                    }
                }

            } catch (Exception ex) {
                Logger.log4j.error(ex.getMessage(), ex);
                renderJSON("{success: false}");
            }

        }
        Logger.log4j.info("response for id of doc" + docId);
        renderJSON("{success: true,docId:'" + docId + "'}");
    }    

    // doc view
    public static void hocrGetter(@Required String url) throws IOException {
        Logger.log4j.info("request received for : " + url);
        AsyncHttpClient asynClient = new AsyncHttpClient();
        Future<Response> rsp = asynClient.prepareGet(url).execute();
        Response r = null;
        try {
            r = rsp.get();
            Logger.log4j.info("sending response for : " + url);
            renderHtml(r.getResponseBody());
        } catch (Exception ex) {
            Logger.log4j.info("error" + ex);
        }
        error(404, "File not found");
    }

    protected static JSONObject _getDoc(Map<String, Object> allParams) {
        F.Promise<JSONResponseWrapper> promise = client(DOCUMENTS_URL
                + "/getDoc", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject jsonResponse = ResponseUtil.checkResponse(getJSON(promise));
        return jsonResponse;
    }

}
