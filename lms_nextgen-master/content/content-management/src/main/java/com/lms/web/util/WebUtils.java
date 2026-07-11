package com.lms.web.util;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Response;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class WebUtils {

	private static final Logger logger = LoggerFactory.getLogger(WebUtils.class);

	public static JSONObject getJSONData(String url) {
		JSONObject json = null;
		try {
			json = new JSONObject(getStringData(url));
		} catch (JSONException e) {
			logger.error(e.getMessage(), e);
		}

		return json;
	}

	public static String getStringData(String url) {
		return getStringData(url, new String[]{});
	}

    public static String getStringData(String url, String... params) {
		AsyncHttpClient asynClient = Dsl.asyncHttpClient();
		Response r = null;
		String fbResponse = null;

		Future<Response> rsp;
		try {
			logger.info("url requested is : " + url);
			rsp = asynClient.prepareGet(url).addHeader("Referer", "https://learnpedia.in").execute();
			r = rsp.get();
			fbResponse = r.getResponseBody();
			logger.info("respobse is : " + fbResponse);
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		} catch (ExecutionException e) {
			logger.error(e.getMessage(), e);
		} finally {
			try {
				asynClient.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return fbResponse;
	}
}