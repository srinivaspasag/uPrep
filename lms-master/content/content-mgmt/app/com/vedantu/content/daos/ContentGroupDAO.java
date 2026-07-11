package com.vedantu.content.daos;

import java.util.Arrays;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.mutable.MutableLong;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import com.mongodb.BasicDBObject;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.models.ContentGroup;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuDBResult;

public class ContentGroupDAO extends VedantuBasicDAO<ContentGroup, ObjectId> {

    private static final ALogger        LOGGER   = Logger.of(ContentGroupDAO.class);

    public static final ContentGroupDAO INSTANCE = new ContentGroupDAO();

    private ContentGroupDAO() {

        super(ContentGroup.class);
    }

    public ContentGroup getContents(String groupId, int start, int size, MutableLong totalHits) {

        BasicDBObject findDetailsQuery = new BasicDBObject();
        findDetailsQuery.put(FIELD_ID, new ObjectId(groupId));
        BasicDBObject fieldsQuery = new BasicDBObject();
        fieldsQuery.put(
                ContentGroup.CONTENTS,
                new BasicDBObject(MongoManager.SLICE, Arrays.asList(new Integer(start),
                        new Integer(size))));
        VedantuDBResult<ContentGroup> recordResults = getInfos(findDetailsQuery, fieldsQuery,
                MongoManager.NO_START, MongoManager.NO_LIMIT, null);
        if (CollectionUtils.isNotEmpty(recordResults.results)) {
            ContentGroup record = recordResults.results.get(0);
            return record;
        }
        return null;
    }

    public ContentGroup removeContent(String groupId, SrcEntity entity) {

        UpdateOperations<ContentGroup> updateOps = getDS().createUpdateOperations(this.entityClazz);
        updateOps.disableValidation();
        updateOps.removeAll("contents", entity);
        updateOps.enableValidation();

        Query<ContentGroup> findQuery = getDS().createQuery(this.entityClazz);
        findQuery = findQuery.filter(FIELD_ID, new ObjectId(groupId));
        findQuery.field("contents.type").equal(entity.type);
        findQuery.field("contents.id").equal(entity.id);

        return null;
    }
}
