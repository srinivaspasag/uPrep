package com.vedantu.cmds.daos;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.mutable.MutableLong;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import com.mongodb.MongoException.DuplicateKey;
import com.vedantu.cmds.mgmt.interfaces.ICMDSResource;
import com.vedantu.cmds.mgmt.interfaces.IContainable;
import com.vedantu.cmds.models.CMDSQuestionSet;
import com.vedantu.cmds.models.event.search.details.CMDSResourceDetails;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuRecordState;

public class CMDSQuestionSetDAO extends CmdsContentDAO<CMDSQuestionSet, ObjectId> implements
        ICMDSResource, IContainable<CMDSQuestionSet> {

    private static final ALogger           LOGGER   = Logger.of(CMDSQuestionDAO.class);

    public static final CMDSQuestionSetDAO INSTANCE = new CMDSQuestionSetDAO();

    public CMDSQuestionSetDAO() {

        super(CMDSQuestionSet.class);
    }

    @Override
    public CMDSResourceDetails getCMDSResourceDetails(VedantuBaseMongoModel model) {

        CMDSQuestionSet set = (CMDSQuestionSet) model;
        CMDSResourceDetails details = new CMDSResourceDetails();
        details.fromMongoModel(model);
        details.content = new SrcEntity(EntityType.CMDSQUESTIONSET, set._getStringId());
        details.queryContext = set.name;
        details.name = set.name;
        LOGGER.debug("question set: " + set);
        return details;
    }

    @Override
    public VedantuBaseMongoModel getPublishedEntity(String id) throws VedantuException {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SrcEntity getGlobalEntity(String id) {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isPublished(String id) {

        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isReadyToPublished(String id) throws VedantuException {

        return false;
    }

    @Override
    public boolean isReadyToPublished(VedantuBaseMongoModel cmdsModel) throws VedantuException {

        if (cmdsModel instanceof CMDSQuestionSet) {
            return true;
        }
        return false;
    }

    @Override
    public List<SrcEntity> getChildren(String id) {

        return null;
    }

    @Override
    public boolean isPublished(VedantuBaseMongoModel model) {

        return false;
    }

    @Override
    public List<CMDSQuestionSet> getContainers(String id, int start, int size,
            VedantuRecordState state, MutableLong totalHits) {

        if (id == null) {
            return null;
        }
        Query<CMDSQuestionSet> findQuery = getQuery();
        findQuery.field(CMDSQuestionSet.QUESTION_IDS).contains(id);
        if (state != null) {
            findQuery.field(ConstantsGlobal.RECORD_STATE).equal(state);
        }
        totalHits.setValue(findQuery.countAll());
        return findQuery.offset(start).limit(size).asList();

    }

    public CMDSQuestionSet updateToProcessing(String questionSetId) {

        if (StringUtils.isEmpty(questionSetId)) {
            return null;
        }

        Query<CMDSQuestionSet> findQuery = getQuery();
        findQuery.field(FIELD_ID).equal(new ObjectId(questionSetId));
        findQuery.field(RECORD_STATE).equal(VedantuRecordState.TEMPORARY);
        UpdateOperations<CMDSQuestionSet> update = getDS().createUpdateOperations(this.entityClazz);
        update.set(ConstantsGlobal.RECORD_STATE, VedantuRecordState.CONFIRMING);
        CMDSQuestionSet questionSet = getDS().findAndModify(findQuery, update, true);
        return questionSet;
    }

    @Override
    public boolean deleteByModel(VedantuBaseMongoModel model) throws VedantuException {

        if (model.recordState != VedantuRecordState.DELETED) {
            return updateState((CMDSQuestionSet) model, VedantuRecordState.DELETED);
        }
        return false;
    }

    /**
     * Use this cautiously with recordState updates
     * 
     */
    @Override
    public void updateModel(CMDSQuestionSet model, List<String> fields) throws VedantuException {

        UpdateOperations<CMDSQuestionSet> updates = getUpdateOperations(model, fields);
        try {
            Query<CMDSQuestionSet> findQuery = getQuery();
            findQuery.field(FIELD_ID).equal(new ObjectId(model._getStringId()));
            getDS().update(findQuery, updates, false);
        } catch (DuplicateKey exception) {
            LOGGER.error("duplicate key exception", exception);
            throw new VedantuException(VedantuErrorCode.ALREADY_ADDED);
        }

    }

}
