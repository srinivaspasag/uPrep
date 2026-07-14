package com.vedantu.events.task.apis;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.vedantu.events.models.Event;
import com.vedantu.mongo.MorphiaManager;

@SuppressWarnings("rawtypes")
public abstract class AbstractConsumer implements IConsumer {

	private static final ALogger LOGGER = Logger.of(AbstractConsumer.class);

	protected void preProcess(IConsumable consumable) {
		// do nothing
	}

	protected abstract void process(IConsumable consumable);

	protected void postProcess(IConsumable consumable) {
		// do nothing
	}

	@Override
	public void consume(IConsumable consumable) {
		preProcess(consumable);
		process(consumable);
		postProcess(consumable);
	}

	protected void remove(Event event, String collectionName) {
		MorphiaManager.INSTANCE.getDS().getDB().getCollection(collectionName);
		DB db = MorphiaManager.INSTANCE.getDS().getDB();
		db.requestStart();
		logInfo(event, "removing event : " + event._getStringId() + " form "
				+ collectionName);
		DBCollection coll = db.getCollection(collectionName);
		DBObject rmEvent = new BasicDBObject();
		rmEvent.put("_id", event.id);
		coll.remove(rmEvent);
		db.requestDone();
	}

	protected void logWarn(IConsumable consumable, String message) {
		String id = (null != consumable) ? consumable._getConsumableId()
				: StringUtils.EMPTY;
		LOGGER.warn("[" + id + "]" + message);
	}

	protected void logInfo(IConsumable consumable, String message) {
		String id = (null != consumable) ? consumable._getConsumableId()
				: StringUtils.EMPTY;
		LOGGER.info("[" + id + "]" + message);
	}

	protected void logDebug(IConsumable consumable, String message) {
		String id = (null != consumable) ? consumable._getConsumableId()
				: StringUtils.EMPTY;
		LOGGER.debug("[" + id + "]" + message);
	}

	protected void logError(IConsumable consumable, String message, Throwable t) {
		String id = (null != consumable) ? consumable._getConsumableId()
				: StringUtils.EMPTY;
		LOGGER.error("[" + id + "]" + message, t);
	}
}
