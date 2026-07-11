package com.vedantu.ext.cmds.utils.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.vedantu.ext.cmds.db.datamanagers.OrgDataManager;
import com.vedantu.ext.cmds.db.models.Organization;
import com.vedantu.ext.cmds.utils.commons.ConstantGlobal;
import com.vedantu.ext.cmds.utils.config.Config;
import com.vedantu.ext.cmds.web.ReqAction;
import com.vedantu.ext.cmds.web.VedantuHttpResponse;

public class WebCommunicator {

    public static final String  KEY_ERROR_CODE    = "errorCode";
    public static final String  KEY_ERROR_MESSAGE = "errorMessage";
    public static final String  KEY_RESULT        = "result";
    public static final String  KEY_LIST          = "list";

    private static final String USER_AGENT        = "Mozilla/5.0";
    private static final Logger LOGGER            = Logger.getLogger("WebCommunicator");

    public static VedantuHttpResponse
            getResult(ReqAction reqAction, Map<String, Object> httpParams) {

        VedantuHttpResponse res = new VedantuHttpResponse();

        HttpPost httpPost = new HttpPost(reqAction.getUrl());
        httpPost.addHeader("User-Agent", USER_AGENT);
        HttpClient httpClient = new DefaultHttpClient();

        try {
            List<NameValuePair> reqParams = new ArrayList<NameValuePair>();
            for (Entry<String, Object> entry : httpParams.entrySet()) {
                if (entry.getValue() != null && entry.getKey() != null) {
                    reqParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()
                            .toString().trim()));
                }
            }
            addCommonRequiredParams(reqParams);
            LOGGER.debug("url: " + httpPost.getURI() + ", reqParams : " + reqParams);

            httpPost.setEntity(new UrlEncodedFormEntity(reqParams, "UTF-8"));
            int timeout = 5*60; // 5 mins in seconds
            org.apache.http.params.HttpParams httpClientParams = httpClient.getParams();
            org.apache.http.params.HttpConnectionParams.setConnectionTimeout(httpClientParams, timeout * 1000); // http.connection.timeout
            org.apache.http.params.HttpConnectionParams.setSoTimeout(httpClientParams, timeout * 1000); // http.socket.timeout

            HttpResponse httpResopnse = httpClient.execute(httpPost);

            if (httpResopnse != null) {
                res.responseCode = httpResopnse.getStatusLine().getStatusCode();
                if (httpResopnse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    res.errorCode = String.valueOf(httpResopnse.getStatusLine().getStatusCode());
                    res.errorMessage = httpResopnse.getStatusLine().getReasonPhrase();
                    return res;
                }
                HttpEntity entity = httpResopnse.getEntity();
                String resString = EntityUtils.toString(entity);
                LOGGER.debug("url: " + httpPost.getURI() + ", response : " + resString);
                JSONObject jsonRes = new JSONObject(resString);
                res.fromJSON(jsonRes);
                EntityUtils.consumeQuietly(entity);
            } else {
                res.responseCode = HttpStatus.SC_REQUEST_TIMEOUT;
            }

        } catch (Throwable e) {
            res.errorMessage = e.getMessage();
            LOGGER.error("Error in WebCommunicator when connecting to " + reqAction.getUrl());
            LOGGER.error("Requested Action is " + reqAction.name());
            LOGGER.error(e.getMessage(), e);
            httpPost.abort();
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
        return res;
    }

    private static void addCommonRequiredParams(List<NameValuePair> reqParams) {

        Organization org = OrgDataManager.INSTANCE.getOrganization();
        if (org != null) {
            reqParams.add(new BasicNameValuePair(ConstantGlobal.ORG_ID, org.id));
            reqParams.add(new BasicNameValuePair(ConstantGlobal.USER_ID, org.adminUserId));
            reqParams.add(new BasicNameValuePair(ConstantGlobal.AUTH_TOKEN, org.authToken));
            reqParams.add(new BasicNameValuePair(ConstantGlobal.SECRET_KEY, org.secretKey));
            reqParams.add(new BasicNameValuePair("callingUserId",  org.adminUserId));
        }
        reqParams.add(new BasicNameValuePair("callingAppId", Config.APP_ID));
        reqParams.add(new BasicNameValuePair("callingAppName", Config.APP_ID));
    }
}
