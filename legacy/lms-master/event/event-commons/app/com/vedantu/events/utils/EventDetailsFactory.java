package com.vedantu.events.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;
import play.api.Play;

import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.events.apis.EventRegistrar;
import com.vedantu.commons.events.apis.IEventDetails;
import com.vedantu.events.errors.DetailsNotFoundException;
import com.vedantu.events.errors.TypeNotMatchedException;

public class EventDetailsFactory {
	private static final ALogger					LOGGER	= Logger.of(EventDetailsFactory.class);
	private static EventDetailsFactory				instance;
	private Map<EventType, Class<IEventDetails>>	detailsMap;

	@SuppressWarnings("unchecked")
	private EventDetailsFactory() throws ClassNotFoundException,
			NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		
		detailsMap = new HashMap<EventType, Class<IEventDetails>>();
		for (EventType eventType : EventType.values()) {
			if (EventRegistrar.INSTANCE.size() == 0
					|| EventRegistrar.INSTANCE.contains(eventType)) {
				String detailsClassName = eventType.getEventDetailsClass();
				if (StringUtils.isNotEmpty(detailsClassName)) {
					LOGGER.debug("loading class:" + detailsClassName
							+ ", for eventType: " + eventType);
					Class<?> detailsClass = Class.forName(detailsClassName
							.trim());

					detailsMap.put(eventType,
							(Class<IEventDetails>) detailsClass);
				} else {
					LOGGER.warn("no detailsClass defined for : " + eventType);
				}
			} else {
				LOGGER.warn("Can not load event type as its not registered explicitly "
						+ eventType);
				Logger.warn("Can not load event type as its not registered explicitly "
						+ eventType);
			}

		}
	}

	public static EventDetailsFactory getInstance() {
		if (instance == null) {
			createInstance();
		}
		return instance;
	}

	private static synchronized void createInstance() {
		if (instance == null) {
			try {
				instance = new EventDetailsFactory();
			} catch (Exception e) {
				LOGGER.error("couldn't load event details factory factory", e);
				Play.stop();
			}
		}
	}

	public IEventDetails getDetails(EventType type)
			throws TypeNotMatchedException, DetailsNotFoundException {

		IEventDetails details = null;
		if (!EventType.isValidEventType(type.name())) {
			throw new TypeNotMatchedException();
		}

		if (detailsMap.get(type) == null) {
			throw new DetailsNotFoundException();
		}

		Class<IEventDetails> clazz = detailsMap.get(type);

		try {
			details = clazz.newInstance();
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw new DetailsNotFoundException();
		}
		return details;
	}

	public boolean verify(EventType type, Class<?> detailsClazz) {
		LOGGER.debug("Type " + type + " Class Name "+ detailsClazz.getName());
		
		LOGGER.debug("Registers class " + detailsMap.get(type) + " details class assignable from registered class "+ detailsClazz.isAssignableFrom(detailsMap.get(type))  );
		return detailsMap.get(type) != null
				&& detailsMap.get(type).isAssignableFrom(detailsClazz );

	}

}
