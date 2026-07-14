package com.vedantu.content.daos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.EncryptionLevel;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.pojos.ScheduleInfo;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.daos.analytics.UserEntityAttemptDAO;
import com.vedantu.content.models.LibraryContentLink;
import com.vedantu.content.models.analytics.UserEntityAttempt;
import com.vedantu.content.pojos.requests.GetEntityReq;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuDBResult;
import com.vedantu.mongo.VedantuRecordState;

public class LibraryContentLinksDAO extends VedantuBasicDAO<LibraryContentLink, ObjectId> {

    private static final ALogger               LOGGER   = Logger.of(LibraryContentLinksDAO.class);
    public static final LibraryContentLinksDAO INSTANCE = new LibraryContentLinksDAO(
                                                                LibraryContentLink.class);

    public LibraryContentLinksDAO(Class<LibraryContentLink> entityClass) {

        super(entityClass);
    }

    public LibraryContentLink addLink(SrcEntity content, SrcEntity targetEntity,
            UserActionType linkType, String actorId, Scope scope) throws VedantuException {

        return addLink(content, targetEntity, linkType, actorId, scope, null);
    }

    public LibraryContentLink addLink(SrcEntity content, SrcEntity targetEntity,
            UserActionType linkType, String actorId, Scope scope, ScheduleInfo scheduleInfo)
            throws VedantuException {

        return addLink(content, targetEntity, linkType, actorId, scope, null, null);
    }

    public LibraryContentLink addLink(SrcEntity content, SrcEntity targetEntity,
            UserActionType linkType, String actorId, Scope updatedScope, ScheduleInfo scheduleInfo,
            Boolean downloadble) throws VedantuException {

        return addLink(content, targetEntity, linkType, actorId, updatedScope, scheduleInfo,
                downloadble, EncryptionLevel.NA, false);
    }

    /* TODO Added by Shivank */
    public LibraryContentLink addLink(SrcEntity content, SrcEntity targetEntity,
            UserActionType linkType, String actorId, Scope updatedScope, ScheduleInfo scheduleInfo,
            Boolean downloadble, EncryptionLevel encLevel, boolean allowDuplicates)
            throws VedantuException {

        return addLink(content, targetEntity, linkType, actorId, updatedScope, scheduleInfo,
                downloadble, encLevel, allowDuplicates, null, -1);
    }

    /**
     * It will add active link to library
     *
     * @param content
     * @param targetEntity
     * @param linkType
     * @param actorId
     * @param updatedScope
     * @param scheduleInfo
     * @param downloadble
     * @param encLevel
     * @return
     * @throws VedantuException
     */
    public LibraryContentLink addLink(SrcEntity content, SrcEntity targetEntity,
            UserActionType linkType, String actorId, Scope updatedScope, ScheduleInfo scheduleInfo,
            Boolean downloadble, EncryptionLevel encLevel, boolean allowDuplicates,
            List<SrcEntity> downloadableEntities, long position) throws VedantuException {

        LOGGER.debug("...................Inside function add link..............");

        MutableLong totalHits = new MutableLong(0L);
        List<LibraryContentLink> links = getLibraryContentLinks(content, targetEntity, linkType,
                actorId, VedantuRecordState.ACTIVE, 0, 1, totalHits);

        if (totalHits.longValue() > 1 && !allowDuplicates) {
            LOGGER.error("content:" + content + ", already added to  targetEntity:" + targetEntity);
            throw new VedantuException(VedantuErrorCode.ALREADY_ADDED, "content:" + content
                    + ", already added to  targetEntity:" + targetEntity);

        }

        LibraryContentLink contentLinkage = null;

        if (CollectionUtils.isNotEmpty(links)) {
            contentLinkage = links.get(0);
            LOGGER.debug("Updating  content Link" + contentLinkage);

        } else {
            LOGGER.debug("Creating new content Link");
            contentLinkage = new LibraryContentLink(targetEntity, content);

        }
        if (position != -1) {
            contentLinkage.position = position;
        }
        if (scheduleInfo != null) {
            contentLinkage.setSchedule(scheduleInfo);
        }
        if (actorId != null) {
            contentLinkage.userId = actorId;
        }
        if (linkType != null) {
            contentLinkage.linkType = linkType;
        }
        if (updatedScope != null && updatedScope != Scope.UNKNOWN) {
            LOGGER.debug("New scope" + updatedScope);
            contentLinkage.setScope(updatedScope);
        }
        if (downloadble != null) {
            LOGGER.debug("New downloadable state" + downloadble);

            if (contentLinkage.getScope() == Scope.PRIVATE && downloadble) {
                LOGGER.debug("Can not make downloadable" + downloadble);
                throw new VedantuException(VedantuErrorCode.CONTENT_NOT_VISIBLE, "content:"
                        + content + ",is not visible" + targetEntity);
            }
            contentLinkage.setDownloadable(downloadble);
        }
        if (encLevel != null
                && (contentLinkage.getEncLevel() != EncryptionLevel.NA || encLevel != EncryptionLevel.NA)) {
            contentLinkage.setEncLevel(encLevel);
        }

        if (content.type == EntityType.MODULE) {
            contentLinkage.setDownloadableEntities(downloadableEntities);
        }

        LOGGER.debug("Saving content link in ILE library"
                + contentLinkage.getDownloadableEntities());
        save(contentLinkage);

        return contentLinkage;
    }

    public List<LibraryContentLink> getLinkBy(SrcEntity source, int start, int size,
            MutableLong totalHits) {

        return getLibraryContentLinks(source, null, null, null, start, size, totalHits);
    }

    public LibraryContentLink getLibraryContentLink(SrcEntity content, SrcEntity targetEntity,
            UserActionType linkType, VedantuRecordState recordState, MutableLong totalHits) {

        List<LibraryContentLink> contentLinks = getLibraryContentLinks(content, targetEntity,
                linkType, null, recordState, 0, 1, totalHits);
        if (CollectionUtils.isNotEmpty(contentLinks)) {
            return contentLinks.get(0);
        }

        return null;
    }

    // add retrieved field here
    public List<LibraryContentLink> getLibraryContentLinks(SrcEntity content,
            SrcEntity targetEntity, UserActionType linkType, String actorId, int start, int size,
            MutableLong totalHits) {

        return getLibraryContentLinks(content, targetEntity, linkType, actorId,
                VedantuRecordState.ACTIVE, start, size, totalHits);
    }

    public List<LibraryContentLink> getLibraryContentLinks(SrcEntity content,
            SrcEntity targetEntity, UserActionType linkType, String actorId,
            VedantuRecordState recordState, int start, int size, MutableLong totalHits) {

        Query<LibraryContentLink> getContentQuery = getDS().createQuery(LibraryContentLink.class);
        LOGGER.debug("Querying for " + LibraryContentLink.class);
        getContentQuery = addTargetFilter(getContentQuery, targetEntity);
        getContentQuery = addSourceFilter(getContentQuery, content);
        getContentQuery = addLinkTypeFilter(getContentQuery, linkType);
        if (recordState != null) {
            getContentQuery.filter("recordState", recordState);
        }
        getContentQuery = getContentQuery.offset(start).limit(size);
        LOGGER.debug("Query: " + getContentQuery.toString());
        List<LibraryContentLink> libraryLinks = getContentQuery.asList();
        if (totalHits != null) {
            long count = getContentQuery.countAll();
            totalHits.setValue(count);
            LOGGER.debug("Total matched results " + totalHits);
        }

        return libraryLinks;
    }
    
    public List<LibraryContentLink> getLibraryContentLinksOfAProgram(String  target,Set<String> digitalLibraryHiddenFields){
    	
    	Query<LibraryContentLink> getContentQuery = getDS().createQuery(LibraryContentLink.class);
    	getContentQuery.filter("target.id", target);
    	List<String> entities=new ArrayList<String>(Arrays.asList("TEST","VIDEO","ASSIGNMENT","DOCUMENT","MODULE"));
    	for(String entity:digitalLibraryHiddenFields){
    		if(entity.equalsIgnoreCase("Assignments")){
    			LOGGER.info("Assignments");
    			entities.remove("ASSIGNMENT");
    		}
    		else if(entity.equalsIgnoreCase("Videos")){
    			LOGGER.info("Videos");
    			entities.remove("VIDEO");
    		}
    		else if(entity.equalsIgnoreCase("Tests")){
    			LOGGER.info("Tests");
    			entities.remove("TEST");
    		}
    		else if(entity.equalsIgnoreCase("Documents")){
    			LOGGER.info("Documents");
    			entities.remove("DOCUMENT");
    		}
    	}
    	LOGGER.info("entities : "+entities);
//    	LOGGER.info("list"+getContentQuery.asList());
    	getContentQuery.filter("recordState", VedantuRecordState.ACTIVE);
    	getContentQuery.field("source.type").hasAnyOf(entities);
    	LOGGER.info("getContentQuery : "+getContentQuery);
    	List<LibraryContentLink> libraryContentLinks=getContentQuery.asList();
    	LOGGER.info("getLibraryContentLinks1 : "+libraryContentLinks);
    	return libraryContentLinks;
    	
    }

    public boolean remove(SrcEntity content, SrcEntity targetEntity, UserActionType linkType)
            throws VedantuException {

        Query<LibraryContentLink> getContentQuery = getDS().createQuery(LibraryContentLink.class);
        getContentQuery = addTargetFilter(getContentQuery, targetEntity);
        getContentQuery = addSourceFilter(getContentQuery, content);
        getContentQuery = addLinkTypeFilter(getContentQuery, linkType);

        LOGGER.debug("Query: " + getContentQuery.toString());

        WriteResult deleteResult = deleteByQuery(getContentQuery);
        if (StringUtils.isNotEmpty(deleteResult.getError())) {
            LOGGER.error("Error occured :" + deleteResult.getError());
            throw new VedantuException(VedantuErrorCode.CAN_NOT_BE_REMOVED);
        }
        return true;
    }

    public boolean updateLastUpdated(SrcEntity content) throws VedantuException {

        long lastUpdated = System.currentTimeMillis();
        LOGGER.debug("............" + content.id + ".....Entering updateLastUpdated.....");
        UpdateOperations<LibraryContentLink> updateLastUpdated = getDS().createUpdateOperations(
                LibraryContentLink.class).set(ConstantsGlobal.LAST_UPDATED, lastUpdated);

        Query<LibraryContentLink> getContentQuery = getDS().createQuery(LibraryContentLink.class);
        getContentQuery = addSourceFilter(getContentQuery, content);
        UpdateResults<LibraryContentLink> updateResults = getDS().update(getContentQuery,
                updateLastUpdated);
        if (updateResults.getHadError()) {
            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR);
        }
        LOGGER.debug("...Exiting updateLastUpdated...");
        return true;
    }

    public boolean updateTargetEntity(String id, SrcEntity content, SrcEntity oldTargetEntity,
            SrcEntity newTargetEntity, UserActionType linkType, Scope scope)
            throws VedantuException {

        UpdateOperations<LibraryContentLink> updateTargetEntity = getDS().createUpdateOperations(
                LibraryContentLink.class);
        Query<LibraryContentLink> getContentQuery = getDS().createQuery(LibraryContentLink.class);

        if (scope != null) {
            updateTargetEntity.add("scope", scope);

        }
        updateTargetEntity.add("target", newTargetEntity);
        if (id != null) {
            getContentQuery.filter(FIELD_ID, new ObjectId(id));
        }
        getContentQuery = addTargetFilter(getContentQuery, oldTargetEntity);
        getContentQuery = addSourceFilter(getContentQuery, content);
        getContentQuery = addLinkTypeFilter(getContentQuery, linkType);

        LOGGER.debug("Query: " + getContentQuery.toString());
        LOGGER.debug("Update Queyr: " + updateTargetEntity.toString());
        UpdateResults<LibraryContentLink> updateResults = update(getContentQuery,
                updateTargetEntity);
        if (updateResults.getHadError()) {
            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR);
        }

        return true;
    }

    private Query<LibraryContentLink> addTargetFilter(Query<LibraryContentLink> query,
            SrcEntity target) {

        if (target != null) {
            if (target.type != null) {
                query = query.filter("target.type", target.type);
                if (target.id != null) {
                    query = query.filter("target.id", target.id);

                }
            }

        }
        return query;
    }

    private Query<LibraryContentLink> addSourceFilter(Query<LibraryContentLink> query,
            SrcEntity source) {

        if (source != null) {
            if (source.type != null) {
                query = query.filter("source.type", source.type);
                if (source.id != null) {
                    query = query.filter("source.id", source.id);

                }
            }

        }
        return query;
    }

    private Query<LibraryContentLink> addLinkTypeFilter(Query<LibraryContentLink> query,
            UserActionType linkType) {

        if (linkType != null) {
            return query.filter("linkType", linkType);

        }
        return query;
    }

	public LibraryContentLink getBySourceAndSection(GetEntityReq req) {
		Query<LibraryContentLink> getContentQuery = getQuery();
		getContentQuery.filter("source.type", req.entityType);
		getContentQuery.filter("source.id", req.entityId);
		getContentQuery.filter("target.type", EntityType.SECTION);
		getContentQuery.filter("target.id", req.sectionId);
		getContentQuery.filter("recordState", VedantuRecordState.ACTIVE);
		return getContentQuery.get();
	}
	public List<LibraryContentLink> getListBySourceIdAndType(GetEntityReq req ) {
		Query<LibraryContentLink> getContentQuery = getQuery();
		getContentQuery.filter("source.type", req.entityType);
		getContentQuery.filter("source.id", req.entityId);
		getContentQuery.filter("target.type", EntityType.MODULE);
		getContentQuery.filter("recordState", VedantuRecordState.ACTIVE);
		return getContentQuery.asList();
	}

    public Map<String, Map<String, Long>> getEntityStartEndTime(String secId, Set<String> entityIds) {
        // TODO Auto-generated method stub
        Map<String, Map<String, Long>> entityStartTimeMap = new HashMap<String, Map<String, Long>>();
        if (StringUtils.isEmpty(secId) || CollectionUtils.isEmpty(entityIds)) {
            LOGGER.error("empty secId or entityIds : ");
            return entityStartTimeMap;
        }
        DBObject query = new BasicDBObject("source.id", new BasicDBObject(MongoManager.IN_QUERY,
                entityIds.toArray()));
        query.put(ConstantsGlobal.TARGET_DOT_ID, secId);
        query.put(ConstantsGlobal.RECORD_STATE, VedantuRecordState.ACTIVE.name());
        LOGGER.debug("getEntityStartTime query : " + query);
        VedantuDBResult<LibraryContentLink> entityStartTimes = LibraryContentLinksDAO.INSTANCE.getInfos(
                query, null, MongoManager.NO_START, MongoManager.NO_LIMIT, null);
        for (LibraryContentLink entityStartTime : entityStartTimes.results) {
            Map<String, Long> time = new HashMap<String, Long>();
            if(entityStartTime.getSchedule() != null){
                time.put("startTime", entityStartTime.getSchedule().startTime == null ? Long.MIN_VALUE: entityStartTime.getSchedule().startTime.getTime());
                time.put("endTime", entityStartTime.getSchedule().endTime == null ? Long.MIN_VALUE: entityStartTime.getSchedule().endTime.getTime());
                time.put("closeTime", entityStartTime.getSchedule().closeTime == null ? Long.MIN_VALUE: entityStartTime.getSchedule().closeTime.getTime());
            }else{
                time.put("startTime", Long.MIN_VALUE);
                time.put("endTime", Long.MIN_VALUE);
                time.put("closeTime", Long.MIN_VALUE);
            }
            entityStartTimeMap.put(entityStartTime.source.id, time);
        }
        LOGGER.debug("returning attempts entity map : " + entityStartTimeMap);
        return entityStartTimeMap;
    }

    public static Long getRemainingTime(Date date){
        return date.getTime() - new Date().getTime();
    }

    public static Long getRemainingTime(long timeCreated) {
        // TODO Auto-generated method stub
        return timeCreated - System.currentTimeMillis();
    }

}
