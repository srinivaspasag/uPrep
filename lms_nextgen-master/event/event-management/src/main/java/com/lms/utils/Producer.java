package com.lms.utils;

import com.lms.common.utils.EventEnqueueManager;
import com.lms.common.utils.EventUtil;
import com.lms.common.utils.ObjectMapperUtils;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.enums.EventType;
import com.lms.common.vedantu.event.api.ConsumableIterator;
import com.lms.common.vedantu.event.api.IProducer;
import com.lms.common.vedantu.mongo.Event;
import com.lms.events.Events;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class Producer implements IProducer<Events> {
    private static final Logger logger = LoggerFactory.getLogger(Producer.class);
    private String collectionName;
    private int batchSize;
    //private DB db;
    private static final long DEFAULT_WAIT_TIME = 5 * 60 * 1000;
    private static final int DEFAULT_BATCH_SIZE = 5;
    @Autowired
    private MongoTemplate mongoTemplate;
    private static final String TYPE = "type";
    private static final String SOURCE = "source";
    private static final String INFO = "info";
    private static final String USER_ID = "userId";
    private static final String SRC_ENTITY = "srcEntity";
    private static final String N_TRIES = "nTries";
    private static final String LAST_TRIED_TIME = "lastTriedTime";
    private static final String PROCESSED_BY = "processedBy";
    private static final String ACTION = "action";
	public void  producer(String collectionName, int batchSize) {
		this.collectionName = collectionName;
		try {
			//DEFAULT_WAIT_TIME = Long.parseLong(Play.application().configuration().getString("reproduce.wait.time"));
		} catch (Exception e) {
			logger.error(" == reproduce.wait.time ==  property not found ", e);
		}
		//db = MorphiaManager.INSTANCE.getDS().getDB();
		if (batchSize < DEFAULT_BATCH_SIZE) {
			batchSize = DEFAULT_BATCH_SIZE;
		}
		this.batchSize = batchSize;
	}

	public void producer(EventType eventType, int batchSize) {
		producer(EventUtil.getEventQueueName(eventType), batchSize);
	}

	@Override
	public ConsumableIterator<Events> produce() {
		Map<SrcEntity, Events> entityWiseEvents = new HashMap<SrcEntity, Events>();

		final long NOW = System.currentTimeMillis();
		boolean exist = mongoTemplate.collectionExists(collectionName);
		if (exist) {
			Query query = new Query();
			Criteria criteria = new Criteria();
			criteria.and(ConstantsGlobal.TIME_CREATED).lt(NOW);
			query.addCriteria(criteria);
			query.with(Sort.by(Sort.Direction.ASC, ConstantsGlobal.TIME_CREATED));
			Set<SrcEntity> dontPickMore = new HashSet<SrcEntity>();
			List<Document> documents = mongoTemplate.find(query, Document.class, collectionName);
			if (documents != null && documents.size() > 0) {
				for (Document document : documents) {

                    logger.debug("objectId : " + document.getObjectId("_id"));

                    Event event = ObjectMapperUtils.convertValue(document, Event.class);
                    if (dontPickMore.contains(event.srcEntity)) {
                        continue;
                    }
                    if (event.timeCreated > NOW
                            || (event.nTries > 0 && (NOW - event.lastTriedTime) < DEFAULT_WAIT_TIME)) {
                        dontPickMore.add(event.srcEntity);
                        continue;
                    }
					if (event.srcEntity != null && !entityWiseEvents.containsKey(event.srcEntity)) {
						entityWiseEvents.put(event.srcEntity, new Events(event.srcEntity));
					}
					entityWiseEvents.get(event.srcEntity).add(event);

				}
				if (!dontPickMore.isEmpty()) {
					logger.debug("dontPickMore : " + dontPickMore);
				}

			} else {
				logger.debug("null list");
			}
		}
		return new ConsumableIterator<Events>(entityWiseEvents.values());
	}

	public void testdocumentSave() {
        Map<SrcEntity, Events> entityWiseEvents = new HashMap<SrcEntity, Events>();
        final long NOW = System.currentTimeMillis();
        boolean exist = mongoTemplate.collectionExists("events_" + EventType.ADD_SOLUTION.name());
		/*if(!exist) {
			Event event = new Event();
			event.setLastUpdated(NOW);
			event.setRecordState(VedantuRecordState.ACTIVE);
			event.setSource("AAA");
			event.setType(EventType.ADD_SOLUTION);
			event.setTimeCreated(NOW);
			event.setUserId("BBBB");
			event.nTries = 4;
			event.action = UserActionType.EventActionType.ADD;
			event.processedBy = new HashSet<>();
			event.srcEntity = new SrcEntity(EntityType.SOLUTION, "SSSSSS");
			Document document = getEventDBObject(event);
			MongoCollection<Document> collection = mongoTemplate.createCollection("events_"+EventType.ADD_SOLUTION.name());
			collection.insertOne(document);
			//collection.updateOne(new Document("", ""), new Document());
		}*/
		if(exist) {
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.and(ConstantsGlobal.TIME_CREATED).lt(NOW);
		query.addCriteria(criteria);
		query.with(Sort.by(Sort.Direction.ASC, ConstantsGlobal.TIME_CREATED));
		Set<SrcEntity> dontPickMore = new HashSet<SrcEntity>();

		List<Document> documents = mongoTemplate.find(query, Document.class, "events_"+EventType.ADD_SOLUTION.name());
		for (Document document : documents) {
            logger.debug("objectId : " + document.getObjectId("_id"));
            Event event = ObjectMapperUtils.convertValue(document, Event.class);
            if (dontPickMore.contains(event.srcEntity)) {
                continue;
            }
            if (event.timeCreated > NOW
                    || (event.nTries > 0 && (NOW - event.lastTriedTime) < DEFAULT_WAIT_TIME)) {
                dontPickMore.add(event.srcEntity);
                continue;
            }
            if (event.srcEntity != null && !entityWiseEvents.containsKey(event.srcEntity)) {
				entityWiseEvents.put(event.srcEntity, new Events(event.srcEntity));
			}
			entityWiseEvents.get(event.srcEntity).add(event);
			
		}
		}
	}
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

}