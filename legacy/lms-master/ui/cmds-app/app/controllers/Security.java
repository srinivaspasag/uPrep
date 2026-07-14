package controllers;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import play.Play;
import play.mvc.Before;
import pojos.UserOrg;
import uicom.response.ErrorInfo;
import uicom.response.JSONResponse;

public class Security extends UIComSecurity {

    @Before(unless = {"login", "logout", "authentify", "authentifyMember", "redirectPage",
        "takeToNotEligiblePage","directaccess",
    "authAccessCode","authentifyAccessCodeMember"})
    static void checkAccess() throws Throwable {

        checkAccessOfRequest();
        String callerUserId = null;
        if (request.params._contains("myUserId")) {
            callerUserId = request.params.get("myUserId");
        }
        String userId = session.get("userId");
        if (callerUserId != null && !callerUserId.equals(userId)) {
            String redirectUrl = request.params.get("newPageOpen");
            redirectUrl = StringUtils.isNotEmpty(redirectUrl) ? redirectUrl : "/";
            render("UIComRegister/redirectPage.html", redirectUrl);
        }
        String orgId = request.params.get("orgId");
        if (StringUtils.isNotEmpty(orgId)) {
            UserOrg orgInfo = Widgets.getCurrentOrgInfo(orgId);
            if (orgInfo != null) {
                String orgUserProfile = orgInfo.getOrgUserProfile();
                if (!(orgUserProfile.equals("MANAGER") || orgUserProfile.equals("TEACHER") || orgUserProfile
                        .equals("EDITOR") || (orgUserProfile.equals("SALESPERSON")))) {
                    String redirectUrl = takeToNotEligiblePage();
                    render("UIComRegister/redirectPage.html", redirectUrl);
                }else{
                    //a check is being made if he is the super admin of the org and if the
                    //business agreement for the org is changed
                    checkIfBusinessTncAccepted(orgId);
                }
            } else {
                String redirectUrl = takeToNotEligiblePage();
                render("UIComRegister/redirectPage.html", redirectUrl);
            }
            /*
             * }else{ session.clear(); flash.keep(redirectUrlKey); String redirectUrl =
             * "/noorgsfound"; render("UIComRegister/redirectPage.html", redirectUrl);
             */
        }
    }

    public static void browseAsUser(String targetUserId) {
        boolean canImpersonate = false;
        try{
                request.params.put("targetUserId", session.get("callingUserId"));
                JSONObject resp = QrPeople._getMemberInfo(null);
                canImpersonate = resp.getJSONObject("result").getJSONObject("info").getBoolean("canImpersonate");
        }catch(Exception ex){}
        if (canImpersonate) {
            session.put("userId", targetUserId);
            renderJSON(new JSONResponse(true));
        } else {
            renderJSON(new JSONResponse(new ErrorInfo("NO_SUFFICIENT_RIGHTS")));
        }
    }

    private static String takeToNotEligiblePage() {

//        UIComSecurity._logout();
        flash.keep(redirectUrlKey);
        String redirectUrl = "/noteligible";
        return redirectUrl;
    }
    public static void authentifyAccessCodeMember(String accessCode, String firstName, String lastName, String email,  String twitterHandle) throws Throwable {

        request.params.put("callingApp", Play.configuration.getProperty("application.name"));
        request.params.put("callingAppId", Play.configuration.getProperty("application.id"));
        _authAccessCodeMember(accessCode, firstName, lastName, email, twitterHandle);
        flash.keep(redirectUrlKey);
        decideOrgPage(null);
    }
    public static void authentify(String username, String password, String orgId) throws Throwable {

        request.params.put("callingApp", Play.configuration.getProperty("application.name"));
        request.params.put("callingAppId", Play.configuration.getProperty("application.id"));
        auth(username, password);
        flash.keep(redirectUrlKey);
        decideOrgPage(orgId);
    }

    public static void authentifyMember(String orgId, String memberId, String password)
            throws Throwable {

        request.params.put("callingApp", Play.configuration.getProperty("application.name"));
        request.params.put("callingAppId", Play.configuration.getProperty("application.id"));
        authMember(orgId, memberId, password);
        flash.keep(redirectUrlKey);
        decideOrgPage(orgId);
    }
    public static void directaccess() throws Throwable{
        request.params.put("dl","true");
        String username=request.params.get("u");
        String password=request.params.get("p");
        request.params.put("username",username);
        request.params.put("password",password);
        request.params.put("callingApp", Play.configuration.getProperty("application.name"));
        request.params.put("callingAppId", Play.configuration.getProperty("application.id"));
        auth(username, password);
        session.put("directLogin",true);
        flash.keep(redirectUrlKey);
        decideOrgPage(null);        
    }
    private static void decideOrgPage(String orgId) throws Throwable {
        if (StringUtils.equals(flash.get(redirectUrlKey), "/") && StringUtils.isNotEmpty(orgId)) {
            redirect("/organization/" + orgId + "/resources");
        } else {
            redirectToOriginalURL();
        }
    }
}
