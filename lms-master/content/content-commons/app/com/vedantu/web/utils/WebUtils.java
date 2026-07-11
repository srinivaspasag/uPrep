package com.vedantu.web.utils;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.Logger.ALogger;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;

public class WebUtils {

	private static final ALogger LOGGER = Logger.of(WebUtils.class);

	public static JSONObject getJSONData(String url) {
		JSONObject json = null;
		try {
			json = new JSONObject(getStringData(url));
		} catch (JSONException e) {
			LOGGER.error(e.getMessage(), e);
		}

		return json;
	}

	public static String getStringData(String url) {
		return getStringData(url, new String[] {});
	}

	public static String getStringData(String url, String... params) {
		AsyncHttpClient asynClient = new AsyncHttpClient();
		Response r = null;
		String fbResponse = null;

		Future<Response> rsp;
		try {
			LOGGER.info("url requested is : " + url);
			rsp = asynClient.prepareGet(url).addHeader("Referer", "https://learnpedia.in").execute();
			r = rsp.get();
			fbResponse = r.getResponseBody();
			LOGGER.info("respobse is : " + fbResponse);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (InterruptedException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (ExecutionException e) {
			LOGGER.error(e.getMessage(), e);
		} finally {
			asynClient.close();
		}
		return fbResponse;
	}
}
