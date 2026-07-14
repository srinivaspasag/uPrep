/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import java.io.File;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import play.Logger;
import play.libs.F;
import play.mvc.With;
import uicom.util.ClientUtil;
import uicom.util.ResponseUtil;

/**
 *
 * @author ajith
 */
@With(Security.class)
public class Boards extends AbstractUIController {

    public static void index() {
        render();
    }
    private static JSONObject _getBoards(Map<String, Object> allParams) {
        F.Promise<JSONResponseWrapper> promise =
                client(ClientUtil.BOARDS_SERVICE_URL + "/boards/getChildren", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }
    private static JSONObject _getOrgBoards(Map<String, Object> allParams){
        String ownerId=request.params.get("orgId");
        if(StringUtils.isEmpty(ownerId)){
            ownerId=session.get("orgId");
        }  
        if(allParams==null){
            allParams=getReqParams();
        }
        allParams.put("context", "ORG");
        allParams.put("ownerId",ownerId);
        JSONObject resp=_getBoards(allParams);
        return resp;
    }
    private static JSONObject _getConsumerBoards(Map<String, Object> allParams){
        if(allParams==null){
            allParams=getReqParams();
        }        
        allParams.put("context","CONSUMER");
        allParams.put("ownerId", "SYSTEM");        
        JSONObject resp=_getBoards(allParams);
        return resp;
    }
    public static void getBoards() {
        F.Promise<JSONResponseWrapper> promise =
                client(ClientUtil.BOARDS_SERVICE_URL + "/boards/getChildren", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        render(resp);
    }
    public static void remove() {
        F.Promise<JSONResponseWrapper> promise =
                client(ClientUtil.BOARDS_SERVICE_URL + "/boards/delete", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void uploadGlobalTree(File boardFile) {
        JSONObject resp = uploadUtil(ClientUtil.BOARDS_SERVICE_URL
                + "/boards/uploadGlobalBoards", null, boardFile);
        render("Boards/uploadBoardsResp.html", resp);
    }

    public static void uploadConsumerTree(File boardFile) {
        JSONObject resp = uploadUtil(ClientUtil.BOARDS_SERVICE_URL
                + "/boards/uploadConsumerBoards", null, boardFile);
        render("Boards/uploadBoardsResp.html", resp);
    }

    public static void uploadOrgTree(File boardFile) {
        JSONObject resp = uploadUtil(ClientUtil.BOARDS_SERVICE_URL
                + "/boards/uploadOrgBoards", null, boardFile);
        render("Boards/uploadBoardsResp.html", resp);
    }

    //view
    public static void globalBoards() {
        request.params.put("context", "GLOBAL");
        JSONObject boards = UIComBoards._getGlobalBoards(null);
        String pageTitle = "Global Boards";
        render("Boards/displayBoards.html", boards, pageTitle);
    }

    public static void consumerBoards() {
        request.params.put("context", "CONSUMER");
        request.params.put("type", "COURSE");
        JSONObject boards = _getConsumerBoards(null);
        String pageTitle = "Consumer Boards";
        render("Boards/displayBoards.html", boards, pageTitle);
    }

    public static void orgBoards(String orgId) {
        request.params.put("context", "ORG");
        request.params.put("type", "COURSE");
        JSONObject boards = _getOrgBoards(null);
        JSONObject orgInfo = Organizations._getOrg(null);
        String pageTitle = "Organization Boards";
        try {
            pageTitle = orgInfo.getJSONObject("result").getString("name");
        } catch (Exception e) {
            Logger.log4j.error("Error in fetching org info for " + orgId);
        }
        render("Boards/displayBoards.html", boards, pageTitle);
    }

    public static void getChildrenOfGlobalBoardNode() {
        JSONObject boards = UIComBoards._getChildrenOfRootNodesForGlobalBoards(null);
        render("Boards/rootNodeChildren.html", boards);
    }

    public static void getChildrenOfConsumerBoardNode() {
        JSONObject boards = UIComBoards._getChildrenOfRootNodesForConsumerBoards(null);
        render("Boards/rootNodeChildren.html", boards);
    }

    public static void getChildrenOfOrgBoardNode() {
        JSONObject boards = UIComBoards._getChildrenOfRootNodesForOrgBoards(null);
        render("Boards/rootNodeChildren.html", boards);
    }
}
