package util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import uicom.util.ClientUtil;

public class TabAppUrlFactory {

    private static final Map<String, String> serviceUrlMap = new HashMap<String, String>();
    public static final TabAppUrlFactory     INSTANCE      = new TabAppUrlFactory();

    private TabAppUrlFactory() {

        serviceUrlMap.put(StringUtils.lowerCase("authenticateMember"),
                ClientUtil.LOGIN_MEMBER_SERVICE_URL);
        serviceUrlMap.put(StringUtils.lowerCase("authenticateUser"), ClientUtil.LOGIN_SERVICE_URL);

        serviceUrlMap.put(StringUtils.lowerCase("getMemberProfile"),
                ClientUtil.ORGANIZATION_SERVICE_URL + "/members/getMemberProfile");
        serviceUrlMap.put(StringUtils.lowerCase("getOrgMemberProfile"),
                ClientUtil.ORGANIZATION_SERVICE_URL + "/members/getOrgMemberProfile");
        serviceUrlMap.put(StringUtils.lowerCase("addMemberWithAccessCode"),
                ClientUtil.ORGANIZATION_SERVICE_URL + "/members/addMemberWithAccessCode");
        serviceUrlMap.put(StringUtils.lowerCase("addOrgMember"),
                ClientUtil.ORGANIZATION_SERVICE_URL + "/members/addMember");
        serviceUrlMap.put(StringUtils.lowerCase("addOrgMemberMapping"),
                ClientUtil.ORGANIZATION_SERVICE_URL + "/members/addMemberMapping");

        serviceUrlMap.put(StringUtils.lowerCase("getSectionByAccessCode"),
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/getSectionByAccessCode");
        serviceUrlMap.put(StringUtils.lowerCase("getOrgInfo"), ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/getOrganizationBySlug");
        serviceUrlMap
                .put(StringUtils.lowerCase("getOrgMemberExtraInputFields"),
                        ClientUtil.ORGANIZATION_SERVICE_URL
                                + "/organizations/getOrgMemberExtraInputFields");
        serviceUrlMap.put(StringUtils.lowerCase("getOrgCategories"),
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/getCategories");
        serviceUrlMap.put(StringUtils.lowerCase("getCategorySections"),
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/getCategorySections");
        serviceUrlMap.put(StringUtils.lowerCase("getProgramCourses"),
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/getProgramCourses");

        serviceUrlMap.put(StringUtils.lowerCase("recordLogin"), ClientUtil.ORGANIZATION_SERVICE_URL
                + "/activityLogger/login");
        serviceUrlMap.put(StringUtils.lowerCase("recordLogout"),
                ClientUtil.ORGANIZATION_SERVICE_URL + "/activityLogger/logout");
        serviceUrlMap.put(StringUtils.lowerCase("recordActivity"),
                ClientUtil.ORGANIZATION_SERVICE_URL + "/activityLogger/record");
        //added to check user in Database : Amrita (SessionExpiry Issue)
        serviceUrlMap.put(StringUtils.lowerCase("checkUserInDB"),
                ClientUtil.ORGANIZATION_SERVICE_URL + "/activityLogger/checkIfUserExists");

        //Check App version to force update android app.
        serviceUrlMap.put(StringUtils.lowerCase("checkAppVersion"),
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/checkAppVersion");

        serviceUrlMap.put(StringUtils.lowerCase("getContentLinks"), ClientUtil.CONTENT_SERVICE_URL
                + "/contents/getContentLinks");
        serviceUrlMap.put(StringUtils.lowerCase("getTests"), ClientUtil.CONTENT_SERVICE_URL
                + "/tests/getTests");
        serviceUrlMap.put(StringUtils.lowerCase("getRemovedContentLinks"),
                ClientUtil.CONTENT_SERVICE_URL + "/contents/getRemovedContentLinks");
        serviceUrlMap.put(StringUtils.lowerCase("getContentDownloadLink"),
                ClientUtil.CONTENT_SERVICE_URL + "/contents/getContentDownloadLink");
        serviceUrlMap.put(StringUtils.lowerCase("getPdfUrl"),
                ClientUtil.CONTENT_SERVICE_URL + "/contents/getPdfDownloadLink");
        serviceUrlMap.put(StringUtils.lowerCase("getContents"), ClientUtil.CONTENT_SERVICE_URL
                + "/contents/getContents");
        serviceUrlMap.put(StringUtils.lowerCase("getEntityInfoForApp"), ClientUtil.CONTENT_SERVICE_URL
                + "/contents/getEntityInfoForApp");
        serviceUrlMap.put(StringUtils.lowerCase("addRatingAndFeedback"), ClientUtil.CONTENT_SERVICE_URL
                + "/contents/addRatingAndFeedback");

        serviceUrlMap.put(StringUtils.lowerCase("getBoards"), ClientUtil.BOARDS_SERVICE_URL
                + "/boards/getChildren");

        serviceUrlMap
                .put(StringUtils.lowerCase("getEntityQuestionsAttemptStat"),
                        ClientUtil.CONTENT_SERVICE_URL
                                + "/analytics/getUserEntityQuestionsAttemptStatInfo");
        serviceUrlMap
                .put(StringUtils.lowerCase("getEntityTestStatus"),
                        ClientUtil.CONTENT_SERVICE_URL
                        + "/analytics/getEntityTestStatus");

        serviceUrlMap.put(StringUtils.lowerCase("getQuestionsSolutions"),
                ClientUtil.CONTENT_SERVICE_URL + "/questions/getQuestionsSolutions");

        serviceUrlMap.put(StringUtils.lowerCase("syncTabletAnalytics"),
                ClientUtil.CONTENT_SERVICE_URL + "/analytics/syncTabletAnalytics");
        serviceUrlMap.put(StringUtils.lowerCase("getAttemptedEntities"),
                ClientUtil.CONTENT_SERVICE_URL + "/analytics/getAttemptedEntities");
        serviceUrlMap.put(StringUtils.lowerCase("getEntityLeaderBoard"),
                ClientUtil.CONTENT_SERVICE_URL + "/analytics/getEntityLeaderBoard");
        serviceUrlMap.put(StringUtils.lowerCase("getUserEntityRank"),
                ClientUtil.CONTENT_SERVICE_URL + "/analytics/getUserEntityRank");

        serviceUrlMap.put(StringUtils.lowerCase("addDiscussion"), ClientUtil.CONTENT_SERVICE_URL
                + "/discussions/addDiscussion");
        serviceUrlMap.put(StringUtils.lowerCase("getDiscussions"), ClientUtil.CONTENT_SERVICE_URL
                + "/discussions/getDiscussions");
        serviceUrlMap.put(StringUtils.lowerCase("getSimilarDiscussions"),
                ClientUtil.CONTENT_SERVICE_URL + "/discussions/getSimilarDiscussions");

        serviceUrlMap.put(StringUtils.lowerCase("addComment"), ClientUtil.CONTENT_SERVICE_URL
                + "/comments/addComment");
        serviceUrlMap.put(StringUtils.lowerCase("getComments"), ClientUtil.CONTENT_SERVICE_URL
                + "/comments/getComments");

        // socials action
        serviceUrlMap.put(StringUtils.lowerCase("follow"), ClientUtil.SOCIALS_WEB_SERVICE_URL
                + "/socials/follow");
        serviceUrlMap.put(StringUtils.lowerCase("unFollow"), ClientUtil.SOCIALS_WEB_SERVICE_URL
                + "/socials/unFollow");
        serviceUrlMap.put(StringUtils.lowerCase("upVote"), ClientUtil.SOCIALS_WEB_SERVICE_URL
                + "/socials/upVote");
        serviceUrlMap.put(StringUtils.lowerCase("view"), ClientUtil.SOCIALS_WEB_SERVICE_URL
                + "/socials/view");

        serviceUrlMap.put(StringUtils.lowerCase("getFollowers"), ClientUtil.SOCIALS_WEB_SERVICE_URL
                + "/socials/getFollowers");

        // billing service api
        serviceUrlMap.put(StringUtils.lowerCase("startTransaction"),
                ClientUtil.BILLING_WEB_SERVICE_URL + "/payments/startTransaction");
        serviceUrlMap.put(StringUtils.lowerCase("updateTransaction"),
                ClientUtil.BILLING_WEB_SERVICE_URL + "/payments/updateTransaction");
        serviceUrlMap.put(StringUtils.lowerCase("applyCoupon"),
                ClientUtil.BILLING_WEB_SERVICE_URL + "/payments/applyCoupon");
        serviceUrlMap.put(StringUtils.lowerCase("getBuyOrders"), ClientUtil.BILLING_WEB_SERVICE_URL
                + "/invoices/getBuyOrders");

        // access code api
        serviceUrlMap.put(StringUtils.lowerCase("verifyAccessCode"), ClientUtil.CMDS_SERVICE_URL
                + "/accessCodes/verifyAccessCode");

        //register GCM registration id
        serviceUrlMap.put(StringUtils.lowerCase("registerById"), ClientUtil.CMDS_SERVICE_URL
                + "/notifications/registerById");

        // module sync api
        serviceUrlMap.put(StringUtils.lowerCase("moduleEntryStatusSyncer"),
                ClientUtil.CONTENT_SERVICE_URL + "/modules/syncModule");

        // test analytics data for teacher view
        serviceUrlMap.put(StringUtils.lowerCase("getTestInfo"), ClientUtil.CONTENT_SERVICE_URL
                + "/tests/getTestInfo");
        serviceUrlMap.put(StringUtils.lowerCase("getTestQuestions"), ClientUtil.CONTENT_SERVICE_URL
                + "/tests/getTestQuestions");
        serviceUrlMap.put(StringUtils.lowerCase("getEntityMarkDistribution"),
                ClientUtil.CONTENT_SERVICE_URL + "/analytics/getEntityMarkDistribution");
        serviceUrlMap.put(StringUtils.lowerCase("getEntityQuestionAttempts"),
                ClientUtil.CONTENT_SERVICE_URL + "/analytics/getEntityQuestionAttempts");
        serviceUrlMap.put(StringUtils.lowerCase("getEntityScheduleAnalytics"),
                ClientUtil.CONTENT_SERVICE_URL + "/analytics/getEntityScheduleAnalytics");
        serviceUrlMap.put(StringUtils.lowerCase("getModuleSchedules"),
                ClientUtil.CONTENT_SERVICE_URL + "/modules/getModuleSchedules");

        // for sales app
        serviceUrlMap.put(StringUtils.lowerCase("getOrgPointsOfSale"),
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/getOrgPointsOfSale");
        serviceUrlMap.put(StringUtils.lowerCase("getPrograms"), ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/getPrograms");
        serviceUrlMap.put(StringUtils.lowerCase("getMembers"), ClientUtil.ORGANIZATION_SERVICE_URL
                + "/members/getMembers");
        serviceUrlMap.put(StringUtils.lowerCase("getProgramCenters"),
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/getProgramCenters");
        serviceUrlMap.put(StringUtils.lowerCase("getSections"), ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/getSections");
        serviceUrlMap.put(StringUtils.lowerCase("getSectionPackageInfo"),
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/getSectionPackageInfo");
        serviceUrlMap.put(StringUtils.lowerCase("getOrgMemberWithEmail"),
                ClientUtil.ORGANIZATION_SERVICE_URL + "/members/getOrgMemberWithEmail");
        serviceUrlMap.put(StringUtils.lowerCase("getSaleDetails"),
                ClientUtil.ORGANIZATION_SERVICE_URL + "/members/getSaleDetails");
        serviceUrlMap.put(StringUtils.lowerCase("updateSaleDetails"),
                ClientUtil.ORGANIZATION_SERVICE_URL + "/members/updateSaleDetails");
        serviceUrlMap.put(StringUtils.lowerCase("getOrganizations"),
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/getOrganizations");
        serviceUrlMap.put(StringUtils.lowerCase("addUserToken"),
                ClientUtil.ORGANIZATION_SERVICE_URL + "/usertokens/addUserToken");
        serviceUrlMap.put(StringUtils.lowerCase("recordTeacherResponse"),
                ClientUtil.CONTENT_SERVICE_URL + "/discussions/recordTeacherResponse");
        serviceUrlMap.put(StringUtils.lowerCase("getDiscussionInfo"),
                ClientUtil.CONTENT_SERVICE_URL + "/discussions/getDiscussionInfo");
        serviceUrlMap.put(StringUtils.lowerCase("getReferralData"),
                ClientUtil.ORGANIZATION_SERVICE_URL + "/members/getReferralData");

        //password
        serviceUrlMap.put(StringUtils.lowerCase("setPassword"),
                ClientUtil.USER_SERVICE_URL + "/users/updateUserForgottenPassword");
        serviceUrlMap.put(StringUtils.lowerCase("forgotPasswordUser"),
                ClientUtil.USER_SERVICE_URL + "/users/sendForgotPasswordMail");
        serviceUrlMap.put(StringUtils.lowerCase("forgotPasswordMember"),
                ClientUtil.ORGANIZATION_SERVICE_URL + "/members/sendForgotPasswordMail");
        serviceUrlMap.put(StringUtils.lowerCase("changePassword"),
                ClientUtil.USER_SERVICE_URL + "/users/changeUserPassword");
        serviceUrlMap.put(StringUtils.lowerCase("getDemoContentReq"),
                ClientUtil.CONTENT_SERVICE_URL + "/application/getContentForDemo");
        serviceUrlMap.put(StringUtils.lowerCase("getStudentsCount"),
                ClientUtil.ORGANIZATION_SERVICE_URL + "/members/getStudentsCount");

        //OTP Related
        serviceUrlMap.put(StringUtils.lowerCase("sendOTP"),
                ClientUtil.ORGANIZATION_SERVICE_URL + "/members/sendOTPApp");
        serviceUrlMap.put(StringUtils.lowerCase("validateOTP"),
                ClientUtil.ORGANIZATION_SERVICE_URL + "/members/validateOTP");

        //Offline Test App related
        serviceUrlMap.put(StringUtils.lowerCase("saveTestUserData"),
                ClientUtil.ORGANIZATION_SERVICE_URL + "/members/saveTestUserData");
        serviceUrlMap.put(StringUtils.lowerCase("getAllUserData"),
                ClientUtil.ORGANIZATION_SERVICE_URL + "/members/getAllUserData");
        //INSTAMOJO FOR ANIL NAIR
        serviceUrlMap.put(StringUtils.lowerCase("getInstaMojoAccessToken"),
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/getInstaMojoAccessToken");
        serviceUrlMap.put(StringUtils.lowerCase("ping"),
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/ping");
        serviceUrlMap.put(StringUtils.lowerCase("getSchedule"),
                ClientUtil.CONTENT_SERVICE_URL + "/classroomconnect/getSchedule");
        serviceUrlMap.put(StringUtils.lowerCase("getDaySchedule"),
                ClientUtil.CONTENT_SERVICE_URL + "/classroomconnect/getDaySchedule");
    }

    public String getServiceUrl(String funtionName) {

        String serviceUrl = serviceUrlMap.get(StringUtils.lowerCase(funtionName));
        // Logger.log4j.info("service url for functionName[" + funtionName + "] : " + serviceUrl);
        return serviceUrl;
    }
}
