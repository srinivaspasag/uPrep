package util;

import play.Play;

public class ClientUtil {

    public final static String WEB_APP_URL = Play.configuration.getProperty("WEB_APP_URL");
    public final static String CMDS_APP_URL = Play.configuration.getProperty("CMDS_APP_URL");
    public final static String USER_SERVICE_URL = Play.configuration.getProperty("USER_SERVICE_URL");
    public final static String ORGANIZATION_SERVICE_URL = Play.configuration.getProperty("ORGANIZATION_SERVICE_URL");
    public final static String CONTENT_SERVICE_URL = Play.configuration.getProperty("CONTENT_SERVICE_URL");
    public final static String COMM_SERVICE_URL = Play.configuration.getProperty("COMM_SERVICE_URL");
    public final static String CMDS_SERVICE_URL = Play.configuration.getProperty("CMDS_SERVICE_URL");
    public final static String EVENT_BUS_SERVICE_URL = Play.configuration.getProperty("EVENT_BUS_SERVICE_URL");
    public final static String BOARDS_SERVICE_URL = Play.configuration.getProperty("BOARDS_SERVICE_URL");
    public final static String SOCIALS_WEB_SERVICE_URL = Play.configuration.getProperty("SOCIALS_WEB_SERVICE_URL");
    public final static String BILLING_WEB_SERVICE_URL = Play.configuration.getProperty("BILLING_WEB_SERVICE_URL");
    public final static String LOGIN_SERVICE_URL = USER_SERVICE_URL + "/users/authenticateUser";
    public final static String AUTH_ACCESS_CODE_SERVICE_URL = ORGANIZATION_SERVICE_URL + "/organizations/getSectionByAccessCode";
    public final static String LOGIN_ACCESS_CODE_SERVICE_URL = ORGANIZATION_SERVICE_URL + "/members/addMemberWithAccessCode";
    public final static String LOGIN_MEMBER_SERVICE_URL = ORGANIZATION_SERVICE_URL + "/members/authenticateMember";
    public final static String SIGNUP_MEMBER_SERVICE_URL = ORGANIZATION_SERVICE_URL + "/members/addMember";
    public final static String LOGOUT_SERVICE_URl = USER_SERVICE_URL + "/users/logout";



    public final static String LIB_WEB_SERVICE_URL = Play.configuration.getProperty("LIB_WEB_SERVICE_URL");
    public final static String PROFILE_WEB_SERVICE_URL = Play.configuration.getProperty("PROFILE_WEB_SERVICE_URL");
    public final static String COLLEGES_WEB_SERVICE_URL = Play.configuration.getProperty("COLLEGES_WEB_SERVICE_URL");
    public final static String POINTS_WEB_SERVICE_URL = Play.configuration.getProperty("POINTS_WEB_SERVICE_URL");
    public final static String MYDESK_WEB_SERVICE_URL = Play.configuration.getProperty("MYDESK_WEB_SERVICE_URL");
    public final static String FOLLOW_WEB_SERVICE_URL = Play.configuration.getProperty("FOLLOW_WEB_SERVICE_URL");
    public final static String GROUPS_WEB_SERVICE_URL = Play.configuration.getProperty("GROUPS_WEB_SERVICE_URL");
    public final static String RECOS_WEB_SERVICE_URL = Play.configuration.getProperty("RECOS_WEB_SERVICE_URL");
    public final static String SEARCH_WEB_SERVICE_URL = Play.configuration.getProperty("SEARCH_WEB_SERVICE_URL");
    public final static String NEWSFEED_WEB_SERVICE_URL = Play.configuration.getProperty("NEWSFEED_WEB_SERVICE_URL");
    public final static String QUESTIONS_WEB_SERVICE_URL = Play.configuration.getProperty("QUESTIONS_WEB_SERVICE_URL");
    public final static String BOARDS_WEB_SERVICE_URL = Play.configuration.getProperty("BOARDS_WEB_SERVICE_URL");
    public final static String CLICK_STREAM_WEB_SERVICE_URL = Play.configuration.getProperty("CLICK_STREAM_WEB_SERVICE_URL");
    public final static String MASTERPLAN_WEB_SERVICE_URL = Play.configuration.getProperty("MASTERPLAN_WEB_SERVICE_URL");
    public final static String DIAGRAM_WEB_SERVICE_URL = Play.configuration.getProperty("DIAGRAM_WEB_SERVICE_URL");

    //push notification to gcm
    public final static String GCM_URL  =  Play.configuration.getProperty("NOTIFICATION_GCM_URL");

    public final static String CREATE_USER_SERVICE_URl = PROFILE_WEB_SERVICE_URL + "/userauthentication/createuser";

    public final static String APP_ID_FIELD_NAME = "appId";
    public final static String APP_ID_FIELD_VALUE = "12345";
    public final static String USER_NAME_FIELD_NAME = "username";

    //TEST
    public final static String DEFAULT_TEST_CACHE_EXPIRY_TIME_HR = Play.configuration.getProperty("DEFAULT_TEST_CACHE_EXPIRY_TIME_HR");
    public final static long TEST_CACHE_GRACE_TIME_MILLISEC = Long.parseLong(Play.configuration.getProperty("TEST_CACHE_GRACE_TIME_MILISEC"));

    public final static String DEFAULT_FETCH_SIZE_10 = "10";
    public final static String DEFAULT_FETCH_SIZE_50 = "50";
    public final static String DEFAULT_FETCH_START = "0";
    public final static String INST_DEFAULT_IMG_PATH = Play.configuration.getProperty("INST_DEFAULT_IMG_PATH");


    //CMDS
    public final static String CMDS_WEB_SERVICE_URL = Play.configuration.getProperty("CMDS_WEB_SERVICE_URL");
    public static String MSG_WEB_SERVICE_URL = Play.configuration.getProperty("MESSAGE_WEB_SERVICE_URL");

    public enum Entity{
        DOCUMENT,FILE,VIDEO,DISCUSSION,QUESTION,CHALLENGE,TEST,ASSIGNMENT,UNKNOWN
    ,   STATUSFEED, USER, REMARK,MODULE}
    public enum ActivityAction{
            OPEN,EDIT,VIEW,COMMENT,DELETE,ATTEMPTED,ADD
            //CMDS
            ,MAKE_VISIBLE, PUBLISH, UPLOAD, MOVE, CHANGE,
            DEACTIVATED_ACTIVATED, REORDER
    }
    public enum ActivityPages{
            // LEARN / DEVICE
            DOCUMENT,FILE,VIDEO,DISCUSSION,QUESTION,CHALLENGE,TEST,ASSIGNMENT,NEW_ENTITY,
            STATUS_FEED,DOUBTS,MEMBERS,SCHEDULE,LIBRARY,TEST_LEADERS,ANALYTICS,
            INSTITUTE_HOME,CHALLENGES,PROFILE,MSG_INBOX,MSG_CONVERSATION,USER_SETTINGS
            ,TEST_ATTEMPT,PRE_TEST,TEST_ANALYTICS,HOME
            //CMDS
            ,ORGANIZATION_INFO, PROGAM_LIBRARY, QUESTIONS, MARKS_SHEET, FOLDER, RESOURCE,
            ADD_CONTENT, ADD_QUESTION, QUESTION_SET, QUESTION_SOLUTION, EDIT_QUESTION,
            TEST_CREATION, ASSIGNMENT_CREATION, TEST_SERIES_CREATION, TEST_SERIES,
            HELP_CENTER, ACADEMIC_STRUCTURE, PROGRAM, CENTER, SECTION, BOARDS, STUDENT,
            MEMBER, MEMBER_PWD, MEMBER_USERNAME, DEVICE_MGNT, DEVICE_ACTIVITY , LIB_EXPORTS,
            OFFLINE_USER,PLANS,DEACTIVATION_POPUP,INVOICES,EXT_SIGNUP,MODULE,MODULE_ENTRY,
            INVENTORY,SHIPMENTS,COUPONS
    }
}
