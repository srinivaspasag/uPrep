package controllers;

import java.util.Map;

import org.json.JSONObject;

import play.Logger;
import play.i18n.Messages;
import play.libs.F;
import pojos.UserOrg;
import uicom.util.ClientUtil;
import uicom.util.ResponseUtil;

public class QrBilling extends AbstractQRUIController{

    public static void home(String orgId) {
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        if(Widgets._amISuperAdmin(currentOrgInfo)){
            String openPageId = request.params.get("openPageId");
            openPageId = openPageId == null || openPageId.isEmpty() ? "billing" : openPageId;
            render("QrBilling/billingDashboard.scala.html",openPageId,currentOrgInfo);
        }else{
            String msg = Messages.get("PAGE_ACCESS_DENIED");
            render("UIComRegister/msgPage.html",msg);
        }
    }

    public static void direct(String orgId) {
        Map<String, Object> allParams = getReqParams();
        JSONObject orgInfo = QrAcadStr._getOrgInfo(allParams);
        String includeName="QrBilling/billingDashboard.scala.html";
        flash.put("ENTRY", "DIRECT");
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(request.params.get("orgId"));
        render("Application/mapper.html",includeName,orgInfo,currentOrgInfo);
    }

    public static void billingDetails() {
        Map<String, Object> allParams = getReqParams();
        F.Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/members/getStudentsCount", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        render("QrBilling/billingTable.html",resp);
    }
}
