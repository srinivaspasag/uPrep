package com.vedantu.cmds.daos;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
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
import com.vedantu.commons.content.interfaces.IPublishable;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.models.AbstractBoardEntityTagModel;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuRecordState;

public abstract class CmdsContentDAO<T extends VedantuBaseMongoModel, K> extends
        VedantuBasicDAO<T, K> implements BoardUpdatable, IPublishable {

    private static final ALogger LOGGER = Logger.of(CmdsContentDAO.class);

    public CmdsContentDAO(Class<T> entityClazz) {

        super(entityClazz);
    }

    public boolean isContentByContentSrcAndCodeExists(SrcEntity contentSrc, String code) {

        Query<T> query = getDS().find(entityClazz);
        code = code.trim().toLowerCase();
        query.or(query.criteria(ConstantsGlobal.CODE).equal(code),
                query.criteria(ConstantsGlobal.CODE).equal(code.toUpperCase()));
        T model = query.filter("contentSrc.id", contentSrc.id)
                .filter("contentSrc.type", contentSrc.type)
                .retrievedFields(true, ConstantsGlobal.CODE).get();

        return model != null;
    }

    public boolean isMovingAllowed(String id) throws VedantuException {

        return true;
    }

    public boolean deleteContent(VedantuBaseMongoModel vedantuBaseMongoModel)
            throws VedantuException {

        return true;
    }

    public String getName(String id) throws VedantuException {

        if (StringUtils.isEmpty(id)) {
            return null;
        }
        T t = getDS().find(entityClazz).filter(FIELD_ID, new ObjectId(id))
                .filter(VedantuBasicDAO.RECORD_STATE, VedantuRecordState.ACTIVE)
                .retrievedFields(true, "name").get();
        if (t instanceof AbstractBoardEntityTagModel) {
            return ((AbstractBoardEntityTagModel) t).name;
        }
        return null;
    }

    @Override
    public List<String> update(boolean changeState, boolean remove, List<String> brdIds)
            throws VedantuException {

        List<String> ids = new ArrayList<String>();
        if (!remove && CollectionUtils.isEmpty(brdIds)) {
            return ids;
        }

        List<String> updateFields = new ArrayList<String>();
        updateFields.add(AbstractBoardEntityTagModel.BOARD_IDS);

        UpdateOperations<T> updateOperations = getDS().createUpdateOperations(entityClazz);

        Query<T> findQuery = getDS().createQuery(entityClazz);

        if (CollectionUtils.isNotEmpty(updateFields)) {

            // if (updateFields.contains("name")) {
            // updateOperations.set("name", name);
            // }
            if (updateFields.contains(AbstractBoardEntityTagModel.BOARD_IDS)) {
                if (remove) {
                    findQuery.field(AbstractBoardEntityTagModel.BOARD_IDS).hasAnyOf(brdIds);
                    updateOperations.removeAll(AbstractBoardEntityTagModel.BOARD_IDS, brdIds);
                } else {

                    updateOperations.set(AbstractBoardEntityTagModel.BOARD_IDS, brdIds);

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
            updateOperations.set(AbstractBoardEntityTagModel.COMPLETED, false);
            updateOperations.set("published", false);
        }
        LOGGER.debug("Find Query " + findQuery.toString());
        List<Key<T>> keys = findQuery.asKeyList();

        UpdateResults<T> results = this.update(findQuery, updateOperations);

        if (results.getHadError()) {
            LOGGER.error("Update brdIds operation failed");
            throw new VedantuException(VedantuErrorCode.CAN_NOT_BE_UPDATED);
        }

        if (CollectionUtils.isNotEmpty(keys)) {
            for (Key<T> key : keys) {
                LOGGER.debug(" Key log" + key.getId().toString());
                ids.add(key.getId().toString());
            }
        }
        return ids;
    }

}
