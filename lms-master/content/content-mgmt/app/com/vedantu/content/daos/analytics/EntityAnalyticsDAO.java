package com.vedantu.content.daos.analytics;

import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.models.analytics.AcademicDimensionType;
import com.vedantu.content.models.analytics.EntityAnalytics;
import com.vedantu.content.models.analytics.EntityMeasures;
import com.vedantu.mongo.VedantuBasicDAO;

public class EntityAnalyticsDAO extends VedantuBasicDAO<EntityAnalytics, ObjectId> {

    private static final ALogger           LOGGER   = Logger.of(EntityAnalyticsDAO.class);

    public static final EntityAnalyticsDAO INSTANCE = new EntityAnalyticsDAO();

    private EntityAnalyticsDAO() {

        super(EntityAnalytics.class);
    }

    public boolean addAnalytics(SrcEntity entity, AcademicDimensionType acadDimType,
            String acadDimId, EntityMeasures measures) {

        LOGGER.debug("addAnalytics for entity: " + entity);
        UpdateOperations<EntityAnalytics> updateOps = getDS()
                .createUpdateOperations(EntityAnalytics.class)
                .inc("measures.attempts", measures.attempts).inc("measures.score", measures.score)
                .inc("measures.correct", measures.correct)
                .inc("measures.incorrect", measures.incorrect).inc("measures.left", measures.left)
                .inc("measures.timeTaken", measures.timeTaken);

        Query<EntityAnalytics> query = getQuery().filter("entity", entity)
                .filter(ACAD_DIM_TYPE, acadDimType).filter(ACAD_DIM_ID, acadDimId);

        final boolean createIfNotPresent = true;
        EntityAnalytics updateResult = getDS().findAndModify(query, updateOps, false,
                createIfNotPresent);

        if(updateResult == null) {
            return false;
        }
        if (measures.score > updateResult.measures.maxScore) {
            updateResult.measures.maxScore = measures.score;
            LOGGER.debug("updating maxScore : " + measures.score);
            save(updateResult);
        }

        LOGGER.debug("update enalytics result");

        return true;
    }

    public EntityAnalytics getEntityAnalytics(SrcEntity entity, AcademicDimensionType acadDimType,
            String acadDimId) {

        Query<EntityAnalytics> query = getQuery().filter(ConstantsGlobal.ENTITY, entity)
                .filter(ACAD_DIM_TYPE, acadDimType).filter(ACAD_DIM_ID, acadDimId);
        return find(query).get();
    }

    private static final String ACAD_DIM_TYPE = "acadDim.type";
    private static final String ACAD_DIM_ID   = "acadDim.id";
}
