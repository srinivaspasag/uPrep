/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package controllers;

import static controllers.UIComSecurity.logout;
import static controllers.UIComSecurity.redirectToOriginalURL;
import static controllers.UIComSecurity.redirectUrlKey;

import java.security.Security;
import java.util.Arrays;

import org.json.JSONObject;

import play.Logger;
import play.cache.Cache;
import play.libs.F;
import play.mvc.Scope;
import play.mvc.With;
import uicom.util.ClientUtil;

/**
 * 
 * @author ajith
 */
@With(Security.class)
public class TNC extends AbstractUIController {

    private static final String[] _NAMES = { "aup", "endUserAgreement", "businessAgreement", "sla",
            "sla", "tts"                };

    public static void acceptUserTNC() {

        boolean agree = Scope.Params.current().get("agrees", boolean.class);
        Logger.log4j.info("USER TO ACCEPT TERMS " + agree);
        if (agree) {
            F.Promise<AbstractUIController.JSONResponseWrapper> promise = client(
                    ClientUtil.USER_SERVICE_URL + "/users/acceptTnC", null, null, false);
            Logger.log4j.info("BEFORE AWAIT");
            await(promise);
            Logger.log4j.info("AFTER AWAIT");
            JSONObject resp = getJSON(promise);
            session.remove("needTNC");
            session.remove("TNC-CODE");
            try {
                flash.keep(redirectUrlKey);
                redirectToOriginalURL();
            } catch (Throwable ex) {
                Logger.log4j.error(ex.getMessage());
            }
        } else {
            String orgId = request.params.get("orgId");
            flash.keep(redirectUrlKey);
            logout(orgId);
        }
    }

    public static void acceptBusinessAgreement() {

        String orgId = request.params.get("orgId");
        boolean agree = Scope.Params.current().get("agrees", boolean.class);
        if (agree) {
            F.Promise<AbstractUIController.JSONResponseWrapper> promise = client(
                    ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/acceptTnC", null, null, false);
            Logger.log4j.info("BEFORE AWAIT");
            await(promise);
            Logger.log4j.info("AFTER AWAIT");
            JSONObject resp = getJSON(promise);
            try {
                String userId = session.get("userId");
                String key = "ORG_TNC_INFO_" + orgId + "_" + userId;
                Cache.safeDelete(key);
                flash.keep(redirectUrlKey);
                redirectToOriginalURL();
            } catch (Throwable ex) {
                Logger.log4j.error(ex.getMessage());
            }
        } else {
            flash.keep(redirectUrlKey);
            logout(orgId);
        }
    }

    public static void termsAndConditions() {

        flash.keep(redirectUrlKey);
        render();
    }

    public static void terms() {

        flash.keep(redirectUrlKey);
        render();
    }

    public static void uprepterms() {

        flash.keep(redirectUrlKey);
        render();
    }

    public static void businessAgreement(String orgId) {

        Logger.log4j.info("Requesting Business Agreement");
        flash.keep("version");
        flash.keep("orgId");
        flash.keep(redirectUrlKey);
        render();
    }

    public static void accessDenied(){
        render();
    }

    public static void getTerm(String name, String version) {

        if (name != null && !name.isEmpty() && Arrays.asList(_NAMES).contains(name)) {
            if (version != null && !version.isEmpty()) {
                name += "_" + version;
            }
            name = name + ".html";
            try {
                render(name);
            } catch (Exception ex) {
                Logger.log4j.info(ex.getLocalizedMessage());
                error(404, "Terms Not Found");
            }
        }
    }
}
