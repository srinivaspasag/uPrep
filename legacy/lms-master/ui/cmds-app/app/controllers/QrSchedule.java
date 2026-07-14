package controllers;

import java.util.Map;

import org.json.JSONObject;

import controllers.AbstractUIController.JSONResponseWrapper;
import play.Logger;
import play.libs.F.Promise;
import play.mvc.With;
import uicom.util.ClientUtil;
import uicom.util.ResponseUtil;

@With(Security.class)
public class QrSchedule extends AbstractQRUIController {

    public static void main() {
        render();
    }
    public static void addMember() {
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/UserManagements/addMember",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }


    //return
    protected static JSONObject _updateMemToAcadStr(Map<String, Object> allParams){
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/metadatas/updateInstructorInProgram",
                allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }


    //direct
    public static void scheduleDirect(){
        Map<String, Object> allParams=getReqParams();
        String includeName="QrSchedule/main.html";
        flash.put("ENTRY", "DIRECT");
        render("Application/mapper.html",includeName);
    }

    public static void myClassroomConnect(String programId, String orgId){
        Map<String, Object> allParams = getReqParams();
        recordActivity(ClientUtil.ActivityPages.SCHEDULE, ClientUtil.ActivityAction.OPEN);
        render();
    }

    public static void addSchedule(){
        Map<String, Object> allParams=getReqParams();
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CONTENT_SERVICE_URL + "/classroomconnect/addSchedule",
                allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void getSchedule(){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/classroomconnect/getSchedule", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void removeDaySchedule(){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/classroomconnect/removeDaySchedule", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }
    public static void removeSchedule(){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/classroomconnect/removeSchedule", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void getScheduleDayInfo(){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/classroomconnect/getDaySchedule", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject scheduleDayInfo = getJSON(promise);
        render(scheduleDayInfo);
    }

}
