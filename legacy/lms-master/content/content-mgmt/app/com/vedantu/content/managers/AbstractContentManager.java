package com.vedantu.content.managers;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableLong;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FieldQueryBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.facet.AbstractFacetBuilder;

import play.Logger;
import play.Logger.ALogger;
import play.Play;

import com.mongodb.BasicDBObject;
import com.vedantu.board.daos.BoardDAO;
import com.vedantu.board.managers.BoardManager;
import com.vedantu.board.pojos.BoardBasicInfo;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.content.interfaces.IContentManager;
import com.vedantu.commons.daos.GranteeOrgProgramDAO;
import com.vedantu.commons.entity.factory.EntityStorageFactory;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.entity.storage.EntityFileStorageException;
import com.vedantu.commons.entity.storage.FileCategory;
import com.vedantu.commons.entity.storage.IEntityFileStorage;
import com.vedantu.commons.entity.storage.ImageSize;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.FollowType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.commons.events.apis.EntityIndexEventMapper;
import com.vedantu.commons.events.apis.IMongoAware;
import com.vedantu.commons.fs.handlers.FileSystemFactory;
import com.vedantu.commons.fs.handlers.LocalFileSystemHandler;
import com.vedantu.commons.pojos.DownloadableFileInfo;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.IListResponseObj;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.commons.pojos.responses.SearchListResponse;
import com.vedantu.commons.relationships.IRelationshipSearchDetails;
import com.vedantu.commons.utils.FileUtils;
import com.vedantu.commons.utils.IAttemptableEntity;
import com.vedantu.commons.utils.IReverseImageMapperProcessor;
import com.vedantu.commons.utils.ISocialEntity;
import com.vedantu.commons.utils.ImageDisplayURLUtil;
import com.vedantu.commons.utils.ObjectIdUtils;
import com.vedantu.commons.utils.ObjectMapperUtils;
import com.vedantu.commons.utils.VedantuStringUtils;
import com.vedantu.commons.utils.image.ImageHTMLUtils;
import com.vedantu.content.commons.interfaces.IIndexable;
import com.vedantu.content.daos.LibraryContentLinksDAO;
import com.vedantu.content.enums.search.SearchResultType;
import com.vedantu.content.event.details.NewsRemoveDetails;
import com.vedantu.content.interfaces.Updatable;
import com.vedantu.content.models.AbstractBoardEntityTagModel;
import com.vedantu.content.pojos.requests.AbstractContentSearchReq;
import com.vedantu.content.pojos.requests.EditContentReq;
import com.vedantu.content.pojos.requests.GetSimilarEntities;
import com.vedantu.content.pojos.responses.AbstractContentUserActionRes;
import com.vedantu.content.search.details.AbstractBoardSearchEntityTagDetails;
import com.vedantu.content.search.details.AbstractFileModelIndexSearchDetails;
import com.vedantu.content.search.details.AbstractSearchDetail;
import com.vedantu.events.utils.EventDetailsFactory;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.mongo.models.GranteeOrgProgram;
import com.vedantu.organization.daos.OrganizationDAO;
import com.vedantu.organization.managers.OrgMemberManager;
import com.vedantu.organization.managers.OrgProgramManager;
import com.vedantu.organization.models.Organization;
import com.vedantu.search.es.ElasticSearchManager;
import com.vedantu.search.utils.ElasticSearchUtils;
import com.vedantu.socials.apis.IAttemptable;
import com.vedantu.socials.apis.ICommentable;
import com.vedantu.socials.apis.IFollowable;
import com.vedantu.socials.apis.IUpVotable;
import com.vedantu.socials.apis.IViewable;
import com.vedantu.user.managers.AbstractVedantuEventManager;
import com.vedantu.user.pojos.EntityUserActionDAO;
import com.vedantu.user.pojos.UserInfo;

public abstract class AbstractContentManager extends AbstractVedantuEventManager implements
        IContentManager, Updatable {

    protected static final int  ELASTIC_SEARCH_REFRESH_TIME         = 500;
    protected static final int  ES_ENSURE_QUERY_STATE_MAX_TRY_COUNT = 5;
    public static final ALogger LOGGER                              = Logger.of(AbstractContentManager.class);

    protected static void validateBoardIds(Set<String> allBordIds) throws VedantuException {

        if (CollectionUtils.isNotEmpty(allBordIds)) {
            int count = (int) BoardDAO.INSTANCE.count(new BasicDBObject(ConstantsGlobal._ID,
                    new BasicDBObject(MongoManager.IN_QUERY, ObjectIdUtils.toObjectIds(
                            new ArrayList<String>(allBordIds), true).toArray())));
            if (count != allBordIds.size()) {
                LOGGER.error("some boards ids from the list [" + allBordIds + "] are not valid");
                throw new VedantuException(VedantuErrorCode.BOARD_NOT_FOUND);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    protected static void isSocialActionAllowed(EntityType entityType, String entityId)
            throws VedantuException {

        VedantuBasicDAO dao = EntityTypeDAOFactory.INSTANCE.get(entityType);
        if (dao == null) {
            throw new VedantuException(VedantuErrorCode.ACTION_NOT_ALLOWED);
        }

        VedantuBaseMongoModel model = dao.getById(entityId, VedantuRecordState.ACTIVE);
        if (model == null) {
            throw new VedantuException(VedantuErrorCode.ACTION_NOT_ALLOWED, "no " + entityType
                    + " found for id[" + entityId + "]");

        }
    }

    protected static void updateParentCommentsCount(String userId, SrcEntity parent)
            throws VedantuException {

        ICommentable dao = (ICommentable) EntityTypeDAOFactory.INSTANCE.get(parent.type);
        LOGGER.debug("updating " + parent + ", comment count");
        VedantuBaseMongoModel model = dao.incCommentsCount(parent.id);
        EventType eventType = EntityIndexEventMapper.INSTANCE.get(parent.type);
        LOGGER.debug("eventType: " + eventType + ", for parentType : " + parent.type);
        if (eventType != null) {
            generateEventAysc(userId, model, EventActionType.UPDATE, eventType,
                    UserActionType.COMMENTED, false);
        }
    }

    protected static void updateParentUpVotesCount(String userId, SrcEntity parent)
            throws VedantuException {

        IUpVotable dao = (IUpVotable) EntityTypeDAOFactory.INSTANCE.get(parent.type);
        LOGGER.info("updating " + parent + ", vote count");
        VedantuBaseMongoModel model = dao.incUpVotesCount(parent.id);
        LOGGER.debug("updated model : " + model);
        // if (model instanceof IIndexable) {
        // EventType eventType = EntityIndexEventMapper.INSTANCE.get(parent.type);
        // Logger.debug("eventType: " + eventType + ", for parentType : " + parent.type);
        // if (eventType != null) {
        // generateEventAysc(userId, model, EventActionType.UPDATE, eventType,
        // UserActionType.VOTED, false);
        // }
        // }

    }

    protected static void updateParentFollowersCount(String userId, SrcEntity parent, int inc)
            throws VedantuException {

        IFollowable dao = (IFollowable) EntityTypeDAOFactory.INSTANCE.get(parent.type);
        LOGGER.debug("updating " + parent + ", follower count");
        VedantuBaseMongoModel model = dao.incFollowersCount(parent.id, inc);
        EventType eventType = EntityIndexEventMapper.INSTANCE.get(parent.type);
        LOGGER.debug("eventType: " + eventType + ", for parentType : " + parent.type);
        if (eventType != null && inc < 0) {
            // inc > 0 FOLLOW && inc < 0 UNFOLLOW in case of UNFOLLOW we need not send news feed so
            // just re-index the entity, in case of follow EntityUserActionUtils.updateEntityCount
            // method will be used
            generateEventAysc(userId, model, EventActionType.UPDATE, eventType,
                    UserActionType.FOLLOWING, false);
        }
    }

    protected static void updateAttemptsCount(String userId, SrcEntity parent, int inc)
            throws VedantuException {

        IAttemptable dao = (IAttemptable) EntityTypeDAOFactory.INSTANCE.get(parent.type);
        LOGGER.info("updating " + parent + ", attempt count");
        VedantuBaseMongoModel model = dao.incAttemptsCount(parent.id, 1);
        LOGGER.debug("updated model: " + model);

    }

    protected static void updateViewsCount(String userId, SrcEntity parent, int inc)
            throws VedantuException {

        IViewable dao = (IViewable) EntityTypeDAOFactory.INSTANCE.get(parent.type);
        LOGGER.debug("updating " + parent + ", view count");
        VedantuBaseMongoModel model = dao.incViewsCount(parent.id, 1);
        EventType eventType = EntityIndexEventMapper.INSTANCE.get(parent.type);
        LOGGER.debug("eventType: " + eventType + ", for parentType : " + parent.type);
        if (eventType != null) {
            generateEventAysc(userId, model, EventActionType.UPDATE, eventType,
                    UserActionType.ATTEMPTED, false);
        }
    }

    protected static BoolQueryBuilder buildSearchQuery(AbstractContentSearchReq searchReq,
            EntityType entityType) {

        MutableLong totalProgramHits = new MutableLong(0L);
        List<GranteeOrgProgram> granteeOrgPrograms = GranteeOrgProgramDAO.INSTANCE
                .getGranteeOrgPrograms(searchReq.orgId, null, totalProgramHits);
        List<String> grantedOrgs = new ArrayList<String>();
        for (GranteeOrgProgram granteeOrgProgram : granteeOrgPrograms) {
            grantedOrgs.add(granteeOrgProgram.providerOrgId);
        }
        BoolQueryBuilder boolQuery = buildSearchQuery(searchReq.resultType,
                searchReq._getResultForUserId(), searchReq.contentSrc, searchReq.includeTypes,
                searchReq.excludeTypes, searchReq.excludeIds, searchReq.query, entityType,
                grantedOrgs);
        try {
            addOrgStructureFilter(searchReq, boolQuery);
        } catch (VedantuException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return boolQuery;
    }

    protected static BoolQueryBuilder buildSearchQuery_(SearchResultType resultType, String userId,
            SrcEntity contentSrc, Collection<String> includeTypes, Collection<String> excludeTypes,
            List<String> excludeIds, String query, EntityType entityType) {
        return buildSearchQuery(resultType,
                userId, contentSrc, includeTypes,
                excludeTypes, excludeIds, query, entityType,null);

    };

    protected static BoolQueryBuilder buildSearchQuery(SearchResultType resultType, String userId,
            SrcEntity contentSrc, Collection<String> includeTypes, Collection<String> excludeTypes,
            List<String> excludeIds, String query, EntityType entityType, List<String> grantorOrgIds) {

        LOGGER.debug(" Getting " + entityType + " for user  :" + userId + " with query " + query);

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        if (contentSrc != null) {
            if (contentSrc.type.name() != EntityType.ORGANIZATION.toString()) {
                boolQuery.must(QueryBuilders.termQuery(ConstantsGlobal.CONTENT_SRC + "."
                        + ConstantsGlobal.ID, contentSrc.id));
            } else if (entityType == EntityType.DISCUSSION) {
                Organization org = OrganizationDAO.INSTANCE.getById(contentSrc.id);
                ArrayList<String> allOrgs = new ArrayList<String>();
                allOrgs.add(contentSrc.id);
                switch (org.doubtsForumMode) {
                case PRIVATE:
                    break;
                case PUBLIC:
                    allOrgs.add(Play.application().configuration().getString("learnpedia.id"));
                    break;
                default:
                    break;
                }
                boolQuery.must(QueryBuilders.termsQuery(ConstantsGlobal.CONTENT_SRC + "."
                        + ConstantsGlobal.ID, allOrgs.toArray()));
            } else {
                ArrayList<String> allOrgs = new ArrayList<String>();
                allOrgs.add(contentSrc.id);
                if (grantorOrgIds != null && grantorOrgIds.size() > 0) {
                    allOrgs.addAll(grantorOrgIds);
                }
                boolQuery.must(QueryBuilders.termsQuery(ConstantsGlobal.CONTENT_SRC + "."
                        + ConstantsGlobal.ID, allOrgs.toArray()));
            }
            boolQuery.must(QueryBuilders.termQuery(ConstantsGlobal.CONTENT_SRC + "."
                    + ConstantsGlobal.TYPE, contentSrc.type.name().toLowerCase()));

        }

        resultType.addSearchQueryFlter(boolQuery, userId);

        if (CollectionUtils.isNotEmpty(excludeIds)) {
            boolQuery.mustNot(QueryBuilders.inQuery(ConstantsGlobal.ID, excludeIds.toArray()));
        }
        if (CollectionUtils.isNotEmpty(includeTypes)) {
            boolQuery.must(QueryBuilders.inQuery(ConstantsGlobal.TYPE, VedantuStringUtils
                    .toLowerCase(includeTypes).toArray()));
        }
        if (CollectionUtils.isNotEmpty(excludeTypes)) {
            boolQuery.mustNot(QueryBuilders.inQuery(ConstantsGlobal.TYPE, VedantuStringUtils
                    .toLowerCase(excludeTypes).toArray()));
        }

        if (StringUtils.isNotEmpty(query)) {
            boolQuery.must(QueryBuilders.queryString(query.toLowerCase()));
        }
        return boolQuery;
    }

    protected static BoolFilterBuilder buildSearchFilter(AbstractContentSearchReq searchReq,
            EntityType entityType) {

        return buildSearchFilter(searchReq, entityType, false);
    }

    protected static BoolFilterBuilder buildSearchFilter(AbstractContentSearchReq searchReq,
            EntityType entityType, boolean noScopeFilter) {

        String userId = searchReq._getResultForUserId();

        BoolFilterBuilder boolFilter = FilterBuilders.boolFilter();
        ElasticSearchUtils.addBoardAndTargetFilter(searchReq.brdIds, searchReq.allBrds, boolFilter);
        if (!noScopeFilter) {
            ElasticSearchUtils.addScopeFilter(userId, boolFilter);
        } else {
            boolFilter.must(FilterBuilders.termsFilter(ConstantsGlobal.SCPOE, Scope.PUBLIC.name()
                    .toLowerCase(), Scope.ORG.name().toLowerCase()));
        }
        return boolFilter;
    }

    private static void addOrgStructureFilter(AbstractContentSearchReq searchReq,
            BoolQueryBuilder boolQuery) throws VedantuException {

    	// checking if current OrgId and  programId are not empty
        if (StringUtils.isNotEmpty(searchReq.orgId) && StringUtils.isNotEmpty(searchReq.programId)) {
        	//Marking current orgId to programOrgId
        	 String programOrgId=searchReq.orgId;
        	 MutableLong totalProgramHits = new MutableLong(0L);
        	 LOGGER.debug("Current orgId"+searchReq);
        	 // getting all the rows of the current orgId as OrgId as key
             List<GranteeOrgProgram> granteeOrgPrograms = GranteeOrgProgramDAO.INSTANCE.getGranteeOrgPrograms(searchReq.orgId, null, totalProgramHits);
             for (GranteeOrgProgram granteeOrgProgram : granteeOrgPrograms) {
            	 // if programId from search request matches the programId from the list above assign the granteeId as the programId
            	 if(granteeOrgProgram.programId.equalsIgnoreCase(searchReq.programId)){
            		 programOrgId=granteeOrgProgram.providerOrgId;
            	 }
     		}

            Collection<String> sectionIds = StringUtils.isNotEmpty(searchReq.sectionId) ? Arrays
                    .asList(searchReq.sectionId) : OrgProgramManager.getProgramSections(
                    programOrgId,
                    searchReq.programId,
                    StringUtils.isEmpty(searchReq.centerId) ? null : Arrays
                            .asList(searchReq.centerId));
            String childType = UserActionType.ADDED.getSearchIndexType();
            boolQuery.must(QueryBuilders.hasChildQuery(childType,
                    QueryBuilders.inQuery(childType + ".dst.id", sectionIds.toArray())));
        }
    }

    protected static <T extends IListResponseObj> SearchListResponse<T> getEntityInfos(
            AbstractContentSearchReq searchReq, EntityType entityType, Class<T> respObj,
            Set<String> returnedEntityIds) {

        AbstractFacetBuilder[] facets = ElasticSearchUtils.getBoardsTagFacets(searchReq.size);
        BoolQueryBuilder boolQuery = buildSearchQuery(searchReq, entityType);
        BoolFilterBuilder boolFilter = buildSearchFilter(searchReq, entityType);
        if (CollectionUtils.isNotEmpty(searchReq.includeModes)) {
            boolFilter.must(FilterBuilders.inFilter(ConstantsGlobal.MODE, VedantuStringUtils
                    .toLowerCase(searchReq.includeModes).toArray()));
        }
        if (CollectionUtils.isNotEmpty(searchReq.includeTypes)) {
            boolFilter.must(FilterBuilders.inFilter(ConstantsGlobal.TYPE, VedantuStringUtils
                    .toLowerCase(searchReq.includeTypes).toArray()));
        }
        if (CollectionUtils.isNotEmpty(searchReq.includeDifficulty)) {
            boolFilter.must(FilterBuilders.inFilter(ConstantsGlobal.DIFFICULTY, VedantuStringUtils
                    .toLowerCase(searchReq.includeDifficulty).toArray()));
        }
        if (CollectionUtils.isNotEmpty(searchReq.scope)) {
            boolFilter.must(FilterBuilders.inFilter(ConstantsGlobal.SCPOE, VedantuStringUtils
                    .toLowerCase(searchReq.scope).toArray()));
        }
        return getEntityInfos(searchReq.orderBy, searchReq.sortOrder, searchReq.start,
                searchReq.size, entityType, respObj, boolQuery, boolFilter, facets,
                returnedEntityIds);

    }

    /**
     *
     * @param getEntityReq
     * @param entityType
     * @param respObj
     * @param boolQuery
     * @param boolFilter
     * @param facets
     * @param returnedEntityIds
     *            --> provide an empty collections all returned entityIds will be added here
     * @return
     */
    protected static <T extends IListResponseObj> SearchListResponse<T> getEntityInfos(
            AbstractContentSearchReq getEntityReq, EntityType entityType, Class<T> respObj,
            BoolQueryBuilder boolQuery, BoolFilterBuilder boolFilter,
            AbstractFacetBuilder[] facets, Set<String> returnedEntityIds) {

        return getEntityInfos(getEntityReq.orderBy, getEntityReq.sortOrder, getEntityReq.start,
                getEntityReq.size, entityType, respObj, boolQuery, boolFilter, facets,
                returnedEntityIds);

    }

    protected static Map<String, Boolean> getUpVotesMap(String userId, Set<String> ids,
            EntityType entityType) {

        return null;
    }

    @SuppressWarnings("rawtypes")
    protected static <T extends IListResponseObj> SearchListResponse<T> getSimilarEntityInfos(
            GetSimilarEntities similarEntityReq, Class<T> respObj, Set<String> returnedEntityIds) {

        VedantuBasicDAO dao = EntityTypeDAOFactory.INSTANCE.get(similarEntityReq.entity.type);
        if (dao == null) {
            return new SearchListResponse<T>();
        }

        AbstractBoardEntityTagModel model = (AbstractBoardEntityTagModel) dao
                .getById(similarEntityReq.entity.id);
        if (model == null) {
            return new SearchListResponse<T>();
        }
        Set<String> brdIds = model.__getAllBoardIds();
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        // TODO: in case of question we also need to add type filter...
        // if (q.type != null) {
        // esQuery.should(QueryBuilders.fieldQuery(ConstantsGlobal.TYPE,
        // q.type.name().toLowerCase()));
        // }
        boolQuery.mustNot(QueryBuilders.fieldQuery(ConstantsGlobal.ID, similarEntityReq.entity.id));

        if (!brdIds.isEmpty()) {
            boolQuery.should(QueryBuilders.inQuery(ConstantsGlobal.BOARDS_ID, brdIds.toArray()));
            boolQuery.should(QueryBuilders.inQuery(ConstantsGlobal.TARGETS_ID, brdIds.toArray()));
        }
        if (CollectionUtils.isNotEmpty(model.tags)) {
            boolQuery.should(QueryBuilders.inQuery(ConstantsGlobal.TAGS, VedantuStringUtils
                    .toLowerCase(model.tags).toArray()));
        }
        if (model.contentSrc != null) {
            boolQuery.must(QueryBuilders.termQuery(ConstantsGlobal.CONTENT_SRC + "."
                    + ConstantsGlobal.ID, model.contentSrc.id));
            boolQuery.must(QueryBuilders.termQuery(ConstantsGlobal.CONTENT_SRC + "."
                    + ConstantsGlobal.TYPE, model.contentSrc.type.name().toLowerCase()));
        }
        BoolFilterBuilder boolFilter = FilterBuilders.boolFilter();
        ElasticSearchUtils.addScopeFilter(similarEntityReq.userId, boolFilter);

        return getEntityInfos(similarEntityReq.orderBy, similarEntityReq.sortOrder,
                similarEntityReq.start, similarEntityReq.size, similarEntityReq.entity.type,
                respObj, boolQuery, boolFilter, null, returnedEntityIds);

    }

    protected static <T extends IListResponseObj> SearchListResponse<T> getEntityInfos(
            String orderBy, String sortOrder, int start, int size, EntityType entityType,
            Class<T> respObj, BoolQueryBuilder boolQuery, BoolFilterBuilder boolFilter,
            AbstractFacetBuilder[] facets, Set<String> returnedEntityIds) {

        QueryBuilder esQuery = QueryBuilders.filteredQuery(boolQuery.hasClauses() ? boolQuery
                : QueryBuilders.matchAllQuery(),
                boolFilter != null ? boolFilter : FilterBuilders.matchAllFilter());
        return getEntityInfos(orderBy, sortOrder, start, size, entityType, respObj, esQuery,
                facets, returnedEntityIds);
    }

    protected static <T extends IListResponseObj> SearchListResponse<T> getEntityInfos(
            String orderBy, String sortOrder, int start, int size, EntityType entityType,
            Class<T> respObj, QueryBuilder esQuery, AbstractFacetBuilder[] facets,
            Set<String> returnedEntityIds) {

        return getEntityInfos(orderBy, sortOrder, start, size, respObj, esQuery, facets,
                returnedEntityIds, entityType.getIndexName(), entityType.getIndexType());
    }

    protected static <T extends IListResponseObj> SearchListResponse<T> getEntityInfos(
            String orderBy, String sortOrder, int start, int size, Class<T> respObj,
            QueryBuilder esQuery, AbstractFacetBuilder[] facets, Set<String> returnedEntityIds,
            String indexName, String indexType) {

        SearchResponse response = ElasticSearchUtils.getSearchResponse(esQuery, orderBy, sortOrder,
                start, size, indexName, indexType, null, facets);

        if (response == null || response.getHits().getTotalHits() == 0) {
            LOGGER.error("empty search response for query : " + esQuery);
            return new SearchListResponse<T>();
        }

        SearchListResponse<T> listResponse = new SearchListResponse<T>();

        SearchHits allHits = response.getHits();
        listResponse.totalHits = allHits.getTotalHits();
        LOGGER.debug("totalHits: " + listResponse.totalHits);
        for (SearchHit hits : allHits.getHits()) {
            LOGGER.trace("hits : " + hits.sourceAsString());
            T model = ObjectMapperUtils.convertValue(hits.sourceAsMap(), respObj);
            if (returnedEntityIds != null && hits.sourceAsMap().get(ConstantsGlobal.ID) != null) {
                returnedEntityIds.add(hits.sourceAsMap().get(ConstantsGlobal.ID).toString());
            }
            listResponse.list.add(model);
        }
        if (facets != null && facets.length > 0) {
            ElasticSearchUtils.addCommonFacetDetails(listResponse.facet, response);
        }
        return listResponse;
    }

	protected static <T extends IListResponseObj> SearchListResponse<T> getEntityQuestionInfos(
			String orderBy, String sortOrder, int start, int size,
			Class<T> respObj, QueryBuilder esQuery,
			AbstractFacetBuilder[] facets, Set<String> returnedEntityIds,
			String indexName, String indexType) {
		int count = ElasticSearchUtils.getQueryCount(esQuery,indexName,indexType);
		if(count > 0){
			size = count;
		}
		SearchResponse response = ElasticSearchUtils.getSearchResponse(esQuery,
				orderBy, sortOrder, start, size, indexName, indexType, null,
				facets);

		if (response == null || response.getHits().getTotalHits() == 0) {
			LOGGER.error("empty search response for query : " + esQuery);
			return new SearchListResponse<T>();
		}
		SearchListResponse<T> listResponse = new SearchListResponse<T>();
		SearchHits allHits = response.getHits();
		//Remove duplicate hits if present.
		Map<String, SearchHit> hitsMap = new HashMap<String, SearchHit>();
		for (SearchHit hits : allHits.getHits()) {
			String id = hits.getSource().get("id").toString();
			long lastUpdated = (Long) hits.getSource().get("lastUpdated");
			if (hitsMap.containsKey(id)) {
				LOGGER.debug("getEntityQuestionInfos inside if ");
				SearchHit hit = hitsMap.get(id);
				long hitLastUpdated = (Long) hit.getSource().get("lastUpdated");
				if (lastUpdated > hitLastUpdated) {
					hitsMap.put(id, hits);
				}
			} else {
				LOGGER.debug("getEntityQuestionInfos inside else ");
				hitsMap.put(id, hits);
			}
		}
		listResponse.totalHits = hitsMap.keySet().size();
		LOGGER.debug("totalHits: " + listResponse.totalHits);
		for (String hitId : hitsMap.keySet()) {
			SearchHit hits = hitsMap.get(hitId);
            LOGGER.trace("hits : " + hits.sourceAsString());
			T model = ObjectMapperUtils.convertValue(hits.sourceAsMap(),
					respObj);
			if (returnedEntityIds != null
					&& hits.sourceAsMap().get(ConstantsGlobal.ID) != null) {
				returnedEntityIds.add(hits.sourceAsMap()
						.get(ConstantsGlobal.ID).toString());
			}
			listResponse.list.add(model);
		}
		if (facets != null && facets.length > 0) {
			ElasticSearchUtils.addCommonFacetDetails(listResponse.facet,
					response);
		}
		return listResponse;
	}

    public static AbstractBoardSearchEntityTagDetails annotateExtraInfo(String userId,
            String orgId, EntityType entityType, AbstractBoardSearchEntityTagDetails entity) {

        List<? extends AbstractBoardSearchEntityTagDetails> entities = Arrays.asList(entity);
        annotateExtraInfo(userId, orgId, entityType, entities);
        return entities.get(0);
    }

    public static void annotateExtraInfo(String userId, String orgId, EntityType entityType,
            List<? extends AbstractBoardSearchEntityTagDetails> entities) {

        annotateExtraInfo(StringUtils.EMPTY, userId, orgId, entityType, entities, false);
    }

    public static void annotateExtraInfo(String secId, String userId, String orgId, EntityType entityType,
            List<? extends AbstractBoardSearchEntityTagDetails> entities) {

        annotateExtraInfo(secId, userId, orgId, entityType, entities, false);
    }

    public static void annotateExtraInfo(String secId, String userId, String orgId, EntityType entityType,
            List<? extends AbstractBoardSearchEntityTagDetails> entities,
            boolean excludeOrgMappingInfo) {

        Set<String> brdIds = new HashSet<String>();

        Set<String> userIds = new HashSet<String>();
        Set<String> entityIds = new HashSet<String>();

        for (AbstractBoardSearchEntityTagDetails details : entities) {
            userIds.add(details.userId);
            entityIds.add(details.id);
            brdIds.addAll(details._getBoardsIds());
        }
        Map<String, BoardBasicInfo> boardsInfoMap = BoardManager.getInfosMap(brdIds);
        // trying to get user details without orgId
        Map<String, ModelBasicInfo> userInfos = getUserInfoMap(null, userIds,
                excludeOrgMappingInfo);

        Map<String, Boolean> entityVoteMap = null;//EntityUserActionDAO.INSTANCE.getEntityUpVoteMap(
                //userId, entityIds);

        Map<String, Boolean> entityAttemptMap = null;
        Map<String, Map<String, Long>> entityStartEndTime = null;
        if (entityType == EntityType.QUESTION || entityType == EntityType.TEST) {
            entityAttemptMap = AnalyticsManager.getEntityAttemptsMap(entityIds, userId);
        }
        if(!StringUtils.isEmpty(secId) && entityType == EntityType.TEST){
            entityStartEndTime = LibraryContentLinksDAO.INSTANCE.getEntityStartEndTime(secId, entityIds);
        }
        Map<String, FollowType> followTypeMap = EntityUserActionDAO.INSTANCE
                .getEntityFollowTypeMap(userId, entityType, entityIds);

        for (AbstractBoardSearchEntityTagDetails details : entities) {
            LOGGER.debug("user info : " + userInfos.get(details.userId) + ", userId: "
                    + details.userId);
            details.boardTree = details.fetchBoardTree(boardsInfoMap);
            LOGGER.debug("details boardTree info : " + details.boardTree);
            details.boards = null;
            if(!StringUtils.isEmpty(secId) && entityType == EntityType.TEST){
                details.startTime = entityStartEndTime.get(details.id).get("startTime");
                details.endTime = entityStartEndTime.get(details.id).get("endTime");
                details.closeTime = entityStartEndTime.get(details.id).get("closeTime");
            }
            if (details instanceof IAttemptableEntity) {
                addAttemptInfo(userId, entityAttemptMap, (IAttemptableEntity) details);
            }
            if (details instanceof ISocialEntity) {
                addSocialActionInfo(userId, details.userId, entityVoteMap, followTypeMap,
                        (ISocialEntity) details);
            }
            if (details instanceof IReverseImageMapperProcessor) {
                ((IReverseImageMapperProcessor) details).addImageSrcUrl();
            }
            details.user = userInfos.get(details.userId);
        }
    }

    public static String addLiveEntityToSearchIndex(final AbstractSearchDetail details,
            final EntityType entityType) {

        return addLiveEntityToSearchIndex(details, entityType, false);
    }

    @SuppressWarnings("unchecked")
    public static String addLiveEntityToSearchIndex(final AbstractSearchDetail details,
            final EntityType entityType, final boolean ensureQueryState) {

        QueryBuilder esQuery = QueryBuilders.termQuery(details._getUniqueId().getName(), details
                ._getUniqueId().getValue());
        SearchHit searchHit = ElasticSearchUtils.findOne(entityType.getIndexName(),
                entityType.getIndexType(), esQuery);
        LOGGER.debug(" searchHit " + searchHit);
        if (searchHit != null) {
            LOGGER.debug("entity already indexed ");
            String id = ElasticSearchManager.getInstance().reIndex(searchHit.getIndex(),
                    ObjectMapperUtils.convertValue(details, Map.class), searchHit.getId(),
                    searchHit.getType());
            if (StringUtils.isNotEmpty(id)) {
                ContentManager.addOrUpdateContentSearchDetails(details);
            }

            return id;
        }
        LOGGER.debug("Indexing entity Live now ");
        String esId = ElasticSearchManager.getInstance().addIndex(entityType.getIndexName(),
                entityType.getIndexType(), ObjectMapperUtils.convertValue(details, Map.class));

        if (StringUtils.isNotEmpty(esId)) {
            ContentManager.addOrUpdateContentSearchDetails(details, ensureQueryState);
        }

        if (StringUtils.isNotEmpty(esId) && ensureQueryState) {
            boolean isQueriable = false;

            int tryCount = 0;
            while (!isQueriable && tryCount < ES_ENSURE_QUERY_STATE_MAX_TRY_COUNT) {
                tryCount++;
                LOGGER.debug("tryCount: " + tryCount + ", query:" + esQuery);
                searchHit = ElasticSearchUtils.findOne(entityType.getIndexName(),
                        entityType.getIndexType(), esQuery);
                if (searchHit != null) {
                    isQueriable = true;
                } else {
                    try {

                        Thread.sleep(ELASTIC_SEARCH_REFRESH_TIME);

                    } catch (InterruptedException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            }
        }
        return esId;
    }

    public static boolean removeLiveEntityToSearchIndex(final String id,
            final EntityType entityType, final boolean ensureQueryState) {

        boolean result = ElasticSearchManager.getInstance().removeEntry(entityType.getIndexName(),
                entityType.getIndexType(), ConstantsGlobal.ID, id);

        FieldQueryBuilder esQuery = QueryBuilders.fieldQuery(ConstantsGlobal.ID, id);
        SearchHit searchHit = null;
        boolean isQueriable = true;
        if (result && ensureQueryState) {
            int tryCount = 0;
            while (isQueriable && tryCount < ES_ENSURE_QUERY_STATE_MAX_TRY_COUNT) {
                tryCount++;
                LOGGER.debug("tryCount: " + tryCount);
                searchHit = ElasticSearchUtils.findOne(entityType.getIndexName(),
                        entityType.getIndexType(), esQuery);
                if (searchHit == null) {
                    isQueriable = false;
                } else {
                    try {

                        Thread.sleep(ELASTIC_SEARCH_REFRESH_TIME);

                    } catch (InterruptedException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            }
        }
        return !isQueriable;
    }

    protected static void generateEventAysc(final String userId, final VedantuBaseMongoModel model,
            final EventActionType action, final EventType eventType,
            final UserActionType userAction, final boolean notificationEnabled)
            throws VedantuException {

        try {
            AbstractSearchDetail details = (AbstractSearchDetail) EventDetailsFactory.getInstance()
                    .getDetails(eventType);

            details.userAction = userAction;
            details.isNotificationEnabled = notificationEnabled;

            details.setAction(action.name());
            IMongoAware mongoDetails = details;
            mongoDetails.fromMongoModel(model);
            LOGGER.debug("loaded IndexDetails from mongomodel: " + details);
            generateEventAysc(userId, details, eventType);
        } catch (Exception exception) {
            throw new VedantuException(VedantuErrorCode.EVENT_NOT_SCHEDULED);
        }

    }

    @SuppressWarnings("unchecked")
    protected static boolean
            delete(final String userId, EventType indexEventType, SrcEntity content)
                    throws VedantuException {

        @SuppressWarnings({ "rawtypes" })
        VedantuBasicDAO contentDAO = EntityTypeDAOFactory.INSTANCE.get(content.type);
        VedantuBaseMongoModel basicMongoModel = contentDAO.getById(content.id);
        if (basicMongoModel == null) {
            return false;
        }
        basicMongoModel.recordState = VedantuRecordState.DELETED;
        contentDAO.save(basicMongoModel);

        if (basicMongoModel instanceof IIndexable) {

            generateEventAysc(userId, basicMongoModel, EventActionType.REMOVE, indexEventType,
                    UserActionType.DELETED, false);
        }

        NewsRemoveDetails newsRemoveDetails = new NewsRemoveDetails();
        newsRemoveDetails.content = content;
        AbstractVedantuEventManager.generateEventAysc(userId, newsRemoveDetails,
                EventType.REMOVE_NEWS);
        return true;
    }

    public static void updateUserActionMappintToEs(IRelationshipSearchDetails details,
            SrcEntity parent, UserActionType actionType, EventActionType eventAction) {

        updateUserActionMappintToEs(details, parent, actionType, eventAction, null);
    }

    public static void updateUserActionMappintToEs(IRelationshipSearchDetails details,
            SrcEntity parent, UserActionType actionType, EventActionType eventAction,
            String parentEsId) {

        updateUserActionMappintToEs(details, parent, actionType.getSearchIndexType(), eventAction,
                parentEsId);
    }

    public static void updateUserActionMappintToEs(IRelationshipSearchDetails details,
            SrcEntity parent, String indexType, EventActionType eventAction, String parentEsId) {

        updateUserActionMappintToEs(details, parent, parent.type.getIndexName(),
                parent.type.getIndexType(), indexType, eventAction, parentEsId);

    }

    public static void updateUserActionMappintToEs(IRelationshipSearchDetails details,
            SrcEntity parent, String indexName, String indexType, String mappingIndexType,
            EventActionType eventAction, String parentEsId) {

        if (StringUtils.isEmpty(indexName) || StringUtils.isEmpty(indexType)) {
            return;
        }
        QueryBuilder esQuery = QueryBuilders.termQuery(ConstantsGlobal.ID, parent.id);

        // we already have the es parent _id
        SearchHit searchHit = StringUtils.isNotEmpty(parentEsId) ? null : ElasticSearchUtils
                .findOne(indexName, indexType, esQuery);
        if (StringUtils.isNotEmpty(parentEsId) || searchHit != null) {
            if (searchHit != null) {
                parentEsId = searchHit.getId();
            }
            if (eventAction == EventActionType.ADD || eventAction == EventActionType.UPDATE) {
                LOGGER.info("adding details to es mapping : " + details + " with parent type "
                        + parent.type + "  and parent es Id " + parentEsId);
                ElasticSearchUtils.addMappingToES(indexName, mappingIndexType.toLowerCase(),
                        details, parentEsId, true);
            } else if (eventAction == EventActionType.REMOVE) {

                LOGGER.debug("removing details to es mapping : " + details + " with parent type "
                        + parent.type + "  and parent es Id " + parentEsId);

                ElasticSearchManager.getInstance().removeEntry(searchHit.getIndex(),
                        mappingIndexType.toLowerCase(), details._getEsQuery());

            }
        } else {
            LOGGER.debug("no hits found for query:" + esQuery);
        }
    }

    public static IListResponseObj collectResourceInfo() {

        return null;
    }

    protected static void addAttemptInfo(String userId, Map<String, Boolean> entityAttemptMap,
            IAttemptableEntity entity) {

        entity._setAttempted(entityAttemptMap != null
                && entityAttemptMap.get(entity._getEntityId()) != null ? entityAttemptMap.get(
                entity._getEntityId()).booleanValue() : false);
    }

    protected static void addSocialActionInfo(String userId, String entityOwnerId,
            Map<String, Boolean> entityVoteMap, Map<String, FollowType> followTypeMap,
            ISocialEntity entity) {

        entity._setVoted(entityVoteMap != null && entityVoteMap.get(entity._getEntityId()) != null ? entityVoteMap
                .get(entity._getEntityId()).booleanValue() : false);
        FollowType followType = StringUtils.equals(userId, entityOwnerId) ? FollowType.OWNER
                : (followTypeMap != null && followTypeMap.get(entity._getEntityId()) != null ? followTypeMap
                        .get(entity._getEntityId()) : FollowType.NONE);
        entity._setFollowType(followType);
    }

    protected static void annotateUserSocialActionInfos(String orgId, String userId,
            EntityType entityType, List<? extends AbstractContentUserActionRes> entites,
            Set<String> userIds, Set<String> entityIds) {

        Map<String, ModelBasicInfo> userInfoMap = getUserInfoMap(orgId, userIds);
        Map<String, Boolean> entityVoteMap = EntityUserActionDAO.INSTANCE.getEntityUpVoteMap(
                userId, entityIds);
        Map<String, FollowType> followTypeMap = EntityUserActionDAO.INSTANCE
                .getEntityFollowTypeMap(userId, entityType, entityIds);
        for (AbstractContentUserActionRes res : entites) {
            String usrId = res.user.id;
            addSocialActionInfo(userId, usrId, entityVoteMap, followTypeMap, res);
            res.user = (UserInfo) userInfoMap.get(usrId);
        }
    }

    public static String removeTempImageSrcAndSaveToFS(EntityType entityType, String content,
            boolean moveImage, String folder) throws EntityFileStorageException {

        return removeTempImageSrcAndSaveToFS(entityType, content, moveImage, null, null, folder);
    }

    public static String removeTempImageSrcAndSaveToFS(EntityType entityType, String content,
            boolean moveImage, Set<String> uuids, Map<String, String> tags, String folder)
            throws EntityFileStorageException {

        LOGGER.debug("removeTempImageSrcAndSaveToFS content: " + content);
        IEntityFileStorage entityStorage = EntityStorageFactory.INSTANCE.get(entityType);
        LocalFileSystemHandler tempFs = FileSystemFactory.INSTANCE.getTempFS();
        if (uuids == null) {
            uuids = new HashSet<String>();
        }
        content = ImageHTMLUtils.removeImageSrcUrl(content, uuids);
        if (moveImage) {

            for (String uuid : uuids) {
                String filePath = tempFs.getFilePath(StringUtils.isEmpty(folder) ? "images" : folder, uuid
                        + ImageDisplayURLUtil.JPG_EXTENTION);
                File file = new File(filePath);
                entityStorage.storeImage(uuid, file, FileCategory.CONVERTED, ImageSize.ORIGINAL,
                        null);
                FileUtils.deleteFile(filePath, file);
            }
        }

        LOGGER.debug("removeTempImageSrcAndSaveToFS updated content: " + content);
        return content;
    }

    public static UserInfo getUserInfo(String orgId, String userId) {

        Map<String, ModelBasicInfo> userInfos = getUserInfoMap(orgId, Arrays.asList(userId));

        return (UserInfo) userInfos.get(userId);
    }

    public static UserInfo getUserInfo(String orgId, String userId, boolean excludeOrgMappingInfo) {

        Map<String, ModelBasicInfo> userInfos = getUserInfoMap(orgId, Arrays.asList(userId),
                excludeOrgMappingInfo);

        return (UserInfo) userInfos.get(userId);
    }

    public static Map<String, ModelBasicInfo> getUserInfoMap(String orgId,
            Collection<String> userIds) {

        return getUserInfoMap(orgId, userIds, false);
    }

    public static Map<String, ModelBasicInfo> getUserInfoMap(String orgId,
            Collection<String> userIds, boolean excludeOrgMappingInfo) {

        return OrgMemberManager.getUserInfoMap(orgId, userIds, excludeOrgMappingInfo);
    }

    protected void annotateLinkInfo(AbstractFileModelIndexSearchDetails model) {

        if (model.linkInfo != null) {
            model.linkInfo.populate();
        }
    }

    protected void annotateLinkInfo(List<? extends AbstractFileModelIndexSearchDetails> models) {

        if (CollectionUtils.isNotEmpty(models)) {
            for (AbstractFileModelIndexSearchDetails model : models) {
                if (model.linkInfo != null) {
                    model.linkInfo.populate();
                }
            }
        }
    }

    @Override
    public boolean update(EditContentReq request) throws VedantuException {

        // This is a dummy implementation
        return false;
    }

    @Override
    public boolean calculate(String id, boolean recalculate,VedantuBaseMongoModel...contents) throws VedantuException{
        return false;
    }


    @Override
    public List<DownloadableFileInfo> getFiles(EntityType entityType, String entityId) throws VedantuException, EntityFileStorageException {

        // TODO Auto-generated method stub
        return null;
    }

}
