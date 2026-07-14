package controllers;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.Play;
import play.mvc.Before;
import play.mvc.Scope;

public class Security extends UIComSecurity {


    @Before(unless = { "login", "logout", "authentify", "authentifyMember", "redirectPage" })
    static void checkAccess() throws Throwable{
        checkAccessOfRequest();
        String allowedUserName = play.Play.configuration.getProperty("login.username");
        Logger.log4j.info("USERNAME ENTERED ======== "+session.get("username")+", REQUIRED ====== "+allowedUserName);
        if (!StringUtils.equals(session.get("username"), allowedUserName)) {
            session.clear();
            flash.put("loginError", "NO_SUFFICIENT_RIGHTS");
            redirect("/login");
        }
    }

    public static void authentify(String username, String password) throws Throwable {

        Scope.Params.current()
                .put("callingApp", Play.configuration.getProperty("application.name"));
        Scope.Params.current()
                .put("callingAppId", Play.configuration.getProperty("application.id"));
        auth(username, password);
        redirectToOriginalURL();
    }

    public static void login() {

        render();
    }
}
