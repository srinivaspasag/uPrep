/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.data.validation.Required;
import play.i18n.Messages;
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
public class QrExports extends AbstractQRUIController{
    public static void home(){
        recordActivity(ClientUtil.ActivityPages.LIB_EXPORTS,ClientUtil.ActivityAction.OPEN);
        render();
    }
    private static JSONObject _getExports(Map<String, Object> allParams){
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsExports/getExports",
                allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }
    public static void exportList(){
        JSONObject resp=_getExports(getReqParams());
        render(resp);
    }
    public static void direct(String orgId){
        recordActivity(ClientUtil.ActivityPages.LIB_EXPORTS,ClientUtil.ActivityAction.OPEN);
        String includeName="QrExports/home.html";
        flash.put("ENTRY", "DIRECT");
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        render("Application/mapper.html",includeName,currentOrgInfo);                          
    }
    public static void submitExportReq(){
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsExports/schedule",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        render("QrExports/post.html",resp);
    }
    private static JSONObject _getExportDetails(){
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsExports/getExportDetails",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }
    public static void getExportDetails(){
        JSONObject resp = _getExportDetails();
        renderJSON(resp.toString());
    }
    public static void viewExportDetails(){
        JSONObject resp = _getExportDetails();
        try {
            JSONObject result = resp.getJSONObject("result");
            JSONArray list = result.getJSONArray("contentInfo");
            for (int i = 0; i < list.length(); i++) {
                JSONObject item = list.getJSONObject(i);
                String errorCode = item.getString("errorCode");
                String errorMessage = "";
                if (!StringUtils.isEmpty(errorCode)) {
                    errorMessage = ResponseUtil._getErrorMessage(errorCode);
                }
                item.put("errorMessage", errorMessage);
            }
        } catch (JSONException ex) {
            Logger.log4j.error(ex.getLocalizedMessage());
        }
        render(resp);
    }
    public static void getPublishStatus(){
        JSONObject resp=Widgets._getStatus();
        renderJSON(resp.toString());
    }
    public static void deleteExport(){
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsExports/delete",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }
    public static void createPopup(String sectionId){
        JSONObject sectionResp = _getSectionData(sectionId);
        render(sectionResp);
    }
    public static void createSDGroup() throws JSONException{
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/sdGroups/schedule",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }
    private static JSONObject _getSDCardsData(String groupId){
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/sdGroups/get",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }
    public static void sdCardGroup(String orgId,String groupId){
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        if(Widgets._amISuperAdmin(currentOrgInfo)){
            JSONObject resp = _getSDCardsData(groupId);
            render(resp);
        }else{
            String msg = Messages.get("PAGE_ACCESS_DENIED");
            render("UIComRegister/msgPage.html",msg);
        }
    }
    public static void directSdCardGroup(String groupId,String orgId){
        recordActivity(ClientUtil.ActivityPages.LIB_EXPORTS,ClientUtil.ActivityAction.OPEN);
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        if(Widgets._amISuperAdmin(currentOrgInfo)){
            JSONObject resp = _getSDCardsData(groupId);
            String includeName="QrExports/sdCardGroup.html";
            flash.put("ENTRY", "DIRECT");
            render("Application/mapper.html",includeName,resp,currentOrgInfo);    
        }else{
            String msg = Messages.get("PAGE_ACCESS_DENIED");
            render("UIComRegister/msgPage.html",msg);
        }
    }
    private static JSONObject _getSdCardContents(String id){
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/sdCards/contents",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }
    public static void getSdCardContents(@Required String id){
        JSONObject resp = _getSdCardContents(id);
        render(resp);
    }
    private static JSONObject _getSDCardGroupsData(String sectionId){
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/sdGroups/gets",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }
    private static JSONObject _getSectionData(String sectionId){
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/getCategorySection",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }
    public static void sdCardGroupsPage(String sectionId){
        JSONObject resp = _getSDCardGroupsData(sectionId);
        JSONObject sectionResp = _getSectionData(sectionId);
        JSONArray currencyArray = uicom.util.Utilities._getCurrencyList();
        render(resp,sectionResp,currencyArray);
    }
    public static void directSdCardGroupsPage(String sectionId,String orgId){
        recordActivity(ClientUtil.ActivityPages.LIB_EXPORTS,ClientUtil.ActivityAction.OPEN);
        JSONObject resp = _getSDCardGroupsData(sectionId);
        JSONObject sectionResp = _getSectionData(sectionId);
        JSONArray currencyArray = uicom.util.Utilities._getCurrencyList();
        String includeName="QrExports/sdCardGroupsPage.html";
        flash.put("ENTRY", "DIRECT");
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        render("Application/mapper.html",includeName,resp,currencyArray,sectionResp,currentOrgInfo);                          
    }
    public static void sdCardContentMove(String id){
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/sdGroups/move",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }
    public static void markSdGroup(String id){
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/sdGroups/mark",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }
    public static void getSectionInfo(String sectionId){
        JSONObject sectionResp = _getSectionData(sectionId);
        renderJSON(sectionResp.toString());
    }

    public static void deleteSdCardGroup(){

        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/sdGroups/deleteSdCardGroup",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }
}
