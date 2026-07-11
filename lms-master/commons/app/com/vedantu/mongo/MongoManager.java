package com.vedantu.mongo;

import java.net.UnknownHostException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;
import play.Play;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class MongoManager {

    private static final ALogger LOGGER         = Logger.of(MongoManager.class);
    public static final int      INCLUDE_FIELD  = 1;
    public static final int      EXCLUDE_FIELD  = 0;
    public static final int      NO_START       = 0;
    public static final int      NO_LIMIT       = 0;
    public static final String   IN_QUERY       = "$in";
    public static final String   WHERE          = "$where";
    public static final String   SIZE           = "$size";
    public static final String   NIN_QUERY      = "$nin";
    public static final String   NE_QUERY       = "$ne";
    public static final String   PUSH_ALL_QUERY = "$pushAll";
    public static final String   PULL_ALL_QUERY = "$pullAll";
    public static final String   SLICE = "$slice";
    public static final String   PUSH_QUERY     = "$push";
    public static final String   PULL_QUERY     = "$pull";

    public enum SortOrder {
        ASC(1), DESC(-1);

        private int value;

        private SortOrder(int value) {

            this.value = value;
        }

        public int getValue() {

            return value;
        };

        public static SortOrder valueOfKey(String key) {

            SortOrder sortOrder = DESC;
            try {
                sortOrder = valueOf(key.trim().toUpperCase());
            } catch (Exception e) {}
            return sortOrder;
        }
    }


    public enum NumberUpdate {
        INCREMENT(1), DECREMENT(-1);

        private int value;

        private NumberUpdate(int value) {

            this.value = value;
        }

        public int getValue() {

            return value;
        };

        public static NumberUpdate valueOfKey(String key) {

            NumberUpdate sortOrder = DECREMENT;
            try {
                sortOrder = valueOf(key.trim().toUpperCase());
            } catch (Exception e) {}
            return sortOrder;
        }
    }



    public static final MongoManager INSTANCE = new MongoManager();

    private final Mongo              mongo;
    private final String             dbName;

    private MongoManager() {

        String host = Play.application().configuration().getString("mongodb.host");
        int port = Play.application().configuration().getInt("mongodb.port");
        String db = Play.application().configuration().getString("mongodb.dbname");

        LOGGER.debug("mongodb - host: " + host + ", port: " + port + ", db: " + db);

        try {
            mongo = new Mongo(host, port);
            dbName = db;
        } catch (UnknownHostException e) {
            throw new RuntimeException("unable to create Mongo instance", e);
        }
    }

    public Mongo getMongo() {

        return mongo;
    }

    public String getDBName() {

        return dbName;
    }

    public static DBObject getFieldsDBObject(List<String> fieldsList, int include_field) {

        if (fieldsList == null) {
            return null;
        }
        DBObject fields = new BasicDBObject();
        for (String fieldName : fieldsList) {
            fields.put(fieldName.trim(), include_field);
        }
        return fields;
    }

    public static DBObject getSortQuery(String orderBy, String sortOrder) {

        if (StringUtils.isEmpty(orderBy)) {
            return null;
        }
        return new BasicDBObject(orderBy, SortOrder.valueOfKey(sortOrder).getValue());
    }

}
