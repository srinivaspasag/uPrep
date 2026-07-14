package com.vedantu.content.commons.daos;

import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.content.models.tests.Test;
import com.vedantu.mongo.VedantuBasicDAO;

public class TestDAO extends VedantuBasicDAO<Test, ObjectId>{
    private static final ALogger            LOGGER   = Logger.of(TestDAO.class);

    public static final TestDAO INSTANCE = new TestDAO();

    private TestDAO() {
        super(Test.class);
    }
        


}
