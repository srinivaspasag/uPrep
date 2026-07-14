package com.vedantu.commons.daos;

import org.bson.types.ObjectId;

import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.models.VedantuUniqueCode;

public class VedantuUniqueCodeDAO extends VedantuBasicDAO<VedantuUniqueCode, ObjectId> {

    public static final VedantuUniqueCodeDAO INSTANCE = new VedantuUniqueCodeDAO();

    private VedantuUniqueCodeDAO() {

        super(VedantuUniqueCode.class);
    }
}
