package controllers;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import play.Logger;
import play.libs.F.Promise;
import play.mvc.Scope.Params;
import play.mvc.With;
import pojos.UserOrg;
import uicom.util.ClientUtil;
import uicom.util.ResponseUtil;
import enums.UserRole;

/**
 *
 * @author anirban
 */
@With(Security.class)
public class QrInvoices extends AbstractQRUIController {

    private static final String ordersType="INSTITUTE_SELL_ORDERS";
    private static JSONObject _getOrders(String orgId) {
        Params allParams = request.params;
        String startStr = (String) allParams.get("start");
        if (startStr == null || StringUtils.isEmpty(startStr)) {
            allParams.put("start", "0");
        }
        allParams.put("orgId", orgId);
        allParams.put("customer.id", orgId);
        allParams.put("customer.type", "ORGANIZATION");
        allParams.put("size", "12");
        Promise<JSONResponseWrapper> promise = client(ClientUtil.BILLING_WEB_SERVICE_URL
                + "/invoices/getBuyOrders", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }

    public static void orgInvoicesDirect(String orgId) {
        recordActivity(ClientUtil.ActivityPages.INVOICES, ClientUtil.ActivityAction.OPEN);
        Map<String, Object> allParams = getReqParams();
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        if (currentOrgInfo != null && "MANAGER".equals(currentOrgInfo.getOrgUserProfile())) {
            JSONObject orgInfo = QrAcadStr._getOrgInfo(allParams);
            JSONObject resp = _getOrders(orgId);
            String includeName = "QrInvoices/home.html";
            flash.put("ENTRY", "DIRECT");
            render("Application/mapper.html", includeName, resp, currentOrgInfo, orgInfo);
        } else {
            error(404, "You do not have permission to view this page.");
        }
    }

    public static void home(String orgId) {
        JSONObject resp = _getOrders(orgId);
        render(resp);
    }

    public static void sellerDashboard() {
        request.params.put("ordersType", ordersType);
        JSONObject ordersResp = ResponseUtil.checkResponse(null);
        if (StringUtils.equalsIgnoreCase(UserRole.MANAGER.name(),
                request.params.get("userRole"))) {
            ordersResp = UIComInvoices._getSellOrders(null);
        }
        render(ordersResp);
    }

    public static void ordersTable() {
        request.params.put("ordersType", ordersType);
        JSONObject ordersResp = ResponseUtil.checkResponse(null);
        if (StringUtils.equalsIgnoreCase(UserRole.MANAGER.name(),
                request.params.get("userRole"))) {
            ordersResp = UIComInvoices._getSellOrders(null);
        }
        render("UIComInvoices/ordersTable.html", ordersResp);
    }

    public static void sellerDashboardDirect(String orgId) {
        request.params.put("ordersType", ordersType);
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        JSONObject ordersResp = ResponseUtil.checkResponse(null);
        request.params.put("customer.id", orgId);
        request.params.put("customer.type", "ORGANIZATION");
        request.params.put("start", "0");
        request.params.put("size", "50");
        if (StringUtils.equalsIgnoreCase(UserRole.MANAGER.name(),
                currentOrgInfo.getOrgUserProfile())) {
            ordersResp = UIComInvoices._getSellOrders(null);
        }
        String includeName = "QrInvoices/sellerDashboard.html";
        flash.put("ENTRY", "DIRECT");
        render("Application/mapper.html", includeName, ordersResp, currentOrgInfo);
    }
}
