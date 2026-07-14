/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.libs.F;
import uicom.util.ClientUtil;
import uicom.util.ResponseUtil;

/**
 *
 * @author ajith
 */
public class UIComWidgets extends AbstractUIController {
    public static void fe(){
        render();
    }

    public static void videoInfo(){
        F.Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/videos/getVideoInfo", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject videoInfo = ResponseUtil.checkResponse(getJSON(promise));
        render(videoInfo);
    }
    public static void pageNotFound(){
        render("errors/pageNotFound.html");
    }
    public static void addDiagrams(){
        Map<String,Object> allParams=getReqParams();
        allParams.put("size",8);
        //JSONObject diagrams = getSearchDiagrams(allParams);
        render();
    }

    public static void uploadImageInContent() {
        Logger.log4j.info("File Uploading In Content =============================== ");
        JSONObject data = uploadUtil(ClientUtil.CONTENT_SERVICE_URL + "/uploads/uploadImage",
                null, null);
        renderJSON(data.toString());
    }
    public static void uploadImageInComm() {
        Logger.log4j.info("File Uploading In Comm =============================== ");
        JSONObject data = uploadUtil(ClientUtil.MSG_WEB_SERVICE_URL + "/uploads/uploadImage",
                null, null);
        renderJSON(data.toString());
    }
    public static void uploadImageInCmds() {
        Logger.log4j.info("File Uploading In Cmds =============================== ");
        JSONObject data = uploadUtil(ClientUtil.CMDS_SERVICE_URL + "/uploads/uploadImage",
                null, null);
        renderJSON(data.toString());
    }
}
