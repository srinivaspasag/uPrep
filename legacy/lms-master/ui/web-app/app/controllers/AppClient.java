package controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.libs.F.Promise;
import play.libs.WS;
import play.mvc.Controller;

public class AppClient extends Controller {

    private class JSONResponseWrapper {

        private JSONObject jsonResponse;

        void setResponse(JSONObject jsonResponse) {
            this.jsonResponse = jsonResponse;
        }

        JSONObject getResponse() {
            return this.jsonResponse;
        }
    }

    public JSONObject client(String actionUrl, Map<String, String[]> reqParams) {

        Map<String, String> sessionParams = new HashMap<String, String>();
        sessionParams.put("appId", "12345");
        sessionParams.put("username", session.get("username"));
        sessionParams.put("userId", session.get("userId"));
        sessionParams.put("userSessionAuthKey", session.get("userSessionAuthKey"));
        sessionParams.put("authKey", session.get("authKey"));

        Map<String, Object> allReqParams = convert(sessionParams, reqParams);

        Logger.log4j.info("sending keys:" + reqParams);
        Logger.log4j.info("sending allReqParams:" + allReqParams);

        final JSONResponseWrapper result = new JSONResponseWrapper();
        final long startTime = System.currentTimeMillis();
        final Promise<WS.HttpResponse> asyncResponse = WS.url(actionUrl).params(allReqParams).postAsync();
        Logger.log4j.info("called postAsync");

        Logger.log4j.info("will await response");

        List<WS.HttpResponse> httpResponses = await(Promise.waitAll(asyncResponse));
        if(null != httpResponses && !httpResponses.isEmpty()) {
            try {
                    Logger.log4j.info("wait over : " + (System.currentTimeMillis() - startTime) + "ms");

                    WS.HttpResponse httpResponse = null;
                    try {
                        httpResponse = httpResponses.get(0);
                    } catch (Exception e) {
                        Logger.log4j.error(e);
                    }
                    if (null != httpResponse) {
                        String rspString = httpResponse.getString();
                        JSONObject jsonResponse = new JSONObject(rspString);
                        Logger.log4j.info("Response" + jsonResponse);
                        result.setResponse(jsonResponse);
                    }

                } catch (JSONException ex) {
                    java.util.logging.Logger.getLogger(AppClient.class.getName()).log(Level.SEVERE, null, ex);
                }
        } else {
            Logger.log4j.error("no httpResponses");
        }

        Logger.log4j.info("will return response");

        return result.getResponse();
    }

    public static Map<String, Object> convert(Map<String, String> sessionParams, Map<String, String[]> reqParams) {
        Map<String, Object> convertedParams = new HashMap<String, Object>();
        if (null != sessionParams && !sessionParams.isEmpty()) {
            for (Entry<String, String> entry : sessionParams.entrySet()) {
                String value = entry.getValue();
                if (null != value) {
                    convertedParams.put(entry.getKey(), value);
                }
            }
        }
        if (null != reqParams && !reqParams.isEmpty()) {
            for (Entry<String, String[]> entry : reqParams.entrySet()) {
                List<String> value = null != entry.getValue() ? Arrays.asList(entry.getValue()) : null;
                if (null != value) {
                    convertedParams.put(entry.getKey(), value);
                }
            }
        }
        return convertedParams;
    }

    public static List<JSONObject> getListIterable(JSONObject jsonResponse, String jArray){
        JSONArray ja=null;List<JSONObject> jsonResponseArray=null;
        try {
             jsonResponseArray = new ArrayList<JSONObject>();
             ja = jsonResponse.getJSONArray(jArray);
            if (null != ja && ja.length() > 0) {
                for (int i = 0; i < ja.length(); i++) {
                    jsonResponseArray.add(ja.getJSONObject(i));
                }
            }
            
        } catch (JSONException e) {
            Logger.log4j.error(e.getMessage());
        }
        return jsonResponseArray;
    }

}
