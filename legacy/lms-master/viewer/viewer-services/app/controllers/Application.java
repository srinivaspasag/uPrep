package controllers;

import com.vedantu.content.pojos.responses.GetStatusRes;
import play.mvc.Result;
import views.html.index;

public class Application extends AbstractVedantuController {
    public static GetStatusRes res = new GetStatusRes();
    public static Result index() {
        return ok(index.render("Your new application is ready."));
    }

    public static Result ping(){
        res.status = true;
        return ok(getResultResponse(res).toObjectNode());
    }

}
