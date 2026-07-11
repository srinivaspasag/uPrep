package com.vedantu.cmds.maintenance.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import play.Logger;
import play.Logger.ALogger;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.vedantu.cmds.daos.CmdsContentLinkDAO;
import com.vedantu.cmds.enums.CmdsContentLinkType;
import com.vedantu.cmds.managers.AbstractCMDSContentManager;
import com.vedantu.cmds.managers.CMDSContentLinkManager;
import com.vedantu.cmds.mgmt.interfaces.ICMDSResource;
import com.vedantu.cmds.models.CMDSContentLink;
import com.vedantu.cmds.models.event.search.details.CMDSResourceDetails;
import com.vedantu.cmds.models.event.search.details.ReIndexDetails;
import com.vedantu.cmds.pojos.requests.ReIndexLibraryContentReq;
import com.vedantu.cmds.pojos.requests.ReIndexResourceReq;
import com.vedantu.cmds.pojos.responses.ReIndexRes;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.commons.events.apis.EntityIndexEventMapper;
import com.vedantu.commons.events.apis.IEventDetails;
import com.vedantu.commons.events.apis.IMongoAware;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.relationships.ContentLinkRelationshipDetails;
import com.vedantu.commons.relationships.EntityUserActionRelationshipSearchDetails;
import com.vedantu.commons.utils.ObjectIdUtils;
import com.vedantu.content.commons.interfaces.IIndexable;
import com.vedantu.content.daos.LibraryContentLinksDAO;
import com.vedantu.content.models.AbstractBoardEntityTagModel;
import com.vedantu.content.models.LibraryContentLink;
import com.vedantu.events.utils.EventDetailsFactory;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuDBResult;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.search.utils.ElasticSearchUtils;
import com.vedantu.user.models.EntityUserActionMapping;
import com.vedantu.user.pojos.EntityUserActionDAO;

public class IndexingManager extends AbstractCMDSContentManager {

    public static final IndexingManager INSTANCE            = new IndexingManager();
    private static final ALogger        LOGGER              = Logger.of(IndexingManager.class);
    public static final int             INDEXING_BATCH_SIZE = 20;

    private IndexingManager() {

    }

    public boolean reIndex(EntityType type, List<String> ids, String userId,
            EntityType containerType, CmdsContentLinkType linkType) throws VedantuException {

        try {
            SrcEntity container = null;
            if (containerType != null && containerType != EntityType.UNKNOWN) {
                container = new SrcEntity(containerType, null);
            }
            @SuppressWarnings("unchecked")
            VedantuBasicDAO<VedantuBaseMongoModel, ObjectId> contentDAO = EntityTypeDAOFactory.INSTANCE
                    .get(type);
            LOGGER.debug("Looking for ids:" + ids);

            List<VedantuBaseMongoModel> mongoModels = contentDAO.getByIds(ObjectIdUtils
                    .toObjectIds(ids));

            // fetch all results from contentDAO and reindex them all.
            EventType indexEvent = EntityIndexEventMapper.INSTANCE.get(type);
            LOGGER.debug("Found index event type :" + indexEvent);

            if (CollectionUtils.isNotEmpty(mongoModels)) {
                LOGGER.debug("Mongo models :" + mongoModels.size());
                for (VedantuBaseMongoModel mongoModel : mongoModels) {
                    if (indexEvent != null) {
                        IEventDetails indexDetails = EventDetailsFactory.getInstance().getDetails(
                                indexEvent);
                        if (indexDetails instanceof IMongoAware && mongoModel instanceof IIndexable) {
                            ((IMongoAware) indexDetails).fromMongoModel(mongoModel);
                            if (mongoModel instanceof AbstractBoardEntityTagModel) {
                                String ownerUserId = ((AbstractBoardEntityTagModel) mongoModel)
                                        ._getUserId();
                                if (ownerUserId != null) {
                                    userId = ownerUserId;
                                }
                            }

                            switch (mongoModel.recordState) {
                            case ACTIVE:
                                generateEventAysc(userId, mongoModel, EventActionType.UPDATE,
                                        indexEvent, UserActionType.UPDATED, false);
                                break;
                            case DELETED:
                                generateEventAysc(userId, mongoModel, EventActionType.REMOVE,
                                        indexEvent, UserActionType.DELETED, false);
                                break;
                            case TEMPORARY:
                                // / ON ILE SIDE THERE SHOULD BE NO CONTENT IN TEMPORARY STATE
                                // except CAUSED by BOARD DELETION
                                if (EntityType.isSupportedContentType(type)) {
                                    generateEventAysc(userId, mongoModel, EventActionType.UPDATE,
                                            indexEvent, UserActionType.UPDATED, false);
                                }
                                break;
                            default:
                                break;

                            }

                        }
                    }
                    LOGGER.debug("now trying to update cmds resource "
                            + (contentDAO instanceof ICMDSResource));
                    if (contentDAO instanceof ICMDSResource) {
                        updateCMDSResource((ICMDSResource) contentDAO, type, mongoModel, container,
                                linkType);
                    }
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Reindiexing failing", ex);
            throw new VedantuException(VedantuErrorCode.REINDEXING_FAILED, ex);
        }
        return true;

    }

    private static void updateCMDSResource(ICMDSResource contentDAO, EntityType type,
            VedantuBaseMongoModel mongoModel, SrcEntity container, CmdsContentLinkType linkType) {

        SrcEntity content = new SrcEntity(type, mongoModel._getStringId());

        CMDSResourceDetails resourceDetails = contentDAO.getCMDSResourceDetails(mongoModel);
        // if the cMDSResourceDetails details is not indexable (queryContent is
        // empty or null) than do not index
        LOGGER.debug("resourceDetails  : " + resourceDetails);
        LOGGER.debug("resourceDetails isIndexable : " + resourceDetails._isIndexable());
        if (!resourceDetails._isIndexable()) {
            return;
        }
        resourceDetails.id = getResourceId(resourceDetails.content);

        switch (mongoModel.recordState) {
        case ACTIVE:
            addLiveEntityToSearchIndex(resourceDetails, EntityType.CMDSRESOURCE, true);
            break;
        case DELETED:
            removeCMDSResource(content);
            break;

        default:
            break;

        }

        List<CMDSContentLink> links = CmdsContentLinkDAO.INSTANCE.getCmdsContentLinks(content,
                container, linkType, null, MongoManager.NO_START, MongoManager.NO_LIMIT,
                mongoModel.recordState, null);
        if (CollectionUtils.isNotEmpty(links)) {
            for (CMDSContentLink link : links) {
                CMDSContentLinkManager.INSTANCE.reIndex(link);
            }
        }

    }

    public ReIndexRes index(ReIndexResourceReq request) throws VedantuException {

        ReIndexRes response = new ReIndexRes();
        int start = 0;
        ReIndexDetails details = new ReIndexDetails();
        details.userId = request.userId;
        if (request.type == null) {
            throw new VedantuException(VedantuErrorCode.INVALID_EVENT_TYPE);
        }
        details.type = request.type;
        details.containerType = request.containerType;
        details.ids = new ArrayList<String>();
        details.linkType = request.linkType;
        @SuppressWarnings("unchecked")
        VedantuBasicDAO<VedantuBaseMongoModel, ObjectId> contentDAO = EntityTypeDAOFactory.INSTANCE
                .get(request.type);
        if (contentDAO == null) {
            LOGGER.error("no dao found for entityType:" + request.type);
            throw new VedantuException(VedantuErrorCode.UNSUPPORTED_CONTENT_TYPE,
                    "no dao found for entityType:" + request.type);
        }
        if (CollectionUtils.isNotEmpty(request.includes)) {
            details.ids.addAll(request.includes);
            generateEventAysc(request.userId, details, EventType.REINDEX_CMDS_RESOURCE);
        } else {

            boolean fetchMore = true;
            while (fetchMore) {
                Set<String> dbIds = contentDAO.getIdsByTime(request.minTimeCreated,
                        request.maxTimeCreated, start, IndexingManager.INDEXING_BATCH_SIZE, null);
                if (CollectionUtils.isEmpty(dbIds)) {
                    LOGGER.debug(" no ids found so no more fetching");
                    fetchMore &= false;
                    continue;
                }
                details.ids.clear();
                details.ids.addAll(dbIds);
                response.totalAffectedDocs += dbIds.size();
                generateEventAysc(request.userId, details, EventType.REINDEX_CMDS_RESOURCE);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    LOGGER.error(" Sleeping disturbed, should never happen", ex);
                }
                fetchMore &= (dbIds.size() == IndexingManager.INDEXING_BATCH_SIZE);
                start += IndexingManager.INDEXING_BATCH_SIZE;

            }
        }
        response.accepted = true;
        return response;
    }

    private static final int REQ_SIZE = 50;

    public ReIndexRes reIndexLibraryContentLinks(ReIndexLibraryContentReq req)
            throws VedantuException {

        ReIndexRes res = new ReIndexRes();
        DBObject query = null;

        query = req.entityType == null ? new BasicDBObject() : new BasicDBObject("source.type",
                req.entityType.name());
        query.put("recordState", VedantuRecordState.ACTIVE.name());
        if (req.ids != null && req.ids.size() > 0) {
            query.put("source.id", new BasicDBObject(MongoManager.IN_QUERY, req.ids.toArray()));
        }
        if (req.linkType != null) {
            query.put(ConstantsGlobal.LINK_TYPE, req.linkType.name());
        }
        if (req.fromTime > 0) {
            query.put(ConstantsGlobal.TIME_CREATED, new BasicDBObject("$gte", req.fromTime));
        }
        if (req.toTime > 0) {
            query.put(ConstantsGlobal.TIME_CREATED, new BasicDBObject("$lte", req.toTime));
        }
        if (CollectionUtils.isNotEmpty(req.linkIds)) {
            query.put(
                    "_id",
                    new BasicDBObject(MongoManager.IN_QUERY, ObjectIdUtils.toObjectIds(req.linkIds)));
        }
        LOGGER.debug("Link updating query " + query.toString());

        int start = 0;
        boolean hasMore = true;
        int totalReindexDocs = 0;
        while (hasMore) {
            VedantuDBResult<LibraryContentLink> links = LibraryContentLinksDAO.INSTANCE.getInfos(
                    query, null, start, REQ_SIZE, MongoManager.getSortQuery(
                            ConstantsGlobal.TIME_CREATED, MongoManager.SortOrder.ASC.name()));

            if (CollectionUtils.isNotEmpty(links.results)) {
                for (LibraryContentLink link : links.results) {
                    ContentLinkRelationshipDetails libraryContentLinkDetails = new ContentLinkRelationshipDetails(
                            link.userId, link.source, link.target, link.getScope());
                    libraryContentLinkDetails.schedule = link.getSchedule();
                    libraryContentLinkDetails.timeCreated = link.timeCreated;
                    libraryContentLinkDetails.lastUpdated = link.lastUpdated;
                    libraryContentLinkDetails.position = link.getPosition();
                    LOGGER.debug("Link information" + link);
                    QueryBuilder esQuery = QueryBuilders.termQuery(ConstantsGlobal.ID,
                            link.source.id);
                    SearchHit hit = ElasticSearchUtils.findOne(link.source.type.getIndexName(),
                            link.source.type.getIndexType(), esQuery);
                    if (hit != null) {
                        totalReindexDocs++;
                        updateUserActionMappintToEs(libraryContentLinkDetails, link.source,
                                link.linkType, EventActionType.UPDATE, hit.getId());
                    }
                }
            }

            hasMore = links.results.size() > 0 && start < links.totalHits;
            start += REQ_SIZE;
        }
        res.accepted = true;
        res.totalAffectedDocs = totalReindexDocs;
        return res;
    }

    public ReIndexRes reIndexUserActionMappings(ReIndexLibraryContentReq req)
            throws VedantuException {

        ReIndexRes res = new ReIndexRes();
        DBObject query = req.entityType == null ? new BasicDBObject() : new BasicDBObject(
                "target.type", req.entityType.name());
        query.put("recordState", VedantuRecordState.ACTIVE.name());
        if (req.ids != null && req.ids.size() > 0) {
            query.put("target.id", new BasicDBObject(MongoManager.IN_QUERY, req.ids));
        }

        if (req.linkType != null) {
            query.put(ConstantsGlobal.ACTION_TYPE, req.linkType.name());
        }

        if (req.fromTime > 0) {
            query.put(ConstantsGlobal.TIME_CREATED, new BasicDBObject("$gte", req.fromTime));
        }
        if (req.toTime > 0) {
            query.put(ConstantsGlobal.TIME_CREATED, new BasicDBObject("$lte", req.toTime));
        }

        if (CollectionUtils.isNotEmpty(req.linkIds)) {
            query.put(
                    "_id",
                    new BasicDBObject(MongoManager.IN_QUERY, ObjectIdUtils.toObjectIds(req.linkIds)));
        }
        LOGGER.debug("Link updating query " + query.toString());

        int start = 0;
        boolean hasMore = true;
        int totalReindexDocs = 0;
        while (hasMore) {
            VedantuDBResult<EntityUserActionMapping> links = EntityUserActionDAO.INSTANCE.getInfos(
                    query, null, start, REQ_SIZE, MongoManager.getSortQuery(
                            ConstantsGlobal.TIME_CREATED, MongoManager.SortOrder.ASC.name()));
            for (EntityUserActionMapping link : links.results) {
                EntityUserActionRelationshipSearchDetails userActionDetails = new EntityUserActionRelationshipSearchDetails(
                        link.userId, link.target.id);
                userActionDetails.timeCreated = link.timeCreated;
                QueryBuilder esQuery = QueryBuilders.termQuery(ConstantsGlobal.ID, link.target.id);
                SearchHit hit = ElasticSearchUtils.findOne(link.target.type.getIndexName(),
                        link.target.type.getIndexType(), esQuery);
                if (hit != null) {
                    totalReindexDocs++;
                    updateUserActionMappintToEs(userActionDetails, link.target, link.actionType,
                            EventActionType.UPDATE, hit.getId());
                }
            }
            hasMore = links.results.size() > 0 && start < links.totalHits;
            start += REQ_SIZE;
        }
        res.accepted = true;
        res.totalAffectedDocs = totalReindexDocs;
        return res;
    }

}
