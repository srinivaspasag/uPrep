package handlers;

import play.Logger;
import play.http.DefaultActionCreator;
import play.mvc.Action;
import play.mvc.Http;
import response.NotAuthorized;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Intercepts all actions, checks if public key matches our saved key, and on succesful verification, allows request to proceed
 * Created by Raghu Teja on 03-07-2017.
 */
public class ActionHandler extends DefaultActionCreator {
    private static final Logger.ALogger LOGGER = Logger.of("CommonRequestHandler");

    private static final String PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC+" +
            "Jq72OpSqeuc5uN3tUirzqJJxQr2D0PS909WriLgxSvtVf+" +
            "j83ECJIK+ZZ/TDm15tGKwhGTQdseglTo5NkjvhGrW2f+" +
            "gSC3KfO5W5OUVNRFuYhz3B9bESBSrgD3JotKJM+" +
            "P3DsMKoPxt+eTC4JsylHh82dnDi6EugWdRlEmYsWwIDAQAB";

    @Override
    public Action createAction(Http.Request request, Method actionMethod) {
        if(!checkValidRequest(request)) {
            return NotAuthorized.getInstance();
        }
        return super.createAction(request, actionMethod);
    }

    private boolean checkValidRequest(Http.Request request) {
        Http.Headers headers = request.getHeaders();
        List<String> authorizations = headers.getAll("publicKey");
        if(authorizations == null || authorizations.size() != 1) {
            int size = authorizations == null? 0 : authorizations.size();
            LOGGER.error("Missing or incorrect number of public key headers. Expected 1, got " + size);
            return false;
        }
        String publicKey = authorizations.get(0);
        boolean authorized = PUBLIC_KEY.equals(publicKey);
        if (!authorized) {
            LOGGER.error("Invalid publicKey. " + publicKey);
        }
        else {
            LOGGER.debug("Public Key is valid");
        }
        return authorized;
    }
}
