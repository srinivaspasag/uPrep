package com.vedantu.commons.daos;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import com.google.code.morphia.query.UpdateResults;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.OperationType;
import com.vedantu.commons.models.mongo.EntityOperationStatus;
import com.vedantu.mongo.VedantuBasicDAO;

public class EntityOperationStatusDAO extends VedantuBasicDAO<EntityOperationStatus, ObjectId> {

    private static final ALogger                 LOGGER   = Logger.of(EntityOperationStatus.class);

    public static final EntityOperationStatusDAO INSTANCE = new EntityOperationStatusDAO();

    private EntityOperationStatusDAO() {

        super(EntityOperationStatus.class);
    }

    public EntityOperationStatus getByOType(EntityType type, String id, OperationType oType) {

        Query<EntityOperationStatus> query = getDS().createQuery(EntityOperationStatus.class);
        query.field("id").equal(id);
        query.field("type").equal(type);
        query.field("oType").equal(oType);
        return query.get();
    }

    public boolean incCompletion(String jobId) {

        if (StringUtils.isEmpty(jobId)) {
            return false;
        }

        int retry = 0;
        UpdateResults<EntityOperationStatus> results = null;
        do {

            UpdateOperations<EntityOperationStatus> updateQuery = getDS().createUpdateOperations(
                    EntityOperationStatus.class);
            updateQuery.inc("numOfStepsCompleted");

            Query<EntityOperationStatus> query = getDS().createQuery(EntityOperationStatus.class);
            //
            // query.field("numOfSteps").lessThan(")"
            query = query.where("this.numOfSteps>=this.numOfStepsCompleted +1");
          
            query.field(FIELD_ID).equal(new ObjectId(jobId));
            LOGGER.debug("Find Query "+ query.toString());
            results = this.update(query, updateQuery);
        } while (retry < 5 && results.getHadError());
        if (retry == 5 || (results != null && results.getHadError())) {
            LOGGER.error(" Failed to updated job status " + jobId);
            return false;
        }
        return true;
    }

    public boolean incSteps(String jobId) {

        int retry = 0;
        UpdateResults<EntityOperationStatus> results = null;
        do {

            UpdateOperations<EntityOperationStatus> updateQuery = getDS().createUpdateOperations(
                    EntityOperationStatus.class);
            updateQuery.inc("numOfSteps");

            Query<EntityOperationStatus> query = getDS().createQuery(EntityOperationStatus.class);
            query.field(FIELD_ID).equal(new ObjectId(jobId));
            results = this.update(query, updateQuery);
        } while (retry < 5 && results.getHadError());
        if (retry == 5 || (results != null && results.getHadError())) {
            LOGGER.error(" Failed to updated job status " + jobId);
            return false;
        }
        return true;
    }

    public boolean updateException(String jobId, VedantuException exception) {

        LOGGER.debug("Updating jobId" + jobId + " with exception " + exception);
        int retry = 0;
        UpdateResults<EntityOperationStatus> results = null;
        do {

            UpdateOperations<EntityOperationStatus> updateQuery = getDS().createUpdateOperations(
                    EntityOperationStatus.class);

            if (exception != null) {
                updateQuery.set("message", exception.getMessage());
                updateQuery.set("errorCode", exception.errorCode);
            } else {
                updateQuery.set("message", "");
                updateQuery.set("errorCode", "");
            }
            Query<EntityOperationStatus> query = getDS().createQuery(EntityOperationStatus.class);
            query.field(FIELD_ID).equal(new ObjectId(jobId));
            results = this.update(query, updateQuery);
        } while (retry < 5 && results.getHadError());
        if (retry == 5 || (results != null && results.getHadError())) {
            LOGGER.error(" Failed to updated job status " + jobId);
            return false;
        }
        return true;
    }

    public boolean updateErrorCode(String jobId, String errorCode) {

        int retry = 0;
        UpdateResults<EntityOperationStatus> results = null;
        do {

            UpdateOperations<EntityOperationStatus> updateQuery = getDS().createUpdateOperations(
                    EntityOperationStatus.class);
            updateQuery.set("errorCode", errorCode);

            Query<EntityOperationStatus> query = getDS().createQuery(EntityOperationStatus.class);
            query.field(FIELD_ID).equal(new ObjectId(jobId));
            results = this.update(query, updateQuery);
        } while (retry < 5 && results.getHadError());
        if (retry == 5 || (results != null && results.getHadError())) {
            LOGGER.error(" Failed to updated job status " + jobId);
            return false;
        }
        return true;
    }
}
