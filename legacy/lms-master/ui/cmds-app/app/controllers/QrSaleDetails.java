package controllers;

import java.util.Map;

import org.json.JSONObject;

import play.Logger;
import play.i18n.Messages;
import play.libs.F;
import pojos.UserOrg;
import uicom.util.ClientUtil;
import uicom.util.ResponseUtil;

public class QrSaleDetails extends AbstractQRUIController {

    public static void home(String orgId) {
        JSONObject resp = saleDetails(orgId);
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        if(Widgets._amISuperAdmin(currentOrgInfo)){
            String openPageId = request.params.get("openPageId");
            openPageId = openPageId == null || openPageId.isEmpty() ? "saledetails" : openPageId;
            render("QrSaleDetails/saleDetails.scala.html",openPageId, resp,currentOrgInfo);
        }else{
            String msg = Messages.get("PAGE_ACCESS_DENIED");
            render("UIComRegister/msgPage.html",msg);
        }
    }

    public static void direct(String orgId) {
        JSONObject resp = saleDetails(orgId);
        Map<String, Object> allParams = getReqParams();
        JSONObject orgInfo = QrAcadStr._getOrgInfo(allParams);
        String includeName="QrSaleDetails/saleDetails.scala.html";
        flash.put("ENTRY", "DIRECT");
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(request.params.get("orgId"));
        render("Application/mapper.html",includeName,orgInfo,currentOrgInfo,resp);
    }

    public static JSONObject saleDetails(String orgId) {
        Map<String, Object> allParams = getReqParams();
        allParams.put("orgId", orgId);
        F.Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.BILLING_WEB_SERVICE_URL + "/sales/getSaleDetailsDisplayInfo", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }

    public static void paymentItemsPopup(){
        render();
    }
}
