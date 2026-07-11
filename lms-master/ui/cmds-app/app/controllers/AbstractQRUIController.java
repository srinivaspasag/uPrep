/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import java.util.HashMap;
import java.util.Map;
import play.Play;

/**
 *
 * @author ajithreddy
 */
public abstract class AbstractQRUIController extends AbstractUIController {

    protected static Map<String, String> getSessionParams() {
        Map<String, String> sessionParams = new HashMap<String, String>();
        if(session.contains("username")&&session.contains("userId")){
            sessionParams.put("appId", Play.configuration.getProperty("auth.appId"));
            sessionParams.put("secretKey", Play.configuration.getProperty("auth.secretKey"));
            sessionParams.put("username", session.get("username"));
            sessionParams.put("userId", session.get("userId"));
            sessionParams.put("callingUserId", session.get("callingUserId"));
            sessionParams.put("organizationId", session.get("organizationId"));
        }
        return sessionParams;
    }
}
