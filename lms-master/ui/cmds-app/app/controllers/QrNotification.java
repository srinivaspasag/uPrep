package controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonParser;
import com.mysql.jdbc.log.Log;

import controllers.AbstractUIController.JSONResponseWrapper;
import play.Logger;
import play.i18n.Messages;
import play.libs.F;
import play.libs.F.Promise;
import play.mvc.Scope;
import pojos.UserOrg;
import uicom.util.ClientUtil;
import uicom.util.ResponseUtil;

/**
 *
 * @author amrita
 */
public class QrNotification extends AbstractQRUIController {

	public static void home(String orgId) {
		UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
		if (Widgets._amISuperAdmin(currentOrgInfo)) {
			String openPageId = request.params.get("openPageId");
			openPageId = openPageId == null || openPageId.isEmpty() ? "notification"
					: openPageId;
			render("QrNotification/notification.html", openPageId);
		} else {
			String msg = Messages.get("PAGE_ACCESS_DENIED");
			render("UIComRegister/msgPage.html", msg);
		}
	}

	public static void sendToGCMDirect() {
		// for getRegIDs
		List<String> regIds = new ArrayList<String>();
		JSONArray list = new JSONArray();
		JSONObject jsonResp = new JSONObject();
		JSONObject resp = new JSONObject();
		JSONObject result = new JSONObject();
		jsonResp = UIComNotification._getRegIds(null);
		// extract regIds from JSONObject , it has field called list.
		int totalHits = 0;
		try {
			jsonResp = jsonResp.getJSONObject("result");
			totalHits = jsonResp.getInt("totalHits");
			list = jsonResp.getJSONArray("list");
			for (int i = 0; i < list.length(); i++) {
				jsonResp = list.getJSONObject(i);
				regIds.add(jsonResp.getString("regId"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		// if no regIds found for the program
		if (totalHits == 0) {
			try {
				result.put("success", "failure");
				resp.put("result", result);
				resp.put("errorCode", "");
				renderJSON(resp.toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		Logger.log4j.info("JSOn Data for regId is: " + regIds.toString());

		JSONObject data = new JSONObject();
		try {
			data.put("registration_ids", regIds);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		F.Promise<AbstractUIController.JSONResponseWrapper> promise = clientForExternalUrl(
				ClientUtil.GCM_URL + "/gcm/send", null, data, true);
		Logger.log4j.info("BEFORE AWAIT");
		await(promise);
		Logger.log4j.info("AFTER AWAIT");
		JSONObject response = getJSON(promise);
		Logger.log4j.info("Amrita response =" + response.toString());
		try {
			result.put("success", "success");
			result.put("totalHits", response.getString("success"));
			result.put("failure", response.getString("failure"));
			result.put("list", response.getJSONArray("results"));
			result.put("cumulativeErrorCode", "null");
			resp.put("result", result);
			resp.put("errorCode", "");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		renderJSON(resp.toString());
	}

	public static void direct() {
		JSONObject orgInfo = _getOrgInfo(null);
		String includeName = "QrNotification/notification.html";
		flash.put("ENTRY", "DIRECT");
		UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(request.params
				.get("orgId"));
		render("Application/mapper.html", includeName, orgInfo, currentOrgInfo);
	}

	protected static JSONObject _getOrgInfo(Map<String, Object> allParams) {

		F.Promise<AbstractUIController.JSONResponseWrapper> promise = client(
				ClientUtil.ORGANIZATION_SERVICE_URL
						+ "/organizations/getOrganization", allParams);
		Logger.log4j.info("BEFORE AWAIT");
		await(promise);
		Logger.log4j.info("AFTER AWAIT");
		JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
		return resp;
	}

	public static void sendFirebaseNotification(Map<String, Object> allParams) {
		if (play.Play.configuration.getProperty("environment")
				.equalsIgnoreCase("prod")) {
			allParams = getReqParams();
			Logger.log4j.info("all params " + allParams);
			JSONObject orgInfo = _getOrgInfo(null);
			String slug = StringUtils.EMPTY;
			String firebaseKey = StringUtils.EMPTY;
			String FCMResponse = "{}";
			JSONObject resp = new JSONObject();
			JSONObject FCMResponseObj;
			// Building FCM body JSON
			JSONObject FCMInput = new JSONObject();
			// Building FCM data JSON
			JSONObject FCMData = new JSONObject();
			try {
				slug = orgInfo.getJSONObject("result").getString("slug");

				// Add common header for FCM api
				String learnpediaId = play.Play.configuration.getProperty(
						"learnpedia.id").trim();
				String reqOrgId = Scope.Params.current().get("orgId").trim();
				Logger.log4j.info("LearnpediaID : " + learnpediaId
						+ "        ReqOrgId : " + reqOrgId);
				if (learnpediaId.equals(reqOrgId)) {
					firebaseKey = play.Play.configuration.getProperty("learnpedia.firebaseKey");
					Logger.log4j.info("Came inside learnpedia");
				} else {
					firebaseKey = play.Play.configuration.getProperty("other.firebaseKey");
					Logger.log4j.info("Came inside other");
				}
				FCMData.put("title",
						Scope.Params.current().get("notificationTitle"));

				FCMData.put("message",
						Scope.Params.current().get("notificationMessage"));
				if (allParams.containsKey("notificationSummary")) {
					FCMData.put("summary",
							Scope.Params.current().get("notificationSummary"));
				}
				if (allParams.containsKey("notificationBigImage")) {
					FCMData.put("imageUrl",
							Scope.Params.current().get("notificationBigImage"));
				}

				// Send NOTIFICAITON to users
				if (!allParams.containsKey("sectionId")) {
					FCMInput.put("to", "/topics/" + slug);
					FCMInput.put("data", FCMData);
					FCMResponse = sendReqToFCM(FCMInput, firebaseKey);
				}
				// Send program specific notification
				else {
					FCMInput.put("to", "/topics/" + slug + "_"
							+ Scope.Params.current().get("sectionId"));
					// Send resource specific notification
					if (allParams.containsKey("entityId")) {
						resp = validateResource(allParams);
						Logger.log4j
								.info("Came into resource specific notification");
						if (resp.getBoolean("result")) {
							Logger.log4j.info("Resource found");
							FCMData.put("entityType", Scope.Params.current()
									.get("entityType"));
							FCMData.put("entityId",
									Scope.Params.current().get("entityId"));
							FCMInput.put("data", FCMData);
							FCMResponse = sendReqToFCM(FCMInput, firebaseKey);
						} else {
							resp.put("errorCode", "RESOURCE_NOT_FOUND");
							resp.put("errorMessage",
									"Cannot find the Resource with the given ID");
							renderJSON(resp.toString());
						}
					} else {
						// TO SEND COMMON MESSAGE TO PROGRAM SPECIFIC USER
						FCMInput.put("data", FCMData);
						FCMResponse = sendReqToFCM(FCMInput, firebaseKey);
					}
				}
				Logger.log4j.info("FCM INPUT" + FCMInput);
				FCMResponseObj = new JSONObject(FCMResponse);
				if (!FCMResponseObj.has("message_id")) {
					resp.put("errorCode", "NOTIFICATION_NOT_SENT");
					resp.put("errorMessage",
							"Couldn't send notification, Please Try sending again!!");
				} else {
					resp.put("errorCode", "");
					resp.put("errorMessage", "");
					resp.put("result", true);
				}
				renderJSON(resp.toString());
			} catch (JSONException e) {
				e.printStackTrace();
				Logger.log4j.info("Error is came in notification "
						+ e.getLocalizedMessage());
			}
		} else {
			Logger.log4j.info("Came from QA or Local");
		}
	}
	public static JSONObject validateResource(Map<String, Object> allParams){
		Promise<JSONResponseWrapper> promise = client(
				ClientUtil.CONTENT_SERVICE_URL + "/contents/validateResource", null);
		Logger.log4j.info("BEFORE AWAIT");
		await(promise);
		Logger.log4j.info("AFTER AWAIT");
		JSONObject resp = new JSONObject();
		resp = ResponseUtil.checkResponse(getJSON(promise));
		return resp;
	}
	public static String sendReqToFCM(JSONObject postJsonData,
			String firebaseKey) {
		String fcmResponse = "";
		try {
			URL url = new URL("https://fcm.googleapis.com/fcm/send");
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type",
					"application/json; charset=UTF-8");
			connection.setRequestProperty("Accept", "application/json");
			connection
					.setRequestProperty("Authorization", "key=" + firebaseKey);
			connection.setDoOutput(true);
			OutputStreamWriter out = new OutputStreamWriter(
					connection.getOutputStream());
			out.write(postJsonData.toString());
			Logger.log4j.info("output " + out.toString());
			out.close();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			String decodedString;
			while ((decodedString = in.readLine()) != null) {
				fcmResponse += decodedString;
			}
			in.close();
		} catch (MalformedURLException e) {
			Logger.log4j.info(
					"MalformedURLException in sendNotification function", e);
			Logger.log4j.info("Error is came in MalformedURLException"
					+ e.getMessage());
		} catch (IOException e) {
			Logger.log4j.info("IOException in sendNotification function", e);
			Logger.log4j.info("Error is came in IOException" + e.getMessage());
		}

		return fcmResponse;
	}

}