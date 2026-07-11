package com.vedantu.events.models;

import java.util.HashSet;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.annotations.Entity;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.commons.events.apis.IEventDetails;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.events.errors.DetailsNotFoundException;
import com.vedantu.events.errors.TypeNotMatchedException;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.utils.EventDetailsFactory;
import com.vedantu.mongo.VedantuBaseMongoModel;

@Entity(value = "events", noClassnameStored = true)
public class Event extends VedantuBaseMongoModel implements IConsumable {

	private static final ALogger LOGGER = Logger.of(Event.class);

	private EventType type;
	private String source;
	public String info;
	private String userId;
	public SrcEntity srcEntity;
	public int nTries;
	public Set<String> processedBy;
	public long lastTriedTime;
	public EventActionType action;

	public Event() {

	}

	private Event(EventType type, String source, String userId,
			SrcEntity srcEntity) {
		super();
		this.type = type;
		this.source = source;
		this.userId = userId;
		if (srcEntity == null) {
			this.srcEntity = new SrcEntity(EntityType.USER, userId);
		} else {
			this.srcEntity = srcEntity;
		}
		this.processedBy = new HashSet<String>();
	}

	public Event(EventType type, String source, String userId,
			IEventDetails eventDetails, SrcEntity srcEntity)
			throws JSONException {
		this(type, source, userId, srcEntity);
		if (eventDetails != null) {

			if (!EventDetailsFactory.getInstance().verify(type,
					eventDetails.getClass())) {
				throw new IllegalArgumentException("unsupported class : "
						+ eventDetails.getClass() + " for eventType : " + type);
			}

		}
		this.info = eventDetails.toJSON().toString();
		LOGGER.debug("event info is : " + this.info);
		if (eventDetails.__getSrcEntity() != null) {
			this.srcEntity = eventDetails.__getSrcEntity();
		} else {
			this.srcEntity = new SrcEntity(EntityType.USER, userId);
		}
	}

	public EventType getType() {
		return type;
	}

	public void setType(EventType type) {
		this.type = type;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public JSONObject _getInfo() {
		try {
			return new JSONObject(info);
		} catch (JSONException e) {
		}
		return null;
	}

	public void _setInfo(JSONObject info) {
		this.info = info.toString();
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public IEventDetails fetchEventDetails() {
		IEventDetails details = null;

		try {
			details = EventDetailsFactory.getInstance().getDetails(type);
			details.fromJSON(new JSONObject(info));
		} catch (JSONException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (SecurityException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (IllegalArgumentException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (TypeNotMatchedException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (DetailsNotFoundException e) {
			LOGGER.error(e.getMessage(), e);
		}

		return details;
	}

	@Override
	public String _getConsumableId() {
		return this._getStringId();
	}

	@Override
	public Set<String> _getProcessedBy() {
		return processedBy;
	}

	@Override
	public void addProcessedBy(String processor) {
		if (processedBy == null) {
			processedBy = new HashSet<String>();
		}
		if (processor != null) {
			processedBy.add(processor);
		}
	}

	@Override
	public String toString() {
		return "Event [type=" + type + ", source=" + source + ", info=" + info
				+ ", userId=" + userId + ", srcEntity=" + srcEntity
				+ ", nTries=" + nTries + ", processedBy=" + processedBy
				+ ", lastTriedTime=" + lastTriedTime + ", action=" + action
				+ ", id=" + id + ", timeCreated=" + timeCreated
				+ ", lastUpdated=" + lastUpdated + "]";
	}

}