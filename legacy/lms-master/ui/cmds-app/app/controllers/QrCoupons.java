package controllers;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import play.Logger;
import play.i18n.Messages;
import play.libs.F.Promise;
import pojos.UserOrg;
import uicom.util.ClientUtil;
import uicom.util.ResponseUtil;

public class QrCoupons extends AbstractQRUIController {

    public static void home(String orgId){
        recordActivity(ClientUtil.ActivityPages.COUPONS,ClientUtil.ActivityAction.OPEN);
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        if(Widgets._amISuperAdmin(currentOrgInfo)){
            String openPageId = request.params.get("openPageId");
            openPageId = openPageId == null || openPageId.isEmpty() ? "active" : openPageId;
            render("QrCoupons/couponsBoard.html",openPageId);
        }else{
            String msg = Messages.get("PAGE_ACCESS_DENIED");
            render("UIComRegister/msgPage.html",msg);
        }
    }

    public static void direct(String orgId){
        Logger.log4j.info("In Coupons direct method");
        recordActivity(ClientUtil.ActivityPages.COUPONS,ClientUtil.ActivityAction.OPEN);
        String includeName="QrCoupons/couponsBoard.html";
        flash.put("ENTRY", "DIRECT");
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        if(Widgets._amISuperAdmin(currentOrgInfo)){
            String openPageId = request.params.get("openPageId");
            openPageId = openPageId == null || openPageId.isEmpty() ? "active" : openPageId;
            render("Application/mapper.html",includeName,currentOrgInfo,openPageId);
        }else{
            String msg = Messages.get("PAGE_ACCESS_DENIED");
            render("UIComRegister/msgPage.html",msg);
        }
    }

    public static void coupons(String orgId){
        Logger.log4j.info("In Coupons - coupons method");
        recordActivity(ClientUtil.ActivityPages.COUPONS,ClientUtil.ActivityAction.OPEN);
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        if(Widgets._amISuperAdmin(currentOrgInfo)){
            render();
        }else{
            String msg = Messages.get("PAGE_ACCESS_DENIED");
            render("UIComRegister/msgPage.html",msg);
        }
    }

    public static void fetchActive(String orgId) {
        Logger.log4j.info("In coupons - fetchActive method");
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.BILLING_WEB_SERVICE_URL + "/couponCodes/getActiveCodes",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        render(resp);
    }

    public static void createCouponPopup(String orgId){
        render();
    }

    public static void postNewCoupon() {
        Logger.log4j.info("In coupons - postNewCoupon");
        recordActivity(ClientUtil.ActivityPages.COUPONS,ClientUtil.ActivityAction.ADD);
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.BILLING_WEB_SERVICE_URL + "/couponCodes/addCouponCode",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void updateCoupon() {
        recordActivity(ClientUtil.ActivityPages.COUPONS,ClientUtil.ActivityAction.EDIT);
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.BILLING_WEB_SERVICE_URL + "/couponCodes/updateCouponCode",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void deleteCoupon() {
        recordActivity(ClientUtil.ActivityPages.COUPONS,ClientUtil.ActivityAction.DELETE);
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.BILLING_WEB_SERVICE_URL + "/couponCodes/deleteCouponCode",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void addEditCouponPopup() {
        recordActivity(ClientUtil.ActivityPages.COUPONS,ClientUtil.ActivityAction.OPEN);
        String code = request.params.get("code");
        String orgId = request.params.get("orgId");
        Logger.log4j.info("orgId is " + orgId);
        Logger.log4j.info("coupon Code is " + code);
        JSONObject couponInfo = null;
        if (StringUtils.isNotEmpty(code) && StringUtils.isNotEmpty(orgId)) {
            Map<String, Object> allParams = getReqParams();
            allParams.put("code", code);
            allParams.put("orgId", orgId);
            couponInfo  = _getCouponInfo(allParams);
        }
        render(couponInfo);
    }

    private static JSONObject _getCouponInfo(Map<String, Object> allParams) {
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.BILLING_WEB_SERVICE_URL + "/couponCodes/getCouponCode",
                allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }
}
