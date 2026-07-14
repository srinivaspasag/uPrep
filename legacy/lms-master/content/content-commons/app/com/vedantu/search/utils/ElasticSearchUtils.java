package com.vedantu.search.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.count.CountRequestBuilder;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.AndFilterBuilder;
import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.OrFilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.facet.AbstractFacetBuilder;
import org.elasticsearch.search.facet.Facet;
import org.elasticsearch.search.facet.FacetBuilders;
import org.elasticsearch.search.facet.Facets;
import org.elasticsearch.search.facet.statistical.StatisticalFacet;
import org.elasticsearch.search.facet.terms.TermsFacet;
import org.elasticsearch.search.facet.terms.TermsFacet.Entry;
import org.elasticsearch.search.facet.terms.TermsFacetBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;

import play.Logger;
import play.Logger.ALogger;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.vedantu.board.managers.BoardManager;
import com.vedantu.board.pojos.BoardBasicInfo;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.enums.boards.BoardType;
import com.vedantu.commons.relationships.IRelationshipSearchDetails;
import com.vedantu.commons.utils.ObjectMapperUtils;
import com.vedantu.commons.utils.VedantuStringUtils;
import com.vedantu.content.search.details.boards.BoardSearchEntity;
import com.vedantu.search.es.ElasticSearchManager;

public class ElasticSearchUtils {

    private static final ALogger LOGGER   = Logger.of(ElasticSearchUtils.class);
    public static final int      NO_LIMIT = -1;
    public static final int      NO_START = 0;

    // private static final int MAX_TAGS = 3;

    public static SearchRequestBuilder buildSearchRequest(QueryBuilder esQuery,
            List<String> orderBys, List<String> sortOrders, int start, int size, String indexName,
            String indexType, List<String> highlighFields, boolean scriptSort,
            AbstractFacetBuilder... facets) {

        List<SortOrder> sortingOrders = new ArrayList<SortOrder>();
        if (CollectionUtils.isNotEmpty(sortOrders)) {
            for (String sortOrder : sortOrders) {
                SortOrder soOrder = SortOrder.DESC;
                try {
                    soOrder = StringUtils.isNotEmpty(sortOrder) ? SortOrder.valueOf(sortOrder
                            .toUpperCase()) : SortOrder.DESC;
                } catch (Exception e) {
                    LOGGER.error(" invalud sortOrder: " + sortOrder, e);
                }
                sortingOrders.add(soOrder);
            }
        }
        LOGGER.debug("ELASTICSEARCH_UTILS: search query : " + esQuery);
        ElasticSearchManager es = ElasticSearchManager.getInstance();
        Client client = es.getClient();
        SearchRequestBuilder search = null;
        try {
            if (size == 0) {
                size = NO_LIMIT;
            }
            search = client.prepareSearch(indexName).setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                    .setQuery(esQuery).setFrom(start).setSize(size).setExplain(true);
            if (StringUtils.isNotEmpty(indexType)) {
                search.setTypes(indexType.trim());
            }
            int i = 0;
            if (CollectionUtils.isNotEmpty(orderBys)) {
                for (String orderBy : orderBys) {
                    SortOrder sortOrder = SortOrder.DESC;
                    try {
                        sortOrder = sortingOrders.get(i);
                    } catch (Exception e) {
                        LOGGER.error("mismatch on orderBy Array and sortOrder Array");
                    }
                    if (scriptSort && StringUtils.isNotEmpty(orderBy)) {
                        search = search.addSort(SortBuilders.scriptSort(
                                "doc['" + orderBy + "'].value", "string").order(sortOrder));
                    } else if (StringUtils.equalsIgnoreCase(orderBy, "mostPopular")) {

                        search = search
                                .addSort(SortBuilders
                                        .scriptSort(
                                                EntityType.isAttemptableEntity(EntityType
                                                        .valueOfKey(indexType)) ? "doc['attempts'].value"
                                                        : ""
                                                                + "doc['views'].value * 0.4 + doc['followers'].value + doc['avgRating'].value + doc['upVotes'].value",
                                                "number").order(sortOrder));
                    } else if (StringUtils.isNotEmpty(orderBy)) {
                        search = search.addSort(SortBuilders.fieldSort(orderBy)
                                .ignoreUnmapped(true).order(sortOrder));
                    }
                    i++;
                }
            }
            if (facets != null) {
                for (AbstractFacetBuilder facet : facets) {
                    search = search.addFacet(facet);
                }
            }
            if (CollectionUtils.isNotEmpty(highlighFields)) {
                for (String field : highlighFields) {
                    search = search.addHighlightedField(field);
                }
            }
            LOGGER.debug("[index : " + indexName + ", type : " + indexType + "], esQuery   : "
                    + esQuery);
            es.closeClient(client);
        } catch (Exception e) {
            LOGGER.error("error on retreiving data from ES, " + e.getMessage(), e);
        }
        return search;
    }

    public static MultiSearchResponse getMultiSearchResponse(List<SearchRequest> searchRequests) {

        ElasticSearchManager es = ElasticSearchManager.getInstance();
        Client client = es.getClient();
        MultiSearchRequest multiSearchRequest = new MultiSearchRequest();
        for (SearchRequest searchRequest : searchRequests) {
            multiSearchRequest.add(searchRequest);
        }
        MultiSearchResponse multiSearchResponse = client.multiSearch(multiSearchRequest)
                .actionGet();
        return multiSearchResponse;
    }

    public static SearchResponse getSearchResponse(QueryBuilder esQuery, int start, int size,
            String indexName, String indexNodeName, List<String> highlighFields,
            AbstractFacetBuilder... facets) {

        return getSearchResponse(esQuery, null, null, start, size, indexName, indexNodeName,
                highlighFields, facets);
    }

    public static SearchResponse getSearchResponse(QueryBuilder esQuery, String orderBy,
            String sortOrder, int start, int size, String indexName, String indexNodeName,
            List<String> highlighFields, AbstractFacetBuilder... facets) {

        return getSearchResponse(esQuery, orderBy, sortOrder, start, size, indexName,
                indexNodeName, highlighFields, false, facets);
    }

    public static SearchResponse getSearchResponse(QueryBuilder esQuery, String orderBy,
            String sortOrder, int start, int size, String indexName, String indexNodeName,
            List<String> highlighFields, boolean scriptSort, AbstractFacetBuilder... facets) {

        LOGGER.debug("OrderBy:" + orderBy + " sortOrder:" + sortOrder);
        return getSearchResponse(esQuery, (StringUtils.isNotEmpty(orderBy) ? Arrays.asList(orderBy)
                : null), (StringUtils.isNotEmpty(sortOrder) ? Arrays.asList(sortOrder) : null),
                start, size, indexName, indexNodeName, highlighFields, scriptSort, facets);
    }

    public static SearchResponse getSearchResponse(QueryBuilder esQuery, List<String> orderBys,
            List<String> sortOrders, int start, int size, String indexName, String indexNodeName,
            List<String> highlighFields, boolean scriptSort, AbstractFacetBuilder... facets) {

        SearchRequestBuilder search = buildSearchRequest(esQuery, orderBys, sortOrders, start,
                size, indexName, indexNodeName, highlighFields, scriptSort, facets);
        SearchResponse response = searchResponse(search);
        return response;
    }

    private static SearchResponse searchResponse(SearchRequestBuilder searchRequest) {

        try {
            return searchRequest.execute().actionGet();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    public static AbstractFacetBuilder[] getBoardsTagFacets(int size) {

        return getBoardsTagFacets(null, null, null, size);
    }

    public static AbstractFacetBuilder[] getBoardsTagFacets(Collection<String> excludeBrdIds,
            Collection<String> excludeTargetIds, Collection<String> excludeTags, int size) {
        TermsFacetBuilder boardFacet = null;
        if(size > 0){
            boardFacet = FacetBuilders.termsFacet("boardFacet").field(
                    ConstantsGlobal.BOARDS_ID).size(size);
        }else{
            boardFacet = FacetBuilders.termsFacet("boardFacet").field(
                    ConstantsGlobal.BOARDS_ID);
        }
        if (CollectionUtils.isNotEmpty(excludeBrdIds)) {
            boardFacet.exclude(excludeBrdIds.toArray());
        }

        TermsFacetBuilder targetFacet = FacetBuilders.termsFacet("targetFacet").field(
                ConstantsGlobal.TARGETS_ID);
        if (CollectionUtils.isNotEmpty(excludeTargetIds)) {
            targetFacet.exclude(excludeTargetIds.toArray());
        }

        AbstractFacetBuilder tagFacet = facetBuilderFromSource("tagFacet", ConstantsGlobal.TAGS,
                excludeTags, size);
        if (CollectionUtils.isNotEmpty(excludeTags)) {
            VedantuStringUtils.toLowerCase(excludeTags);
            tagFacet.facetFilter(FilterBuilders.notFilter(FilterBuilders.inFilter(
                    ConstantsGlobal.TAGS, excludeTags.toArray())));
        }
        return new AbstractFacetBuilder[] { boardFacet, targetFacet, tagFacet };
    }

    public static Map<String, Object> facetQueryResult(String indexName, String indexType,
            AbstractFacetBuilder... facets) {

        return facetQueryResult(indexName, indexType, null, facets);
    }

    public static Map<String, Object> facetQueryResult(String indexName, String indexType,
            QueryBuilder query, AbstractFacetBuilder... facets) {

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        SearchResponse searchResponse = getSearchResponse(
                (query == null ? QueryBuilders.matchAllQuery() : query), 0, 0, indexName,
                indexType, null, facets);
        addAllFacetDetails(result, searchResponse);
        return result;
    }

    public static void
            addAllFacetDetails(Map<String, Object> result, SearchResponse searchResponse) {

        Map<String, Map<String, Integer>> facetResultMap = getMultipleFacetMap(searchResponse);
        LOGGER.debug("facet map : " + facetResultMap);
        result.put(ConstantsGlobal.FACET, facetResultMap);
    }

    public static void addStatisticalFacetDetails(Map<String, Object> result,
            SearchResponse searchResponse, String facetName) {

        Map<String, Map<String, Integer>> facetResultMap = getMultipleFacetMap(searchResponse);
        LOGGER.debug("statistical facet map : " + facetResultMap);
        result.putAll(facetResultMap.get(facetName));
    }

    public static void addCommonFacetDetails(Map<String, Object> result,
            SearchResponse searchResponse) {

        Map<String, Map<String, Integer>> facetResultMap = getMultipleFacetMap(searchResponse);
        result.putAll(getFacetCommonResponse(facetResultMap));
    }

    private static Map<String, Object> getFacetCommonResponse(
            Map<String, Map<String, Integer>> facetResultMap) {

        Map<String, Object> facetObject = new HashMap<String, Object>();
        Map<String, Integer> targetAndBoardFacet = new LinkedHashMap<String, Integer>();
        if (facetResultMap.get("boardFacet") != null) {
            targetAndBoardFacet.putAll(facetResultMap.get("boardFacet"));
        }
        if (facetResultMap.get("targetFacet") != null) {
            targetAndBoardFacet.putAll(facetResultMap.get("targetFacet"));
        }
        List<DBObject> targetFacetList = new ArrayList<DBObject>();
        List<DBObject> courseFacetList = new ArrayList<DBObject>();
        List<DBObject> topicFacetList = new ArrayList<DBObject>();
        List<DBObject> subTopicFacetList = new ArrayList<DBObject>();

        Map<String, BoardBasicInfo> boardBasicInfoMap = BoardManager
                .getInfosMap(targetAndBoardFacet.keySet());
        // TODO: add boards info,
        // BoardUtil.getBoardSearchEntityMap(targetAndBoardFacet.keySet());
        for (java.util.Map.Entry<String, Integer> entry : targetAndBoardFacet.entrySet()) {
            DBObject target = new BasicDBObject();
            BoardBasicInfo basicInfo = boardBasicInfoMap.get(entry.getKey());
            if (basicInfo == null) {
                continue;
            }
            BoardSearchEntity boardSearchEntity = new BoardSearchEntity();
            boardSearchEntity.fromBoardBasicInfo(basicInfo);
            target.put("obj", boardSearchEntity);
            target.put("count", entry.getValue());
            if (boardSearchEntity.type == BoardType.EXAM) {
                targetFacetList.add(target);
            } else if (boardSearchEntity.type == BoardType.COURSE) {
                courseFacetList.add(target);
            } else if (boardSearchEntity.type == BoardType.TOPIC) {
                topicFacetList.add(target);
            } else if (boardSearchEntity.type == BoardType.SUBTOPIC) {
                subTopicFacetList.add(target);
            }
        }
        Map<String, Integer> tagFacet = facetResultMap.get("tagFacet");
        List<Map<String, Object>> tagFacetList = new ArrayList<Map<String, Object>>();
        if (tagFacet != null) {
            for (java.util.Map.Entry<String, Integer> entry : tagFacet.entrySet()) {
                Map<String, Object> target = new HashMap<String, Object>();
                target.put("obj", entry.getKey());
                target.put("count", entry.getValue());
                tagFacetList.add(target);
            }
        }
        facetObject.put(ConstantsGlobal.TARGETS, targetFacetList);
        facetObject.put(ConstantsGlobal.COURSES, courseFacetList);
        facetObject.put(ConstantsGlobal.TOPICS, topicFacetList);
        facetObject.put(ConstantsGlobal.SUB_TOPICS, subTopicFacetList);
        facetObject.put(ConstantsGlobal.TAGS, tagFacetList);
        return facetObject;
    }

    public static void addScopeFilter(String userId, BoolFilterBuilder boolFilter) {

        OrFilterBuilder orFilter = FilterBuilders
                .orFilter(FilterBuilders.termsFilter(ConstantsGlobal.SCPOE, Scope.PUBLIC.name()
                        .toLowerCase(), Scope.ORG.name().toLowerCase()), FilterBuilders.termFilter(
                        ConstantsGlobal.USER_ID, userId));
        boolFilter.must(orFilter);
    }

    public static void addUserIdFilter(String userId, BoolFilterBuilder boolFilter) {

        OrFilterBuilder orFilter = FilterBuilders
                .orFilter(FilterBuilders.termFilter(
                        ConstantsGlobal.USER_ID, userId));
        boolFilter.must(orFilter);
    }

    public static boolean addBoardAndTargetFilter(Collection<String> brdIds, boolean allBrds,
            BoolFilterBuilder boolFilter) {

        boolean blFilter = CollectionUtils.isNotEmpty(brdIds);
        if (blFilter) {
            if (allBrds) {
                AndFilterBuilder andFilter = FilterBuilders.andFilter();
                for (String brdId : brdIds) {
                    andFilter.add(FilterBuilders.orFilter(
                            FilterBuilders.termFilter(ConstantsGlobal.BOARDS_ID, brdId),
                            FilterBuilders.termFilter(ConstantsGlobal.TARGETS_ID, brdId)));
                }
                boolFilter.must(andFilter);
            } else {
                boolFilter.must(FilterBuilders.orFilter(
                        FilterBuilders.inFilter(ConstantsGlobal.BOARDS_ID, brdIds.toArray()),
                        FilterBuilders.inFilter(ConstantsGlobal.TARGETS_ID, brdIds.toArray())));
            }
        }
        return blFilter;
    }

    public static void addFollowingQueryFilter(BoolQueryBuilder boolQuery, String userId) {

        String childType = UserActionType.FOLLOWING.getSearchIndexType();
        boolQuery.must(QueryBuilders.hasChildQuery(childType,
                QueryBuilders.fieldQuery(childType + "." + ConstantsGlobal.USER_ID, userId)));
    }

    public static AbstractFacetBuilder facetBuilderFromSource(String facetName, String fieldName,
            int size) {

        return facetBuilderFromSource(facetName, fieldName, null, size);
    }

    public static AbstractFacetBuilder facetBuilderFromSource(String facetName, String fieldName,
            Collection<String> excludeValues, int size) {

        TermsFacetBuilder termFacet = FacetBuilders.termsFacet(facetName)
                .scriptField("_source." + fieldName).size(size);
        if (CollectionUtils.isNotEmpty(excludeValues)) {
            termFacet.exclude(excludeValues.toArray());
        }
        return termFacet;
    }

    public static Set<String> getFacetSet(SearchResponse searchResponse, String facetName) {

        Set<String> facetSet = new HashSet<String>();
        facetSet.addAll(getFacetMap(searchResponse, facetName).keySet());
        LOGGER.info("faceSet result : " + facetSet);
        return facetSet;
    }

    public static Map<String, Map<String, Integer>> getMultipleFacetMap(
            SearchResponse searchResponse) {

        Map<String, Map<String, Integer>> namedFacetMap = new LinkedHashMap<String, Map<String, Integer>>();
        if (searchResponse != null) {
            Facets facets = searchResponse.facets();
            if (facets != null) {
                for (Facet facet : facets.facets()) {
                    Map<String, Integer> facetMap = new LinkedHashMap<String, Integer>();
                    if (facet instanceof TermsFacet) {
                        TermsFacet termFacet = (TermsFacet) facet;
                        LOGGER.info("facet is temr facet: " + termFacet.getName()
                                + " missingCount: " + termFacet.getMissingCount() + " otherCount: "
                                + termFacet.otherCount() + " totalCount: "
                                + termFacet.getTotalCount());
                        for (Entry entry : termFacet.getEntries()) {
                            LOGGER.info("facet term: " + entry.getTerm());
                            facetMap.put(entry.getTerm(), entry.getCount());
                        }
                    } else if (facet instanceof StatisticalFacet) {
                        StatisticalFacet statFacet = (StatisticalFacet) facet;
                        LOGGER.info("facet is statistical facet total : " + statFacet.getTotal());
                        facetMap.put("total", (int) statFacet.getTotal());
                    }
                    namedFacetMap.put(facet.name(), facetMap);
                }
            }
            LOGGER.info("facetMap map  result : " + namedFacetMap);
        } else {
            LOGGER.error("es response is null ");
        }
        return namedFacetMap;
    }

    public static Map<String, Integer> getFacetMap(SearchResponse searchResponse, String facetName) {

        Map<String, Map<String, Integer>> multiFacetMap = getMultipleFacetMap(searchResponse);
        Map<String, Integer> facetMap = multiFacetMap.get(facetName);
        return facetMap != null ? facetMap : new HashMap<String, Integer>();
    }

    @SuppressWarnings("unchecked")
    public static void addMappingToES(String indexName, String indexType,
            IRelationshipSearchDetails relationshipSearchDetails, String parentId,
            boolean checkIfPresent) {

        SearchHit searchHit = checkIfPresent ? findOne(indexName, indexType,
                relationshipSearchDetails._getEsQuery()) : null;
        ElasticSearchManager es = ElasticSearchManager.getInstance();
        LOGGER.info("Mapping parent Id : " + parentId);
        if (searchHit == null) {
            LOGGER.info("adding object to es : " + relationshipSearchDetails);
            String id = es.addIndex(indexName, indexType,
                    ObjectMapperUtils.convertValue(relationshipSearchDetails, Map.class), parentId);
            LOGGER.info("successfully added mapping to es rspId: " + id);
        } else {
            es.reIndex(searchHit.getIndex(),
                    ObjectMapperUtils.convertValue(relationshipSearchDetails, Map.class),
                    searchHit.getId(), searchHit.getType(), parentId);
            LOGGER.info("search hits resposne : " + searchHit.getSource());
        }
    }

    // indexType-->nodeName
    public static SearchHit findOne(String indexName, String indexType, QueryBuilder esQuery) {

        SearchResponse searchResponse = null;
        try {
            searchResponse = getSearchResponse(esQuery, NO_START, NO_LIMIT, indexName, indexType,
                    null);
        } catch (Exception ex) {
            LOGGER.error("Failure while querying to es :" + ex.getMessage(), ex);
            return null;
        }
        if (searchResponse == null || searchResponse.getHits().getTotalHits() == 0) {
            LOGGER.error("no search hit found for [index: " + indexName + ", indexType: "
                    + indexType + "], esQuery: " + esQuery);
            return null;
        }
        SearchHit searchHit = searchResponse.getHits().getHits()[0];
        return searchHit;
    }

	public static int getQueryCount(QueryBuilder esQuery, String indexName,
			String indexType) {
		ElasticSearchManager es = ElasticSearchManager.getInstance();
		Client client = es.getClient();
		CountRequestBuilder countReq;
		countReq = client.prepareCount(indexName).setQuery(esQuery)
				.setTypes(indexType.trim());
		int totalCount = (int) countReq.execute().actionGet().count();
		LOGGER.debug("getContents totalCount" + totalCount);
		es.closeClient(client);
		return totalCount;
	}
}
