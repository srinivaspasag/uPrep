package com.vedantu.mongo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.dao.BasicDAO;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import com.google.code.morphia.query.UpdateResults;
import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException.DuplicateKey;
import com.mongodb.WriteResult;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.commons.pojos.responses.ModelExtendedInfo;
import com.vedantu.commons.utils.ObjectIdUtils;
import com.vedantu.commons.utils.ObjectMapperUtils;

public abstract class VedantuBasicDAO<T extends VedantuBaseMongoModel, K> extends BasicDAO<T, K> {

    private static final ALogger  LOGGER       = Logger.of(VedantuBasicDAO.class);
    protected static final String FIELD_ID     = "_id";
    protected static final String RECORD_STATE = "recordState";

    public VedantuBasicDAO(Class<T> entityClass) {

        super(entityClass, MongoManager.INSTANCE.getMongo(), MorphiaManager.INSTANCE.getMorphia(),
                MongoManager.INSTANCE.getDBName());
    }

    protected Datastore getDS() {

        return MorphiaManager.INSTANCE.getDS();
    }

    @SuppressWarnings("unchecked")
    public <B extends ModelBasicInfo> B getBasicInfo(String id) {

        T result = getById(id);
        LOGGER.debug("......in getBasicDAO......" + result + id);
        B basicInfo = null != result ? (B) result.toBasicInfo() : null;

        return basicInfo;
    }

    @SuppressWarnings("unchecked")
    public <E extends ModelExtendedInfo> E getExtendedInfo(String id) {

        T result = getById(id);
        E extendedInfo = null != result ? (E) result.toExtendedInfo() : null;

        return extendedInfo;
    }

    public T findOne(DBObject query, DBObject fields) {

        return findOne(query, fields, null);
    }

    public T findOne(DBObject query, DBObject fields, DBObject orderBy) {

        DBCollection coll = getDS().getCollection(entityClazz);
        DBObject result = coll.findOne(query, fields, orderBy);
        T model = ObjectMapperUtils.convertToVedantuBaseModel(result, entityClazz);
        return model;
    }

    public VedantuDBResult<T> getInfos(DBObject query, DBObject fields, int start, int size,
            DBObject orderQuery) {

        DBCollection coll = getDS().getCollection(entityClazz);
        LOGGER.info("EntityClass : " + getEntityClass() + ", query:" + query + ", fields:" + fields
                + ", orderQuery:" + orderQuery + ", start:" + start + ", size:" + size);
        DBCursor cursor = null;
        if (fields != null) {
            cursor = coll.find(query, fields);
        } else {
            cursor = coll.find(query);
        }
        if (orderQuery != null) {
            cursor = cursor.sort(orderQuery);
        }
        cursor = cursor.skip(start).limit(size);

        VedantuDBResult<T> result = new VedantuDBResult<T>();
        result.totalHits = cursor.count();
        while (cursor.hasNext()) {
            DBObject d = cursor.next();
            LOGGER.debug("dbObject : " + d);
            T model = ObjectMapperUtils.convertToVedantuBaseModel(d, entityClazz);
            result.results.add(model);
        }
        return result;
    }

    public void markActive(T entity) {

        entity.recordState = VedantuRecordState.ACTIVE;
        // updateState(entity, VedantuRecordState.ACTIVE);
    }

    public void markActive(String id) {

        updateState(getById(id), VedantuRecordState.ACTIVE);
    }

    public void markDeleted(T entity) {

        entity.recordState = VedantuRecordState.DELETED;
        // updateState(entity, VedantuRecordState.DELETED);
    }

    public void markDeleted(String id) {

        updateState(getById(id), VedantuRecordState.DELETED);
    }

    public boolean updateState(T entity, VedantuRecordState state) {

        LOGGER.debug("Updated entity :" + entity + " new state " + state + this.entityClazz + " T"
                + entity.getClass());

        UpdateOperations<T> updateState = getDS().createUpdateOperations(entityClazz);
        updateState.set(RECORD_STATE, state);
        Query<T> updateQuery = this.getQuery();
        updateQuery.filter(FIELD_ID, entity.id);
        LOGGER.debug("Updated entity :" + entity + " with query " + updateQuery.toString());
        UpdateResults<T> results = this.update(updateQuery, updateState);
        if (!results.getHadError()) {
            LOGGER.debug("Updated recordstate to :" + state);
            entity.recordState = state;
            return true;
        }
        LOGGER.error("Unable to update recordstate to :" + state);
        return false;
    }

    @Override
    public WriteResult delete(T entity) {

        WriteResult r = super.delete(entity);
        entity.recordState = VedantuRecordState.DELETED;
        return r;
    }

    public T getById(String id) {

        if (ObjectIdUtils.hasInvalidId(id)) {
            return null;
        }
        return getById(new ObjectId(id));
    }

    public T getById(ObjectId id) {

        if (null == id) {
            return null;
        }
        T t = getDS().find(entityClazz).filter(FIELD_ID, id).get();
        return t;
    }

    public T getById(String id, VedantuRecordState state) {

        if (null == id) {
            return null;
        }

        return getById(new ObjectId(id), state);
    }

    public T getById(ObjectId id, VedantuRecordState state) {

        if (null == id) {
            return null;
        }
        T t = getDS().find(entityClazz).filter(FIELD_ID, id).filter(RECORD_STATE, state).get();
        return t;
    }

    public List<T> getByIds(List<ObjectId> ids) {

        if (CollectionUtils.isEmpty(ids)) {
            return null;
        }
        List<T> ts = getDS().find(entityClazz).field(FIELD_ID).hasAnyOf(ids).asList();
        return ts;

    }

    public List<T> getByIds(List<ObjectId> ids, VedantuRecordState state) {

        if (CollectionUtils.isEmpty(ids)) {
            return null;
        }
        List<T> ts = getDS().find(entityClazz).field(FIELD_ID).hasAnyOf(ids)
                .filter(RECORD_STATE, state).asList();
        return ts;
    }

    public List<T> getByIds(List<ObjectId> ids, VedantuRecordState state,
            List<SortOrderInfo> sortOrders) {

        if (CollectionUtils.isEmpty(ids)) {
            return null;
        }
        Query<T> query = getDS().find(entityClazz).field(FIELD_ID).hasAnyOf(ids)
                .filter(RECORD_STATE, state).order(StringUtils.join(sortOrders, ','));
        LOGGER.debug("Executing query" + query.toString() + " sort by "
                + StringUtils.join(sortOrders, ','));
        List<T> ts = query.asList();
        return ts;
    }

    protected Query<T> getQuery() {

        return getDS().createQuery(entityClazz);
    }

    public long count(DBObject query) {

        return getCollection().count(query);
    }

    public void updateModel(T model, List<String> fields) throws VedantuException {

        try {
            LOGGER.debug("updating fields : " + fields + ", entityClazz: " + entityClazz);
            UpdateOperations<T> update = getUpdateOperations(model, fields);

            getDS().update(getDS().createQuery(entityClazz).filter(FIELD_ID, model.id), update);
        } catch (DuplicateKey exception) {
            LOGGER.error("duplicate key exception", exception);
            throw new VedantuException(VedantuErrorCode.ALREADY_ADDED);
        } catch (Exception e) {
            Logger.error(e.getMessage(), e);
            throw new VedantuException(VedantuErrorCode.CAN_NOT_BE_UPDATED, e);
        }
    }

    protected UpdateOperations<T> getUpdateOperations(T model, List<String> fields)
            throws VedantuException {

        UpdateOperations<T> update = getDS().createUpdateOperations(entityClazz);
        try {
            for (String field : fields) {
                Object value = null;
                if (field.contains(".")) {
                    value = getQualifiedFields(model, field);

                } else {
                    if (field.isEmpty() || field == null) {
                        continue;
                    }

                    value = model.getClass().getField(field).get(model);

                }
                if (value == null) {
                    update.unset(field);

                } else {
                    update.set(field, value);
                }
            }
            update.set(ConstantsGlobal.LAST_UPDATED, System.currentTimeMillis());

            return update;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new VedantuException(VedantuErrorCode.CAN_NOT_BE_UPDATED, e);
        }
    }

    private Object getQualifiedFields(T model, String field) throws NoSuchFieldException,
            IllegalArgumentException, SecurityException, IllegalAccessException {

        if (field.contains(".")) {
            Object value = model;
            String[] splits = field.split("\\.");
            for (int i = 0; i < splits.length; i++) {
                value = value.getClass().getField(splits[i]).get(value);
            }
            return value;
        }
        throw new NoSuchFieldException(field);
    }

    /**
     * this will return userId of element with _id=id
     *
     * @return
     */
    public String getOwnerId(String id) {

        if (ObjectIdUtils.hasInvalidId(id)) {
            return null;
        }
        DBObject query = new BasicDBObject(FIELD_ID, new ObjectId(id));
        DBObject result = getCollection().findOne(
                query,
                MongoManager.getFieldsDBObject(Arrays.asList(ConstantsGlobal.USER_ID),
                        MongoManager.INCLUDE_FIELD));
        if (result != null) {
            return (String) result.get(ConstantsGlobal.USER_ID);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    static final <T extends VedantuBaseMongoModel, B extends ModelBasicInfo> List<B> _toBasicInfos(
            List<T> results) {

        List<B> basicInfos = new ArrayList<B>();
        if (CollectionUtils.isNotEmpty(results)) {
            for (T t : results) {
                basicInfos.add((B) t.toBasicInfo());
            }
        }
        return basicInfos;
    }

    @SuppressWarnings("unchecked")
    static final <T extends VedantuBaseMongoModel, E extends ModelExtendedInfo> List<E>
            _toExtendedInfos(List<T> results) {

        List<E> extendedInfos = new ArrayList<E>();
        if (CollectionUtils.isNotEmpty(results)) {
            for (T t : results) {
                extendedInfos.add((E) t.toExtendedInfo());
            }
        }
        return extendedInfos;
    }

    public final <B extends ModelBasicInfo> List<B> toBasicInfos(List<T> results) {

        return _toBasicInfos(results);
    }

    public final <E extends ModelExtendedInfo> List<E> toExtendedInfos(List<T> results) {

        return _toExtendedInfos(results);
    }

    public final Map<String, T> toInfosMap(Collection<T> results) {

        Map<String, T> infosMap = new LinkedHashMap<String, T>();
        if (CollectionUtils.isNotEmpty(results)) {
            for (T t : results) {
                if (null == t) {
                    continue;
                }
                infosMap.put(t._getStringId(), t);
            }
        }
        return infosMap;
    }

    @SuppressWarnings("unchecked")
    public final <B extends ModelBasicInfo> Map<String, B> toBasicInfosMap(Collection<T> results) {

        Map<String, B> infosMap = new LinkedHashMap<String, B>();
        if (CollectionUtils.isNotEmpty(results)) {
            for (T t : results) {
                if (null == t) {
                    continue;
                }
                infosMap.put(t._getStringId(), (B) t.toBasicInfo());
            }
        }
        return infosMap;
    }

    @SuppressWarnings("unchecked")
    public final <E extends ModelExtendedInfo> Map<String, E> toExtendedInfosMap(
            Collection<T> results) {

        Map<String, E> infosMap = new LinkedHashMap<String, E>();
        if (CollectionUtils.isNotEmpty(results)) {
            for (T t : results) {
                if (null == t) {
                    continue;
                }
                infosMap.put(t._getStringId(), (E) t.toExtendedInfo());
            }
        }
        return infosMap;
    }

    public final <B extends ModelBasicInfo> Map<String, B> basicInfosToMap(Collection<B> results) {

        Map<String, B> infosMap = new LinkedHashMap<String, B>();
        if (CollectionUtils.isNotEmpty(results)) {
            for (B b : results) {
                if (null == b) {
                    continue;
                }
                infosMap.put(b.id, b);
            }
        }
        return infosMap;
    }

    protected void log(ALogger logger, UpdateResults<T> result) {

        if (null == result) {
            return;
        }
        logger.info("updateResult insertCount: " + result.getInsertedCount() + ", updateCount: "
                + result.getUpdatedCount() + ", newId: " + result.getNewId() + ", hadError: "
                + result.getHadError() + ", error: " + result.getError());
    }

    public Set<String> getExistingIds(List<String> ids) {

        Set<String> existingIds = new HashSet<String>();

        List<ObjectId> oIds = ObjectIdUtils.toObjectIds(ids, true);
        DBObject query = new BasicDBObject(FIELD_ID, new BasicDBObject(MongoManager.IN_QUERY, oIds));
        DBObject fields = MongoManager.getFieldsDBObject(Arrays.asList(FIELD_ID),
                MongoManager.INCLUDE_FIELD);
        VedantuDBResult<T> tResults = getInfos(query, fields, MongoManager.NO_START,
                MongoManager.NO_LIMIT, null);
        for (T t : tResults.results) {
            existingIds.add(t._getStringId());
        }
        return existingIds;
    }

    public Set<String> getIdsByTime(long minTimeCreated, long maxTimeCreated, int start, int size) {

        return getIdsByTime(minTimeCreated, maxTimeCreated, start, size, null);
    }

    public Set<String> getIdsByTime(long minTimeCreated, long maxTimeCreated, int start, int size,
            VedantuRecordState state) {

        List<T> models = getByTime(minTimeCreated, maxTimeCreated, start, size, state);
        Set<String> ids = new HashSet<String>();
        for (T model : models) {
            ids.add(model._getStringId());
        }
        return ids;
    }

    public List<T> getByTime(long minTimeCreated, long maxTimeCreated, int start, int size) {

        return getByTime(minTimeCreated, maxTimeCreated, start, size, null);

    }

    public List<T> getByTime(long minTimeCreated, long maxTimeCreated, int start, int size,
            VedantuRecordState state) {

        Query<T> query = getQuery();

        if (minTimeCreated > 0) {
            query.field("timeCreated").greaterThanOrEq(minTimeCreated);
        }
        if (maxTimeCreated > 0) {
            query.field("timeCreated").lessThan(maxTimeCreated);
        }
        if (state != null) {
            query.filter(RECORD_STATE, state);
        }
        query.offset(start).limit(size);
        LOGGER.debug(" Query " + query + " start : " + start + " size " + size);
        return query.asList();
    }

    @SuppressWarnings("unchecked")
    public List<Object> getDistinct(String key, DBObject query) {

        return getCollection().distinct(key, query);
    }

    public AggregationOutput aggregate(DBObject firstOp, DBObject... additionalOps) {

        LOGGER.info("aggregation query : " + firstOp + "," + Arrays.asList(additionalOps));
        AggregationOutput aggregationOutput = getCollection().aggregate(firstOp, additionalOps);
        LOGGER.debug("aggregationOutput : " + aggregationOutput);
        return aggregationOutput;
    }

    public VedantuBaseMongoModel instantiate() throws VedantuException {

        try {
            return this.entityClazz.newInstance();
        } catch (InstantiationException e) {
            LOGGER.error("Can not instiate class", e);
            throw new VedantuException(VedantuErrorCode.UNSUPPORTED_CONTENT_TYPE);
        } catch (IllegalAccessException e) {
            LOGGER.error("Can not instiate class", e);
            throw new VedantuException(VedantuErrorCode.UNSUPPORTED_CONTENT_TYPE);
        }

    }

    public boolean update(DBObject query, DBObject object, boolean upsert, boolean multi) {

        DBCollection coll = getDS().getCollection(entityClazz);

        LOGGER.info("EntityClass : " + getEntityClass() + ", query:" + query + "update" + object
                + ", upsert allowed:" + upsert + ", multi:" + multi);
        WriteResult result = coll.update(query, object, upsert, multi);
        if (result.getError() != null) {
            LOGGER.debug("Error found in data " + result.getError());
            return false;
        }
        return true;
    }

    public Set<String> getIds(DBObject query) {

        Set<String> ids = new HashSet<String>();
        LOGGER.debug("query : " + query);
        DBCursor cur = getCollection()
                .find(query,
                        MongoManager.getFieldsDBObject(Arrays.asList(FIELD_ID),
                                MongoManager.INCLUDE_FIELD));
        while (cur.hasNext()) {
            ids.add(cur.next().get(FIELD_ID).toString());
        }
        LOGGER.debug("returning ids : " + ids);
        return ids;
    }

}
