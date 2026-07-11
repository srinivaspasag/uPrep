package com.vedantu.commons.daos;

import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.models.Counter;

public class CounterDAO extends VedantuBasicDAO<Counter, ObjectId> {

    private static final ALogger   LOGGER   = Logger.of(CounterDAO.class);

    public static final CounterDAO INSTANCE = new CounterDAO();

    public CounterDAO() {

        super(Counter.class);

    }

    public long getNextSequence(String collectionName, String field) {

        return getNextSequence(collectionName, field, 1);
    }

    public long getNextSequence(String collectionName, String field, int byValue) {

        UpdateOperations<Counter> updateSequence = getDS().createUpdateOperations(Counter.class);
        updateSequence.inc("value", byValue);

        Query<Counter> findQuery = getDS().createQuery(Counter.class);

        findQuery.field("collection").equal(collectionName);
        findQuery.field("field").equal(field);

        Counter counter = getDS().findAndModify(findQuery, updateSequence, false, true);
        return counter.value;

    }
}
