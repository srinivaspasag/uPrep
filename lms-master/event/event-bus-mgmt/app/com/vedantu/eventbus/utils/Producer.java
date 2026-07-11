package com.vedantu.eventbus.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import play.Logger;
import play.Logger.ALogger;
import play.Play;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.ObjectMapperUtils;
import com.vedantu.eventbus.events.Events;
import com.vedantu.events.models.Event;
import com.vedantu.events.task.apis.ConsumableIterator;
import com.vedantu.events.task.apis.IProducer;
import com.vedantu.events.utils.EventUtil;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.MorphiaManager;

public class Producer implements IProducer<Events> {
	private static final ALogger LOGGER = Logger.of(Producer.class);
	private String collectionName;
	private int batchSize;
	private DB db;
	private static long DEFAULT_WAIT_TIME = 5 * 60 * 1000;
	private static int DEFAULT_BATCH_SIZE = 5;

	public Producer(String collectionName, int batchSize) {
		this.collectionName = collectionName;
		try {
			DEFAULT_WAIT_TIME = Long.parseLong(Play.application()
					.configuration().getString("reproduce.wait.time"));
		} catch (Exception e) {
			LOGGER.error(" == reproduce.wait.time ==  property not found ", e);
		}
		db = MorphiaManager.INSTANCE.getDS().getDB();
		if (batchSize < DEFAULT_BATCH_SIZE) {
			batchSize = DEFAULT_BATCH_SIZE;
		}
		this.batchSize = batchSize;
	}

	public Producer(EventType eventType, int batchSize) {
		this(EventUtil.getEventQueueName(eventType), batchSize);
	}

	@Override
	public ConsumableIterator<Events> produce() {
		Map<SrcEntity, Events> entityWiseEvents = new HashMap<SrcEntity, Events>();

		final long NOW = System.currentTimeMillis();

		boolean exist = db.collectionExists(collectionName);
		if (exist) {
			DBCollection coll = db.getCollection(collectionName);
			DBObject query = new BasicDBObject();
			query.put(ConstantsGlobal.TIME_CREATED, new BasicDBObject("$lt",
					NOW));
			// NOTE: below query is not used
			// we need to ensure that re-enqueued events are processed first
			// DONT uncomment
			// query.put("lastTriedTime",
			// new BasicDBObject("$lt",
			// (System.currentTimeMillis() - DEFULT_WAIT_TIME)));
			LOGGER.debug("Fetching number of events"+ batchSize);
			DBCursor cursor = coll
					.find(query)
					.sort(new BasicDBObject(ConstantsGlobal.TIME_CREATED,
							MongoManager.SortOrder.ASC.getValue()))
					.limit(batchSize);
			LOGGER.debug("[" + collectionName + "] producer query is : "
					+ query+ " cursor size" + cursor.size());
			if (null != cursor && cursor.size() > 0) {

				Set<SrcEntity> dontPickMore = new HashSet<SrcEntity>();

				while (cursor.hasNext()) {
					DBObject e = cursor.next();
					LOGGER.debug("event dbObject : " + e);
					Event event = ObjectMapperUtils.convertToVedantuBaseModel(
							e, Event.class);
					LOGGER.debug("event obj : " + event);
					if (dontPickMore.contains(event.srcEntity)) {
						continue;
					}
					if (event.timeCreated > NOW
							|| (event.nTries > 0 && (NOW - event.lastTriedTime) < DEFAULT_WAIT_TIME)) {
						dontPickMore.add(event.srcEntity);
						continue;
					}
					if (event.srcEntity != null
							&& !entityWiseEvents.containsKey(event.srcEntity)) {
						entityWiseEvents.put(event.srcEntity, new Events(
								event.srcEntity));
					}
					entityWiseEvents.get(event.srcEntity).add(event);
				}
				if (!dontPickMore.isEmpty()) {
					LOGGER.debug("dontPickMore : " + dontPickMore);
				}
			} else {
				LOGGER.debug("null cursor");
			}

		}
		return new ConsumableIterator<Events>(entityWiseEvents.values());
	}
}