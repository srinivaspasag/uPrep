package controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.Play;
import play.libs.F.Promise;
import play.mvc.Http;
import play.mvc.Router;
import play.mvc.Router.Route;
import pojos.UserOrg;
import uicom.util.ClientUtil;
import uicom.util.ResponseUtil;

public class Application extends AbstractQRUIController {
//    public static void home(){
//        render();
//    }
    public static void newfe(){
        render();
}
    public static void fe(){
            render();
    }
    public static void pdf(){
            render();
    }
    public static void syntaxHighlighter(){
            render();
    }
    public static void testtake(){
            render();
    }
    public static void index() throws JSONException{

    }
    public static void jsloader(){
            render();
    }
    public static void homeContent(){
            render();
    }
    public static void buttons(){
        JSONArray a=new JSONArray();
            render();
    }


    public static void notEligible(){
            render();
    }
    public static void noOrgsFound(){
            render();
    }
    public static void errorInRootFolder(){
            render();
    }
    public static void grapher(){
            render();
    }
    public static void test(){
//        Route r= Router.route(request);
       // r=Router.route(request);
    //    Logger.log4j.info(r);
        //Logger.log4j.info(Router.routes);
        render();
        Http.Request req=new Http.Request().createRequest(null,"GET","/", null, null, null, null, null, true, 80, null, true, null, null);
        Route r= Router.route(req);
        Route defaultRoute=Router.routes.get(Router.routes.size()-1);
        Boolean isValidRoute=Router.routes.contains(r)&&!defaultRoute.equals(r);
        render();
    }
    public static void testing(){
//        if(Play.mode.isDev()){
//            render();
//        }else{
//            error(404, "You do not have permission to view this page.");
//        }
        render();
    }
    private static JSONObject _getAppSysInfo() {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CMDS_APP_URL
                + "/sysinfo/get", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        resp = ResponseUtil.checkResponse(resp);
        return resp;
    }
    public static void getAboutAppPopup(String orgId) throws JSONException{
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        String instLogo = currentOrgInfo.getOrgThumbnail();
        instLogo = uicom.util.Utilities.getInstLogo(instLogo);
        JSONObject sysInfo = _getAppSysInfo();
        String buildVer = sysInfo.getJSONObject("result").getString("buildVersion");
        render("UIComTags/aboutApp.html",instLogo,buildVer);
    }
    public static void makeFile() throws JSONException {
        String qqfile = request.params.get("qqfile");
        String UPLOAD_PATH = getUploadPath();
        String filename = request.headers.get("x-file-name").value();
        JSONObject resp = new JSONObject();
        if (request.isNew) {

            FileOutputStream moveTo = null;

            Logger.info("Name of the file %s", qqfile);

            Logger.info("Absolute on where to send %s", Play.getFile("").getAbsolutePath()
                    + File.separator + "uploads" + File.separator);
            InputStream data = null;
            try {

                data = request.body;
                File inputDoc = new File(UPLOAD_PATH + File.separator + filename);
                moveTo = new FileOutputStream(inputDoc);
                IOUtils.copy(data, moveTo);
            } catch (Exception ex) {
                Logger.log4j.error(ex.getMessage(), ex);
                resp.put("success", false);
                renderJSON(resp.toString());
            } finally {
                if (data != null) {
                    Logger.log4j.info("closing data input stream");
                    try {
                        data.close();
                    } catch (Exception ex) {
                        Logger.log4j.error(ex.getMessage(), ex);
                    }
                }
            }

        }
        resp.put("success", true);
        resp.put("fileName", filename);
        resp.put("id", request.params.get("id"));
        resp.put("type", request.params.get("type"));
        renderJSON(resp.toString());
    }
    public static void uploadImage() {
        Logger.log4j.info("File Upload REQ Came =============================== ");
        String UPLOAD_PATH = getUploadPath();
        File inputDoc = new File(UPLOAD_PATH + File.separator + request.params.get("qqfile"));
        Logger.log4j.info("File Upload REQ Came =============================== "+inputDoc);
        JSONObject data = uploadUtil(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/uploads/uploadImage", null, inputDoc);
        renderJSON(data.toString());
    }
}