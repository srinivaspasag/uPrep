package controllers;

import play.Logger;
import play.Logger.ALogger;
import play.data.Form;
import play.mvc.Result;
import views.html.index;

import com.vedantu.organization.pojos.requests.Guest;

public class Application extends AbstractVedantuController {

	private static final ALogger LOGGER = Logger.of(Application.class);

	public static final String server = "1";

	public static Result index() {
		LOGGER.debug("testing log message");
		return ok(index.render("Your new application is ready."));
	}
	public static Result sayHello(String name) {
		return ok("Hello " + name + "!");
	}
	private static Form<Guest> guestForm = Form.form(Guest.class);

	public static Result sayHelloFormally() {
		Guest guest = guestForm.bindFromRequest().get();
		if (null == guest) {
			guest = new Guest();
			guest.firstName = "guest";
		}
		return ok("Hello " + (null != guest.firstName ? guest.firstName : "")
				+ " " + (null != guest.lastName ? guest.lastName : "") + "!");
	}

	public static Result ping(){
        return ok(getResultResponse(server).toObjectNode());
    }

}
