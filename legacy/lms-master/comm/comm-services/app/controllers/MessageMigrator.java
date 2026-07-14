package controllers;

import java.io.IOException;

import play.Logger;
import play.Logger.ALogger;
import play.mvc.Result;

import com.vedantu.comm.upgrades.MessageUpdater;
import com.vedantu.commons.JSONResponse;
import com.vedantu.commons.VedantuException;

public class MessageMigrator extends AbstractVedantuController {

    private static final ALogger LOGGER = Logger.of(MessageMigrator.class);

    public static Result updateConversations() throws IOException {

        try {

            // request = requestForm.get();
            MessageUpdater.migrateConversations();
        } catch (VedantuException e) {
            return ok((new JSONResponse(e)).toObjectNode());

        }

        return ok(getResultResponse(true).toObjectNode());

    }

    public static Result updateUserConversations() throws IOException {

        try {

            // request = requestForm.get();
            MessageUpdater.migrateUserConversations();
        } catch (VedantuException e) {
            return ok((new JSONResponse(e)).toObjectNode());

        }

        return ok(getResultResponse(true).toObjectNode());

    }
}
