/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package controllers;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.Play;
import play.libs.F;
import play.mvc.With;
import uicom.util.ClientUtil;
import uicom.util.ResponseUtil;

import com.google.gson.Gson;

/**
 *
 * @author ajith
 */
@With(Security.class)
public class Widgets extends AbstractUIController {

    public static void sysInfo() {

        List<JSONObject> sysInfos = _getSysInfos();
        render(sysInfos);
    }

    public static void sysInfoLatest() {

        List<JSONObject> sysInfos = _getSysInfos();
        render(sysInfos);
    }

    public static List<JSONObject> _getSysInfos() {

        String[] backendPorts = { ClientUtil.LEARN_APP_URL, ClientUtil.WEB_APP_URL, ClientUtil.CMDS_APP_URL,
                ClientUtil.USER_SERVICE_URL, ClientUtil.ORGANIZATION_SERVICE_URL,
                ClientUtil.CONTENT_SERVICE_URL, ClientUtil.CMDS_SERVICE_URL,
                ClientUtil.BOARDS_SERVICE_URL, ClientUtil.COMM_SERVICE_URL,
                ClientUtil.SOCIALS_WEB_SERVICE_URL };
        List<JSONObject> sysInfos = new ArrayList<JSONObject>();
        for (int k = 0; k < backendPorts.length; k++) {
            JSONObject resp = _getSysInfo(backendPorts[k]);
            sysInfos.add(resp);
        }
        String thisAppName = Play.configuration.getProperty("application.name");
        String thisAppRespStr = new Gson().toJson(SysInfo._getSysInfo());
        JSONObject thisAppSysInfo = new JSONObject();
        try {
            thisAppSysInfo.put("errorCode", "");
            thisAppSysInfo.put("result", new JSONObject(thisAppRespStr));
            thisAppSysInfo.put("urlHit", thisAppName);
        } catch (JSONException ex) {
            Logger.log4j.error("Some error in putting url hit in response for " + thisAppName
                    + ". Exception: " + ex.getMessage());
        }
        sysInfos.add(thisAppSysInfo);
        return sysInfos;
    }

    private static JSONObject _getSysInfo(String urlHit) {

        Logger.log4j.info("Requesting for =====================>" + urlHit);
        F.Promise<JSONResponseWrapper> promise = client(urlHit + "/sysinfo/get", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        try {
            resp.put("urlHit", urlHit);
        } catch (JSONException ex) {
            Logger.log4j.error("Some error in putting url hit in response for " + urlHit
                    + ". Exception: " + ex.getMessage());
        }
        return resp;
    }
}
