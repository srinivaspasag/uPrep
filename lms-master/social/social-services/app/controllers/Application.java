package controllers;

import play.mvc.Result;
import views.html.index;

public class Application extends AbstractVedantuController {

    public static final String server = "1";

    public static Result index() {
        return ok(index.render("Your new application is ready."));
    }

    public static Result ping(){
        return ok(getResultResponse(server).toObjectNode());
    }

}
