/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;
import java.util.Map;

import org.json.JSONObject;

import play.Logger;
import play.libs.F.Promise;
import play.mvc.With;
import pojos.UserOrg;
import uicom.util.ClientUtil;
import uicom.util.ResponseUtil;
/**
 *
 * @author anirban
 */
@With(Security.class)
public class QrDevices extends AbstractQRUIController{
    private static JSONObject _getDevices(Map<String, Object> allParams){
//        allParams.put("deviceType", "WEB");
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/activityLogger/getUsers",
                allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }
    private static JSONObject _getUserActivities(Map<String, Object> allParams){
//        allParams.put("deviceType", "WEB");
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/activityLogger/getUserStatus",
                allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }
    public static void home(){
        recordActivity(ClientUtil.ActivityPages.DEVICE_MGNT,ClientUtil.ActivityAction.OPEN);
        render();
    }
    public static void direct(String orgId){
        recordActivity(ClientUtil.ActivityPages.DEVICE_MGNT,ClientUtil.ActivityAction.OPEN);
        String includeName="QrDevices/home.html";
        flash.put("ENTRY", "DIRECT");
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        render("Application/mapper.html",includeName,currentOrgInfo);                          
    }
    public static void deviceTable(){
        JSONObject devicesResp=_getDevices(getReqParams());
        render(devicesResp);
    }
    public static void userDeviceDetails(String targetUserId){
        recordActivity(ClientUtil.ActivityPages.DEVICE_ACTIVITY,ClientUtil.ActivityAction.OPEN,ClientUtil.Entity.USER,targetUserId);
        JSONObject devicesResp=_getUserActivities(getReqParams());
        render(devicesResp);
    }
    public static void userDeviceTable(){
        JSONObject devicesResp=_getUserActivities(getReqParams());
        render(devicesResp);
    }
}
