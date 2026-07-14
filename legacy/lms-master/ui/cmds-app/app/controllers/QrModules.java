/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import play.Logger;
import play.libs.F;
import play.mvc.With;
import pojos.UserOrg;
import uicom.util.ClientUtil;
import uicom.util.ResponseUtil;

/**
 *
 * @author anirban
 */
@With(Security.class)
public class QrModules extends AbstractQRUIController {

    public static void home(String orgId, String id) {
        if (StringUtils.isEmpty(id)) {
            render();
        } else {
            if(request.params._contains("targetId") && request.params._contains("targetType")){
                request.params.put("target.type", request.params.get("targetType"));
                request.params.put("target.id", request.params.get("targetId"));
            }
            JSONObject moduleInfo = _getModuleInfo(null);
            render(moduleInfo);
        }
    }

    public static void createModule() {
        QrResources.checkAndSetFolderId();
        recordActivity(ClientUtil.ActivityPages.MODULE, ClientUtil.ActivityAction.ADD);
        F.Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsModules/createModule",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void setSchedule(){
        recordActivity(ClientUtil.ActivityPages.MODULE, ClientUtil.ActivityAction.SET_SCHEDULE);
        F.Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsModules/setSchedule",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void deleteSchedule(){
        recordActivity(ClientUtil.ActivityPages.MODULE, ClientUtil.ActivityAction.SET_SCHEDULE);
        F.Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsModules/deleteSchedule",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void updateModule() {
        recordActivity(ClientUtil.ActivityPages.MODULE, ClientUtil.ActivityAction.EDIT);
        F.Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsModules/updateModule",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void addModuleEntries() {
        recordActivity(ClientUtil.ActivityPages.MODULE_ENTRY, ClientUtil.ActivityAction.ADD);
        F.Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsModules/addModuleEntries",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void updateModuleEntries() {
        recordActivity(ClientUtil.ActivityPages.MODULE_ENTRY, ClientUtil.ActivityAction.EDIT);
        F.Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsModules/updateModuleEntries",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void deleteModuleEntry() {
        recordActivity(ClientUtil.ActivityPages.MODULE_ENTRY, ClientUtil.ActivityAction.DELETE);
        F.Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsModules/deleteModuleEntry",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void moveModuleEntry() {
        recordActivity(ClientUtil.ActivityPages.MODULE_ENTRY, ClientUtil.ActivityAction.EDIT);
        F.Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsModules/moveModuleEntry",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void getAllModules() {
        recordActivity(ClientUtil.ActivityPages.MODULE_ENTRY, ClientUtil.ActivityAction.DELETE);
        F.Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsModules/allModules",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void editModuleTags() {
        JSONObject moduleInfo = _getModuleInfo(null);
        render(moduleInfo);
    }

    public static void moduleContents(){
        JSONObject moduleInfo = _getModuleInfo(null);
        render(moduleInfo);
    }


    protected static JSONObject _getModuleInfo(Map<String, Object> allParams) {

        F.Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsModules/getCMDSModuleInfo", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }

    public static void direct(String orgId, String id) {
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        String includeName = "QrModules/home.html";
        flash.put("ENTRY", "DIRECT");
        if (StringUtils.isEmpty(id)) {
            render("Application/mapper.html", includeName, currentOrgInfo);
        } else {
            if(request.params._contains("targetId") && request.params._contains("targetType")){
                request.params.put("target.type", request.params.get("targetType"));
                request.params.put("target.id", request.params.get("targetId"));
            }
            JSONObject moduleInfo = _getModuleInfo(null);
            render("Application/mapper.html", includeName, currentOrgInfo, moduleInfo);
        }

    }
}
