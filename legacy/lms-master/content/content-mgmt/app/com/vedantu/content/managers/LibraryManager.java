package com.vedantu.content.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import play.Logger;
import play.Logger.ALogger;

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.content.interfaces.IPublishable;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.enums.EncryptionLevel;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.commons.pojos.ScheduleInfo;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.ListResponse;
import com.vedantu.commons.relationships.ContentLinkRelationshipDetails;
import com.vedantu.commons.utils.ObjectMapperUtils;
import com.vedantu.content.commons.interfaces.ILibraryContent;
import com.vedantu.content.daos.LibraryContentLinksDAO;
import com.vedantu.content.models.LibraryContentLink;
import com.vedantu.content.pojos.tests.EntityScheduleInfo;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.MongoManager.SortOrder;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.organization.managers.OrgProgramManager;
import com.vedantu.search.utils.ElasticSearchUtils;

public class LibraryManager extends AbstractContentManager {

    private static final ALogger LOGGER = Logger.of(LibraryManager.class);

    public static LibraryContentLink addToLibrary(SrcEntity content, SrcEntity targetEntity,
            UserActionType linkType, String actorId, Scope scope, ScheduleInfo scheduleInfo)
            throws VedantuException {

        return addToLibrary(content, targetEntity, linkType, actorId, scope, scheduleInfo, null);
    }

    public static LibraryContentLink addToLibrary(SrcEntity content, SrcEntity targetEntity,
            UserActionType linkType, String actorId, Scope scope, ScheduleInfo scheduleInfo,
            String parentEsId) throws VedantuException {

        return addToLibrary(content, targetEntity, linkType, actorId, scope, scheduleInfo, null,
                null);
    }

    public static LibraryContentLink addToLibrary(SrcEntity content, SrcEntity targetEntity,
            UserActionType linkType, String actorId, Scope scope, ScheduleInfo scheduleInfo,
            String parentEsId, Boolean downloadble) throws VedantuException {

        return addToLibrary(content, targetEntity, linkType, actorId, scope, scheduleInfo,
                parentEsId, downloadble, null);
    }

    public static LibraryContentLink addToLibrary(SrcEntity content, SrcEntity targetEntity,
            UserActionType linkType, String actorId, Scope scope, ScheduleInfo scheduleInfo,
            String parentEsId, Boolean downloadble, EncryptionLevel level) throws VedantuException {

        return addToLibrary(content, targetEntity, linkType, actorId, scope, scheduleInfo,
                parentEsId, downloadble, level, null, -1);

    }

    public static LibraryContentLink addToLibrary(SrcEntity content, SrcEntity targetEntity,
            UserActionType linkType, String actorId, Scope scope, ScheduleInfo scheduleInfo,
            String parentEsId, Boolean downloadble, EncryptionLevel level,
            List<SrcEntity> cmdsDownloadableEntities, long position) throws VedantuException {

        LOGGER.debug("Adding " + content + " to library at targetEntity" + targetEntity);

        if (scope == Scope.PRIVATE) {
            // overriding in case scope made private
            downloadble = new Boolean(false);
        }

        List<SrcEntity> downloadableEntities = new ArrayList<SrcEntity>();

        if (cmdsDownloadableEntities != null) {
            for (SrcEntity cmdsEntity : cmdsDownloadableEntities) {

                VedantuBasicDAO dao = EntityTypeDAOFactory.INSTANCE.get(cmdsEntity.type);

                SrcEntity globalEntity = ((IPublishable) dao).getGlobalEntity(cmdsEntity.id);

                downloadableEntities.add(globalEntity);

            }
        }

        LibraryContentLink link = LibraryContentLinksDAO.INSTANCE.addLink(content, targetEntity,
                linkType, actorId, scope, scheduleInfo, downloadble, level, false,
                downloadableEntities, position);

        LOGGER.debug("............function begin" + link.getDownloadableEntities() + "......"
                + link.position);

        ContentLinkRelationshipDetails libraryContentLinkDetails = new ContentLinkRelationshipDetails(
                link.userId, link.source, link.target, link.getScope());
        libraryContentLinkDetails.schedule = link.getSchedule();
        libraryContentLinkDetails.downloadble = link.isDownloadable();
        libraryContentLinkDetails.encLevel = link.getEncLevel();
        libraryContentLinkDetails.position = position != -1 ? position : 0;

        if (link.getScope() == Scope.PRIVATE) {
            LibraryContentLinksDAO.INSTANCE.updateState(link, VedantuRecordState.DELETED);

            updateUserActionMappintToEs(libraryContentLinkDetails, content, UserActionType.ADDED,
                    EventActionType.REMOVE, parentEsId);
        } else {

            updateUserActionMappintToEs(libraryContentLinkDetails, content, UserActionType.ADDED,
                    EventActionType.ADD, parentEsId);
        }

        // now add the same to contents index
        // TODO: remove this code if we want to go with uniform library structure
        if (ILibraryContent.libraryEntityType.contains(content.type)) {
            QueryBuilder esQuery = QueryBuilders
                    .boolQuery()
                    .must(QueryBuilders.termQuery(ConstantsGlobal.ID, content.id))
                    .must(QueryBuilders.termQuery(ConstantsGlobal.TYPE, content.type.name()
                            .toLowerCase()));
            SearchHit hit = ElasticSearchUtils.findOne(ILibraryContent.INDEX_NAME,
                    ILibraryContent.INDEX_TYPE, esQuery);
            if (hit != null) {
                updateUserActionMappintToEs(libraryContentLinkDetails, content,
                        ILibraryContent.INDEX_NAME, ILibraryContent.INDEX_TYPE,
                        UserActionType.ADDED.getSearchIndexType(), EventActionType.UPDATE,
                        hit.getId());
            }
        }
        LOGGER.debug("............function end" + link.getDownloadableEntities() + "......");
        return link;

    }

    public static AggregationOutput getEntityScheduleAggregationOutput(
            Collection<String> sourceIds, Collection<String> targetIds, int start, int size,
            boolean latestScheduleOnly) {

        if (sourceIds == null && targetIds == null) {
            return null;
        }
        DBObject match = CollectionUtils.isEmpty(targetIds) ? new BasicDBObject()
                : new BasicDBObject("target.id", new BasicDBObject(MongoManager.IN_QUERY,
                        targetIds.toArray()));
        match.put(ConstantsGlobal.RECORD_STATE, VedantuRecordState.ACTIVE.name());
        if (CollectionUtils.isNotEmpty(sourceIds)) {
            match.put("source.id", new BasicDBObject(MongoManager.IN_QUERY, sourceIds.toArray()));
        }

        DBObject sort = new BasicDBObject("schedule.startTime", SortOrder.ASC.getValue());

        DBObject id = new BasicDBObject("entityId", "$source.id");
        DBObject sId = new BasicDBObject("month",
                new BasicDBObject("$month", "$schedule.startTime"));
        sId.put("day", new BasicDBObject("$dayOfMonth", "$schedule.startTime"));
        sId.put("year", new BasicDBObject("$year", "$schedule.startTime"));
        id.put("startTime", sId);
        DBObject group1 = new BasicDBObject("_id", id);
        group1.put("schedule", new BasicDBObject("$last", "$schedule"));
        group1.put("targets", new BasicDBObject("$push", "$target"));

        DBObject group2 = new BasicDBObject("_id", "$_id.entityId");
        group2.put("schedule", new BasicDBObject("$first", "$schedule"));

        if (latestScheduleOnly) {
            group2.put("targets", new BasicDBObject("$first", "$targets"));
        } else {
            DBObject scheduleAgg = new BasicDBObject("schedule", "$schedule");
            scheduleAgg.put("targets", "$targets");
            group2.put("schedules", new BasicDBObject("$push", scheduleAgg));
        }

        DBObject matchQuery = new BasicDBObject("$match", match);

        List<DBObject> additionalOps = new ArrayList<DBObject>();
        DBObject sortQuery = new BasicDBObject("$sort", sort);
        additionalOps.add(sortQuery);
        DBObject group1Query = new BasicDBObject("$group", group1);
        additionalOps.add(group1Query);
        DBObject group2Query = new BasicDBObject("$group", group2);
        additionalOps.add(group2Query);

        DBObject finalSort = new BasicDBObject("schedule.startTime", SortOrder.DESC.getValue());

        additionalOps.add(new BasicDBObject("$sort", finalSort));

        if (start != MongoManager.NO_START) {
            additionalOps.add(new BasicDBObject("$skip", start));
        }
        if (size != MongoManager.NO_LIMIT) {
            additionalOps.add(new BasicDBObject("$limit", size));
        }

        AggregationOutput aggregationOutput = LibraryContentLinksDAO.INSTANCE.aggregate(matchQuery,
                additionalOps.toArray(new BasicDBObject[] {}));
        return aggregationOutput;
    }

    @SuppressWarnings("unchecked")
    private static List<EntityScheduleInfo> populateScheduleInfos(List<DBObject> scheduleAggObjs) {

        List<EntityScheduleInfo> schedules = new ArrayList<EntityScheduleInfo>();

        for (DBObject scheduleAggObj : scheduleAggObjs) {
            ScheduleInfo scheduleInfo = ObjectMapperUtils.convertValue(
                    scheduleAggObj.get(ConstantsGlobal.SCHEDULE), ScheduleInfo.class);
            List<DBObject> targets = (List<DBObject>) scheduleAggObj.get(ConstantsGlobal.TARGETS);
            Set<String> sectnIds = new HashSet<String>();
            for (DBObject entity : targets) {
                SrcEntity srcEntity = new SrcEntity(EntityType.valueOfKey((String) entity
                        .get(ConstantsGlobal.TYPE)), (String) entity.get(ConstantsGlobal.ID));
                if (EntityType.SECTION.equals(srcEntity.type)) {
                    sectnIds.add(srcEntity.id);
                } else if (EntityType.MODULE.equals(srcEntity.type)) {
                    List<LibraryContentLink> links = LibraryContentLinksDAO.INSTANCE.createQuery()
                            .filter(ConstantsGlobal.LINK_TYPE, UserActionType.ADDED)
                            .filter(ConstantsGlobal.RECORD_STATE, VedantuRecordState.ACTIVE)
                            .filter("source.id", srcEntity.id)
                            .filter("target.type", EntityType.SECTION).asList();
                    for (LibraryContentLink link : links) {
                        sectnIds.add(link.target.id);
                    }
                }
            }

            if (sectnIds.isEmpty()) {
                continue;
            }

            EntityScheduleInfo entityScheduleInfo = new EntityScheduleInfo();
            entityScheduleInfo.schedule = scheduleInfo;
            entityScheduleInfo.programs = OrgProgramManager.getProgramBySectionIds(sectnIds, true);
            schedules.add(entityScheduleInfo);
        }
        return schedules;
    }

    @SuppressWarnings("unchecked")
    public static ListResponse<EntityScheduleInfo> getEntityScheduleInfoRes(String orgId,
            String entityId) throws VedantuException {

        ListResponse<EntityScheduleInfo> res = new ListResponse<EntityScheduleInfo>();
        AggregationOutput aggregationOutput = getEntityScheduleAggregationOutput(
                Arrays.asList(entityId), null, MongoManager.NO_START, MongoManager.NO_LIMIT, false);
        LOGGER.debug("aggregationOutput : " + aggregationOutput);
        List<EntityScheduleInfo> schedules = new ArrayList<EntityScheduleInfo>();
        for (DBObject d : aggregationOutput.results()) {
            String eId = (String) d.get(ConstantsGlobal._ID);
            LOGGER.debug("entityId: " + eId);
            List<DBObject> scheduleAggObjs = (List<DBObject>) d.get(ConstantsGlobal.SCHEDULES);
            schedules = populateScheduleInfos(scheduleAggObjs);
            break;
        }
        res.totalHits = schedules.size();
        res.list = schedules;
        return res;
    }

}