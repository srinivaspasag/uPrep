package controllers;

import play.Logger;
import play.Logger.ALogger;
import play.data.Form;
import play.mvc.Result;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.content.managers.ApplicationManager;
import com.vedantu.content.pojos.requests.GetContentForDemoReq;
import com.vedantu.content.pojos.requests.GetContentReq;
import com.vedantu.content.pojos.responses.GetContentRes;
import com.vedantu.content.pojos.responses.GetStatusRes;

public class Application extends AbstractVedantuController {

    private static final ALogger LOGGER = Logger.of(Application.class);
    public static GetStatusRes res = new GetStatusRes();
    public static Result getContentResponse() throws VedantuException {

        Form<GetContentReq> requestForm = Form.form(GetContentReq.class).bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }
        GetContentReq request = requestForm.get();
        GetContentRes getContentRes = ApplicationManager.getContentResponse(request);
        return ok(getResultResponse(getContentRes).toObjectNode());
    }

    public static Result getContentForDemo() throws VedantuException {
        Form<GetContentForDemoReq> requestForm = Form.form(GetContentForDemoReq.class).bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }
        GetContentForDemoReq request = requestForm.get();
        GetContentRes getContentRes = ApplicationManager.getContentForDemo(request);
        return ok(getResultResponse(getContentRes).toObjectNode());
    }

    public static Result ping(){
        res.status = true;
        return ok(getResultResponse(res).toObjectNode());
    }

}
