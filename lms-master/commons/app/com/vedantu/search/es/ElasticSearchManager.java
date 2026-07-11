package com.vedantu.search.es;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequest;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse.AnalyzeToken;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.count.CountRequest;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.deletebyquery.DeleteByQueryRequest;
import org.elasticsearch.action.deletebyquery.DeleteByQueryResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsException;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.indices.IndexMissingException;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.facet.Facet;
import org.elasticsearch.search.facet.FacetBuilders;
import org.elasticsearch.search.facet.Facets;
import org.elasticsearch.search.facet.terms.TermsFacet;
import org.elasticsearch.search.facet.terms.TermsFacet.ComparatorType;
import org.elasticsearch.search.facet.terms.TermsFacet.Entry;
import org.elasticsearch.search.facet.terms.TermsFacetBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.Logger.ALogger;
import play.Play;

public class ElasticSearchManager {

    private static final ALogger        LOGGER              = Logger.of(ElasticSearchManager.class);
    private static ElasticSearchManager instance;
    // private static Node node;
    private static TransportClient      client;
    public static int                   DEFAULT_RESULT_SIZE = 10;
    private static int                  PAGE_NUMBER         = 0;
    public final static String          TYPE                = "type";
    public final static String          ID                  = "id";
    public final static String          SOURCE              = "source";
    public final static String          VERSION             = "version";
    public final static String          TOTAL_RESULTS       = "totalResults";

    private ElasticSearchManager() {

        String clusterName = Play.application().configuration()
                .getString("elasticsearch.cluster.name", "elasticsearch");
        Set<InetSocketTransportAddress> transportAddresses = new HashSet<InetSocketTransportAddress>();

        // TODO:change this reading method
        List<String> hosts = Play.application().configuration()
                .getStringList("elasticsearch.transport.host", Arrays.asList("127.0.0.1:9300"));
        for (String esHost : hosts) {
            LOGGER.info("elasticsearch host : " + esHost);
            String[] hostParams = StringUtils.split(esHost, ":");
            if (hostParams.length == 2) {
                LOGGER.debug("adding elasticsearch host details [host:" + hostParams[0]
                        + ", port: " + hostParams[1] + "]");
                transportAddresses.add(new InetSocketTransportAddress(hostParams[0], Integer
                        .parseInt(hostParams[1])));
            }
        }
        Builder builder = ImmutableSettings.settingsBuilder();
        builder.put("cluster.name", clusterName);
        builder.put("client.transport.sniff", false);
        builder.put("client.transport.ping_timeout", 60000);
        Settings s = builder.build();
        TransportClient tmp = new TransportClient(s);
        LOGGER.debug("adding  es TransportClient client");
        for (InetSocketTransportAddress taransportAddress : transportAddresses) {
            tmp.addTransportAddress(taransportAddress);
        }
        LOGGER.info("transport client connected to es : " + tmp.connectedNodes());
        client = tmp;
    }

    public static ElasticSearchManager getInstance() {
        LOGGER.debug("Came inside ElasticSearchManager getInstance");
        if (instance == null) {
            LOGGER.debug("Came inside ElasticSearchManager getInstance - null");
            createInstance();
        }
        return instance;
    }

    private static synchronized void createInstance() {

        if (instance == null) {
            LOGGER.debug("creating a new instance of ElasticSearch Handler");
            instance = new ElasticSearchManager();
        }
    }

    public Client getClient() {

        return client;
    }

    public void closeClient(Client client) {

        if (null != client) {
            // client.close();
            LOGGER.debug("ES client, need not be closed");
        } else {
            LOGGER.debug("ES cannot close null client");
        }
    }

    public String addIndex(String indexName, String nodeName, Map<String, Object> objectMap) {

        return addIndex(indexName, nodeName, objectMap, null);
    }

    public String addIndex(String indexName, String nodeName, String objectMap, String parentId) {

        if (StringUtils.isEmpty(indexName) || StringUtils.isEmpty(nodeName) || objectMap == null) {
            LOGGER.info("indexName or nodeName or Object is empty");
            return null;
        }
        indexName = indexName.trim();
        Client client = getClient();
        IndexResponse response = null;
        try {
            LOGGER.info("adding object : " + objectMap + " to ES");
            IndexRequestBuilder indexRequest = client.prepareIndex(indexName, nodeName).setSource(
                    objectMap);
            if (StringUtils.isNotEmpty(parentId)) {
                indexRequest.setParent(parentId);
            }
            response = indexRequest.execute().actionGet();
            LOGGER.info("refreshing index : " + indexRequest.request().refresh(true).refresh());
        } catch (Exception e) {

            LOGGER.error(e.getMessage(), e);

            synchronized (indexName.intern()) {
                IndicesExistsResponse r = client.admin().indices()
                        .exists(new IndicesExistsRequest(indexName)).actionGet();
                if (!r.exists()) {
                    LOGGER.error("index " + indexName + " does not exist, creating a new index "
                            + indexName);
                    // TODO: Add analyzers (fetch from factory based on
                    // indexName)
                    try {
                        XContentBuilder analyzerQuery = XContentFactory.jsonBuilder().startObject()
                                .startObject("analysis").startObject("analyzer")
                                .startObject("synonym").field("tokenizer", "whitespace")
                                .array("filter", new String[] { "synonym" }).endObject()
                                .endObject().startObject("filter").startObject("    ")
                                .field("type", "synonym")
                                .field("synonyms_path", "analysis/synonym.txt").endObject()
                                .endObject().endObject().endObject();

                        CreateIndexResponse rsp = client
                                .admin()
                                .indices()
                                .create(new CreateIndexRequest(indexName)
                                        .settings(ImmutableSettings.settingsBuilder()
                                                .loadFromSource(analyzerQuery.string())))
                                .actionGet();
                        LOGGER.info("indexed got created : " + rsp.acknowledged());
                        LOGGER.info("now indexing the object to ES");
                        IndexRequestBuilder indexRequest = client.prepareIndex(indexName, nodeName)
                                .setSource(objectMap);
                        if (StringUtils.isNotEmpty(parentId)) {
                            indexRequest.setParent(parentId);
                        }
                        response = indexRequest.execute().actionGet();
                    } catch (IOException ex) {
                        LOGGER.error(ex.getMessage(), ex);
                    }
                } else {
                    LOGGER.error("index " + indexName + " exist!! " + indexName);
                }
            }

        }
        LOGGER.info("*************** Field name is :" + nodeName + " *******************");

        closeClient(client);
        return null != response ? response.getId() : null;
    }

    public String addIndex(String indexName, String nodeName, Map<String, Object> objectMap,
            String parentId) {

        if (StringUtils.isEmpty(indexName) || StringUtils.isEmpty(nodeName) || objectMap == null) {
            LOGGER.info("indexName or nodeName or Object is empty");
            return null;
        }
        indexName = indexName.trim();
        Client client = getClient();
        IndexResponse response = null;
        try {
            LOGGER.info("adding object : " + objectMap + " to ES");
            IndexRequestBuilder indexRequest = client.prepareIndex(indexName, nodeName).setSource(
                    objectMap);
            if (StringUtils.isNotEmpty(parentId)) {
                indexRequest.setParent(parentId);
            }
            response = indexRequest.execute().actionGet();
            LOGGER.info("refreshing index : " + indexRequest.request().refresh(true).refresh());
        } catch (Exception e) {

            LOGGER.error(e.getMessage(), e);

            synchronized (indexName.intern()) {
                IndicesExistsResponse r = client.admin().indices()
                        .exists(new IndicesExistsRequest(indexName)).actionGet();
                if (!r.exists()) {
                    LOGGER.error("index " + indexName + " does not exist, creating a new index "
                            + indexName);
                    // TODO: Add analyzers (fetch from factory based on
                    // indexName)
                    try {
                        XContentBuilder analyzerQuery = XContentFactory.jsonBuilder().startObject()
                                .startObject("analysis").startObject("analyzer")
                                .startObject("synonym").field("tokenizer", "whitespace")
                                .array("filter", new String[] { "synonym" }).endObject()
                                .endObject().startObject("filter").startObject("	")
                                .field("type", "synonym")
                                .field("synonyms_path", "analysis/synonym.txt").endObject()
                                .endObject().endObject().endObject();

                        CreateIndexResponse rsp = client
                                .admin()
                                .indices()
                                .create(new CreateIndexRequest(indexName)
                                        .settings(ImmutableSettings.settingsBuilder()
                                                .loadFromSource(analyzerQuery.string())))
                                .actionGet();
                        LOGGER.info("indexed got created : " + rsp.acknowledged());
                        LOGGER.info("now indexing the object to ES");
                        IndexRequestBuilder indexRequest = client.prepareIndex(indexName, nodeName)
                                .setSource(objectMap);
                        if (StringUtils.isNotEmpty(parentId)) {
                            indexRequest.setParent(parentId);
                        }
                        response = indexRequest.execute().actionGet();
                    } catch (IOException ex) {
                        LOGGER.error(ex.getMessage(), ex);
                    }
                } else {
                    LOGGER.error("index " + indexName + " exist!! " + indexName);
                }
            }

        }
        LOGGER.info("*************** Field name is :" + nodeName + " *******************");

        closeClient(client);
        return null != response ? response.getId() : null;
    }

    public long getFecetsCount() {

        return 0;
    }

    public static long getFieldCount(String indexName, QueryBuilder countQuery) {

        ElasticSearchManager es = ElasticSearchManager.getInstance();
        Client client = es.getClient();
        CountRequest countRequest = new CountRequest(indexName);
        countRequest.query(countQuery);
        countRequest.minScore(CountRequest.DEFAULT_MIN_SCORE);
        LOGGER.info("requesting for count for index: " + indexName + " with query "
                + countQuery.toString());
        CountResponse countResponse = null;
        try {
            countResponse = client.count(countRequest).actionGet();
        } catch (Exception e) {
            LOGGER.error("index not found ", e);
        }
        long count = 0;
        if (countResponse == null) {
            LOGGER.error("null count response ");
            return count;
        }
        LOGGER.info("countResponse totalCount: " + countResponse.getCount());
        count = countResponse.count();
        es.closeClient(client);

        return count;
    }

    // returns the index id
    public JSONObject getUniqueIndexResult(String indexName, String nodeName, String fieldName,
            String uniqueFieldValue) {

        if (StringUtils.isEmpty(fieldName) || StringUtils.isEmpty(uniqueFieldValue)) {
            LOGGER.error("fieldName [" + fieldName + "], and uniqueFieldValue [ "
                    + uniqueFieldValue + "] can not be null");
            return null;
        }
        JSONObject searchResponse = null;
        Client client = getClient();
        try {
            SearchRequestBuilder searchRequest = client.prepareSearch(indexName)
                    .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                    .setQuery(QueryBuilders.fieldQuery(fieldName, uniqueFieldValue)).setSize(1)
                    .setExplain(true);
            if (StringUtils.isNotEmpty(nodeName)) {
                searchRequest.setTypes(nodeName);
            }
            SearchResponse response = searchRequest.execute().actionGet();
            if (response == null) {
                LOGGER.error("es response for query " + fieldName + "= " + uniqueFieldValue
                        + " is null");
            } else {

                SearchHits allHits = response.getHits();
                LOGGER.info("es response : time = " + response.getTookInMillis() + " ms, hits = "
                        + allHits.getTotalHits());
                SearchHit[] hits = allHits.getHits();
                if (null != hits && hits.length > 0) {
                    SearchHit hit = hits[0];

                    try {
                        searchResponse = new JSONObject();
                        searchResponse.put(ID, hit.id());
                        searchResponse.put(TYPE, hit.type());
                        searchResponse.put(VERSION, hit.version());
                        searchResponse.put(SOURCE, hit.sourceAsString());
                    } catch (JSONException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("index " + indexName + " not found ", e);
        }

        closeClient(client);
        return searchResponse;
    }

    // returns your the index id
    public List<JSONObject> getAllUniqueIndexResult(String indexName, String fieldName,
            Set<String> uniqueFieldValues) {

        List<JSONObject> searchResponseList = new ArrayList<JSONObject>();
        Client client = getClient();
        try {
            SearchResponse response = client.prepareSearch(indexName)
                    .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                    .setQuery(QueryBuilders.inQuery(fieldName, uniqueFieldValues.toArray()))
                    .setExplain(true).execute().actionGet();
            if (response == null) {
                LOGGER.error("es response for query " + fieldName + "= " + uniqueFieldValues
                        + " is null");
            } else {
                SearchHits allHits = response.getHits();
                LOGGER.info("es response : time = " + response.getTookInMillis() + " ms, hits = "
                        + allHits.getTotalHits());
                SearchHit[] hits = allHits.getHits();
                if (null != hits && hits.length > 0) {
                    for (SearchHit hit : hits) {
                        try {
                            JSONObject searchResponse = new JSONObject();

                            searchResponse.put(ID, hit.id());
                            searchResponse.put(TYPE, hit.type());
                            searchResponse.put(VERSION, hit.version());
                            searchResponse.put(SOURCE, hit.sourceAsString());
                            searchResponseList.add(searchResponse);
                        } catch (JSONException e) {
                            LOGGER.error(e.getMessage(), e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("index " + indexName + " not found ", e);
        }

        closeClient(client);
        return searchResponseList;
    }

    // this method is not used so far but it can be used to get high no of
    // results
    public JSONObject
            getAllIndexResult(String indexName, String fieldName, String uniqueFieldValue) {

        final int resultQuerySize = 100;
        final long timeValue = 60000;

        Client client = getClient();
        JSONObject searchResponse = new JSONObject();

        SearchResponse scrollSearchResponse = null;
        try {
            scrollSearchResponse = client.prepareSearch(indexName).setSearchType(SearchType.SCAN)
                    .setScroll(new TimeValue(timeValue))
                    .setQuery(QueryBuilders.fieldQuery(fieldName, uniqueFieldValue))
                    .setSize(resultQuerySize).setExplain(true).execute().actionGet();
        } catch (Exception e) {
            LOGGER.error("index " + indexName + " not found ", e);
            closeClient(client);
            return searchResponse;
        }

        while (true) {
            scrollSearchResponse = client.prepareSearchScroll(scrollSearchResponse.getScrollId())
                    .setScroll(new TimeValue(timeValue)).execute().actionGet();

            boolean hitsRead = false;

            SearchHits allHits = scrollSearchResponse.getHits();

            for (SearchHit hits : allHits.getHits()) {
                hitsRead = true;
                try {
                    searchResponse.put(ID, hits.id());
                    searchResponse.put(TYPE, hits.type());
                } catch (JSONException e) {
                    LOGGER.error(e.getLocalizedMessage());
                }
            }

            if (!hitsRead) {
                break;
            }
        }
        closeClient(client);
        return searchResponse;
    }

    public String reIndex(String indexName, Map<String, Object> objectMap, String id, String type) {

        return reIndex(indexName, objectMap, id, type, null);
    }

    public String reIndex(String indexName, Map<String, Object> objectMap, String id, String type,
            String parentId) {

        Client client = getClient();
        String response = null;
        try {
            IndexRequest indexReq = new IndexRequest(indexName, type, id);

            indexReq.source(objectMap);
            if (StringUtils.isNotEmpty(parentId)) {
                indexReq.parent(parentId);
            }

            response = client.index(indexReq).actionGet().getId();
            LOGGER.info("re-indexing for " + indexName + " and source " + objectMap + " response:"
                    + response);
        } catch (IndexMissingException e) {
            LOGGER.error("Index " + indexName + " missing  " + e);
        }
        closeClient(client);
        return response;
    }

    public boolean removeEntry(String indexName, String indexType, QueryBuilder esQuery) {
        LOGGER.debug("Inside ES remove entry");
        boolean result = false;
        DeleteByQueryRequest deleteQuery = new DeleteByQueryRequest();
        deleteQuery.indices(new String[] { indexName });
        deleteQuery.types(indexType);
        deleteQuery.query(esQuery);
        Client client = getClient();
        LOGGER.debug("BEFORE AWAIT");
        ActionFuture<DeleteByQueryResponse> deleteResponseFuture = client
                .deleteByQuery(deleteQuery);
        LOGGER.debug("AFTER AWAIT");
        DeleteByQueryResponse deleteResponse = deleteResponseFuture.actionGet();
        if (deleteResponse != null) {
            LOGGER.info("document in es deleted successfully esQuery : " + esQuery
                    + " from Index : " + indexName + ", indiesMap: " + deleteResponse.indices());
            result = true;
        }
        closeClient(client);
        return result;
    }

    public boolean removeEntry(String indexName, String indexType, String fieldName,
            String uniqueFieldValue) {

        QueryBuilder esQuery = QueryBuilders.fieldQuery(fieldName, uniqueFieldValue);
        return removeEntry(indexName, indexType, esQuery);
    }

    public List<JSONObject> querySearch(String indexName, QueryBuilder queryBuilder, int start,
            int size) {

        SearchResponse response = null;
        List<JSONObject> searchResponse = new ArrayList<JSONObject>();
        start = Math.max(0, start);
        if (size < 1) {
            size = DEFAULT_RESULT_SIZE;
        }
        Client client = getClient();
        try {
            LOGGER.info("query : " + queryBuilder.toString() + " for " + indexName + " and start :"
                    + start + " and size : " + size);
            response = client.prepareSearch(indexName)
                    .setSearchType(SearchType.DFS_QUERY_THEN_FETCH).setQuery(queryBuilder)
                    .setFrom(start).setSize(size).setExplain(true).execute().actionGet();
            if (response == null) {
                LOGGER.error("es response for query " + queryBuilder.toString() + " is null");
                return searchResponse;
            }
            SearchHits allHits = response.getHits();
            LOGGER.info("es response : time = " + response.getTookInMillis() + " ms, hits = "
                    + allHits.getTotalHits());

            for (SearchHit hits : allHits.getHits()) {
                JSONObject hit = new JSONObject(hits.sourceAsString());
                searchResponse.add(hit);
            }
            JSONObject totalResults = new JSONObject();
            totalResults.put(TOTAL_RESULTS, allHits.getTotalHits());
            searchResponse.add(totalResults);

        } catch (IndexMissingException e) {
            LOGGER.error("Index " + indexName + "missing  " + e);
        } catch (JSONException e) {
            LOGGER.error("error on casting the es string response to json ", e);
        }
        closeClient(client);
        LOGGER.info("query result in elasticsearch.java -> : " + searchResponse);
        return searchResponse;
    }

    public List<JSONObject> querySearch(String indexName, String query, int start, int size) {

        SearchResponse response = null;
        List<JSONObject> searchResponse = new ArrayList<JSONObject>();
        start = Math.max(0, start);
        if (size < 1) {
            size = DEFAULT_RESULT_SIZE;
        }
        Client client = getClient();
        try {
            LOGGER.info("query : " + query + " for " + indexName + " and start :" + start
                    + " and size : " + size);
            response = client.prepareSearch(indexName)
                    .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                    .setQuery(QueryBuilders.queryString(query)).setFrom(start).setSize(size)
                    .setExplain(true).execute().actionGet();
            if (response == null) {
                LOGGER.error("es response for query " + query + " is null");
                return searchResponse;
            }
            SearchHits allHits = response.getHits();
            LOGGER.info("es response : time = " + response.getTookInMillis() + " ms, hits = "
                    + allHits.getTotalHits());

            for (SearchHit hits : allHits.getHits()) {
                JSONObject hit = new JSONObject(hits.sourceAsString());
                searchResponse.add(hit);
            }
            JSONObject totalResults = new JSONObject();
            totalResults.put(TOTAL_RESULTS, allHits.getTotalHits());
            searchResponse.add(totalResults);

        } catch (IndexMissingException e) {
            LOGGER.error("Index " + indexName + "missing  " + e);
        } catch (JSONException e) {
            LOGGER.error("error on casting the es string response to json ", e);
        }
        closeClient(client);
        LOGGER.info("query result in elasticsearch.java -> : " + searchResponse);
        return searchResponse;
    }

    // fieldQuery and inQuery works similarly
    // insted of field query we can use termQuery, if we want to catch the
    // result
    public List<JSONObject> fieldSearch(String indexName, String fieldName, String query,
            int start, int size) {

        List<JSONObject> searchResponse = new ArrayList<JSONObject>();
        Client client = getClient();
        SearchResponse response = client.prepareSearch(indexName)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(QueryBuilders.fieldQuery(fieldName, query)).setFrom(start).setSize(size)
                .setExplain(true).execute().actionGet();
        if (response == null) {
            LOGGER.error("es response for query " + fieldName + " = " + query + " is null");
            return searchResponse;
        }
        SearchHits allHits = response.getHits();
        LOGGER.info("es response : time = " + response.getTookInMillis() + " ms, hits = "
                + allHits.getTotalHits());

        for (SearchHit hits : allHits.getHits()) {
            LOGGER.info("search hits info is : " + hits.sourceAsString());
            try {
                searchResponse.add(new JSONObject(hits.sourceAsString()));
            } catch (JSONException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        closeClient(client);
        return searchResponse;
    }

    // for searching a patter in the index which starts with the input prefix

    public List<String> prefixSearch(String indexName, String fieldName, String query) {

        List<String> searchResponse = new ArrayList<String>();
        Client client = getClient();
        SearchResponse response = client.prepareSearch(indexName)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(QueryBuilders.prefixQuery(fieldName, query))
                .setFrom(PAGE_NUMBER * DEFAULT_RESULT_SIZE).setSize(DEFAULT_RESULT_SIZE)
                .setExplain(true).execute().actionGet();
        if (response == null) {
            LOGGER.error("es response for query " + fieldName + " = " + query + " is null");
            return searchResponse;
        }
        SearchHits allHits = response.getHits();
        LOGGER.info("es response : time = " + response.getTookInMillis() + " ms, hits = "
                + allHits.getTotalHits());

        for (SearchHit hits : allHits.getHits()) {
            searchResponse.add(hits.sourceAsString());
        }
        closeClient(client);
        return searchResponse;
    }

    public List<JSONObject> filteredSearchForFields(String indexName, String fieldName,
            String filterBy, String valueToFilter, String query) {

        List<JSONObject> searchResponse = new ArrayList<JSONObject>();
        SearchResponse response = null;
        Client client = getClient();
        try {
            response = client
                    .prepareSearch(indexName)
                    .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                    .setQuery(
                            QueryBuilders.filteredQuery(
                                    QueryBuilders.prefixQuery(fieldName, query),
                                    FilterBuilders.inFilter(filterBy, valueToFilter)))
                    .setFrom(PAGE_NUMBER * DEFAULT_RESULT_SIZE).setSize(DEFAULT_RESULT_SIZE)
                    .setExplain(true).execute().actionGet();
        } catch (Exception e) {
            LOGGER.error("index not found or query is not valid", e);
        }
        if (response == null) {
            LOGGER.error("es response for query " + fieldName + " = " + query + " is null");
            return searchResponse;
        }

        SearchHits allHits = response.getHits();
        LOGGER.info("es response : time = " + response.getTookInMillis() + " ms, hits = "
                + allHits.getTotalHits());
        try {
            for (SearchHit hits : allHits.getHits()) {
                JSONObject j = null;

                j = new JSONObject(hits.sourceAsString());
                searchResponse.add(j);
            }
            JSONObject totalResults = new JSONObject();
            totalResults.put(TOTAL_RESULTS, allHits.getTotalHits());
            searchResponse.add(totalResults);
        } catch (JSONException e) {
            LOGGER.error("error in casting the search response to json", e);
        }
        closeClient(client);
        return searchResponse;
    }

    public List<JSONObject> filteredSearch(String indexName, String filterBy, String valueToFilter,
            String query, int start) {

        List<JSONObject> searchResponse = new ArrayList<JSONObject>();
        if (start < 0) {
            start = 0;
        }
        SearchResponse response = null;
        Client client = getClient();
        try {
            response = client
                    .prepareSearch(indexName)
                    .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                    .setQuery(
                            QueryBuilders.filteredQuery(QueryBuilders.queryString(query),
                                    FilterBuilders.inFilter(filterBy, valueToFilter).cache(true)))
                    .setFrom(start).setSize(DEFAULT_RESULT_SIZE).setExplain(true).execute()
                    .actionGet();
        } catch (Exception e) {
            LOGGER.error("Index  not found or query is not valid", e);
        }
        if (response == null) {
            LOGGER.error("es response for query: " + query + " is null");
            return searchResponse;
        }
        SearchHits allHits = response.getHits();
        LOGGER.info("es response : time = " + response.getTookInMillis() + " ms, hits = "
                + allHits.getTotalHits());
        try {
            for (SearchHit hits : allHits.getHits()) {
                JSONObject json = null;

                json = new JSONObject(hits.sourceAsString());
                searchResponse.add(json);
            }
            JSONObject totalResults = new JSONObject();
            totalResults.put(TOTAL_RESULTS, allHits.getTotalHits());
            searchResponse.add(totalResults);
        } catch (JSONException e) {
            LOGGER.error("error on casting the es response to json", e);
        }

        closeClient(client);
        return searchResponse;
    }

    public static List<String> getTrendingSearches(int size) {

        ElasticSearchManager es = ElasticSearchManager.getInstance();
        Client client = es.getClient();
        TermsFacetBuilder termQueryFacet = FacetBuilders.termsFacet("trendingSearches")
                .fields("query").order(ComparatorType.REVERSE_COUNT);

        SearchResponse response = null;
        if (termQueryFacet != null) {
            try {
                response = client.prepareSearch("trendingSearches")
                        // remove this hardcoding
                        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH).addFacet(termQueryFacet)
                        .setSize(size).setExplain(true).execute().actionGet();
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        List<String> trendingSearches = new ArrayList<String>();
        if (response != null) {
            Facets facets = response.facets();
            if (facets != null) {
                for (Facet facet : facets.facets()) {
                    if (facet instanceof TermsFacet) {
                        TermsFacet termFacet = (TermsFacet) facet;
                        for (Entry entry : termFacet.getEntries()) {
                            trendingSearches.add(entry.getTerm());
                            LOGGER.info(entry.getTerm() + " replys " + entry.getCount());
                        }
                    }
                }
            }
        } else {
            LOGGER.info("trending search es response is null");
        }
        es.closeClient(client);
        return trendingSearches;
    }

    public static void main(String[] args) throws IOException {

        ElasticSearchManager es = ElasticSearchManager.getInstance();
        Client client = es.getClient();

        // testMain1(client);
        String indexName = "synonym_analyzer_test";
        String analyzerName = "synonym_analyzer";
        String bodyText = "shako";
        boolean preferLocal = true;

        try {
            // testMainCreateIndex(client);
            testAnalyzeApi(indexName, bodyText, analyzerName, preferLocal, client);
            synonymTestMainCreateIndex(client);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void synonymTestMainCreateIndex(Client client) throws SettingsException,
            IOException {

        // XContentBuilder analyzerQuery = XContentFactory.jsonBuilder()
        // .startObject().startObject("index").startObject("analysis")
        // .startObject("analyzer").startObject("synonym_analyzer")
        // .field("type", "custom").field("tokenizer", "whitespace")
        // .array("filter", new String[] { "stop", "synonym_filter" })
        // .endObject().endObject().startObject("filter")
        // .startObject("synonym_filter").field("type", "synonym")
        // .field("synonyms_path", "analysis/synonym.txt").endObject()
        // .endObject().endObject().endObject().endObject();

        /**
         * synonyms_path is relative to config file location
         **/
        XContentBuilder mappingQuery = XContentFactory.jsonBuilder().startObject()
                .startObject("lastname").startObject("properties").startObject("lastname")
                .field("type", "string").field("analyzer", "synonym_analyzer").field("boost", 10)
                .endObject().endObject().endObject().endObject();
        System.out.println("creating synonym analyzer test index");
        // System.out.println("analyzer setting query is : "
        // + analyzerQuery.string());
        System.out.println("analyzer mapping query is : " + mappingQuery.string());
        CreateIndexResponse cResp = client
                .admin()
                .indices()
                .prepareCreate("synonym_analyzer_test")
                .setSettings(
                        ImmutableSettings
                                .settingsBuilder()
                                .loadFromUrl(
                                        new File(
                                                "/home/shankar/reading/es_analyzer/synonyms_analyzer_settings.txt")
                                                .toURI().toURL()))
                .addMapping("lastname", mappingQuery.string()).execute().actionGet();

        System.out.println("index created : " + cResp.acknowledged() + ", and rsp is : "
                + cResp.toString());

    }

    public static void testMainCreateIndex(Client client) throws SettingsException, IOException {

        XContentBuilder analyzerQuery = XContentFactory.jsonBuilder().startObject()
                .startObject("index").startObject("analysis").startObject("analyzer")
                .startObject("my_analyzer").field("type", "custom")
                .field("tokenizer", "whitespace")
                .array("filter", new String[] { "stop", "my_ngram" }).endObject().endObject()
                .startObject("filter").startObject("my_ngram").field("type", "nGram")
                .field("min_gram", 3).field("max_gram", 5).endObject().endObject().endObject()
                .endObject().endObject();
        XContentBuilder mappingQuery = XContentFactory.jsonBuilder().startObject()
                .startObject("ngramField").startObject("properties").startObject("firstname")
                .field("type", "string").field("analyzer", "my_analyzer").field("boost", 10)
                .endObject().endObject().endObject().endObject();
        System.out.println("analyzer setting query is : " + analyzerQuery.string());
        System.out.println("analyzer mapping query is : " + mappingQuery.string());
        CreateIndexResponse cResp = client
                .admin()
                .indices()
                .prepareCreate("token_test")
                .setSettings(
                        ImmutableSettings.settingsBuilder().loadFromSource(analyzerQuery.string()))
                .addMapping("firstname", mappingQuery.string()).execute().actionGet();

        System.out.println("index created : " + cResp.acknowledged() + ", and rsp is : "
                + cResp.toString());
    }

    public static void testAnalyzeApi(String indexName, String bodyText, String analyzerName,
            boolean preferLocal, Client client) throws Exception {

        try {
            AnalyzeRequest analyzerRequest = new AnalyzeRequest(indexName, bodyText);
            analyzerRequest.analyzer(analyzerName);
            analyzerRequest.preferLocal(preferLocal);
            IndicesAdminClient indexClient = client.admin().indices();
            AnalyzeResponse analyzerResponse = indexClient.analyze(analyzerRequest).actionGet();
            for (AnalyzeToken token : analyzerResponse.getTokens()) {
                System.out.println("RESULT TOKEN  : " + token.getTerm());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e);
        }
    }

    public static void testMain1(Client client) {

        TermsFacetBuilder tf = new TermsFacetBuilder("replyCountFacet")
                .fields("uncategorizedTag")
                .facetFilter(
                        FilterBuilders.termsFilter("uncategorizedTag", "iit").cache(true)
                                .filterName("tagFilter")).size(20);
        // XContentBuilder facetQuery = XContentFactory.jsonBuilder()
        // .startObject().startObject("replyCountFacet")
        // .startObject("terms").field("field", "query").endObject()
        // .endObject().endObject();
        QueryBuilder query = QueryBuilders.filteredQuery(
                QueryBuilders.inQuery("uncategorizedTag", "roorkee", "iit"),
                FilterBuilders
                        .notFilter(
                                FilterBuilders.inFilter("docType", "note").cache(true)
                                        .filterName("tagFilter")).cache(true)
                        .filterName("docTagSuggestation"));
        SearchResponse response = client.prepareSearch("doctest")
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH).setQuery(query).addFacet(tf)
                .setExplain(true).execute().actionGet();
        System.out.println("Total hits : " + response.getHits().getTotalHits());

        if (response != null) {
            Facets facets = response.facets();
            if (facets != null) {
                for (Facet facet : facets.facets()) {
                    if (facet instanceof TermsFacet) {
                        TermsFacet termFacet = (TermsFacet) facet;
                        System.out.println("terms facet missing length "
                                + termFacet.getMissingCount() + ", entry length is "
                                + termFacet.getEntries().size());
                        for (Entry entry : termFacet.getEntries()) {
                            System.out.println(entry.getTerm() + " replys " + entry.getCount());
                        }
                    }
                }
            }
        }

        SearchHits allHits = response.getHits();
        System.out.println("total time taken : " + response.getTookInMillis());
        if (allHits != null) {
            for (SearchHit hits : allHits.getHits()) {
                System.out.println(hits.sourceAsString());

            }
        }
    }
}
