package com.vedantu.events.utils;

import org.json.JSONException;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.commons.events.apis.IEventDetails;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.events.models.Event;

public class EventUtil {
	private static final ALogger LOGGER = Logger.of(EventUtil.class);
	public static final String EVENTS = "events_";

	// public static void generateInstantEmailEvent(String email, String name,
	// String userId, ITemplateDetails templateDetails,
	// EmailTemplateType templateType, String requestUrl) {
	// InstantEmailDetails instantEmailDetails = new InstantEmailDetails();
	// if (!EmailValidator.getInstance().isValid(email)) {
	// LOGGER.error("Email for userId: " + userId
	// + " is invalid as : " + email);
	// return;
	// }
	// instantEmailDetails.emailId = email;
	// instantEmailDetails.name = name;
	// instantEmailDetails.emailTemplateType = templateType;
	// instantEmailDetails.templateDetails = templateDetails;
	// generateEvent(EventType.INSTANT_EMAIL, requestUrl, userId,
	// instantEmailDetails, null);
	// }
	//
	// public static void generateBulkEmailEvent(String email, String name,
	// String userId, ITemplateDetails templateDetails,
	// EmailTemplateType templateType, String requestUrl) {
	// generateBulkEmailEvent(email, name, userId, templateDetails,
	// templateType, requestUrl, null);
	// }
	//
	// public static void generateBulkEmailEvent(String email, String name,
	// String userId, ITemplateDetails templateDetails,
	// EmailTemplateType templateType, String requestUrl,
	// String mailToUserId) {
	// generateBulkEmailEvent(email, name, userId, templateDetails,
	// templateType, requestUrl, 0, mailToUserId);
	// }
	//
	// public static void generateBulkEmailEvent(String email, String name,
	// String userId, ITemplateDetails templateDetails,
	// EmailTemplateType templateType, String requestUrl,
	// long dispatchTime, String mailToUserId) {
	// BulkEmailDetails bulkEmailDetails = new BulkEmailDetails();
	// if (!EmailValidator.getInstance().isValid(email)) {
	// LOGGER.error("Email for userId: " + userId
	// + " is invalid as : " + email);
	// return;
	// }
	// bulkEmailDetails.emailId = email;
	// bulkEmailDetails.name = name;
	// bulkEmailDetails.emailTemplateType = templateType;
	// bulkEmailDetails.templateDetails = templateDetails;
	// bulkEmailDetails.userId = mailToUserId;
	// generateEvent(EventType.BULK_EMAIL, requestUrl, userId,
	// bulkEmailDetails, new SrcEntity(EntityType.USER, userId), null,
	// dispatchTime);
	//
	// }

	public static String getEventQueueName(EventType eventType) {
		if (null == eventType) {
			LOGGER.error("null eventType");
			throw new IllegalArgumentException("null eventType");
		}
		return new StringBuilder(EVENTS).append(eventType.name()).toString();
	}

	public static String generateEvent(EventType eventType, String requestUrl,
			String userId, IEventDetails details, SrcEntity srcEntity,
			EventActionType action) {
		return generateEvent(eventType, requestUrl, userId, details, srcEntity,
				action, 0);
	}

	public static String generateEvent(EventType eventType, String requestUrl,
			String userId, IEventDetails details, SrcEntity srcEntity) {
		return generateEvent(eventType, requestUrl, userId, details, srcEntity,
				null, 0);
	}

	public static String generateEvent(EventType eventType, String requestUrl,
			String userId, IEventDetails details, SrcEntity srcEntity,
			EventActionType action, long processTime) {
		String generatedEventId = null;
		try {
			if (eventType != null && userId != null) {
				LOGGER.debug("creating event for : " + eventType
						+ " for userId: " + userId + " and source url is : "
						+ requestUrl);
				Event event = new Event(eventType, requestUrl, userId, details,
						srcEntity);
				event.action = action;
				event.timeCreated = processTime != 0 ? processTime : System
						.currentTimeMillis();
				generatedEventId = EventEnqueueManager.enqueue(event);
				LOGGER.info("event[" + generatedEventId + "] got created for "
						+ eventType + " : for : " + userId + ", action:"
						+ action);
			} else {
				LOGGER.error("can not create event for empty userId or eventType");
			}
		} catch (JSONException e) {
			LOGGER.error("error on generating the event for : " + eventType, e);
		}
		return generatedEventId;
	}
}
