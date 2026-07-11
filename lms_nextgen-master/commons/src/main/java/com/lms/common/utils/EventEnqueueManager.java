package com.lms.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.enums.EventType;
import com.lms.common.vedantu.mongo.Event;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class EventEnqueueManager {

    private static final Logger logger = LoggerFactory.getLogger(EventEnqueueManager.class);

    private static ObjectMapper mapper;
    private static final String TYPE = "type";
    private static final String SOURCE = "source";
    private static final String INFO = "info";
    private static final String USER_ID = "userId";
    private static final String SRC_ENTITY = "srcEntity";
    private static final String N_TRIES = "nTries";
    private static final String LAST_TRIED_TIME = "lastTriedTime";
    private static final String PROCESSED_BY = "processedBy";
    private static final String ACTION = "action";
    @Value("${spring.data.mongodb.host}")
    private String host;
    @Value("${spring.data.mongodb.database}")
    private String database;
    @Value("${spring.data.mongodb.port}")
    private String port;
    private static final Set<String> collections = new HashSet<String>();
    @Autowired
    private MongoTemplate mongoTemplate;

    private static Document getEventDBObject(Event event) {

        Document eventDBObj = new Document();
        eventDBObj.put(TYPE, event.getType().name());
        eventDBObj.put(SOURCE, event.getSource());
        eventDBObj.put(ConstantsGlobal.TIME_CREATED, event.timeCreated);
	       /* if (event._getInfo() != null) {
	            eventDBObj.put(INFO, event._getInfo().toString());
	        }*/
        eventDBObj.put(USER_ID, event.getUserId());
        eventDBObj.put(SRC_ENTITY,
                EventEnqueueManager.mapper().convertValue(event.srcEntity, Map.class));
        eventDBObj.put(N_TRIES, event.nTries);
        eventDBObj.put(LAST_TRIED_TIME, event.lastTriedTime);
        eventDBObj.put(PROCESSED_BY, event.processedBy);
        if (event.action != null) {
            eventDBObj.put(ACTION, event.action.name());
        }
        return eventDBObj;
    }

    public String enqueue(Event event) throws UnknownHostException {
        return enqueueOrUpdate(event, null, false);
    }

    public static final String BUCKET_NAME_SUCCESS = "consumed_EVENTS";
    public static final String BUCKET_NAME_FAILURE = "failed_EVENTS";

    public String updateEvent(Event event) throws UnknownHostException {
        return enqueueOrUpdate(event, null, true);
    }

    public void addToSuccessBucket(Event event) throws UnknownHostException {
        enqueueOrUpdate(event, BUCKET_NAME_SUCCESS, false);
    }

    public void addToFailedBucket(Event event) throws UnknownHostException {
        enqueueOrUpdate(event, BUCKET_NAME_FAILURE, false);
    }

    private String enqueueOrUpdate(Event event, String collectionName,
                                   boolean isUpdate) throws UnknownHostException {
        EventType eventType = event.getType();
        if (!EventType.isValidEventType(eventType.name())) {
            logger.error("unknown event type : " + eventType);
            return null;
        }

        final String opType = (isUpdate ? "update" : "enqueue");
        logger.debug("trying to " + opType + " event");
        final String queueCollectionName = (collectionName != null && !collectionName.isEmpty()) ? collectionName : EventUtil
                .getEventQueueName(eventType);
        boolean present = collections.contains(queueCollectionName);
        Document eventDBObj = getEventDBObject(event);
        MongoCollection<Document> collection = null;
        if (!present) {
            present = createCollection(queueCollectionName, eventType, eventDBObj);
        }
        if (present) {
            collection = mongoTemplate.getCollection(queueCollectionName);
            if (isUpdate) {
                Document updateDocument = new Document("_id", event.id);
                //collection.updateOne(updateDocument, eventDBObj);
                collection.updateOne(updateDocument, eventDBObj);
            } else {
                collection.insertOne(eventDBObj);
            }
            logger.info(opType + " successful");

        }
        String eventId = eventDBObj.get(ConstantsGlobal._ID) != null ? eventDBObj
                .get(ConstantsGlobal._ID).toString() : null;
        logger.info("generated eventId is : " + eventId);
        return eventId;
    }

    private synchronized boolean createCollection(final String collectionName,
                                                  final EventType eventType, final Document eventDBObj) {
        boolean present = mongoTemplate.collectionExists(collectionName);
        if (!present) {
            logger.debug("creating collection " + collectionName + " for event ");
            try {
                mongoTemplate.createCollection(collectionName);
            } catch (Exception e) {
                logger.error("collection already exist", e);
            }
            present = mongoTemplate.collectionExists(collectionName);
        } else {
            logger.debug("collection present : " + collectionName);
        }
        collections.add(collectionName);
        return present;
    }
  /*  private static String enqueueOrUpdate(Event event, String collectionName,
                                          boolean isUpdate) throws UnknownHostException {
        EventType eventType = event.getType();
        if (!EventType.isValidEventType(eventType.name())) {
            logger.error("unknown event type : " + eventType);
            return null;
        }

        final String opType = (isUpdate ? "update" : "enqueue");
        logger.debug("trying to " + opType + " event");
        MongoClient mongoClient=new MongoClient("host", Integer.parseInt("port"));
        DB db=mongoClient.getDB("database");
        final String queueCollectionName = !(collectionName.isEmpty()) ? EventUtil
                .getEventQueueName(eventType) : collectionName;
        DBObject eventDBObj = getEventDBObject(event);
        DBCollection coll = null;
        boolean present = collections.contains(queueCollectionName);
        if (!present) {
            present = createCollection(db, queueCollectionName, eventType,
                    eventDBObj);
        }

        if (present) {
            coll = db.getCollection(queueCollectionName);
            if (isUpdate) {
                coll.update(new BasicDBObject("_id", event.id), eventDBObj);
            } else {
                coll.insert(eventDBObj);
            }
            logger.info(opType + " successful");
        }
        String eventId = eventDBObj.get(ConstantsGlobal._ID) != null ? eventDBObj
                .get(ConstantsGlobal._ID).toString() : null;
        logger.info("generated eventId is : " + eventId);
        db.requestDone();
        return eventId;
    }*/

   /* private synchronized static boolean createCollection(final DB db,
                                                         final String collectionName, final EventType eventType,
                                                         final DBObject eventDBObj) {
        boolean present = db.collectionExists(collectionName);
        if (!present) {
            logger.debug("creating collection " + collectionName
                    + " for event ");
            try {
                db.createCollection(collectionName, eventDBObj);
            } catch (Exception e) {
                logger.error("collection already exist", e);
            }
            present = db.collectionExists(collectionName);
        } else {
            logger.debug("collection present : " + collectionName);
        }
        if (!collections.contains(collectionName)) {
            collections.add(collectionName);
        }
        return present;
    }

    private static DBObject getEventDBObject(Event event) {

        DBObject eventDBObj = new BasicDBObject();
        eventDBObj.put(TYPE, event.getType().name());
        eventDBObj.put(SOURCE, event.getSource());
        eventDBObj.put(ConstantsGlobal.TIME_CREATED, event.timeCreated);
        if (event._getInfo() != null) {
            eventDBObj.put(INFO, event._getInfo().toString());
        }
        eventDBObj.put(USER_ID, event.getUserId());
        eventDBObj.put(SRC_ENTITY,
                mapper().convertValue(event.srcEntity, Map.class));
        eventDBObj.put(N_TRIES, event.nTries);
        eventDBObj.put(LAST_TRIED_TIME, event.lastTriedTime);
        eventDBObj.put(PROCESSED_BY, event.processedBy);
        if (event.action != null) {
            eventDBObj.put(ACTION, event.action.name());
        }
        return eventDBObj;
    }*/

    public static ObjectMapper mapper() {
        if (mapper == null) {
            mapper = new ObjectMapper();

        }
        return mapper;
    }
}