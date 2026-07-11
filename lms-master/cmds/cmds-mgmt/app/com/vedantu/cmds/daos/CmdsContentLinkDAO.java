package com.vedantu.cmds.daos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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
import com.vedantu.cmds.enums.CmdsContentLinkType;
import com.vedantu.cmds.models.CMDSContentLink;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.daos.CounterDAO;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.mongo.MongoManager.SortOrder;
import com.vedantu.mongo.SortOrderInfo;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuRecordState;

public class CmdsContentLinkDAO extends VedantuBasicDAO<CMDSContentLink, ObjectId> {

    private static final ALogger           LOGGER   = Logger.of(CmdsContentLinkDAO.class);
    public static final CmdsContentLinkDAO INSTANCE = new CmdsContentLinkDAO(CMDSContentLink.class);

    public CmdsContentLinkDAO(Class<CMDSContentLink> entityClass) {

        super(entityClass);
        // TODO Auto-generated constructor stub
    }

    public static String getCounterName(CMDSContentLink link, String suffix) {

        LOGGER.debug("Getting counter name for" + link.target + suffix + link.linkType);
        return getCounterName(link.target, link.linkType, suffix);
    }

    protected static String getCounterName(SrcEntity target, CmdsContentLinkType linkType,
            String suffix) {

        LOGGER.debug("Getting counter name for" + target + suffix + linkType);
        return target.type.name().toLowerCase() + "_" + target.id + "_"
                + linkType.name().toLowerCase() + "_" + suffix.trim().toLowerCase();
    }

    public CMDSContentLink addLink(SrcEntity content, SrcEntity targetEntity,
            CmdsContentLinkType linkType, String actorId) throws VedantuException {

        return addLink(content, targetEntity, linkType, actorId, false);
    }

    public CMDSContentLink addLink(SrcEntity content, SrcEntity targetEntity,
            CmdsContentLinkType linkType, String actorId, boolean allowDuplicates)
            throws VedantuException {

        MutableLong totalHits = new MutableLong(0L);
        List<CMDSContentLink> links = getCmdsContentLinks(content, targetEntity, linkType, null, 0,
                1, VedantuRecordState.ACTIVE, totalHits);

        if (totalHits.longValue() > 1 && !allowDuplicates) {
            LOGGER.error("Added multiple times ");
            throw new VedantuException(VedantuErrorCode.ALREADY_ADDED);
        }

        CMDSContentLink contentLinkage = null;
        if (CollectionUtils.isNotEmpty(links) && !allowDuplicates) {
            contentLinkage = links.get(0);
        } else {
            contentLinkage = new CMDSContentLink(targetEntity, content);

            contentLinkage.userId = actorId;
            contentLinkage.linkType = linkType;
            // this is ever increasing number for all positions
            contentLinkage.position = CounterDAO.INSTANCE.getNextSequence(
                    CmdsContentLinkDAO.INSTANCE.getCollection().getName(),
                    CmdsContentLinkDAO.getCounterName(contentLinkage, CMDSContentLink.POSITION), 1);

        }
        save(contentLinkage);
        return contentLinkage;
    }

    public List<CMDSContentLink> getLinkBy(SrcEntity source, int start, int size,
            VedantuRecordState recordState, MutableLong totalHits) {

        return getCmdsContentLinks(source, null, null, null, start, size, recordState, totalHits);
    }

    public List<CMDSContentLink>
            getCmdsContentLinks(SrcEntity content, SrcEntity targetEntity,
                    CmdsContentLinkType linkType, String actorId, int start, int size,
                    MutableLong totalHits) {

        return getCmdsContentLinks(content, targetEntity, linkType, actorId, start, size, null,
                totalHits);
    }

    // add retrieved field here
    public List<CMDSContentLink> getCmdsContentLinks(SrcEntity content, SrcEntity targetEntity,
            CmdsContentLinkType linkType, String actorId, int start, int size,
            VedantuRecordState recordState, MutableLong totalHits) {

        return getCmdsContentLinks(content, targetEntity, linkType, actorId, null, start, size,
                recordState, totalHits);
    }

    public List<CMDSContentLink> getCmdsContentLinks(SrcEntity content, SrcEntity targetEntity,
            CmdsContentLinkType linkType, String actorId, Scope scope, int start, int size,
            VedantuRecordState recordState, MutableLong totalHits) {

        return getCmdsContentLinks(
                content,
                targetEntity,
                linkType,
                actorId,
                scope,
                start,
                size,
                recordState,
                totalHits,
                new HashSet<SortOrderInfo>(Arrays.asList(new SortOrderInfo(SortOrder.DESC,
                        CMDSContentLink.TIME_CREATED))));

    }

    public List<CMDSContentLink> getCmdsContentLinks(SrcEntity content, SrcEntity targetEntity,
            CmdsContentLinkType linkType, String actorId, Scope scope, int start, int size,
            VedantuRecordState recordState, MutableLong totalHits, Set<SortOrderInfo> orders) {

        Query<CMDSContentLink> getContentQuery = getDS().createQuery(CMDSContentLink.class);
        LOGGER.debug("Querying for " + CMDSContentLink.class);
        getContentQuery = addTargetFilter(getContentQuery, targetEntity);
        getContentQuery = addSourceFilter(getContentQuery, content);
        getContentQuery = addLinkTypeFilter(getContentQuery, linkType);
        getContentQuery = addScope(getContentQuery, scope);
        if (recordState != null) {
            getContentQuery.field("recordState").equal(recordState);
        }

        if (CollectionUtils.isNotEmpty(orders)) {
            LOGGER.debug("Sort by " + StringUtils.join(orders, ","));

            getContentQuery = getContentQuery.order(StringUtils.join(orders, ",")).limit(size)
                    .offset(start);
        }

        LOGGER.debug("Query: " + getContentQuery.toString());

        List<CMDSContentLink> cmdsContentLinks = getContentQuery.asList();

        if (totalHits != null) {
            long totalHts = getContentQuery.countAll();
            totalHits.setValue(totalHts);
            LOGGER.debug("Total matched results " + totalHts);
        }

        return cmdsContentLinks;
    }

    private List<String> toStringList(List<? extends Enum<?>> enums) {

        List<String> values = new ArrayList<String>();
        for (Enum<?> e : enums) {
            values.add(e.name());
        }
        return values;
    }

    public List<CMDSContentLink> getCmdsContentLinksForTargets(SrcEntity content,
            List<SrcEntity> targetEntities, CmdsContentLinkType linkType, String actorId,
            int start, int size, VedantuRecordState recordState, MutableLong totalHits) {

        LOGGER.debug("Geting links by targets");
        Query<CMDSContentLink> findQuery = getQuery();

        Set<String> targetTypes = new HashSet<String>();
        Set<String> targetIds = new HashSet<String>();
        for (SrcEntity target : targetEntities) {
            targetTypes.add(target.type.name());
            targetIds.add(target.id);

        }
        findQuery = addSourceFilter(findQuery, content);
        LOGGER.debug("target ids" + targetIds);
        findQuery.field("target.type").in(targetTypes);
        findQuery.field("target.id").in(targetIds);
        findQuery.field("linkType").equal(linkType);
        if (StringUtils.isNotEmpty(actorId)) {
            findQuery.field("actorId").equal(actorId);
        }
        if (recordState != null) {
            findQuery.field("recordState").equal(recordState);
        }
        totalHits.setValue(findQuery.countAll());
        LOGGER.debug("Query: " + findQuery.toString());
        return findQuery.order("-timeCreated").offset(start).limit(size).asList();
    }

    public List<CMDSContentLink> getCmdsContentLinks(List<EntityType> includeTypes,
            List<EntityType> excludeTypes, SrcEntity targetEntity, CmdsContentLinkType linkType,
            String actorId, int start, int size, MutableLong totalHits) {

        Query<CMDSContentLink> getContentQuery = getDS().createQuery(CMDSContentLink.class);
        LOGGER.debug("Querying for " + CMDSContentLink.class);
        getContentQuery = addTargetFilter(getContentQuery, targetEntity);
        if (excludeTypes != null) {
            getContentQuery.field("source.type").notIn(excludeTypes);
        }
        if (includeTypes != null) {
            getContentQuery.field("source.type").in(includeTypes);
        }
        getContentQuery = addLinkTypeFilter(getContentQuery, linkType);
        getContentQuery = getContentQuery.offset(start).limit(size);
        LOGGER.debug("Query: " + getContentQuery.toString());
        getContentQuery = getContentQuery.order("timeUpdated");
        List<CMDSContentLink> cmdsContentLinks = getContentQuery.asList();
        long hits = getContentQuery.countAll();
        totalHits.setValue(hits);

        LOGGER.debug("Total matched results " + hits);

        return cmdsContentLinks;
    }

    public CMDSContentLink remove(SrcEntity content, SrcEntity targetEntity,
            CmdsContentLinkType linkType) throws VedantuException {

        Query<CMDSContentLink> getContentQuery = getDS().createQuery(CMDSContentLink.class);
        getContentQuery = addTargetFilter(getContentQuery, targetEntity);
        getContentQuery = addSourceFilter(getContentQuery, content);
        getContentQuery = addLinkTypeFilter(getContentQuery, linkType);

        LOGGER.debug("Query: " + getContentQuery.toString());

        UpdateOperations<CMDSContentLink> deleteAllOperation = getDS().createUpdateOperations(
                CMDSContentLink.class);
        deleteAllOperation.set("recordState", VedantuRecordState.DELETED);
        //
        // UpdateResults<CMDSContentLink> updateResult = getDS().update(getContentQuery,
        // deleteAllOperation;
        CMDSContentLink link = getDS().findAndModify(getContentQuery, deleteAllOperation, true);

        // if (link == null) {
        // return link;
        // }

        // if (StringUtils.isNotEmpty(updateResult.getError())) {
        // LOGGER.error("Error occured :" + updateResult.getError());
        // throw new VedantuException(VedantuErrorCode.CAN_NOT_BE_REMOVED);
        // }
        if (link != null) {
            link.recordState = VedantuRecordState.DELETED;
        }
        return link;
    }

    public boolean updateTargetEntity(String id, SrcEntity content, SrcEntity oldTargetEntity,
            SrcEntity newTargetEntity, CmdsContentLinkType linkType, Scope scope)
            throws VedantuException {

        UpdateOperations<CMDSContentLink> updateTargetOperation = getDS().createUpdateOperations(
                CMDSContentLink.class);
        Query<CMDSContentLink> getContentQuery = getDS().createQuery(CMDSContentLink.class);

        if (scope != null) {
            updateTargetOperation = updateTargetOperation.set("scope", scope);

        }
        updateTargetOperation = updateTargetOperation.set("target.type", newTargetEntity.type);
        updateTargetOperation = updateTargetOperation.set("target.id", newTargetEntity.id);
        updateTargetOperation = updateTargetOperation.set(CMDSContentLink.POSITION,
                CounterDAO.INSTANCE.getNextSequence(CmdsContentLinkDAO.INSTANCE.getCollection()
                        .getName(), CmdsContentLinkDAO.getCounterName(newTargetEntity, linkType,
                        CMDSContentLink.POSITION), 1));
        if (id != null) {
            getContentQuery.filter(FIELD_ID, new ObjectId(id));
        }
        getContentQuery = addTargetFilter(getContentQuery, oldTargetEntity);
        getContentQuery = addSourceFilter(getContentQuery, content);
        getContentQuery = addLinkTypeFilter(getContentQuery, linkType);

        LOGGER.debug("Query: " + getContentQuery.toString());
        LOGGER.debug("Update Query: " + updateTargetOperation.toString());
        UpdateResults<CMDSContentLink> updateResults = update(getContentQuery,
                updateTargetOperation);
        if (updateResults.getHadError()) {

            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR);
        }

        return true;
    }

    private Query<CMDSContentLink> addTargetFilter(Query<CMDSContentLink> query, SrcEntity target) {

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

    private Query<CMDSContentLink>
            addTargetTypeFilter(Query<CMDSContentLink> query, EntityType type) {

        return addTargetFilter(query, new SrcEntity(type, null));
    }

    private Query<CMDSContentLink>
            addSourceTypeFilter(Query<CMDSContentLink> query, EntityType type) {

        return addSourceFilter(query, new SrcEntity(type, null));
    }

    private Query<CMDSContentLink> addSourceFilter(Query<CMDSContentLink> query, SrcEntity source) {

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

    private Query<CMDSContentLink> addActedByFiler(Query<CMDSContentLink> query, String userId) {

        if (StringUtils.isEmpty(userId)) {
            return query;
        }
        query = query.filter("actedBy.type", EntityType.USER);
        query = query.filter("actedBy.id", userId);
        return query;
    }

    private Query<CMDSContentLink> addSortBy(Query<CMDSContentLink> query, String fieldName) {

        return query.order(fieldName);

    }

    private Query<CMDSContentLink> addScope(Query<CMDSContentLink> query, Scope scope) {

        if (scope == null || scope == Scope.UNKNOWN) {
            return query;
        }
        return query.filter("scope", scope);

    }

    private Query<CMDSContentLink> addLinkTypeFilter(Query<CMDSContentLink> query,
            CmdsContentLinkType linkType) {

        if (linkType == null || linkType == CmdsContentLinkType.UNKNOWN) {
            return query;
        }
        return query.filter("linkType", linkType);
    }

}
