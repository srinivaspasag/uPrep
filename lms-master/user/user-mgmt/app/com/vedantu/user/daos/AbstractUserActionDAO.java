package com.vedantu.user.daos;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.Key;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import com.google.code.morphia.query.UpdateResults;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.content.interfaces.BoardUpdatable;
import com.vedantu.commons.content.interfaces.IContent;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.socials.apis.ICommentable;
import com.vedantu.socials.apis.IFollowable;
import com.vedantu.socials.apis.IUpVotable;
import com.vedantu.socials.apis.IViewable;

public abstract class AbstractUserActionDAO<T extends VedantuBaseMongoModel, K> extends
        VedantuBasicDAO<T, K> implements ICommentable, IUpVotable, IFollowable, IViewable,
        BoardUpdatable, IContent {

    private static final ALogger LOGGER = Logger.of(AbstractUserActionDAO.class);

    public AbstractUserActionDAO(Class<T> entityClass) {

        super(entityClass);
    }

    @Override
    public VedantuBaseMongoModel incCommentsCount(String id) {

        UpdateOperations<T> update = getDS().createUpdateOperations(entityClazz).inc(
                ICommentable.FIELD_NAME, 1);

        T model = getDS().findAndModify(getQuery().filter(FIELD_ID, new ObjectId(id)), update);
        return model;
    }

    @Override
    public VedantuBaseMongoModel incViewsCount(String id, int inc) {

        UpdateOperations<T> update = getDS().createUpdateOperations(entityClazz).inc(
                IViewable.FIELD_NAME, inc);

        T model = getDS().findAndModify(getQuery().filter(FIELD_ID, new ObjectId(id)), update);
        return model;
    }

    @Override
    public VedantuBaseMongoModel incUpVotesCount(String id) {

        UpdateOperations<T> update = getDS().createUpdateOperations(entityClazz).inc(
                IUpVotable.FIELD_NAME, 1);

        T model = getDS().findAndModify(getQuery().filter(FIELD_ID, new ObjectId(id)), update);
        return model;
    }

    @Override
    public VedantuBaseMongoModel incFollowersCount(String id, int inc) {

        UpdateOperations<T> update = getDS().createUpdateOperations(entityClazz).inc(
                IFollowable.FIELD_NAME, inc);

        T model = getDS().findAndModify(getQuery().filter(FIELD_ID, new ObjectId(id)), update);
        return model;
    }

    public VedantuBaseMongoModel incSolutionCount(String id, int inc) {

        UpdateOperations<T> update = getDS().createUpdateOperations(entityClazz).inc(
                ConstantsGlobal.SOLUTIONS, inc);

        T model = getDS().findAndModify(getQuery().filter(FIELD_ID, new ObjectId(id)), update);
        return model;
    }

    @Override
    public List<String> update(boolean changeState, boolean remove, List<String> brdIds)
            throws VedantuException {

        List<String> ids = new ArrayList<String>();
        if (!remove && CollectionUtils.isEmpty(brdIds)) {
            return ids;
        }

        Query<T> findQuery = getDS().createQuery(entityClazz);

        List<String> updateFields = new ArrayList<String>();
        updateFields.add("boardIds");

        UpdateOperations<T> updateOperations = getDS().createUpdateOperations(entityClazz);

        if (CollectionUtils.isNotEmpty(updateFields)) {

            // if (updateFields.contains("name")) {
            // updateOperations.set("name", name);
            // }

            if (updateFields.contains("boardIds")) {
                if (remove) {
                    findQuery.field("boardIds").hasAnyOf(brdIds);
                    updateOperations.removeAll("boardIds", brdIds);
                } else {

                    updateOperations.set("boardIds", brdIds);
                }
            }

            // if (updateFields.contains("targetIds")) {
            // updateOperations.set("targetIds", targetIds);
            // }

            // if (updateFields.contains("difficulty")) {
            // updateOperations.set("difficulty", difficulty);
            // }
            //
            // if (updateFields.contains("tags")) {
            // updateOperations.set("tags", tags);
            // }
            //

        }

        if (changeState) {
            // updateOperations.set("published", false);
            updateOperations.set("recordState", VedantuRecordState.TEMPORARY.name());

        }

        List<Key<T>> keys = findQuery.asKeyList();
        UpdateResults<T> results = this.update(findQuery, updateOperations);

        if (results.getHadError()) {
            LOGGER.error("Update  operation failed");
            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR);
        }

        if (CollectionUtils.isNotEmpty(keys)) {
            for (Key<T> key : keys) {
                LOGGER.debug(" Key log" + key.getId().toString());
                ids.add(key.getId().toString());
            }
        }
        return ids;

    }

    @Override
    public List<SrcEntity> getChildren(String id) {

        // TODO Auto-generated method stub
        return null;
    }

}
