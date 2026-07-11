package response;

import handlers.LpErrorHandler;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import java.util.concurrent.CompletionStage;

/**
 * Created by Raghu Teja on 03-07-2017.
 */
public final class NotAuthorized extends Action.Simple {
    private static NotAuthorized INSTANCE;

    private NotAuthorized() {
    }

    public static NotAuthorized getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new NotAuthorized();
        }
        return INSTANCE;
    }

    @Override
    public CompletionStage<Result> call(Http.Context ctx) {
        return LpErrorHandler.CLIENT_ERROR;
    }
}
