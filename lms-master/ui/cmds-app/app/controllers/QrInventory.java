/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;
import org.json.JSONObject;

import play.Logger;
import play.i18n.Messages;
import play.libs.F.Promise;
import pojos.UserOrg;
import uicom.util.ClientUtil;
import uicom.util.ResponseUtil;
/**
 *
 * @author anirban
 */
public class QrInventory extends AbstractQRUIController{
    public static void home(String orgId){
        recordActivity(ClientUtil.ActivityPages.INVENTORY,ClientUtil.ActivityAction.OPEN);
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        if(Widgets._amISuperAdmin(currentOrgInfo)){
            String openPageId = request.params.get("openPageId");
            openPageId = openPageId == null || openPageId.isEmpty() ? "inventory" : openPageId;
            render("QrInventory/sellerBoard.html",openPageId);
        }else{
            String msg = Messages.get("PAGE_ACCESS_DENIED");
            render("UIComRegister/msgPage.html",msg);
        }
    }
    public static void direct(String orgId){
        recordActivity(ClientUtil.ActivityPages.INVENTORY,ClientUtil.ActivityAction.OPEN);
        String includeName="QrInventory/sellerBoard.html";
        flash.put("ENTRY", "DIRECT");
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        if(Widgets._amISuperAdmin(currentOrgInfo)){
            String openPageId = request.params.get("openPageId");
            openPageId = openPageId == null || openPageId.isEmpty() ? "inventory" : openPageId;
            render("Application/mapper.html",includeName,currentOrgInfo,openPageId);
        }else{
            String msg = Messages.get("PAGE_ACCESS_DENIED");
            render("UIComRegister/msgPage.html",msg);
        }
    }
    public static void inventory(String orgId){
        recordActivity(ClientUtil.ActivityPages.INVENTORY,ClientUtil.ActivityAction.OPEN);
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        if(Widgets._amISuperAdmin(currentOrgInfo)){
            JSONObject pointsOfSale = _getPointsOfSale();
            render(pointsOfSale);
        }else{
            String msg = Messages.get("PAGE_ACCESS_DENIED");
            render("UIComRegister/msgPage.html",msg);
        }
    }
    public static void shipments(String orgId){
        recordActivity(ClientUtil.ActivityPages.SHIPMENTS,ClientUtil.ActivityAction.OPEN);
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        if(Widgets._amISuperAdmin(currentOrgInfo)){
            JSONObject pointsOfSale = _getPointsOfSale();
            render(pointsOfSale);
        }else{
            String msg = Messages.get("PAGE_ACCESS_DENIED");
            render("UIComRegister/msgPage.html",msg);
        }
    }
    public static void directShipments(String orgId){
        recordActivity(ClientUtil.ActivityPages.INVENTORY,ClientUtil.ActivityAction.OPEN);
        String includeName="QrInventory/sellerBoard.html";
        flash.put("ENTRY", "DIRECT");
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        if(Widgets._amISuperAdmin(currentOrgInfo)){
            String openPageId = "shipments";
            render("Application/mapper.html",includeName,currentOrgInfo,openPageId);
        }else{
            String msg = Messages.get("PAGE_ACCESS_DENIED");
            render("UIComRegister/msgPage.html",msg);
        }
    }
    public static void createInventoryPopup(String orgId){
       
        JSONObject pointsOfSale = _getPointsOfSale();
        render(pointsOfSale);
    }
    public static void createBulkAccessCodesPopup(String orgId){
        JSONObject pointsOfSale = _getPointsOfSale();
        render(pointsOfSale);
    }
    public static void directOrders(String orgId){
        recordActivity(ClientUtil.ActivityPages.INVENTORY,ClientUtil.ActivityAction.OPEN);
        String includeName="QrInventory/sellerBoard.html";
        flash.put("ENTRY", "DIRECT");
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        if(Widgets._amISuperAdmin(currentOrgInfo)){
            String openPageId = "orders";
            render("Application/mapper.html",includeName,currentOrgInfo,openPageId);
        }else{
            String msg = Messages.get("PAGE_ACCESS_DENIED");
            render("UIComRegister/msgPage.html",msg);
        }
    }
    public static void postNewEntry(){
        recordActivity(ClientUtil.ActivityPages.INVENTORY,ClientUtil.ActivityAction.ADD);
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/accessCodes/generateAccessCode",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        render(resp);
    }
    public static JSONObject postBulkAccessCodeReq(){
        recordActivity(ClientUtil.ActivityPages.INVENTORY,ClientUtil.ActivityAction.ADD);
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/accessCodes/generateBulkAccessCode",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }
    public static void updateShipmentStatus(){
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/accessCodes/updateShipmentStatus",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }
    public static void resendAccessCodeEmail(){
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/accessCodes/resendEmail",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }
    private static JSONObject _getPointsOfSale(){
        recordActivity(ClientUtil.ActivityPages.INVENTORY,ClientUtil.ActivityAction.ADD);
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/getOrgPointsOfSale",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }
    public static void fetchInventories(){
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/accessCodes/getAccessCodes",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        render(resp);
    }
    public static void fetchShipments(){
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/accessCodes/getAccessCodes",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        render(resp);
    }
    public static void searchSellableEntity(){
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/accessCodes/getSellableItems",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }
    public static void addRemoveDeviceId(){
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/accessCodes/deviceManagement",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }
}
