package controllers;

import java.util.Map;

import org.json.JSONObject;

import play.Logger;
import play.libs.F.Promise;
import play.mvc.With;
import pojos.UserOrg;
import uicom.util.ClientUtil;
import uicom.util.ResponseUtil;

@With(Security.class)
public class QrBoards extends AbstractQRUIController {

    //courses
    public static void main() {
        recordActivity(ClientUtil.ActivityPages.BOARDS,ClientUtil.ActivityAction.OPEN);
        JSONObject courses = UIComBoards._getOrgBoards(null);
        render(courses);
    }
    public static void getPublicBoards() {
        JSONObject boards = UIComBoards._getConsumerBoards(null);
        renderJSON(boards.toString());
    }
    public static void addBoard() {
        recordActivity(ClientUtil.ActivityPages.BOARDS,ClientUtil.ActivityAction.ADD);
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/Metadatas/addBoard",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void shareQuestionsDirect(String orgId) {
        if(orgId.equals(play.Play.configuration.getProperty("learnpedia.id"))){
        recordActivity(ClientUtil.ActivityPages.BOARDS,ClientUtil.ActivityAction.OPEN);
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsResources/getQuestionSharingBasicInfo", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(request.params.get("orgId"));
        String includeName = "QrBoards/shareQuestions.html";
        flash.put("ENTRY", "DIRECT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        render("Application/mapper.html", includeName,resp,currentOrgInfo);
		}
		else{
			render("errors/404.html");
		}
    }

    public static void shareQuestions() {
        recordActivity(ClientUtil.ActivityPages.BOARDS,ClientUtil.ActivityAction.OPEN);
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsResources/getQuestionSharingBasicInfo", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        render(resp);
    }

    public static void addMappings() {
        recordActivity(ClientUtil.ActivityPages.BOARDS,ClientUtil.ActivityAction.ADD);
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsResources/addMappings", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        render(resp);
    }

    public static void saveMapping() {
        recordActivity(ClientUtil.ActivityPages.BOARDS,ClientUtil.ActivityAction.ADD);
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsResources/saveMapping", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void deleteMapping() {
        recordActivity(ClientUtil.ActivityPages.BOARDS,ClientUtil.ActivityAction.ADD);
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsResources/deleteMapping", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void shareMapping() {
        recordActivity(ClientUtil.ActivityPages.BOARDS,ClientUtil.ActivityAction.ADD);
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsResources/shareMapping", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void visibleMapping() {
        recordActivity(ClientUtil.ActivityPages.BOARDS,ClientUtil.ActivityAction.ADD);
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsResources/visibleMapping", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void editBoard() {
        recordActivity(ClientUtil.ActivityPages.BOARDS,ClientUtil.ActivityAction.EDIT);
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/Metadatas/editBoard",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }
    public static void topicTree(){
        Map<String, Object> allParams=getReqParams();

        allParams.put("parentId",request.params.get("instParentId"));
        JSONObject topics = UIComBoards._getOrgBoards(allParams);

        String publicBrdId=request.params.get("publicParentId");
        JSONObject publicTopics=null;
        if(!publicBrdId.isEmpty()){
            allParams.put("parentId",publicBrdId);
             publicTopics= UIComBoards._getOrgBoards(allParams);
        }
        render(publicTopics,topics);
    }
    public static void subTopics() {
        JSONObject subTopics=new JSONObject();
        Map<String, Object> allParams=getReqParams();
        if(request.params.get("treeType").equals("INSTITUTE")){
             subTopics= UIComBoards._getOrgBoards(allParams);
        }else if(request.params.get("treeType").equals("PUBLIC")){
            subTopics= UIComBoards._getConsumerBoards(allParams);
        }
        render(subTopics);
    }

    //other utilities
    public static void coursesvChoose() {
        request.params.put("type","COURSE");
        request.params.put("start","0");
        request.params.put("size","200");
        JSONObject courses=UIComBoards._getOrgBoards(null);
        render(courses);
    }


    //direct
    public static void coursemanagementDirect(String orgId){
        recordActivity(ClientUtil.ActivityPages.BOARDS,ClientUtil.ActivityAction.OPEN);
        request.params.put("start","0");
        request.params.put("size","200");
        request.params.put("type","COURSE");
        Map<String, Object> allParams=getReqParams();
        JSONObject courses=UIComBoards._getOrgBoards(allParams);
        String includeName="QrBoards/main.html";
        flash.put("ENTRY", "DIRECT");
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        render("Application/mapper.html",includeName,courses,currentOrgInfo);
    }
}
