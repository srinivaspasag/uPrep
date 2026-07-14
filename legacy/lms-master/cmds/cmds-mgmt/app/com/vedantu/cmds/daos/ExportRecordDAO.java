package com.vedantu.cmds.daos;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableLong;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import com.google.code.morphia.query.UpdateResults;
import com.mongodb.BasicDBObject;
import com.vedantu.cmds.enums.ExportState;
import com.vedantu.cmds.models.ExportRecord;
import com.vedantu.cmds.pojos.export.EntityExportRecord;
import com.vedantu.commons.content.interfaces.IDownloadable;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuDBResult;
import com.vedantu.mongo.VedantuRecordState;

public class ExportRecordDAO extends VedantuBasicDAO<ExportRecord, ObjectId> implements
        IDownloadable {

    private static final ALogger        LOGGER   = Logger.of(ExportRecordDAO.class);

    public static final ExportRecordDAO INSTANCE = new ExportRecordDAO();

    private ExportRecordDAO() {

        super(ExportRecord.class);
    }

    @Override
    public String getDownloadName(String id, VedantuBaseMongoModel record) {

        ExportRecord currentRecord = null;

        if (record == null) {
            currentRecord = getById(id);
        } else {
            if (record instanceof ExportRecord) {
                currentRecord = (ExportRecord) record;
            }
        }

        if (currentRecord == null) {
            return null;
        }

        String newName = null;
        if (StringUtils.isNotEmpty(currentRecord.name)) {
            newName = currentRecord.name.trim().replaceAll(" ", "_");

        }
        return newName;

    }

    public boolean updateEntityExport(String exportId, EntityExportRecord record) {

        UpdateOperations<ExportRecord> updateOps = getDS().createUpdateOperations(this.entityClazz);
        updateOps.disableValidation();
        updateOps.set("sources.$.timeExported", record.timeExported);
        updateOps.set("sources.$.exportedSize", record.exportedSize);
        updateOps.set("sources.$.succeeded", record.succeeded);
        if (StringUtils.isNotEmpty(record.errorCode)) {
            updateOps.set("sources.$.errorCode", record.errorCode);
        }
        updateOps.enableValidation();
        Query<ExportRecord> findQuery = getDS().createQuery(this.entityClazz);
        findQuery = findQuery.filter(FIELD_ID, new ObjectId(exportId));
        findQuery.field("sources.content").equal(record.content);
        
        UpdateResults<ExportRecord> results = this.update(findQuery, updateOps);

        if (!results.getHadError()) {
            return true;
        }

        LOGGER.error(" Error occured while updated entity export record " + results.getError());
        return false;

    }

    public List<ExportRecord> getExports(Set<String> sections, ExportState state, int start,
            int size, MutableLong totalHits) {

        Query<ExportRecord> recordQuery = getDS().createQuery(this.entityClazz);
        recordQuery.filter("target.type", EntityType.SECTION);
        if (CollectionUtils.isNotEmpty(sections)) {
            recordQuery.field("target.id").in(sections);
        }
        if (state != null && state != ExportState.UNKNOWN) {
            recordQuery.filter("state", state);
        }
        recordQuery.filter("recordState", VedantuRecordState.ACTIVE);
        recordQuery.retrievedFields(false, "sources");
        recordQuery.order("-timeCreated").offset(start).limit(size);
        totalHits.setValue(recordQuery.countAll());

        return recordQuery.asList();

    }

    public ExportRecord
            getExportDetails(String exportId, int start, int size, MutableLong totalHits) {

        BasicDBObject findDetailsQuery = new BasicDBObject();
        findDetailsQuery.put(FIELD_ID, new ObjectId(exportId));
        BasicDBObject fieldsQuery = new BasicDBObject();
        fieldsQuery.put(
                "sources",
                new BasicDBObject(MongoManager.SLICE, Arrays.asList(new Integer(start),
                        new Integer(size))));
        VedantuDBResult<ExportRecord> recordResults = getInfos(findDetailsQuery, fieldsQuery,
                MongoManager.NO_START, MongoManager.NO_LIMIT, null);
        if (CollectionUtils.isNotEmpty(recordResults.results)) {
            ExportRecord record = recordResults.results.get(0);
            
            return record;
        }
        return null;
    }

    public boolean update(String exportId, List<ExportState> olderStates, ExportState newer,
            boolean notWithOlderState) {

        UpdateOperations<ExportRecord> updateOps = getDS().createUpdateOperations(this.entityClazz);
        updateOps.disableValidation();
        updateOps.set("state", newer);

        updateOps.enableValidation();
        Query<ExportRecord> findQuery = getDS().createQuery(this.entityClazz);
        findQuery.filter(FIELD_ID, new ObjectId(exportId));
        if (olderStates != null && !notWithOlderState) {
            findQuery.field("state").hasAnyOf(olderStates);
        }else if( olderStates !=null && notWithOlderState ){
            findQuery.field("state").hasNoneOf(olderStates);
        }

        UpdateResults<ExportRecord> results = this.update(findQuery, updateOps);

        int count = results.getInsertedCount();
        LOGGER.error(" Document count " +results.getInsertedCount() + "  "+results.getUpdatedCount()+ results.getUpdatedExisting());

        if (!results.getHadError() ) {
            return true;
        }

        LOGGER.error(" Error occured whileupdated entity export record " + results.getError());
        return false;

    }
}
