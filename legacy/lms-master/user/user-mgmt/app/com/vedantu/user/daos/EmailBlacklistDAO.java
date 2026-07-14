package com.vedantu.user.daos;

import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.user.enums.EmailBlacklistAction;
import com.vedantu.user.models.EmailBlacklist;

public class EmailBlacklistDAO extends VedantuBasicDAO<EmailBlacklist, ObjectId> {

    private static final ALogger          LOGGER   = Logger.of(EmailBlacklistDAO.class);

    public static final EmailBlacklistDAO INSTANCE = new EmailBlacklistDAO();

    private EmailBlacklistDAO() {

        super(EmailBlacklist.class);
    }

    public EmailBlacklist addToBlacklist(String email, String reason) throws VedantuException {

        EmailBlacklist emailBlacklist = getQuery().filter(ConstantsGlobal.EMAIL,
                email.toLowerCase()).get();
        if (emailBlacklist == null) {
            emailBlacklist = new EmailBlacklist(email);
        }
        LOGGER.debug("adding  blacklist info for email: " + email);
        emailBlacklist.addBlacklistInfo(reason, EmailBlacklistAction.BLOCKED);
        save(emailBlacklist);
        return emailBlacklist;

    }

    public EmailBlacklist removeFromBlacklist(String email, String reason) {

        EmailBlacklist emailBlacklist = getQuery().filter(ConstantsGlobal.EMAIL,
                email.toLowerCase()).get();
        if (emailBlacklist == null) {
            emailBlacklist = new EmailBlacklist(email);
        }
        emailBlacklist.addBlacklistInfo(reason, EmailBlacklistAction.UNBLOCKED);
        save(emailBlacklist);
        return emailBlacklist;

    }

    public EmailBlacklist getBlacklistInfo(String email) {

        EmailBlacklist emailBlacklist = getQuery().filter(ConstantsGlobal.EMAIL,
                email.toLowerCase()).get();
        return emailBlacklist;

    }

    public boolean isBlacklisted(String email) {

        EmailBlacklist emailBlacklist = getQuery()
                .filter(ConstantsGlobal.EMAIL, email.toLowerCase()).filter("blacklisted", true)
                .retrievedFields(true, FIELD_ID).get();
        return emailBlacklist != null;

    }

}
