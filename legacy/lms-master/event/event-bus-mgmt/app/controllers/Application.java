package controllers;

import org.json.JSONException;
import org.json.JSONObject;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

import com.vedantu.eventbus.email.details.AbstractEmailNotificationDetails;
import com.vedantu.eventbus.email.details.VoteEmailNotificationDetails;
import com.vedantu.user.pojos.UserEmailInfo;

public class Application extends Controller {

    public static Result index() throws JSONException {

        AbstractEmailNotificationDetails details = new VoteEmailNotificationDetails();
        details.user = new UserEmailInfo();
        JSONObject json = new JSONObject();
        JSONObject name = new JSONObject();
        name.put("firstName", "Shankar");
        name.put("lastName", "Kotuli");
        name.put("id", "userId1");
        json.put("actor", name);

        JSONObject src = new JSONObject();
        src.put("type", "TEST");
        src.put("id", "123");
        src.put("name", "Test 1");
        src.put("why", "OWNER");
        src.put("contentSrc", new JSONObject("{\"id\" : \"adadfadsf\"}"));
        json.put("src", src);
        details.details = json;
        details.user.firstName = "SSK";
        details.user.lastName = "Kotuli";
        return ok(index.render(details));
    }

}
