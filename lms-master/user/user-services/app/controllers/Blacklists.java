package controllers;

import play.data.Form;
import play.mvc.Result;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.user.managers.EmailBlacklistManager;
import com.vedantu.user.pojos.requests.BlacklistEmailReq;
import com.vedantu.user.pojos.requests.GetBlacklistEmailReq;
import com.vedantu.user.pojos.requests.GetBlacklistedEmailsReq;
import com.vedantu.user.pojos.responses.BlacklistEmailRes;
import com.vedantu.user.pojos.responses.GetBlacklistEmailRes;
import com.vedantu.user.pojos.responses.GetBlacklistedEmailsRes;

public class Blacklists extends AbstractVedantuController {

    public static Result blacklist() {

        Form<BlacklistEmailReq> form = Form.form(BlacklistEmailReq.class).bindFromRequest();
        if (form.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        BlacklistEmailReq req = form.get();
        BlacklistEmailRes res = null;
        try {
            res = EmailBlacklistManager.INSTANCE.addEmailToBlacklist(req);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(res).toObjectNode());
    }

    public static Result removeFromBlacklist() {

        Form<BlacklistEmailReq> form = Form.form(BlacklistEmailReq.class).bindFromRequest();
        if (form.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        BlacklistEmailReq req = form.get();
        BlacklistEmailRes res = null;
        try {
            res = EmailBlacklistManager.INSTANCE.removeEmailFromBlacklist(req);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(res).toObjectNode());
    }

    public static Result getBlacklistInfo() {

        Form<GetBlacklistEmailReq> form = Form.form(GetBlacklistEmailReq.class).bindFromRequest();
        if (form.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        GetBlacklistEmailReq req = form.get();
        GetBlacklistEmailRes res = null;
        try {
            res = EmailBlacklistManager.INSTANCE.getBlacklistInfo(req);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(res).toObjectNode());
    }

    public static Result getBlacklistedEmails() {

        Form<GetBlacklistedEmailsReq> form = Form.form(GetBlacklistedEmailsReq.class)
                .bindFromRequest();
        if (form.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        GetBlacklistedEmailsReq req = form.get();
        GetBlacklistedEmailsRes res = null;
        try {
            res = EmailBlacklistManager.INSTANCE.getBlacklistedEmails(req);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(res).toObjectNode());
    }

}
