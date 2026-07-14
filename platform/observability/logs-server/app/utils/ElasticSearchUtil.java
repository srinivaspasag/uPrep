package utils;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import play.Logger;

/**
 * Created by Raghu Teja on 29-06-2017.
 */
public class ElasticSearchUtil {

    private static final Logger.ALogger LOGGER = Logger.of("ElasticSearchUtil");

    public static boolean indexExists(String index, TransportClient client) {
        IndicesAdminClient indices = client.admin().indices();
        IndicesExistsRequest request = new IndicesExistsRequest(index);
        IndicesExistsResponse response = indices.exists(request).actionGet();
        boolean exists = response.isExists();
        LOGGER.info("Index " + index + " exists?  " + exists);
        return exists;
    }

    public static void createIndex(String index, TransportClient client) {
        IndicesAdminClient indices = client.admin().indices();
        CreateIndexRequestBuilder builder = indices.prepareCreate(index);
        CreateIndexResponse response = builder.get();
        LOGGER.info("Created index {" + index + "} ? : " + response.isAcknowledged());
    }
}
