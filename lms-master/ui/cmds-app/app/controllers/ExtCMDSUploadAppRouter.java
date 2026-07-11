package controllers;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.libs.F;
import util.DesktopAppUrlFactory;
import util.TabAppUrlFactory;

public class ExtCMDSUploadAppRouter extends AbstractUIController {

    public static void getOrgInfo(String orgCmdsURL) {

        JSONObject json = __getJSONData(DesktopAppUrlFactory.INSTANCE.getServiceUrl("getOrgInfo"),
                getReqParams());
        renderJSON(json.toString());
    }

    public static void authenticate(boolean useGlobalUsername) {

        Logger.log4j.info("authenticate user with orgCredential : " + useGlobalUsername);
        Map<String, Object> reqParams = getReqParams();
        JSONObject jsonData = useGlobalUsername ? __getJSONData(
                TabAppUrlFactory.INSTANCE.getServiceUrl("authenticateUser"), reqParams)
                : __getJSONData(TabAppUrlFactory.INSTANCE.getServiceUrl("authenticateMember"),
                        reqParams);
        renderJSON(jsonData.toString());
    }

    public static void validateOrgAppCredentials() {

        JSONObject json = __getJSONData(
                DesktopAppUrlFactory.INSTANCE.getServiceUrl("validateOrgAppCredentials"),
                getReqParams());
        renderJSON(json.toString());
    }

    public static void createFolder() {

        JSONObject json = __getJSONData(
                DesktopAppUrlFactory.INSTANCE.getServiceUrl("createFolder"), getReqParams());
        renderJSON(json.toString());
    }

    public static void getFolders() {

        JSONObject json = __getJSONData(DesktopAppUrlFactory.INSTANCE.getServiceUrl("getFolders"),
                getReqParams());
        renderJSON(json.toString());
    }

    public static void getContentLinks() {

        JSONObject jsonData = __getJSONData(TabAppUrlFactory.INSTANCE
                .getServiceUrl("getContentLinks"));

        renderJSON(jsonData.toString());
    }

    public static void getRemovedContentLinks() {

        JSONObject jsonData = __getJSONData(DesktopAppUrlFactory.INSTANCE
                .getServiceUrl("getRemovedContentLinks"));

        renderJSON(jsonData.toString());
    }

    public static void getSDCardGroups() {

        JSONObject jsonData = __getJSONData(DesktopAppUrlFactory.INSTANCE
                .getServiceUrl("getSDCardGroups"));

        renderJSON(jsonData.toString());
    }

    public static void getSecureURL() {

        JSONObject jsonData = __getJSONData(DesktopAppUrlFactory.INSTANCE
                .getServiceUrl("getSecureURL"));

        renderJSON(jsonData.toString());
    }

    public static void getFileInfos() {

        JSONObject jsonData = __getJSONData(DesktopAppUrlFactory.INSTANCE
                .getServiceUrl("getFileInfos"));

        renderJSON(jsonData.toString());
    }

    public static void getSDCardGroupInfo() {

        JSONObject jsonData = __getJSONData(DesktopAppUrlFactory.INSTANCE
                .getServiceUrl("getSDCardGroupInfo"));

        renderJSON(jsonData.toString());
    }

    public static void getPrograms() {

        JSONObject jsonData = __getJSONData(DesktopAppUrlFactory.INSTANCE
                .getServiceUrl("getPrograms"));

        renderJSON(jsonData.toString());
    }

    public static void getCentersOfProgram() {

        JSONObject jsonData = __getJSONData(DesktopAppUrlFactory.INSTANCE
                .getServiceUrl("getCentersOfProgram"));

        renderJSON(jsonData.toString());
    }

    public static void getSectionsOfCenter() {

        JSONObject jsonData = __getJSONData(DesktopAppUrlFactory.INSTANCE
                .getServiceUrl("getSectionsOfCenter"));

        renderJSON(jsonData.toString());
    }

    public static void getLibrary() {

        JSONObject jsonData = __getJSONData(DesktopAppUrlFactory.INSTANCE
                .getServiceUrl("getLibrary"));

        renderJSON(jsonData.toString());
    }

    public static void getContents() {

        JSONObject jsonData = __getJSONData(DesktopAppUrlFactory.INSTANCE
                .getServiceUrl("getContents"));

        renderJSON(jsonData.toString());
    }

    private static JSONObject __getJSONData(String url) {

        return __getJSONData(url, getReqParams());
    }

    private static JSONObject __getJSONData(String url, Map<String, Object> allParams) {

        Logger.log4j.info("fetching json data from backend server url:" + url);
        JSONObject jsonResponse = null;
        try {
            F.Promise<AbstractUIController.JSONResponseWrapper> promise = client(url, allParams);
            Logger.log4j.info("BEFORE AWAIT");
            await(promise);
            Logger.log4j.info("AFTER AWAIT");
            jsonResponse = getJSON(promise);
            if (jsonResponse == null) {
                Logger.error("no response from backend server : " + jsonResponse);
                jsonResponse = new JSONObject();
                jsonResponse.put("errorCode", "SERVICE_ERROR");
            }
        } catch (IllegalArgumentException e) {
            Logger.log4j.error(e.getMessage(), e);
        } catch (JSONException e) {
            Logger.log4j.error(e.getMessage(), e);
        }
        return jsonResponse;
    }
}
