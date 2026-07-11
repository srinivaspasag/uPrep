package controllers;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import play.Logger;
import play.libs.F;
import play.mvc.Scope.Params;
import uicom.util.ClientUtil;
import uicom.util.ResponseUtil;

/**
 *
 * @author amrita
 */

public class UIComNotification extends AbstractUIController {

	protected static JSONObject _getRegIds(Map<String, Object> allParams1) {
		Params allParams = request.params;
        String startStr = (String) allParams.get("start");
        if (startStr == null || StringUtils.isEmpty(startStr)) {
            allParams.put("start", "0");
        }
        F.Promise<JSONResponseWrapper> promise = client(ClientUtil.CMDS_SERVICE_URL
                + "/notifications/getRegIds", null);
        Logger.log4j.info("BEFORE AWAIT");
        //await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }
}