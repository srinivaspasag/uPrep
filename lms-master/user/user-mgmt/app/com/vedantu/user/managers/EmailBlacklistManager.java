package com.vedantu.user.managers;

import org.apache.commons.lang3.StringUtils;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.MongoManager.SortOrder;
import com.vedantu.mongo.VedantuDBResult;
import com.vedantu.user.daos.EmailBlacklistDAO;
import com.vedantu.user.models.EmailBlacklist;
import com.vedantu.user.pojos.requests.BlacklistEmailReq;
import com.vedantu.user.pojos.requests.GetBlacklistEmailReq;
import com.vedantu.user.pojos.requests.GetBlacklistedEmailsReq;
import com.vedantu.user.pojos.responses.BlacklistEmailRes;
import com.vedantu.user.pojos.responses.GetBlacklistEmailRes;
import com.vedantu.user.pojos.responses.GetBlacklistedEmailsRes;

public class EmailBlacklistManager {

    public static EmailBlacklistManager INSTANCE = new EmailBlacklistManager();

    private EmailBlacklistManager() {

        super();
    }

    public BlacklistEmailRes addEmailToBlacklist(BlacklistEmailReq req) throws VedantuException {

        BlacklistEmailRes res = new BlacklistEmailRes();
        res.success = EmailBlacklistDAO.INSTANCE.addToBlacklist(req.email, req.reason) != null;
        return res;
    }

    public BlacklistEmailRes removeEmailFromBlacklist(BlacklistEmailReq req)
            throws VedantuException {

        BlacklistEmailRes res = new BlacklistEmailRes();
        res.success = EmailBlacklistDAO.INSTANCE.removeFromBlacklist(req.email, req.reason) != null;
        return res;
    }

    public GetBlacklistEmailRes getBlacklistInfo(GetBlacklistEmailReq req) throws VedantuException {

        EmailBlacklist emailBlacklist = EmailBlacklistDAO.INSTANCE.getBlacklistInfo(req.email);
        if (emailBlacklist == null) {
            throw new VedantuException(VedantuErrorCode.ACTIVITY_NOT_AVAILABLE, "email : "
                    + req.email + " was never blacklisted");
        }
        GetBlacklistEmailRes res = toBlacklistEmailPojo(emailBlacklist);
        return res;
    }

    public GetBlacklistedEmailsRes getBlacklistedEmails(GetBlacklistedEmailsReq req)
            throws VedantuException {

        GetBlacklistedEmailsRes res = new GetBlacklistedEmailsRes();
        DBObject query = new BasicDBObject();

        if (StringUtils.isNotEmpty(req.query)) {
            query.put(ConstantsGlobal.EMAIL, req.query.toLowerCase().trim());
        }

        VedantuDBResult<EmailBlacklist> result = EmailBlacklistDAO.INSTANCE.getInfos(
                query,
                null,
                req.start,
                req.size,
                MongoManager.getSortQuery(ConstantsGlobal.TIME_CREATED,
                        SortOrder.valueOfKey(req.sortOrder).name()));
        res.totalHits = result.totalHits;
        for (EmailBlacklist info : result.results) {
            res.list.add(toBlacklistEmailPojo(info));
        }
        return res;
    }

    private GetBlacklistEmailRes toBlacklistEmailPojo(EmailBlacklist emailBlacklist) {

        GetBlacklistEmailRes res = new GetBlacklistEmailRes();
        res.email = emailBlacklist.email;
        res.blacklisted = emailBlacklist.blacklisted;
        res.infos = emailBlacklist.infos;
        return res;
    }

}
