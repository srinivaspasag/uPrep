package controllers;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.data.validation.Required;
import play.i18n.Messages;
import play.libs.F;
import play.libs.WS;
import play.libs.F.Promise;
import play.libs.WS.HttpResponse;
import play.mvc.Scope;
import play.mvc.With;
import pojos.UserOrg;
import uicom.response.ErrorInfo;
import uicom.response.JSONResponse;
import uicom.util.ClientUtil;
import uicom.util.ResponseUtil;

@With(Security.class)
public class QrPeople extends AbstractQRUIController {

	public static void addMemberSubmit() {

		recordActivity(ClientUtil.ActivityPages.MEMBERS,
				ClientUtil.ActivityAction.ADD);
		Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
				ClientUtil.ORGANIZATION_SERVICE_URL + "/members/addMember",
				null);
		Logger.log4j.info("BEFORE AWAIT");
		await(promise);
		Logger.log4j.info("AFTER AWAIT");
		JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
		renderJSON(resp.toString());
	}

	private static JSONArray _getCustomSignupFields(String profile) {

		profile = profile == null || StringUtils.isEmpty(profile) ? "STUDENT"
				: profile;
		request.params.put("targetOrgMemberProfile", profile);
		Promise<JSONResponseWrapper> promise = client(
				ClientUtil.ORGANIZATION_SERVICE_URL
						+ "/organizations/getOrgMemberExtraInputFields", null);
		Logger.log4j.info("BEFORE AWAIT");
		await(promise);
		Logger.log4j.info("AFTER AWAIT");
		JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
		JSONArray array = null;
		try {
			if (StringUtils.isEmpty(resp.getString("errorCode"))) {
				array = resp.getJSONObject("result").getJSONArray("fields");
			}
		} catch (JSONException ex) {
			Logger.log4j.error(ex.getMessage());
			array = null;
		}
		return array;
	}

	public static void addEditMember() {

		recordActivity(ClientUtil.ActivityPages.MEMBERS,
				ClientUtil.ActivityAction.OPEN);
		String targetUserId = request.params.get("targetUserId");
		JSONObject memberInfo = null;
		if (StringUtils.isNotEmpty(targetUserId)) {
			memberInfo = _getMemberInfo(null);
		}
		render(memberInfo);
	}

	public static void editMemberSubmit() {

		recordActivity(ClientUtil.ActivityPages.MEMBERS,
				ClientUtil.ActivityAction.EDIT);
		Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
				ClientUtil.ORGANIZATION_SERVICE_URL + "/members/updateMember",
				null);
		Logger.log4j.info("BEFORE AWAIT");
		await(promise);
		Logger.log4j.info("AFTER AWAIT");
		JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));

		try {
			if (StringUtils.isEmpty(resp.getString("errorCode"))
					&& StringUtils.equals(request.params.get("targetUserId"),
							session.get("userId"))) {
				session.put("orgUserFirstName", request.params.get("firstName"));
				session.put("orgUserLastName", request.params.get("lastName"));
			}
		} catch (Exception e) {
			Logger.log4j.error("Error in setting org pic in session"
					+ e.getMessage());
		}
		renderJSON(resp.toString());
	}

	public static void addStudentSubmit() {

		recordActivity(ClientUtil.ActivityPages.STUDENT,
				ClientUtil.ActivityAction.ADD);
		Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
				ClientUtil.ORGANIZATION_SERVICE_URL + "/members/addMember",
				null);
		Logger.log4j.info("BEFORE AWAIT");
		await(promise);
		Logger.log4j.info("AFTER AWAIT");
		JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
		renderJSON(resp.toString());
	}

	public static void addEditStudent() {

		recordActivity(ClientUtil.ActivityPages.STUDENT,
				ClientUtil.ActivityAction.OPEN);
		String targetUserId = request.params.get("targetUserId");
		JSONObject memberInfo = null;
		if (StringUtils.isNotEmpty(targetUserId)) {
			memberInfo = _getMemberInfo(null);
		}
		JSONArray signUpFields = _getCustomSignupFields("STUDENT");
		render(memberInfo, signUpFields);
	}

	public static void addOfflineUserSubmit() {

		recordActivity(ClientUtil.ActivityPages.OFFLINE_USER,
				ClientUtil.ActivityAction.ADD);
		Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
				ClientUtil.ORGANIZATION_SERVICE_URL + "/members/addMember",
				null);
		Logger.log4j.info("BEFORE AWAIT");
		await(promise);
		Logger.log4j.info("AFTER AWAIT");
		JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
		renderJSON(resp.toString());
	}

	public static void editOfflineUserSubmit() {

		recordActivity(ClientUtil.ActivityPages.OFFLINE_USER,
				ClientUtil.ActivityAction.EDIT);
		Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
				ClientUtil.ORGANIZATION_SERVICE_URL + "/members/updateMember",
				null);
		Logger.log4j.info("BEFORE AWAIT");
		await(promise);
		Logger.log4j.info("AFTER AWAIT");
		JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
		renderJSON(resp.toString());
	}

	public static void addEditOfflineUser() {

		recordActivity(ClientUtil.ActivityPages.OFFLINE_USER,
				ClientUtil.ActivityAction.OPEN);
		String targetUserId = request.params.get("targetUserId");
		JSONObject memberInfo = null;
		if (StringUtils.isNotEmpty(targetUserId)) {
			memberInfo = _getMemberInfo(null);
		}
		render(memberInfo);
	}

	public static void uploadStudentsFile() {

		JSONObject resp = uploadUtil(ClientUtil.ORGANIZATION_SERVICE_URL
				+ "/members/uploadStudents", null, null);
		recordActivity(ClientUtil.ActivityPages.STUDENT,
				ClientUtil.ActivityAction.UPLOAD);
		// try {
		// String errorCode = resp.getString("errorCode");
		// if (!StringUtils.equals(errorCode, "ACTION_NOT_ALLOWED")
		// && !StringUtils.equals(errorCode, "INVALID_INPUT_DATA")
		// && !StringUtils.equals(errorCode, "STUDENT_FILE_UNPARSEABLE")) {
		// resp = ResponseUtil.checkResponse(resp);
		// }
		// } catch (JSONException e) {
		// resp = ResponseUtil.checkResponse(resp);
		// }
		if (resp == null) {
			resp = new JSONObject(new JSONResponse("",
					"Some error occured. Please try again", "UPLOAD_ERROR"));
		}
		renderJSON(resp.toString());
	}

	public static void uploadPeopleProfilePics() {

		JSONObject resp = uploadUtil(ClientUtil.ORGANIZATION_SERVICE_URL
				+ "/members/bulkUploadProfilePics", null, null);
		if (resp == null) {
			resp = new JSONObject(new JSONResponse("",
					"Some error occured. Please try again", "UPLOAD_ERROR"));
		}
		renderJSON(resp.toString());
	}

	public static void editStudentSubmit() {

		recordActivity(ClientUtil.ActivityPages.STUDENT,
				ClientUtil.ActivityAction.EDIT);
		Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
				ClientUtil.ORGANIZATION_SERVICE_URL + "/members/updateMember",
				null);
		Logger.log4j.info("BEFORE AWAIT");
		await(promise);
		Logger.log4j.info("AFTER AWAIT");
		JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
		renderJSON(resp.toString());
	}

	public static void people() {

		recordActivity(ClientUtil.ActivityPages.MEMBERS,
				ClientUtil.ActivityAction.OPEN);
		_putReqParamsForPeople();
		JSONObject people = _getMembers(null);
		UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(request.params
				.get("orgId"));
		boolean canImpersonate = _canImpersonate();
		render(people, currentOrgInfo, canImpersonate);
	}

	protected static boolean _canImpersonate() {

		boolean canImpersonate = false;
		try {
			request.params.put("targetUserId", session.get("callingUserId"));
			JSONObject resp = _getMemberInfo(null);
			canImpersonate = resp.getJSONObject("result").getJSONObject("info")
					.getBoolean("canImpersonate");
		} catch (Exception ex) {
		}
		return canImpersonate;
	}

	public static void peopleTable() {

		Map<String, Object> allParams = getReqParams();
		String targetProfile = request.params.get("targetProfile");
		JSONObject people = null;

		if (targetProfile.equals("ORGANIZATION")) {
			allParams.put("providerOrgId", request.params.get("orgId"));
			people = _getOrgsOfProg(allParams);
		} else {
			people = _getMembers(allParams);
		}

		UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(request.params
				.get("orgId"));
		if (targetProfile.equals("STUDENT")) {

			render("QrPeople/studentsTable.html", people, currentOrgInfo);
		} else if (targetProfile.equals("ORGANIZATION")) {
			boolean canImpersonate = _canImpersonate();
			render("QrPeople/sharedOrgsTable.html",people, canImpersonate, currentOrgInfo);
		} else if (targetProfile.equals("OFFLINE_USER")) {
			// JSONObject people = _getMembers(allParams);
			render("QrPeople/offlineUsersTable.html", people, currentOrgInfo);
		} else {
			boolean canImpersonate = _canImpersonate();
			// JSONObject people = _getMembers(allParams);
			render("QrPeople/membersTable.html", people, currentOrgInfo,
					canImpersonate);
		}
	}

	public static void studentPage() {

		recordActivity(ClientUtil.ActivityPages.STUDENT,
				ClientUtil.ActivityAction.OPEN);
		JSONObject memberInfo = _getMemberInfo(null);
		UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(request.params
				.get("orgId"));
		render(memberInfo, currentOrgInfo);
	}

	public static void offlineUserPage() {

		recordActivity(ClientUtil.ActivityPages.STUDENT,
				ClientUtil.ActivityAction.OPEN);
		JSONObject memberInfo = _getMemberInfo(null);
		UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(request.params
				.get("orgId"));
		render(memberInfo, currentOrgInfo);
	}

	public static void memberPage() {

		recordActivity(ClientUtil.ActivityPages.MEMBER,
				ClientUtil.ActivityAction.OPEN);
		JSONObject memberInfo = _getMemberInfo(null);
		UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(request.params
				.get("orgId"));
		render(memberInfo, currentOrgInfo);
	}

	public static void showUserMappings() {

		JSONArray programs = new JSONArray();
		String targetProfile = "";
		try {
			JSONObject memberInfo = _getMemberInfo(null);
			JSONObject info = memberInfo.getJSONObject("result").getJSONObject(
					"info");
			programs = info.getJSONObject("mappings").getJSONArray("programs");
			targetProfile = info.getString("profile");
		} catch (Exception e) {
			Logger.log4j.info(e.getMessage());
		}
		render("Widgets/mappingsTable.html", programs, targetProfile);
	}

	public static void addMemToAcadStr() {

		Promise<AbstractQRUIController.JSONResponseWrapper> promise = clientWithAllHeaders(
				ClientUtil.ORGANIZATION_SERVICE_URL
						+ "/members/addMemberMapping", null, null);
		Logger.log4j.info("BEFORE AWAIT");
		await(promise);
		Logger.log4j.info("AFTER AWAIT");
		JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
		renderJSON(resp.toString());
	}

	public static void addCourseToMemAcadStr() {

		Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
				ClientUtil.ORGANIZATION_SERVICE_URL
						+ "/members/updateMemberMapping", null);
		Logger.log4j.info("BEFORE AWAIT");
		await(promise);
		Logger.log4j.info("AFTER AWAIT");
		JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
		renderJSON(resp.toString());
	}

	public static void removeMemFromAcadStr() {

		Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
				ClientUtil.ORGANIZATION_SERVICE_URL
						+ "/members/removeMemberMapping", null);
		Logger.log4j.info("BEFORE AWAIT");
		await(promise);
		Logger.log4j.info("AFTER AWAIT");
		JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
		renderJSON(resp.toString());
	}

	public static void removeCourseFromMemAcadStr() {

		Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
				ClientUtil.ORGANIZATION_SERVICE_URL
						+ "/members/updateMemberMapping", null);
		Logger.log4j.info("BEFORE AWAIT");
		await(promise);
		Logger.log4j.info("AFTER AWAIT");
		JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
		renderJSON(resp.toString());
	}

	public static void setRoleOfMember() {

		Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
				ClientUtil.CMDS_SERVICE_URL
						+ "/UserManagements/setRoleOfMember", null);
		Logger.log4j.info("BEFORE AWAIT");
		await(promise);
		Logger.log4j.info("AFTER AWAIT");
		JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
		renderJSON(resp.toString());
	}

	public static void uploadProfilePic() {

		JSONObject resp = ResponseUtil.checkResponse(uploadUtil(
				ClientUtil.ORGANIZATION_SERVICE_URL
						+ "/members/uploadProfilePic", null, null));
		try {
			if (StringUtils.isEmpty(resp.getString("errorCode"))
					&& StringUtils.equals(session.get("userId"),
							request.params.get("targetUserId"))) {
				String thumbnail = resp.getJSONObject("result").getString(
						"thumbnail");
				session.put("orgUserProfilePic", thumbnail);
			}
		} catch (Exception e) {
			Logger.log4j.error("Error in setting org pic in session"
					+ e.getMessage());
		}
		renderJSON(resp.toString());
	}

	public static void bulkUpdateStudentsInSection() {

		Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
				ClientUtil.ORGANIZATION_SERVICE_URL
						+ "/members/bulkUpdateStudentsInSection", null);
		Logger.log4j.info("BEFORE AWAIT");
		await(promise);
		Logger.log4j.info("AFTER AWAIT");
		JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
		renderJSON(resp.toString());
	}

	public static void getMyMemberInfo() {

		request.params.put("targetUserId", session.get("userId"));
		JSONObject resp = _getMemberInfo(null);
		renderJSON(resp.toString());
	}

	// public profile
	public static void changePublicSettings() {

		request.params.put("targetUserId", session.get("userId"));
		Map<String, Object> allParams = getReqParams();
		JSONObject memberInfo = _getMemberInfo(allParams);
		JSONObject userInfo = _getUserPublicInfo(allParams);
		render("/UIComTags/settings.html", memberInfo, userInfo);
	}

	public static void editPublicProfile() {

		JSONObject publicProfile = _getUserPublicInfo(null);
		render(publicProfile);
	}

	public static void editPublicProfileSubmit() {

		request.params.put("targetUserId", session.get("userId"));
		F.Promise<AbstractUIController.JSONResponseWrapper> promise = client(
				ClientUtil.USER_SERVICE_URL + "/users/updateUser", null);
		Logger.log4j.info("BEFORE AWAIT");
		await(promise);
		Logger.log4j.info("AFTER AWAIT");
		JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
		renderJSON(resp.toString());
	}

	public static void uploadPublicProfilePic() {

		JSONObject resp = uploadUtil(ClientUtil.USER_SERVICE_URL
				+ "/users/uploadProfilePic", null, null);
		renderJSON(resp.toString());
	}

	// email,username,password reset by admin
	public static void unsetMemberEmail() {

		F.Promise<AbstractUIController.JSONResponseWrapper> promise = client(
				ClientUtil.ORGANIZATION_SERVICE_URL + "/members/unsetEmail",
				null);
		Logger.log4j.info("BEFORE AWAIT");
		await(promise);
		Logger.log4j.info("AFTER AWAIT");
		JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
		renderJSON(resp.toString());
	}

	public static void changeMemberPassword() {

		recordActivity(ClientUtil.ActivityPages.MEMBER_PWD,
				ClientUtil.ActivityAction.CHANGE);
		JSONObject resp = _updatePassword(null);
		renderJSON(resp.toString());
	}

	public static void getTestUsersData() {
	    Logger.log4j.info("orgId is"+request.params.get("orgId"));
	    String orgId = request.params.get("orgId");
        Map<String, Object> allParams = getReqParams();
        final Promise<HttpResponse> promise = WS
                .url(ClientUtil.ORGANIZATION_SERVICE_URL + "/members/getTestUsersData")
                .params(allParams).headers(new HashMap<String, String>()).timeout("5min")
                .getAsync();
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        try {
            HttpResponse httpResponse = promise.get();
            String contentType = httpResponse.getContentType();
            Logger.log4j.info("Content Type: " + contentType);
            if ("text/csv".equals(contentType)) {
                InputStream stream = httpResponse.getStream();
                renderBinary(stream, "testUsersData.csv", false);
            }else if("text/html; charset=utf-8".equals(contentType)){
                render("errors/404.html");
            }
            else{
                String errorMessage = httpResponse.getString();
                Logger.log4j.info("errorMessage is"+errorMessage);
                flash.put("testUserDataError", errorMessage);
                redirect("/organization/"+orgId+"/people?targetProfile=STUDENT");
                renderText(httpResponse.getString());
            }
            Logger.log4j.info("Response: " + httpResponse.getString());
        } catch (Exception e) {
            Logger.log4j.error(e);
        }
    }

	public static void getStudentsData(){
	    Logger.log4j.info("orgId is"+request.params.get("orgId"));
        String orgId = request.params.get("orgId");
        Map<String, Object> allParams = getReqParams();
        final Promise<HttpResponse> promise = WS
                .url(ClientUtil.ORGANIZATION_SERVICE_URL + "/members/getStudentsData")
                .params(allParams).headers(new HashMap<String, String>()).timeout("5min")
                .getAsync();
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        try {
            HttpResponse httpResponse = promise.get();
            String contentType = httpResponse.getContentType();
            Logger.log4j.info("Content Type: " + contentType);
            if ("text/csv".equals(contentType)) {
                InputStream stream = httpResponse.getStream();
                renderBinary(stream, "studentsData.csv", false);
            }else if("text/html; charset=utf-8".equals(contentType)){
                render("errors/404.html");
            }
            else{
                String errorMessage = httpResponse.getString();
                Logger.log4j.info("errorMessage is"+errorMessage);
                flash.put("testUserDataError", errorMessage);
                redirect("/organization/"+orgId+"/people?targetProfile=STUDENT");
                renderText(httpResponse.getString());
            }
            Logger.log4j.info("Response: " + httpResponse.getString());
        } catch (Exception e) {
            Logger.log4j.error(e);
        }
	}

	public static void resetUsername() throws Throwable {

		recordActivity(ClientUtil.ActivityPages.MEMBER_USERNAME,
				ClientUtil.ActivityAction.CHANGE);
		F.Promise<AbstractUIController.JSONResponseWrapper> promise = client(
				ClientUtil.ORGANIZATION_SERVICE_URL + "/members/resetUsername",
				null);
		Logger.log4j.info("BEFORE AWAIT");
		await(promise);
		Logger.log4j.info("AFTER AWAIT");
		JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
		renderJSON(resp.toString());
	}

	public static void uploadMemberProfilePic() {

		JSONObject resp = ResponseUtil.checkResponse(uploadUtil(
				ClientUtil.ORGANIZATION_SERVICE_URL
						+ "/members/uploadProfilePic", null, null));
		try {
			if (StringUtils.isEmpty(resp.getString("errorCode"))
					&& StringUtils.equals(request.params.get("targetUserId"),
							session.get("userId"))) {
				String thumbnail = resp.getJSONObject("result").getString(
						"thumbnail");
				session.put("orgUserProfilePic", thumbnail);
			}
		} catch (Exception e) {
			Logger.log4j.error("Error in setting org pic in session"
					+ e.getMessage());
		}
		renderJSON(resp.toString());
	}

	// return
	protected static JSONObject _getMembers(Map<String, Object> allParams) {

		Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
				ClientUtil.ORGANIZATION_SERVICE_URL + "/members/getMembers",
				allParams);
		Logger.log4j.info("BEFORE AWAIT");
		await(promise);
		Logger.log4j.info("AFTER AWAIT");
		JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
		return resp;
	}

	protected static JSONObject _getOrganizations(Map<String, Object> allParams) {

		Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
				ClientUtil.ORGANIZATION_SERVICE_URL
						+ "/organizations/getOrganizations", allParams);
		Logger.log4j.info("BEFORE AWAIT");
		await(promise);
		Logger.log4j.info("AFTER AWAIT");
		JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
		return resp;
	}

	protected static JSONObject _getOrgsOfProg(Map<String, Object> allParams) {

		Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
				ClientUtil.ORGANIZATION_SERVICE_URL
						+ "/organizations/getSharedOrgsByProgId", allParams);
		Logger.log4j.info("BEFORE AWAIT");
		await(promise);
		Logger.log4j.info("AFTER AWAIT");
		JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
		return resp;
	}

 	protected static JSONObject _shareProgToOrg(Map<String, Object> allParams) {

		Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
				ClientUtil.ORGANIZATION_SERVICE_URL
						+ "/organizations/shareProgToOrg", allParams);
		Logger.log4j.info("BEFORE AWAIT");
		await(promise);
		Logger.log4j.info("AFTER AWAIT");
		JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
		return resp;
	}

	protected static JSONObject _getMemberInfo(Map<String, Object> allParams) {

		Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
				ClientUtil.ORGANIZATION_SERVICE_URL
						+ "/members/getMemberProfile", allParams);
		Logger.log4j.info("BEFORE AWAIT");
		await(promise);
		Logger.log4j.info("AFTER AWAIT");
		JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
		return resp;
	}

	protected static JSONObject _getUserPublicInfo(Map<String, Object> allParams) {

		F.Promise<AbstractUIController.JSONResponseWrapper> promise = client(
				ClientUtil.USER_SERVICE_URL + "/users/getUserSelfFullProfile",
				allParams);
		Logger.log4j.info("BEFORE AWAIT");
		await(promise);
		Logger.log4j.info("AFTER AWAIT");
		JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
		return resp;
	}

	protected static JSONObject _updatePassword(Map<String, Object> allParams) {

		F.Promise<AbstractUIController.JSONResponseWrapper> promise = client(
				ClientUtil.USER_SERVICE_URL + "/users/updateUserPassword",
				allParams);
		Logger.log4j.info("BEFORE AWAIT");
		await(promise);
		Logger.log4j.info("AFTER AWAIT");
		JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
		return resp;
	}

	// direct
	public static void peopleDirect(String orgId) {

		recordActivity(ClientUtil.ActivityPages.MEMBERS,
				ClientUtil.ActivityAction.OPEN);
		_putReqParamsForPeople();
		JSONObject people = _getMembers(null);
		boolean canImpersonate = _canImpersonate();
		String includeName = "QrPeople/people.html";
		flash.put("ENTRY", "DIRECT");
		UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
		render("Application/mapper.html", includeName, people, currentOrgInfo,
				canImpersonate);
	}

	private static void _putReqParamsForPeople() {

		Scope.Params reqParams = request.params;
		if (StringUtils.isEmpty(reqParams.get("start"))) {
			request.params.put("start", "0");
		}
		if (StringUtils.isEmpty(reqParams.get("size"))) {
			request.params.put("size", "50");
		}
		if (StringUtils.isEmpty(reqParams.get("targetProfile"))) {
			request.params.put("targetProfile", "TEACHER");
		}
	}

	// granteeId gives access to orgId to the particular programId
	public static void shareProgToOrg(String programId, String providerOrgId, String subscriberOrgId) {

		 if (StringUtils.isEmpty(request.params.get("targetProfile"))) {
	            request.params.put("targetProfile", "ORGANIZATION");
	        }
		Map<String,Object> param = new HashMap<String, Object>();
		param.put("programId", programId);
		param.put("providerOrgId",request.params.get("orgId"));
		param.put("subscriberOrgId", subscriberOrgId);
		JSONObject people = _shareProgToOrg(param);
		boolean canImpersonate = QrPeople._canImpersonate();
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(request.params.get("orgId"));
        render("QrPeople/people.html",people, canImpersonate, currentOrgInfo);
	}


 	public static void studentDirect(String targetUserId, String orgId) {

		recordActivity(ClientUtil.ActivityPages.STUDENT,
				ClientUtil.ActivityAction.OPEN, ClientUtil.Entity.USER,
				targetUserId);
		JSONObject memberInfo = _getMemberInfo(null);
		String includeName = "QrPeople/studentPage.html";
		flash.put("ENTRY", "DIRECT");
		UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
		render("Application/mapper.html", includeName, memberInfo,
				currentOrgInfo);
	}

	public static void offlineUserDirect(String targetUserId, String orgId) {

		recordActivity(ClientUtil.ActivityPages.OFFLINE_USER,
				ClientUtil.ActivityAction.OPEN, ClientUtil.Entity.USER,
				targetUserId);
		JSONObject memberInfo = _getMemberInfo(null);
		String includeName = "QrPeople/offlineUserPage.html";
		flash.put("ENTRY", "DIRECT");
		UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
		render("Application/mapper.html", includeName, memberInfo,
				currentOrgInfo);
	}

	public static void memberDirect(String targetUserId, String orgId) {

		recordActivity(ClientUtil.ActivityPages.MEMBER,
				ClientUtil.ActivityAction.OPEN, ClientUtil.Entity.USER,
				targetUserId);
		JSONObject memberInfo = _getMemberInfo(null);
		String includeName = "QrPeople/memberPage.html";
		flash.put("ENTRY", "DIRECT");
		UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
		render("Application/mapper.html", includeName, memberInfo,
				currentOrgInfo);
	}

	public static void profileDirect(String orgId) {

		recordActivity(ClientUtil.ActivityPages.MEMBER,
				ClientUtil.ActivityAction.OPEN);
		JSONObject publicProfile = _getUserPublicInfo(null);
		String includeName = "QrPeople/publicProfile.html";
		flash.put("ENTRY", "DIRECT");
		UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
		render("Application/mapper.html", includeName, publicProfile,
				currentOrgInfo);
	}

	public static void addMemberDirect(String orgId, String profile) {

		String includeName = _addEditMemberDirect(ClientUtil.ActivityAction.ADD);
		flash.put("ENTRY", "DIRECT");
		UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
		JSONArray signUpFields = _getCustomSignupFields(profile);
		render("Application/mapper.html", includeName, currentOrgInfo,
				signUpFields);
	}

	public static void editMemberDirect(String orgId, String profile) {

		JSONObject memberInfo = null;
		String userid = request.params.get("userid");
		if (StringUtils.isNotEmpty(userid)) {
			request.params.put("targetUserId", userid);
			memberInfo = _getMemberInfo(null);
		} else {
			addMemberDirect(orgId, profile);
		}
		String includeName = _addEditMemberDirect(ClientUtil.ActivityAction.EDIT);
		flash.put("ENTRY", "DIRECT");
		UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
		JSONArray signUpFields = _getCustomSignupFields(profile);
		render("Application/mapper.html", includeName, memberInfo,
				currentOrgInfo, signUpFields);
	}

	private static String _addEditMemberDirect(ClientUtil.ActivityAction action) {

		recordActivity(ClientUtil.ActivityPages.MEMBER, action);
		String profile = request.params.get("profile");
		if (StringUtils.isEmpty(profile)) {
			profile = "TEACHER";
		}
		request.params.put("targetProfile", profile);
		String includeName = "QrPeople/addEditMember.html";
		if (StringUtils.equalsIgnoreCase(profile, "STUDENT")) {
			includeName = "QrPeople/addEditStudent.html";
		} else if (StringUtils.equalsIgnoreCase(profile, "OFFLINE_USER")) {
			includeName = "QrPeople/addEditOfflineUser.html";
		}
		return includeName;
	}

	public static void showDeactivationPopup(String orgId,
			@Required String targetUserId) {

		recordActivity(ClientUtil.ActivityPages.DEACTIVATION_POPUP,
				ClientUtil.ActivityAction.OPEN);
		UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
		if ("MANAGER".equals(currentOrgInfo.getOrgUserProfile())) {
			render("QrPeople/deactivationPopup.html", targetUserId);
		}
		JSONResponse resp = new JSONResponse(new ErrorInfo("ACCESS_DENIED",
				Messages.get("ACCESS_DENIED")));
		renderJSON(resp.toString());
	}

	public static void scheduleDeactivateMember(@Required String targetUserId) {

		ClientUtil.ActivityAction actionType = ClientUtil.ActivityAction.DEACTIVATED_ACTIVATED;
		recordActivity(ClientUtil.ActivityPages.DEACTIVATION_POPUP, actionType,
				ClientUtil.Entity.USER, targetUserId);
		F.Promise<AbstractUIController.JSONResponseWrapper> promise = client(
				ClientUtil.ORGANIZATION_SERVICE_URL + "/members/activate", null);
		Logger.log4j.info("BEFORE AWAIT");
		await(promise);
		Logger.log4j.info("AFTER AWAIT");
		JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
		renderJSON(resp.toString());
	}

	public static void editEndDatePopup(){
		Map<String, Object> allParams = getReqParams();
		String sectionId = request.params.get("sectionId");
		String targetUserId = request.params.get("targetUserId");
		String targetOrgMemberId = request.params.get("targetOrgMemberId");
		render(allParams,sectionId,targetUserId,targetOrgMemberId);
	}

	public static void updateEndDate(){
		Map<String, Object> allParams = getReqParams();
		Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
				ClientUtil.ORGANIZATION_SERVICE_URL
						+ "/members/updateEndDateMapping", allParams);
		Logger.log4j.info("BEFORE AWAIT");
		await(promise);
		Logger.log4j.info("AFTER AWAIT");
		JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
		renderJSON(resp.toString());
	}

    public static void sendEmailsPopup(){
        Map<String, Object> allParams = getReqParams();
        Map<String,String> paramsMap = new HashMap<String,String>();
        for (Map.Entry<String, Object> entry : allParams.entrySet()) {
            paramsMap.put(entry.getKey(), request.params.get(entry.getKey()));
        }
        render(paramsMap);
    }

    public static void sendEmail(){
        Map<String, Object> allParams = getReqParams();
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/members/sendEmailsToStudents", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    private static JSONObject _getPointsOfSale() {
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/getOrgPointsOfSale", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }

    private static JSONObject _getSectionPackageInfo(Map<String, Object> allParams) {
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/getSectionPackageInfo", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }

    private static JSONObject _getSaleDetails(Map<String, Object> allParams) {
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/members/getSaleDetails", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }

    public static void addSaleDetails() {
        Map<String, Object> allParams = getReqParams();
        JSONObject pointsOfSale = _getPointsOfSale();
        JSONObject sectionResp = _getSectionPackageInfo(allParams);
        Logger.log4j.info("POS:" + pointsOfSale);
        render(pointsOfSale, sectionResp);
    }

    public static void editSaleDetails() {
        Map<String, Object> allParams = getReqParams();
        JSONObject saleDetailsResp = _getSaleDetails(allParams);
        render(saleDetailsResp);
    }

    public static void updateSaleDetails() {
        Map<String, Object> allParams = getReqParams();
        Logger.log4j.info("Update sale details function");
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL
                        + "/members/updateSaleDetails", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }
    
    public static void getStudentActivity(){
    	
        Map<String, Object> allParams = getReqParams();
        allParams.put("userId", allParams.get("targetUserId"));
        Logger.log4j.info("allParams of getLatestActivityOfAStudent  "+allParams);
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL
                        + "/activityLogger/getStudentActivity", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
		render("QrPeople/getStudentActivity.html", resp);
    }
}
