/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.Play;
import play.data.validation.Required;
import play.libs.F.Action;
import play.libs.F.Promise;
import play.libs.WS;
import play.mvc.Controller;
import play.mvc.Http.Header;
import play.mvc.Scope;
import play.vfs.VirtualFile;
import response.ErrorInfo;
import response.JSONResponse;
import util.ClientUtil;
import util.ResponseUtil;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import com.ning.http.multipart.FilePart;
import com.ning.http.multipart.StringPart;

public abstract class AbstractUIController extends Controller {

    protected static Map<String, String> getSessionParams() {

        Map<String, String> sessionParams = new HashMap<String, String>();
        sessionParams.put("callingApp", Play.configuration.getProperty("application.name"));
        sessionParams.put("callingAppId", Play.configuration.getProperty("application.id"));
        sessionParams.put("userId", session.get("userId"));
        sessionParams.put("callingUserId", session.get("callingUserId"));
        return sessionParams;
    }

    protected static Map<String, Object> getSession() {

        Map<String, String> sessionParams = getSessionParams();
        Map<String, Object> allParams = new HashMap<String, Object>();
        if (null != sessionParams && !sessionParams.isEmpty()) {
            Logger.log4j.info("sessionParams : " + sessionParams);
            for (Entry<String, String> entry : sessionParams.entrySet()) {
                String value = entry.getValue();
                if (null != value) {
                    allParams.put(entry.getKey(), value);
            	}
            }
        }
        return allParams;
    }

    protected static Map<String, Object> getReqParams() {

        Map<String, String[]> reqParams = Scope.Params.current().all();
        Map<String, Object> allParams = new HashMap<String, Object>();
        if (null != reqParams && !reqParams.isEmpty()) {
            StringBuilder sb = new StringBuilder("reqParams : {");
            boolean isFirst = true;
            for (Map.Entry<String, String[]> entry : reqParams.entrySet()) {
                if (!isFirst) {
                    sb.append(", ");
                }
                sb.append(entry.getKey()).append("=[")
                        .append(StringUtils.join(entry.getValue(), ",")).append("]");
                isFirst = false;
            }
            sb.append("}");
            // Logger.log4j.info(sb.toString());
            for (Entry<String, String[]> entry : reqParams.entrySet()) {
                List<String> value = null != entry.getValue() ? Arrays.asList(entry.getValue())
                        : null;
                if (null != value) {
                    allParams.put(entry.getKey(), value);
                }
            }
        }
        String callingApp = Play.configuration.getProperty("application.name");
        String callingAppId = Play.configuration.getProperty("application.id");
        if (StringUtils.isEmpty(callingAppId)) {
            callingAppId = callingApp;
        }
        if (StringUtils.isNotEmpty(callingApp) && !allParams.containsKey("callingApp")) {
            allParams.put("callingApp", callingApp);
        }
        if (StringUtils.isNotEmpty(callingAppId) && !allParams.containsKey("callingAppId")) {
            allParams.put("callingAppId", callingAppId);
        }
        return allParams;
    }

    protected static Promise<JSONResponseWrapper> client(final String actionUrl,
            Map<String, Object> allParams) {

        return client(actionUrl, allParams, null, true);
    }
    protected static Promise<JSONResponseWrapper> client(final String actionUrl,
            Map<String, Object> allParams, Map<String, String> allHeaders) {

        return client(actionUrl, allParams, allHeaders, true);
    }

    protected static Promise<JSONResponseWrapper> client(final String actionUrl,
            Map<String, Object> reqParams, Map<String, String> allHeaders, Boolean logParams) {

        Logger.log4j.info("requesting for :: [" + actionUrl + "]");

        Map<String, Object> inputParams = null != reqParams ? reqParams : getReqParams();
        Map<String, Object> allReqParams = inputParams;
        if(allReqParams == null){
            allReqParams = new HashMap<String, Object>();
        }
        allReqParams.putAll(getSession());

        if (logParams) {
            Logger.log4j
                    .info("request :: [" + actionUrl + "] sending allReqParams:" + allReqParams);
        }
        if(allHeaders == null){
            allHeaders = new HashMap<String, String>();
        }
        final JSONResponseWrapper jsonResponseWrapper = new JSONResponseWrapper();
        final Promise<WS.HttpResponse> asyncResponse = WS.url(actionUrl).params(allReqParams).headers(allHeaders).timeout("5min")
                .postAsync();
        Logger.log4j.info("called postAsync");

        final Promise<JSONResponseWrapper> promise = new Promise<JSONResponseWrapper>();

        asyncResponse.onRedeem(new Action<Promise<WS.HttpResponse>>() {

            public void invoke(Promise<WS.HttpResponse> t) {

                JSONObject json = null;
                try {
                    json = toJSON(t.get());
                    Logger.log4j.info("response :: [" + actionUrl + "] (status="
                            + t.get().getStatus() + ")==> " + json);
                } catch (Exception e) {
                    Logger.log4j.error(e);
                }

                jsonResponseWrapper.setJSON(json);

                Logger.log4j.info("request time taken :: [" + actionUrl + "] = "
                        + jsonResponseWrapper.timeTaken() + "ms");
                promise.invoke(jsonResponseWrapper);
            }
        });

        return promise;
    }

    protected static Promise<JSONResponseWrapper> clientForExternalUrl(final String actionUrl,
            Map<String, String> allHeaders, JSONObject data,  Boolean logParams) {

        Logger.log4j.info("requesting for :: [" + actionUrl + "]");
        Map<String, Object> inputParams = getReqParams();
        Map<String, Object> allReqParams = inputParams;
        if(allReqParams == null){
            allReqParams = new HashMap<String, Object>();
        }
        allReqParams.putAll(getSession());
        if (logParams) {
            Logger.log4j
                    .info("request :: [" + actionUrl + "] allReqParams:" + allReqParams);
        }
        String message = allReqParams.get("message").toString();
        message = message.substring(1, message.length()-1);
        JSONObject msg = new JSONObject();
        try{
        msg.put("Message", message);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        try {
        if(!data.has("data"))
            data.put("data",msg);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        if (logParams) {
            Logger.log4j
                    .info("request :: [" + actionUrl + "] sending data:" + data.toString());
        }

        if(allHeaders == null){
            allHeaders = new HashMap<String, String>();
            allHeaders.put("Content-Type", "application/json");
            allHeaders.put("Authorization","key=__GOOGLE_API_KEY_REDACTED__");
        }

        final JSONResponseWrapper jsonResponseWrapper = new JSONResponseWrapper();
        final Promise<WS.HttpResponse> asyncResponse = WS.url(actionUrl).headers(allHeaders).body(data).timeout("5min")
                .postAsync();
        Logger.log4j.info("called postAsync");

        final Promise<JSONResponseWrapper> promise = new Promise<JSONResponseWrapper>();

        asyncResponse.onRedeem(new Action<Promise<WS.HttpResponse>>() {

            public void invoke(Promise<WS.HttpResponse> t) {

                JSONObject json = null;
                try {
                    json = toJSON(t.get());
                    Logger.log4j.info("response :: [" + actionUrl + "] (status="
                            + t.get().getStatus() + ")==> " + json);
                } catch (Exception e) {
                    Logger.log4j.error(e);
                }

                jsonResponseWrapper.setJSON(json);

                Logger.log4j.info("request time taken :: [" + actionUrl + "] = "
                        + jsonResponseWrapper.timeTaken() + "ms");
                promise.invoke(jsonResponseWrapper);
            }
        });

        return promise;
    }


    protected static JSONObject toJSON(WS.HttpResponse httpResponse) {

        JSONObject jsonResponse = null;
        try {
            if (null != httpResponse) {
                String rspString = httpResponse.getString();
                // Logger.log4j.debug("response string: " + rspString);
                jsonResponse = new JSONObject(rspString);
            }

        } catch (JSONException e) {
            Logger.log4j.error(e);
        }
        return jsonResponse;
    }

    protected static JSONObject getJSON(Promise<JSONResponseWrapper> promise) {

        JSONObject j = null;
        try {
            j = promise.get().getJSON();
        } catch (Exception e) {
            Logger.log4j.error(e);
        }
        return j;
    }

    protected static class JSONResponseWrapper {

        private long       startMillis;
        private long       endMillis;
        private JSONObject json;

        protected JSONResponseWrapper() {

            startMillis = System.currentTimeMillis();
        }

        protected void setJSON(JSONObject json) {

            this.json = json;
            endMillis = System.currentTimeMillis();
        }

        protected JSONObject getJSON() {

            return this.json;
        }

        protected long timeTaken() {

            return endMillis - startMillis;
        }
    }

    protected static String getUploadPath() {

        return Play.getFile("").getAbsolutePath() + File.separator + "uploads";
    }

    private static File makeFile() {

        File file = null;
        String UPLOAD_PATH = getUploadPath();
        Logger.log4j.info("starting the upload progress");
        if (request.isNew) {
            FileOutputStream moveTo = null;

            // Another way I used to grab the name of the file
            String filename = request.headers.get("x-file-name").value();
            Logger.info("Absolute on where to send %s", UPLOAD_PATH + File.separator);
            InputStream data = null;
            try {
                data = request.body;
                File up = new File(UPLOAD_PATH);
                if (!up.exists()) {
                    Logger.log4j.info("making directory " + up.getAbsolutePath() + ", result is : "
                            + up.mkdir());
                }
                File inputDoc = new File(UPLOAD_PATH + File.separator + filename);
                moveTo = new FileOutputStream(inputDoc);
                Logger.log4j.info("copying file to local system");
                IOUtils.copy(data, moveTo);
                Logger.log4j.info("file copied to local system : " + inputDoc.getAbsolutePath());
                moveTo.close();
                file = inputDoc;
            } catch (Exception ex) {
                Logger.log4j.error(ex.getMessage(), ex);
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
        return file;
    }

    protected static JSONObject uploadUtil(String url, Map<String, String[]> reqParams, File file) {

        if (file == null) {
            file = makeFile();
        }

        JSONObject jsonResponse = new JSONObject();

        Map<String, String[]> scopeParams = Scope.Params.current().all();
        Map<String, String[]> inputParams = null != reqParams ? reqParams : scopeParams;

        String uploadFileParamName = request.params.get("uploadFileParamName");
        if (request.isNew) {
            try {
                AsyncHttpClient asynClient = new AsyncHttpClient();

                AsyncHttpClient.BoundRequestBuilder reqBuilder = appendReqParams(
                        asynClient.preparePost(url), inputParams);
                Future<Response> response = reqBuilder
                        .addHeader("Content-Type",
                                "multipart/form-data;boundary=randomBoundaryNotInAnyOfParts")
                        .addBodyPart(new FilePart(uploadFileParamName, file)).execute();
                Response r = null;

                try {
                    r = response.get();
                    String rspString = r.getResponseBody();
                    jsonResponse = new JSONObject(rspString);
                    Logger.log4j.info("response for upload" + jsonResponse);
                } catch (InterruptedException e) {
                    Logger.log4j.error(e.getMessage(), e);
                } catch (ExecutionException e) {
                    Logger.log4j.error(e.getMessage(), e);
                } catch (JSONException e) {
                    Logger.log4j.error(e.getMessage(), e);
                } finally {
                    asynClient.close();
                }

            } catch (Exception ex) {
                Logger.log4j.error(ex.getMessage(), ex);
                jsonResponse = new JSONObject(new JSONResponse(new ErrorInfo("UPLOAD_FILED","upload failed due to local")));
            } finally {
                if (file != null && file.exists()) {
                    Logger.log4j
                            .info("uploaded file got deleted from local dir : " + file.delete());
                }
            }

        }
        return jsonResponse;
    }

    protected static AsyncHttpClient.BoundRequestBuilder appendReqParams(
            AsyncHttpClient.BoundRequestBuilder reqBuilder, Map<String, String[]> reqParams) {

        // adding input params
        if (null != reqParams && !reqParams.isEmpty()) {
            for (String key : reqParams.keySet()) {
                String[] value = reqParams.get(key);
                if (null != value && value.length > 0) {
                    if (value.length > 1) {
                        for (String v : value) {
                            reqBuilder.addBodyPart(new StringPart(key, v));
                        }
                    } else if (value[0] != null) {
                        reqBuilder.addBodyPart(new StringPart(key, value[0]));
                    }
                }
            }
        }

        // adding session params
        Map<String, String> sessionParams = getSessionParams();
        if (null != sessionParams && !sessionParams.isEmpty()) {
            Logger.log4j.info("sessionParams : " + sessionParams);
            for (Entry<String, String> entry : sessionParams.entrySet()) {
                String value = entry.getValue();
                if (null != value) {
                    reqBuilder.addBodyPart(new StringPart(entry.getKey(), value));
                        }
                    }
                }
        return reqBuilder;
    }

    public static JSONObject syncCaller(String url, Map<String, Collection<String>> reqParams) {

        Logger.log4j.info("requesting for :: [" + url + "]");

        Map<String, Collection<String>> inputParams = null != reqParams ? reqParams
                : getReqParamsForSyncCaller();

        Map<String, Collection<String>> allReqParams = inputParams;
        if(allReqParams == null){
            allReqParams = new HashMap<String, Collection<String>>();
        }
        allReqParams.putAll(getSessionParamsForSyncCaller());

        Logger.log4j.info("request :: [" + url + "] sending allReqParams:" + allReqParams);
        JSONObject resp = null;
        try {
            AsyncHttpClient asynClient = new AsyncHttpClient();
            Future<Response> rsp = asynClient.preparePost(url).setParameters(allReqParams)
                    .execute();
            Response r = null;
            try {
                r = rsp.get();
                resp = ResponseUtil.checkResponse(new JSONObject(r.getResponseBody()));
                Logger.log4j.info("response :: [" + url + "] (status=" + rsp.get().getStatusCode()
                        + ")==> " + resp);
            } catch (InterruptedException e) {
                Logger.log4j.error(e.getLocalizedMessage());
            } catch (ExecutionException e) {
                Logger.log4j.error(e.getLocalizedMessage());
            } catch (JSONException e) {
                Logger.log4j.error(e.getLocalizedMessage());
            } finally {
                asynClient.close();
            }
        } catch (IOException ex) {
            Logger.log4j.info("error" + ex.getMessage());
            resp = new JSONObject(new JSONResponse("", "UNKNOWN_ERROR", "UNKNOWN_ERRROR"));
        }
        return resp;
    }

    private static Map<String, Collection<String>> getReqParamsForSyncCaller() {

        Map<String, String[]> params = Scope.Params.current.get().all();
        Map<String, Collection<String>> allParams = new HashMap<String, Collection<String>>();
        if (null != params && !params.isEmpty()) {
            for (String key : params.keySet()) {
                String[] value = params.get(key);
                if (null != value && value.length > 0) {
                    allParams.put(key, Arrays.asList(value));
                }
            }
        }
        return allParams;
    }

    private static Map<String, Collection<String>> getSessionParamsForSyncCaller() {

        Map<String, String> sessionParams = getSessionParams();
        Map<String, Collection<String>> allParams = new HashMap<String, Collection<String>>();
        if (null != sessionParams && !sessionParams.isEmpty()) {
            Logger.log4j.info("sessionParams : " + sessionParams);
            for (Entry<String, String> entry : sessionParams.entrySet()) {
                String value = entry.getValue();
                if (null != value) {
                    allParams.put(entry.getKey(), Arrays.asList(value));
                }
            }
        }
        return allParams;
    }

    protected static void recordActivity(@Required ClientUtil.ActivityPages page,
            @Required ClientUtil.ActivityAction action) {

        _recordActivity(page, action, null, null);
    }

    protected static void recordActivity(@Required ClientUtil.ActivityPages page,
            @Required ClientUtil.ActivityAction action, ClientUtil.Entity srcType, String srcId) {

        _recordActivity(page, action, srcType, srcId);
    }

    private static void _recordActivity(@Required ClientUtil.ActivityPages page,
            @Required ClientUtil.ActivityAction action, ClientUtil.Entity srcType, String srcId) {

        Map<String, Object> allParams = getReqParams();
        allParams.put("page", page);
        allParams.put("userAction", action);
        allParams.put("deviceType", "WEB");
        if (srcType != null && srcId != null) {
            allParams.put("entity.type", srcType);
            allParams.put("entity.id", srcId);
        }
        allParams.put("deviceId", session.getId());
        allParams.put("userId", session.get("callingUserId"));
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/activityLogger/record", allParams);
        Logger.log4j.info("ACTIVITY SUBMITTED");
    }

    protected static Promise<JSONResponseWrapper> clientWithAllHeaders(String url,
            Map<String, Object> allParams, Map<String, String> extraHeaders){

        Map<String, String> newHeaders = new HashMap<String, String>();//session.all();
        if(extraHeaders != null){
            newHeaders.putAll(extraHeaders);
        }
        Map<String, Header> allHeaders = request.headers;
        for (Map.Entry<String, Header> key : allHeaders.entrySet()) {
            newHeaders.put(key.getKey(), key.getValue().value());
        }
        Logger.log4j.info("ALL HEADERS ============= "+newHeaders);
        return client(url, allParams, newHeaders);
    }

    public static String renderTheme(String fileName){
        return renderTheme(null, fileName);
    }

    public static String renderTheme(String orgId, String fileName){
        int themeNumber = 0;
        if(session.contains("isNewUI")){
            if(Boolean.parseBoolean(session.get("isNewUI"))){
                themeNumber = Integer.parseInt(session.get("theme"));
            }
        }
        String theme = themeNumber == 0 ? "theme1/" : "theme"+themeNumber+"/";
        VirtualFile vf = VirtualFile.fromRelativePath("/app/views/"+theme+fileName);
        File f = vf.getRealFile();
        if (!f.isFile()) {
            // The file does not exist.
            return "theme1/"+fileName;
        } else {
            // The file exist.
            return theme+fileName;
        }
    }

    public static String getHTMLFilePath(String className){
        return getHTMLFilePath(className, null);
    }

    public static String getHTMLFilePath(String className, String fileName){
        if(fileName == null){
            fileName = Thread.currentThread().getStackTrace()[3].getMethodName();
        }
        if(className == null){
            return fileName+".html";
        }else{
            return className+"/"+fileName+".html";
        }
    }
}
