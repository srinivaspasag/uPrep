/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;
import org.json.JSONObject;

import play.Logger;
import play.libs.F.Promise;
import pojos.UserOrg;
import uicom.util.ClientUtil;
import uicom.util.ResponseUtil;
/**
 *
 * @author anirban
 */
public class QrPlans extends AbstractQRUIController{
    private static JSONObject _getPlans(){
        request.params.put("state", "ACTIVE");
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/licensing/getPlans", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject plansResp = ResponseUtil.checkResponse(getJSON(promise));
        return plansResp;
    }
    public static void orgPlans() {
        recordActivity(ClientUtil.ActivityPages.PLANS, ClientUtil.ActivityAction.OPEN);
        JSONObject plansResp = _getPlans();
        JSONObject orgInfo = QrAcadStr._getOrgInfo(null);
        render(plansResp,orgInfo);
    }
    public static void orgPlansDirect(String orgId) {
        recordActivity(ClientUtil.ActivityPages.PLANS, ClientUtil.ActivityAction.OPEN);
        JSONObject plansResp = _getPlans();
        JSONObject orgInfo = QrAcadStr._getOrgInfo(null);
        String includeName = "QrPlans/orgPlans.html";
        flash.put("ENTRY", "DIRECT");
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        if(currentOrgInfo!=null && "MANAGER".equals(currentOrgInfo.getOrgUserProfile())){
            render("Application/mapper.html", includeName, currentOrgInfo, plansResp, orgInfo);
        }else{
            error(404, "You do not have permission to view this page.");
        }
    }
}
