package com.vedantu.billing.dao;

import org.bson.types.ObjectId;

import com.mongodb.MongoException.DuplicateKey;
import com.vedantu.billing.enums.ItemCategory;
import com.vedantu.billing.models.ProvidedTax;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.mongo.VedantuBasicDAO;

public class TaxDAO extends VedantuBasicDAO<ProvidedTax, ObjectId> {

    public static TaxDAO INSTANCE = new TaxDAO();

    private TaxDAO() {

        super(ProvidedTax.class);
    }

    public boolean addTax(String name, String desc, float percentage, String location,
            ItemCategory category) throws VedantuException {

        try {
            ProvidedTax tax = new ProvidedTax(name, percentage, desc, location, category);
            save(tax);
        } catch (DuplicateKey keyException) {
            throw new VedantuException(VedantuErrorCode.ALREADY_ADDED);

        }
        return true;

    }
    // TODO add delete or update later on
}
