package controllers;

import java.io.File;
import java.io.FileNotFoundException;
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
import play.libs.F.Promise;
import play.mvc.Http.Request;
import play.mvc.Scope;
import play.mvc.With;
import uicom.util.ClientUtil;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import com.ning.http.multipart.FilePart;

@With(Security.class)
public class Playlists extends AbstractUIController {
    public static void PLItems() {    
        JSONObject playlists=_getPlaylists(null);
        if(Scope.Params.current()._contains("target")){
            flash.put("target",Scope.Params.current().get("target"));
        }    
        render(playlists);
    }
    public static void PLView() {
        JSONObject playlist=_getPlaylistInfo(null);
        render(playlist);
    }
    public static void PLCurate() {
        Scope.Params.current().put("type","EXAM");
        Scope.Params.current().put("size", "7");
        Map<String, Object> allParams=getReqParams();
        JSONObject exams=Boards._getBoards(allParams);
        JSONObject playlist=_getPlaylistInfo(allParams);
        render(playlist,exams);
    }
    public static void createPL() {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.LIB_WEB_SERVICE_URL + "/playlists/createPlaylist", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject jsonResponse = getJSON(promise);
        JSONObject errorCheck=null;
        try {
            if (jsonResponse == null || !jsonResponse.getString("errorCode").isEmpty()) {
                errorCheck = ErrorStore.getJSONReqError(jsonResponse);
                renderJSON(errorCheck.toString());
            }
        } catch (JSONException ex) {
           Logger.log4j.error(ex.getMessage());
        }
        renderJSON(jsonResponse.toString());
    }

    public static void getPlaylists() {
        Map<String, Object> allParams=getReqParams();
        JSONObject playlists = _getPlaylists(allParams);
        JSONObject errorCheck=null;
        try {
            if (playlists == null || !playlists.getString("errorCode").isEmpty()) {
                errorCheck = ErrorStore.getJSONReqError(playlists);
                renderJSON(errorCheck.toString());
            }
        } catch (JSONException ex) {
           Logger.log4j.error(ex.getMessage());
        }
        renderJSON(playlists.toString());
    }
    public static void getPlaylistInfo() {
        JSONObject playlist = _getPlaylistInfo(null);
        JSONObject errorCheck=null;
        try {
            if (playlist == null || !playlist.getString("errorCode").isEmpty()) {
                errorCheck = ErrorStore.getJSONReqError(playlist);
                renderJSON(errorCheck.toString());
            }
        } catch (JSONException ex) {
           Logger.log4j.error(ex.getMessage());
        }
        renderJSON(playlist.toString());
    }
    public static void updatePL() {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.LIB_WEB_SERVICE_URL + "/playlists/updatePlaylist", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject jsonResponse = getJSON(promise);
        JSONObject errorCheck=null;
        try {
            if (jsonResponse == null || !jsonResponse.getString("errorCode").isEmpty()) {
                errorCheck = ErrorStore.getJSONReqError(jsonResponse);
                renderJSON(errorCheck.toString());
            }
        } catch (JSONException ex) {
           Logger.log4j.error(ex.getMessage());
        }
        renderJSON(jsonResponse.toString());
    }
    public static void updatePLStatus() {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.LIB_WEB_SERVICE_URL + "/playlists/publishPlaylist", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        renderJSON(resp.toString());
    }
    public static void removePlaylist() {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.LIB_WEB_SERVICE_URL + "/playlists/removePlaylist", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject jsonResponse = getJSON(promise);
        JSONObject errorCheck=null;
        try {
            if (jsonResponse == null || !jsonResponse.getString("errorCode").isEmpty()) {
                errorCheck = ErrorStore.getJSONReqError(jsonResponse);
                renderJSON(errorCheck.toString());
            }
        } catch (JSONException ex) {
           Logger.log4j.error(ex.getMessage());
        }
        renderJSON(jsonResponse.toString());
    }



//topics in playlist
    public static void addTopic() {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.LIB_WEB_SERVICE_URL + "/playlists/addTopic", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject jsonResponse = getJSON(promise);
        JSONObject errorCheck=null;
        try {
            if (jsonResponse == null || !jsonResponse.getString("errorCode").isEmpty()) {
                errorCheck = ErrorStore.getJSONReqError(jsonResponse);
                renderJSON(errorCheck.toString());
            }
        } catch (JSONException ex) {
           Logger.log4j.error(ex.getMessage());
        }
        renderJSON(jsonResponse.toString());
    }

    public static void updateTopic() {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.LIB_WEB_SERVICE_URL + "/playlists/updateTopic", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject jsonResponse = getJSON(promise);
        JSONObject errorCheck=null;
        try {
            if (jsonResponse == null || !jsonResponse.getString("errorCode").isEmpty()) {
                errorCheck = ErrorStore.getJSONReqError(jsonResponse);
                renderJSON(errorCheck.toString());
            }
        } catch (JSONException ex) {
           Logger.log4j.error(ex.getMessage());
        }
        renderJSON(jsonResponse.toString());
    }

    public static void PLTopic() {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.LIB_WEB_SERVICE_URL + "/playlists/getTopicInfo", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject topicInfo = getJSON(promise);
        render(topicInfo);
    }
    public static void moveTopic() {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.LIB_WEB_SERVICE_URL + "/playlists/moveTopic", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject jsonResponse = getJSON(promise);
        JSONObject errorCheck=null;
        try {
            if (jsonResponse == null || !jsonResponse.getString("errorCode").isEmpty()) {
                errorCheck = ErrorStore.getJSONReqError(jsonResponse);
                renderJSON(errorCheck.toString());
            }
        } catch (JSONException ex) {
           Logger.log4j.error(ex.getMessage());
        }
        renderJSON(jsonResponse.toString());
    }
    public static void removeTopic() {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.LIB_WEB_SERVICE_URL + "/playlists/removeTopic", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject jsonResponse = getJSON(promise);
        JSONObject errorCheck=null;
        try {
            if (jsonResponse == null || !jsonResponse.getString("errorCode").isEmpty()) {
                errorCheck = ErrorStore.getJSONReqError(jsonResponse);
                renderJSON(errorCheck.toString());
            }
        } catch (JSONException ex) {
           Logger.log4j.error(ex.getMessage());
        }
        renderJSON(jsonResponse.toString());
    }



    //sections in playlist
    public static void addSection() {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.LIB_WEB_SERVICE_URL + "/playlists/addSection", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject jsonResponse = getJSON(promise);
        JSONObject errorCheck=null;
        try {
            if (jsonResponse == null || !jsonResponse.getString("errorCode").isEmpty()) {
                errorCheck = ErrorStore.getJSONReqError(jsonResponse);
                renderJSON(errorCheck.toString());
            }
        } catch (JSONException ex) {
           Logger.log4j.error(ex.getMessage());
        }
        renderJSON(jsonResponse.toString());
    }

    public static void updateSection() {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.LIB_WEB_SERVICE_URL + "/playlists/updateSection", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject jsonResponse = getJSON(promise);
        JSONObject errorCheck=null;
        try {
            if (jsonResponse == null || !jsonResponse.getString("errorCode").isEmpty()) {
                errorCheck = ErrorStore.getJSONReqError(jsonResponse);
                renderJSON(errorCheck.toString());
            }
        } catch (JSONException ex) {
           Logger.log4j.error(ex.getMessage());
        }
        renderJSON(jsonResponse.toString());
    }
    public static void addSourceToSection() {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.LIB_WEB_SERVICE_URL + "/playlists/addSourceToSection", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject jsonResponse = getJSON(promise);
        JSONObject errorCheck=null;
        try {
            if (jsonResponse == null || !jsonResponse.getString("errorCode").isEmpty()) {
                errorCheck = ErrorStore.getJSONReqError(jsonResponse);
                renderJSON(errorCheck.toString());
            }
        } catch (JSONException ex) {
           Logger.log4j.error(ex.getMessage());
        }
        renderJSON(jsonResponse.toString());
    }
    public static void moveSection() {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.LIB_WEB_SERVICE_URL + "/playlists/moveSection", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject jsonResponse = getJSON(promise);
        JSONObject errorCheck=null;
        try {
            if (jsonResponse == null || !jsonResponse.getString("errorCode").isEmpty()) {
                errorCheck = ErrorStore.getJSONReqError(jsonResponse);
                renderJSON(errorCheck.toString());
            }
        } catch (JSONException ex) {
           Logger.log4j.error(ex.getMessage());
        }
        renderJSON(jsonResponse.toString());
    }
    public static void removeSection() {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.LIB_WEB_SERVICE_URL + "/playlists/removeSection", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject jsonResponse = getJSON(promise);
        JSONObject errorCheck=null;
        try {
            if (jsonResponse == null || !jsonResponse.getString("errorCode").isEmpty()) {
                errorCheck = ErrorStore.getJSONReqError(jsonResponse);
                renderJSON(errorCheck.toString());
            }
        } catch (JSONException ex) {
           Logger.log4j.error(ex.getMessage());
        }
        renderJSON(jsonResponse.toString());
    }

    public static void cropDocumentPage() {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.LIB_WEB_SERVICE_URL + "/playlists/cropDocumentPage", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject jsonResponse = getJSON(promise);
        JSONObject errorCheck=null;
        try {
            if (jsonResponse == null || !jsonResponse.getString("errorCode").isEmpty()) {
                errorCheck = ErrorStore.getJSONReqError(jsonResponse);
                renderJSON(errorCheck.toString());
            }
        } catch (JSONException ex) {
           Logger.log4j.error(ex.getMessage());
        }
        renderJSON(jsonResponse.toString());
    }
    public static void removeImage() {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.LIB_WEB_SERVICE_URL + "/playlists/removeImageFromSection", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject jsonResponse = getJSON(promise);
        JSONObject errorCheck=null;
        try {
            if (jsonResponse == null || !jsonResponse.getString("errorCode").isEmpty()) {
                errorCheck = ErrorStore.getJSONReqError(jsonResponse);
                renderJSON(errorCheck.toString());
            }
        } catch (JSONException ex) {
           Logger.log4j.error(ex.getMessage());
        }
        renderJSON(jsonResponse.toString());
    }


    //others
    public static void getPLPeople(){
        Map<String, Object> allParams=getReqParams();
        JSONObject commons=Widgets._getEntityCommonFollowing(allParams);
        JSONObject allFollowers=Widgets._getEntityFollowers(allParams);
        render("/tags/docPL/people.html",commons,allFollowers);
    }
    public static void PLReviews(){
        render();
    }

    
    //utilities
    public static String getPLUrl(String title,String plId){
        String url="";
        if(title.isEmpty()||plId.isEmpty()){
            url="/";
        }
        else{
            url="/playlist/"+plId;
        }
        return url;
    }
    public static void playlistDirect(@Required String playlistId){
        JSONObject playlist=_getPlaylistInfo(null);
        String includeName="Playlists/PLView.html";
        flash.put("ENTRY", "DIRECT");
        render("Application/myPages.html",includeName,playlist);
    }
    public static void curatePlaylistDirect(){
        MyContents.myContentDirect("Playlists");
    }


    
    //return functions
    public static JSONObject _getPlaylists(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.LIB_WEB_SERVICE_URL + "/playlists/getPlaylists", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject playlists = getJSON(promise);
        return playlists;
    }
    public static JSONObject _getPlaylistInfo(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.LIB_WEB_SERVICE_URL + "/playlists/getPlaylistInfo", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        return resp;
    }


    public static void uploadImage(String qqfile, String playlistId, String topicId,String sectionId){
        String caption=Request.current().params.get("caption");
        String UPLOAD_PATH = Play.getFile("").getAbsolutePath() + File.separator + "uploads";
        Logger.log4j.info("starting the upload progress");
        JSONObject jsonResponse = new JSONObject();
        if (request.isNew) {
            FileOutputStream moveTo = null;
            
            Logger.info("Name of the file %s", qqfile);
            // Another way I used to grab the name of the file
            String filename = request.headers.get("x-file-name").value();

            Logger.info("Absolute on where to send %s", UPLOAD_PATH + File.separator);
            try {
                Logger.log4j.info("sending caption "+caption);
                InputStream data = request.body;
                File up = new File(UPLOAD_PATH);
                if (!up.exists()) {
                    Logger.log4j.info("making directory " + up.getAbsolutePath() + ", result is : " + up.mkdir());
                }
                File inputDoc = new File(UPLOAD_PATH + File.separator + filename);
                moveTo = new FileOutputStream(inputDoc);
                IOUtils.copy(data, moveTo);
                moveTo.close();
                AsyncHttpClient asynClient = new AsyncHttpClient();
                Future<Response> response = asynClient.preparePost(ClientUtil.LIB_WEB_SERVICE_URL + "/playlists/uploadImageToSection?userId=" +
                        session.get("userId") + "&appId="+Play.configuration.getProperty("auth.appId")+"&username=" + session.get("username") + "&playlistId=" + playlistId + "&topicId=" +
                        topicId + "&sectionId=" + sectionId+"&caption="+caption).addHeader("Content-Type", "multipart/form-data;boundary=randomBoundaryNotInAnyOfParts")
                        .addBodyPart(new FilePart("file", inputDoc)).execute();
                Response r = null;
                try {
                    r = response.get();
                    String rspString = r.getResponseBody();
                    jsonResponse = new JSONObject(rspString);
                    if(  inputDoc.delete() ){
                    	Logger.log4j.debug("uploaded image "+ inputDoc.getAbsolutePath() +"got deleted from local");
                    }
                    Logger.log4j.info("responseg for playlist image upload" + jsonResponse);

                } catch (InterruptedException e) {
                    Logger.log4j.error(e.getLocalizedMessage());
                } catch (ExecutionException e) {
                    Logger.log4j.error(e.getLocalizedMessage());
                } catch (JSONException e) {
                    Logger.log4j.error(e.getLocalizedMessage());
                } finally {
                    asynClient.close();
                }

            }catch(FileNotFoundException e){
                Logger.log4j.error(e.getMessage());
                }catch(IOException e){
                Logger.log4j.error(e.getMessage());
                }
        }

        try {
            String imageUrl = "";
            if (jsonResponse != null && jsonResponse.get("errorMessage").toString().isEmpty()) {
                imageUrl = jsonResponse.getString("result");
            }
            renderJSON("{success: true,imageUrl:'" + imageUrl + "'}");
        } catch (Exception e) {
            Logger.log4j.error("error " + e);
        }
    }
}
