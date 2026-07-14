package handlers;

import play.Logger;
import play.http.HttpErrorHandler;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Created by Raghu Teja on 30-06-2017.
 */
public class LpErrorHandler implements HttpErrorHandler {
    private static final Logger.ALogger LOGGER = Logger.of("ErrorHandler");

    public static final CompletableFuture<Result> CLIENT_ERROR =
            CompletableFuture.completedFuture(Controller.notFound("Page Not Found"));
    private static final CompletableFuture<Result> SERVER_ERROR =
            CompletableFuture.completedFuture(Controller.internalServerError("Internal Server Error"));

    @Override
    public CompletionStage<Result> onClientError(Http.RequestHeader request, int statusCode, String message) {
        LOGGER.error("Status: " + statusCode + "   Description: " + message);
        return CLIENT_ERROR;
    }

    @Override
    public CompletionStage<Result> onServerError(Http.RequestHeader request, Throwable exception) {
        LOGGER.error("Error occurred on backend: " + String.valueOf(exception.getMessage()), exception);
        return SERVER_ERROR;
    }
}
