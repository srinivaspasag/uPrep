package controllers;

import play.Logger;
import play.Logger.ALogger;
import play.data.Form;
import play.mvc.Result;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.organization.managers.UserTokenManager;
import com.vedantu.organization.pojos.requests.organizations.AddUserTokenReq;
import com.vedantu.organization.pojos.responses.organizations.AddUserTokenRes;

public class UserTokens extends AbstractVedantuController {

    private static final ALogger LOGGER = Logger.of(UserTokens.class);

    public static Result addUserToken() {

        Form<AddUserTokenReq> addUserTokenForm = Form.form(AddUserTokenReq.class).bindFromRequest();
        if (addUserTokenForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(addUserTokenForm))).toObjectNode());
        }
        AddUserTokenReq addUserTokenReq = addUserTokenForm.get();
        AddUserTokenRes addUserTokenRes = null;
        addUserTokenRes = UserTokenManager.addUserToken(addUserTokenReq);
        return ok(getResultResponse(addUserTokenRes).toObjectNode());
    }
}
