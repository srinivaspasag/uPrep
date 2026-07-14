package com.vedantu.content.daos;

import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.content.models.DoubtTransaction;
import com.vedantu.mongo.VedantuBasicDAO;

public class DoubtTransactionDAO extends VedantuBasicDAO<DoubtTransaction, ObjectId> {

    private static final ALogger            LOGGER   = Logger.of(DoubtTransactionDAO.class);

    public static final DoubtTransactionDAO INSTANCE = new DoubtTransactionDAO();

    private DoubtTransactionDAO() {
        super(DoubtTransaction.class);
    }

    public DoubtTransaction addDoubtTransaction(String discussionId) {
        DoubtTransaction doubtTransaction = new DoubtTransaction(discussionId);
        save(doubtTransaction);
        return doubtTransaction;
    }

    public DoubtTransaction getByDiscussionId(String discussionId) {
        DoubtTransaction doubtTransaction = getQuery().filter("discussionId", discussionId).get();
        return doubtTransaction;
    }

}
