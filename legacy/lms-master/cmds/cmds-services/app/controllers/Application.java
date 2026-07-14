package controllers;

import play.mvc.Result;

public class Application extends AbstractVedantuController {

    public static final String server = "1";

    public static Result index() {
//        return ok(index.render("Your new application is ready."));
        return ok();
    }

    public static Result ping(){
        return ok(getResultResponse(server).toObjectNode());
    }

}
